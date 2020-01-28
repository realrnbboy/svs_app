package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;

import io.realm.RealmObject;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.ToneUtil;

import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND1;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND2;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND3;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND4;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND5;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.BAND6;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.DCRF;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.DPEAK;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.DRMS;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK1;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK2;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK3;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK4;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK5;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.PEAK6;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.TEMPERATURE;

/**
 * Created by nspil on 2018-02-20.
 */

public class MonitoringTrendActivity extends BaseActivity{

    public static final String TAG = "MonitoringTrendActivity";
    private SVS svs = SVS.getInstance();

    private String[] chart01XValues = {
            "",
            DPEAK.toString(),
            DRMS.toString(),
            DCRF.toString(),
            "",
    };

    private String[] chart02XValues = {
            "",
            PEAK1.toString(),
            PEAK2.toString(),
            PEAK3.toString(),
            PEAK4.toString(),
            PEAK5.toString(),
            PEAK6.toString(),
            "",
    };

    private String[] chart03XValues = {
            "",
            BAND1.toString(),
            BAND2.toString(),
            BAND3.toString(),
            BAND4.toString(),
            BAND5.toString(),
            BAND6.toString(),
            "",
    };

    private CombinedChart combinedChart01;
    private CombinedChart combinedChart02;
    private CombinedChart combinedChart03;

    private ProgressBar progressBar;
    private Button button_trend_search;
    private Button button_record;
    private TextView textview_temperature;

    public static Activity thisActivity;

    private boolean showRawChart = false;

    private final BroadcastReceiver StatusChangeReceiverOnChart = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.UPLOAD_ARRIVE)) {
                handler.sendEmptyMessage(0);
            }
            else if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {
                handler.sendEmptyMessage(0);
            } else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                        finish();
                    }
                });
            } else if(action.equals(DefBLEdata.MEASURE_PERCENT)){
                ArrayList<String> strings = intent.getStringArrayListExtra("datas");

                if(strings != null && strings.size() > 0)
                {
                    String strData = strings.get(0);
                    int iProgress = Integer.parseInt(strData);

                    progressBar.setProgress(iProgress);
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
        setContentView(R.layout.chart_trend_activity);

        Toolbar svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.monitoring1_screen);

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnChart, makeUpdateIntentFilter());

        thisActivity = this;

        init();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        //로컬 모드 일때만, 녹화버튼 활성화
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            button_record = findViewById(R.id.button_record);
            button_record.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {

                    goIntent(ChartRecordActivity.class, true);
                    svs.setRecorded(true);
                }
            });
        }

        textview_temperature = (TextView) findViewById(R.id.temperature_chartraw);
        textview_temperature.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View view) {

                goIntent(TEMPERATURE);

            }
        });


        button_trend_search = (Button) findViewById(R.id.button_control_ble);
        button_trend_search.setText("Info");
        button_trend_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

            if(svs.getLinkedSvsUuid() != null)
            {
                IntentUtil.goActivity(MonitoringTrendActivity.this, DiagnosisActivity.class);
            }
            else
            {
                ToastUtil.showShort("Please connect to the device.");
            }

            }
        });

        update();
    }

    private void goIntent(DefConstant.TrendValue trendValue){

        if(trendValue != null)
        {
            goIntent(MonitoringDetailActivity.class, false, EXTRA_STR_TREND_VALUE, trendValue);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        thisActivity = null;

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnChart);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        for(int i=0;combinedChart01.getData() != null && i<combinedChart01.getData().getDataSetCount();i++) {
            combinedChart01.getData().removeDataSet(combinedChart01.getData().getDataSetByIndex(i));
        }

        for(int i=0;combinedChart02.getData() != null && i<combinedChart02.getData().getDataSetCount();i++) {
            combinedChart02.getData().removeDataSet(combinedChart02.getData().getDataSetByIndex(i));
        }

        for(int i=0;combinedChart03.getData() != null && i<combinedChart03.getData().getDataSetCount();i++) {
            combinedChart03.getData().removeDataSet(combinedChart03.getData().getDataSetByIndex(i));
        }


        combinedChart01.setUnbindEnabled(true);
        combinedChart02.setUnbindEnabled(true);
        combinedChart03.setUnbindEnabled(true);

        combinedChart01 = null;
        combinedChart02 = null;
        combinedChart03 = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");


        //로컬 모드 일때만, 녹화버튼 활성화
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            RealmObject realmObject = svs.getLinkedEquipmentData();
            if (realmObject != null) {
                button_record = findViewById(R.id.button_record);
                button_record.setVisibility(View.VISIBLE);
            } else {
                button_record = findViewById(R.id.button_record);
                button_record.setVisibility(View.GONE);
            }
        }
        else
        {
            button_record = findViewById(R.id.button_record);
            button_record.setVisibility(View.GONE);
        }
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

    private void init(){
        combinedChart01 = (CombinedChart) findViewById(R.id.chart01_CombinedChart_chartraw);
        combinedChart02 = (CombinedChart) findViewById(R.id.chart02_CombinedChart_chartraw);
        combinedChart03 = (CombinedChart) findViewById(R.id.chart03_CombinedChart_chartraw);

        combinedChart01.setOnChartGestureListener(OCGL01);
        combinedChart01.setNoDataText(getResources().getString(R.string.collectingchartdata));

        combinedChart02.setOnChartGestureListener(OCGL02);
        combinedChart02.setNoDataText(getResources().getString(R.string.collectingchartdata));

        combinedChart03.setOnChartGestureListener(OCGL03);
        combinedChart03.setNoDataText(getResources().getString(R.string.collectingchartdata));

        ArrayList<CombinedChart> chartList = new ArrayList<>();
        chartList.add(combinedChart01);
        chartList.add(combinedChart02);
        chartList.add(combinedChart03);

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
            l.setEnabled(false);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setAxisMinimum(0.0f);
            rightAxis.setTextColor(Color.WHITE);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0.0f);
            leftAxis.setTextColor(Color.WHITE);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0.0f);
            xAxis.setLabelCount(25);
            xAxis.setTextColor(Color.WHITE);


            if(chart == combinedChart01){
                xAxis.setAxisMaximum(chart01XValues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart01XValues[(int)value % chart01XValues.length];
                    }
                });
            }else if(chart == combinedChart02){
                xAxis.setAxisMaximum(chart02XValues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart02XValues[(int)value % chart03XValues.length];
                    }
                });
            }else if(chart == combinedChart03){
                xAxis.setAxisMaximum(chart03XValues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart03XValues[(int)value % chart03XValues.length];
                    }
                });
            }
            xAxis.setGranularity(1.0f);
        }

    }

    private OnChartGestureListener OCGL01 = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart01.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

            float layerWidth = combinedChart01.getWidth()/4;
            SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();
            float [] spaceend = new float[2];
            spaceend[0] = layerWidth + layerWidth/2;
            spaceend[1] = spaceend[0] + layerWidth;

            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.UNKNOWN;
            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getTimeEna().getdPeak() != 0){
                tappedTrendValue = DPEAK;
            }else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getTimeEna().getdRms() != 0){
                tappedTrendValue = DRMS;
            }else if(me.getX() > spaceend[1] && svsCode.getTimeEna().getdCrf() != 0){
                tappedTrendValue = DCRF;
            }

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

    private OnChartGestureListener OCGL02 = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart02.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

            float layerWidth = combinedChart02.getWidth()/7;
            SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

            float [] spaceend = new float[5];
            spaceend[0] = layerWidth + layerWidth/2;
            spaceend[1] = spaceend[0] + layerWidth;
            spaceend[2] = spaceend[1] + layerWidth;
            spaceend[3] = spaceend[2] + layerWidth;
            spaceend[4] = spaceend[3] + layerWidth;


            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.UNKNOWN;
            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getFreqEna()[0].getdPeak() != 0){
                tappedTrendValue = PEAK1;
            }else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getFreqEna()[1].getdPeak() != 0){
                tappedTrendValue = PEAK2;
            }else if(me.getX() > spaceend[1] && me.getX() < spaceend[2] && svsCode.getFreqEna()[2].getdPeak() != 0){
                tappedTrendValue = PEAK3;
            }else if(me.getX() > spaceend[2] && me.getX() < spaceend[3] && svsCode.getFreqEna()[3].getdPeak() != 0){
                tappedTrendValue = PEAK4;
            }else if(me.getX() > spaceend[3] && me.getX() < spaceend[4] && svsCode.getFreqEna()[4].getdPeak() != 0){
                tappedTrendValue = PEAK5;
            }else if(me.getX() > spaceend[4] && svsCode.getFreqEna()[5].getdPeak() != 0){
                tappedTrendValue = PEAK6;
            }

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

    private OnChartGestureListener OCGL03 = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart03.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

            float layerWidth = combinedChart03.getWidth()/7;
            SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

            float [] spaceend = new float[5];
            spaceend[0] = layerWidth + layerWidth/2;
            spaceend[1] = spaceend[0] + layerWidth;
            spaceend[2] = spaceend[1] + layerWidth;
            spaceend[3] = spaceend[2] + layerWidth;
            spaceend[4] = spaceend[3] + layerWidth;


            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.UNKNOWN;
            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getFreqEna()[0].getdBnd() != 0){
                tappedTrendValue = BAND1;
            }
            else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getFreqEna()[1].getdBnd() != 0){
                tappedTrendValue = BAND2;
            }
            else if(me.getX() > spaceend[1] && me.getX() < spaceend[2] && svsCode.getFreqEna()[2].getdBnd() != 0){
                tappedTrendValue = BAND3;
            }
            else if(me.getX() > spaceend[2] && me.getX() < spaceend[3] && svsCode.getFreqEna()[3].getdBnd() != 0){
                tappedTrendValue = BAND4;
            }
            else if(me.getX() > spaceend[3] && me.getX() < spaceend[4] && svsCode.getFreqEna()[4].getdBnd() != 0){
                tappedTrendValue = BAND5;
            }
            else if(me.getX() > spaceend[4] && svsCode.getFreqEna()[5].getdBnd() != 0){
                tappedTrendValue = BAND6;
            }

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

        try {
            ArrayList<MeasureData> measureDatas = SVS.getInstance().getMeasureDatas();
            int size = measureDatas.size();

            if(size >= 1)
            {
                MeasureData data = measureDatas.get(size-1);

                String temp = String.format("Temp : %d \u00B0C",data.getlTempCurrent());
                textview_temperature.setText(temp);

                combinedChart01.setData(generateChart01Data(data));
                combinedChart01.invalidate();

                combinedChart02.setData(generateChart02Date(data));
                combinedChart02.invalidate();

                combinedChart03.setData(generateChart03Date(data));
                combinedChart03.invalidate();

                checkBeep(data);

                String msg = "CHART : update";
                DefLog.d(TAG, msg);
            }
        } catch (Exception ex) {

        }
    }

    private BarDataSet generateBarData(int xValue, float value, float valueMax, float valueWrn, float valueDan, boolean isLeftAxis){
        ArrayList<BarEntry> entriesBarTemperature = new ArrayList<>();

        entriesBarTemperature.add(new BarEntry(xValue, valueMax));
        entriesBarTemperature.add(new BarEntry(xValue, value));


        BarDataSet barDataSet = new BarDataSet(entriesBarTemperature, "");
        barDataSet.setAxisDependency(isLeftAxis ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
        barDataSet.setHighlightEnabled(false);
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueFormatter(new MyValueFormatter());

        int[] colors = new int[2];

        if(valueMax >= valueDan) {
            //colors[0] = Color.rgb(242,10,10);
            colors[0] = Color.parseColor("#3da5ff");
        } else if(valueMax >= valueWrn) {
            //colors[0] = Color.rgb(255,229,61);
            colors[0] = Color.parseColor("#3da5ff");
        } else {
            //colors[0] = Color.rgb(97,255,61);
            colors[0] = Color.parseColor("#3da5ff");
        }

        if(value >= valueDan) {
            colors[1] = Color.parseColor("#f20a0a");
        } else if(value >= valueWrn) {
            colors[1] = Color.parseColor("#ffe53d");
        } else {
            colors[1] = Color.parseColor("#61ff3d");
        }

        barDataSet.setColors(colors);

        return barDataSet;
    }

    private LineDataSet generateLineData(float xStartVal, float value, boolean isWrn, boolean isLeftAxis){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(xStartVal, value));
        entries.add(new Entry(xStartVal+1, value));

        LineDataSet lineDataSet = new LineDataSet(entries,"");
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(isWrn == true ? Color.YELLOW : Color.RED);
        lineDataSet.setAxisDependency(isLeftAxis == true ? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setValueFormatter(new MyValueFormatter());

        return lineDataSet;
    }

    private CombinedData generateChart01Data(MeasureData data){

        BarData barData = new BarData();
        barData.setBarWidth(0.7f);

        LineData lineData = new LineData();
        MeasureData measuredata_max = SVS.getInstance().getMeasuredata_max();

        SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

        if(svsCode.getTimeEna().getdPeak() != 0) {
            try {
                //draw chart1 barChartTimeCode peak
                BarDataSet barDataSet = generateBarData(1, data.getSvsTime().getdPeak(), measuredata_max.getSvsTime().getdPeak(),svsCode.getTimeWrn().getdPeak(),svsCode.getTimeDan().getdPeak(), true);
                barData.addDataSet(barDataSet);

                //draw chart1 lineChartTimeCodes peak Wrn
                LineDataSet lineDataSet1 = generateLineData(0.5f,  svsCode.getTimeWrn().getdPeak(), true, true);
                lineData.addDataSet(lineDataSet1);

                //draw chart1 lineChartTimeCodes peak Dan
                LineDataSet lineDataSet2 = generateLineData(0.5f,  svsCode.getTimeDan().getdPeak(), false, true);
                lineData.addDataSet(lineDataSet2);
            } catch (Exception ex) {

            }
        }

        if(svsCode.getTimeEna().getdRms() != 0) {
            try {
                //draw chart1 barChartTimeCode Rms
                BarDataSet barDataSet = generateBarData(2, data.getSvsTime().getdRms(), measuredata_max.getSvsTime().getdRms(),svsCode.getTimeWrn().getdRms(),svsCode.getTimeDan().getdRms(), true);
                barData.addDataSet(barDataSet);

                //draw chart1 lineChartTimeCodes Rms Wrn
                LineDataSet lineDataSet1 = generateLineData(1.5f,  svsCode.getTimeWrn().getdRms(), true, true);
                lineData.addDataSet(lineDataSet1);

                //draw chart1 lineChartTimeCodes Rms Dan
                LineDataSet lineDataSet2 = generateLineData(1.5f,  svsCode.getTimeDan().getdRms(), false, true);
                lineData.addDataSet(lineDataSet2);
            } catch (Exception ex) {

            }
        }

        if(svsCode.getTimeEna().getdCrf() != 0) {
            try {
                //draw chart1 barChartTimeCode Crf
                BarDataSet barDataSet = generateBarData(3, data.getSvsTime().getdCrf(), measuredata_max.getSvsTime().getdCrf(),svsCode.getTimeWrn().getdCrf(),svsCode.getTimeDan().getdCrf(), false);
                barData.addDataSet(barDataSet);

                //draw chart1 lineChartTimeCodes crf Wrn
                LineDataSet lineDataSet1 = generateLineData(2.5f,  svsCode.getTimeWrn().getdCrf(), true, false);
                lineData.addDataSet(lineDataSet1);

                //draw chart1 lineChartTimeCodes crf Dan
                LineDataSet lineDataSet2 = generateLineData(2.5f,  svsCode.getTimeDan().getdCrf(), false, false);
                lineData.addDataSet(lineDataSet2);
            } catch (Exception ex) {

            }
        }

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        lineData.setDrawValues(false);
        combinedData.setData(lineData);

        return combinedData;
    }


    private CombinedData generateChart02Date(MeasureData data){
        BarData barData = new BarData();
        barData.setBarWidth(0.7f);

        LineData lineData = new LineData();

        MeasureData measuredata_max = SVS.getInstance().getMeasuredata_max();
        SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

        //draw peak data
        BarDataSet barDataSet = null;
        LineDataSet lineDataSet;

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++){

            float xStartVal = i+0.5f;

            if(svsCode.getFreqEna()[i].getdPeak() != 0) {
                try {
                    barDataSet = generateBarData(i+1, data.getSvsFreq()[i].getdPeak(), measuredata_max.getSvsFreq()[i].getdPeak(),svsCode.getFreqWrn()[i].getdPeak(), svsCode.getFreqDan()[i].getdPeak(), true);
                    barDataSet.setHighlightEnabled(false);
                    barDataSet.setValueTextColor(Color.WHITE);
                    barData.addDataSet(barDataSet);

                    lineDataSet = generateLineData(xStartVal, svsCode.getFreqWrn()[i].getdPeak(), true, true);
                    lineDataSet.setHighlightEnabled(false);
                    lineData.addDataSet(lineDataSet);

                    lineDataSet = generateLineData(xStartVal, svsCode.getFreqDan()[i].getdPeak(), false, true);
                    lineDataSet.setHighlightEnabled(false);
                    lineData.addDataSet(lineDataSet);

                    Log.d("TTTT","Char02 a:"+data.getSvsFreq()[i].getdPeak()+",b:"+measuredata_max.getSvsFreq()[i].getdPeak()+",c:"+svsCode.getFreqWrn()[i].getdPeak()+",d:"+svsCode.getFreqDan()[i].getdPeak());


                } catch (Exception ex) {

                }
            }
        }

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        lineData.setDrawValues(false);
        combinedData.setData(lineData);

        return combinedData;
    }


    private CombinedData generateChart03Date(MeasureData data){
        BarData barData = new BarData();
        barData.setBarWidth(0.7f);

        LineData lineData = new LineData();

        MeasureData measuredata_max = SVS.getInstance().getMeasuredata_max();
        SVSCode svsCode = SVS.getInstance().getUploaddata().getSvsParam().getCode();

        //draw band data
        BarDataSet barDataSet = null;
        LineDataSet lineDataSet;

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++){

            float xStartVal = i+0.5f;

            if(svsCode.getFreqEna()[i].getdBnd() != 0) {
                try {
                    barDataSet = generateBarData(i+1, data.getSvsFreq()[i].getdBnd(), measuredata_max.getSvsFreq()[i].getdBnd(),svsCode.getFreqWrn()[i].getdBnd(), svsCode.getFreqDan()[i].getdBnd(), true);
                    barDataSet.setHighlightEnabled(false);
                    barDataSet.setValueTextColor(Color.WHITE);
                    barData.addDataSet(barDataSet);

                    lineDataSet = generateLineData(xStartVal, svsCode.getFreqWrn()[i].getdBnd(), true, true);
                    lineDataSet.setHighlightEnabled(false);
                    lineData.addDataSet(lineDataSet);

                    lineDataSet = generateLineData(xStartVal, svsCode.getFreqDan()[i].getdBnd(), false, true);
                    lineDataSet.setHighlightEnabled(false);
                    lineData.addDataSet(lineDataSet);
                } catch (Exception ex) {

                }
            }
        }

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        lineData.setDrawValues(false);
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

        SVSParam svsParam = SVS.getInstance().getUploaddata().getSvsParam();
        SVSCode svsCode = svsParam.getCode();
        boolean andFlag = svsParam.getnAndFlag() > 0 ? true : false;

        if(!andFlag)
        {
            //1개만 Warning이상 이라면 Beep가 울림

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
        else
        {
            //모든 조건이 Warning 이상이 되어야 Beep가 울림.

            boolean isOverWarning = true;

            if(data.getSvsTime().getdPeak() <= svsCode.getTimeWrn().getdPeak()) {
                isOverWarning = isOverWarning & false;
            }

            if(data.getSvsTime().getdRms() <= svsCode.getTimeWrn().getdRms()) {
                isOverWarning = isOverWarning & false;
            }

            if(data.getSvsTime().getdCrf() <= svsCode.getTimeWrn().getdCrf()) {
                isOverWarning = isOverWarning & false;
            }

            for(int i = 0; i< DefCMDOffset.BAND_MAX; i++)
            {
                if(data.getSvsFreq()[i].getdPeak() <= svsCode.getFreqWrn()[i].getdPeak()) {
                    isOverWarning = isOverWarning & false;
                }

                if(data.getSvsFreq()[i].getdBnd() <= svsCode.getFreqWrn()[i].getdBnd()) {
                    isOverWarning = isOverWarning & false;
                }
            }

            if(isOverWarning)
            {
                ToneUtil.play(ToneGenerator.TONE_PROP_BEEP);
                return;
            }
        }


    }

}
