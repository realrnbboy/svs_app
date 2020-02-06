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
import androidx.core.content.ContextCompat;

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
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.RmsListAdapter;

// added by hslee 2020-01-29
// 진단분석 결과1 화면
public class RecordManagerActivity extends BaseActivity {

    private static final String TAG = "RecordManagerActivity";

    String dateFormat = "dd/MM/yyyy";
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
        setContentView(R.layout.activity_record_manager);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Record manager");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

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

        // 이전 Activity 에서 전달받은 데이터 가져오기
        matrix2 = (MATRIX_2_Type)intent.getSerializableExtra("matrix2");
        if( matrix2 == null ) {
            ToastUtil.showShort("matrix2 data is null");
            return;
        }

        String endd = Utils.getCurrentTime(dateFormat);
        String startd = Utils.addDateDay(endd, -7, dateFormat);

        final TextView textViewStartd = findViewById(R.id.textViewStartd);
        textViewStartd.setText(startd);
        final TextView textViewEndd = findViewById(R.id.textViewEndd);
        textViewEndd.setText(endd);

        // rms 값 추가
        addRmsItem();

        listViewRms = findViewById(R.id.listViewRms);

        rmsListAdapter = new RmsListAdapter(this, R.layout.list_item_rms, rmsList, getResources());
        listViewRms.setAdapter(rmsListAdapter);

        Button buttonToday = findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String today = Utils.getCurrentTime(dateFormat);
                textViewStartd.setText(today);
                textViewEndd.setText(today);
            }
        });

        Button buttonWeek = findViewById(R.id.buttonWeek);
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String endd = Utils.getCurrentTime(dateFormat);
                String startd = Utils.addDateDay(endd, -7, dateFormat);
                textViewStartd.setText(startd);
                textViewEndd.setText(endd);
            }
        });

        Button buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 홈 화면으로 이동
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    // rms 값 추가
    private void addRmsItem() {
        RmsModel rmsModel1 = new RmsModel();
        RmsModel rmsModel2 = new RmsModel();
        RmsModel rmsModel3 = new RmsModel();

        rmsModel1.setName("PT1");
        rmsModel2.setName("PT2");
        rmsModel3.setName("PT3");

        float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
        float rms2 = analysisData.getMeasureData2().getSvsTime().getdRms();
        float rms3 = analysisData.getMeasureData3().getSvsTime().getdRms();
        rmsModel1.setRms(rms1);
        rmsModel2.setRms(rms2);
        rmsModel3.setRms(rms3);

        float danger1 = analysisData.getMeasureData1().getRmsDanger();
        float danger2 = analysisData.getMeasureData2().getRmsDanger();
        float danger3 = analysisData.getMeasureData3().getRmsDanger();
        rmsModel1.setDanger(danger1);
        rmsModel2.setDanger(danger2);
        rmsModel3.setDanger(danger3);

        float warning1 = analysisData.getMeasureData1().getRmsWarning();
        float warning2 = analysisData.getMeasureData2().getRmsWarning();
        float warning3 = analysisData.getMeasureData3().getRmsWarning();
        rmsModel1.setWarning(warning1);
        rmsModel2.setWarning(warning2);
        rmsModel3.setWarning(warning3);

        rmsList.add(rmsModel1);
        rmsList.add(rmsModel2);
        rmsList.add(rmsModel3);
    }

    private void initChart() {
        combinedChartRawData = findViewById(R.id.combinedChartRawData);
        combinedChartRawData.getDescription().setEnabled(false);
        combinedChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        combinedChartRawData.setOnChartGestureListener(OCGL);
        combinedChartRawData.setMaxVisibleValueCount(20);
        //combinedChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        combinedChartRawData.setNoDataText("no data. please select today or week");

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


    private void drawChart() throws Exception {

        if( analysisData == null ) {
            return;
        }

        float [] data1 = analysisData.getMeasureData1().getAxisBuf().getfFreq();
        float [] data2 = analysisData.getMeasureData2().getAxisBuf().getfFreq();
        float [] data3 = analysisData.getMeasureData3().getAxisBuf().getfFreq();

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
        xAxis.setAxisMaximum(valueList1.size() - 1);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅

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
