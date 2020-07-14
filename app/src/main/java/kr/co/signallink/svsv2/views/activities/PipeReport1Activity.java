package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.model.Constants;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020.06.19
// report 결과1 화면
public class PipeReport1Activity extends BaseActivity {

    private static final String TAG = "PipeReport1Activity";

    String equipmentUuid = null;

    LineChart lineChartRawData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe_report1);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("report1");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        initChartRawData();
        drawChartRawData();

        Intent intent = getIntent();

        equipmentUuid = intent.getStringExtra("equipmentUuid");

        TextView textViewDate = findViewById(R.id.textViewDate);
        String date = intent.getStringExtra("date");
        textViewDate.setText(date);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // report2 화면으로 이동
                Intent intent = new Intent(getBaseContext(), PipeReport2Activity.class);
                String pipeName = getIntent().getStringExtra("pipeName");
                String pipeImage = getIntent().getStringExtra("pipeImage");
                String pipeLocation = getIntent().getStringExtra("pipeLocation");
                String pipeOperationScenario = getIntent().getStringExtra("pipeOperationScenario");

                intent.putExtra("equipmentUuid", equipmentUuid);
                intent.putExtra("pipeName", pipeName);
                intent.putExtra("pipeImage", pipeImage);
                intent.putExtra("pipeLocation", pipeLocation);
                intent.putExtra("pipeOperationScenario", pipeOperationScenario);
                startActivity(intent);
            }
        });
    }

    // 차트 그리기
    private void drawChartRawData() {

        Intent intent = getIntent();
        float[] data1 = intent.getFloatArrayExtra("data1");
        float[] data2 = intent.getFloatArrayExtra("data2");
        float[] data3 = intent.getFloatArrayExtra("data3");
        float[] data4 = Utils.getConcernDataList();
        float[] data5 = Utils.getProblemDataList();

        if( data1 == null || data2 == null || data3 == null ) {
            DefLog.d(TAG, "data is null");
            return;
        }

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();
        ArrayList<Float> valueList4 = new ArrayList<>();
        ArrayList<Float> valueList5 = new ArrayList<>();

        try {
            if (data1 != null) {
                for (float v : data1) {
                    valueList1.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Vertical", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), false));

            if (data2 != null) {
                for (float v : data2) {
                    valueList2.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Horizontal", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myblue), false));

            if (data3 != null) {
                for (float v : data3) {
                    valueList3.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Axial", valueList3, ContextCompat.getColor(getBaseContext(), R.color.hotpink), false));

            if (data4 != null) {
                for (float v : data4) {
                    valueList4.add(v);
                }
            }

            lineData.addDataSet(generateLineData("concern", valueList4, ContextCompat.getColor(getBaseContext(), R.color.myorange), false));

            if (data5 != null) {
                for (float v : data5) {
                    valueList5.add(v);
                }
            }

            lineData.addDataSet(generateLineData("problem", valueList5, ContextCompat.getColor(getBaseContext(), R.color.myred), false));
        } catch (Exception ex) {
            return;
        }

        // valueList.clear();

        lineData.setDrawValues(true);

        XAxis xAxis = lineChartRawData.getXAxis();
        xAxis.setAxisMaximum(Constants.MAX_PIPE_X_VALUE);

        lineChartRawData.setData(lineData);
        lineChartRawData.invalidate();
    }

    private void initChartRawData() {
        lineChartRawData = findViewById(R.id.lineChartRawData);
        lineChartRawData.getDescription().setEnabled(false);
        lineChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        lineChartRawData.setMaxVisibleValueCount(20);
        //lineChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRawData.setNoDataText("no data.");
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
        leftAxis.setDrawGridLines(false);
        //leftAxis.setAxisMinimum(1);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = lineChartRawData.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {   // added by hslee 2020.06.19

                int index = (int)value;
                if( index == 0 )
                    return "1";
                else
                    return String.valueOf(index);
            }
        });

//        final ScrollView scrollView = findViewById(R.id.scrollView);
//        lineChartRawData.setOnTouchListener(new View.OnTouchListener() {    // 차크 클릭 시, 스크롤뷰의 스크롤 기능을 off 하여 차트 스크롤 기능을 방해하지 않게 함.
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        scrollView.requestDisallowInterceptTouchEvent(true);
//                        break;
//                    }
//                    case MotionEvent.ACTION_CANCEL:
//                    case MotionEvent.ACTION_UP: {
//                        scrollView.requestDisallowInterceptTouchEvent(false);
//                        break;
//                    }
//                }
//
//                return false;
//            }
//        });
    }

    private LineDataSet generateLineData(String label, ArrayList<Float> yDataList, int lineColor, boolean bDrawCircle){
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet lineDataSet = null;
        try {

            for(int i=0; i<yDataList.size(); i++) {
                entries.add(new Entry(i, yDataList.get(i)));
            }

            lineDataSet = new LineDataSet(entries, label);

            lineDataSet.setDrawCircleHole(bDrawCircle);
            lineDataSet.setDrawCircles(bDrawCircle);
            lineDataSet.setColor(lineColor);
            lineDataSet.setDrawFilled(false);    // 값 하단 색  채움
            lineDataSet.setValueTextColor(Color.WHITE);
            lineDataSet.setDrawValues(true);
            lineDataSet.setHighlightEnabled(true);  // 클릭했을 때, 십자기로 줄표시해줌

            lineDataSet.setCircleColor(lineColor); // LineChart에서 Line Circle Color 설정
            lineDataSet.setCircleColorHole(lineColor); // LineChart에서 Line Hole Circle Color 설정

        }
        catch (Exception e) {
            e.printStackTrace();
        }

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

    private OnChartValueSelectedListener onChartValueSelectedListenerRawData = new OnChartValueSelectedListener() {

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            TextView textViewSelectedItemValue = findViewById(R.id.textViewSelectedRawDataValue);
            textViewSelectedItemValue.setText(String.format("%.3f", e.getY()));
        }

        @Override
        public void onNothingSelected() {

        }
    };
}
