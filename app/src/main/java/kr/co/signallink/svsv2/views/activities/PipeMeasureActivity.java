package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.ResultDiagnosisData;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.server.OnTCPSendCallback;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ProgressDialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

import static java.lang.Math.log10;

// added by hslee 2020-03-19
// 배관 측정 화면
public class PipeMeasureActivity extends BaseActivity {

    private static final String TAG = "PipeMeasureActivity";

    AnalysisData analysisData = null;

    LineChart lineChartRawData;

    boolean bMeasure = false;   // 측정했는지 여부

    MeasureData measureDataSensor1 = null;

    String equipmentUuid = null;

    float [] measuredFreq1 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq2 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq3 = null;  // measureActivity에서 측정된 데이터

    private static boolean isUpload = false;
    Context m_context;

    int nowMeasurePosition = 1; // 측정하려는 센서 위치

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context = this;

        setContentView(R.layout.activity_pipe_measure);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Pipe Measurement");

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

        if( measuredFreq1 != null && measuredFreq2 != null && measuredFreq3 != null ) { // 기존에 측정한 데이터가 있으면
            bMeasure = true;    // 측정된 상태로 표시
        }

        // 이전 Activity 에서 전달받은 데이터 가져오기
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( analysisData == null ) {
            ToastUtil.showShort("analysis data is null");
            return;
        }

        initView();
        initChart();
    }

    void initView() {

        Button buttonAnalysis = findViewById(R.id.buttonAnalysis);
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( bMeasure ) {
                    // 다음 화면으로 이동
                    Intent intent = new Intent(getBaseContext(), PipeResultActivity.class);
                    intent.putExtra("analysisData", analysisData);
                    intent.putExtra("equipmentUuid", equipmentUuid);
                    startActivity(intent);
                }
                else {  // 측정을 하지 않은 경우
                    if( measuredFreq1 == null ) {
                        ToastUtil.showShort("Please measure vertical");
                    }
                    else if( measuredFreq2 == null ) {
                        ToastUtil.showShort("Please measure horizontal");
                    }
                    else {
                        ToastUtil.showShort("Please measure axial");
                    }
                }
            }
        });

        Button buttonMeasure = findViewById(R.id.buttonMeasure);
        buttonMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup radioGroup = findViewById(R.id.radioGroupSensorPosition);
                int radioButtonId = radioGroup.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = radioGroup.findViewById(radioButtonId);
                nowMeasurePosition = radioGroup.indexOfChild(selectedRadioButton) + 1;

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), MeasureExeActivity.class);
                intent.putExtra("modePump", false);   // mode pump or pipe
                intent.putExtra("measurePosition", nowMeasurePosition);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, DefConstant.REQUEST_SENSING_RESULT);
            }
        });

        Button buttonUpload = findViewById(R.id.buttonUpload);  // 웹서버로 업로드 버튼 클릭
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( bMeasure ) {
                    if( isUpload ) {
                        ToastUtil.showShort("Already uploaded.");
                        //return;
                    }
                    guideUploadToWebManager();
                }
                else {  // 측정을 하지 않은 경우
                    ToastUtil.showShort("Please measure first.");
                }
            }
        });
    }

    private void initChart() {
        lineChartRawData = findViewById(R.id.lineChartRawData);
        lineChartRawData.getDescription().setEnabled(false);
        lineChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        lineChartRawData.setNoDataText("no data. please measure first");
        lineChartRawData.setOnChartValueSelectedListener(onChartValueSelectedListenerRawData);
        lineChartRawData.setScaleXEnabled(false);   // added by hslee 2020.07.15 x측 zoom하면 임시로 넣은 label값이 맞지 않게 됨

        Legend l = lineChartRawData.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = lineChartRawData.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = lineChartRawData.getAxisLeft();
        //leftAxis.setDrawGridLines(false);
        //leftAxis.setAxisMinimum(0);   // 다른 pipe 차트는 log계산 후 표시하지만, measure는 그냥 표시함.
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = lineChartRawData.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
        //applyXAxisDefault(xAxis, l);
        xAxis.setLabelCount(4);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if( value == 300 )// added by hslee 2020.07.15
                    return "100";
                else if( value == 600 )
                    return "200";
                else if( value == 900 )
                    return "300";
                else
                    return String.valueOf(index);
            }
        });

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

        if( measuredFreq1 != null || measuredFreq2 != null || measuredFreq3 != null) { // 기존에 측정한 데이터가 있으면 표시
            drawChart();
        }
    }


    private void drawChart(){

        try {
            //Thread.sleep(1000); // 차트 초기화 시간 - 추가 안하면 정상적으로 표시 안될 수 있음.
        }
        catch (Exception e) {
        }

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();
//
//        float[] data2 = getConcernDataList();
//        float[] data3 = getProblemDataList();

        try {
            float[] data1 = measuredFreq1;
            float[] data2 = measuredFreq2;
            float[] data3 = measuredFreq3;
            if (data1 != null) {
                for (float v : data1) {
                    valueList1.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Vertical", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));

            if (data2 != null) {
                for (float v : data2) {
                    valueList2.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Horizontal", valueList2, ContextCompat.getColor(getBaseContext(), android.R.color.white)));
//
            if (data3 != null) {
                for (float v : data3) {
                    valueList3.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Axial", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myBlueLight)));
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
        //xAxis.setAxisMaximum((float) 0.01);

        lineChartRawData.setData(lineData);
        lineChartRawData.invalidate();
    }

    private LineDataSet generateLineData(String label, ArrayList<Float> valueList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(i, valueList.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);

        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(lineColor);
        lineDataSet.setDrawFilled(false);    // 값 하단 색  채움
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(10f);

        return lineDataSet;
    }

    private LineDataSet generateLineData2(String label, ArrayList<Float> valueList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();

        for(int i=0; i<valueList.size(); i++){
            entries.add(new Entry(valueList.get(i), i));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, label);

        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(lineColor);
        lineDataSet.setDrawFilled(false);    // 값 하단 색  채움
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

                if( measureDataSensor1 == null ) {
                    ToastUtil.showShort("failed to measure data trans");
                    return;
                }

                isUpload = false;

                switch( nowMeasurePosition ) {
                    case 1:
                        analysisData.setMeasureData1(measureDataSensor1);
                        measuredFreq1 = measureDataSensor1.getAxisBuf().getfFreq();
                        break;
                    case 2:
                        analysisData.setMeasureData2(measureDataSensor1);
                        measuredFreq2 = measureDataSensor1.getAxisBuf().getfFreq();
                        break;
                    case 3:
                        analysisData.setMeasureData3(measureDataSensor1);
                        measuredFreq3 = measureDataSensor1.getAxisBuf().getfFreq();
                        break;
                }


                if( measuredFreq1 != null && measuredFreq2 != null && measuredFreq3 != null ) {
                    bMeasure = true;
                }

                try {
                    drawChart();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //guideConfigUploadToWebManager();  // deleted by hslee 2020.05.25 시그널링크 요청으로 임시 비활성화
            }
        }
        else {
            if (requestCode == DefConstant.REQUEST_SENSING_RESULT) { // 센싱을 정상적으로 실행하지 않은 경우
                //bMeasure = false; // 추가하면 센싱 중 취소 시, 처음부터 다시 해야됨.
            }
        }
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

    //RawData 업로드 할지 물어보기
    private void guideUploadToWebManager(){
        //SVS.getInstance().measuredFreq = measuredFreq1;
        SVS.getInstance().sensorType60 = true;

        //업로드 체크
        if(!isUpload)
        {
            //업로드 팝업
            DialogUtil.yesNo(this, "Upload Raw Data", "Do you want to upload the data you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doUploadToWebManager();
                }
            }, null);
        }
        else
        {
            //재업로드 팝업
            DialogUtil.yesNo(this, "Re Upload Raw Data", "Do you want to upload the data you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doUploadToWebManager();
                }
            }, null);
        }
    }

    //RawData 업로드
    private void doUploadToWebManager(){

        final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(this, "Upload Raw Data", "Data transfer is in progress. Please wait.");
        progressDialogUtil.show();

        TCPSendUtil.sendRawMeasure(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {

                progressDialogUtil.hide();

                DialogUtil.confirm(m_context, "Success", "Successfully uploaded raw data to the server", null);

                isUpload = true;
                //enableUploadToWebManager(true);
            }

            @Override
            public void onFailed(String tag, String msg) {
                progressDialogUtil.hide();

                DialogUtil.confirm(m_context, "Failed to upload raw data to the server.", msg, null);

            }
        });
    }



    //센서 정보 업로드 할지 물어보기
    private void guideConfigUploadToWebManager(){
        DialogUtil.yesNo(m_context, "Upload Sensor Infomation", "Do you want to upload the sensor information you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doUploadConfigToWebManager();
            }
        }, null);
    }

    //센서 정보 업로드
    private void doUploadConfigToWebManager(){
        SVS.getInstance().sensorType60 = true;

        final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(m_context, "Upload Sensor information", "Data transfer is in progress. Please wait.");
        progressDialogUtil.show();

        TCPSendUtil.sendConfig(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {


                progressDialogUtil.hide();

                DialogUtil.confirm(m_context, "Success", "Successfully uploaded sensor information to the server", null);

            }

            @Override
            public void onFailed(String tag, String msg) {
                progressDialogUtil.hide();

                DialogUtil.yesNo(m_context, "Failed to upload sensor information. Try again?", msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //재업로드 로직
                        doUploadToWebManager();
                    }
                }, null);

            }
        });
    }
}
