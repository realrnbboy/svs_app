package kr.co.signallink.svsv2.services;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmObject;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.command.ResponseCommandPacket;
import kr.co.signallink.svsv2.command.SendCommand;
import kr.co.signallink.svsv2.command.SendCommandPacket;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefConvert;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.TimeCalcUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

import static java.lang.System.exit;
import static kr.co.signallink.svsv2.commons.DefConstant.MEASURE_OPTION.MEASURE_V4;
import static kr.co.signallink.svsv2.commons.DefConstant.MEASURE_OPTION.RAW_NONE;
import static kr.co.signallink.svsv2.commons.DefConstant.MEASURE_OPTION.RAW_WITH_FREQ;
import static kr.co.signallink.svsv2.commons.DefConstant.MEASURE_OPTION.RAW_WITH_TIME;
import static kr.co.signallink.svsv2.commons.DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private static Context context;
    private SVS svs;
    private UartService uartService = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Timer cmdTimer;
    private ResponseCommandHandler responseCommandHandler = new ResponseCommandHandler(this);

    public  Context getAppContext() {
        return context;
    }

    private static MyApplication myApplication = null;
    public static MyApplication getInstance(){
        if(myApplication == null){
            myApplication = new MyApplication();
        }

        return myApplication;
    }

    public void onCreate() {
        super.onCreate();

        myApplication = this;

        //Init
        context = getApplicationContext();
        svs = SVS.getInstance();
        initRootDir();
        initScreenMode();

        //Service
        registerService();

        //Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ToastUtil.showShort("Bluetooth is not available");
            exit(0);
            return;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        //Service
        unregisterService();


        //Bluetooth
        if (bluetoothAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            bluetoothAdapter.disable();
        }

        if(cmdTimer != null) {
            cmdTimer.cancel();
            cmdTimer = null;
        }


    }

    private void initRootDir() {
        File fRootFileDir = new File(svs.getRootdir());
        if(!fRootFileDir.exists()){
            fRootFileDir.mkdir();
        }
    }

    private void initScreenMode() {

        String strScreenMode = SharedUtil.load(SharedUtil.KEY.CURRENT_SCREEN_MODE.toString(), DefConstant.SCREEN_MODE.LOCAL.toString());
        SharedUtil.save(SharedUtil.KEY.CURRENT_SCREEN_MODE.toString(), strScreenMode);

    }



    public void uartDisconnect(){
        uartService.disconnect();
    }

    private ServiceConnection uartServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            uartService = ((UartService.LocalBinder) rawBinder).getService();
            DefLog.d(TAG, "onServiceConnected uartService= " + uartService);
            if (!uartService.initialize()) {
                DefLog.d(TAG, "Unable to initialize Bluetooth");
                exit(0);
            }

            svs.setUartService(uartService);
        }

        public void onServiceDisconnected(ComponentName classname) {

            if(cmdTimer != null) {
                cmdTimer.cancel();
                cmdTimer = null;
            }

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiverOnMain = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                svs.setBleConnectState(DefConstant.UART_PROFILE_CONNECTED);
                ToastUtil.showShort(R.string.connected);
                fisrt_register_sendcommand();
                run_sendCommandTask();
            }

            else if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                uartService.enableTXNotification();
                broadcastUpdate(DefBLEdata.DISCOVERED_ARRIVE);
            }

            else if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {

                if(DefConstant.UART_PROFILE_DISCONNECTED == svs.getBleConnectState()) {
                    ToastUtil.showShort(R.string.notfound);
                } else {
                    ToastUtil.showShort(R.string.disconnected);
                }

                uartService.close();
                svs.setBleConnectState(DefConstant.UART_PROFILE_DISCONNECTED);



                DefLog.d(TAG, "UART_DISCONNECT_MSG");

                if(cmdTimer != null) {
                    cmdTimer.cancel();
                    cmdTimer = null;
                }

                //연결 끊고 0.5초 슬립 필수
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                broadcastUpdate(DefBLEdata.DISCONNECTION_ARRIVE);

                Message msg = new Message();
                msg.what = DefBLEdata.RESPONSECOMMAND_DISCONNECT;
                responseCommandHandler.sendMessage(msg);
            }

            //*********************//
            else if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                try {
                    SendCommandPacket sendCommandPacket = svs.getCurrentSendCommand();
                    synchronized (sendCommandPacket)
                    {

                        if(DefConstant.SVSTRASACTION_ING != sendCommandPacket.getStatus()) {
                            return;
                        }

                        TimeCalcUtil timeCalcUtil = new TimeCalcUtil("Action "+sendCommandPacket.getType());

                        sendCommandPacket.putBytes(txValue);

                        if(sendCommandPacket.byteSize() >= DefCMDOffset.CMD_HEAD_LENGTH)
                        {
                            byte[] rspcmd = sendCommandPacket.getBytes();
                            int size = DefConvert.byteToInt(rspcmd, DefCMDOffset.CMD_LENGTH_OFFSET, DefCMDOffset.CMD_LENGTH_SIZE, ByteOrder.LITTLE_ENDIAN);

                            int rspCmdLength = rspcmd.length;
                            int headerLength = size;
                            String percent = ""+(int)(rspCmdLength * 100.f /headerLength);
                            broadcastUpdate(DefBLEdata.MEASURE_PERCENT, percent, ""+headerLength, ""+rspCmdLength);

                            Log.d("TTTT","Received Size: rspCmdLength:"+rspCmdLength+",headerLength:"+headerLength+",percent:"+percent);
                            if(rspCmdLength >= headerLength)
                            {
                                DefBLEdata.CMD type = sendCommandPacket.getType();

                                timeCalcUtil.printGap("Packaging");


                                ResponseCommandPacket responseCommandPacket = new ResponseCommandPacket(type, size, rspcmd);

                                Message msg = new Message();
                                msg.what = DefBLEdata.RESPONSECOMMAND_ARRIVE;
                                msg.obj = responseCommandPacket;
                                responseCommandHandler.sendMessage(msg);

                                timeCalcUtil.printGap("Activity Toss. msg.type:"+responseCommandPacket.getType()+",size:"+responseCommandPacket.getSize());


                                sendCommandPacket.byteReset();
                                sendCommandPacket.setStatus(DefConstant.SVSTRASACTION_DONE);


                                timeCalcUtil.printGap("End");
                            }
                        }
                    }
                } catch (Exception e) {
                    DefLog.d(TAG, e.toString());
                }
            }
        }
    };

    private void fisrt_register_sendcommand() {
        svs.addSendCommand_Init(DefBLEdata.CMD.HELLO);
        svs.addSendCommand_Init(DefBLEdata.CMD.UPLOAD);

    }

    public void addSendCommand_forLastPLCInfo() {

        //Realm
        Realm realm = Realm.getDefaultInstance();
        realm.refresh();

        //Find SVSEntity
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            SVSEntity svsEntity = (SVSEntity)SVS.getInstance().getLinkedSvsData();

            //Error
            if(svsEntity == null)
            {
                Log.d(TAG,"Error SvsEntity. func:addSendCommand_forLastPLCInfo()");
                return;
            }

            //Send Command
            if(DefConstant.DeviceModel.canPlcFunction(svsEntity)){
                if(svsEntity.getPlcState() == DefConstant.PLCState.ON){
                    svs.addSendCommand_Init(DefBLEdata.CMD.PLC_ON);
                }
                else {
                    svs.addSendCommand_Init(DefBLEdata.CMD.PLC_OFF);
                }
            }
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);

        return intentFilter;
    }

    private void registerService() {

        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, uartServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiverOnMain, makeGattUpdateIntentFilter());
    }

    private void unregisterService() {

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiverOnMain);
        } catch (Exception e) {
            DefLog.d(TAG, e.toString());
        }

        unbindService(uartServiceConnection);
        if(uartService != null)
            uartService.stopSelf();
        uartService = null;
    }

    private void run_sendCommandTask() {

        if(cmdTimer != null) {
            cmdTimer.cancel();
            cmdTimer = null;
        }

        TimerTask cmdTask = new TimerTask() {
            @Override
            public void run() {

                if (uartService != null && DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState())
                {
                    SendCommandPacket sendCommandPacket = svs.getCurrentSendCommand();
                    synchronized (sendCommandPacket)
                    {
                        DefBLEdata.CMD packetType = sendCommandPacket.getType();
                        int packetStatus = sendCommandPacket.getStatus();
                        int tryCounting = sendCommandPacket.getPeriodCounting();

                        Log.d("TTTT","Application CmdTask time:"+sendCommandPacket.timeStamp+",type:"+packetType+",status:"+packetStatus+",try:"+tryCounting);

                        if (DefBLEdata.CMD.NONE != packetType)
                        {
                            if (DefConstant.SVSTRASACTION_INIT == packetStatus)
                            {
                                //전송
                                sendCommandPacket.setStatus(DefConstant.SVSTRASACTION_ING);
                                boolean ret = SendCommand.send(packetType, uartService);
                                if(!ret){
                                    sendCommandPacket.refreshLastSendCommandTimeStamp();
                                    sendCommandPacket.setStatus(DefConstant.SVSTRASACTION_INIT);
                                    Log.d("TTTT","Send fail");
                                }
                                else
                                {
                                    Log.d("TTTT","Send success");
                                }
                            }
                            else if (DefConstant.SVSTRASACTION_ING == packetStatus)
                            {
                                if (tryCounting > DefConstant.TIME_LIMIT)
                                {
                                    Log.d("TTTT","over Try");

                                    if(DefBLEdata.CMD.UPLOAD == packetType)
                                    {
                                        broadcastUpdate(DefBLEdata.UPLOAD_NOTARRIVE);
                                    }

                                    svs.delSendCommand_Ing();
                                }
                                else
                                {
//                                    //마지막으로 시도한 시간에서 일정 시간이 지났는데도 불구하고, 받은 패킷이 0이라면 커멘드를 다시 요청하기
//                                    long lastTimeStamp = sendCommandPacket.getLastSendCommandTimeStamp();
//                                    long currentTimeStamp = System.currentTimeMillis();
//                                    boolean isEmptyBytes = sendCommandPacket.byteSize() == 0;
//                                    Log.d("TTTT","check Try empty:"+isEmptyBytes +",current:"+currentTimeStamp+",last:"+lastTimeStamp+",gap:"+(currentTimeStamp-lastTimeStamp));
//
//
//                                    if(isEmptyBytes && (lastTimeStamp + DefConstant.RETRY_SEND_TIME) < currentTimeStamp)
//                                    {
//                                        Log.d("TTTT","reTry");
//                                        SendCommand.send(packetType, uartService);
//                                        sendCommandPacket.refreshLastSendCommandTimeStamp();
//                                    }

                                    sendCommandPacket.addPeriodCounting();
                                }
                            }
                            else
                            {
                                Log.d("TTTT", "sleep start");

                                //학습이 끝난 후에 5초 딜레이를 줘서, 정리할 시간을 확보 한다.
                                if(packetType == DefBLEdata.CMD.LEARNING)
                                {
                                    try {
                                        Thread.sleep(5 * 1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

//                                //모든 데이터 송/수신 후 2초 딜레이 필요함 (SVS40 단일쓰레드 시리즈만 해야됨.)
//                                try {
//                                    Thread.sleep(2000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }

                                Log.d("TTTT", "sleep end");

                                //리스트에서 삭제
                                svs.delSendCommand_Done(sendCommandPacket);
                            }
                        }
                        else
                        {
                            //측정 계속 하기
                            addSendCommand_Init_Measure();
                        }
                    }
                }
            }
        };

        cmdTimer = new Timer();
        cmdTimer.schedule(cmdTask, DefConstant.DELAYTIME, DefConstant.PERIODTIME);
    }

    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void broadcastUpdate(final String action, String... strDatas) {

        final Intent intent = new Intent(action);
        intent.putStringArrayListExtra("datas",new ArrayList<String>(Arrays.asList(strDatas)));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    //접속한 SVSData의 MeasureOption에 따라서 보내는 커멘드가 다름.
    private void addSendCommand_Init_Measure()
    {

        SendCommandPacket sendCommandPacket = svs.getCurrentSendCommand();
        DefBLEdata.CMD packetType = sendCommandPacket.getType();

        synchronized (sendCommandPacket)
        {
            //Learning 중일때는 측정을 하지 않음.
            if(packetType == DefBLEdata.CMD.LEARNING)
            {
                return;
            }
            //측정이 진행중일때는 측정 커멘드를 보내지 않음.
            else if(packetType == DefBLEdata.CMD.MEASURE_V4
                    || packetType == DefBLEdata.CMD.MEASURE_OPTION_NONE
                    || packetType == DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME
                    || packetType == DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ
                    || packetType == DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ
            ){
                if(sendCommandPacket.getStatus() == DefConstant.SVSTRASACTION_ING)
                {
                    return;
                }
            }
        }


//        boolean test = true;
//        if(test){
//            return;
//        }


        //필수 (getMeasureOption()함수가 Activity에서 변경할경우, 바로 반영이 안되는 현상이 있어서 꼭 refresh를 해줘야 한다.
        Realm realm = Realm.getDefaultInstance();
        realm.refresh();

        RealmObject realmObject = svs.getLinkedSvsData();
        if(realmObject instanceof SVSEntity)
        {
            SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
            if(svsEntity != null)
            {
                int fwVer = svsEntity.getFwVer();
                DefConstant.MEASURE_OPTION measureOption = svsEntity.getMeasureOption();
                final int measureOptionCount = svsEntity.getMeasureOptionCount();

                //Unknown
                if(fwVer == -1)
                {
                    //Measure Process
                    if(measureOptionCount > 0)
                    {
                        if(measureOption == RAW_WITH_TIME_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ);
                        }
                        else if(measureOption == RAW_WITH_TIME) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME);
                        }
                        else if(measureOption == RAW_WITH_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ);
                        }
                        else {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                        }
                    }
                    else {
                        svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                    }
                }
                else
                {
                    //버전 체크 및 변경 기능
                    Boolean isChanged = false;
                    if(fwVer >= DefConstant.FwVer.SupportRawMeasure.getFwVer())
                    {
                        //이전 버전을 쓰고 있다면 최신버전으로 변경.
                        if(measureOption == MEASURE_V4)
                        {
                            measureOption = RAW_NONE;
                            isChanged = true;
                        }
                    }
                    else
                    {
                        //구버전 펌웨어 인데, 최신 Measure Option을 사용하고 있으면 구버전 MeasureOption으로 변경
                        if(measureOption != MEASURE_V4)
                        {
                            measureOption = MEASURE_V4;
                            isChanged = true;
                        }
                    }

                    //구버전 옵션에 대한 변경을 DB에 반영
                    if(isChanged)
                    {
                        final DefConstant.MEASURE_OPTION changedMeasureOption = measureOption;
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
                                svsEntity.setMeasureOption(changedMeasureOption);
                            }
                        });
                        DatabaseUtil.refresh();
                    }


                    //Measure Process
                    if(measureOptionCount > 0)
                    {
                        if(measureOption == RAW_WITH_TIME_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ);
                        }
                        else if(measureOption == RAW_WITH_TIME) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME);
                        }
                        else if(measureOption == RAW_WITH_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ);
                        }
                        else {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                        }
                    }
                    else {
                        svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                    }
                }

                //공통

                //MeasureOptionCount를 감소시키고 DB에 반영
                if(measureOptionCount > 0)
                {
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
                            svsEntity.setMeasureOptionCount(measureOptionCount-1);
                        }
                    });
                    DatabaseUtil.refresh();
                }

            }
            else
            {
                Log.e("TTTT","SvsEntity is null. Request not measure.");
            }
        }
        else if(realmObject instanceof WSVSEntity)
        {
            WSVSEntity wsvsEntity = (WSVSEntity)svs.getLinkedSvsData();
            if(wsvsEntity != null)
            {
                int fwVer = wsvsEntity.getFwVer();
                DefConstant.MEASURE_OPTION measureOption = wsvsEntity.getMeasureOption();
                final int measureOptionCount = wsvsEntity.getMeasureOptionCount();

                //Unknown
                if(fwVer == -1)
                {
                    //Measure Process
                    if(measureOptionCount > 0)
                    {
                        if(measureOption == RAW_WITH_TIME_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ);
                        }
                        else if(measureOption == RAW_WITH_TIME) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME);
                        }
                        else if(measureOption == RAW_WITH_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ);
                        }
                        else {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                        }
                    }
                    else {
                        svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                    }
                }
                else
                {
                    //버전 체크 및 변경 기능
                    Boolean isChanged = false;
                    if(fwVer >= DefConstant.FwVer.SupportRawMeasure.getFwVer())
                    {
                        //이전 버전을 쓰고 있다면 최신버전으로 변경.
                        if(measureOption == MEASURE_V4)
                        {
                            measureOption = RAW_NONE;
                            isChanged = true;
                        }
                    }
                    else
                    {
                        //구버전 펌웨어 인데, 최신 Measure Option을 사용하고 있으면 구버전 MeasureOption으로 변경
                        if(measureOption != MEASURE_V4)
                        {
                            measureOption = MEASURE_V4;
                            isChanged = true;
                        }
                    }

                    //구버전 옵션에 대한 변경을 DB에 반영
                    if(isChanged)
                    {
                        final DefConstant.MEASURE_OPTION changedMeasureOption = measureOption;
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                WSVSEntity wsvsEntity = (WSVSEntity)svs.getLinkedSvsData();
                                wsvsEntity.setMeasureOption(changedMeasureOption);
                            }
                        });
                        DatabaseUtil.refresh();
                    }


                    //Measure Process
                    if(measureOptionCount > 0)
                    {
                        if(measureOption == RAW_WITH_TIME_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ);
                        }
                        else if(measureOption == RAW_WITH_TIME) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME);
                        }
                        else if(measureOption == RAW_WITH_FREQ) {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ);
                        }
                        else {
                            svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                        }
                    }
                    else {
                        svs.addSendCommand_Init(DefBLEdata.CMD.MEASURE_OPTION_NONE);
                    }
                }


                //공통

                //MeasureOptionCount를 감소시키고 DB에 반영
                if(measureOptionCount > 0)
                {
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            WSVSEntity wsvsEntity = (WSVSEntity)svs.getLinkedSvsData();
                            wsvsEntity.setMeasureOptionCount(measureOptionCount-1);
                        }
                    });
                    DatabaseUtil.refresh();
                }

            }
            else
            {
                Log.e("TTTT","SvsEntity is null. Request not measure.");
            }
        }
        //*/
    }
}
