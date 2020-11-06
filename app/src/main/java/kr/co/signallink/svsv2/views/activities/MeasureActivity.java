package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
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
import kr.co.signallink.svsv2.model.Constants;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.server.OnTCPSendCallback;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ProgressDialogUtil;
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

    private static boolean isUpload = false;
    int uploadedSensorIndex = 0;
    int uploadedConfigSensorIndex = 0;
    Context m_context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_context = this;

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

        Button buttonExplorer = findViewById(R.id.buttonExplorer);
        buttonExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.myfiles");
                if( intent == null ) {
                    intent = getPackageManager().getLaunchIntentForPackage("com.lge.filemanager");
                }

                if( intent == null ) {
                    ToastUtil.showShort("not support open file manager.");
                    return;
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // not work
                //String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "SVSdata" + File.separator + "csv" + File.separator + "pump" + File.separator;
                //intent.setData(Uri.parse(path));
                startActivity(intent);
            }
        });

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

        Button buttonUpload = findViewById(R.id.buttonUpload);  // 웹서버로 업로드 버튼 클릭
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( bMeasure ) {
                    if( isUpload ) {
                        ToastUtil.showShort("Already uploaded.");
                        //return;
                    }

                    uploadedSensorIndex = 0;

                    guideUploadToWebManager();
                }
                else {  // 측정을 하지 않은 경우
                    ToastUtil.showShort("Please measure first.");
                }
            }
        });

        Button buttonTestData = findViewById(R.id.buttonTestData);
        buttonTestData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bTestData = true;
                makeMatrix2();

                // 다음 화면으로 이동
                //Intent intent = new Intent(getBaseContext(), ResultDiagnosisActivity.class);
                Intent intent = new Intent(getBaseContext(), ResultActivity.class);

                DIAGNOSIS_DATA_Type[] rawData = mainData.fnGetRawDatas();   // 임시데이터

                intent.putExtra("matrix2", matrix2);
                intent.putExtra("analysisData", analysisData);
                intent.putExtra("equipmentUuid", equipmentUuid);
                intent.putExtra("rawData", rawData);
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
        lineChartRawData.setScaleXEnabled(false);   // added by hslee 2020-10-30 x측 zoom하면 임의로 넣은 label값이 맞지 않게 됨

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
        xAxis.setLabelCount(9, true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if( index == 127 )// added by hslee 2020.07.15
                    return "200";
                else if( index == 255 )
                    return "400";
                else if( index == 383 )
                    return "600";
                else if( index == 511 )
                    return "800";
                else if( index == 639 )
                    return "1000";
                else if( index == 767 )
                    return "1200";
                else if( index == 895 )
                    return "1400";
                else if( index == 1023 )
                    return "1600";
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

        if( !(measuredFreq1 == null || measuredFreq2 == null || measuredFreq3 == null) ) { // 기존에 측정한 데이터가 있으면 표시
            drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
        }
    }

    private OnChartValueSelectedListener onChartValueSelectedListenerRawData = new OnChartValueSelectedListener() {

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            TextView textViewSelectedItemValue = findViewById(R.id.textViewSelectedRawDataValue);
            textViewSelectedItemValue.setText(String.format("%.3fmm/s", e.getY()));
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

                lineData.addDataSet(generateLineData("Vertical", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));
            }

            if( bShowChartPt2 ) {
                if (data2 != null) {
                    for (float v : data2) {
                        valueList2.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("Horizontal", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));
            }

            if( bShowChartPt3 ) {
                if (data3 != null) {
                    for (float v : data3) {
                        valueList3.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("Axial", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myblue)));
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
            if( i < 2 ) {    // added by hslee 2020-10-30 펌프는 앞의 두개 0으로 처리
                entries.add(new Entry(i, 0));
                continue;
            }

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
            //rawData1.fSamplingRate = (float) 1.338975e+03;
            rawData1.dFreq = analysisData.getMeasureData1().getAxisBuf().getfFreq(); // added by hslee 2020-11-05 dfreq는 안쓴다고 함.
            rawData1.dPwrSpectrum = analysisData.getMeasureData1().getAxisBuf().getfFreq(); // added by hslee 2020-11-05 freq가 스펙트럼이라고 함.

            DIAGNOSIS_DATA_Type rawData2 = new DIAGNOSIS_DATA_Type();
            //rawData2.fSamplingRate = (float) 1.386214e+03;
            rawData2.dFreq = analysisData.getMeasureData2().getAxisBuf().getfFreq();
            rawData2.dPwrSpectrum = analysisData.getMeasureData2().getAxisBuf().getfFreq();

            DIAGNOSIS_DATA_Type rawData3 = new DIAGNOSIS_DATA_Type();
            //rawData3.fSamplingRate = (float) 1.384258e+03;
            rawData3.dFreq = analysisData.getMeasureData3().getAxisBuf().getfFreq();
            rawData3.dPwrSpectrum = analysisData.getMeasureData3().getAxisBuf().getfFreq();

            rawData1.fSamplingRate = analysisData.getMeasureData1().getfSplFreqMes();   // added by hslee 2020-11-05
            rawData2.fSamplingRate = analysisData.getMeasureData2().getfSplFreqMes();
            rawData3.fSamplingRate = analysisData.getMeasureData3().getfSplFreqMes();
//            rawData1.fSamplingRate = 3378.1f;
//            rawData2.fSamplingRate = 3378.1f;
//            rawData3.fSamplingRate = 3378.1f;

//            rawData1.fSamplingRate = (float)(analysisData.getMeasureData1().getAxisBuf().getfFreq()[Constants.FREQ_ELE - 1] * 2);
//            rawData2.fSamplingRate = (float)(analysisData.getMeasureData2().getAxisBuf().getfFreq()[Constants.FREQ_ELE - 1] * 2);
//            rawData3.fSamplingRate = (float)(analysisData.getMeasureData3().getAxisBuf().getfFreq()[Constants.FREQ_ELE - 1] * 2);

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

//                DIAGNOSIS_DATA_Type testData1 = new DIAGNOSIS_DATA_Type();
//                DIAGNOSIS_DATA_Type testData2 = new DIAGNOSIS_DATA_Type();
//                DIAGNOSIS_DATA_Type testData3 = new DIAGNOSIS_DATA_Type();

//                testData1.dPwrSpectrum = new float[]{1.6494629f, 3.2989259f, 4.948389f, 6.5978518f, 8.247314f, 9.896778f, 11.546241f, 13.1957035f, 14.845166f, 16.494629f, 18.144093f, 19.793556f, 21.443018f, 23.092482f, 24.741943f, 26.391407f, 28.04087f, 29.690332f, 31.339796f, 32.989258f, 34.63872f, 36.288185f, 37.93765f, 39.587112f, 41.236572f, 42.886036f, 44.5355f, 46.184963f, 47.834427f, 49.483887f, 51.13335f, 52.782814f, 54.432278f, 56.08174f, 57.7312f, 59.380665f, 61.03013f, 62.679592f, 64.329056f, 65.978516f, 67.62798f, 69.27744f, 70.9269f, 72.57637f, 74.22583f, 75.8753f, 77.52476f, 79.174225f, 80.823685f, 82.473145f, 84.12261f, 85.77207f, 87.42154f, 89.071f, 90.72046f, 92.36993f, 94.01939f, 95.66885f, 97.31831f, 98.96777f, 100.61724f, 102.2667f, 103.91617f, 105.56563f, 107.21509f, 108.864555f, 110.514015f, 112.16348f, 113.81294f, 115.4624f, 117.11187f, 118.76133f, 120.4108f, 122.06026f, 123.70972f, 125.359184f, 127.008644f, 128.65811f, 130.30757f, 131.95703f, 133.60649f, 135.25597f, 136.90543f, 138.55489f, 140.20435f, 141.8538f, 143.50328f, 145.15274f, 146.8022f, 148.45166f, 150.10112f, 151.7506f, 153.40005f, 155.04951f, 156.69897f, 158.34845f, 159.99791f, 161.64737f, 163.29683f, 164.94629f, 166.59576f, 168.24522f, 169.89468f, 171.54414f, 173.1936f, 174.84308f, 176.49254f, 178.142f, 179.79146f, 181.44092f, 183.0904f, 184.73985f, 186.38931f, 188.03877f, 189.68823f, 191.3377f, 192.98717f, 194.63663f, 196.28609f, 197.93555f, 199.58502f, 201.23448f, 202.88394f, 204.5334f, 206.18286f, 207.83234f, 209.4818f, 211.13126f, 212.78072f, 214.43018f, 216.07965f, 217.72911f, 219.37857f, 221.02803f, 222.67749f, 224.32697f, 225.97643f, 227.62589f, 229.27534f, 230.9248f, 232.57428f, 234.22374f, 235.8732f, 237.52266f, 239.17212f, 240.8216f, 242.47105f, 244.12051f, 245.76997f, 247.41943f, 249.06891f, 250.71837f, 252.36783f, 254.01729f, 255.66675f, 257.31622f, 258.96567f, 260.61514f, 262.26462f, 263.91406f, 265.56354f, 267.21298f, 268.86246f, 270.51193f, 272.16138f, 273.81085f, 275.4603f, 277.10977f, 278.75925f, 280.4087f, 282.05817f, 283.7076f, 285.3571f, 287.00656f, 288.656f, 290.30548f, 291.95493f, 293.6044f, 295.25388f, 296.90332f, 298.5528f, 300.20224f, 301.8517f, 303.5012f, 305.15063f, 306.8001f, 308.44955f, 310.09903f, 311.7485f, 313.39795f, 315.04742f, 316.6969f, 318.34634f, 319.99582f, 321.64526f, 323.29474f, 324.9442f, 326.59366f, 328.24313f, 329.89258f, 331.54205f, 333.19153f, 334.84097f, 336.49045f, 338.1399f, 339.78937f, 341.43884f, 343.0883f, 344.73776f, 346.3872f, 348.03668f, 349.68616f, 351.3356f, 352.98508f, 354.63452f, 356.284f, 357.93347f, 359.58292f, 361.2324f, 362.88184f, 364.5313f, 366.1808f, 367.83023f, 369.4797f, 371.12915f, 372.77863f, 374.4281f, 376.07755f, 377.72702f, 379.37646f, 381.02594f, 382.6754f, 384.32486f, 385.97433f, 387.62378f, 389.27325f, 390.92273f, 392.57217f, 394.22165f, 395.8711f, 397.52057f, 399.17004f, 400.8195f, 402.46896f, 404.1184f, 405.76788f, 407.41736f, 409.0668f, 410.71628f, 412.36572f, 414.0152f, 415.66467f, 417.31412f, 418.9636f, 420.61304f, 422.2625f, 423.912f, 425.56143f, 427.2109f, 428.86035f, 430.50983f, 432.1593f, 433.80875f, 435.45822f, 437.10767f, 438.75714f, 440.40662f, 442.05606f, 443.70554f, 445.35498f, 447.00446f, 448.65393f, 450.30338f, 451.95285f, 453.6023f, 455.25177f, 456.90125f, 458.5507f, 460.20016f, 461.8496f, 463.49908f, 465.14856f, 466.798f, 468.44748f, 470.09692f, 471.7464f, 473.39587f, 475.04532f, 476.6948f, 478.34424f, 479.9937f, 481.6432f, 483.29263f, 484.9421f, 486.59155f, 488.24103f, 489.8905f, 491.53995f, 493.18942f, 494.83887f, 496.48834f, 498.13782f, 499.78726f, 501.43674f, 503.08618f, 504.73566f, 506.38513f, 508.03458f, 509.68405f, 511.3335f, 512.983f, 514.63245f, 516.2819f, 517.93134f, 519.5808f, 521.2303f, 522.87976f, 524.52924f, 526.17865f, 527.8281f, 529.4776f, 531.1271f, 532.77655f, 534.42596f, 536.07544f, 537.7249f, 539.3744f, 541.02386f, 542.6733f, 544.32275f, 545.9722f, 547.6217f, 549.2712f, 550.9206f, 552.57007f, 554.21954f, 555.869f, 557.5185f, 559.1679f, 560.8174f, 562.46686f, 564.11633f, 565.7658f, 567.4152f, 569.0647f, 570.7142f, 572.36365f, 574.0131f, 575.66254f, 577.312f, 578.9615f, 580.61096f, 582.26044f, 583.90985f, 585.5593f, 587.2088f, 588.8583f, 590.50775f, 592.15717f, 593.80664f, 595.4561f, 597.1056f, 598.75507f, 600.4045f, 602.05396f, 603.7034f, 605.3529f, 607.0024f, 608.6518f, 610.3013f, 611.95074f, 613.6002f, 615.2497f, 616.8991f, 618.5486f, 620.19806f, 621.84753f, 623.497f, 625.1464f, 626.7959f, 628.4454f, 630.09485f, 631.7443f, 633.3938f, 635.0432f, 636.6927f, 638.34216f, 639.99164f, 641.6411f, 643.2905f, 644.94f, 646.5895f, 648.23895f, 649.8884f, 651.53784f, 653.1873f, 654.8368f, 656.48627f, 658.13574f, 659.78516f, 661.43463f, 663.0841f, 664.7336f, 666.38306f, 668.0325f, 669.68195f, 671.3314f, 672.9809f, 674.6304f, 676.2798f, 677.92926f, 679.57874f, 681.2282f, 682.8777f, 684.5271f, 686.1766f, 687.82605f, 689.4755f, 691.125f, 692.7744f, 694.4239f, 696.07336f, 697.72284f, 699.3723f, 701.0217f, 702.6712f, 704.3207f, 705.97015f, 707.6196f, 709.26904f, 710.9185f, 712.568f, 714.21747f, 715.86694f, 717.51636f, 719.16583f, 720.8153f, 722.4648f, 724.11426f, 725.7637f, 727.41315f, 729.0626f, 730.7121f, 732.3616f, 734.011f, 735.66046f, 737.30994f, 738.9594f, 740.6089f, 742.2583f, 743.9078f, 745.55725f, 747.2067f, 748.8562f, 750.5056f, 752.1551f, 753.80457f, 755.45404f, 757.1035f, 758.7529f, 760.4024f, 762.0519f, 763.70135f, 765.3508f, 767.00024f, 768.6497f, 770.2992f, 771.94867f, 773.59814f, 775.24756f, 776.89703f, 778.5465f, 780.196f, 781.84546f, 783.4949f, 785.14435f, 786.7938f, 788.4433f, 790.0928f, 791.7422f, 793.39166f, 795.04114f, 796.6906f, 798.3401f, 799.9895f, 801.639f, 803.28845f, 804.9379f, 806.5874f, 808.2368f, 809.8863f, 811.53577f, 813.18524f, 814.8347f, 816.48413f, 818.1336f, 819.7831f, 821.43256f, 823.08203f, 824.73145f, 826.3809f, 828.0304f, 829.6799f, 831.32935f, 832.97876f, 834.62823f, 836.2777f, 837.9272f, 839.57666f, 841.2261f, 842.87555f, 844.525f, 846.1745f, 847.824f, 849.4734f, 851.12286f, 852.77234f, 854.4218f, 856.0713f, 857.7207f, 859.3702f, 861.01965f, 862.6691f, 864.3186f, 865.968f, 867.6175f, 869.26697f, 870.91644f, 872.5659f, 874.21533f, 875.8648f, 877.5143f, 879.16376f, 880.81323f, 882.46265f, 884.1121f, 885.7616f, 887.4111f, 889.06055f, 890.70996f, 892.35944f, 894.0089f, 895.6584f, 897.30786f, 898.9573f, 900.60675f, 902.2562f, 903.9057f, 905.5552f, 907.2046f, 908.85406f, 910.50354f, 912.153f, 913.8025f, 915.4519f, 917.1014f, 918.75085f, 920.4003f, 922.0498f, 923.6992f, 925.3487f, 926.99817f, 928.64764f, 930.2971f, 931.94653f, 933.596f, 935.2455f, 936.89496f, 938.54443f, 940.19385f, 941.8433f, 943.4928f, 945.1423f, 946.79175f, 948.44116f, 950.09064f, 951.7401f, 953.3896f, 955.03906f, 956.6885f, 958.33795f, 959.9874f, 961.6369f, 963.2864f, 964.9358f, 966.58527f, 968.23474f, 969.8842f, 971.5337f, 973.1831f, 974.8326f, 976.48206f, 978.13153f, 979.781f, 981.4304f, 983.0799f, 984.7294f, 986.37885f, 988.0283f, 989.67773f, 991.3272f, 992.9767f, 994.62616f, 996.27563f, 997.92505f, 999.5745f, 1001.224f, 1002.8735f, 1004.52295f, 1006.17236f, 1007.82184f, 1009.4713f, 1011.1208f, 1012.77026f, 1014.4197f, 1016.06915f, 1017.7186f, 1019.3681f, 1021.0176f, 1022.667f, 1024.3165f, 1025.966f, 1027.6154f, 1029.2649f, 1030.9143f, 1032.5638f, 1034.2133f, 1035.8627f, 1037.5122f, 1039.1616f, 1040.8112f, 1042.4606f, 1044.11f, 1045.7595f, 1047.4089f, 1049.0585f, 1050.7079f, 1052.3573f, 1054.0068f, 1055.6562f, 1057.3058f, 1058.9552f, 1060.6046f, 1062.2542f, 1063.9036f, 1065.5531f, 1067.2025f, 1068.8519f, 1070.5015f, 1072.1509f, 1073.8004f, 1075.4498f, 1077.0992f, 1078.7488f, 1080.3982f, 1082.0477f, 1083.6971f, 1085.3466f, 1086.9961f, 1088.6455f, 1090.295f, 1091.9445f, 1093.5939f, 1095.2434f, 1096.8928f, 1098.5424f, 1100.1918f, 1101.8412f, 1103.4907f, 1105.1401f, 1106.7897f, 1108.4391f, 1110.0885f, 1111.738f, 1113.3875f, 1115.037f, 1116.6864f, 1118.3358f, 1119.9854f, 1121.6348f, 1123.2843f, 1124.9337f, 1126.5831f, 1128.2327f, 1129.8821f, 1131.5316f, 1133.181f, 1134.8304f, 1136.48f, 1138.1294f, 1139.7789f, 1141.4283f, 1143.0778f, 1144.7273f, 1146.3767f, 1148.0262f, 1149.6757f, 1151.3251f, 1152.9746f, 1154.624f, 1156.2736f, 1157.923f, 1159.5724f, 1161.2219f, 1162.8713f, 1164.5209f, 1166.1703f, 1167.8197f, 1169.4692f, 1171.1187f, 1172.7682f, 1174.4176f, 1176.067f, 1177.7166f, 1179.366f, 1181.0155f, 1182.6649f, 1184.3143f, 1185.9639f, 1187.6133f, 1189.2628f, 1190.9122f, 1192.5616f, 1194.2112f, 1195.8606f, 1197.5101f, 1199.1595f, 1200.809f, 1202.4585f, 1204.1079f, 1205.7574f, 1207.4069f, 1209.0563f, 1210.7058f, 1212.3552f, 1214.0048f, 1215.6542f, 1217.3036f, 1218.9531f, 1220.6025f, 1222.2521f, 1223.9015f, 1225.5509f, 1227.2004f, 1228.8499f, 1230.4994f, 1232.1488f, 1233.7982f, 1235.4478f, 1237.0972f, 1238.7467f, 1240.3961f, 1242.0455f, 1243.6951f, 1245.3445f, 1246.994f, 1248.6434f, 1250.2928f, 1251.9424f, 1253.5918f, 1255.2413f, 1256.8907f, 1258.5402f, 1260.1897f, 1261.8391f, 1263.4886f, 1265.1381f, 1266.7876f, 1268.437f, 1270.0864f, 1271.736f, 1273.3854f, 1275.0349f, 1276.6843f, 1278.3337f, 1279.9833f, 1281.6327f, 1283.2822f, 1284.9316f, 1286.581f, 1288.2306f, 1289.88f, 1291.5295f, 1293.179f, 1294.8284f, 1296.4779f, 1298.1273f, 1299.7769f, 1301.4263f, 1303.0757f, 1304.7252f, 1306.3746f, 1308.0242f, 1309.6736f, 1311.323f, 1312.9725f, 1314.622f, 1316.2715f, 1317.9209f, 1319.5703f, 1321.2198f, 1322.8693f, 1324.5188f, 1326.1682f, 1327.8176f, 1329.4672f, 1331.1166f, 1332.7661f, 1334.4155f, 1336.065f, 1337.7145f, 1339.3639f, 1341.0134f, 1342.6628f, 1344.3123f, 1345.9618f, 1347.6112f, 1349.2607f, 1350.9102f, 1352.5596f, 1354.2091f, 1355.8585f, 1357.508f, 1359.1575f, 1360.8069f, 1362.4564f, 1364.1058f, 1365.7554f, 1367.4048f, 1369.0542f, 1370.7037f, 1372.3531f, 1374.0027f, 1375.6521f, 1377.3015f, 1378.951f, 1380.6005f, 1382.25f, 1383.8994f, 1385.5488f, 1387.1984f, 1388.8478f, 1390.4973f, 1392.1467f, 1393.7961f, 1395.4457f, 1397.0951f, 1398.7446f, 1400.394f, 1402.0435f, 1403.693f, 1405.3424f, 1406.992f, 1408.6414f, 1410.2908f, 1411.9403f, 1413.5897f, 1415.2393f, 1416.8887f, 1418.5381f, 1420.1876f, 1421.837f, 1423.4866f, 1425.136f, 1426.7854f, 1428.4349f, 1430.0844f, 1431.7339f, 1433.3833f, 1435.0327f, 1436.6823f, 1438.3317f, 1439.9812f, 1441.6306f, 1443.28f, 1444.9296f, 1446.579f, 1448.2285f, 1449.8779f, 1451.5273f, 1453.1769f, 1454.8263f, 1456.4758f, 1458.1252f, 1459.7747f, 1461.4242f, 1463.0736f, 1464.7231f, 1466.3726f, 1468.022f, 1469.6715f, 1471.3209f, 1472.9705f, 1474.6199f, 1476.2693f, 1477.9188f, 1479.5682f, 1481.2178f, 1482.8672f, 1484.5166f, 1486.1661f, 1487.8156f, 1489.4651f, 1491.1145f, 1492.7639f, 1494.4135f, 1496.0629f, 1497.7124f, 1499.3618f, 1501.0112f, 1502.6608f, 1504.3102f, 1505.9597f, 1507.6091f, 1509.2585f, 1510.9081f, 1512.5575f, 1514.207f, 1515.8564f, 1517.5059f, 1519.1554f, 1520.8048f, 1522.4543f, 1524.1038f, 1525.7532f, 1527.4027f, 1529.0521f, 1530.7017f, 1532.3511f, 1534.0005f, 1535.65f, 1537.2994f, 1538.949f, 1540.5984f, 1542.2478f, 1543.8973f, 1545.5468f, 1547.1963f, 1548.8457f, 1550.4951f, 1552.1447f, 1553.7941f, 1555.4436f, 1557.093f, 1558.7424f, 1560.392f, 1562.0414f, 1563.6909f, 1565.3403f, 1566.9897f, 1568.6393f, 1570.2887f, 1571.9382f, 1573.5876f, 1575.237f, 1576.8866f, 1578.536f, 1580.1855f, 1581.835f, 1583.4844f, 1585.1339f, 1586.7833f, 1588.4329f, 1590.0823f, 1591.7317f, 1593.3812f, 1595.0306f, 1596.6802f, 1598.3296f, 1599.979f, 1601.6285f, 1603.278f, 1604.9275f, 1606.5769f, 1608.2263f, 1609.8759f, 1611.5253f, 1613.1748f, 1614.8242f, 1616.4736f, 1618.1232f, 1619.7726f, 1621.4221f, 1623.0715f, 1624.721f, 1626.3705f, 1628.0199f, 1629.6694f, 1631.3188f, 1632.9683f, 1634.6178f, 1636.2672f, 1637.9167f, 1639.5662f, 1641.2156f, 1642.8651f, 1644.5145f, 1646.1641f, 1647.8135f, 1649.4629f, 1651.1124f, 1652.7618f, 1654.4114f, 1656.0608f, 1657.7102f, 1659.3597f, 1661.0092f, 1662.6587f, 1664.3081f, 1665.9575f, 1667.607f, 1669.2565f, 1670.906f, 1672.5554f, 1674.2048f, 1675.8544f, 1677.5038f, 1679.1533f, 1680.8027f, 1682.4521f, 1684.1017f, 1685.7511f, 1687.4006f, 1689.05f};
//                testData2.dPwrSpectrum = new float[]{1.6494629f, 3.2989259f, 4.948389f, 6.5978518f, 8.247314f, 9.896778f, 11.546241f, 13.1957035f, 14.845166f, 16.494629f, 18.144093f, 19.793556f, 21.443018f, 23.092482f, 24.741943f, 26.391407f, 28.04087f, 29.690332f, 31.339796f, 32.989258f, 34.63872f, 36.288185f, 37.93765f, 39.587112f, 41.236572f, 42.886036f, 44.5355f, 46.184963f, 47.834427f, 49.483887f, 51.13335f, 52.782814f, 54.432278f, 56.08174f, 57.7312f, 59.380665f, 61.03013f, 62.679592f, 64.329056f, 65.978516f, 67.62798f, 69.27744f, 70.9269f, 72.57637f, 74.22583f, 75.8753f, 77.52476f, 79.174225f, 80.823685f, 82.473145f, 84.12261f, 85.77207f, 87.42154f, 89.071f, 90.72046f, 92.36993f, 94.01939f, 95.66885f, 97.31831f, 98.96777f, 100.61724f, 102.2667f, 103.91617f, 105.56563f, 107.21509f, 108.864555f, 110.514015f, 112.16348f, 113.81294f, 115.4624f, 117.11187f, 118.76133f, 120.4108f, 122.06026f, 123.70972f, 125.359184f, 127.008644f, 128.65811f, 130.30757f, 131.95703f, 133.60649f, 135.25597f, 136.90543f, 138.55489f, 140.20435f, 141.8538f, 143.50328f, 145.15274f, 146.8022f, 148.45166f, 150.10112f, 151.7506f, 153.40005f, 155.04951f, 156.69897f, 158.34845f, 159.99791f, 161.64737f, 163.29683f, 164.94629f, 166.59576f, 168.24522f, 169.89468f, 171.54414f, 173.1936f, 174.84308f, 176.49254f, 178.142f, 179.79146f, 181.44092f, 183.0904f, 184.73985f, 186.38931f, 188.03877f, 189.68823f, 191.3377f, 192.98717f, 194.63663f, 196.28609f, 197.93555f, 199.58502f, 201.23448f, 202.88394f, 204.5334f, 206.18286f, 207.83234f, 209.4818f, 211.13126f, 212.78072f, 214.43018f, 216.07965f, 217.72911f, 219.37857f, 221.02803f, 222.67749f, 224.32697f, 225.97643f, 227.62589f, 229.27534f, 230.9248f, 232.57428f, 234.22374f, 235.8732f, 237.52266f, 239.17212f, 240.8216f, 242.47105f, 244.12051f, 245.76997f, 247.41943f, 249.06891f, 250.71837f, 252.36783f, 254.01729f, 255.66675f, 257.31622f, 258.96567f, 260.61514f, 262.26462f, 263.91406f, 265.56354f, 267.21298f, 268.86246f, 270.51193f, 272.16138f, 273.81085f, 275.4603f, 277.10977f, 278.75925f, 280.4087f, 282.05817f, 283.7076f, 285.3571f, 287.00656f, 288.656f, 290.30548f, 291.95493f, 293.6044f, 295.25388f, 296.90332f, 298.5528f, 300.20224f, 301.8517f, 303.5012f, 305.15063f, 306.8001f, 308.44955f, 310.09903f, 311.7485f, 313.39795f, 315.04742f, 316.6969f, 318.34634f, 319.99582f, 321.64526f, 323.29474f, 324.9442f, 326.59366f, 328.24313f, 329.89258f, 331.54205f, 333.19153f, 334.84097f, 336.49045f, 338.1399f, 339.78937f, 341.43884f, 343.0883f, 344.73776f, 346.3872f, 348.03668f, 349.68616f, 351.3356f, 352.98508f, 354.63452f, 356.284f, 357.93347f, 359.58292f, 361.2324f, 362.88184f, 364.5313f, 366.1808f, 367.83023f, 369.4797f, 371.12915f, 372.77863f, 374.4281f, 376.07755f, 377.72702f, 379.37646f, 381.02594f, 382.6754f, 384.32486f, 385.97433f, 387.62378f, 389.27325f, 390.92273f, 392.57217f, 394.22165f, 395.8711f, 397.52057f, 399.17004f, 400.8195f, 402.46896f, 404.1184f, 405.76788f, 407.41736f, 409.0668f, 410.71628f, 412.36572f, 414.0152f, 415.66467f, 417.31412f, 418.9636f, 420.61304f, 422.2625f, 423.912f, 425.56143f, 427.2109f, 428.86035f, 430.50983f, 432.1593f, 433.80875f, 435.45822f, 437.10767f, 438.75714f, 440.40662f, 442.05606f, 443.70554f, 445.35498f, 447.00446f, 448.65393f, 450.30338f, 451.95285f, 453.6023f, 455.25177f, 456.90125f, 458.5507f, 460.20016f, 461.8496f, 463.49908f, 465.14856f, 466.798f, 468.44748f, 470.09692f, 471.7464f, 473.39587f, 475.04532f, 476.6948f, 478.34424f, 479.9937f, 481.6432f, 483.29263f, 484.9421f, 486.59155f, 488.24103f, 489.8905f, 491.53995f, 493.18942f, 494.83887f, 496.48834f, 498.13782f, 499.78726f, 501.43674f, 503.08618f, 504.73566f, 506.38513f, 508.03458f, 509.68405f, 511.3335f, 512.983f, 514.63245f, 516.2819f, 517.93134f, 519.5808f, 521.2303f, 522.87976f, 524.52924f, 526.17865f, 527.8281f, 529.4776f, 531.1271f, 532.77655f, 534.42596f, 536.07544f, 537.7249f, 539.3744f, 541.02386f, 542.6733f, 544.32275f, 545.9722f, 547.6217f, 549.2712f, 550.9206f, 552.57007f, 554.21954f, 555.869f, 557.5185f, 559.1679f, 560.8174f, 562.46686f, 564.11633f, 565.7658f, 567.4152f, 569.0647f, 570.7142f, 572.36365f, 574.0131f, 575.66254f, 577.312f, 578.9615f, 580.61096f, 582.26044f, 583.90985f, 585.5593f, 587.2088f, 588.8583f, 590.50775f, 592.15717f, 593.80664f, 595.4561f, 597.1056f, 598.75507f, 600.4045f, 602.05396f, 603.7034f, 605.3529f, 607.0024f, 608.6518f, 610.3013f, 611.95074f, 613.6002f, 615.2497f, 616.8991f, 618.5486f, 620.19806f, 621.84753f, 623.497f, 625.1464f, 626.7959f, 628.4454f, 630.09485f, 631.7443f, 633.3938f, 635.0432f, 636.6927f, 638.34216f, 639.99164f, 641.6411f, 643.2905f, 644.94f, 646.5895f, 648.23895f, 649.8884f, 651.53784f, 653.1873f, 654.8368f, 656.48627f, 658.13574f, 659.78516f, 661.43463f, 663.0841f, 664.7336f, 666.38306f, 668.0325f, 669.68195f, 671.3314f, 672.9809f, 674.6304f, 676.2798f, 677.92926f, 679.57874f, 681.2282f, 682.8777f, 684.5271f, 686.1766f, 687.82605f, 689.4755f, 691.125f, 692.7744f, 694.4239f, 696.07336f, 697.72284f, 699.3723f, 701.0217f, 702.6712f, 704.3207f, 705.97015f, 707.6196f, 709.26904f, 710.9185f, 712.568f, 714.21747f, 715.86694f, 717.51636f, 719.16583f, 720.8153f, 722.4648f, 724.11426f, 725.7637f, 727.41315f, 729.0626f, 730.7121f, 732.3616f, 734.011f, 735.66046f, 737.30994f, 738.9594f, 740.6089f, 742.2583f, 743.9078f, 745.55725f, 747.2067f, 748.8562f, 750.5056f, 752.1551f, 753.80457f, 755.45404f, 757.1035f, 758.7529f, 760.4024f, 762.0519f, 763.70135f, 765.3508f, 767.00024f, 768.6497f, 770.2992f, 771.94867f, 773.59814f, 775.24756f, 776.89703f, 778.5465f, 780.196f, 781.84546f, 783.4949f, 785.14435f, 786.7938f, 788.4433f, 790.0928f, 791.7422f, 793.39166f, 795.04114f, 796.6906f, 798.3401f, 799.9895f, 801.639f, 803.28845f, 804.9379f, 806.5874f, 808.2368f, 809.8863f, 811.53577f, 813.18524f, 814.8347f, 816.48413f, 818.1336f, 819.7831f, 821.43256f, 823.08203f, 824.73145f, 826.3809f, 828.0304f, 829.6799f, 831.32935f, 832.97876f, 834.62823f, 836.2777f, 837.9272f, 839.57666f, 841.2261f, 842.87555f, 844.525f, 846.1745f, 847.824f, 849.4734f, 851.12286f, 852.77234f, 854.4218f, 856.0713f, 857.7207f, 859.3702f, 861.01965f, 862.6691f, 864.3186f, 865.968f, 867.6175f, 869.26697f, 870.91644f, 872.5659f, 874.21533f, 875.8648f, 877.5143f, 879.16376f, 880.81323f, 882.46265f, 884.1121f, 885.7616f, 887.4111f, 889.06055f, 890.70996f, 892.35944f, 894.0089f, 895.6584f, 897.30786f, 898.9573f, 900.60675f, 902.2562f, 903.9057f, 905.5552f, 907.2046f, 908.85406f, 910.50354f, 912.153f, 913.8025f, 915.4519f, 917.1014f, 918.75085f, 920.4003f, 922.0498f, 923.6992f, 925.3487f, 926.99817f, 928.64764f, 930.2971f, 931.94653f, 933.596f, 935.2455f, 936.89496f, 938.54443f, 940.19385f, 941.8433f, 943.4928f, 945.1423f, 946.79175f, 948.44116f, 950.09064f, 951.7401f, 953.3896f, 955.03906f, 956.6885f, 958.33795f, 959.9874f, 961.6369f, 963.2864f, 964.9358f, 966.58527f, 968.23474f, 969.8842f, 971.5337f, 973.1831f, 974.8326f, 976.48206f, 978.13153f, 979.781f, 981.4304f, 983.0799f, 984.7294f, 986.37885f, 988.0283f, 989.67773f, 991.3272f, 992.9767f, 994.62616f, 996.27563f, 997.92505f, 999.5745f, 1001.224f, 1002.8735f, 1004.52295f, 1006.17236f, 1007.82184f, 1009.4713f, 1011.1208f, 1012.77026f, 1014.4197f, 1016.06915f, 1017.7186f, 1019.3681f, 1021.0176f, 1022.667f, 1024.3165f, 1025.966f, 1027.6154f, 1029.2649f, 1030.9143f, 1032.5638f, 1034.2133f, 1035.8627f, 1037.5122f, 1039.1616f, 1040.8112f, 1042.4606f, 1044.11f, 1045.7595f, 1047.4089f, 1049.0585f, 1050.7079f, 1052.3573f, 1054.0068f, 1055.6562f, 1057.3058f, 1058.9552f, 1060.6046f, 1062.2542f, 1063.9036f, 1065.5531f, 1067.2025f, 1068.8519f, 1070.5015f, 1072.1509f, 1073.8004f, 1075.4498f, 1077.0992f, 1078.7488f, 1080.3982f, 1082.0477f, 1083.6971f, 1085.3466f, 1086.9961f, 1088.6455f, 1090.295f, 1091.9445f, 1093.5939f, 1095.2434f, 1096.8928f, 1098.5424f, 1100.1918f, 1101.8412f, 1103.4907f, 1105.1401f, 1106.7897f, 1108.4391f, 1110.0885f, 1111.738f, 1113.3875f, 1115.037f, 1116.6864f, 1118.3358f, 1119.9854f, 1121.6348f, 1123.2843f, 1124.9337f, 1126.5831f, 1128.2327f, 1129.8821f, 1131.5316f, 1133.181f, 1134.8304f, 1136.48f, 1138.1294f, 1139.7789f, 1141.4283f, 1143.0778f, 1144.7273f, 1146.3767f, 1148.0262f, 1149.6757f, 1151.3251f, 1152.9746f, 1154.624f, 1156.2736f, 1157.923f, 1159.5724f, 1161.2219f, 1162.8713f, 1164.5209f, 1166.1703f, 1167.8197f, 1169.4692f, 1171.1187f, 1172.7682f, 1174.4176f, 1176.067f, 1177.7166f, 1179.366f, 1181.0155f, 1182.6649f, 1184.3143f, 1185.9639f, 1187.6133f, 1189.2628f, 1190.9122f, 1192.5616f, 1194.2112f, 1195.8606f, 1197.5101f, 1199.1595f, 1200.809f, 1202.4585f, 1204.1079f, 1205.7574f, 1207.4069f, 1209.0563f, 1210.7058f, 1212.3552f, 1214.0048f, 1215.6542f, 1217.3036f, 1218.9531f, 1220.6025f, 1222.2521f, 1223.9015f, 1225.5509f, 1227.2004f, 1228.8499f, 1230.4994f, 1232.1488f, 1233.7982f, 1235.4478f, 1237.0972f, 1238.7467f, 1240.3961f, 1242.0455f, 1243.6951f, 1245.3445f, 1246.994f, 1248.6434f, 1250.2928f, 1251.9424f, 1253.5918f, 1255.2413f, 1256.8907f, 1258.5402f, 1260.1897f, 1261.8391f, 1263.4886f, 1265.1381f, 1266.7876f, 1268.437f, 1270.0864f, 1271.736f, 1273.3854f, 1275.0349f, 1276.6843f, 1278.3337f, 1279.9833f, 1281.6327f, 1283.2822f, 1284.9316f, 1286.581f, 1288.2306f, 1289.88f, 1291.5295f, 1293.179f, 1294.8284f, 1296.4779f, 1298.1273f, 1299.7769f, 1301.4263f, 1303.0757f, 1304.7252f, 1306.3746f, 1308.0242f, 1309.6736f, 1311.323f, 1312.9725f, 1314.622f, 1316.2715f, 1317.9209f, 1319.5703f, 1321.2198f, 1322.8693f, 1324.5188f, 1326.1682f, 1327.8176f, 1329.4672f, 1331.1166f, 1332.7661f, 1334.4155f, 1336.065f, 1337.7145f, 1339.3639f, 1341.0134f, 1342.6628f, 1344.3123f, 1345.9618f, 1347.6112f, 1349.2607f, 1350.9102f, 1352.5596f, 1354.2091f, 1355.8585f, 1357.508f, 1359.1575f, 1360.8069f, 1362.4564f, 1364.1058f, 1365.7554f, 1367.4048f, 1369.0542f, 1370.7037f, 1372.3531f, 1374.0027f, 1375.6521f, 1377.3015f, 1378.951f, 1380.6005f, 1382.25f, 1383.8994f, 1385.5488f, 1387.1984f, 1388.8478f, 1390.4973f, 1392.1467f, 1393.7961f, 1395.4457f, 1397.0951f, 1398.7446f, 1400.394f, 1402.0435f, 1403.693f, 1405.3424f, 1406.992f, 1408.6414f, 1410.2908f, 1411.9403f, 1413.5897f, 1415.2393f, 1416.8887f, 1418.5381f, 1420.1876f, 1421.837f, 1423.4866f, 1425.136f, 1426.7854f, 1428.4349f, 1430.0844f, 1431.7339f, 1433.3833f, 1435.0327f, 1436.6823f, 1438.3317f, 1439.9812f, 1441.6306f, 1443.28f, 1444.9296f, 1446.579f, 1448.2285f, 1449.8779f, 1451.5273f, 1453.1769f, 1454.8263f, 1456.4758f, 1458.1252f, 1459.7747f, 1461.4242f, 1463.0736f, 1464.7231f, 1466.3726f, 1468.022f, 1469.6715f, 1471.3209f, 1472.9705f, 1474.6199f, 1476.2693f, 1477.9188f, 1479.5682f, 1481.2178f, 1482.8672f, 1484.5166f, 1486.1661f, 1487.8156f, 1489.4651f, 1491.1145f, 1492.7639f, 1494.4135f, 1496.0629f, 1497.7124f, 1499.3618f, 1501.0112f, 1502.6608f, 1504.3102f, 1505.9597f, 1507.6091f, 1509.2585f, 1510.9081f, 1512.5575f, 1514.207f, 1515.8564f, 1517.5059f, 1519.1554f, 1520.8048f, 1522.4543f, 1524.1038f, 1525.7532f, 1527.4027f, 1529.0521f, 1530.7017f, 1532.3511f, 1534.0005f, 1535.65f, 1537.2994f, 1538.949f, 1540.5984f, 1542.2478f, 1543.8973f, 1545.5468f, 1547.1963f, 1548.8457f, 1550.4951f, 1552.1447f, 1553.7941f, 1555.4436f, 1557.093f, 1558.7424f, 1560.392f, 1562.0414f, 1563.6909f, 1565.3403f, 1566.9897f, 1568.6393f, 1570.2887f, 1571.9382f, 1573.5876f, 1575.237f, 1576.8866f, 1578.536f, 1580.1855f, 1581.835f, 1583.4844f, 1585.1339f, 1586.7833f, 1588.4329f, 1590.0823f, 1591.7317f, 1593.3812f, 1595.0306f, 1596.6802f, 1598.3296f, 1599.979f, 1601.6285f, 1603.278f, 1604.9275f, 1606.5769f, 1608.2263f, 1609.8759f, 1611.5253f, 1613.1748f, 1614.8242f, 1616.4736f, 1618.1232f, 1619.7726f, 1621.4221f, 1623.0715f, 1624.721f, 1626.3705f, 1628.0199f, 1629.6694f, 1631.3188f, 1632.9683f, 1634.6178f, 1636.2672f, 1637.9167f, 1639.5662f, 1641.2156f, 1642.8651f, 1644.5145f, 1646.1641f, 1647.8135f, 1649.4629f, 1651.1124f, 1652.7618f, 1654.4114f, 1656.0608f, 1657.7102f, 1659.3597f, 1661.0092f, 1662.6587f, 1664.3081f, 1665.9575f, 1667.607f, 1669.2565f, 1670.906f, 1672.5554f, 1674.2048f, 1675.8544f, 1677.5038f, 1679.1533f, 1680.8027f, 1682.4521f, 1684.1017f, 1685.7511f, 1687.4006f, 1689.05f};
//                testData3.dPwrSpectrum = new float[]{1.6494629f, 3.2989259f, 4.948389f, 6.5978518f, 8.247314f, 9.896778f, 11.546241f, 13.1957035f, 14.845166f, 16.494629f, 18.144093f, 19.793556f, 21.443018f, 23.092482f, 24.741943f, 26.391407f, 28.04087f, 29.690332f, 31.339796f, 32.989258f, 34.63872f, 36.288185f, 37.93765f, 39.587112f, 41.236572f, 42.886036f, 44.5355f, 46.184963f, 47.834427f, 49.483887f, 51.13335f, 52.782814f, 54.432278f, 56.08174f, 57.7312f, 59.380665f, 61.03013f, 62.679592f, 64.329056f, 65.978516f, 67.62798f, 69.27744f, 70.9269f, 72.57637f, 74.22583f, 75.8753f, 77.52476f, 79.174225f, 80.823685f, 82.473145f, 84.12261f, 85.77207f, 87.42154f, 89.071f, 90.72046f, 92.36993f, 94.01939f, 95.66885f, 97.31831f, 98.96777f, 100.61724f, 102.2667f, 103.91617f, 105.56563f, 107.21509f, 108.864555f, 110.514015f, 112.16348f, 113.81294f, 115.4624f, 117.11187f, 118.76133f, 120.4108f, 122.06026f, 123.70972f, 125.359184f, 127.008644f, 128.65811f, 130.30757f, 131.95703f, 133.60649f, 135.25597f, 136.90543f, 138.55489f, 140.20435f, 141.8538f, 143.50328f, 145.15274f, 146.8022f, 148.45166f, 150.10112f, 151.7506f, 153.40005f, 155.04951f, 156.69897f, 158.34845f, 159.99791f, 161.64737f, 163.29683f, 164.94629f, 166.59576f, 168.24522f, 169.89468f, 171.54414f, 173.1936f, 174.84308f, 176.49254f, 178.142f, 179.79146f, 181.44092f, 183.0904f, 184.73985f, 186.38931f, 188.03877f, 189.68823f, 191.3377f, 192.98717f, 194.63663f, 196.28609f, 197.93555f, 199.58502f, 201.23448f, 202.88394f, 204.5334f, 206.18286f, 207.83234f, 209.4818f, 211.13126f, 212.78072f, 214.43018f, 216.07965f, 217.72911f, 219.37857f, 221.02803f, 222.67749f, 224.32697f, 225.97643f, 227.62589f, 229.27534f, 230.9248f, 232.57428f, 234.22374f, 235.8732f, 237.52266f, 239.17212f, 240.8216f, 242.47105f, 244.12051f, 245.76997f, 247.41943f, 249.06891f, 250.71837f, 252.36783f, 254.01729f, 255.66675f, 257.31622f, 258.96567f, 260.61514f, 262.26462f, 263.91406f, 265.56354f, 267.21298f, 268.86246f, 270.51193f, 272.16138f, 273.81085f, 275.4603f, 277.10977f, 278.75925f, 280.4087f, 282.05817f, 283.7076f, 285.3571f, 287.00656f, 288.656f, 290.30548f, 291.95493f, 293.6044f, 295.25388f, 296.90332f, 298.5528f, 300.20224f, 301.8517f, 303.5012f, 305.15063f, 306.8001f, 308.44955f, 310.09903f, 311.7485f, 313.39795f, 315.04742f, 316.6969f, 318.34634f, 319.99582f, 321.64526f, 323.29474f, 324.9442f, 326.59366f, 328.24313f, 329.89258f, 331.54205f, 333.19153f, 334.84097f, 336.49045f, 338.1399f, 339.78937f, 341.43884f, 343.0883f, 344.73776f, 346.3872f, 348.03668f, 349.68616f, 351.3356f, 352.98508f, 354.63452f, 356.284f, 357.93347f, 359.58292f, 361.2324f, 362.88184f, 364.5313f, 366.1808f, 367.83023f, 369.4797f, 371.12915f, 372.77863f, 374.4281f, 376.07755f, 377.72702f, 379.37646f, 381.02594f, 382.6754f, 384.32486f, 385.97433f, 387.62378f, 389.27325f, 390.92273f, 392.57217f, 394.22165f, 395.8711f, 397.52057f, 399.17004f, 400.8195f, 402.46896f, 404.1184f, 405.76788f, 407.41736f, 409.0668f, 410.71628f, 412.36572f, 414.0152f, 415.66467f, 417.31412f, 418.9636f, 420.61304f, 422.2625f, 423.912f, 425.56143f, 427.2109f, 428.86035f, 430.50983f, 432.1593f, 433.80875f, 435.45822f, 437.10767f, 438.75714f, 440.40662f, 442.05606f, 443.70554f, 445.35498f, 447.00446f, 448.65393f, 450.30338f, 451.95285f, 453.6023f, 455.25177f, 456.90125f, 458.5507f, 460.20016f, 461.8496f, 463.49908f, 465.14856f, 466.798f, 468.44748f, 470.09692f, 471.7464f, 473.39587f, 475.04532f, 476.6948f, 478.34424f, 479.9937f, 481.6432f, 483.29263f, 484.9421f, 486.59155f, 488.24103f, 489.8905f, 491.53995f, 493.18942f, 494.83887f, 496.48834f, 498.13782f, 499.78726f, 501.43674f, 503.08618f, 504.73566f, 506.38513f, 508.03458f, 509.68405f, 511.3335f, 512.983f, 514.63245f, 516.2819f, 517.93134f, 519.5808f, 521.2303f, 522.87976f, 524.52924f, 526.17865f, 527.8281f, 529.4776f, 531.1271f, 532.77655f, 534.42596f, 536.07544f, 537.7249f, 539.3744f, 541.02386f, 542.6733f, 544.32275f, 545.9722f, 547.6217f, 549.2712f, 550.9206f, 552.57007f, 554.21954f, 555.869f, 557.5185f, 559.1679f, 560.8174f, 562.46686f, 564.11633f, 565.7658f, 567.4152f, 569.0647f, 570.7142f, 572.36365f, 574.0131f, 575.66254f, 577.312f, 578.9615f, 580.61096f, 582.26044f, 583.90985f, 585.5593f, 587.2088f, 588.8583f, 590.50775f, 592.15717f, 593.80664f, 595.4561f, 597.1056f, 598.75507f, 600.4045f, 602.05396f, 603.7034f, 605.3529f, 607.0024f, 608.6518f, 610.3013f, 611.95074f, 613.6002f, 615.2497f, 616.8991f, 618.5486f, 620.19806f, 621.84753f, 623.497f, 625.1464f, 626.7959f, 628.4454f, 630.09485f, 631.7443f, 633.3938f, 635.0432f, 636.6927f, 638.34216f, 639.99164f, 641.6411f, 643.2905f, 644.94f, 646.5895f, 648.23895f, 649.8884f, 651.53784f, 653.1873f, 654.8368f, 656.48627f, 658.13574f, 659.78516f, 661.43463f, 663.0841f, 664.7336f, 666.38306f, 668.0325f, 669.68195f, 671.3314f, 672.9809f, 674.6304f, 676.2798f, 677.92926f, 679.57874f, 681.2282f, 682.8777f, 684.5271f, 686.1766f, 687.82605f, 689.4755f, 691.125f, 692.7744f, 694.4239f, 696.07336f, 697.72284f, 699.3723f, 701.0217f, 702.6712f, 704.3207f, 705.97015f, 707.6196f, 709.26904f, 710.9185f, 712.568f, 714.21747f, 715.86694f, 717.51636f, 719.16583f, 720.8153f, 722.4648f, 724.11426f, 725.7637f, 727.41315f, 729.0626f, 730.7121f, 732.3616f, 734.011f, 735.66046f, 737.30994f, 738.9594f, 740.6089f, 742.2583f, 743.9078f, 745.55725f, 747.2067f, 748.8562f, 750.5056f, 752.1551f, 753.80457f, 755.45404f, 757.1035f, 758.7529f, 760.4024f, 762.0519f, 763.70135f, 765.3508f, 767.00024f, 768.6497f, 770.2992f, 771.94867f, 773.59814f, 775.24756f, 776.89703f, 778.5465f, 780.196f, 781.84546f, 783.4949f, 785.14435f, 786.7938f, 788.4433f, 790.0928f, 791.7422f, 793.39166f, 795.04114f, 796.6906f, 798.3401f, 799.9895f, 801.639f, 803.28845f, 804.9379f, 806.5874f, 808.2368f, 809.8863f, 811.53577f, 813.18524f, 814.8347f, 816.48413f, 818.1336f, 819.7831f, 821.43256f, 823.08203f, 824.73145f, 826.3809f, 828.0304f, 829.6799f, 831.32935f, 832.97876f, 834.62823f, 836.2777f, 837.9272f, 839.57666f, 841.2261f, 842.87555f, 844.525f, 846.1745f, 847.824f, 849.4734f, 851.12286f, 852.77234f, 854.4218f, 856.0713f, 857.7207f, 859.3702f, 861.01965f, 862.6691f, 864.3186f, 865.968f, 867.6175f, 869.26697f, 870.91644f, 872.5659f, 874.21533f, 875.8648f, 877.5143f, 879.16376f, 880.81323f, 882.46265f, 884.1121f, 885.7616f, 887.4111f, 889.06055f, 890.70996f, 892.35944f, 894.0089f, 895.6584f, 897.30786f, 898.9573f, 900.60675f, 902.2562f, 903.9057f, 905.5552f, 907.2046f, 908.85406f, 910.50354f, 912.153f, 913.8025f, 915.4519f, 917.1014f, 918.75085f, 920.4003f, 922.0498f, 923.6992f, 925.3487f, 926.99817f, 928.64764f, 930.2971f, 931.94653f, 933.596f, 935.2455f, 936.89496f, 938.54443f, 940.19385f, 941.8433f, 943.4928f, 945.1423f, 946.79175f, 948.44116f, 950.09064f, 951.7401f, 953.3896f, 955.03906f, 956.6885f, 958.33795f, 959.9874f, 961.6369f, 963.2864f, 964.9358f, 966.58527f, 968.23474f, 969.8842f, 971.5337f, 973.1831f, 974.8326f, 976.48206f, 978.13153f, 979.781f, 981.4304f, 983.0799f, 984.7294f, 986.37885f, 988.0283f, 989.67773f, 991.3272f, 992.9767f, 994.62616f, 996.27563f, 997.92505f, 999.5745f, 1001.224f, 1002.8735f, 1004.52295f, 1006.17236f, 1007.82184f, 1009.4713f, 1011.1208f, 1012.77026f, 1014.4197f, 1016.06915f, 1017.7186f, 1019.3681f, 1021.0176f, 1022.667f, 1024.3165f, 1025.966f, 1027.6154f, 1029.2649f, 1030.9143f, 1032.5638f, 1034.2133f, 1035.8627f, 1037.5122f, 1039.1616f, 1040.8112f, 1042.4606f, 1044.11f, 1045.7595f, 1047.4089f, 1049.0585f, 1050.7079f, 1052.3573f, 1054.0068f, 1055.6562f, 1057.3058f, 1058.9552f, 1060.6046f, 1062.2542f, 1063.9036f, 1065.5531f, 1067.2025f, 1068.8519f, 1070.5015f, 1072.1509f, 1073.8004f, 1075.4498f, 1077.0992f, 1078.7488f, 1080.3982f, 1082.0477f, 1083.6971f, 1085.3466f, 1086.9961f, 1088.6455f, 1090.295f, 1091.9445f, 1093.5939f, 1095.2434f, 1096.8928f, 1098.5424f, 1100.1918f, 1101.8412f, 1103.4907f, 1105.1401f, 1106.7897f, 1108.4391f, 1110.0885f, 1111.738f, 1113.3875f, 1115.037f, 1116.6864f, 1118.3358f, 1119.9854f, 1121.6348f, 1123.2843f, 1124.9337f, 1126.5831f, 1128.2327f, 1129.8821f, 1131.5316f, 1133.181f, 1134.8304f, 1136.48f, 1138.1294f, 1139.7789f, 1141.4283f, 1143.0778f, 1144.7273f, 1146.3767f, 1148.0262f, 1149.6757f, 1151.3251f, 1152.9746f, 1154.624f, 1156.2736f, 1157.923f, 1159.5724f, 1161.2219f, 1162.8713f, 1164.5209f, 1166.1703f, 1167.8197f, 1169.4692f, 1171.1187f, 1172.7682f, 1174.4176f, 1176.067f, 1177.7166f, 1179.366f, 1181.0155f, 1182.6649f, 1184.3143f, 1185.9639f, 1187.6133f, 1189.2628f, 1190.9122f, 1192.5616f, 1194.2112f, 1195.8606f, 1197.5101f, 1199.1595f, 1200.809f, 1202.4585f, 1204.1079f, 1205.7574f, 1207.4069f, 1209.0563f, 1210.7058f, 1212.3552f, 1214.0048f, 1215.6542f, 1217.3036f, 1218.9531f, 1220.6025f, 1222.2521f, 1223.9015f, 1225.5509f, 1227.2004f, 1228.8499f, 1230.4994f, 1232.1488f, 1233.7982f, 1235.4478f, 1237.0972f, 1238.7467f, 1240.3961f, 1242.0455f, 1243.6951f, 1245.3445f, 1246.994f, 1248.6434f, 1250.2928f, 1251.9424f, 1253.5918f, 1255.2413f, 1256.8907f, 1258.5402f, 1260.1897f, 1261.8391f, 1263.4886f, 1265.1381f, 1266.7876f, 1268.437f, 1270.0864f, 1271.736f, 1273.3854f, 1275.0349f, 1276.6843f, 1278.3337f, 1279.9833f, 1281.6327f, 1283.2822f, 1284.9316f, 1286.581f, 1288.2306f, 1289.88f, 1291.5295f, 1293.179f, 1294.8284f, 1296.4779f, 1298.1273f, 1299.7769f, 1301.4263f, 1303.0757f, 1304.7252f, 1306.3746f, 1308.0242f, 1309.6736f, 1311.323f, 1312.9725f, 1314.622f, 1316.2715f, 1317.9209f, 1319.5703f, 1321.2198f, 1322.8693f, 1324.5188f, 1326.1682f, 1327.8176f, 1329.4672f, 1331.1166f, 1332.7661f, 1334.4155f, 1336.065f, 1337.7145f, 1339.3639f, 1341.0134f, 1342.6628f, 1344.3123f, 1345.9618f, 1347.6112f, 1349.2607f, 1350.9102f, 1352.5596f, 1354.2091f, 1355.8585f, 1357.508f, 1359.1575f, 1360.8069f, 1362.4564f, 1364.1058f, 1365.7554f, 1367.4048f, 1369.0542f, 1370.7037f, 1372.3531f, 1374.0027f, 1375.6521f, 1377.3015f, 1378.951f, 1380.6005f, 1382.25f, 1383.8994f, 1385.5488f, 1387.1984f, 1388.8478f, 1390.4973f, 1392.1467f, 1393.7961f, 1395.4457f, 1397.0951f, 1398.7446f, 1400.394f, 1402.0435f, 1403.693f, 1405.3424f, 1406.992f, 1408.6414f, 1410.2908f, 1411.9403f, 1413.5897f, 1415.2393f, 1416.8887f, 1418.5381f, 1420.1876f, 1421.837f, 1423.4866f, 1425.136f, 1426.7854f, 1428.4349f, 1430.0844f, 1431.7339f, 1433.3833f, 1435.0327f, 1436.6823f, 1438.3317f, 1439.9812f, 1441.6306f, 1443.28f, 1444.9296f, 1446.579f, 1448.2285f, 1449.8779f, 1451.5273f, 1453.1769f, 1454.8263f, 1456.4758f, 1458.1252f, 1459.7747f, 1461.4242f, 1463.0736f, 1464.7231f, 1466.3726f, 1468.022f, 1469.6715f, 1471.3209f, 1472.9705f, 1474.6199f, 1476.2693f, 1477.9188f, 1479.5682f, 1481.2178f, 1482.8672f, 1484.5166f, 1486.1661f, 1487.8156f, 1489.4651f, 1491.1145f, 1492.7639f, 1494.4135f, 1496.0629f, 1497.7124f, 1499.3618f, 1501.0112f, 1502.6608f, 1504.3102f, 1505.9597f, 1507.6091f, 1509.2585f, 1510.9081f, 1512.5575f, 1514.207f, 1515.8564f, 1517.5059f, 1519.1554f, 1520.8048f, 1522.4543f, 1524.1038f, 1525.7532f, 1527.4027f, 1529.0521f, 1530.7017f, 1532.3511f, 1534.0005f, 1535.65f, 1537.2994f, 1538.949f, 1540.5984f, 1542.2478f, 1543.8973f, 1545.5468f, 1547.1963f, 1548.8457f, 1550.4951f, 1552.1447f, 1553.7941f, 1555.4436f, 1557.093f, 1558.7424f, 1560.392f, 1562.0414f, 1563.6909f, 1565.3403f, 1566.9897f, 1568.6393f, 1570.2887f, 1571.9382f, 1573.5876f, 1575.237f, 1576.8866f, 1578.536f, 1580.1855f, 1581.835f, 1583.4844f, 1585.1339f, 1586.7833f, 1588.4329f, 1590.0823f, 1591.7317f, 1593.3812f, 1595.0306f, 1596.6802f, 1598.3296f, 1599.979f, 1601.6285f, 1603.278f, 1604.9275f, 1606.5769f, 1608.2263f, 1609.8759f, 1611.5253f, 1613.1748f, 1614.8242f, 1616.4736f, 1618.1232f, 1619.7726f, 1621.4221f, 1623.0715f, 1624.721f, 1626.3705f, 1628.0199f, 1629.6694f, 1631.3188f, 1632.9683f, 1634.6178f, 1636.2672f, 1637.9167f, 1639.5662f, 1641.2156f, 1642.8651f, 1644.5145f, 1646.1641f, 1647.8135f, 1649.4629f, 1651.1124f, 1652.7618f, 1654.4114f, 1656.0608f, 1657.7102f, 1659.3597f, 1661.0092f, 1662.6587f, 1664.3081f, 1665.9575f, 1667.607f, 1669.2565f, 1670.906f, 1672.5554f, 1674.2048f, 1675.8544f, 1677.5038f, 1679.1533f, 1680.8027f, 1682.4521f, 1684.1017f, 1685.7511f, 1687.4006f, 1689.05f};
////            testData2.dFreq = new float[]{};
//                //          testData3.dFreq = new float[]{};
//                testData1.dFreq = new float[] {0.002352875f, 0.17392406f, 0.056772802f, 0.038906954f, 0.02554982f, 0.02465599f, 0.019499654f, 0.015789712f, 0.01798137f, 0.013139083f, 0.01744151f, 0.8331911f, 1.6940593f, 0.86715055f, 0.019657876f, 0.006621742f, 0.00496805f, 0.006657499f, 0.006805731f, 0.007507796f, 0.006039778f, 0.003882728f, 0.00725534f, 0.08566503f, 0.20824848f, 0.12435449f, 0.013415371f, 0.005842275f, 0.003999491f, 0.003761599f, 0.002691248f, 0.005744129f, 0.00564411f, 0.006955612f, 0.009640003f, 0.067828394f, 0.18530558f, 0.11938139f, 0.031928565f, 0.029515876f, 0.013461159f, 0.00684432f, 0.006128904f, 0.012896769f, 0.012712862f, 0.007690065f, 0.009360479f, 0.058576364f, 0.1801535f, 0.13105811f, 0.03415562f, 0.022913257f, 0.013901375f, 0.021440335f, 0.013190506f, 0.013236349f, 0.012907221f, 0.006171377f, 0.003938401f, 0.019897398f, 0.06718949f, 0.055856533f, 0.015896397f, 0.007257002f, 0.003803261f, 0.005361924f, 0.003456182f, 0.002475974f, 0.002414632f, 0.001827909f, 0.001531091f, 0.002338757f, 0.007242581f, 0.008830944f, 0.007934677f, 0.002569057f, 0.001983328f, 0.00158329f, 0.001282904f, 0.001479515f, 0.00186066f, 0.001807898f, 0.001113832f, 0.001500107f, 0.002805864f, 0.003158269f, 0.001873631f, 0.00204814f, 0.001862373f, 0.001422688f, 0.00153102f, 0.001712682f, 0.002371447f, 0.001864986f, 0.001346601f, 0.00161704f, 0.002354945f, 0.00313092f, 0.002844788f, 0.001439072f, 0.00149931f, 0.002241504f, 0.002122868f, 0.002012123f, 0.002038971f, 0.002343752f, 0.002219685f, 0.002667409f, 0.004773263f, 0.009339777f, 0.010087984f, 0.005312859f, 0.002585283f, 0.002679217f, 0.003498065f, 0.003144029f, 0.002474015f, 0.00255686f, 0.002872725f, 0.002898922f, 0.005408285f, 0.022012671f, 0.02610157f, 0.012328049f, 0.007775403f, 0.006955759f, 0.007362327f, 0.00589407f, 0.009953146f, 0.011819654f, 0.009809156f, 0.009424898f, 0.011851555f, 0.022416046f, 0.022447918f, 0.012650079f, 0.014392274f, 0.018242707f, 0.019852519f, 0.015436172f, 0.010288938f, 0.012811865f, 0.010506642f, 0.006978403f, 0.008349934f, 0.026327321f, 0.03610115f, 0.016664714f, 0.009681768f, 0.010696091f, 0.012689143f, 0.012707289f, 0.00998347f, 0.011897906f, 0.01310564f, 0.012449855f, 0.010579009f, 0.024414895f, 0.037055474f, 0.028203757f, 0.01699492f, 0.015929354f, 0.013016664f, 0.012288164f, 0.014878093f, 0.014012525f, 0.012382926f, 0.013039988f, 0.011826747f, 0.018798904f, 0.028878339f, 0.023327522f, 0.017816614f, 0.013069293f, 0.01268298f, 0.007974936f, 0.009657098f, 0.010206861f, 0.010575183f, 0.012192179f, 0.012200679f, 0.023092795f, 0.042095568f, 0.032457493f, 0.019131662f, 0.010958207f, 0.007259667f, 0.005081322f, 0.006314379f, 0.005811188f, 0.007577282f, 0.007210991f, 0.006524868f, 0.006916152f, 0.009850766f, 0.011377064f, 0.007168345f, 0.006180026f, 0.006184164f, 0.006032097f, 0.004999199f, 0.007365559f, 0.006928503f, 0.005951845f, 0.008253465f, 0.011568977f, 0.022119472f, 0.02151143f, 0.013728172f, 0.011400786f, 0.008635538f, 0.007649388f, 0.009499593f, 0.008915743f, 0.008238656f, 0.007613342f, 0.00853797f, 0.011239056f, 0.015469533f, 0.011878969f, 0.006825539f, 0.006927755f, 0.007193148f, 0.007140738f, 0.005674497f, 0.004777091f, 0.006224803f, 0.00703078f, 0.007584919f, 0.006568417f, 0.008355086f, 0.00806194f, 0.007997002f, 0.008989586f, 0.007661955f, 0.006936116f, 0.007374359f, 0.00762008f, 0.007844741f, 0.011006556f, 0.011114121f, 0.010117064f, 0.011552237f, 0.010872217f, 0.008526491f, 0.011002386f, 0.009846796f, 0.010334111f, 0.009893817f, 0.007582249f, 0.009207061f, 0.009715351f, 0.014759429f, 0.012122848f, 0.018439366f, 0.019684657f, 0.017248452f, 0.017100349f, 0.015645234f, 0.01656882f, 0.017214224f, 0.013115668f, 0.014264287f, 0.014405501f, 0.018411092f, 0.019446248f, 0.015613445f, 0.01705956f, 0.024244009f, 0.026072128f, 0.019985305f, 0.014684466f, 0.018847043f, 0.02076368f, 0.021502905f, 0.016867727f, 0.014484319f, 0.014405094f, 0.013705052f, 0.009364863f, 0.007823864f, 0.011167575f, 0.009735578f, 0.007348758f, 0.006870633f, 0.007000903f, 0.007819669f, 0.007362677f, 0.005714286f, 0.008306129f, 0.008964052f, 0.009933064f, 0.011007502f, 0.012410922f, 0.008151218f, 0.008206905f, 0.009438852f, 0.011880454f, 0.009974561f, 0.005309421f, 0.006167268f, 0.007007422f, 0.00657684f, 0.00989263f, 0.010716549f, 0.006623969f, 0.004763788f, 0.002680919f, 0.003947675f, 0.004155722f, 0.004608769f, 0.004127878f, 0.004551992f, 0.004959853f, 0.006171641f, 0.013391901f, 0.016434588f, 0.01010167f, 0.009369021f, 0.010001784f, 0.007258018f, 0.006018336f, 0.006097271f, 0.006764789f, 0.007006552f, 0.006688689f, 0.007846606f, 0.009915856f, 0.010405785f, 0.006978354f, 0.005628798f, 0.005056845f, 0.004567787f, 0.006181624f, 0.00711242f, 0.006700257f, 0.007858874f, 0.008730287f, 0.009024377f, 0.01802605f, 0.02361851f, 0.018786129f, 0.010121361f, 0.007264919f, 0.0084435f, 0.00740917f, 0.007509788f, 0.007201495f, 0.00931944f, 0.009220007f, 0.007663757f, 0.009519144f, 0.016311029f, 0.017191552f, 0.009168311f, 0.007794071f, 0.007091126f, 0.005680729f, 0.005053945f, 0.006688556f, 0.006329584f, 0.005467494f, 0.006692035f, 0.007401538f, 0.010075531f, 0.010970352f, 0.008845273f, 0.007784323f, 0.006648336f, 0.006545667f, 0.006530069f, 0.006552551f, 0.006420468f, 0.006601438f, 0.007515941f, 0.00789046f, 0.012394044f, 0.01370288f, 0.012093606f, 0.011193373f, 0.008636602f, 0.008701596f, 0.008305109f, 0.007665399f, 0.008108757f, 0.008103475f, 0.008358182f, 0.011665211f, 0.011457363f, 0.010470009f, 0.008635438f, 0.009237307f, 0.007370081f, 0.007783473f, 0.006958226f, 0.006565179f, 0.006843217f, 0.006832315f, 0.007826992f, 0.005883348f, 0.006754258f, 0.007073307f, 0.007115599f, 0.006795332f, 0.005108963f, 0.005720477f, 0.008024867f, 0.008069524f, 0.006770057f, 0.005964703f, 0.008684581f, 0.008367121f, 0.008456064f, 0.013313401f, 0.015473225f, 0.019838968f, 0.017417211f, 0.014148278f, 0.015810817f, 0.017438656f, 0.011212794f, 0.020196479f, 0.034787707f, 0.041365623f, 0.03902496f, 0.034626827f, 0.04140154f, 0.05298933f, 0.056176588f, 0.04464259f, 0.051126953f, 0.05560557f, 0.05581751f, 0.061727528f, 0.094198965f, 0.09128525f, 0.08759078f, 0.094397165f, 0.082784556f, 0.070794076f, 0.078944534f, 0.07762801f, 0.0637616f, 0.049786925f, 0.03351313f, 0.060549308f, 0.072091274f, 0.049820915f, 0.035226054f, 0.030608678f, 0.048162613f, 0.061060105f, 0.048519675f, 0.04346899f, 0.04888079f, 0.04513429f, 0.037805915f, 0.041876014f, 0.040373556f, 0.031864356f, 0.029877773f, 0.036828414f, 0.043205015f, 0.04522918f, 0.040579602f, 0.028065387f, 0.030403629f, 0.028701413f, 0.036688805f, 0.03767396f, 0.04881544f, 0.04597323f, 0.035685427f, 0.04267066f, 0.03839526f, 0.033937626f, 0.02695858f, 0.025353502f, 0.029988972f, 0.02807536f, 0.024400257f, 0.025726954f, 0.027286569f, 0.032781392f, 0.029365543f, 0.033069536f, 0.031309616f, 0.030624088f, 0.02844637f, 0.020845793f, 0.024520775f, 0.022014793f, 0.017783593f, 0.01589112f, 0.022086492f, 0.01998619f, 0.017490556f, 0.013115249f, 0.012574809f, 0.010953848f, 0.011082267f, 0.014514598f, 0.010657059f, 0.01025458f, 0.00913839f, 0.011505366f, 0.013280942f, 0.010611065f, 0.007678622f, 0.009032896f, 0.011697031f, 0.011441552f, 0.009887121f, 0.008316503f, 0.011087516f, 0.013322854f, 0.013011806f, 0.008876251f, 0.009685622f, 0.008644987f, 0.008636064f, 0.012930672f, 0.014871642f, 0.014907549f, 0.014246044f, 0.014173442f, 0.012146135f, 0.013879485f, 0.012506855f, 0.012616901f, 0.019780701f, 0.017144797f, 0.021456933f, 0.022078605f, 0.015301096f, 0.022679688f, 0.020190407f, 0.024802541f, 0.021607175f, 0.024070617f, 0.02772924f, 0.02099305f, 0.024451975f, 0.02208883f, 0.028382866f, 0.030610925f, 0.022277033f, 0.02319192f, 0.016538516f, 0.017409593f, 0.019592451f, 0.016098535f, 0.016659103f, 0.012112351f, 0.008647012f, 0.01107058f, 0.008655481f, 0.011105062f, 0.008887306f, 0.007745082f, 0.006845911f, 0.00463349f, 0.004746807f, 0.006513352f, 0.006512781f, 0.005617516f, 0.004582172f, 0.005259312f, 0.005008984f, 0.005140945f, 0.006256531f, 0.005946416f, 0.004700895f, 0.003800897f, 0.003669117f, 0.004123178f, 0.003763903f, 0.003844903f, 0.003334261f, 0.004990666f, 0.004553844f, 0.003694736f, 0.00518909f, 0.00454661f, 0.003136176f, 0.002564182f, 0.002682853f, 0.002938295f, 0.002684941f, 0.002308485f, 0.003248416f, 0.003264328f, 0.003038331f, 0.003798821f, 0.004346031f, 0.003678205f, 0.004820689f, 0.005082952f, 0.006381451f, 0.007088109f, 0.00607523f, 0.006263761f, 0.007461265f, 0.006292301f, 0.007048119f, 0.006624585f, 0.008150622f, 0.009892037f, 0.010918266f, 0.011297271f, 0.009989481f, 0.007312886f, 0.011489194f, 0.010634602f, 0.007989111f, 0.009353643f, 0.010266541f, 0.010862168f, 0.012406202f, 0.010794087f, 0.011386423f, 0.008797897f, 0.007789336f, 0.008614531f, 0.008576175f, 0.00998966f, 0.007793165f, 0.008767082f, 0.005850388f, 0.006376988f, 0.006053975f, 0.005748408f, 0.006237035f, 0.004650753f, 0.004960409f, 0.004571111f, 0.004704535f, 0.004333498f, 0.002757898f, 0.001518726f, 0.001449835f, 0.00145311f, 0.001713983f, 0.001700186f, 0.001664241f, 0.00178002f, 0.001078787f, 0.001612956f, 0.001556005f, 0.001244985f, 0.001419114f, 0.001441544f, 0.001154113f, 0.001252188f, 0.001667138f, 0.002333915f, 0.001827064f, 0.00205241f, 0.002022466f, 0.001795134f, 0.002276028f, 0.002721084f, 0.001955187f, 0.001657691f, 0.001799332f, 0.001705184f, 0.001564216f, 0.001340882f, 0.001309832f, 0.001726277f, 0.001784689f, 0.001597002f, 0.001560939f, 0.001498138f, 0.001778723f, 0.001747714f, 0.001302193f, 0.001411087f, 0.001491935f, 0.001641486f, 0.002680977f, 0.002828229f, 0.002358541f, 0.002370849f, 0.002240581f, 0.003581686f, 0.00329059f, 0.003001835f, 0.003319006f, 0.003672659f, 0.003061445f, 0.003472908f, 0.003107442f, 0.002851286f, 0.004459553f, 0.004145018f, 0.003793657f, 0.00437647f, 0.004165652f, 0.004827493f, 0.003713735f, 0.004341733f, 0.004564002f, 0.006477087f, 0.006218393f, 0.0048984f, 0.004949323f, 0.00593084f, 0.007128664f, 0.008061335f, 0.006401655f, 0.007131972f, 0.006282989f, 0.006865172f, 0.007129618f, 0.004963271f, 0.005871155f, 0.006101331f, 0.006471154f, 0.005489173f, 0.005036685f, 0.009017382f, 0.009223593f, 0.007791338f, 0.006406753f, 0.004969276f, 0.00726302f, 0.007157898f, 0.007080755f, 0.007979861f, 0.009452985f, 0.008614939f, 0.011302142f, 0.01127706f, 0.011570155f, 0.01385883f, 0.017575407f, 0.020443667f, 0.018215485f, 0.014384871f, 0.020059459f, 0.019958215f, 0.019113777f, 0.018712169f, 0.017330607f, 0.018010147f, 0.01711668f, 0.017336583f, 0.013079565f, 0.011391079f, 0.017133871f, 0.014790674f, 0.015407443f, 0.011935414f, 0.008828901f, 0.012764458f, 0.012919587f, 0.012188365f, 0.011927964f, 0.012516997f, 0.013648554f, 0.013034855f, 0.011144773f, 0.010392262f, 0.009881699f, 0.010505907f, 0.009706666f, 0.008685858f, 0.008399777f, 0.008936191f, 0.009992292f, 0.008166178f, 0.007457639f, 0.005625518f, 0.007385891f, 0.006172917f, 0.005638564f, 0.006454359f, 0.005491018f, 0.005577671f, 0.005939981f, 0.005292276f, 0.004788847f, 0.005822138f, 0.006513183f, 0.006819567f, 0.006414862f, 0.00644439f, 0.005251908f, 0.005550671f, 0.006173568f, 0.005100494f, 0.005028968f, 0.00474349f, 0.004552972f, 0.004254628f, 0.003673034f, 0.00256374f, 0.003230402f, 0.00314338f, 0.002483396f, 0.002097628f, 0.003519584f, 0.003878436f, 0.003224196f, 0.003223681f, 0.00237092f, 0.002221699f, 0.00328041f, 0.003844985f, 0.003259752f, 0.003416617f, 0.003299634f, 0.00313027f, 0.003945038f, 0.00317386f, 0.002976726f, 0.003352892f, 0.003363663f, 0.003299651f, 0.003138206f, 0.002759509f, 0.00202412f, 0.001541745f, 0.001575266f, 0.001761943f, 0.002063158f, 0.001730877f, 0.002547237f, 0.002166617f, 0.001641803f, 0.002410736f, 0.002810197f, 0.002469529f, 0.001866816f, 0.002143178f, 0.002343387f, 0.002259297f, 0.001566353f, 0.001356493f, 0.002144289f, 0.002945008f, 0.002120238f, 0.00151928f, 0.001541674f, 0.001640942f, 0.001828055f, 0.002242721f, 0.002081343f, 0.001577344f, 0.001568595f, 0.002272825f, 0.002930113f, 0.002466378f, 0.002475999f, 0.002515673f, 0.002511309f, 0.002171671f, 0.001789594f, 0.002585795f, 0.002630895f, 0.002573689f, 0.002187777f, 0.002354257f, 0.003118328f, 0.003042874f, 0.002891091f, 0.002757f, 0.002496031f, 0.00233912f, 0.001895476f, 0.00299055f, 0.00283367f, 0.00207658f, 0.00150418f, 0.002002047f, 0.002692735f, 0.002072748f, 0.002091709f, 0.002988731f, 0.00376663f, 0.003444771f, 0.002619363f, 0.004511825f, 0.004089345f, 0.003316758f, 0.002922402f, 0.003154437f, 0.004848775f, 0.004421092f, 0.004543236f, 0.004044508f, 0.004709614f, 0.00444809f, 0.003471007f, 0.005587307f, 0.004859509f, 0.004784612f, 0.00374732f, 0.003779577f, 0.004481546f, 0.00403884f, 0.004882728f, 0.00383701f, 0.003477503f, 0.003719427f, 0.002682841f, 0.002656651f, 0.002421016f, 0.002780878f, 0.002584994f, 0.002022987f, 0.002624447f, 0.003018513f, 0.003142728f, 0.003157926f, 0.003491795f, 0.002508663f, 0.001989286f, 0.002911798f, 0.002270781f, 0.00146682f, 0.001614471f, 0.001501937f, 0.002030511f, 0.002504497f, 0.002308773f, 0.003177954f, 0.002735988f, 0.002146881f, 0.001793764f, 0.002082069f, 0.002283823f, 0.002365106f, 0.002564144f, 0.002466899f, 0.002828222f, 0.003038555f, 0.003310807f, 0.003619962f, 0.002654514f, 0.002520339f, 0.002422897f, 0.002784713f, 0.003144534f, 0.003040179f, 0.003127291f, 0.003417346f, 0.004233551f, 0.003150825f, 0.002578801f, 0.00235362f, 0.002671577f, 0.003402354f, 0.003059879f, 0.00304802f, 0.002808622f, 0.002760032f, 0.002256942f, 0.001934494f, 0.002366734f, 0.00215792f, 0.002083182f, 0.002459609f, 0.002406709f, 0.002082164f, 0.002216834f, 0.002137178f, 0.001292917f, 0.001183465f, 0.001345627f, 0.000907f, 0.000958f, 0.001338602f, 0.001283154f, 0.00121808f, 0.001354935f, 0.001431116f, 0.001567928f, 0.001283604f, 0.001182724f, 0.001015752f, 0.00116227f, 0.001024098f, 0.001180465f, 0.001282854f, 0.001345438f, 0.001186331f, 0.001263742f, 0.001197491f, 0.0011329f, 0.001287062f, 0.001247912f, 0.001344212f, 0.001221312f, 0.001308215f, 0.001682544f, 0.001424684f, 0.001915954f, 0.001984675f, 0.001963332f, 0.00175404f, 0.001707177f, 0.001727514f, 0.001838059f, 0.00212345f, 0.001902902f, 0.001898419f, 0.00218117f, 0.002400706f, 0.001865833f, 0.001887776f, 0.001804582f, 0.001801437f, 0.00217554f, 0.002135991f, 0.001948492f, 0.002255871f, 0.001825003f, 0.001571575f, 0.0015219f, 0.001352544f, 0.001070607f, 0.001080622f};
//                testData2.dFreq = new float[] {0.002201803f, 0.12730117f, 0.044110592f, 0.034444213f, 0.021044578f, 0.021183593f, 0.025962632f, 0.015223781f, 0.013565576f, 0.014291794f, 0.022391815f, 0.48862183f, 1.0973775f, 0.6146185f, 0.027973684f, 0.006893443f, 0.006399355f, 0.006578447f, 0.00544852f, 0.005740256f, 0.005700375f, 0.007131796f, 0.013790272f, 0.16583057f, 0.4879404f, 0.34334412f, 0.048086785f, 0.010686033f, 0.007946972f, 0.010975087f, 0.007898336f, 0.010907118f, 0.011911221f, 0.006976829f, 0.008292528f, 0.023218846f, 0.086951494f, 0.07260388f, 0.011110401f, 0.008503649f, 0.004249589f, 0.00774869f, 0.006908462f, 0.011548687f, 0.010453346f, 0.005030336f, 0.008306059f, 0.012558615f, 0.044740725f, 0.04496762f, 0.011784949f, 0.012014638f, 0.007256426f, 0.003080281f, 0.002003229f, 0.001879377f, 0.002343753f, 0.001945214f, 0.001912229f, 0.002225726f, 0.00694395f, 0.00808113f, 0.003879793f, 0.002761769f, 0.002048538f, 0.001502837f, 0.001391231f, 0.001609995f, 0.001811666f, 0.001837724f, 0.001866708f, 0.001988659f, 0.003110508f, 0.006876255f, 0.015882293f, 0.009739561f, 0.002834088f, 0.001914819f, 0.001698542f, 0.001948945f, 0.001469674f, 0.000935f, 0.001170424f, 0.001184024f, 0.002109082f, 0.004355224f, 0.004398329f, 0.002206593f, 0.001645419f, 0.001698099f, 0.001732263f, 0.002702954f, 0.003074631f, 0.002239098f, 0.002206523f, 0.00186551f, 0.002590559f, 0.006834178f, 0.010564888f, 0.005850658f, 0.004029483f, 0.005947596f, 0.005782897f, 0.006242164f, 0.008551344f, 0.006496788f, 0.004788844f, 0.004789935f, 0.006138657f, 0.013965726f, 0.022899829f, 0.014127565f, 0.003639317f, 0.002771733f, 0.002391814f, 0.003325999f, 0.004953818f, 0.004315148f, 0.003202418f, 0.003140536f, 0.003529573f, 0.005305318f, 0.011514075f, 0.009158144f, 0.004272956f, 0.004034881f, 0.005097836f, 0.006322342f, 0.007631457f, 0.009082785f, 0.008580328f, 0.005751964f, 0.005977887f, 0.012836675f, 0.039801236f, 0.035596527f, 0.010356125f, 0.00500654f, 0.006140125f, 0.00585972f, 0.005979048f, 0.004631548f, 0.003819251f, 0.003372295f, 0.003033497f, 0.00665695f, 0.02313444f, 0.023957325f, 0.007598298f, 0.003569562f, 0.003939961f, 0.002721018f, 0.003878574f, 0.003127128f, 0.00290997f, 0.002783639f, 0.002288454f, 0.00416819f, 0.014945577f, 0.018353757f, 0.00703711f, 0.001871772f, 0.003950387f, 0.003512444f, 0.004105364f, 0.003680913f, 0.004754499f, 0.004712546f, 0.003646396f, 0.00347248f, 0.009792376f, 0.013401816f, 0.006631479f, 0.003986847f, 0.002721792f, 0.002767771f, 0.003733907f, 0.003624465f, 0.003181296f, 0.003789784f, 0.004060443f, 0.005475688f, 0.015281254f, 0.026248846f, 0.01801483f, 0.007923064f, 0.005681441f, 0.005464874f, 0.005348648f, 0.0052558f, 0.004333213f, 0.003025231f, 0.003126341f, 0.002942709f, 0.003965843f, 0.005758266f, 0.006500712f, 0.003646599f, 0.002112159f, 0.002067711f, 0.002792925f, 0.002663024f, 0.001991242f, 0.002576065f, 0.002740311f, 0.003390739f, 0.00320691f, 0.004075373f, 0.005098255f, 0.002862089f, 0.001424986f, 0.001991591f, 0.002815206f, 0.002661989f, 0.002587999f, 0.00262648f, 0.00232085f, 0.002424682f, 0.003098675f, 0.005447456f, 0.008066284f, 0.004920688f, 0.002355858f, 0.002639601f, 0.002262051f, 0.002392473f, 0.002221308f, 0.003434255f, 0.00360257f, 0.002886279f, 0.00209472f, 0.003055435f, 0.008064378f, 0.007329973f, 0.003530859f, 0.00340944f, 0.003586297f, 0.003837374f, 0.003205183f, 0.004154974f, 0.004486757f, 0.004432872f, 0.004936433f, 0.004018963f, 0.008363648f, 0.008359043f, 0.005627514f, 0.005253899f, 0.006962563f, 0.008263364f, 0.006345353f, 0.00527513f, 0.006309703f, 0.006554611f, 0.007256907f, 0.008764745f, 0.013439668f, 0.013252053f, 0.006857829f, 0.003328749f, 0.002604648f, 0.003326884f, 0.003456364f, 0.00384034f, 0.003791399f, 0.004300947f, 0.002675089f, 0.003400708f, 0.005646185f, 0.006070034f, 0.004841459f, 0.004850271f, 0.003799359f, 0.004023421f, 0.004513068f, 0.004070459f, 0.004271904f, 0.002991135f, 0.003667106f, 0.005244794f, 0.006356067f, 0.005833165f, 0.004129081f, 0.004872425f, 0.004129888f, 0.005261121f, 0.006694926f, 0.006751259f, 0.005486195f, 0.006372112f, 0.008154569f, 0.007177835f, 0.008826245f, 0.011031114f, 0.009665535f, 0.008886475f, 0.008849734f, 0.011872869f, 0.009131582f, 0.011076991f, 0.008900236f, 0.008403557f, 0.009609051f, 0.006003929f, 0.005439981f, 0.009662634f, 0.006624419f, 0.004367647f, 0.002986124f, 0.002635538f, 0.002460304f, 0.00305911f, 0.002779832f, 0.002060832f, 0.003143956f, 0.002833593f, 0.00313643f, 0.005286936f, 0.005692377f, 0.005443515f, 0.004568181f, 0.004189906f, 0.004408669f, 0.004055794f, 0.004150124f, 0.00284845f, 0.002635581f, 0.003857265f, 0.003477572f, 0.002961273f, 0.003702085f, 0.003745421f, 0.003006905f, 0.003736036f, 0.003242911f, 0.003231108f, 0.003738071f, 0.003113968f, 0.003437633f, 0.00338714f, 0.004045141f, 0.009173898f, 0.010261887f, 0.006592822f, 0.003889532f, 0.003599743f, 0.002767588f, 0.003197759f, 0.003884452f, 0.003210964f, 0.002308964f, 0.002814649f, 0.003234927f, 0.004003014f, 0.005245843f, 0.005110108f, 0.004316666f, 0.002835573f, 0.002542171f, 0.002941f, 0.003409736f, 0.003341975f, 0.003651033f, 0.003985283f, 0.004059936f, 0.006583904f, 0.008234021f, 0.007209875f, 0.005294397f, 0.004664436f, 0.003820919f, 0.00360099f, 0.005358389f, 0.005891565f, 0.00503189f, 0.0048358f, 0.005707217f, 0.008797606f, 0.010479555f, 0.01134227f, 0.008487623f, 0.005863491f, 0.007791344f, 0.005842496f, 0.006739907f, 0.007965548f, 0.007614477f, 0.008831213f, 0.007081883f, 0.008067858f, 0.006773615f, 0.009974808f, 0.008434597f, 0.0072024f, 0.005762624f, 0.003802413f, 0.004577579f, 0.003433535f, 0.003322959f, 0.003210249f, 0.003024596f, 0.004082466f, 0.003928354f, 0.005279666f, 0.004188859f, 0.003381451f, 0.003191737f, 0.003011113f, 0.003142852f, 0.002465093f, 0.002527388f, 0.003255348f, 0.002314624f, 0.002106838f, 0.002350005f, 0.003147413f, 0.002934311f, 0.0025473f, 0.002442728f, 0.002588025f, 0.003201701f, 0.002752983f, 0.002703252f, 0.00273478f, 0.002720786f, 0.002312796f, 0.002890772f, 0.004969903f, 0.004152251f, 0.003647022f, 0.003190347f, 0.004380569f, 0.004697512f, 0.004368799f, 0.004138811f, 0.004188808f, 0.00479434f, 0.006025621f, 0.004688245f, 0.004552933f, 0.005707262f, 0.006187435f, 0.005096617f, 0.004489184f, 0.006046749f, 0.004569514f, 0.005022408f, 0.004931439f, 0.004587055f, 0.004720241f, 0.003614869f, 0.005990079f, 0.00582451f, 0.003732854f, 0.004277667f, 0.003319692f, 0.003568341f, 0.002165692f, 0.001910403f, 0.00216246f, 0.002318037f, 0.00288603f, 0.002571002f, 0.002602925f, 0.002994552f, 0.002568692f, 0.002375392f, 0.002223141f, 0.00244997f, 0.002649397f, 0.002207482f, 0.001885778f, 0.001664675f, 0.002250166f, 0.00224945f, 0.002661974f, 0.003134007f, 0.002615421f, 0.002264273f, 0.00278778f, 0.002752787f, 0.002124277f, 0.002393045f, 0.002512005f, 0.00233865f, 0.002492648f, 0.002137017f, 0.001814198f, 0.002289871f, 0.002869487f, 0.003038559f, 0.002239149f, 0.001934985f, 0.002462432f, 0.002575495f, 0.002421769f, 0.002507606f, 0.00255928f, 0.0028948f, 0.002919328f, 0.003370967f, 0.003340182f, 0.00370649f, 0.00353344f, 0.002483141f, 0.002489171f, 0.002690796f, 0.002001094f, 0.001944529f, 0.002325691f, 0.0022779f, 0.002468772f, 0.002618216f, 0.002413364f, 0.00279985f, 0.002599623f, 0.002758567f, 0.00274686f, 0.002809938f, 0.002843466f, 0.002193596f, 0.002177731f, 0.003041292f, 0.003215934f, 0.003356719f, 0.003037466f, 0.00298871f, 0.002262475f, 0.002087745f, 0.002757678f, 0.002418262f, 0.002762945f, 0.003221054f, 0.003147093f, 0.003907634f, 0.004778129f, 0.005598324f, 0.005146298f, 0.005303535f, 0.007031651f, 0.007677855f, 0.008713714f, 0.005638289f, 0.005677695f, 0.006124063f, 0.005531779f, 0.005264752f, 0.003832695f, 0.004657036f, 0.00459557f, 0.003865303f, 0.00459157f, 0.004208036f, 0.003654569f, 0.002999982f, 0.00315558f, 0.003330447f, 0.002939395f, 0.002992992f, 0.002764042f, 0.002783382f, 0.002243078f, 0.002238093f, 0.002347885f, 0.002108314f, 0.002213929f, 0.002550941f, 0.001899238f, 0.002109102f, 0.001648909f, 0.002078855f, 0.001783253f, 0.001714636f, 0.002325785f, 0.001882223f, 0.00140822f, 0.00112403f, 0.001851332f, 0.001696211f, 0.001164946f, 0.000993f, 0.000995f, 0.001069167f, 0.001047784f, 0.001005738f, 0.000917f, 0.001048193f, 0.001222878f, 0.001174527f, 0.001077326f, 0.000741f, 0.00075f, 0.000685f, 0.000676f, 0.000781f, 0.000714f, 0.00075f, 0.000935f, 0.000907f, 0.001150785f, 0.000868f, 0.000712f, 0.000713f, 0.000735f, 0.000857f, 0.000727f, 0.000879f, 0.000753f, 0.000571f, 0.000834f, 0.000819f, 0.000686f, 0.000544f, 0.000537f, 0.001009152f, 0.000857f, 0.000725f, 0.000673f, 0.000694f, 0.000814f, 0.000892f, 0.001266582f, 0.001175191f, 0.001162577f, 0.001318548f, 0.001284338f, 0.001318322f, 0.001498491f, 0.00143062f, 0.001621504f, 0.001576876f, 0.00152298f, 0.001515498f, 0.001810714f, 0.001884926f, 0.002143368f, 0.002090198f, 0.002253864f, 0.00179984f, 0.001317295f, 0.001390246f, 0.001961567f, 0.001501375f, 0.0010768f, 0.00133888f, 0.001548078f, 0.001321456f, 0.001559223f, 0.001850775f, 0.0014659f, 0.001583899f, 0.001690804f, 0.00141162f, 0.001185879f, 0.001402148f, 0.001452361f, 0.001292821f, 0.001436333f, 0.001111288f, 0.000876f, 0.000826f, 0.000591f, 0.000932f, 0.00085f, 0.001195129f, 0.001049737f, 0.000838f, 0.000745f, 0.000688f, 0.000868f, 0.001088746f, 0.001166338f, 0.000912f, 0.000942f, 0.001122957f, 0.001258559f, 0.001432608f, 0.001255099f, 0.001236677f, 0.001248525f, 0.001344167f, 0.00121911f, 0.000996f, 0.00132967f, 0.001443989f, 0.001578397f, 0.001390416f, 0.001308318f, 0.001231627f, 0.001310105f, 0.001309679f, 0.00160668f, 0.001389094f, 0.001562097f, 0.001530275f, 0.001233852f, 0.001376998f, 0.001206978f, 0.001530769f, 0.001086491f, 0.001033149f, 0.00116664f, 0.000899f, 0.001168508f, 0.000979f, 0.000856f, 0.001114646f, 0.001071339f, 0.000685f, 0.000609f, 0.000644f, 0.000975f, 0.00103255f, 0.000838f, 0.000825f, 0.00086f, 0.000866f, 0.000824f, 0.00077f, 0.000627f, 0.000735f, 0.000668f, 0.000687f, 0.00094f, 0.001213556f, 0.001075644f, 0.001066737f, 0.001180859f, 0.000861f, 0.001025152f, 0.001222247f, 0.001662024f, 0.001522578f, 0.001366786f, 0.00121736f, 0.001220326f, 0.001226291f, 0.001140454f, 0.001181137f, 0.000928f, 0.000963f, 0.000905f, 0.000919f, 0.000864f, 0.000868f, 0.000818f, 0.000861f, 0.000725f, 0.000698f, 0.000555f, 0.000546f, 0.00066f, 0.000798f, 0.000616f, 0.00042f, 0.000528f, 0.000746f, 0.000704f, 0.000592f, 0.000556f, 0.000605f, 0.000448f, 0.000533f, 0.000594f, 0.000691f, 0.000608f, 0.000444f, 0.000398f, 0.000607f, 0.000571f, 0.000525f, 0.000393f, 0.000636f, 0.000834f, 0.000724f, 0.000617f, 0.000582f, 0.000509f, 0.000428f, 0.000486f, 0.000525f, 0.000579f, 0.000639f, 0.000764f, 0.000575f, 0.000557f, 0.000652f, 0.000833f, 0.000672f, 0.000622f, 0.000813f, 0.000906f, 0.000798f, 0.00097f, 0.000927f, 0.000855f, 0.001087637f, 0.001122596f, 0.001150525f, 0.001011543f, 0.001144388f, 0.000891f, 0.000786f, 0.000843f, 0.000732f, 0.001050679f, 0.000936f, 0.000727f, 0.00099f, 0.001020521f, 0.000946f, 0.00097f, 0.001180282f, 0.001239922f, 0.00101897f, 0.000923f, 0.000699f, 0.001065092f, 0.001515077f, 0.001221773f, 0.001001067f, 0.000603f, 0.000683f, 0.000849f, 0.000742f, 0.000769f, 0.000622f, 0.000686f, 0.000677f, 0.000594f, 0.000577f, 0.000768f, 0.000625f, 0.000466f, 0.000787f, 0.000828f, 0.000551f, 0.000702f, 0.000847f, 0.000962f, 0.000725f, 0.000968f, 0.00068f, 0.000813f, 0.000943f, 0.000848f, 0.001071905f, 0.000826f, 0.001296525f, 0.001252475f, 0.001183245f, 0.001259382f, 0.001007188f, 0.001234341f, 0.001154061f, 0.001148548f, 0.001260115f, 0.001218403f, 0.001137791f, 0.001099387f, 0.000951f, 0.00093f, 0.000942f, 0.00103404f, 0.00087f, 0.000945f, 0.000783f, 0.000789f, 0.000628f, 0.000821f, 0.000836f, 0.000795f, 0.000778f, 0.000993f, 0.000906f, 0.001001002f, 0.000891f, 0.000838f, 0.000916f, 0.00066f, 0.000898f, 0.000831f, 0.000993f, 0.001023258f, 0.000896f, 0.000966f, 0.000945f, 0.001217946f, 0.001140261f, 0.001118644f, 0.001133866f, 0.001303037f, 0.001200097f, 0.001335529f, 0.00161561f, 0.001323199f, 0.002109632f, 0.001842788f, 0.001739922f, 0.001919068f, 0.001782116f, 0.002051297f, 0.001729032f, 0.001666532f, 0.00186555f, 0.002006381f, 0.001823831f, 0.001657471f, 0.001658661f, 0.001622731f, 0.00130892f, 0.001235305f, 0.001322343f, 0.001235877f, 0.001060891f, 0.000974f, 0.000839f, 0.001004397f, 0.001289083f, 0.001235407f, 0.000864f, 0.00106324f, 0.000977f, 0.000925f, 0.001213047f, 0.001253325f, 0.000868f, 0.000695f, 0.000825f, 0.000827f, 0.000791f, 0.000735f, 0.001001082f, 0.000929f, 0.001103626f, 0.001261598f, 0.000954f, 0.000576f, 0.000726f, 0.000973f, 0.00113594f, 0.00081f, 0.000885f, 0.000809f, 0.001186665f, 0.001142818f, 0.001024846f, 0.000943f, 0.000727f, 0.000913f, 0.000867f, 0.001012864f, 0.000924f, 0.000812f, 0.001015386f, 0.000845f, 0.000948f, 0.000907f, 0.000698f, 0.000545f, 0.000609f, 0.000749f, 0.000683f, 0.000529f, 0.000479f, 0.000378f, 0.000582f, 0.000555f, 0.000424f, 0.000415f, 0.000445f, 0.000443f, 0.000423f, 0.000382f, 0.000434f, 0.000497f, 0.000548f, 0.000435f, 0.000321f, 0.000325f, 0.000428f, 0.000508f, 0.000446f, 0.000447f, 0.000429f, 0.000364f, 0.000299f, 0.00048f, 0.000484f, 0.000436f, 0.000457f, 0.000461f, 0.000546f, 0.000443f, 0.000447f, 0.000574f, 0.000562f, 0.000592f, 0.000513f, 0.000434f, 0.000381f, 0.000468f, 0.000496f, 0.000434f, 0.000446f, 0.000501f, 0.000415f, 0.000369f, 0.000298f, 0.000321f, 0.00036f, 0.000345f, 0.000319f, 0.000347f, 0.000475f, 0.000334f, 0.000264f, 0.000406f, 0.000513f, 0.000335f, 0.000205f, 0.000272f, 0.000343f, 0.000281f};
//                testData3.dFreq = new float[] {0.002305141f, 0.10516118f, 0.040704846f, 0.035347767f, 0.029254375f, 0.019184764f, 0.008639024f, 0.010162195f, 0.011888483f, 0.012700908f, 0.01085605f, 0.14515881f, 0.33942086f, 0.19542365f, 0.010163555f, 0.006739845f, 0.007512707f, 0.005446439f, 0.005413051f, 0.009280259f, 0.00900058f, 0.007308035f, 0.014918301f, 0.12684655f, 0.40794834f, 0.3049723f, 0.047293223f, 0.0212639f, 0.018264193f, 0.03468896f, 0.018769337f, 0.025318969f, 0.03399126f, 0.029844085f, 0.034890346f, 0.054669857f, 0.25536278f, 0.24029364f, 0.051634874f, 0.021139987f, 0.013072802f, 0.004595465f, 0.003130684f, 0.004297582f, 0.004675672f, 0.004619654f, 0.007974426f, 0.011087227f, 0.03643728f, 0.04169594f, 0.012367134f, 0.007549437f, 0.006896972f, 0.005563875f, 0.00338322f, 0.003186838f, 0.004218711f, 0.003299254f, 0.002416197f, 0.002982644f, 0.013784827f, 0.01887821f, 0.006895516f, 0.003082934f, 0.002501704f, 0.002974532f, 0.002960581f, 0.002190187f, 0.002372842f, 0.001796469f, 0.001562752f, 0.001593562f, 0.00503583f, 0.009111404f, 0.016675213f, 0.01187196f, 0.00371459f, 0.00291834f, 0.001842336f, 0.001581523f, 0.00171422f, 0.001474335f, 0.001453661f, 0.001427985f, 0.001657754f, 0.003361356f, 0.002707959f, 0.002381983f, 0.001649402f, 0.001755374f, 0.001403145f, 0.000958f, 0.001138228f, 0.001227313f, 0.000669f, 0.001117642f, 0.001673387f, 0.001804645f, 0.002305883f, 0.001859599f, 0.001525574f, 0.001306623f, 0.000936f, 0.000964f, 0.001006743f, 0.001239272f, 0.001390892f, 0.001397499f, 0.001056702f, 0.001607822f, 0.00234485f, 0.001868467f, 0.001122775f, 0.001372483f, 0.001228525f, 0.001011462f, 0.001300714f, 0.002356943f, 0.002728903f, 0.002574529f, 0.002385732f, 0.004071447f, 0.014999954f, 0.015026766f, 0.006846698f, 0.003456516f, 0.004190824f, 0.003909543f, 0.005253288f, 0.005583242f, 0.006741159f, 0.004914306f, 0.003155109f, 0.003745391f, 0.009487325f, 0.011147477f, 0.004323472f, 0.002518175f, 0.003289423f, 0.006477832f, 0.005671857f, 0.003999306f, 0.00592782f, 0.00536965f, 0.005842827f, 0.007103932f, 0.027904812f, 0.039599266f, 0.013886963f, 0.005810131f, 0.006790843f, 0.010391427f, 0.011988711f, 0.00858408f, 0.008661605f, 0.008003619f, 0.006173309f, 0.006745147f, 0.021461012f, 0.035738822f, 0.019575624f, 0.007583415f, 0.005180517f, 0.003938425f, 0.005814081f, 0.005853235f, 0.00449474f, 0.00397869f, 0.004455254f, 0.005320506f, 0.00901722f, 0.023608768f, 0.015231221f, 0.006787435f, 0.004382541f, 0.00370164f, 0.004598327f, 0.00520781f, 0.00484009f, 0.003483118f, 0.003162213f, 0.003471621f, 0.003399384f, 0.007168f, 0.007894791f, 0.005459394f, 0.002635999f, 0.00151647f, 0.002380699f, 0.002692724f, 0.002025856f, 0.002714099f, 0.002583535f, 0.002893258f, 0.004152353f, 0.0084877f, 0.007638632f, 0.003465049f, 0.003027295f, 0.002662777f, 0.0022895f, 0.001808907f, 0.001683281f, 0.002146454f, 0.002208722f, 0.002208292f, 0.00156159f, 0.003245761f, 0.004487433f, 0.004085179f, 0.001586277f, 0.000722f, 0.001326949f, 0.001226198f, 0.000744f, 0.00094f, 0.000693f, 0.000871f, 0.000806f, 0.000768f, 0.001009632f, 0.000808f, 0.000672f, 0.00063f, 0.000684f, 0.000582f, 0.000804f, 0.000912f, 0.000718f, 0.000665f, 0.000747f, 0.001012103f, 0.001133993f, 0.001142436f, 0.001010819f, 0.000769f, 0.000802f, 0.000747f, 0.000847f, 0.000747f, 0.000713f, 0.000708f, 0.000626f, 0.00071f, 0.000934f, 0.00081f, 0.000554f, 0.001067253f, 0.000869f, 0.001025479f, 0.00119214f, 0.001334244f, 0.0013126f, 0.001091644f, 0.001072741f, 0.001315698f, 0.001562516f, 0.002262872f, 0.0016307f, 0.00106533f, 0.001015094f, 0.000933f, 0.001265591f, 0.001072684f, 0.000966f, 0.000934f, 0.000767f, 0.001093765f, 0.001348751f, 0.001466429f, 0.001248f, 0.001288855f, 0.001546709f, 0.001680682f, 0.001505605f, 0.001610904f, 0.001411579f, 0.000779f, 0.000733f, 0.000758f, 0.000835f, 0.000975f, 0.000539f, 0.000705f, 0.000524f, 0.000502f, 0.000438f, 0.000358f, 0.000393f, 0.000614f, 0.000534f, 0.000443f, 0.000437f, 0.000504f, 0.000589f, 0.000523f, 0.000303f, 0.000566f, 0.000677f, 0.000936f, 0.00088f, 0.00107781f, 0.001226963f, 0.001115539f, 0.000864f, 0.001188067f, 0.001397869f, 0.000898f, 0.000763f, 0.001074124f, 0.001222431f, 0.001145798f, 0.001360439f, 0.001437053f, 0.001704303f, 0.001954504f, 0.003080883f, 0.004795015f, 0.006079092f, 0.004705516f, 0.0033368f, 0.002342624f, 0.002423425f, 0.002258695f, 0.0022251f, 0.00191201f, 0.001280761f, 0.001154187f, 0.00112733f, 0.001361858f, 0.001876923f, 0.001830282f, 0.001269967f, 0.001344599f, 0.001382058f, 0.001043427f, 0.001136534f, 0.00172013f, 0.001760206f, 0.00132746f, 0.001409465f, 0.00208097f, 0.004419495f, 0.003571309f, 0.002462067f, 0.002370516f, 0.001612828f, 0.001384832f, 0.0013425f, 0.001762201f, 0.001994801f, 0.001392844f, 0.001754025f, 0.001955858f, 0.003384468f, 0.00320199f, 0.002052252f, 0.001427282f, 0.001156618f, 0.001408883f, 0.001469924f, 0.00160971f, 0.001622919f, 0.001389114f, 0.001371265f, 0.001811138f, 0.001652206f, 0.001983133f, 0.001874959f, 0.001672746f, 0.00179503f, 0.001612618f, 0.001464851f, 0.001287445f, 0.001894339f, 0.001735635f, 0.001324952f, 0.001879161f, 0.001732528f, 0.001618591f, 0.002275505f, 0.002778322f, 0.002376845f, 0.002430048f, 0.001902901f, 0.002066542f, 0.00269008f, 0.002309885f, 0.002871141f, 0.003085534f, 0.002174071f, 0.003129841f, 0.002855771f, 0.002503928f, 0.00265952f, 0.002414124f, 0.002370013f, 0.002267743f, 0.002425141f, 0.002348725f, 0.002706202f, 0.003332187f, 0.003056884f, 0.003115914f, 0.002714924f, 0.00254988f, 0.002792904f, 0.00189772f, 0.001695693f, 0.002051704f, 0.00183392f, 0.002938179f, 0.002476499f, 0.002264028f, 0.00249783f, 0.002246454f, 0.00249804f, 0.002871939f, 0.003155877f, 0.002313107f, 0.001951125f, 0.001948675f, 0.002493373f, 0.002365303f, 0.00204221f, 0.002254032f, 0.002414738f, 0.003963193f, 0.004684949f, 0.003497589f, 0.003873844f, 0.004085241f, 0.003251998f, 0.00411854f, 0.005245625f, 0.005180783f, 0.004768109f, 0.005336303f, 0.004829624f, 0.005698801f, 0.006248393f, 0.006647684f, 0.006202976f, 0.004076893f, 0.00370782f, 0.00293982f, 0.002193354f, 0.002027538f, 0.002038599f, 0.002244282f, 0.002296399f, 0.001994309f, 0.001510999f, 0.001766651f, 0.002120932f, 0.002284398f, 0.002321629f, 0.001483267f, 0.001773296f, 0.002202436f, 0.002271757f, 0.002005006f, 0.003289796f, 0.002334088f, 0.002391706f, 0.002919054f, 0.003202386f, 0.003330788f, 0.003566009f, 0.002433355f, 0.00341302f, 0.003991399f, 0.004241697f, 0.003698204f, 0.003569514f, 0.003586405f, 0.003979072f, 0.004256835f, 0.004125505f, 0.003893554f, 0.00332167f, 0.002621158f, 0.004004135f, 0.004884727f, 0.003342015f, 0.003795587f, 0.005181467f, 0.004965166f, 0.006700737f, 0.005720262f, 0.005722994f, 0.005493121f, 0.005420729f, 0.004853934f, 0.003486977f, 0.003900985f, 0.00333836f, 0.003370209f, 0.002290948f, 0.002156534f, 0.001761862f, 0.001972194f, 0.002053761f, 0.001976816f, 0.001832108f, 0.001580108f, 0.001476104f, 0.001749167f, 0.001804638f, 0.001409196f, 0.000796f, 0.001300261f, 0.001387081f, 0.000973f, 0.001234963f, 0.00145673f, 0.001363022f, 0.001280642f, 0.001608937f, 0.001686483f, 0.001563296f, 0.002037515f, 0.001823276f, 0.001757286f, 0.001219651f, 0.001466236f, 0.001988647f, 0.001418011f, 0.001520369f, 0.001112124f, 0.001354043f, 0.00185269f, 0.001392727f, 0.001043497f, 0.001219945f, 0.001657798f, 0.001560814f, 0.001557649f, 0.001429847f, 0.002362338f, 0.002553555f, 0.002696159f, 0.002825508f, 0.002885913f, 0.003091159f, 0.003425562f, 0.004238214f, 0.003576999f, 0.004466259f, 0.004909488f, 0.005870864f, 0.005570168f, 0.003805781f, 0.004530043f, 0.004666289f, 0.004083003f, 0.002956505f, 0.003371739f, 0.003161074f, 0.002639809f, 0.002502231f, 0.002264681f, 0.001995191f, 0.002307946f, 0.001800144f, 0.001595696f, 0.001497853f, 0.001965116f, 0.002045576f, 0.001624907f, 0.000987f, 0.001323718f, 0.001465781f, 0.001419161f, 0.001227414f, 0.001046592f, 0.001158525f, 0.001133342f, 0.001042682f, 0.001401326f, 0.000895f, 0.00119753f, 0.001294095f, 0.001913285f, 0.00176956f, 0.001172977f, 0.001182687f, 0.001173177f, 0.001745063f, 0.001864959f, 0.001637643f, 0.001609579f, 0.001737648f, 0.001950104f, 0.00181714f, 0.001356101f, 0.001424585f, 0.00200484f, 0.002430782f, 0.003011999f, 0.002512563f, 0.002110398f, 0.002964418f, 0.003405554f, 0.002619809f, 0.002783823f, 0.003354239f, 0.002736518f, 0.00320843f, 0.00330317f, 0.002628348f, 0.002556329f, 0.002929253f, 0.003397441f, 0.003831522f, 0.003716834f, 0.003926885f, 0.004086383f, 0.002705357f, 0.003517128f, 0.004119731f, 0.004984435f, 0.005386706f, 0.003860228f, 0.003376998f, 0.00368498f, 0.00250677f, 0.002349729f, 0.00193505f, 0.002119504f, 0.002285911f, 0.001912319f, 0.001468478f, 0.001925249f, 0.001839323f, 0.001645233f, 0.001609081f, 0.001456548f, 0.001448885f, 0.001470439f, 0.000809f, 0.001148144f, 0.001025706f, 0.000708f, 0.000942f, 0.000923f, 0.000936f, 0.000891f, 0.001164229f, 0.001548627f, 0.000984f, 0.00058f, 0.000781f, 0.001116597f, 0.000917f, 0.000798f, 0.000868f, 0.000726f, 0.000847f, 0.000971f, 0.000937f, 0.00104786f, 0.000988f, 0.001254966f, 0.001365491f, 0.000902f, 0.001122138f, 0.001081135f, 0.001104993f, 0.001145102f, 0.001197845f, 0.001068547f, 0.001299065f, 0.001317719f, 0.000986f, 0.001396252f, 0.001347894f, 0.001425142f, 0.001470056f, 0.001263618f, 0.001403545f, 0.001650517f, 0.001763508f, 0.001364781f, 0.001158728f, 0.001724259f, 0.001592266f, 0.001221561f, 0.001052316f, 0.00133666f, 0.001405302f, 0.001497928f, 0.001551167f, 0.001147127f, 0.001677909f, 0.001512098f, 0.001432796f, 0.00216381f, 0.002709694f, 0.002118608f, 0.002292099f, 0.002192518f, 0.002305813f, 0.002783977f, 0.001655367f, 0.001919221f, 0.002194562f, 0.002279168f, 0.001907525f, 0.002081764f, 0.002599978f, 0.00170804f, 0.003239712f, 0.00291117f, 0.002795399f, 0.003327417f, 0.002850395f, 0.002831016f, 0.00275882f, 0.002061817f, 0.002110818f, 0.002851645f, 0.002670188f, 0.002882505f, 0.002768552f, 0.001818128f, 0.003082583f, 0.003421347f, 0.003087481f, 0.002160554f, 0.002342267f, 0.00445505f, 0.003010515f, 0.002254156f, 0.003569413f, 0.002863579f, 0.0045244f, 0.005024935f, 0.003811979f, 0.003804853f, 0.003978587f, 0.005900834f, 0.005292884f, 0.004969081f, 0.005650917f, 0.006177912f, 0.006824567f, 0.005906532f, 0.008311788f, 0.007653321f, 0.006707429f, 0.007053205f, 0.006645951f, 0.005935208f, 0.004920899f, 0.006668446f, 0.006474445f, 0.004482053f, 0.004441945f, 0.004384358f, 0.005335052f, 0.00557408f, 0.004000021f, 0.004315595f, 0.003795397f, 0.003509274f, 0.003893351f, 0.003356516f, 0.003281744f, 0.004138558f, 0.0048732f, 0.004264096f, 0.00340627f, 0.003295425f, 0.00330081f, 0.00300703f, 0.002895619f, 0.002435696f, 0.00234794f, 0.003020482f, 0.002604308f, 0.002030038f, 0.002108365f, 0.002279403f, 0.001808825f, 0.002461311f, 0.002272578f, 0.002329429f, 0.00206101f, 0.00200547f, 0.001821006f, 0.001352023f, 0.001475847f, 0.001675431f, 0.00170369f, 0.001291679f, 0.001133708f, 0.001378742f, 0.000814f, 0.000893f, 0.000869f, 0.000508f, 0.000528f, 0.000693f, 0.000851f, 0.000689f, 0.000877f, 0.001146486f, 0.001129354f, 0.001114023f, 0.001018813f, 0.001246339f, 0.001546191f, 0.001711518f, 0.001410377f, 0.001146317f, 0.001814134f, 0.001854956f, 0.002180548f, 0.002409135f, 0.002355482f, 0.002730953f, 0.002160939f, 0.001777194f, 0.001874249f, 0.001862887f, 0.001698926f, 0.00149838f, 0.001215804f, 0.000998f, 0.000932f, 0.000881f, 0.000832f, 0.001084252f, 0.000958f, 0.00100949f, 0.001099744f, 0.001158618f, 0.001264592f, 0.001222262f, 0.001325588f, 0.001602468f, 0.0019086f, 0.001624859f, 0.001052662f, 0.001688933f, 0.001920191f, 0.001839543f, 0.001379589f, 0.00107644f, 0.001438908f, 0.001595785f, 0.001335797f, 0.001162842f, 0.001362072f, 0.001668719f, 0.001314799f, 0.001329456f, 0.001331219f, 0.001228516f, 0.001102553f, 0.001047314f, 0.001106391f, 0.001007431f, 0.001299382f, 0.001438476f, 0.001456239f, 0.001442761f, 0.001098225f, 0.000991f, 0.001401438f, 0.001358663f, 0.001183144f, 0.0012267f, 0.001403365f, 0.001264661f, 0.001092484f, 0.001305425f, 0.001285593f, 0.001166894f, 0.000706f, 0.000878f, 0.001046086f, 0.00111362f, 0.000979f, 0.000953f, 0.000912f, 0.00132058f, 0.001299423f, 0.001140835f, 0.001285982f, 0.001490186f, 0.001083409f, 0.001341209f, 0.001396428f, 0.001851579f, 0.001560626f, 0.001280679f, 0.001280213f, 0.001819847f, 0.001923373f, 0.001431171f, 0.00146658f, 0.001667598f, 0.00195292f, 0.0014203f, 0.001663816f, 0.001846272f, 0.00165698f, 0.001409969f, 0.001452433f, 0.001791245f, 0.001588231f, 0.001417299f, 0.001571629f, 0.001981105f, 0.001714673f, 0.001659999f, 0.00162392f, 0.002128857f, 0.001908025f, 0.002001178f, 0.002575322f, 0.002293365f, 0.002429828f, 0.0019391f, 0.002337308f, 0.002345754f, 0.001893986f, 0.001735977f, 0.001721187f, 0.002774558f, 0.002538053f, 0.001818855f, 0.001549858f, 0.001193312f, 0.001457353f, 0.001597998f, 0.001650411f, 0.001449222f, 0.001810648f, 0.001508542f, 0.001573653f, 0.001338375f, 0.001592652f, 0.001644478f, 0.001729033f, 0.001582896f, 0.002252615f, 0.002016201f, 0.001725733f, 0.001630471f, 0.001905229f, 0.002665248f, 0.001818223f, 0.001552717f, 0.001814516f, 0.001875472f, 0.001814615f, 0.001742311f, 0.002015264f, 0.001594827f, 0.001090375f, 0.001484627f, 0.001371473f, 0.001095111f, 0.001067505f, 0.001145579f, 0.001026396f, 0.000631f, 0.001001644f, 0.001132421f, 0.001119685f, 0.001137164f, 0.000943f, 0.001071774f, 0.000825f, 0.001120754f, 0.00115858f, 0.001201227f, 0.001202593f, 0.001074837f, 0.001328378f, 0.000887f, 0.000868f, 0.000832f, 0.001209066f, 0.00117225f, 0.001196063f, 0.00103197f, 0.001102184f, 0.000842f, 0.000741f, 0.001073008f, 0.001043592f, 0.001119018f, 0.000959f, 0.001082716f, 0.001080585f, 0.001151277f, 0.001365889f, 0.001448846f, 0.000967f, 0.001113558f, 0.001404734f, 0.001375884f, 0.001499085f, 0.001301356f, 0.001195556f, 0.00143836f, 0.001349487f, 0.001284261f, 0.000949f, 0.001393908f, 0.00164231f, 0.001226982f, 0.001162187f, 0.001010136f, 0.001098641f, 0.001189384f, 0.001251703f, 0.001074344f, 0.001037806f, 0.000935f};
//
//                measureDataSensor1.getAxisBuf().setfFreq(testData1.dFreq);  // for test
//                measureDataSensor2.getAxisBuf().setfFreq(testData2.dFreq);
//                measureDataSensor3.getAxisBuf().setfFreq(testData3.dFreq);
//                measureDataSensor1.getAxisBuf().setfTime(testData1.dPwrSpectrum);
//                measureDataSensor2.getAxisBuf().setfTime(testData2.dPwrSpectrum);
//                measureDataSensor3.getAxisBuf().setfTime(testData3.dPwrSpectrum);

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
                isUpload = false;

                try {
                    drawChart(measuredFreq1, measuredFreq2, measuredFreq3);
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

        TCPSendUtil.sendRawMeasureSensor3(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {

                uploadedSensorIndex++;

                if( uploadedSensorIndex > 2 ) { // 3개 전부 업로드된 경우만
                    progressDialogUtil.hide();
                    DialogUtil.confirm(m_context, "Success", "Successfully uploaded raw data to the server", null);

                    isUpload = true;
                }
                //enableUploadToWebManager(true);
            }

            @Override
            public void onFailed(String tag, String msg) {
                progressDialogUtil.hide();

                //DialogUtil.confirm(m_context, "Failed to upload raw data to the server.", msg, null);
                ToastUtil.showLong("Failed to upload raw data to the server." + msg);

            }
        });
    }

    //센서 정보 업로드 할지 물어보기
    private void guideConfigUploadToWebManager(){
        DialogUtil.yesNo(m_context, "Upload Sensor Infomation", "Do you want to upload the sensor information you are currently viewing to Web Manager?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    doUploadConfigToWebManager(1);
                    Thread.sleep(1000);
                    doUploadConfigToWebManager(2);
                    Thread.sleep(1000);
                    doUploadConfigToWebManager(3);
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    //센서 정보 업로드
    private void doUploadConfigToWebManager(int sensorIndex){
        SVS.getInstance().sensorType60 = true;
        uploadedConfigSensorIndex = 0;

        //final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(m_context, "Upload Sensor information", "Data transfer is in progress. Please wait.");
        //progressDialogUtil.show();

        TCPSendUtil.sendConfig(sensorIndex, new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {

                uploadedConfigSensorIndex++;

                if( uploadedConfigSensorIndex > 2 ) { // 3개 전부 업로드된 경우만
                    //progressDialogUtil.hide();
                    DialogUtil.confirm(m_context, "Success", "Successfully uploaded sensor information to the server", null);
                }
            }

            @Override
            public void onFailed(String tag, String msg) {
                //progressDialogUtil.hide();

                DialogUtil.yesNo(m_context, "Failed to upload sensor information. Try again?", msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //재업로드 로직
                        //doUploadConfigToWebManager();
                    }
                }, null);

            }
        });
    }
}
