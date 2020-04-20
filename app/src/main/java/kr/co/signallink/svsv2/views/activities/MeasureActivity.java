package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.ResultDiagnosisData;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.utils.ToastUtil;

// added by hslee 2020-01-29
// 측정 화면
public class MeasureActivity extends BaseActivity {

    private static final String TAG = "MeasureActivity";

    AnalysisData analysisData = null;
    MainData mainData = null;

    MATRIX_2_Type matrix2;

    LineChart lineChartRawData;

    boolean bMeasure = false;   // 측정했는지 여부
    boolean bTestData = false;  // 테스트데이터 사용 여부

    MeasureData measureDataSensor1 = null;
    MeasureData measureDataSensor2 = null;
    MeasureData measureDataSensor3 = null;

    String equipmentUuid = null;

    float [] measuredFreq1 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq2 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq3 = null;  // measureActivity에서 측정된 데이터

    boolean bShowChartPt1 = true; // 차트의 pt1 표시 여부
    boolean bShowChartPt2 = true; // 차트의 pt2 표시 여부
    boolean bShowChartPt3 = true; // 차트의 pt3 표시 여부

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Measurement");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        Intent intent = getIntent();

        equipmentUuid = intent.getStringExtra("equipmentUuid");
        measuredFreq1 = (float[]) intent.getSerializableExtra("measuredFreq1");
        measuredFreq2 = (float[]) intent.getSerializableExtra("measuredFreq2");
        measuredFreq3 = (float[]) intent.getSerializableExtra("measuredFreq3");

        if( !(measuredFreq1 == null || measuredFreq2 == null || measuredFreq3 == null) ) { // 기존에 측정한 데이터가 있으면
            bMeasure = true;    // 측정된 상태로 표시
        }

        // 이전 Activity 에서 전달받은 데이터 가져오기
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( analysisData == null ) {
            ToastUtil.showShort("analysis data is null");
            return;
        }

        mainData = new MainData(this);
        mainData.init(analysisData.getDiagVar1());

        initView();
        initChart();
    }

    void initView() {

        Button buttonAnalysis = findViewById(R.id.buttonAnalysis);
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( bMeasure ) {
                    //matrix2 계산
                    makeMatrix2();

                    // 다음 화면으로 이동
                    Intent intent = new Intent(getBaseContext(), ResultActivity.class);
                    intent.putExtra("matrix2", matrix2);
                    intent.putExtra("analysisData", analysisData);
                    intent.putExtra("equipmentUuid", equipmentUuid);
                    startActivity(intent);
                }
                else {  // 측정을 하지 않은 경우
                    ToastUtil.showShort("Please measure first");
                }
            }
        });

        Button buttonMeasure = findViewById(R.id.buttonMeasure);
        buttonMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), MeasureExeActivity.class);

                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, DefConstant.REQUEST_SENSING_RESULT);
            }
        });

        Button buttonTestData = findViewById(R.id.buttonTestData);
        buttonTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bTestData = true;
                makeMatrix2();

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), ResultDiagnosisActivity.class);
                //Intent intent = new Intent(getBaseContext(), ResultActivity.class);

                intent.putExtra("matrix2", matrix2);
                intent.putExtra("analysisData", analysisData);
                intent.putExtra("equipmentUuid", equipmentUuid);
                startActivity(intent);
            }
        });

        final ImageView imageViewPt1 = findViewById(R.id.imageViewPt1);
        imageViewPt1.setSelected(true);// 초기값은 선택되있음.
        LinearLayout linearLayoutPt1 = findViewById(R.id.linearLayoutPt1);
        linearLayoutPt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bShowChartPt1 = !bShowChartPt1;

                imageViewPt1.setSelected(bShowChartPt1);

                drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
            }
        });

        final ImageView imageViewPt2 = findViewById(R.id.imageViewPt2);
        imageViewPt2.setSelected(true);// 초기값은 선택되있음.
        LinearLayout linearLayoutPt2 = findViewById(R.id.linearLayoutPt2);
        linearLayoutPt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bShowChartPt2 = !bShowChartPt2;

                imageViewPt2.setSelected(bShowChartPt2);

                drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
            }
        });

        final ImageView imageViewPt3 = findViewById(R.id.imageViewPt3);
        imageViewPt3.setSelected(true);// 초기값은 선택되있음.
        LinearLayout linearLayoutPt3 = findViewById(R.id.linearLayoutPt3);
        linearLayoutPt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bShowChartPt3 = !bShowChartPt3;

                imageViewPt3.setSelected(bShowChartPt3);

                drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
            }
        });

    }

    private void initChart() {
        lineChartRawData = findViewById(R.id.lineChartRawData);
        lineChartRawData.getDescription().setEnabled(false);
        lineChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        //lineChartRawData.setMaxVisibleValueCount(20);
        //lineChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRawData.setNoDataText("no data. please measure first");
        lineChartRawData.setOnChartValueSelectedListener(onChartValueSelectedListenerRawData);

        Legend l = lineChartRawData.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = lineChartRawData.getAxisRight();
        rightAxis.setEnabled(false);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMinimum(10);
//        rightAxis.setTextColor(Color.RED);

        YAxis leftAxis = lineChartRawData.getAxisLeft();
        //leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = lineChartRawData.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)

        //xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
        //applyXAxisDefault(xAxis, l);

        final ScrollView scrollView = findViewById(R.id.scrollView);
        lineChartRawData.setOnTouchListener(new View.OnTouchListener() {    // 차크 클릭 시, 스크롤뷰의 스크롤 기능을 off 하여 차트 스크롤 기능을 방해하지 않게 함.
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                    }
                }

                return false;
            }
        });

        if( !(measuredFreq1 == null || measuredFreq2 == null || measuredFreq3 == null) ) { // 기존에 측정한 데이터가 있으면 표시
            drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
        }
    }

    private OnChartValueSelectedListener onChartValueSelectedListenerRawData = new OnChartValueSelectedListener() {

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            TextView textViewSelectedItemValue = findViewById(R.id.textViewSelectedRawDataValue);
            textViewSelectedItemValue.setText(String.valueOf(e.getY()));
        }

        @Override
        public void onNothingSelected() {

        }
    };


    private void drawChart(float[] data1, float[] data2, float[] data3){

        try {
            //Thread.sleep(1000); // 차트 초기화 시간 - 추가 안하면 정상적으로 표시 안될 수 있음.
        }
        catch (Exception e) {
        }

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();

        try {
            if( bShowChartPt1 ) {

                if (data1 != null) {
                    for (float v : data1) {
                        valueList1.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt1", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));
            }

            if( bShowChartPt2 ) {
                if (data2 != null) {
                    for (float v : data2) {
                        valueList2.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt2", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));
            }

            if( bShowChartPt3 ) {
                if (data3 != null) {
                    for (float v : data3) {
                        valueList3.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt3", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myblue)));
            }
        } catch (Exception ex) {
            return;
        }

       // valueList.clear();
        lineData.setDrawValues(true);

        XAxis xAxis = lineChartRawData.getXAxis();
        int xAxisMaximum = valueList1.size() <= 0 ? 0 : valueList1.size() - 1;
        xAxisMaximum = xAxisMaximum <= 0 ? valueList2.size() - 1 : xAxisMaximum;
        xAxisMaximum = xAxisMaximum <= 0 ? valueList3.size() - 1 : xAxisMaximum;

        xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅

        lineChartRawData.setData(lineData);
        lineChartRawData.invalidate();
    }

    private LineDataSet generateLineData(String label, ArrayList<Float> valueList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);

//        lineDataSet.setDrawCircleHole(false);
//        lineDataSet.setDrawCircles(false);
//        lineDataSet.setValueTextColor(Color.WHITE);
//        lineDataSet.setHighlightEnabled(false);
//
//        lineDataSet.setColor(lineColor);

        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(lineColor);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);

        return lineDataSet;
    }


    public void makeMatrix2() {

        if( analysisData.getValueVar2() == null ) { // 초기 세팅이 안되어 있을 경우 세팅.
            analysisData.setValueVar2(mainData.valueVar2);
            analysisData.setRangeVar2(mainData.rangeVar2);
            analysisData.setLowerVar2(mainData.lowerVar2);
            analysisData.setUpperVar2(mainData.upperVar2);
            analysisData.setFeatureInfos(mainData.featureInfos);
        }

        DiagnosisInfo diagnosis = new DiagnosisInfo(analysisData);

        //DIAGNOSIS_DATA_Type[] rawData = analysisData.fnGetRawDatas();   // 센서의 rawdata로 변경 해야함.

        if( bTestData ) {
            DIAGNOSIS_DATA_Type[] rawData = mainData.fnGetRawDatas();   // 임시데이터
            matrix2 = diagnosis.fnMakeMatrix2(rawData[0], rawData[1], rawData[2]);

//            analysisData.getMeasureData1().getAxisBuf().setfFreq(rawData[0].dFreq);
//            analysisData.getMeasureData2().getAxisBuf().setfFreq(rawData[1].dFreq);
//            analysisData.getMeasureData3().getAxisBuf().setfFreq(rawData[2].dFreq);
//
//            analysisData.getMeasureData1().getAxisBuf().setfTime(rawData[0].dPwrSpectrum);
//            analysisData.getMeasureData2().getAxisBuf().setfTime(rawData[1].dPwrSpectrum);
//            analysisData.getMeasureData3().getAxisBuf().setfTime(rawData[2].dPwrSpectrum);
        }
        else {
            DIAGNOSIS_DATA_Type rawData1 = new DIAGNOSIS_DATA_Type();
            rawData1.fSamplingRate = (float) 1.338975e+03;
            rawData1.dFreq = analysisData.getMeasureData1().getAxisBuf().getfFreq();
            rawData1.dPwrSpectrum = analysisData.getMeasureData1().getAxisBuf().getfFreq();

            DIAGNOSIS_DATA_Type rawData2 = new DIAGNOSIS_DATA_Type();
            rawData2.fSamplingRate = (float) 1.386214e+03;
            rawData2.dFreq = analysisData.getMeasureData2().getAxisBuf().getfFreq();
            rawData2.dPwrSpectrum = analysisData.getMeasureData2().getAxisBuf().getfFreq();

            DIAGNOSIS_DATA_Type rawData3 = new DIAGNOSIS_DATA_Type();
            rawData3.fSamplingRate = (float) 1.384258e+03;
            rawData3.dFreq = analysisData.getMeasureData3().getAxisBuf().getfFreq();
            rawData3.dPwrSpectrum = analysisData.getMeasureData3().getAxisBuf().getfFreq();

            matrix2 = diagnosis.fnMakeMatrix2(rawData1, rawData2, rawData3);
        }

        double [][] tResultDiagnosis = diagnosis.resultDiagnosis;
        if( tResultDiagnosis != null ) {

            // 정렬된 데이터를 구하기 위해 클래스 구성
            ResultDiagnosisData[] resultDiagnosisData = new ResultDiagnosisData[tResultDiagnosis.length];

            for( int i = 0; i<tResultDiagnosis.length; i++ ) {
                resultDiagnosisData[i] = new ResultDiagnosisData();

                resultDiagnosisData[i].no = mainData.causeInfos.infos[i].nNo;

                resultDiagnosisData[i].cause = mainData.causeInfos.infos[i].strCause;
                resultDiagnosisData[i].desc = mainData.causeInfos.infos[i].strDesc;

                resultDiagnosisData[i].rank = tResultDiagnosis[i][0];
                resultDiagnosisData[i].sum = tResultDiagnosis[i][1];
                resultDiagnosisData[i].ratio = tResultDiagnosis[i][2];
            }

            // 1번째 행렬로 정렬하기 위해 Comparator를 이용합니다
            Arrays.sort(resultDiagnosisData, new Comparator<ResultDiagnosisData>() {
                // Override된 compare 함수를 어떻게 정의하냐에 따라서 다양한 정렬이 가능해집니다
                @Override
                public int compare(ResultDiagnosisData o1, ResultDiagnosisData o2) {
                    return o1.rank < o2.rank ? -1 : 1; // 내림자순 정렬을 하고 싶다면 o2와 o1의 위치를 바꿔줍니다
                }
            });

            analysisData.setResultDiagnosis(resultDiagnosisData);
        }
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

    @Override
    public void finish() {
        setReturnIntent();

        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setReturnIntent() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("measuredFreq1", measuredFreq1);
        returnIntent.putExtra("measuredFreq2", measuredFreq2);
        returnIntent.putExtra("measuredFreq3", measuredFreq3);

        returnIntent.putExtra("analysisData", analysisData);

        setResult(Activity.RESULT_OK, returnIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == DefConstant.REQUEST_SENSING_RESULT) { // 센싱을 정상적으로 실행한 경우

                measureDataSensor1 = (MeasureData) data.getSerializableExtra("measureDataSensor1");
                measureDataSensor2 = (MeasureData) data.getSerializableExtra("measureDataSensor2");
                measureDataSensor3 = (MeasureData) data.getSerializableExtra("measureDataSensor3");

                if( measureDataSensor1 == null || measureDataSensor2 == null || measureDataSensor3 == null ) {
                    ToastUtil.showShort("failed to measure data trans");
                    return;
                }

                measuredFreq1 = measureDataSensor1.getAxisBuf().getfFreq();
                measuredFreq2 = measureDataSensor2.getAxisBuf().getfFreq();
                measuredFreq3 = measureDataSensor3.getAxisBuf().getfFreq();

                analysisData.setMeasureData1(measureDataSensor1);
                analysisData.setMeasureData2(measureDataSensor2);
                analysisData.setMeasureData3(measureDataSensor3);

                System.arraycopy(measuredFreq1, 0, analysisData.csvMeasureData1, 0, DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX);
                System.arraycopy(measuredFreq2, 0, analysisData.csvMeasureData2, 0, DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX);
                System.arraycopy(measuredFreq3, 0, analysisData.csvMeasureData3, 0, DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX);

                bMeasure = true;

                try {
                    drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            if (requestCode == DefConstant.REQUEST_SENSING_RESULT) { // 센싱을 정상적으로 실행하지 않은 경우
                //bMeasure = false; // 추가하면 센싱 중 취소 시, 처음부터 다시 해야됨.
            }
        }
    }

}
