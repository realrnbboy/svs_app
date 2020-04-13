package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.ResultDiagnosisListAdapter;

// added by hslee 2020-01-29
// 진단분석 결과1 화면
public class RecordManagerActivity extends BaseActivity implements OnChartValueSelectedListener {

    private static final String TAG = "RecordManagerActivity";

    //String dateFormat = "yyyy-MM-dd HH:mm";
    AnalysisData analysisData = null;
    String equipmentUuid = null;

    LinearLayout linearLayoutDefectCause;
    ListView listViewResultDiagnosis;
    ResultDiagnosisListAdapter resultDiagnosisListAdapter;
    ArrayList<CauseModel> resultDiagnosisList = new ArrayList<>();

    LineChart lineChartRms;
    RealmResults<AnalysisEntity> analysisEntityList;
    ArrayList<RmsModel> previousRmsModelList;
    ArrayList<Date> xDataList = new ArrayList<>();
    private final float XAXIS_LABEL_DEFAULT_ROATION = 70f;

    TextView textViewStartd;
    TextView textViewEndd;

    boolean bUsePreviousActivityData = false;
    boolean bShowPreviousData = true;   // 이전 화면에서 전달한 데이터를 사용할 경우, 아이템 클릭시 널포인트 오류가 나는 부분이 있음, 이를 구분하기 위해 사용

    boolean bShowChartPt1 = true; // 차트의 pt1 표시 여부
    boolean bShowChartPt2 = true; // 차트의 pt2 표시 여부
    boolean bShowChartPt3 = true; // 차트의 pt3 표시 여부

    boolean bShowCause = true;  // 이전 데이터를 사용할 경우만 사용

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

        Intent intent = getIntent();

        equipmentUuid = intent.getStringExtra("equipmentUuid");
        bShowCause = intent.getBooleanExtra("bShowCause", false);

        // 이전 Activity 에서 전달받은 데이터 가져오기
        //matrix2 = (MATRIX_2_Type)intent.getSerializableExtra("matrix2");


        // 이전 Activity 에서 전달받은 데이터 가져오기
        previousRmsModelList = (ArrayList<RmsModel>)intent.getSerializableExtra("previousRmsModelList");

        // 이전 Activity 에서 전달받은 데이터 가져오기
        //analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( previousRmsModelList != null ) {     // 초기데이터는 1주일 치 보여주기로 함. 2020.04.13

            String endd = Utils.getCurrentTime("yyyy-MM-dd");
            String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

            textViewStartd.setText(startd);
            textViewEndd.setText(endd);

            Thread t = new Thread() {
                public void run() {

                    // 차트 그리기
                    bUsePreviousActivityData = true;
                    drawChart(false);
                }
            };

            t.start();

            // 진단결과 값 추가
            //redrawResultDiagnosisItem(0);
        }
    }

    void initView() {

        String endd = Utils.getCurrentTime("yyyy-MM-dd");
        String startd = endd;
        //String startd = Utils.addDateDay(endd, -6, dateFormat);

        textViewStartd = findViewById(R.id.textViewStartd);
        textViewStartd.setText(startd);
        textViewEndd = findViewById(R.id.textViewEndd);
        textViewEndd.setText(endd);

        listViewResultDiagnosis = findViewById(R.id.listViewCause);

        resultDiagnosisListAdapter = new ResultDiagnosisListAdapter(this, R.layout.list_item_result_diagnosis, resultDiagnosisList, getResources());
        listViewResultDiagnosis.setAdapter(resultDiagnosisListAdapter);

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
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

                drawChart(false);
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

                drawChart(false);
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

                drawChart(false);
            }
        });

        linearLayoutDefectCause = findViewById(R.id.linearLayoutDefectCause);
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
//            analysisEntityList = realm.where(AnalysisEntity.class)
//                    .equalTo("equipmentUuid", equipmentUuid)
//                    .findAll()
//                    //.sort("created", Sort.DESCENDING);
//                    .sort("created", Sort.ASCENDING);
//
//            analysisEntityList = realm.where(AnalysisEntity.class)
//                    .lessThanOrEqualTo("created", endLong)
//                    .equalTo("equipmentUuid", equipmentUuid)
//                    .findAll()
//                    //.sort("created", Sort.DESCENDING);
//                    .sort("created", Sort.ASCENDING);
//
//            analysisEntityList = realm.where(AnalysisEntity.class)
//                    .greaterThanOrEqualTo("created", startLong)
//                    .equalTo("equipmentUuid", equipmentUuid)
//                    .findAll()
//                    //.sort("created", Sort.DESCENDING);
//                    .sort("created", Sort.ASCENDING);

            analysisEntityList = realm.where(AnalysisEntity.class)
                    .equalTo("type", 1) // 1 rms, 2 frequency
                    .greaterThanOrEqualTo("created", startLong)
                    .lessThanOrEqualTo("created", endLong)
                    .equalTo("equipmentUuid", equipmentUuid)
                    .findAll()
                    //.sort("created", Sort.DESCENDING);
                    .sort("created", Sort.ASCENDING);

            if( analysisEntityList != null ) {
                // 차트 그리기
                bUsePreviousActivityData = false;
                drawChart(bShowInitPreviousReport);
            }
            else {
                ToastUtil.showShort("no data.");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void redrawResultDiagnosisItem(int entityIndex) {

        resultDiagnosisList.clear();

        if( bShowPreviousData ) {    // 이전화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우
            if( analysisData != null ) {
                if( bShowCause ) {
                    linearLayoutDefectCause.setVisibility(View.VISIBLE);

                    for (int i = 0; i < analysisData.resultDiagnosis.length && i < 5; i++) {

                        CauseModel model = new CauseModel();

                        String cause = analysisData.resultDiagnosis[i].cause + "-" + analysisData.resultDiagnosis[i].desc;
                        model.setCause(cause);
                        model.setRatio(analysisData.resultDiagnosis[i].ratio);

                        resultDiagnosisList.add(model);
                    }
                }
                else {
                    linearLayoutDefectCause.setVisibility(View.GONE);
                }
            }
        }
        else {
            AnalysisEntity analysisEntity = analysisEntityList.get(entityIndex);
            if( analysisEntity != null ) {
                RealmList<String> causeList = analysisEntity.getCause();
                RealmList<String> causeDescList = analysisEntity.getCauseDesc();
                RealmList<Double> ratioList = analysisEntity.getRatio();
                boolean tShowCause = analysisEntity.isbShowCause();

                if( tShowCause ) {
                    linearLayoutDefectCause.setVisibility(View.VISIBLE);

                    if (causeList != null) {
                        resultDiagnosisList.clear();    // 기존 데이터 삭제

                        for (int i = 0; i < causeList.size(); i++) {
                            CauseModel model = new CauseModel();

                            String cause = causeList.get(i) + "-" + causeDescList.get(i);
                            model.setCause(cause);
                            model.setRatio(ratioList.get(i));

                            resultDiagnosisList.add(model);
                        }

                        resultDiagnosisListAdapter.notifyDataSetChanged();
                    }
                }
                else {
                    linearLayoutDefectCause.setVisibility(View.GONE);
                }
            }
        }

    }

    private void initChart() {
        lineChartRms = findViewById(R.id.lineChartRms);
        lineChartRms.setOnChartValueSelectedListener(this);
        lineChartRms.getDescription().setEnabled(false);
        lineChartRms.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        //lineChartRms.setMaxVisibleValueCount(20);
        //lineChartRms.setNoDataText(getResources().getString(R.string.recordingchartdata));
        lineChartRms.setNoDataText("no data. please select today or week");

        Legend l = lineChartRms.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = lineChartRms.getAxisRight();
        rightAxis.setEnabled(false);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMinimum(10);
//        rightAxis.setTextColor(Color.RED);

        YAxis leftAxis = lineChartRms.getAxisLeft();
        //leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = lineChartRms.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;
                if( value < 0 )
                    return "";

                if( index >= xDataList.size() )
                    return "";

                String str = "";
                try {

                    //Date date = svs.getMeasureDatas().get(index).getCaptureTime();
                    str = DateUtil.convertDate(xDataList.get(index), "MM-dd HH:mm");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return str;
            }
        });

        //xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelRotationAngle(XAXIS_LABEL_DEFAULT_ROATION); //X축에 있는 라벨의 각도
        //applyXAxisDefault(xAxis, l);
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
        //lineChartRms.setViewPortOffsets(100, 15, 50, 30);
        //lineChartRms.invalidate();
    }

    private void drawChart(boolean bShowInitPreviousReport) {

        try {
            Thread.sleep(1000); // 차트 초기화 시간 - 추가 안하면 정상적으로 표시 안됨.
        }
        catch (Exception e) {
        }

        LineData lineData = new LineData();

        ArrayList<Float> yDataList1 = new ArrayList<>();
        ArrayList<Float> yDataList2 = new ArrayList<>();
        ArrayList<Float> yDataList3 = new ArrayList<>();

        xDataList.clear();
        lineChartRms.clear();

        if( bUsePreviousActivityData ) {    // 이전 화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우

            try {

//                float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
//                float rms2 = analysisData.getMeasureData2().getSvsTime().getdRms();
//                float rms3 = analysisData.getMeasureData3().getSvsTime().getdRms();
////                float rms1 = 1;
////                float rms2 = 2;
////                float rms3 = 3;
                for( RmsModel rmsModel : previousRmsModelList ) {
                    yDataList1.add((float) rmsModel.getRms1());
                    yDataList2.add((float) rmsModel.getRms2());
                    yDataList3.add((float) rmsModel.getRms3());

                    Date created = new Date(rmsModel.getCreated());
                    xDataList.add(created);
                }

                if( bShowChartPt1 ) {
                    //yDataList1.add(rms1);

                    LineDataSet lineDataSet1 = generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen));
                    if (lineDataSet1 != null)
                        lineData.addDataSet(lineDataSet1);
                }

                if( bShowChartPt2 ) {
                    //yDataList2.add(rms2);

                    LineDataSet lineDataSet2 = generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange));
                    if (lineDataSet2 != null)
                        lineData.addDataSet(lineDataSet2);
                }

                if( bShowChartPt3 ) {
                    //yDataList3.add(rms3);

                    LineDataSet lineDataSet3 = generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue));
                    if (lineDataSet3 != null)
                        lineData.addDataSet(lineDataSet3);
                }

                //xDataList.add(analysisData.getMeasureData1().getCaptureTime());
//                if( previousRmsModelList != null && previousRmsModelList.size() > 0 ) {
//
//                }

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
                    xDataList.add(created);
                }


                if( bShowChartPt1 ) {
                    LineDataSet lineDataSet1 = generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen));
                    if (lineDataSet1 != null)
                        lineData.addDataSet(lineDataSet1);
                }

                if( bShowChartPt2 ) {
                    LineDataSet lineDataSet2 = generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange));
                    if (lineDataSet2 != null)
                        lineData.addDataSet(lineDataSet2);
                }

                if( bShowChartPt3 ) {
                    LineDataSet lineDataSet3 = generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue));
                    if (lineDataSet3 != null)
                        lineData.addDataSet(lineDataSet3);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if( xDataList.size() > 0 ) {
            //CombinedData combinedData = new CombinedData();

            lineData.setDrawValues(true);

            //combinedData.setData(lineData);

            XAxis xAxis = lineChartRms.getXAxis();
            int xAxisMaximum = yDataList1.size() <= 0 ? 0 : yDataList1.size()-1;
            xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅
            //xAxis.setAxisMaximum(1);

            lineChartRms.setData(lineData);
            lineChartRms.invalidate();


            if( bShowInitPreviousReport ) {
                redrawResultDiagnosisItem(xAxisMaximum);    // 하단 리스트 초기데이터 일때 그리기
            }
        }
    }

    private LineDataSet generateLineData(String label, ArrayList<Float> yDataList, int lineColor){
        ArrayList<Entry> entries = new ArrayList<>();
        LineDataSet lineDataSet = null;
        try {

            for(int i=0; i<yDataList.size(); i++) {
                entries.add(new Entry(i, yDataList.get(i)));
            }

            lineDataSet = new LineDataSet(entries, label);

            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setDrawCircles(true);
            lineDataSet.setValueTextColor(Color.WHITE);
            lineDataSet.setHighlightEnabled(true);  // 클릭했을 때, 십자기로 줄표시해줌

            lineDataSet.setColor(lineColor);
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int i = (int) e.getX();
        redrawResultDiagnosisItem(i);
    }

    @Override
    public void onNothingSelected() {

    }
}
