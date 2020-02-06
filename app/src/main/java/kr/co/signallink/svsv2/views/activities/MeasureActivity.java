package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import io.realm.OrderedRealmCollection;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.ResultDiagnosisData;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-01-29
// 측정 화면
public class MeasureActivity extends BaseActivity {

    private static final String TAG = "MeasureActivity";

    AnalysisData analysisData = null;
    MainData mainData = null;

    MATRIX_2_Type matrix2;

    CombinedChart combinedChartRawData;

    boolean bMeasure = false;   // 측정했는지 여부

    MeasureData measureDataSensor1 = null;
    MeasureData measureDataSensor2 = null;
    MeasureData measureDataSensor3 = null;

    String equipmentUuid = null;

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

        mainData = new MainData(this);

        initView();
        initChart();
    }

    void initView() {

        Intent intent = getIntent();

        // 이전 Activity 에서 전달받은 데이터 가져오기
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( analysisData == null ) {
            ToastUtil.showShort("analysis data is null");
            return;
        }

        equipmentUuid = intent.getStringExtra("equipmentUuid");

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

    }

    private void initChart() {
        combinedChartRawData = findViewById(R.id.combinedChartRawData);
        combinedChartRawData.getDescription().setEnabled(false);
        combinedChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        combinedChartRawData.setOnChartGestureListener(OCGL);
        combinedChartRawData.setMaxVisibleValueCount(20);
        //combinedChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        combinedChartRawData.setNoDataText("no data. please measure");

        Legend l = combinedChartRawData.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChartRawData.getAxisRight();
        rightAxis.setEnabled(false);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMinimum(10);
//        rightAxis.setTextColor(Color.RED);

        YAxis leftAxis = combinedChartRawData.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = combinedChartRawData.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//
//                int index = (int)value;
//
//                String str = "test";
//                try {
//                    //Date date = svs.getMeasureDatas().get(index).getCaptureTime();
//                    //str = DateUtil.convertDefaultDetailDate(date);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return str;
//            }
//        });

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
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
            combinedChartRawData.fitScreen();
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


    private void drawChart(float[] data1, float[] data2, float[] data3) throws Exception{

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();

        try {

            if( data1 != null ) {
                for (float v : data1) {
                    valueList1.add(v);
                }
            }

            lineData.addDataSet(generateLineData("pt1", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));

            if( data2 != null ) {
                for (float v : data2) {
                    valueList2.add(v);
                }
            }

            lineData.addDataSet(generateLineData("pt2", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));

            if( data3 != null ) {
                for (float v : data3) {
                    valueList3.add(v);
                }
            }

            lineData.addDataSet(generateLineData("pt3", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myblue)));
        } catch (Exception ex) {
            return;
        }

       // valueList.clear();

        CombinedData combinedData = new CombinedData();

        lineData.setDrawValues(true);

        combinedData.setData(lineData);

        XAxis xAxis = combinedChartRawData.getXAxis();
        int xAxisMaximum = valueList1.size() <= 0 ? 0 : valueList1.size() - 1;
        xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅

        combinedChartRawData.setData(combinedData);
        combinedChartRawData.invalidate();
    }

    private LineDataSet generateLineData(String label, ArrayList<Float> valueList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);

        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setHighlightEnabled(false);

        lineDataSet.setColor(lineColor);

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
        DIAGNOSIS_DATA_Type[] rawData = mainData.fnGetRawDatas();   // 임시데이터

        matrix2 = diagnosis.fnMakeMatrix2(rawData[0], rawData[1], rawData[2]);

        double [][] tResultDiagnosis = diagnosis.resultDiagnosis;
        if( tResultDiagnosis != null ) {

            // 정렬된 데이터를 구하기 위해 클래스 구성
            ResultDiagnosisData[] resultDiagnosisData = new ResultDiagnosisData[tResultDiagnosis.length];

            for( int i = 0; i<tResultDiagnosis.length; i++ ) {
                resultDiagnosisData[i] = new ResultDiagnosisData();

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
    public void onBackPressed() {
        super.onBackPressed();
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

                analysisData.setMeasureData1(measureDataSensor1);
                analysisData.setMeasureData2(measureDataSensor2);
                analysisData.setMeasureData3(measureDataSensor3);

                bMeasure = true;

                float [] data1 = measureDataSensor1.getAxisBuf().getfTime();
                float [] data2 = measureDataSensor2.getAxisBuf().getfTime();
                float [] data3 = measureDataSensor3.getAxisBuf().getfTime();

                try {
                    drawChart(data1, data2, data3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            if (requestCode == DefConstant.REQUEST_SENSING_RESULT) { // 센싱을 정상적으로 실행하지 않은 경우
                bMeasure = false;
            }
        }
    }

}
