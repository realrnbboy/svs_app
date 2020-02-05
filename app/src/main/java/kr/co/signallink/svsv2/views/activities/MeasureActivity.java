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

// added by hslee 2020-01-29
// 측정 화면
public class MeasureActivity extends BaseActivity {

    private static final String TAG = "MeasureActivity";

    AnalysisData analysisData = null;
    MainData mainData = null;   // for test
    private SVS svs = SVS.getInstance();
    private OrderedRealmCollection<SVSEntity> svsEntities;

    MATRIX_2_Type matrix2;

    CombinedChart combinedChartRawData;

    boolean bMeasure = false;   // 측정했는지 여부

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

        drawChart();

    }

    void initView() {

        Intent intent = getIntent();

        // 이전 Activity 에서 전달받은 데이터 가져오기
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( analysisData == null ) {
            ToastUtil.showShort("analysis data is null");
            return;
        }

        Button buttonAnalysis = findViewById(R.id.buttonAnalysis);
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( !bMeasure ) {   // !!!!! 원래 !없음
                    //matrix2 계산
                    makeMatrix2();

                    // 다음 화면으로 이동
                    Intent intent = new Intent(getBaseContext(), ResultActivity.class);
                    intent.putExtra("matrix2", matrix2);
                    intent.putExtra("analysisData", analysisData);
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
        combinedChartRawData.setBackgroundColor(getResources().getColor(R.color.colorContent));
        combinedChartRawData.setOnChartGestureListener(OCGL);
        combinedChartRawData.setMaxVisibleValueCount(20);
        combinedChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));

        Legend l = combinedChartRawData.getLegend();
        l.setTextColor(Color.WHITE);
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChartRawData.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(10);
        rightAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = combinedChartRawData.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = combinedChartRawData.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;

                String str = "test";
                try {
                    //Date date = svs.getMeasureDatas().get(index).getCaptureTime();
                    //str = DateUtil.convertDefaultDetailDate(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return str;
            }
        });

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        xAxis.setAxisMaximum(210);
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



    private void drawChart() {

        LineData lineData = new LineData();

        ArrayList<Float> valueList = new ArrayList<>();
        ArrayList<MeasureData> measuredatas = svs.getRecordMeasureDatas();

        try {
//            for(int i=0; i<measuredatas.size(); i++){
//                valueList.add(measuredatas.get(i).getSvsTime().getdRms());
//            }

            valueList.add((float) 3.1);
            valueList.add((float) 14.1);
            valueList.add((float) 25.1);
            lineData.addDataSet(generateLineData(valueList, Color.GREEN));
            //lineData.addDataSet(generateLineData(true, svsCode.getTimeWrn().getdRms()));
            //lineData.addDataSet(generateLineData(false, svsCode.getTimeDan().getdRms()));
        } catch (Exception ex) {
            return;
        }

        valueList.clear();

        CombinedData combinedData = new CombinedData();
        lineData.setDrawValues(true);
        combinedData.setData(lineData);

        combinedChartRawData.setData(combinedData);
        combinedChartRawData.invalidate();
    }

    private LineDataSet generateLineData(ArrayList<Float> valueList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "what???");
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(lineColor);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueFormatter(new MyValueFormatter());
        lineDataSet.setFillDrawable(getResources().getDrawable(R.drawable.trend_gradient));

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
                bMeasure = true;
            }
        }
        else {
            if (requestCode == DefConstant.REQUEST_SENSING_RESULT) { // 센싱을 정상적으로 실행하지 않은 경우
                bMeasure = false;
            }
        }
    }

}
