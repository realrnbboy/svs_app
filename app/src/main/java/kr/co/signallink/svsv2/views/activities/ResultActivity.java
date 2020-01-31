package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.RmsListAdapter;

// added by hslee 2020-01-29
// 진단분석 결과1 화면
public class ResultActivity extends BaseActivity {

    private static final String TAG = "ResultActivity";

    AnalysisData analysisData = null;
    MATRIX_2_Type matrix2;

    ListView listViewRms;
    RmsListAdapter rmsListAdapter;
    ArrayList<RmsModel> rmsList = new ArrayList<>();

    CombinedChart combinedChartRawData;
    private SVS svs = SVS.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Result");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

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

        // 이전 Activity 에서 전달받은 데이터 가져오기
        matrix2 = (MATRIX_2_Type)intent.getSerializableExtra("matrix2");
        if( matrix2 == null ) {
            ToastUtil.showShort("matrix2 data is null");
            return;
        }

        TextView textViewCode = findViewById(R.id.textViewCode);
        textViewCode.setText(String.valueOf(analysisData.diagVar1.nCode));

        // rms 값 추가
        addRmsItem();

        listViewRms = findViewById(R.id.listViewRms);

        rmsListAdapter = new RmsListAdapter(this, R.layout.list_item_rms, rmsList, getResources());
        listViewRms.setAdapter(rmsListAdapter);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), ResultDiagnosisActivity.class);
                intent.putExtra("analysisData", analysisData);
                intent.putExtra("matrix2", matrix2);
                startActivity(intent);
            }
        });
    }

    // rms 값 추가
    private void addRmsItem() {
        RmsModel rmsModel1 = new RmsModel();
        RmsModel rmsModel2 = new RmsModel();
        RmsModel rmsModel3 = new RmsModel();
        RmsModel rmsModel4 = new RmsModel();

        rmsModel1.setName("PT1");
        rmsModel2.setName("PT2");
        rmsModel3.setName("PT3");
        rmsModel4.setName("PT4");

        rmsModel1.setRms(7.2);
        rmsModel2.setRms(4.1);
        rmsModel3.setRms(3.5);
        rmsModel4.setRms(1.1);

        rmsModel1.setDanger(5.3);
        rmsModel2.setDanger(2.3);
        rmsModel3.setDanger(5.3);
        rmsModel4.setDanger(3.3);

        rmsModel1.setWarning(2.2);
        rmsModel2.setWarning(2.2);
        rmsModel3.setWarning(2.2);
        rmsModel4.setWarning(2.2);

        rmsList.add(rmsModel1);
        rmsList.add(rmsModel2);
        rmsList.add(rmsModel3);
        rmsList.add(rmsModel4);
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

}
