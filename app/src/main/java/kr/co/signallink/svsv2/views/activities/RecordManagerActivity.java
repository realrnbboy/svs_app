package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.databases.PresetEntity;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.MyValueFormatter;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.ResultDiagnosisListAdapter;
import kr.co.signallink.svsv2.views.adapters.RmsListAdapter;

import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefConstant.TrendValue.RAW_TIME;

// added by hslee 2020-01-29
// 진단분석 결과1 화면
public class RecordManagerActivity extends BaseActivity {

    private static final String TAG = "RecordManagerActivity";

    //String dateFormat = "yyyy-MM-dd HH:mm";
    AnalysisData analysisData = null;
    MATRIX_2_Type matrix2;
    String equipmentUuid = null;

    ListView listViewResultDiagnosis;
    ResultDiagnosisListAdapter resultDiagnosisListAdapter;
    ArrayList<CauseModel> resultDiagnosisList = new ArrayList<>();

    CombinedChart combinedChartRms;
    RealmResults<AnalysisEntity> analysisEntityList;
    ArrayList<Date> xDataList = new ArrayList<>();
    private final float XAXIS_LABEL_DEFAULT_ROATION = 70f;

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

        equipmentUuid = intent.getStringExtra("equipmentUuid");

        // 이전 Activity 에서 전달받은 데이터 가져오기
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");

        // 이전 Activity 에서 전달받은 데이터 가져오기
        matrix2 = (MATRIX_2_Type)intent.getSerializableExtra("matrix2");

        String endd = Utils.getCurrentTime("yyyy-MM-dd");
        String startd = endd;
        //String startd = Utils.addDateDay(endd, -6, dateFormat);

        final TextView textViewStartd = findViewById(R.id.textViewStartd);
        textViewStartd.setText(startd);
        final TextView textViewEndd = findViewById(R.id.textViewEndd);
        textViewEndd.setText(endd);

        // 진단결과 값 추가
        addResultDiagnosisItem(true);

        listViewResultDiagnosis = findViewById(R.id.listViewCause);

        resultDiagnosisListAdapter = new ResultDiagnosisListAdapter(this, R.layout.list_item_result_diagnosis, resultDiagnosisList, getResources());
        listViewResultDiagnosis.setAdapter(resultDiagnosisListAdapter);

        Button buttonToday = findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String today = Utils.getCurrentTime("yyyy-MM-dd");
                String startd = Utils.addDateDay(today, -1, "yyyy-MM-dd");
                textViewStartd.setText(today);
                textViewEndd.setText(today);

                getDataFromDb(startd, today);
            }
        });

        Button buttonWeek = findViewById(R.id.buttonWeek);
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");
                textViewStartd.setText(startd);
                textViewEndd.setText(endd);

                getDataFromDb(startd, endd);
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

    // db에서 날짜에 맞는 데이터 가져오기
    void getDataFromDb(String startd, String endd) {
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
            Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endd);

            long startLong = startDate.getTime();
            long endLong = endDate.getTime();

            Realm realm = Realm.getDefaultInstance();
            analysisEntityList = realm.where(AnalysisEntity.class)
                    .between("created", startLong, endLong)
                    .equalTo("equipmentUuid", equipmentUuid)
                    .findAll()
                    //.sort("created", Sort.DESCENDING);
                    .sort("created", Sort.ASCENDING);

            if( analysisEntityList != null ) {
                drawChart(false);

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addResultDiagnosisItem(boolean bUsePreviousActivityData) {

        if( bUsePreviousActivityData ) {    // 이전화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우
            if( analysisData != null ) {
                for (int i = 0; i < analysisData.resultDiagnosis.length && i < 5; i++) {

                    CauseModel model = new CauseModel();

                    String cause = analysisData.resultDiagnosis[i].cause + "-" + analysisData.resultDiagnosis[i].desc;
                    model.setCause(cause);
                    model.setRatio(analysisData.resultDiagnosis[i].ratio);

                    resultDiagnosisList.add(model);
                }
            }
        }
        else {
            for( AnalysisEntity analysisEntity : analysisEntityList ) {
                //analysisEntity.g();

            }
        }
    }

    private void initChart() {
        combinedChartRms = findViewById(R.id.combinedChartRms);
        combinedChartRms.getDescription().setEnabled(false);
        combinedChartRms.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorContent));
        combinedChartRms.setOnChartGestureListener(OCGL);
        combinedChartRms.setMaxVisibleValueCount(20);
        //combinedChartRms.setNoDataText(getResources().getString(R.string.recordingchartdata));
        combinedChartRms.setNoDataText("no data. please select today or week");

        Legend l = combinedChartRms.getLegend();
        l.setTextColor(Color.WHITE);    // 범례 글자 색
        l.setWordWrapEnabled(false);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = combinedChartRms.getAxisRight();
        rightAxis.setEnabled(false);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisMinimum(10);
//        rightAxis.setTextColor(Color.RED);

        YAxis leftAxis = combinedChartRms.getAxisLeft();
        //leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setTextColor(Color.WHITE);

        XAxis xAxis = combinedChartRms.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setDrawGridLines(false);
        //xAxis.setLabelCount(5);
        //xAxis.setAvoidFirstLastClipping(true); //X 축에서 처음과 끝에 있는 라벨이 짤리는걸 방지해 준다. (index 0번째를 그냥 없앨때도 있다.)
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;

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

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(svs.getMeasureDatas().size()-1);
        //xAxis.setAxisMaximum(80);
        xAxis.setGranularity(1.0f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setLabelRotationAngle(XAXIS_LABEL_DEFAULT_ROATION); //X축에 있는 라벨의 각도
        //applyXAxisDefault(xAxis, l);
        adjustViewportForChart();
    }


    // 차트 위치 조정
    private void adjustViewportForChart(){

        //X축 라벨 사이즈의 최대 높이
        Rect rectByText = SizeUtil.getTextRect(DateUtil.convertDate(new Date(), "MM-dd HH:mm"), combinedChartRms.getXAxis().getTypeface(), combinedChartRms.getXAxis().getTextSize());
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
        float legendSize = combinedChartRms.getLegend().mNeededHeight + combinedChartRms.getLegend().getYOffset();

        //뷰포트 offset
        float offsetLeft = combinedChartRms.getViewPortHandler().offsetLeft();
        float offsetRight = combinedChartRms.getViewPortHandler().offsetRight();
        float offsetSide = (offsetLeft > offsetRight) ? offsetLeft : offsetRight;
        float offsetBottom = textYSize + legendSize + SizeUtil.dpToPx(5); //15 is Padding

        combinedChartRms.setViewPortOffsets(offsetSide+50,SizeUtil.dpToPx(5), offsetSide+50, offsetBottom+30);
        combinedChartRms.invalidate();
    }

    private void applyXAxisDefault(XAxis xAxis, Legend l){

        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        xAxis.setGranularity(1.0f);
        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(SVS.getInstance().getMeasureDatas().size()-1);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                int index = (int)value;

                String str = "";
                try {
                    str = DateUtil.convertDate(xDataList.get(index), "yyyy-MM-dd HH");
                } catch (Exception e) {
                    DefLog.d(TAG, e.toString());
                }

                return str;
            }
        });
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
            combinedChartRms.fitScreen();
        }

        @Override
        public void onChartDoubleTapped(MotionEvent me) {

        }

        @Override
        public void onChartSingleTapped(MotionEvent me) {

//            int itemCount = xDataList.size();
//            if( itemCount == 0 )
//                return;
//
//            float layerWidth = combinedChartRms.getWidth() / itemCount;
//
//            float [] spaceend = new float[itemCount-1];
//
//            for( int i = 0; i < spaceend.length; i++ ) {
//                if( i == 0 ) {  // 첫번째 구간
//                    spaceend[i] = layerWidth;
//                }
//                else if( i + 1 == spaceend.length ) {   // 마지막 구간
//                    spaceend[i] = spaceend[i + 1] + layerWidth;
//                }
//                else {  // 나머지 구간
//                    spaceend[i] = layerWidth;
//                }
//            }
//
//            spaceend[0] = layerWidth + layerWidth/2;
//            spaceend[1] = spaceend[0] + layerWidth;
//            spaceend[2] = spaceend[1] + layerWidth;
//            spaceend[3] = spaceend[2] + layerWidth;
//            spaceend[4] = spaceend[3] + layerWidth;
//
//            String date;
//            if(me.getX() > 0 && me.getX() < spaceend[0] && svsCode.getFreqEna()[0].getdPeak() != 0){
//                tappedTrendValue = PEAK1;
//            }else if(me.getX() > spaceend[0] && me.getX() < spaceend[1] && svsCode.getFreqEna()[1].getdPeak() != 0){
//                tappedTrendValue = PEAK2;
//            }else if(me.getX() > spaceend[1] && me.getX() < spaceend[2] && svsCode.getFreqEna()[2].getdPeak() != 0){
//                tappedTrendValue = PEAK3;
//            }else if(me.getX() > spaceend[2] && me.getX() < spaceend[3] && svsCode.getFreqEna()[3].getdPeak() != 0){
//                tappedTrendValue = PEAK4;
//            }else if(me.getX() > spaceend[3] && me.getX() < spaceend[4] && svsCode.getFreqEna()[4].getdPeak() != 0){
//                tappedTrendValue = PEAK5;
//            }else if(me.getX() > spaceend[4] && svsCode.getFreqEna()[5].getdPeak() != 0){
//                tappedTrendValue = PEAK6;
//            }
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


    private void drawChart(boolean bUsePreviousActivityData) {

        LineData lineData = new LineData();

        ArrayList<Float> yDataList1 = new ArrayList<>();
        ArrayList<Float> yDataList2 = new ArrayList<>();
        ArrayList<Float> yDataList3 = new ArrayList<>();

        if( bUsePreviousActivityData ) {    // 이전 화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우

            try {

                float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
                float rms2 = analysisData.getMeasureData2().getSvsTime().getdRms();
                float rms3 = analysisData.getMeasureData3().getSvsTime().getdRms();

                float[] data1 = {rms1};
                float[] data2 = {rms2};
                float[] data3 = {rms3};

                if (data1 != null) {
                    for (float v : data1) {
                        if( xDataList.size() == 0 )
                            xDataList.add(analysisData.getMeasureData1().getCaptureTime());
                        yDataList1.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));

                if (data2 != null) {
                    for (float v : data2) {
                        if( xDataList.size() == 0 )
                            xDataList.add(analysisData.getMeasureData2().getCaptureTime());
                        yDataList2.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));

                if (data3 != null) {
                    for (float v : data3) {
                        if( xDataList.size() == 0 )
                            xDataList.add(analysisData.getMeasureData3().getCaptureTime());
                        yDataList3.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue)));
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

                lineData.addDataSet(generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));
                lineData.addDataSet(generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));
                lineData.addDataSet(generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // valueList.clear();

        if( xDataList.size() > 0 ) {
            CombinedData combinedData = new CombinedData();

            lineData.setDrawValues(true);

            combinedData.setData(lineData);

            XAxis xAxis = combinedChartRms.getXAxis();
            int xAxisMaximum = yDataList1.size() <= 0 ? 0 : yDataList1.size() - 1;
            xAxis.setAxisMaximum(xAxisMaximum);    // data1,2,3의 데이터 개수가 같다고 가정하고, 한개만 세팅

            combinedChartRms.setData(combinedData);
            combinedChartRms.invalidate();
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

            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setValueTextColor(Color.WHITE);
            lineDataSet.setHighlightEnabled(false);

            lineDataSet.setColor(lineColor);

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

}
