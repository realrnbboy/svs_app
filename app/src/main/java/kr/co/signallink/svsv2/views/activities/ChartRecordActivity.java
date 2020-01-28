package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.user.SVS;

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
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.UNKNOWN;

/**
 * Created by nspil on 2018-02-20.
 */

public class ChartRecordActivity extends BaseActivity{

    public static final String TAG = "ChartRecordActivity";
    private SVS svs = SVS.getInstance();

    private String[] chart01Xvalues = {
            "",
            DPEAK.toString(),
            DRMS.toString(),
            DCRF.toString(),
            "",
    };

    private String[] chart02Xvalues = {
            "",
            PEAK1.toString(),
            PEAK2.toString(),
            PEAK3.toString(),
            PEAK4.toString(),
            PEAK5.toString(),
            PEAK6.toString(),
            "",
    };

    private String[] chart03Xvalues = {
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

    private Button button_trend_search;
    private Button button_record;
    private TextView alarm_chartrecord;
    private TextView textview_temperature;
    public ToneGenerator toneGenerator;
    private DefConstant.TrendValue currentTrendValue = UNKNOWN;

    public static Activity chartrecordActivity;
    private boolean checkrecordmax = false;

    private final BroadcastReceiver StatusChangeReceiverOnChart= new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {
                handler.sendEmptyMessage(0);
            } else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                        finish();
                    }
                });
            }
        }
    };

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(!checkrecordmax) {
                update();
            }

            drawAlarm();

            //모든 횟수 기록시, 자동으로 다른 창으로 이동
            if(svs.getRecordCount() >= DefConstant.RECORDCOUNT_MAX && !checkrecordmax) {
                checkrecordmax = true;
                if(button_trend_search.getText().toString().isEmpty())
                {
                    goIntent(DPEAK);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chartrecord_activity);

        Toolbar svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.record_screen);


        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnChart, makeUpdateIntentFilter());

        init();

        chartrecordActivity = ChartRecordActivity.this;
        checkrecordmax = false;

        alarm_chartrecord = findViewById(R.id.alarm_chartrecord);
        textview_temperature = findViewById(R.id.temperature_chartrecord);
        textview_temperature.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View view) {
                goIntent(TEMPERATURE);
                currentTrendValue = TEMPERATURE;
            }
        });

        button_trend_search = findViewById(R.id.button_control_ble);
        button_trend_search.setText("");
        button_trend_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                goIntent(currentTrendValue);
            }
        });

        update();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

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

        svs.setRecordcomment(null);
        svs.setRecorded(false);
        svs.clearRawMeasureDatas();
        svs.clearRecordMeasurDatas();
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");
        button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        dialog_Close();
    }


    private void goIntent(DefConstant.TrendValue trendValue){

        if(trendValue != UNKNOWN)
        {
            goIntent(TrendRecordActivity.class, false, EXTRA_STR_TREND_VALUE, trendValue);

            button_trend_search.setText(trendValue.toString());
        }

    }
    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }

    private void init(){
        combinedChart01 = findViewById(R.id.chart01_CombinedChart_chartrecord);
        combinedChart02 = findViewById(R.id.chart02_CombinedChart_chartrecord);
        combinedChart03 = findViewById(R.id.chart03_CombinedChart_chartrecord);

        combinedChart01.getAxisLeft().setAxisMinimum(0);
        combinedChart01.getAxisRight().setAxisMinimum(0);
        combinedChart01.setOnChartGestureListener(OCGL01);
        combinedChart01.setNoDataText(getResources().getString(R.string.recordingchartdata));

        combinedChart02.getAxisLeft().setAxisMinimum(0);
        combinedChart02.getAxisRight().setAxisMinimum(0);
        combinedChart02.setOnChartGestureListener(OCGL02);
        combinedChart02.setNoDataText(getResources().getString(R.string.recordingchartdata));

        combinedChart03.getAxisLeft().setAxisMinimum(0);
        combinedChart03.getAxisRight().setAxisMinimum(0);
        combinedChart03.setOnChartGestureListener(OCGL03);
        combinedChart03.setNoDataText(getResources().getString(R.string.recordingchartdata));

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
                xAxis.setAxisMaximum(chart01Xvalues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart01Xvalues[(int)value % chart01Xvalues.length];
                    }
                });
            }else if(chart == combinedChart02){
                xAxis.setAxisMaximum(chart02Xvalues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart02Xvalues[(int)value % chart03Xvalues.length];
                    }
                });
            }else if(chart == combinedChart03){
                xAxis.setAxisMaximum(chart03Xvalues.length-1);
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return chart03Xvalues[(int)value % chart03Xvalues.length];
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
            SVSCode svsCode =svs.getUploaddata().getSvsParam().getCode();
            float [] spaceend = new float[2];
            spaceend[0] = layerWidth + layerWidth/2;
            spaceend[1] = spaceend[0] + layerWidth;

            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.UNKNOWN;
            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getTimeEna().getdPeak() != 0){
                tappedTrendValue = DPEAK;
            }
            else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getTimeEna().getdRms() != 0){
                tappedTrendValue = DRMS;
            }
            else if(me.getX() > spaceend[1] && svsCode.getTimeEna().getdCrf() != 0){
                tappedTrendValue = DCRF;
            }
            goIntent(tappedTrendValue);
            currentTrendValue = tappedTrendValue;
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
            SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

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
            currentTrendValue = tappedTrendValue;

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
            SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

            float [] spaceend = new float[5];
            spaceend[0] = layerWidth + layerWidth/2;
            spaceend[1] = spaceend[0] + layerWidth;
            spaceend[2] = spaceend[1] + layerWidth;
            spaceend[3] = spaceend[2] + layerWidth;
            spaceend[4] = spaceend[3] + layerWidth;

            DefConstant.TrendValue tappedTrendValue = DefConstant.TrendValue.UNKNOWN;
            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getFreqEna()[0].getdBnd() != 0){
                tappedTrendValue = BAND1;
            }else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getFreqEna()[1].getdBnd() != 0){
                tappedTrendValue = BAND2;
            }else if(me.getX() > spaceend[1] && me.getX() < spaceend[2] && svsCode.getFreqEna()[2].getdBnd() != 0){
                tappedTrendValue = BAND3;
            }else if(me.getX() > spaceend[2] && me.getX() < spaceend[3] && svsCode.getFreqEna()[3].getdBnd() != 0){
                tappedTrendValue = BAND4;
            }else if(me.getX() > spaceend[3] && me.getX() < spaceend[4] && svsCode.getFreqEna()[4].getdBnd() != 0){
                tappedTrendValue = BAND5;
            }else if(me.getX() > spaceend[4] && svsCode.getFreqEna()[5].getdBnd() != 0){
                tappedTrendValue = BAND6;
            }
            goIntent(tappedTrendValue);
            currentTrendValue = tappedTrendValue;
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

    private void drawAlarm() {
        int count = svs.getRecordCount();
        if(count >= DefConstant.RECORDCOUNT_MAX)
            count = DefConstant.RECORDCOUNT_MAX;

        String str = String.valueOf(count) + "/" + String.valueOf(DefConstant.RECORDCOUNT_MAX);
        alarm_chartrecord.setText(str);
    }

    public void update() {

        try {
            if(svs.getRecordMeasureDatas().size() >= 1 ) {
                MeasureData data = svs.getRecordMeasureDatas().get(svs.getRecordMeasureDatas().size()-1);

                TextView temperature = findViewById(R.id.temperature_chartrecord);
                String temp = String.format("Temperature : %d \u00B0C",data.getlTempCurrent());
                temperature.setText(temp);

                combinedChart01.setData(generateChart01Data(data));
                combinedChart01.invalidate();

                combinedChart02.setData(generateChart02Date(data));
                combinedChart02.invalidate();

                combinedChart03.setData(generateChart03Date(data));
                combinedChart03.invalidate();

                checkbeep(data);
            }
        } catch (Exception ex) {

        }
    }

    private BarDataSet generateBarData(int xValue, float value, float valueMax, float valueWrn, float valueDan, boolean isLeftAxis){
        ArrayList<BarEntry> entriesBarTemperature = new ArrayList<>();

        entriesBarTemperature.add(new BarEntry(xValue, valueMax));
        entriesBarTemperature.add(new BarEntry(xValue, value));


        BarDataSet barDataSet = new BarDataSet(entriesBarTemperature, "");
        barDataSet.setAxisDependency(isLeftAxis == true? YAxis.AxisDependency.LEFT : YAxis.AxisDependency.RIGHT);
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
        MeasureData measuredata_max = svs.getMeasuredata_max();

        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

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

        MeasureData measuredata_max = svs.getMeasuredata_max();
        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

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

                    lineDataSet = generateLineData(xStartVal,  svsCode.getFreqWrn()[i].getdPeak(), true, true);
                    lineDataSet.setHighlightEnabled(false);
                    lineData.addDataSet(lineDataSet);

                    lineDataSet = generateLineData(xStartVal,  svsCode.getFreqDan()[i].getdPeak(), false, true);
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


    private CombinedData generateChart03Date(MeasureData data){
        BarData barData = new BarData();
        barData.setBarWidth(0.7f);

        LineData lineData = new LineData();

        MeasureData measuredata_max = svs.getMeasuredata_max();
        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

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

    private void playTone(int mediaFileRawId) {
        Log.d(TAG, "playTone");
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            }
            toneGenerator.startTone(mediaFileRawId, 200);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        Log.d(TAG, "ToneGenerator released");
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }

            }, 200);
        } catch (Exception e) {
            Log.d(TAG, "Exception while playing sound:" + e);
        }
    }

    private void checkbeep(MeasureData data) {

        MeasureData measuredata_max = svs.getMeasuredata_max();
        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

        if(svsCode.getTimeEna().getdPeak() != 0) {
            if(data.getSvsTime().getdPeak() > svsCode.getTimeWrn().getdPeak()) {
                try {
                    playTone(ToneGenerator.TONE_PROP_BEEP);
                }catch (Exception ex) {}

                return;
            }
        }

        if(svsCode.getTimeEna().getdRms() != 0) {
            if(data.getSvsTime().getdRms() > svsCode.getTimeWrn().getdRms()) {
                try {
                    playTone(ToneGenerator.TONE_PROP_BEEP);
                }catch (Exception ex) {}
                return;
            }
        }

        if(svsCode.getTimeEna().getdCrf() != 0) {
            if(data.getSvsTime().getdCrf() > svsCode.getTimeWrn().getdCrf()) {
                try {
                    playTone(ToneGenerator.TONE_PROP_BEEP);
                }catch (Exception ex) {}
                return;
            }
        }

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++){

            if(svsCode.getFreqEna()[i].getdPeak() != 0) {
                if(data.getSvsFreq()[i].getdPeak() > svsCode.getFreqWrn()[i].getdPeak()) {
                    try {
                        playTone(ToneGenerator.TONE_PROP_BEEP);
                    }catch (Exception ex) {}
                    return;
                }

            }

            if(svsCode.getFreqEna()[i].getdBnd() != 0) {
                if(data.getSvsFreq()[i].getdBnd() > svsCode.getFreqWrn()[i].getdBnd()) {
                    try {
                        playTone(ToneGenerator.TONE_PROP_BEEP);
                    }catch (Exception ex) {}
                    return;
                }
            }
        }
    }

    private void confirmCancel() {
        svs.setRecordcomment(null);
        svs.setRecorded(false);
        svs.clearRawMeasureDatas();
        svs.clearRecordMeasurDatas();

        goIntent(MonitoringTrendActivity.class, true);
    }

    private void dialog_Close(){

        DialogUtil.yesNo(this,
                "Record",
                "Do you want to cancel the record?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        confirmCancel();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }


}
