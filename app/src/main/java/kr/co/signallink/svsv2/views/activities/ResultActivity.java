package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.RmsListAdapter;
import kr.co.signallink.svsv2.views.interfaces.RmsListClickListener;

// added by hslee 2020-01-29
// 진단분석 결과1 화면
public class ResultActivity extends BaseActivity {

    private static final String TAG = "ResultActivity";

    AnalysisData analysisData = null;
    MATRIX_2_Type matrix2;

    ListView listViewRms;
    RmsListAdapter rmsListAdapter;
    ArrayList<RmsModel> rmsList = new ArrayList<>();

    LineChart lineChartRawData;

    String equipmentUuid = null;

    boolean bRmsResultGood1 = false;    // rms 결과가 모두 good일 경우 next 버튼을 눌렀을때, DiagnosisActivity를 표시하지 않고, 바로 recordManager 화면을 표시한다.
    boolean bRmsResultGood2 = false;    // rms 결과가 모두 good일 경우 next 버튼을 눌렀을때, DiagnosisActivity를 표시하지 않고, 바로 recordManager 화면을 표시한다.
    boolean bRmsResultGood3 = false;    // rms 결과가 모두 good일 경우 next 버튼을 눌렀을때, DiagnosisActivity를 표시하지 않고, 바로 recordManager 화면을 표시한다.

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

        try {
            drawChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        equipmentUuid = intent.getStringExtra("equipmentUuid");

        TextView textViewCode = findViewById(R.id.textViewCode);
        String code;
        if( analysisData.diagVar1.nCode == 0 )
            code = "ANSI HI 9.6.4";
        else if( analysisData.diagVar1.nCode == 1 )
            code = "API 610";
        else if( analysisData.diagVar1.nCode == 2 )
            code = "ISO 10816 Cat.1";
        else if( analysisData.diagVar1.nCode == 3 )
            code = "ISO 10816 Cat.2";
        else if( analysisData.diagVar1.nCode == 4 )
            code = "Project VIB Spec";
        else
            code = "invalid value";

        textViewCode.setText(code);

        TextView textViewInputPower = findViewById(R.id.textViewInputPower);
        textViewInputPower.setText(String.valueOf(analysisData.diagVar1.nInputPower) + " kW");

        TextView textViewLineFrequency = findViewById(R.id.textViewLineFrequency);
        String lineFrequency;
        if( analysisData.diagVar1.nLineFreq == 0 )
            lineFrequency = "50 Hz";
        else if( analysisData.diagVar1.nLineFreq == 1 )
            lineFrequency = "60 Hz";
        else
            lineFrequency = "invalid value";
        textViewLineFrequency.setText(lineFrequency);

        TextView textViewEquipmentRpm = findViewById(R.id.textViewEquipmentRpm);
        textViewEquipmentRpm.setText(String.valueOf(analysisData.diagVar1.nRPM));

        // rms 값 추가
        addRmsItem();

        listViewRms = findViewById(R.id.listViewRms);

        rmsListAdapter = new RmsListAdapter(this, R.layout.list_item_rms, rmsList, getResources(), new RmsListClickListener() {
            @Override
            public void setRmsStatus(int position, boolean bGood) {
                switch(position) {
                    case 0 :
                        bRmsResultGood1 = bGood; break;
                    case 1 :
                        bRmsResultGood2 = bGood; break;
                    case 2 :
                        bRmsResultGood3 = bGood; break;

                }
            }
        });
        listViewRms.setAdapter(rmsListAdapter);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 다음 화면으로 이동
                Intent intent;
                if( bRmsResultGood1 && bRmsResultGood2 && bRmsResultGood3 ) {   // rms 결과가 모두 good일 경우 next 버튼을 눌렀을때, DiagnosisActivity를 표시하지 않고, 바로 recordManager 화면을 표시한다.
                    intent = new Intent(getBaseContext(), RecordManagerActivity.class);
                }
                else {
                    intent = new Intent(getBaseContext(), ResultDiagnosisActivity.class);
                }

                intent.putExtra("analysisData", analysisData);
                intent.putExtra("matrix2", matrix2);
                intent.putExtra("equipmentUuid", equipmentUuid);
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
        lineChartRawData = findViewById(R.id.lineChartRawData);
        lineChartRawData.getDescription().setEnabled(false);
        lineChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        lineChartRawData.setMaxVisibleValueCount(20);
        //lineChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRawData.setNoDataText("no data.");

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
    }


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

        lineData.setDrawValues(true);

        XAxis xAxis = lineChartRawData.getXAxis();
        int xAxisMaximum = valueList1.size() <= 0 ? 0 : valueList1.size() - 1;
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
