package kr.co.signallink.svsv2.views.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
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
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Date;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefComboBox;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSAxisBuf;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.StringUtil;

import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_RATIO;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_TIME_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.RAW_FREQ;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.RAW_TIME;

/**
 * Created by nspil on 2018-02-13.
 */

public class MonitoringDetailActivity extends BaseActivity implements OnChartValueSelectedListener {

    public static MonitoringDetailActivity thisActivity;

    public static final String TAG = "MonitoringDetailActivity";
    private SVS svs = SVS.getInstance();
    private DefConstant.TrendValue currentTrendValue = DefConstant.TrendValue.UNKNOWN;

    private TextView tvRawDataDomainTimeStamp;
    private CombinedChart combinedChart = null;
    private Button button_trend_stop;
    private Button button_record;
    private TextView tvTrendTitle_trendraw;
    private TextView tvTrendValue_trendraw;

    private MonitoringRawDataActivity chartrawActivity = (MonitoringRawDataActivity) MonitoringRawDataActivity.thisActivity;
    private UploadData uploadData;

    private final float XAXIS_LABEL_DEFAULT_ROATION = 70f;
    private float xAixsLabelRotation = XAXIS_LABEL_DEFAULT_ROATION;


    private final BroadcastReceiver StatusChangeReceiverOnTrend = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.MEASURE_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "MEASURE_ARRIVE");
                        update();
                    }
                });
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitoring_detail_activity);
        thisActivity = this;

        Toolbar svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        currentTrendValue = (DefConstant.TrendValue) getIntent().getSerializableExtra(EXTRA_STR_TREND_VALUE);
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(currentTrendValue.toString());


        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnTrend, makeUpdateIntentFilter());

        init();

        button_record = findViewById(R.id.button_record);
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            button_record.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newIntent = new Intent(MonitoringDetailActivity.this, ChartRecordActivity.class);
                    startActivity(newIntent);
                    svs.setRecorded(true);

                    chartrawActivity.finish();
                    finish();
                }
            });
        }

        button_trend_stop = (Button) findViewById(R.id.button_control_ble);
        if(svs.isBtrendstop())
            button_trend_stop.setText("resume");
        else
            button_trend_stop.setText("pause");

        button_trend_stop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(svs.isBtrendstop()) {
                    svs.setBtrendstop(false);
                    button_trend_stop.setText("pause");
                } else {
                    svs.setBtrendstop(true);
                    button_trend_stop.setText("resume");
                }
            }
        });

        //gone : RawTime과 RawFreq일때는 pause버튼과 녹화 버튼이 보이지 않게 변경함
        if(currentTrendValue == RAW_TIME || currentTrendValue == RAW_FREQ)
        {
            button_record.setVisibility(View.GONE);
            button_trend_stop.setVisibility(View.GONE);
        }

        tvTrendTitle_trendraw = findViewById(R.id.tvTrendTitle_trendraw);
        tvTrendValue_trendraw = findViewById(R.id.tvTrendValue_trendraw);

        drawChart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thisActivity = null;
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnTrend);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }

        combinedChart.setUnbindEnabled(true);
        combinedChart = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");

        if(currentTrendValue == RAW_TIME || currentTrendValue == RAW_FREQ)
        {
            button_record.setVisibility(View.GONE);
            button_trend_stop.setVisibility(View.GONE);
        }
        else
        {
            if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
            {
                if(((EquipmentEntity)svs.getLinkedEquipmentData()).getName() != null) {
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

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        Log.i("VAL SELECTED","Value: " + e.getY() + ", xIndex: " + e.getX() + ", DataSet index: " + h.getDataSetIndex());


        ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
        int measureDataSize = measureDatas.size();


        String strTitle = "";
        String strValue = "";

        if(currentTrendValue == DefConstant.TrendValue.RAW_TIME)
        {
            for(int i=measureDataSize-1; i>=0; i--)
            {
                MeasureData measureData = measureDatas.get(i);

                SVSAxisBuf axisBuf = measureData.getAxisBuf();

                //특정 시간의 MeasureData안에 Time이던 Freq이던 한가지라도 있으면 데이터를 출력
                if(axisBuf.getInputTimeLength() > 0 || axisBuf.getInputFreqLength() > 0)
                {
                    int index = (int)e.getX();

                    //Time Domain X Axis Value
                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
                    float xValue = index / samplingFreqHz;
                    strTitle = ""+StringUtil.decimalFormatDot3(xValue)+"s";

                    //Value
                    float[] fTimes = axisBuf.getfTime();
                    if(fTimes != null && index < fTimes.length)
                    {
                        strValue = ""+fTimes[index];
                    }

                    break;
                }
            }
        }
        else if(currentTrendValue == DefConstant.TrendValue.RAW_FREQ)
        {
            for(int i=measureDataSize-1; i>=0; i--)
            {
                MeasureData measureData = measureDatas.get(i);

                SVSAxisBuf axisBuf = measureData.getAxisBuf();

                //특정 시간의 MeasureData안에 Time이던 Freq이던 한가지라도 있으면 데이터를 출력
                if(axisBuf.getInputTimeLength() > 0 || axisBuf.getInputFreqLength() > 0)
                {
                    int index = (int)e.getX();

                    //Time Domain X Axis Value
                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
                    float xValue = samplingFreqHz * index / MEASURE_AXIS_FREQ_ELE_MAX;
                    strTitle = ""+StringUtil.decimalFormatDot1(xValue)+"Hz";

                    //Value
                    float[] fFreqs = axisBuf.getfFreq();
                    if(fFreqs != null && index < fFreqs.length)
                    {
                        strValue = ""+fFreqs[index];
                    }

                    break;
                }
            }
        }
        else
        {
            int index = (int)e.getX();

            if(index < measureDataSize && index > 0)
            {
                MeasureData measureData = measureDatas.get(index);

                //Date
                Date clickDate = measureData.getCaptureTime();
                strTitle = DateUtil.convertDefaultDetailDate(clickDate);

                //Value
                strValue = ""+e.getY();
            }
        }


        //출력
        tvTrendTitle_trendraw.setText(strTitle);
        tvTrendValue_trendraw.setText(strValue);
    }

    @Override
    public void onNothingSelected() {
        //..
    }

    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }

    private void init(){

        tvRawDataDomainTimeStamp = (TextView)findViewById(R.id.tvRawDataDomainTimeStamp);

        combinedChart = (CombinedChart)findViewById(R.id.CombnedChart_trendraw);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setBackgroundColor(getResources().getColor(R.color.colorContent));
        combinedChart.setOnChartGestureListener(OCGL);
        combinedChart.setOnChartValueSelectedListener(this);
        combinedChart.setMaxVisibleValueCount(20);
        combinedChart.setNoDataText(getResources().getString(R.string.collectingchartdata));

        Legend l = combinedChart.getLegend();
        l.setTextColor(Color.WHITE);
        l.setWordWrapEnabled(false);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)
        xAxis.setLabelCount(5); //X축에 있는 라벨의 갯수 (절대적은 아님)


        if(currentTrendValue == DefConstant.TrendValue.RAW_TIME)
        {
            xAixsLabelRotation = 0;
            applyXAxisOptions(xAxis, l, RAW_TIME);
        }
        else if(currentTrendValue == DefConstant.TrendValue.RAW_FREQ)
        {
            xAixsLabelRotation = 0;
            applyXAxisOptions(xAxis, l, RAW_FREQ);
        }
        else
        {
            rightAxis.setAxisMinimum(0);
            leftAxis.setAxisMinimum(0);
            xAixsLabelRotation = XAXIS_LABEL_DEFAULT_ROATION;
            applyXAxisDefault(xAxis, l);
        }

        xAxis.setLabelRotationAngle(xAixsLabelRotation); //X축에 있는 라벨의 각도
    }

    private void applyXAxisDefault(XAxis xAxis, Legend l){

        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        xAxis.setGranularity(1.0f);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(SVS.getInstance().getMeasureDatas().size()-1);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;

                String str = "";
                try {
                    Date date = svs.getMeasureDatas().get(index).getCaptureTime();
                    str = DateUtil.convertDate(date, "HH:mm:ss");
                } catch (Exception e) {
                    DefLog.d(TAG, e.toString());
                }

                return str;
            }
        });
    }

    private void applyXAxisOptions(XAxis xAxis, Legend l, final DefConstant.TrendValue trendValue)
    {
        //final int xAxisDataSize = trendValue == RAW_TIME ? MEASURE_AXIS_TIME_ELE_MAX : MEASURE_AXIS_FREQ_ELE_MAX;
        final int xAxisDataSize = trendValue == RAW_TIME ? 512 /*MEASURE_AXIS_TIME_ELE_MAX*/ : MEASURE_AXIS_FREQ_ELE_MAX;

        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        xAxis.setGranularity(xAxisDataSize / 4);
        xAxis.setAxisMaximum(xAxisDataSize * MEASURE_AXIS_RATIO);
        xAxis.setDrawGridLines(true);

        if(trendValue == RAW_TIME)
        {
            xAxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    String str = "";

                    int index = (int)value;

                    int measureIndex = index / xAxisDataSize;

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
                                    tvRawDataDomainTimeStamp.setText(strDate);

                                    //x축 표기
                                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
                                    float xValue = index / samplingFreqHz;
                                    str = ""+ StringUtil.decimalFormatDot3(xValue);

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
                                    tvRawDataDomainTimeStamp.setText(strDate);

                                    //x축 표기
                                    float samplingFreqHz = DefComboBox.samplingfreqToHz(uploadData.getSvsParam().getnSplFreq());
                                    float xValue = samplingFreqHz * index / xAxisDataSize;
                                    str = ""+ StringUtil.decimalFormatDot1(xValue);
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


    public void update() {

        runOnUiThread(new Runnable() {
            public void run() {
                if(!svs.isBtrendstop()) {
                    combinedChart.highlightValues(null);
                    drawChart();
                }
            }
        });
    }

    private void drawChart(){

        uploadData = svs.getUploaddata();

        ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
        int measureDataSize = measureDatas.size();

        //계산이 필요한 데이터는 최소 갯수가 필요함
        if(!DefConstant.TrendType.RAWS.hasTrendValue(currentTrendValue)) {
            if(measureDataSize < 2) {
                return;
            }
        }


        //X축의 아이템 갯수 사이즈
        XAxis xAxis = combinedChart.getXAxis();
        if(currentTrendValue == DefConstant.TrendValue.RAW_TIME) {
            xAxis.setAxisMaximum(512 /*MEASURE_AXIS_TIME_ELE_MAX*/);
        } else if(currentTrendValue == DefConstant.TrendValue.RAW_FREQ) {
            xAxis.setAxisMaximum(MEASURE_AXIS_FREQ_ELE_MAX);
        } else {
            xAxis.setAxisMaximum(measureDataSize-1);
        }

        //선 준비
        LineData lineData = new LineData();

        ArrayList<Float> valueList = new ArrayList<>();
        SVSParam svsParam = svs.getUploaddata().getSvsParam();
        SVSCode svsCode = svs.getUploaddata().getSvsParam().getCode();

        if(currentTrendValue.equals(DefConstant.TrendValue.TEMPERATURE)){
            try {
                for(int i=0; i<measureDataSize; i++){
                    valueList.add((float)measureDatas.get(i).getlTempCurrent());
                }

                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, (float)svsParam.getlTpWrn()));
                lineData.addDataSet(generateLineData(false, (float)svsParam.getlTpDan()));

            } catch (Exception ex) {
                return;
            }
        }else if(currentTrendValue.equals(DefConstant.TrendValue.DPEAK)){
            try {
                for(int i=0; i<measureDataSize; i++){
                    valueList.add(measureDatas.get(i).getSvsTime().getdPeak());
                }

                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdPeak()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdPeak()));
            } catch (Exception ex) {
                return;
            }
        }else if(currentTrendValue.equals(DefConstant.TrendValue.DRMS)){
            try {
                for(int i=0; i<measureDataSize; i++){
                    valueList.add(measureDatas.get(i).getSvsTime().getdRms());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdRms()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdRms()));
            } catch (Exception ex) {
                return;
            }
        }else if(currentTrendValue.equals(DefConstant.TrendValue.DCRF)){
            try {
                for(int i=0; i<measureDataSize; i++){
                    valueList.add(measureDatas.get(i).getSvsTime().getdCrf());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdCrf()));
                lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdCrf()));
            } catch (Exception ex) {
                return;
            }
        }else if(DefConstant.TrendType.PEAKS.hasTrendValue(currentTrendValue)){
            try {
                int index = currentTrendValue.getIndex();
                for(int i=0; i<measureDataSize; i++){
                    valueList.add(measureDatas.get(i).getSvsFreq()[index].getdPeak());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getFreqWrn()[index].getdPeak()));
                lineData.addDataSet(generateLineData(false, svsCode.getFreqDan()[index].getdPeak()));
            } catch (Exception ex) {
                return;
            }
        }else if(DefConstant.TrendType.BANDS.hasTrendValue(currentTrendValue)){
            try {
                int index = currentTrendValue.getIndex();
                for(int i=0; i<measureDataSize; i++){
                    valueList.add(measureDatas.get(i).getSvsFreq()[index].getdBnd());
                }
                lineData.addDataSet(generateLineData(valueList));
                lineData.addDataSet(generateLineData(true, svsCode.getFreqWrn()[index].getdBnd()));
                lineData.addDataSet(generateLineData(false, svsCode.getFreqDan()[index].getdBnd()));
            } catch (Exception ex) {
                return;
            }
        }else if(DefConstant.TrendValue.RAW_TIME.equals(currentTrendValue)){
            try {

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


                lineData.addDataSet(generateLineData(valueList));
            }
            catch (Exception e) {
                return;
            }
        }else if(DefConstant.TrendValue.RAW_FREQ.equals(currentTrendValue)){
            try {
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
                lineData.addDataSet(generateLineData(valueList));
            }
            catch (Exception e) {
                return;
            }
        }

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        combinedData.setData(lineData);

        combinedChart.setData(combinedData);
        //combinedChart.invalidate();

        adjustViewportForChart();
    }

    //이상한 뷰포트 바로 잡기
    private void adjustViewportForChart(){

        //X축 라벨 사이즈의 최대 높이
        Rect rectByText = SizeUtil.getTextRect(DateUtil.convertDate(new Date(), "HH:mm:ss"), combinedChart.getXAxis().getTypeface(), combinedChart.getXAxis().getTextSize());
        float textYSize = rectByText.height();

        if(xAixsLabelRotation != 0 && xAixsLabelRotation != 180) //수평이 아니라면
        {
            //회전 한 width, height <<회전변환 공식 참고>>
            double radian = Math.toRadians(xAixsLabelRotation);
            double rotWidth = (rectByText.width() * Math.cos(radian)) - (rectByText.height() * Math.sin(radian));
            double rotHeight = (rectByText.width() * Math.sin(radian)) + (rectByText.height() * Math.cos(radian));


            //빗변 계산
            double hypotenuse = Math.sqrt(Math.pow(rotWidth, 2) + Math.pow(rotHeight, 2)); //최대길

            //대입
            textYSize = (float)hypotenuse;
        }

        //레전드의 높이
        float legendSize = combinedChart.getLegend().mNeededHeight + combinedChart.getLegend().getYOffset();

        //뷰포트 offset
        float offsetLeft = combinedChart.getViewPortHandler().offsetLeft();
        float offsetRight = combinedChart.getViewPortHandler().offsetRight();
        float offsetSide = (offsetLeft > offsetRight) ? offsetLeft : offsetRight;
        float offsetBottom = textYSize + legendSize + SizeUtil.dpToPx(5); //5 is Padding

        combinedChart.setViewPortOffsets(offsetSide,SizeUtil.dpToPx(5), offsetSide, offsetBottom);
        combinedChart.invalidate();
    }


    private LineDataSet generateLineData(boolean isWrn, float value){
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, value));
        entries.add(new Entry(SVS.getInstance().getMeasureDatas().size()-1, value));

        LineDataSet lineDataSet = new LineDataSet(entries,isWrn == true? "Warning" : "Danger");
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(false);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setColor(isWrn == true ? Color.YELLOW : Color.RED);

        return lineDataSet;
    }

    private LineDataSet generateLineData(ArrayList<Float> valueList){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, currentTrendValue.toString());
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new MyValueFormatter());
        lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.trend_gradient));

        return lineDataSet;
    }

    private OnChartGestureListener OCGL = new OnChartGestureListener() {
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
            combinedChart.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

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
}
