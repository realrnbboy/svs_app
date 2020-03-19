package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.app.ProgressDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.SVSAdapter;

// added by hslee
// SVSLocationAutoModeActivity 의 내용을 복사하여 이름만 바꾸고 조금 수정한 클래스.
public class MeasureExeActivity extends Activity {

    private static final String TAG = "MeasureExeActivity";
    private SVS svs = SVS.getInstance();

    private UartService uartService = null;

    private OrderedRealmCollection<SVSEntity> svsEntities;

    private boolean showProgressDialogButton = false;

    private BluetoothAdapter checkbluetooth = null;

    private ConnectSVSItems connectSVSItems = null;

    private boolean auto_working = true;
    private int bypassSaveCnt = 0; //기존에 연결되어있던 기기를 끊으면서 저장하지 않는걸 기억하기 위해 존재하는 변수.

    MeasureData measureDataSensor1 = null;
    MeasureData measureDataSensor2 = null;
    MeasureData measureDataSensor3 = null;

    boolean bModeSensor = true; // sensor or pipe

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
                                    sucessSave = save();
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

        bModeSensor = getIntent().getBooleanExtra("modeSensor", true);

        Log.d("TTTT","SVS AutoMode onCreate");

        init_service();

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnMain, makeUpdateIntentFilter());

        checkbluetooth = BluetoothAdapter.getDefaultAdapter();
        if (checkbluetooth == null) {
            ToastUtil.showShort("Bluetooth is not available");
            finish();
            return;
        }

        svsEntities = ((EquipmentEntity)svs.getSelectedEquipmentData()).getSvsEntities();
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
                //화면에 보인 순서와 다르게, SVS_LOCATION 이름순으로 AutoProcessing 시작.
                OrderedRealmCollection<SVSEntity> orderedSvsEntities = svsEntities.sort("SVS_LOCATION"); //Location 정렬..

                for(SVSEntity svsEntity : orderedSvsEntities)
                {
                    if(svsEntity.isValid())
                    {
                        ConnectSVSItem connectSVSItem = new ConnectSVSItem();
                        connectSVSItem.setSvsUuid(svsEntity.getUuid());
                        connectSVSItem.setAddress(svsEntity.getAddress());
                        connectSVSItem.setSvsLocation(svsEntity.getSvsLocation());

                        svsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.RAW_WITH_FREQ);  // added by hslee
                        //svsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ);  // added by hslee
                        svsEntity.setMeasureOptionCount(1); // added by hslee

                        connectSVSItems.add(connectSVSItem);

                        if( !bModeSensor )   // added by hslee 2020-03-19 배관진단에서는 센서1개만 사용
                            break;
                    }
                    else
                    {
                        ToastUtil.showShort(R.string.notSVSLocationName);
                    }
                }
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

    private boolean save() {

        EquipmentEntity equipmentEntity = (EquipmentEntity)svs.getLinkedEquipmentData();
        SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
        final String linkedEquipmentUuid = equipmentEntity.getUuid();
        final String linkedSvsUuid = svsEntity.getUuid();

        if(linkedEquipmentUuid != null && linkedSvsUuid != null)
        {

            ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();

            Log.d("TTTT","SVS measureCnt:"+measureDatas.size());

            if(measureDatas.size() > 0)
            {
                MeasureData measureData = measureDatas.get(0);



                SVSParam svsParam = SVS.getInstance().getUploaddata().getSvsParam();    // added by hslee
                SVSCode svsCode = svsParam.getCode();
                SVSTime svsTimeWarning = svsCode.getTimeWrn();
                float rmsWarning = svsTimeWarning.getdRms();
                SVSTime svsTimeDanger = svsCode.getTimeDan();
                float rmsDanger = svsTimeDanger.getdRms();

                measureData.setRmsWarning(rmsWarning);
                measureData.setRmsDanger(rmsDanger);

                if( measureDataSensor1 == null )    // added by hslee
                    measureDataSensor1 = measureData;
                else if( measureDataSensor2 == null )
                    measureDataSensor2 = measureData;
                else
                    measureDataSensor3 = measureData;



                Date date = measureData.getCaptureTime();
                String strDate = DateUtil.getDateStringBySimpleFormat(date);
                String strTime = DateUtil.getTimeStringBySimpleFormat(date);

                //Dir
                String historyDir = DefFile.FOLDER.HISTORY.getFullPath();
                String equipmentDir = historyDir + linkedEquipmentUuid + File.separator;
                String svsDir = equipmentDir + linkedSvsUuid + File.separator;
                String dateDir = svsDir + strDate + File.separator;
                String timeDir = dateDir + strTime + File.separator;

                //폴더 확인
                File fTimeDir = new File(timeDir);
                if (!fTimeDir.exists()) {
                    boolean b = fTimeDir.mkdirs();
                    if(!b){
                        ToastUtil.showShort("Can't Make File.");
                        return false;
                    }
                }

                //Write Upload, Measure, Comment
                FileUtil.writeUpload(timeDir);
                FileUtil.writeMeasure(timeDir, new ArrayList<MeasureData>(Arrays.asList(measureData)));
                FileUtil.writeComment(timeDir, "Auto Saved.");


                //상태
                HistoryData historyData = new HistoryData();
                UploadData uploaddata = ParserCommand.rawupload(svs.getRawuploaddata());
                historyData.setUploaddata(uploaddata);
                DefConstant.SVS_STATE svsState = DefConstant.SVS_STATE.DEFAULT;
                if (svs.getRecordMeasureDatas() != null)
                {
                    historyData.calcAverageMeasure(svs.getRecordMeasureDatas());

                    MeasureData measuredata = historyData.getAveragemeasure();
                    svsState = FileUtil.calcSVSState(uploaddata, measuredata);
                }

                //기록 갯수
                int totalHistoryCount = 0; //장비의 총 갯수
                int targetHistoryCount = 0; //연결된 기기의 갯수

                //기기의 기록 갯수 구하기
                for(SVSEntity child : equipmentEntity.getSvsEntities())
                {
                    String childUuid = child.getUuid();

                    int historyCount = FileUtil.getSubDirCount(equipmentDir + childUuid + File.separator);
                    if(childUuid.equals(linkedSvsUuid))
                    {
                        targetHistoryCount = historyCount;
                    }

                    totalHistoryCount += historyCount;
                }

                //상태와 갯수를 파일로 쓰기
                final String equipmentLastRecordContent = strDate + "(" + totalHistoryCount + ")";
                final String svsLastRecordContent = strDate + "(" + targetHistoryCount + ")";
                final DefConstant.SVS_STATE lastSvsState = svsState;
                DatabaseUtil.transaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        EquipmentEntity equipmentEntity = new RealmDao<EquipmentEntity>(EquipmentEntity.class).loadByUuid(linkedEquipmentUuid);
                        if(equipmentEntity != null)
                        {
                            equipmentEntity.setLastRecord(equipmentLastRecordContent);
                            equipmentEntity.setSvsState(lastSvsState);
                        }

                        SVSEntity svsEntity = new RealmDao<SVSEntity>(SVSEntity.class).loadByUuid(linkedSvsUuid);
                        if(svsEntity != null)
                        {
                            svsEntity.setLastRecord(svsLastRecordContent);
                            svsEntity.setSvsState(lastSvsState);
                        }
                    }
                });
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    private void showProgressDialog(boolean showButton, String subMessage)
    {
        String equipmentName = ((EquipmentEntity)svs.getLinkedEquipmentData()).getName();
        String title = "Processing sensor";
        String message = equipmentName
                + " " + connectSVSItems.getCurrentIndexSvsLocation().toString().toUpperCase()
                + "\n\n"+ subMessage + "...";

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        TextView textViewStatus = findViewById(R.id.textViewStatus);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        textViewTitle.setText(title);
        textViewStatus.setText(message);
        if(!showProgressDialogButton && showButton)
        {
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectSVSItems.removeAll();
                    if(DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState())
                        uartService.disconnect();
                    else
                        broadcastUpdate(UartService.ACTION_GATT_DISCONNECTED);

                    cancel();
                }
            });

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
        returnIntent.putExtra("measureDataSensor1", measureDataSensor1);
        returnIntent.putExtra("measureDataSensor2", measureDataSensor2);
        returnIntent.putExtra("measureDataSensor3", measureDataSensor3);

        finish();
    }
}
