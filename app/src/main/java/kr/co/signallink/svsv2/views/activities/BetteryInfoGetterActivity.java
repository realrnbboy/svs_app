package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.command.ParserCommand;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.HistoryData;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.user.ConnectSVSItem;
import kr.co.signallink.svsv2.user.ConnectSVSItems;
import kr.co.signallink.svsv2.user.RegisterSVSItem;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

import static kr.co.signallink.svsv2.commons.DefConstant.PLCState.OFF;

// added by hslee
// SVSLocationAutoModeActivity 의 내용을 복사하여 이름만 바꾸고 조금 수정한 클래스.
public class BetteryInfoGetterActivity extends Activity {

    private static final String TAG = "BetteryInfoGetterActivity";
    private SVS svs = SVS.getInstance();

    private UartService uartService = null;

    private OrderedRealmCollection<SVSEntity> svsEntities;
    //ArrayList<RegisterSVSItem> sensorList;
    RegisterSVSItem sensorEntity;
    int sensorPosition;

    private boolean showProgressDialogButton = false;

    private BluetoothAdapter checkbluetooth = null;

    private ConnectSVSItems connectSVSItems = null;

    private boolean auto_working = true;
    private int bypassSaveCnt = 0; //기존에 연결되어있던 기기를 끊으면서 저장하지 않는걸 기억하기 위해 존재하는 변수.

    MeasureData measureDataSensor1 = null;
    MeasureData measureDataSensor2 = null;
    MeasureData measureDataSensor3 = null;

    final int maxConnectTryCount = 5;    // added by hslee 2020.03.30
    int connectTryCount = 0;    // added by hslee 2020.03.30
    int trySensorNumber = 0;  // 현재 시도 중인 센서

    private final BroadcastReceiver StatusChangeReceiverOnMain = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(DefBLEdata.DISCOVERED_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        if(svs.getLinkedEquipmentData() != null)
                        {
                            if(connectSVSItems.isAutoSaveMode()) {
                                svs.setRecorded(true);
                            }
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.HELLO_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "HELLO_ARRIVE");

                        if(svs.getLinkedEquipmentData() != null)
                        {
                            showProgressDialog(false, getResources().getString(R.string.requestsvsinfo));
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.BAT_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "BAT_ARRIVE");

                        if(svs.getLinkedEquipmentData() != null)
                        {
                            showProgressDialog(false, getResources().getString(R.string.requestsettinginfo));
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.BATTERY_ARRIVE)) {  // added by hslee 2020.04.29
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "BATTERY_ARRIVE");

                        complete();
                    }
                });
            } else if (action.equals(DefBLEdata.UPLOAD_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "UPLOAD_ARRIVE");

                        if(svs.getLinkedEquipmentData() != null)
                        {
                            if(connectSVSItems != null && connectSVSItems.size() > 0)
                            {
                                if(connectSVSItems.isAutoSaveMode())
                                {
                                    showProgressDialog(false, getResources().getString(R.string.autosaving));
                                }
                            }
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.UPLOAD_NOTARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "UPLOAD_NOTARRIVE");

                        if(svs.getLinkedEquipmentData() != null)
                        {
                            hideProgressDialog();

                            ToastUtil.showShort(R.string.notuploaded);
                            uartService.disconnect();
                        }
                    }
                });
            }
            else if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {

                if(connectSVSItems != null && connectSVSItems.size() > 0)
                {
                    if(connectSVSItems.isAutoSaveMode())
                    {
                        if(!svs.isRecorded()) {
                            DefLog.d(TAG, "--------------------------> disconnect");
                            uartService.disconnect();
                        }
                    }
                }
            }
            else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {

                //ToastUtil.showShort("disconn!!!" + connectTryCount);
                //System.out.println("disconn!!! : " + connectTryCount + ", trySensorNumber : " + trySensorNumber);

                if( ++connectTryCount >= maxConnectTryCount + trySensorNumber ) {  // added by hslee 2020.03.30
                    ToastUtil.showShort("Max connect try count over.");
                    cancel();
                    return;
                }

                //세이브를 패스해야되는 숫자 카운트
                if(bypassSaveCnt > 0)
                {
                    bypassSaveCnt--;

                    //만약 패스 숫자를 감소하던 도중 0이 되면, 아래 로직을 실행
                    if(bypassSaveCnt == 0)
                    {
                        svs.clear();

                        Log.d("TTTT","SVS AutoSaveConnect");

                        //전체 SVS를 리스트로 추가하는 작업.
                        autoSaveConnect();
                    }
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            DefLog.d(TAG, "DISCONNECTION_ARRIVE");

                            if (!checkbluetooth.isEnabled()) {
                                DefLog.d(TAG, "onResume - BT not enabled yet");
                                checkbluetooth.enable();
                            }

                            hideProgressDialog();

                            if(connectSVSItems != null && connectSVSItems.size() > 0)
                            {
                                boolean sucessSave = true;
                                if(connectSVSItems.isAutoSaveMode()) {
                                    //sucessSave = save2();
                                }
                                svs.clear();

                                //성공시에만 다음 기기로 갈 수 있게 변경. (간혹 같은 기기를 disconnect하고 다시 connect하면 정상적으로 measureData를 받지 못하는 경우가 생김)
                                if(sucessSave){
                                    connectSVSItems.nextIndex();

                                }

                                if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
                                {
                                    boolean connected = uartService.connect(connectSVSItems.getCurrentIndexAddress());
                                    if(!connected) {
                                        hideProgressDialog();
                                    }
                                    else
                                    {
                                        svs.setLinkedSvsUuid(connectSVSItems.getCurrentIndexUuid());
                                        showProgressDialog(true, getResources().getString(R.string.requestconnect));
                                    }

                                }
                                else if(connectSVSItems.getIndex_connecting() == connectSVSItems.size())
                                {
                                    //순차적 모두 기록하면 완료하고 뒤로가는 로직
                                    svs.setLinkedSvsUuid(null);
                                    ToastUtil.showShort(R.string.Completed);

                                    // added by hslee
                                    complete();
                                }

                            }
                            else if(connectSVSItems != null && connectSVSItems.size() == 0)
                            {
                                //취소하게 되면 뒤로가는 로직
                                svs.setLinkedSvsUuid(null);
                                ToastUtil.showShort("Canceled.");
                                //finish(); // deleted by hslee
                                cancel();
                            }
                        }
                    });
                }



            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_measure_exe);
        sensorEntity = (RegisterSVSItem) getIntent().getSerializableExtra("sensorEntity");
        if( sensorEntity == null ) {
            finish();
        }
        sensorPosition = getIntent().getIntExtra("sensorPosition", -1);
        if( sensorPosition < 0 ) {
            finish();
        }

        Log.d("TTTT","SVS AutoMode onCreate");

        init_service();

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnMain, makeUpdateIntentFilter());

        checkbluetooth = BluetoothAdapter.getDefaultAdapter();
        if (checkbluetooth == null) {
            ToastUtil.showShort("Bluetooth is not available");
            finish();
            return;
        }


        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectSVSItems.removeAll();
                if(DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState()) {
                    uartService.disconnect();
                }
                else {
                    broadcastUpdate(UartService.ACTION_GATT_DISCONNECTED);
                }

                cancel();
            }
        });

        //svsEntities = ((EquipmentEntity)svs.getSelectedEquipmentData()).getSvsEntities();
        //svsEntities = sensorList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DefLog.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnMain);
        } catch (Exception e) {
            DefLog.d(TAG, e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");

        if (!checkbluetooth.isEnabled()) {
            DefLog.d(TAG, "onResume - BT not enabled yet");
            dialogBleOnOff();
        } else {
            if(auto_working) {
                autoSaveConnect();
                auto_working = false;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.HELLO_ARRIVE);
        intentFilter.addAction(DefBLEdata.BAT_ARRIVE);
        intentFilter.addAction(DefBLEdata.BATTERY_ARRIVE);
        intentFilter.addAction(DefBLEdata.UPLOAD_ARRIVE);
        intentFilter.addAction(DefBLEdata.UPLOAD_NOTARRIVE);
        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCOVERED_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }

    private void init_service() {
        uartService = svs.getUartService();
    }



    private void autoSaveConnect() {

        if (uartService != null && DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState())
        {
            uartService.disconnect();
            bypassSaveCnt = 1;
        }
        else
        {
            connectSVSItems = new ConnectSVSItems();
            connectSVSItems.setAutoSaveMode(true);


            //Request RawData by Option.
            DatabaseUtil.transaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    SVSEntity svsEntity = new SVSEntity();
                    svsEntity.setUuid(sensorEntity.getUuid());
                    //svsEntity.setSerialNum(sensor.getS());
                    svsEntity.setName(sensorEntity.getName());
                    svsEntity.setAddress(sensorEntity.getAddress());
                    svsEntity.setImageUri(sensorEntity.getImageUri());
                    svsEntity.setLastRecord(sensorEntity.getLastRecord());
                    svsEntity.setSvsLocation(sensorEntity.getSvsLocation());
                    svsEntity.setSvsState(sensorEntity.getSvsState());
                    svsEntity.setPlcState(sensorEntity.getPlcState());
                    //svsEntity.setMeasureOption(sensor.getMeasureOption());
                    svsEntity.setMeasureOptionCount(1);

                    ConnectSVSItem connectSVSItem = new ConnectSVSItem();
                    connectSVSItem.setSvsUuid(svsEntity.getUuid());
                    connectSVSItem.setAddress(svsEntity.getAddress());
                    connectSVSItem.setSvsLocation(svsEntity.getSvsLocation());
                    connectSVSItem.setDeviceName(svsEntity.getName());

                    svsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.BATTERY);  // added by hslee
                    //svsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ);  // added by hslee

                    svs.bBatteryInfoRequest = true;
                    svs.svsEntityBatteryInfo = svsEntity;
                    connectSVSItems.add(connectSVSItem);
                }
            });


            if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
            {
                svs.setLinkedSvsUuid(connectSVSItems.getCurrentIndexUuid());

                showProgressDialog(true, getResources().getString(R.string.requestconnect));

                boolean connected = uartService.connect(connectSVSItems.getCurrentIndexAddress());
                if(!connected) {
                    hideProgressDialog();
                }

            }

            if(connectSVSItems.size() == 0)
            {
                ToastUtil.showShort(R.string.notregistered,"9");
            }
        }
    }

    private void showProgressDialog(boolean showButton, String subMessage)
    {
        String title = "Processing sensor";
        String message = "Getting battery information"
                //+ " " + connectSVSItems.getCurrentIndexSvsLocation().toString().toUpperCase()
                + " " + connectSVSItems.getCurrentItem().getDeviceName()
                + "\n\n"+ subMessage + "...";

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewStatus = findViewById(R.id.textViewStatus);

        textViewTitle.setText(title);
        textViewStatus.setText(message);
        if(!showProgressDialogButton && showButton) {
            showProgressDialogButton = true;
        }

        if(progressBar.getVisibility() != View.VISIBLE )
        {
            //progressDialog.show();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressDialog(){
        //progressDialog.dismiss();
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }


    private void dialogBleOnOff() {

        DialogUtil.yesNo(this,
            getResources().getString(R.string.automode_screen),
            "SVS App requests to turn on Bluetooth.",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    checkbluetooth.enable();
                    ToastUtil.showShort(R.string.BleTurnON);

                    if(auto_working) {
                        autoSaveConnect();
                        auto_working = false;
                    }
                }
            },
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    checkbluetooth.disable();
                    ToastUtil.showShort(R.string.BleTurnOFF);
                    dialog.cancel();
                    ToastUtil.showShort(R.string.notCompleted);
                    //finish(); // deleted by hslee
                }
            }
        );
    }

    private void dialogBleClose() {

        EquipmentEntity equipmentEntity = (EquipmentEntity)svs.getLinkedEquipmentData();
        SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();

        final String equipmentName = equipmentEntity.getName();
        final String svsLocation = svsEntity.getSvsLocation().toString();


        DialogUtil.yesNo(this,


                equipmentName + "(" + svsLocation + ")",
            "Do you want to disconnect?",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    uartService.disconnect();
                }
            },
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                    cancel();
                }
            }
        );
    }

    private void cancel() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void complete() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        returnIntent.putExtra("battery", svs.batteryLevel);
        returnIntent.putExtra("sensorPosition", sensorPosition);

        finish();
    }

    @Override public void onBackPressed() { // added by hslee 2020.04.10
        ToastUtil.showLong("back button is disabled during measure.");
    }
}
