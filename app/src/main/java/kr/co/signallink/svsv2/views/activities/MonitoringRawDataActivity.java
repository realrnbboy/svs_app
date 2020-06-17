package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmObject;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefComboBox;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.dto.SVSAxisBuf;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.server.OnTCPSendCallback;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.ProgressDialogUtil;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.ToneUtil;

import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_RATIO;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_TIME_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.RAW_FREQ;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.RAW_TIME;

/**
 * Created by nspil on 2018-02-20.
 */

public class MonitoringRawDataActivity extends BaseActivity{

    public static final String TAG = "MonitoringRawDataActivity";
    private SVS svs = SVS.getInstance();

    private CombinedChart combinedChart04;
    private CombinedChart combinedChart05;

    private TextView tvTimeStamp;

    private ProgressBar progressBar;
    private Button button_trend_search;
    private Button button_record;
    private Button btnStart;
    private TextView tvProcessingState;
    private Spinner measure_option_spinner;
    private Button btnUploadToWebManager;


    //StartButton
    public static boolean startEnabled = true;

    //UploadData
    private UploadData uploadData = null;

    //UploadCheck
    private static boolean isUpload = false;

    //Measure
    private DefConstant.MEASURE_OPTION[] measureOptions = null;
    private DefConstant.MEASURE_OPTION currentMeasureOption = DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ;
    private ArrayAdapter<DefConstant.MEASURE_OPTION> arrayAdapter;

    //UUID
    private String svsUuid = null;

    float[] samplingRateTime = new float [MEASURE_AXIS_TIME_ELE_MAX];      // added by hslee 2020.06.17
    float[] samplingRateFreq = new float [MEASURE_AXIS_FREQ_ELE_MAX];      // added by hslee 2020.06.17

    public static Activity thisActivity;

    private final BroadcastReceiver StatusChangeReceiverOnChart = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.UPLOAD_ARRIVE)) {
                handler.sendEmptyMessage(0);
            }
            else if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {
                handler.sendEmptyMessage(0);
            }
            else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                        finish();
                    }
                });

                //버튼 잠금
                startEnabled = true;
                btnStart.setEnabled(startEnabled);
                btnUploadToWebManager.setEnabled(!startEnabled);
            }
            else if(action.equals(DefBLEdata.MEASURE_PERCENT)){
                ArrayList<String> strings = intent.getStringArrayListExtra("datas");

                if(strings != null && strings.size() > 0)
                {
                    //퍼센트
                    String strPercent = strings.get(0);
                    int iProgress = Integer.parseInt(strPercent);
                    progressBar.setProgress(iProgress);

                    //버튼 잠금 해제
                    String strHeaderLength = strings.get(1);
                    int headerLength = Integer.parseInt(strHeaderLength);
                    if(iProgress >= 100 && headerLength > 85) //100퍼센트 이상, 헤더 길이가 Trend(85)가 아닐때만 잠금 풀기
                    {
                        isUpload = false;
                        startEnabled = true;
                        btnStart.setEnabled(startEnabled);//버튼 잠금 풀기
                        btnUploadToWebManager.setEnabled(!startEnabled);
                    }
                }

            }
        }
    };

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            update();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_rawdata_activity);

        Toolbar svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.monitoring2_screen);

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnChart, makeUpdateIntentFilter());

        thisActivity = this;

        initMeasureData();
        initView();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //버튼 잠금
                startEnabled = false;
                btnStart.setEnabled(startEnabled);

                //Toast
                ToastUtil.showLong("Will begin in the next measurement cycle");

                //Request RawData by Option.
                DatabaseUtil.transaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        RealmObject realmObject = svs.getLinkedSvsData();

                        if(realmObject instanceof  SVSEntity)
                        {
                            SVSEntity svsEntity = (SVSEntity)realmObject;
                            //svsEntity.setMeasureOption(currentMeasureOption);
                            svsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ); //강제 All
                            svsEntity.setMeasureOptionCount(1);
                        }
                        else
                        {
                            WSVSEntity wsvsEntity = (WSVSEntity)realmObject;
                            wsvsEntity.setMeasureOption(currentMeasureOption);
                            wsvsEntity.setMeasureOption(DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ); //강제 All
                            wsvsEntity.setMeasureOptionCount(1);
                        }

                    }
                });
            }
        });
        btnStart.setEnabled(startEnabled);


        button_trend_search = (Button) findViewById(R.id.button_control_ble);
        button_trend_search.setText("Info");
        button_trend_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(svs.getLinkedSvsUuid() != null)
            {
                IntentUtil.goActivity(MonitoringRawDataActivity.this, DiagnosisActivity.class);
            }
            else
            {
                ToastUtil.showShort("Please connect to the device.");
            }

            }
        });



        //초기 Measure 스피너 인덱스
        int initMeasureOptionSpinnerIndex = 0;
        for(int i=0; i<measureOptions.length; i++){
            if(measureOptions[i] == currentMeasureOption){
                initMeasureOptionSpinnerIndex = i;
                break;
            }
        }

        //Measure
        arrayAdapter = new ArrayAdapter<DefConstant.MEASURE_OPTION>(this, R.layout.spinner_item, measureOptions);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measure_option_spinner = findViewById(R.id.measure_option_spinner);
        measure_option_spinner.setAdapter(arrayAdapter);
        measure_option_spinner.setSelection(initMeasureOptionSpinnerIndex);
        measure_option_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {

                DefConstant.MEASURE_OPTION measureOption = measureOptions[position];

                if(measureOption != currentMeasureOption)
                {
                    currentMeasureOption = measureOption;

                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmObject realmObject = svs.getLinkedSvsData();

                            if(realmObject instanceof  SVSEntity)
                            {
                                SVSEntity svsEntity = (SVSEntity)realmObject;
                                svsEntity.setMeasureOption(currentMeasureOption);
                            }
                            else
                            {
                                WSVSEntity wsvsEntity = (WSVSEntity)realmObject;
                                wsvsEntity.setMeasureOption(currentMeasureOption);
                            }
                        }
                    });
                }

            }
            public void onNothingSelected(AdapterView<?>  parent) {
            }
        });

        //Sensor Info Upload to Web Manager
        btnUploadToWebManager = findViewById(R.id.btnUploadToWebManager);
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

        update();
        checkUploadToWebManager(null);
    }

    private void goIntent(DefConstant.TrendValue trendValue){

        if(trendValue != null)
        {
            goIntent(MonitoringDetailActivity.class, false, EXTRA_STR_TREND_VALUE, trendValue);
        }

    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        thisActivity = null;

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnChart);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        for(int i=0;combinedChart04.getData() != null && i<combinedChart04.getData().getDataSetCount();i++) {
            combinedChart04.getData().removeDataSet(combinedChart04.getData().getDataSetByIndex(i));
        }

        for(int i=0;combinedChart05.getData() != null && i<combinedChart05.getData().getDataSetCount();i++) {
            combinedChart05.getData().removeDataSet(combinedChart05.getData().getDataSetByIndex(i));
        }

        combinedChart04.setUnbindEnabled(true);
        combinedChart05.setUnbindEnabled(true);

        combinedChart04 = null;
        combinedChart05 = null;

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.UPLOAD_ARRIVE);
        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);
        intentFilter.addAction(DefBLEdata.MEASURE_PERCENT);

        return intentFilter;
    }

    private void initMeasureData()
    {
        //Find SVSEntity
        RealmObject realmObject = svs.getLinkedSvsData();
        if(realmObject instanceof  SVSEntity)
        {
            SVSEntity svsEntity = (SVSEntity)realmObject;

            if(svsEntity != null)
            {
                svsUuid = svsEntity.getUuid();
                currentMeasureOption = svsEntity.getMeasureOption();

                if(svsEntity.getFwVer() >= DefConstant.FwVer.SupportRawMeasure.getFwVer())
                {
                    measureOptions = new DefConstant.MEASURE_OPTION[]{
                            //DefConstant.MEASURE_OPTION.RAW_NONE,
                            DefConstant.MEASURE_OPTION.RAW_WITH_TIME,
                            DefConstant.MEASURE_OPTION.RAW_WITH_FREQ,
                            DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ
                    };
                }
                else
                {
                    measureOptions = new DefConstant.MEASURE_OPTION[]{
                            //DefConstant.MEASURE_OPTION.MEASURE_V4
                    };
                }
            }
            else
            {
                ToastUtil.showLong("Could not get information from connected device.");
                finish();
            }
        }
        else
        {
            WSVSEntity wsvsEntity = (WSVSEntity)realmObject;

            if(wsvsEntity != null)
            {
                svsUuid = wsvsEntity.id;
                currentMeasureOption = wsvsEntity.getMeasureOption();

                if(wsvsEntity.getFwVer() >= DefConstant.FwVer.SupportRawMeasure.getFwVer())
                {
                    measureOptions = new DefConstant.MEASURE_OPTION[]{
                            //DefConstant.MEASURE_OPTION.RAW_NONE,
                            DefConstant.MEASURE_OPTION.RAW_WITH_TIME,
                            DefConstant.MEASURE_OPTION.RAW_WITH_FREQ,
                            DefConstant.MEASURE_OPTION.RAW_WITH_TIME_FREQ
                    };
                }
                else
                {
                    measureOptions = new DefConstant.MEASURE_OPTION[]{
                            //DefConstant.MEASURE_OPTION.MEASURE_V4
                    };
                }
            }
            else
            {
                ToastUtil.showLong("Could not get information from connected device.");
                finish();
            }
        }

    }

    private void initView(){

        tvTimeStamp = (TextView)findViewById(R.id.tvTimeStamp);

        combinedChart04 = (CombinedChart) findViewById(R.id.chart04_CombinedChart_chartraw);
        combinedChart05 = (CombinedChart) findViewById(R.id.chart05_CombinedChart_chartraw);

        combinedChart04.setOnChartGestureListener(OCGL04);
        combinedChart04.setNoDataText(getResources().getString(R.string.collectingchartdata));

        combinedChart05.setOnChartGestureListener(OCGL05);
        combinedChart05.setNoDataText(getResources().getString(R.string.collectingchartdata));

        ArrayList<CombinedChart> chartList = new ArrayList<>();
        chartList.add(combinedChart04);
        chartList.add(combinedChart05);

        for(CombinedChart chart : chartList){
            chart.getDescription().setEnabled(false);
            chart.setBackgroundColor(getResources().getColor(R.color.colorContent));
            chart.setDrawGridBackground(false);
            chart.setDrawBarShadow(false);
            chart.setHighlightFullBarEnabled(false);

            Legend l = chart.getLegend();
            l.setWordWrapEnabled(true);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setTextColor(Color.WHITE);
            l.setEnabled(true);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setTextColor(Color.WHITE);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setTextColor(Color.WHITE);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelCount(25);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(Color.WHITE);

            if(chart == combinedChart04)
            {
                applyXAxisOptions(xAxis, RAW_TIME);
            }
            else if(chart == combinedChart05)
            {
                applyXAxisOptions(xAxis, RAW_FREQ);
            }


        }

        tvProcessingState = (TextView)findViewById(R.id.tvProcessingState);
        TimerTask task = new TimerTask() {

            boolean light = true;

            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!startEnabled){

                            tvProcessingState.setVisibility(View.VISIBLE);
                        }
                        else {
                            tvProcessingState.setVisibility(View.GONE);
                        }

                        tvProcessingState.setTextColor(light ? Color.WHITE : Color.TRANSPARENT);
                        light = !light;
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 500);

    }

    // added by hslee 2020.06.17
    private void makeXAxisFloatValue() {

        try {
            int size = svs.getMeasureDatas().size();

            if( size > 0 ) {
                ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
                int len = measureDatas.size();

                int count = -1;

                //가장 최근 데이터부터 검사
                for(int i=len-1; i>=0; i--) {
                    MeasureData measureData = measureDatas.get(i);

                    SVSAxisBuf axisBuf = measureData.getAxisBuf();
                    if(axisBuf.getInputFreqLength() > 0 || axisBuf.getInputTimeLength() > 0) {  // 값을 있을 경우만 사용
                        float tSamplingRateTime = 0;
                        for( int j = 0; j < MEASURE_AXIS_TIME_ELE_MAX; j++ ) {
                            tSamplingRateTime = (1 / measureData.getfSplFreqMes() + tSamplingRateTime);
                            samplingRateTime[j] = tSamplingRateTime;
                        }

                        float tSamplingRateFreq = 0;
                        for( int j = 0; j < MEASURE_AXIS_FREQ_ELE_MAX; j++ ) {
                            tSamplingRateFreq = (measureData.getfSplFreqMes() / 1024 / 2 + tSamplingRateFreq);
                            samplingRateFreq[j] = tSamplingRateFreq;
                        }
                        break;
                    }
                }
            }

        } catch (Exception e) {
            DefLog.d(TAG, e.toString());
        }
    }

    private void applyXAxisOptions(XAxis xAxis, final DefConstant.TrendValue trendValue)
    {
        //final int xAxisDataSize = trendValue == RAW_TIME ? 512 /*MEASURE_AXIS_TIME_ELE_MAX*/ : MEASURE_AXIS_FREQ_ELE_MAX;
        final int xAxisDataSize = trendValue == RAW_TIME ? MEASURE_AXIS_TIME_ELE_MAX /*MEASURE_AXIS_TIME_ELE_MAX*/ : MEASURE_AXIS_FREQ_ELE_MAX;   // added by hslee 2020.06.17

        xAxis.setGranularity(xAxisDataSize / 4);
        xAxis.setAxisMaximum(xAxisDataSize);   // added by hslee 2020.06.17
        //xAxis.setAxisMaximum(xAxisDataSize * MEASURE_AXIS_RATIO);

        if(trendValue == RAW_TIME)
        {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    String str = "";

                    int index = (int)value;

                    int measureIndex = index / xAxisDataSize;
                    //index 보정하기 (MeasureIndex가 0이 아니어서 (index가 xAxisDataSize랑 같아서 1이 되버림), 처음 RawData를 보여줄때, 마지막 인덱스 값이 출력이 안되는 버그가 있었음)
                    if(index == xAxisDataSize){
                        measureIndex = 0;
                    }

                    try {
                        int size = svs.getMeasureDatas().size();

                        if(measureIndex < size)
                        {
                            ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
                            int len = measureDatas.size();

                            int count = -1;

                            //가장 최근 데이터부터 검사
                            for(int i=len-1; i>=0; i--)
                            {
                                MeasureData measureData = measureDatas.get(i);

                                SVSAxisBuf axisBuf = measureData.getAxisBuf();
                                if(axisBuf.getInputFreqLength() > 0 || axisBuf.getInputTimeLength() > 0)
                                {
                                    count++;
                                }

                                if(measureIndex == count)
                                {
                                    //시간 표기
                                    Date date = measureData.getCaptureTime();
                                    String strDate = DateUtil.convertDate(date, "HH:mm:ss");
                                    tvTimeStamp.setText("Time "+strDate);

                                    //x축 표기
//                                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
//                                    float xValue = index / samplingFreqHz;
//                                    str = ""+ StringUtil.decimalFormatDot3(xValue);   // added by hslee 2020.06.17

                                    //samplingRateTime = (1 / measureData.getfSplFreqMes() + samplingRateTime);
                                    str = value == 2048 ? String.valueOf(samplingRateTime[(int)value-1]) : String.valueOf(samplingRateTime[(int)value]);   // added by hslee 2020.06.17

                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        DefLog.d(TAG, e.toString());
                    }

                    return str;
                }
            });
        }
        else if(trendValue == RAW_FREQ)
        {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    String str = "";

                    int index = (int)value;

                    int measureIndex = index / xAxisDataSize;
                    //index 보정하기 (MeasureIndex가 0이 아니어서 (index가 xAxisDataSize랑 같아서 1이 되버림), 처음 RawData를 보여줄때, 마지막 인덱스 값이 출력이 안되는 버그가 있었음)
                    if(index == xAxisDataSize){
                        measureIndex = 0;
                    }

                    try {
                        int size = svs.getMeasureDatas().size();

                        if(measureIndex < size)
                        {
                            ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
                            int len = measureDatas.size();

                            int count = -1;

                            //가장 최근 데이터부터 검사
                            for(int i=len-1; i>=0; i--)
                            {
                                MeasureData measureData = measureDatas.get(i);

                                SVSAxisBuf axisBuf = measureData.getAxisBuf();
                                if(axisBuf.getInputFreqLength() > 0 || axisBuf.getInputTimeLength() > 0)
                                {
                                    count++;
                                }

                                if(measureIndex == count)
                                {
                                    //시간 표기
                                    Date date = measureData.getCaptureTime();
                                    String strDate = DateUtil.convertDate(date, "HH:mm:ss");
                                    //tvFreqDomainTimeStamp.setText("Capture "+strDate);

                                    //x축 표기
//                                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
//                                    float xValue = samplingFreqHz * index / xAxisDataSize;
//                                    str = ""+ StringUtil.decimalFormatDot0(xValue);      // added by hslee 2020.06.17

                                    str = value == 1024 ? String.valueOf(samplingRateFreq[(int)value-1]) : String.valueOf(samplingRateFreq[(int)value]);       // added by hslee 2020.06.17
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        DefLog.d(TAG, e.toString());
                    }

                    return str;
                }
            });
        }

    }

    private OnChartGestureListener OCGL04 = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart04.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.RAW_TIME;
            goIntent(tappedTrendValue);
        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {

        }
    };

    private OnChartGestureListener OCGL05 = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart04.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.RAW_FREQ;
            goIntent(tappedTrendValue);
        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

        }

        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

        }

        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {

        }
    };

    public void update() {

        makeXAxisFloatValue();  // added by hslee   2020.06.17

        uploadData = svs.getUploaddata();

        try {
            ArrayList<MeasureData> measureDatas = SVS.getInstance().getMeasureDatas();
            int size = measureDatas.size();

            if(size >= 1)
            {
                //그래프 업데이트
                combinedChart04.setData(generateChart04Date(measureDatas));
                combinedChart04.invalidate();

                combinedChart05.setData(generateChart05Date(measureDatas));
                combinedChart05.invalidate();


                //마지막으로 AxisBuf에 실제 데이터를 가지고 있는 MeasureData 찾기
                MeasureData data = null;
                for(int i=size-1; i>=0; i--)
                {
                    MeasureData temp = measureDatas.get(i);

                    SVSAxisBuf axisBuf = temp.getAxisBuf();
                    if(axisBuf.getInputFreqLength() > 0 || axisBuf.getInputTimeLength() > 0)
                    {
                        data = temp;
                        break;
                    }
                }

                //Raw데이터에 대한 비프음
                if(data != null)
                {
                    checkBeep(data);

                    String msg = "CHART : update";
                    DefLog.d(TAG, msg);
                }

                checkUploadToWebManager(data);
            }
        } catch (Exception ex) {

        }
    }

    private CombinedData generateChart04Date(ArrayList<MeasureData> measureDatas){

        ArrayList<Float> valueList = new ArrayList<>();

        int showCount = MEASURE_AXIS_RATIO;
        int len = measureDatas.size();

        for(int i=len-1; i>=0 && showCount>0; i--)
        {
            MeasureData measureData = measureDatas.get(i);

            SVSAxisBuf axisBuf = measureData.getAxisBuf();

            //특정 시간의 MeasureData안에 Time이던 Freq이던 한가지라도 있으면 데이터를 출력
            if(axisBuf.getInputTimeLength() > 0 || axisBuf.getInputFreqLength() > 0)
            {
                for(float time : axisBuf.getfTime()){
                    valueList.add(time);
                }

                showCount--;
            }
        }

        //남은 횟수 만큼, 빈 버퍼를 채우기
        for(int i=0; i<showCount; i++)
        {
            int emptyCount = MEASURE_AXIS_TIME_ELE_MAX;
            while(emptyCount >= 0)
            {
                valueList.add((float)0);
                emptyCount--;
            }
        }

        LineData lineData = new LineData();
        lineData.addDataSet(generateLineRawData(DefConstant.TrendValue.RAW_TIME.toString(), valueList));
        lineData.setHighlightEnabled(false);

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);

        return combinedData;
    }

    private CombinedData generateChart05Date(ArrayList<MeasureData> measureDatas){

        ArrayList<Float> valueList = new ArrayList<>();

        int showCount = MEASURE_AXIS_RATIO;
        int len = measureDatas.size();

        for(int i=len-1; i>=0 && showCount>0; i--)
        {
            MeasureData measureData = measureDatas.get(i);

            SVSAxisBuf axisBuf = measureData.getAxisBuf();

            //특정 시간의 MeasureData안에 Time이던 Freq이던 한가지라도 있으면 데이터를 출력
            if(axisBuf.getInputTimeLength() > 0 || axisBuf.getInputFreqLength() > 0)
            {
                for(float freq : axisBuf.getfFreq()){
                    valueList.add(freq);
                }

                showCount--;
            }
        }

        //남은 횟수 만큼, 빈 버퍼를 채우기
        for(int i=0; i<showCount; i++)
        {
            int emptyCount = MEASURE_AXIS_FREQ_ELE_MAX;
            while(emptyCount >= 0)
            {
                valueList.add((float)0);
                emptyCount--;
            }
        }

        LineData lineData = new LineData();
        lineData.addDataSet(generateLineRawData(DefConstant.TrendValue.RAW_FREQ.toString(), valueList));
        lineData.setHighlightEnabled(false);

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);

        return combinedData;
    }


    private LineDataSet generateLineRawData(String label, ArrayList<Float> valueList){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new MyValueFormatter());

        return lineDataSet;
    }

    private void checkBeep(MeasureData data) {

        SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

        if(svsCode.getTimeEna().getdPeak() != 0) {
            if(data.getSvsTime().getdPeak() > svsCode.getTimeWrn().getdPeak()) {
                ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                return;
            }
        }

        if(svsCode.getTimeEna().getdRms() != 0) {
            if(data.getSvsTime().getdRms() > svsCode.getTimeWrn().getdRms()) {
                ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                return;
            }
        }

        if(svsCode.getTimeEna().getdCrf() != 0) {
            if(data.getSvsTime().getdCrf() > svsCode.getTimeWrn().getdCrf()) {
                ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                return;
            }
        }

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++){

            if(svsCode.getFreqEna()[i].getdPeak() != 0) {
                if(data.getSvsFreq()[i].getdPeak() > svsCode.getFreqWrn()[i].getdPeak()) {
                    ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                    return;
                }

            }

            if(svsCode.getFreqEna()[i].getdBnd() != 0) {
                if(data.getSvsFreq()[i].getdBnd() > svsCode.getFreqWrn()[i].getdBnd()) {
                    ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                    return;
                }
            }
        }
    }

    //RawData 업로드 가능한지 체크
    private void checkUploadToWebManager(MeasureData data){

        boolean enabled = false;

        if(data != null)
        {
            SVSAxisBuf axisBuf = data.getAxisBuf();
            if(axisBuf.getInputFreqLength() > 0 && axisBuf.getInputTimeLength() > 0)
            {
                enabled = true;
            }
        }

        //적용
        enableUploadToWebManager(enabled);
    }

    private void enableUploadToWebManager(boolean enabled){

        btnUploadToWebManager.setEnabled(enabled);

        if(enabled)
        {
            if(!isUpload){
                btnUploadToWebManager.setText("Upload Raw Data to Web Manager");
            }
            else {
                btnUploadToWebManager.setText("Re Upload Raw Data to Web Manager");
            }
        }
        else
        {
            btnUploadToWebManager.setText("To upload, please press the 'START' button and try.");
        }
    }


    //RawData 업로드 할지 물어보기
    private void guideUploadToWebManager(){

        //업로드 체크
        if(!isUpload)
        {
            //업로드 팝업
            DialogUtil.yesNo(thisActivity, "Upload Raw Data", "Do you want to upload the data you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doUploadToWebManager();
                }
            }, null);
        }
        else
        {
            //재업로드 팝업
            DialogUtil.yesNo(thisActivity, "Re Upload Raw Data", "Do you want to upload the data you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doUploadToWebManager();
                }
            }, null);
        }
    }

    //RawData 업로드
    private void doUploadToWebManager(){

        final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(thisActivity, "Upload Raw Data", "Data transfer is in progress. Please wait.");
        progressDialogUtil.show();

        TCPSendUtil.sendRawMeasure(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {

                progressDialogUtil.hide();

                DialogUtil.confirm(thisActivity, "Success", "Successfully uploaded raw data to the server", null);

                isUpload = true;
                enableUploadToWebManager(true);
            }

            @Override
            public void onFailed(String tag, String msg) {
                progressDialogUtil.hide();

                DialogUtil.confirm(thisActivity, "Failed to upload raw data to the server.", msg, null);

            }
        });
    }

}
