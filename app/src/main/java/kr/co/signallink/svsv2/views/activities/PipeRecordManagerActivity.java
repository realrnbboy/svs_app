package kr.co.signallink.svsv2.views.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.model.Constants;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-03-19
// 진단분석 결과1 화면
public class PipeRecordManagerActivity extends BaseActivity {

    private static final String TAG = "PipeRecordManagerActivity";

    //String dateFormat = "yyyy-MM-dd HH:mm";
    String equipmentUuid = null;

    LineChart lineChartRms;
    LineChart lineChartRawData;

    TextView textViewStartd;
    TextView textViewEndd;

    RealmResults<AnalysisEntity> analysisEntityList;
    AnalysisEntity selectedAnalysisEntity;  // db에서 불러온 데이터 클릭했을 때 사용됨
    RmsModel selectedRmsModel;  // 이전 화면에서 전달받은 데이터 클릭했을 때 사용됨
    ArrayList<RmsModel> previousRmsModelList;
    ArrayList<Date> rmsXDataList = new ArrayList<>();
    private final float XAXIS_LABEL_DEFAULT_ROATION = 70f;

    boolean bUsePreviousActivityData = false;
    boolean bShowPreviousData = true;   // 이전 화면에서 전달한 데이터를 사용할 경우, 아이템 클릭시 널포인트 오류가 나는 부분이 있음, 이를 구분하기 위해 사용

    float[] reportData1;
    float[] reportData2;
    float[] reportData3;
    String reportDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe_record_manager);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Pipe Record manager");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        initView();
        initChartRms();
        initChartRawData();

        Intent intent = getIntent();

        equipmentUuid = intent.getStringExtra("equipmentUuid");

        // 이전 Activity 에서 전달받은 데이터 가져오기
        previousRmsModelList = (ArrayList<RmsModel>)intent.getSerializableExtra("previousRmsModelList");
        if( previousRmsModelList != null ) {     // 초기데이터는 1주일 치 보여주기로 함. 2020.04.13

            String endd = Utils.getCurrentTime("yyyy-MM-dd");
            String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

            textViewStartd.setText(startd);
            textViewEndd.setText(endd);

            Thread t = new Thread() {
                public void run() {

                    // 차트 그리기
                    bUsePreviousActivityData = true;
                    drawChartRms(false);
                }
            };

            t.start();
        }

        //processButtonClickWeek(true);   // added by hslee 2020.04.13 초기화면에 1주일 정보 보여주기
    }

    private DatePickerDialog.OnDateSetListener datePickerDialoglistenerStart = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            monthOfYear++;  // 1작은 숫자로 반환됨
            String month = monthOfYear < 10 ? "0"+monthOfYear : String.valueOf(monthOfYear);    // 2자리수 채우기
            String day = dayOfMonth < 10 ? "0"+dayOfMonth : String.valueOf(dayOfMonth);    // 2자리수 채우기
            textViewStartd.setText(year + "-" + month + "-" + day);
        }
    };

    private DatePickerDialog.OnDateSetListener datePickerDialoglistenerEnd = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            monthOfYear++;  // 1작은 숫자로 반환됨
            String month = monthOfYear < 10 ? "0"+monthOfYear : String.valueOf(monthOfYear);    // 2자리수 채우기
            String day = dayOfMonth < 10 ? "0"+dayOfMonth : String.valueOf(dayOfMonth);    // 2자리수 채우기
            textViewEndd.setText(year + "-" + month + "-" + day);
        }
    };

    void initView() {
        String endd = Utils.getCurrentTime("yyyy-MM-dd");
        String startd = endd;
        //String startd = Utils.addDateDay(endd, -6, dateFormat);

        textViewStartd = findViewById(R.id.textViewStartd);
        textViewStartd.setText(startd);
        textViewEndd = findViewById(R.id.textViewEndd);
        textViewEndd.setText(endd);

        textViewStartd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String []date = textViewStartd.getText().toString().split("-");
                    int year = Integer.parseInt(date[0]);
                    int month = Integer.parseInt(date[1]);
                    int day = Integer.parseInt(date[2]);
                    DatePickerDialog dialog = new DatePickerDialog(PipeRecordManagerActivity.this, datePickerDialoglistenerStart, year, month-1, day);
                    dialog.show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        textViewEndd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String []date = textViewEndd.getText().toString().split("-");
                    int year = Integer.parseInt(date[0]);
                    int month = Integer.parseInt(date[1]);
                    int day = Integer.parseInt(date[2]);
                    DatePickerDialog dialog = new DatePickerDialog(PipeRecordManagerActivity.this, datePickerDialoglistenerEnd, year, month-1, day);
                    dialog.show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ImageButton imageButtonSearch = findViewById(R.id.imageButtonSearch);
        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = textViewStartd.getText().toString();
                String endd = Utils.addDateDay(textViewEndd.getText().toString(), 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌

                bShowPreviousData = false;
                getDataFromDb(false, start, endd);
            }
        });

        Button buttonToday = findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String today = Utils.getCurrentTime("yyyy-MM-dd");
                String endd = Utils.addDateDay(today, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                textViewStartd.setText(today);
                textViewEndd.setText(today);

                bShowPreviousData = false;
                getDataFromDb(false, today, endd);
            }
        });

        Button buttonWeek = findViewById(R.id.buttonWeek);
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processButtonClickWeek(false);
            }
        });

        Button buttonClose = findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 홈 화면으로 이동
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("pipePumpMode", "pipe");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        Button buttonReport = findViewById(R.id.buttonReport);
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( reportDate == null ) {
                    ToastUtil.showShort("please select rms in Overall trend chart.");
                    return;
                }

                // report1 화면으로 이동
                Intent intent = new Intent(getBaseContext(), PipeReport1Activity.class);
                intent.putExtra("equipmentUuid", equipmentUuid);

                intent.putExtra("date", reportDate);
                intent.putExtra("data1", reportData1);
                intent.putExtra("data2", reportData2);
                intent.putExtra("data3", reportData3);

                try {
                    String pipeName = bUsePreviousActivityData ? selectedRmsModel.getPipeName() : selectedAnalysisEntity.getPipeName();
                    String pipeImage = bUsePreviousActivityData ? selectedRmsModel.getPipeImage() : selectedAnalysisEntity.getPipeImage();
                    String pipeLocation = bUsePreviousActivityData ? selectedRmsModel.getPipeLocation() : selectedAnalysisEntity.getPipeLocation();
                    String pipeOperationScenario = bUsePreviousActivityData ? selectedRmsModel.getPipeOperationScenario() : selectedAnalysisEntity.getPipeOperationScenario();
                    intent.putExtra("pipeName", pipeName);
                    intent.putExtra("pipeImage", pipeImage);
                    intent.putExtra("pipeLocation", pipeLocation);
                    intent.putExtra("pipeOperationScenario", pipeOperationScenario);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showShort("invalid status " + bUsePreviousActivityData + ", " + (selectedRmsModel == null ? "selectedRmsModel null" : "") + (selectedAnalysisEntity == null ? "selectedAnalysisEntity null" : ""));
                }
                startActivity(intent);
            }
        });
    }

    void processButtonClickWeek(boolean bShowInitPreviousReport) {
        String endd = Utils.getCurrentTime("yyyy-MM-dd");
        String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
        String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

        textViewStartd.setText(startd);
        textViewEndd.setText(endd);

        bShowPreviousData = false;
        getDataFromDb(bShowInitPreviousReport, startd, tEndd);
    }

    // db에서 날짜에 맞는 데이터 가져오기
    void getDataFromDb(boolean bShowInitPreviousReport, String startd, String endd) {
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endd);

            long startLong = startDate.getTime();
            long endLong = endDate.getTime();

            Realm realm = Realm.getDefaultInstance();

            analysisEntityList = realm.where(AnalysisEntity.class)
                    .equalTo("type", 2) // 1 rms, 2 frequency
                    .greaterThanOrEqualTo("created", startLong)
                    .lessThanOrEqualTo("created", endLong)
                    .equalTo("equipmentUuid", equipmentUuid)
                    .findAll()
                    //.sort("created", Sort.DESCENDING);
                    .sort("created", Sort.ASCENDING);

            if( analysisEntityList != null ) {
                // 차트 그리기
                bUsePreviousActivityData = false;
                drawChartRms(bShowInitPreviousReport);
            }
            else {
                ToastUtil.showShort("please select rms.");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 화면 하단의 assessment result 차트 그리기
    private void drawChartRawData(int entityIndex) {

        float[] data1 = null;
        float[] data2 = null;
        float[] data3 = null;
        float[] data4 = Utils.getConcernDataList();
        float[] data5 = Utils.getProblemDataList();
        if( bShowPreviousData ) {    // 이전화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우
//            if (analysisData == null) {
//                return;
//            }

            //data1 = analysisData.getMeasureData1().getAxisBuf().getfFreq();
            data1 = previousRmsModelList.get(entityIndex).getFrequency1();
            data2 = previousRmsModelList.get(entityIndex).getFrequency2();
            data3 = previousRmsModelList.get(entityIndex).getFrequency3();

            selectedRmsModel = previousRmsModelList.get(entityIndex);
        }
        else {
            //data1 = new float[DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX];data1 = new float[Constants.MAX_PIPE_X_VALUE];
            data1 = new float[DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX];
            data2 = new float[DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX];
            data3 = new float[DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX];

            AnalysisEntity analysisEntity = analysisEntityList.get(entityIndex);
            if( analysisEntity != null ) {
                RealmList<Double> frequencyList1 = analysisEntity.getFrequency1();
                if( frequencyList1 != null ) {
                    for (int i = 0; i < frequencyList1.size(); i++) {
                        double frequency = frequencyList1.get(i);
                        data1[i] = (float)frequency;
                    }
                }
                RealmList<Double> frequencyList2 = analysisEntity.getFrequency2();
                if( frequencyList2 != null ) {
                    for (int i = 0; i < frequencyList2.size(); i++) {
                        double frequency = frequencyList2.get(i);
                        data2[i] = (float)frequency;
                    }
                }
                RealmList<Double> frequencyList3 = analysisEntity.getFrequency3();
                if( frequencyList3 != null ) {
                    for (int i = 0; i < frequencyList3.size(); i++) {
                        double frequency = frequencyList3.get(i);
                        data3[i] = (float)frequency;
                    }
                }

                selectedAnalysisEntity = analysisEntity;
            }
        }

        reportData1 = data1;
        reportData2 = data2;
        reportData3 = data3;

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
                    // v = (float)Math.log(v);
                    valueList1.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Vertical", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), false));

            if (data2 != null) {
                for (float v : data2) {
                    // v = (float)Math.log(v);
                    valueList2.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Horizontal", valueList2, ContextCompat.getColor(getBaseContext(), android.R.color.white), false));

            if (data3 != null) {
                for (float v : data3) {
                    // v = (float)Math.log(v);
                    valueList3.add(v);
                }
            }

            lineData.addDataSet(generateLineData("Axial", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myBlueLight), false));

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
//        int xAxisMaximum = valueList1.size() <= 0 ? 0 : valueList1.size() - 1;
//        xAxisMaximum = xAxisMaximum <= 0 ? valueList2.size() - 1 : xAxisMaximum;
//        xAxisMaximum = xAxisMaximum <= 0 ? valueList3.size() - 1 : xAxisMaximum;
//        xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅
        xAxis.setAxisMaximum(DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX);

        lineChartRawData.setData(lineData);
        lineChartRawData.invalidate();
    }

    private void initChartRms() {
        lineChartRms = findViewById(R.id.lineChartRms);
        //lineChartRms.setOnChartValueSelectedListener(this);
        lineChartRms.getDescription().setEnabled(false);
        lineChartRms.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        //lineChartRms.setMaxVisibleValueCount(20);
        //lineChartRms.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRms.setNoDataText("no data. please select today or week");
        lineChartRms.setOnChartValueSelectedListener(onChartValueSelectedListenerRms);

        Legend l = lineChartRms.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = lineChartRms.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = lineChartRms.getAxisLeft();
        //leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = lineChartRms.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if( value < 0 )
                    return "";

                if( index >= rmsXDataList.size() )
                    return "";

                String str = "";
                try {

                    //Date date = svs.getMeasureDatas().get(index).getCaptureTime();
                    str = DateUtil.convertDate(rmsXDataList.get(index), "MM-dd HH:mm");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return str;
            }
        });

        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelRotationAngle(XAXIS_LABEL_DEFAULT_ROATION); //X축에 있는 라벨의 각도
        adjustViewportForChart();

        final ScrollView scrollView = findViewById(R.id.scrollView);
        lineChartRms.setOnTouchListener(new View.OnTouchListener() {    // 차크 클릭 시, 스크롤뷰의 스크롤 기능을 off 하여 차트 스크롤 기능을 방해하지 않게 함.
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
    }

    private void initChartRawData() {
        lineChartRawData = findViewById(R.id.lineChartRawData);
        lineChartRawData.getDescription().setEnabled(false);
        lineChartRawData.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        lineChartRawData.setMaxVisibleValueCount(20);
        //lineChartRawData.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRawData.setNoDataText("no data.");
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
    }


    // 차트 위치 조정
    private void adjustViewportForChart(){

        //X축 라벨 사이즈의 최대 높이
        Rect rectByText = SizeUtil.getTextRect(DateUtil.convertDate(new Date(), "MM-dd HH:mm"), lineChartRms.getXAxis().getTypeface(), lineChartRms.getXAxis().getTextSize());
        float textYSize = rectByText.height();

        //회전 한 width, height <<회전변환 공식 참고>>
        double radian = Math.toRadians(XAXIS_LABEL_DEFAULT_ROATION);
        double rotWidth = (rectByText.width() * Math.cos(radian)) - (rectByText.height() * Math.sin(radian));
        double rotHeight = (rectByText.width() * Math.sin(radian)) + (rectByText.height() * Math.cos(radian));


        //빗변 계산
        double hypotenuse = Math.sqrt(Math.pow(rotWidth, 2) + Math.pow(rotHeight, 2)); //최대길이

        //대입
        textYSize = (float)hypotenuse;

        //레전드의 높이
        float legendSize = lineChartRms.getLegend().mNeededHeight + lineChartRms.getLegend().getYOffset();

        //뷰포트 offset
        float offsetLeft = lineChartRms.getViewPortHandler().offsetLeft();
        float offsetRight = lineChartRms.getViewPortHandler().offsetRight();
        float offsetSide = (offsetLeft > offsetRight) ? offsetLeft : offsetRight;
        float offsetBottom = textYSize + legendSize + SizeUtil.dpToPx(5); //15 is Padding

        lineChartRms.setViewPortOffsets(offsetSide+100,SizeUtil.dpToPx(5), offsetSide+50, offsetBottom+30);
        //lineChartRms.invalidate();
    }

    private void drawChartRms(boolean bShowInitPreviousReport) {

        try {
            Thread.sleep(1000); // 차트 초기화 시간 - 추가 안하면 정상적으로 표시 안됨.
        }
        catch (Exception e) {
        }

        LineData lineData = new LineData();

        ArrayList<Float> yDataList1 = new ArrayList<>();
        ArrayList<Float> yDataList2 = new ArrayList<>();
        ArrayList<Float> yDataList3 = new ArrayList<>();

        rmsXDataList.clear();
        lineChartRms.clear();

        if( bUsePreviousActivityData ) {    // 이전 화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우

            try {

                //float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
                for( RmsModel rmsModel : previousRmsModelList ) {
                    yDataList1.add((float) rmsModel.getRms1());
                    yDataList2.add((float) rmsModel.getRms2());
                    yDataList3.add((float) rmsModel.getRms3());

                    Date created = new Date(rmsModel.getCreated());
                    rmsXDataList.add(created);
                }

                //yDataList1.add(rms1);

                LineDataSet lineDataSet1 = generateLineData("Vertical", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), true);
                if (lineDataSet1 != null)
                    lineData.addDataSet(lineDataSet1);

                LineDataSet lineDataSet2 = generateLineData("Horizontal", yDataList2, ContextCompat.getColor(getBaseContext(), android.R.color.white), true);
                if (lineDataSet2 != null)
                    lineData.addDataSet(lineDataSet2);

                LineDataSet lineDataSet3 = generateLineData("Axial", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myBlueLight), true);
                if (lineDataSet3 != null)
                    lineData.addDataSet(lineDataSet3);

//                if( bShowInitPreviousReport ) {
//                    drawChartRawData((rmsXDataList.size() - 1) < 0 ? 0 : rmsXDataList.size() - 1);    // 하단 차트 초기데이터일때 그리기
//                }
                //rmsXDataList.add(analysisData.getMeasureData1().getCaptureTime());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {

            try {
                for( AnalysisEntity analysisEntity : analysisEntityList ) {
                    yDataList1.add(analysisEntity.getRms1());
                    yDataList2.add(analysisEntity.getRms2());
                    yDataList3.add(analysisEntity.getRms3());

                    Date created = new Date(analysisEntity.getCreated());
                    rmsXDataList.add(created);
                }

                LineDataSet lineDataSet1 = generateLineData("Vertical", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), true);
                if (lineDataSet1 != null) {
                    lineData.addDataSet(lineDataSet1);
                }

                LineDataSet lineDataSet2 = generateLineData("Horizontal", yDataList2, ContextCompat.getColor(getBaseContext(), android.R.color.white), true);
                if (lineDataSet2 != null) {
                    lineData.addDataSet(lineDataSet2);
                }

                LineDataSet lineDataSet3 = generateLineData("Axial", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myBlueLight), true);
                if (lineDataSet3 != null) {
                    lineData.addDataSet(lineDataSet3);
                }

                if( bShowInitPreviousReport ) {
                    drawChartRawData((rmsXDataList.size() - 1) < 0 ? 0 : rmsXDataList.size() - 1);    // 하단 차트 초기데이터일때 그리기
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if( rmsXDataList.size() > 0 ) {
            //CombinedData combinedData = new CombinedData();

            lineData.setDrawValues(true);

            //combinedData.setData(lineData);

            XAxis xAxis = lineChartRms.getXAxis();
            int xAxisMaximum = yDataList1.size() <= 0 ? 0 : yDataList1.size()-1;
            xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅

            lineChartRms.setData(lineData);
            lineChartRms.invalidate();
        }
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

//    @Override
//    public void onValueSelected(Entry e, Highlight h) {
//        int i = (int) e.getX();
//        drawChartRawData(i);
//
//        TextView textViewSelectedItemValue = findViewById(R.id.textViewSelectedRmsValue);
//        textViewSelectedItemValue.setText(String.format("%.3f", e.getY()));
//    }
//
//    @Override
//    public void onNothingSelected() {
//
//    }

    private OnChartValueSelectedListener onChartValueSelectedListenerRms = new OnChartValueSelectedListener() {

        @Override
        public void onValueSelected(Entry e, Highlight h) {
            int i = (int) e.getX();
            drawChartRawData(i);

            TextView textViewSelectedItemValue = findViewById(R.id.textViewSelectedRmsValue);
            textViewSelectedItemValue.setText(String.format("%.3f", e.getY()));

            String x = lineChartRms.getXAxis().getValueFormatter().getFormattedValue(e.getX(), lineChartRms.getXAxis());
            reportDate = "2020-" + x;   // 현재 구조에서 년도 가져올 방법이 없음.   // added by hslee 2020-06-22
        }

        @Override
        public void onNothingSelected() {

        }
    };

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
