package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmObject;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.command.SendCommand;
import kr.co.signallink.svsv2.command.SendCommandPacket;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.server.OnTCPSendCallback;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ProgressDialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.DiagnosisAdapter;

import static kr.co.signallink.svsv2.commons.DefBLEdata.CMD.LEARNING;
import static kr.co.signallink.svsv2.commons.DefBLEdata.CMD.UPLOAD;
import static kr.co.signallink.svsv2.commons.DefConstant.PLCState.OFF;
import static kr.co.signallink.svsv2.commons.DefConstant.PLCState.ON;
import static kr.co.signallink.svsv2.commons.DefConstant.SVSTRASACTION_DONE;

/**
 * Created by nspil on 2018-02-20.
 */

public class DiagnosisActivity extends BaseActivity{

    public static Activity thisActivity;

    private static final String TAG = "DiagnosisActivity";

    //View
    private LinearLayout llPlc;
    private ImageButton ibtnPlcState;
    private LinearLayout btnLearning;
    private ListView listView;



    //Common
    private SVS svs = SVS.getInstance();
    private String linkedAddress = null;
    private DiagnosisAdapter diagnosisAdapter;
    private String svsUuid;

    //PLC
    private DefConstant.PLCState currentPlcState = OFF;

    //Learning
    private static ProgressDialogUtil progressDialogUtil;
    private static HashMap<String, Long> lastRequestTimeStampMap;
    private static int count = 0;
    private long LEARNING_TIME_OUT = 2 * 60 * 1000; //default 타임아웃
    private long UPLOAD_TIME_OUT = 20 * 1000;//default



    private final BroadcastReceiver StatusChangeReceiverOnChart = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(DefBLEdata.LEARNING_ARRIVE))
            {
                //학습 성공 메세지 전달
                ToastUtil.showLong("Learning success.\n" +
                        "Please wait, Receiving learned data.");
            }
            else if (action.equals(DefBLEdata.UPLOAD_ARRIVE))
            {
                //업로드 정보 도착

                resetLearningTimeOut();

                if(lastRequestTimeStampMap.containsKey(linkedAddress))
                {
                    //타임아웃 핸들러 취소
                    timeoutHandler.removeMessages(MESSAGE_LEARNING);
                    timeoutHandler.removeMessages(MESSAGE_UPLOADING);

                    //시간 정보 제거
                    lastRequestTimeStampMap.remove(linkedAddress);

                    //프로그래스 닫기
                    if(progressDialogUtil != null){
                        progressDialogUtil.hide();
                        progressDialogUtil = null;
                    }

                    //화면 리스트 전체 갱신
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            diagnosisAdapter.notifyDataSetInvalidated();
                        }
                    });

                    //성공 메세지 전달
                    ToastUtil.showLong("Update sensor information based on learned data");

                    //업로드 정보를 WebManager에 전달할지 묻기. (웹 모드 일때만 묻기)
                    if(SVS.getInstance().getScreenMode() == DefConstant.SCREEN_MODE.WEB)
                    {
                        guideUploadToWebManager();
                    }

                    return;
                }
            }
            else if(action.equals(DefBLEdata.DISCONNECTION_ARRIVE))
            {
                //연결 끊김
                Log.d("TTTT", "DiagnosisActivity Disconnection_arrive");

                //학습이 진행중 이였을때.
                if(lastRequestTimeStampMap.containsKey(linkedAddress))
                {
                    //타임아웃 핸들러 취소
                    timeoutHandler.removeMessages(MESSAGE_LEARNING);
                    timeoutHandler.removeMessages(MESSAGE_UPLOADING);

                    //시간 정보 제거
                    lastRequestTimeStampMap.remove(linkedAddress);

                    //다이얼로그 닫기
                    if(progressDialogUtil != null){
                        progressDialogUtil.hide();
                        progressDialogUtil = null;
                    }

                    //알람.
                    if(!DiagnosisActivity.this.isFinishing()){
                        DialogUtil.confirm(DiagnosisActivity.this, "SVS Disconnected", "Learning function failed\n" +
                                "Please check your device and try again.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(DiagnosisActivity.thisActivity != null){
                                    DiagnosisActivity.thisActivity.finish();
                                }
                            }
                        });
                    }
                }
                else
                {
                }

            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diagnosis_activity);
        thisActivity = this;
        if(lastRequestTimeStampMap == null){
            lastRequestTimeStampMap = new HashMap<>();
        }

        Toolbar svsToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.diagnosis_screen);
        ((Button)findViewById(R.id.button_record)).setVisibility(View.GONE);


        Button button_trend_search = (Button) findViewById(R.id.button_control_ble);
        button_trend_search.setText("Refresh");
        button_trend_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                guideUploadDataUpdating();
            }
        });


        //PLC
        llPlc = (LinearLayout)findViewById(R.id.llPlc);
        ibtnPlcState = (ImageButton)findViewById(R.id.ibtnPlcState);
        ibtnPlcState.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean selected = ibtnPlcState.isSelected();

                boolean toggleSelected = !selected;

                ibtnPlcState.setSelected(toggleSelected);

                setPlcState(DefConstant.PLCState.findPLCState(toggleSelected));
            }
        });

        initData();

        //Learning
        btnLearning = findViewById(R.id.btnLearning);
        btnLearning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLearning();
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnChart, makeUpdateIntentFilter());

        //Adapter Category
        ArrayList<DefConstant.DIAGNOSIS_CATEGORY> diagnosisCategoryArrayList = new ArrayList<>();
        for(DefConstant.DIAGNOSIS_CATEGORY category : DefConstant.DIAGNOSIS_CATEGORY.values()){

            //BeepAndFlag 화면
            if(category == DefConstant.DIAGNOSIS_CATEGORY.BEEP_AND_OR_CONDITION)
            {
                //이전버전에선 지원하지 않음.
                final int currentVer = SVS.getInstance().getHellodata().getuFwVer();
                final int supportVer = DefConstant.FwVer.SupportBeepAndFlag.getFwVer();
                if(currentVer < supportVer)
                {
                    continue;
                }
            }

            //else
            diagnosisCategoryArrayList.add(category);
        }

        //Adapter
        diagnosisAdapter = new DiagnosisAdapter(this, 0, diagnosisCategoryArrayList.toArray(new DefConstant.DIAGNOSIS_CATEGORY[diagnosisCategoryArrayList.size()]));
        listView = (ListView)findViewById(R.id.diagnosis_content_list);
        listView.setAdapter(diagnosisAdapter);

        //Sensor Info Upload to Web Manager
        Button btnUploadToWebManager = findViewById(R.id.btnUploadToWebManager);
        btnUploadToWebManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guideUploadToWebManager();

            }
        });

        //Go to Web Manager
        ImageButton btnGoToWebManager = findViewById(R.id.btnGoToWebManager);
        btnGoToWebManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goIntent(WebActivity.class, false, WebActivity.TARGET_UUID, svsUuid);
            }
        });

        //스크린 모드 가져와서 웹 모드가 아니라면, 웹 관련된 서비스 감추기
        if(SVS.getInstance().getScreenMode() != DefConstant.SCREEN_MODE.WEB)
        {
            btnUploadToWebManager.setVisibility(View.GONE);
            btnGoToWebManager.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thisActivity = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnChart);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void initData()
    {
        //Find SVSEntity
        RealmObject realmObject = svs.getLinkedSvsData();
        if(realmObject instanceof  SVSEntity)
        {
            SVSEntity svsEntity = (SVSEntity)realmObject;

            if(svsEntity != null)
            {
                svsUuid = svsEntity.getUuid();
                linkedAddress = svsEntity.getAddress();
                currentPlcState = svsEntity.getPlcState();
            }
            else
            {
                ToastUtil.showLong("Could not get information from connected device.");
                finish();
            }

            resetLearningTimeOut();

            //PLC
            if(DefConstant.DeviceModel.canPlcFunction(svsEntity)){
                llPlc.setVisibility(View.VISIBLE);
                ibtnPlcState.setSelected(currentPlcState.getBoolean());
                ibtnPlcState.setEnabled(true);
            }
            else {
                llPlc.setVisibility(View.GONE);
                ibtnPlcState.setSelected(false);
                ibtnPlcState.setEnabled(false);
            }
        }
        else
        {
            WSVSEntity wsvsEntity = (WSVSEntity)realmObject;

            if(wsvsEntity != null)
            {
                svsUuid = wsvsEntity.id;
                linkedAddress = wsvsEntity.getAddress();
                currentPlcState = wsvsEntity.getPlcState();
            }
            else
            {
                ToastUtil.showLong("Could not get information from connected device.");
                finish();
            }

            resetLearningTimeOut();

            //PLC
            if(DefConstant.DeviceModel.canPlcFunction(wsvsEntity)){
                llPlc.setVisibility(View.VISIBLE);
                ibtnPlcState.setSelected(currentPlcState.getBoolean());
                ibtnPlcState.setEnabled(true);
            }
            else {
                llPlc.setVisibility(View.GONE);
                ibtnPlcState.setSelected(false);
                ibtnPlcState.setEnabled(false);
            }
        }
    }

    //러닝 카운트에 따른 러닝 타임아웃
    private void resetLearningTimeOut()
    {
        UploadData uploadData = svs.getUploaddata();
        SVSParam svsParam = uploadData.getSvsParam();

        long learnCnt = svsParam.getlLearnCnt();
        if(learnCnt == 0){
            learnCnt = 1;
        }

        int fftAverage = svsParam.getnFftAvg();
        if(fftAverage == 0){
            fftAverage = 1;
        }

        LEARNING_TIME_OUT = Math.abs(learnCnt) * Math.abs(fftAverage) * 2000 ; //러닝카운트, fftAverage 당 2.5초
        LEARNING_TIME_OUT += UPLOAD_TIME_OUT;
    }


    private static IntentFilter makeUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.UPLOAD_ARRIVE);
        intentFilter.addAction(DefBLEdata.LEARNING_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }


    private void setPlcState(final DefConstant.PLCState plcState){

        //Error Check
        RealmObject realmObject = svs.getLinkedSvsData();
        if(realmObject instanceof SVSEntity)
        {
            SVSEntity svsEntity = (SVSEntity)realmObject;
            if(svsEntity == null)
            {
                ToastUtil.showShort("Error SVS Data.");
                return;
            }

            //Send Command
            if(plcState == ON){
                svs.addSendCommand_Init(DefBLEdata.CMD.PLC_ON);
            }
            else {
                svs.addSendCommand_Init(DefBLEdata.CMD.PLC_OFF);
            }

            //Apply Data
            new RealmDao<SVSEntity>(SVSEntity.class).transaction(svsEntity, new RealmDao.IDao() {
                @Override
                public void excuteRealm(Realm realm, RealmObject obj) {

                    SVSEntity svsEntity = (SVSEntity)obj;

                    svsEntity.setPlcState(plcState);
                }
            });
        }
        else
        {
            WSVSEntity wsvsEntity = (WSVSEntity)realmObject;
            if(wsvsEntity == null)
            {
                ToastUtil.showShort("Error SVS Data.");
                return;
            }

            //Send Command
            if(plcState == ON){
                svs.addSendCommand_Init(DefBLEdata.CMD.PLC_ON);
            }
            else {
                svs.addSendCommand_Init(DefBLEdata.CMD.PLC_OFF);
            }

            //Apply Data
            new RealmDao<WSVSEntity>(WSVSEntity.class).transaction(wsvsEntity, new RealmDao.IDao() {
                @Override
                public void excuteRealm(Realm realm, RealmObject obj) {

                    WSVSEntity wsvsEntity = (WSVSEntity)obj;

                    wsvsEntity.setPlcState(plcState);
                }
            });
        }

        currentPlcState = plcState;


        ToastUtil.showShort("Success.\nCurrent PLC State is "+(plcState == ON ? "on":"off")+".");
    }

    private void dialogLearning(){

        //Error Check
        RealmObject realmObject = svs.getLinkedSvsData();
        if(realmObject == null)
        {
            ToastUtil.showShort("Error SVS Data.");
            return;
        }

        //Time Check
        final long currentTimeStamp = System.currentTimeMillis();
        if(lastRequestTimeStampMap.containsKey(linkedAddress))
        {
            long lastRequestTimeStamp = lastRequestTimeStampMap.get(linkedAddress);
            long gap = currentTimeStamp - lastRequestTimeStamp;

            //강제 취소를 한 후, 다시 시도하려면 타임아웃 시간을 넘어야 한다.
            if(gap < LEARNING_TIME_OUT)
            {
                //몇 초를 더 기다려야 하는지 사용자에게 알림.
                long remain = lastRequestTimeStamp + LEARNING_TIME_OUT - currentTimeStamp;

                String msg = String.format("You can request a re-learning %.1f second later.", (float)remain/1000);
                DialogUtil.yesNo(DiagnosisActivity.this, "Do you want to force learning?", msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        guideLearning();
                    }
                }, null);
                return;
            }
            else
            {
                //타임아웃 시간이 넘었으면, 시간 정보를 없애고 러닝 시도
                lastRequestTimeStampMap.remove(linkedAddress);
            }
        }

        guideLearning();

    }

    private void guideLearning(){
        DialogUtil.yesNo(this,
                "Do you want to run the learning function?",
                "The more LearningCount, the longer the learning time.\n" +
                        "During the learning, the measurement function pauses.\n" +
                        String.format("It may take up to %.0f seconds", (float)LEARNING_TIME_OUT/1000),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLearning();
                    }
                },
                null
        );
    }

    private void doLearning(){

        //시작 시간 기록
        final long startTimeStamp = System.currentTimeMillis();
        synchronized (lastRequestTimeStampMap){
            lastRequestTimeStampMap.put(linkedAddress, startTimeStamp);
        }

        //요청
        SendCommand.canLearning = true;
        svs.addSendCommand_Init(LEARNING);
        svs.addSendCommand_Init(UPLOAD);

        //대기
        progressDialogUtil = new ProgressDialogUtil(this,
                "Learning..",
                "Please wait. It can not be canceled during learning.");

        progressDialogUtil.show();


        //타임 아웃 타이머
        Message msg = new Message();
        msg.what = MESSAGE_LEARNING;
        timeoutHandler.sendMessageDelayed(msg, LEARNING_TIME_OUT);
    }

    //블루투스로 부터 새로운 센서 정보 업데이트를 할지 묻기
    private void guideUploadDataUpdating(){
        DialogUtil.yesNo(this,
                "Do you want to update the sensor information?",
                        "During the update, the measurement function pauses.\n" +
                        String.format("It may take up to %.0f seconds", (float)UPLOAD_TIME_OUT/1000),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doUploadDataUpdating();
                    }
                },
                null
        );
    }


    private void doUploadDataUpdating(){

        //시작 시간 기록
        final long startTimeStamp = System.currentTimeMillis();
        synchronized (lastRequestTimeStampMap){
            lastRequestTimeStampMap.put(linkedAddress, startTimeStamp);
        }

        //요청
        svs.addSendCommand_Init(UPLOAD);

        //대기
        progressDialogUtil = new ProgressDialogUtil(this,
                "Updating Sensor Info..",
                "Please wait. It can not be canceled during update.");
        progressDialogUtil.show();


        //타임 아웃 타이머
        Message msg = timeoutHandler.obtainMessage(MESSAGE_UPLOADING);
        timeoutHandler.sendMessageDelayed(msg, UPLOAD_TIME_OUT);
    }

    private final int MESSAGE_LEARNING = 1;
    private final int MESSAGE_UPLOADING = 2;
    private Handler timeoutHandler = new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg) {

            if(msg.what == MESSAGE_LEARNING)
            {
                synchronized (lastRequestTimeStampMap)
                {
                    if(lastRequestTimeStampMap.containsKey(linkedAddress))
                    {
                        long timeStamp = lastRequestTimeStampMap.get(linkedAddress);
                        long currentTimeStamp = System.currentTimeMillis();
                        long flowTimeStamp = currentTimeStamp - timeStamp;

                        //시간 제거
                        lastRequestTimeStampMap.remove(linkedAddress);

                        //다이얼로그 제거
                        if(progressDialogUtil != null){
                            progressDialogUtil.hide();
                            progressDialogUtil = null;
                        }

                        //학습 커맨드 강제 종료
                        SendCommandPacket learningCommand = svs.getCurrentSendCommand();
                        if(learningCommand != null){
                            if(learningCommand.getType() == LEARNING)
                            {
                                learningCommand.setStatus(SVSTRASACTION_DONE);
                                svs.delSendCommand_Done();
                            }
                        }

                        //업로드 커맨드 강제 종료
                        SendCommandPacket uploadCommand = svs.getCurrentSendCommand();
                        if(uploadCommand != null){
                            if(uploadCommand.getType() == UPLOAD)
                            {
                                uploadCommand.setStatus(SVSTRASACTION_DONE);
                                svs.delSendCommand_Done();
                            }
                        }

                        //Learning 다시 할 수 있게 변경
                        SendCommand.canLearning = true;

                        //실패 알림

                        if(!DiagnosisActivity.this.isFinishing()) {
                            DialogUtil.confirm(DiagnosisActivity.this,
                                    "TimeOut",
                                    "Learning Fail. Please check the device and request again.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }
                            );
                        }


                    }
                }
            }

            else if(msg.what == MESSAGE_UPLOADING)
            {
                synchronized (lastRequestTimeStampMap)
                {
                    if(lastRequestTimeStampMap.containsKey(linkedAddress))
                    {
                        long timeStamp = lastRequestTimeStampMap.get(linkedAddress);
                        long currentTimeStamp = System.currentTimeMillis();
                        long flowTimeStamp = currentTimeStamp - timeStamp;

                        //시간 제거
                        lastRequestTimeStampMap.remove(linkedAddress);

                        //다이얼로그 제거
                        if(progressDialogUtil != null){
                            progressDialogUtil.hide();
                            progressDialogUtil = null;
                        }

                        //업로드 커맨드 강제 종료
                        SendCommandPacket uploadCommand = svs.getCurrentSendCommand();
                        if(uploadCommand != null){
                            if(uploadCommand.getType() == UPLOAD)
                            {
                                uploadCommand.setStatus(SVSTRASACTION_DONE);
                                svs.delSendCommand_Done();
                            }
                        }

                        //실패 알림
                        if(!DiagnosisActivity.this.isFinishing()) {
                            DialogUtil.confirm(DiagnosisActivity.this,
                                    "TimeOut",
                                    "Updating Fail. Please check the device and request again.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }
                            );
                        }


                    }
                }
            }

            return false;
        }
    });

    //센서 정보 업로드 할지 물어보기
    private void guideUploadToWebManager(){
        DialogUtil.yesNo(thisActivity, "Upload Sensor Infomation", "Do you want to upload the sensor information you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doUploadToWebManager();
            }
        }, null);
    }

    //센서 정보 업로드
    private void doUploadToWebManager(){

        final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(thisActivity, "Upload Sensor information", "Data transfer is in progress. Please wait.");
        progressDialogUtil.show();

        TCPSendUtil.sendConfig(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {


                progressDialogUtil.hide();

                DialogUtil.confirm(thisActivity, "Success", "Successfully uploaded sensor information to the server", null);

            }

            @Override
            public void onFailed(String tag, String msg) {
                progressDialogUtil.hide();

                DialogUtil.yesNo(thisActivity, "Failed to upload sensor information. Try again?", msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //재업로드 로직
                        doUploadToWebManager();
                    }
                }, null);

            }
        });
    }
}
