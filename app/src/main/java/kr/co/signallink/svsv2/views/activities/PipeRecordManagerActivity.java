package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.SizeUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-03-19
// 진단분석 결과1 화면
public class PipeRecordManagerActivity extends BaseActivity implements OnChartValueSelectedListener {

    private static final String TAG = "PipeRecordManagerActivity";

    //String dateFormat = "yyyy-MM-dd HH:mm";
    AnalysisData analysisData = null;
    String equipmentUuid = null;

    LineChart lineChartRms;
    LineChart lineChartRawData;

    RealmResults<AnalysisEntity> analysisEntityList;
    ArrayList<Date> rmsXDataList = new ArrayList<>();
    private final float XAXIS_LABEL_DEFAULT_ROATION = 70f;

    boolean bUsePreviousActivityData = false;
    boolean bShowPreviousData = true;   // 이전 화면에서 전달한 데이터를 사용할 경우, 아이템 클릭시 널포인트 오류가 나는 부분이 있음, 이를 구분하기 위해 사용

    boolean bShowChartPt1 = true; // 차트의 pt1 표시 여부
    boolean bShowChartPt2 = true; // 차트의 pt2 표시 여부
    boolean bShowChartPt3 = true; // 차트의 pt3 표시 여부

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
        analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
        if( analysisData != null ) {

            Thread t = new Thread() {
                public void run() {

                    // 차트 그리기
                    bUsePreviousActivityData = true;
                    drawChartRms();
                }
            };

            t.start();

            // 진단결과 값 추가
            drawChartRawData(0);
        }
    }

    void initView() {

        String endd = Utils.getCurrentTime("yyyy-MM-dd");
        String startd = endd;
        //String startd = Utils.addDateDay(endd, -6, dateFormat);

        final TextView textViewStartd = findViewById(R.id.textViewStartd);
        textViewStartd.setText(startd);
        final TextView textViewEndd = findViewById(R.id.textViewEndd);
        textViewEndd.setText(endd);

        Button buttonToday = findViewById(R.id.buttonToday);
        buttonToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String today = Utils.getCurrentTime("yyyy-MM-dd");
                String endd = Utils.addDateDay(today, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                textViewStartd.setText(today);
                textViewEndd.setText(today);

                bShowPreviousData = false;
                getDataFromDb(today, endd);
            }
        });

        Button buttonWeek = findViewById(R.id.buttonWeek);
        buttonWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");
                textViewStartd.setText(startd);
                textViewEndd.setText(endd);

                bShowPreviousData = false;
                getDataFromDb(startd, tEndd);
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

                drawChartRms();
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

                drawChartRms();
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

                drawChartRms();
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
                drawChartRms();
            }
            else {
                ToastUtil.showShort("no data.");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 화면 하단의 assessment result 차트 그리기
    private void drawChartRawData(int entityIndex) {

        float[] data1 = null;
        float[] data2 = Utils.getConcernDataList();
        float[] data3 = Utils.getProblemDataList();
        if( bShowPreviousData ) {    // 이전화면(ResultDiagnosisActivity)에서 전달받은 데이터를 사용할 경우
            if (analysisData == null) {
                return;
            }

            data1 = analysisData.getMeasureData1().getAxisBuf().getfFreq();
        }
        else {
            AnalysisEntity analysisEntity = analysisEntityList.get(entityIndex);
            if( analysisEntity != null ) {
                RealmList<Double> frequencyList = analysisEntity.getFrequency();
                if( frequencyList != null ) {
                    for (int i = 0; i < frequencyList.size(); i++) {
                        double frequency = frequencyList.get(i);
                        data1[i] = (float)frequency;
                    }
                }

            }
        }

        if( data1 == null ) {
            DefLog.d(TAG, "data1 is null");
            return;
        }

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();

        try {

            if (bShowChartPt1) {
                if (data1 != null) {
                    for (float v : data1) {
                        valueList1.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("pt1", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), false));
            }

            if (bShowChartPt2) {
                if (data2 != null) {
                    for (float v : data2) {
                        valueList2.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("concern", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myorange), false));
            }

            if (bShowChartPt3) {
                if (data3 != null) {
                    for (float v : data3) {
                        valueList3.add(v);
                    }
                }

                lineData.addDataSet(generateLineData("problem", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myred), false));
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

    private void initChartRms() {
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
    }

    private void initChartRawData() {
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

    private void drawChartRms() {

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

                float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
//                float rms2 = analysisData.getMeasureData2().getSvsTime().getdRms();
//                float rms3 = analysisData.getMeasureData3().getSvsTime().getdRms();

                if( bShowChartPt1 ) {
                    yDataList1.add(rms1);

                    LineDataSet lineDataSet1 = generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), true);
                    if (lineDataSet1 != null)
                        lineData.addDataSet(lineDataSet1);
                }

//                if( bShowChartPt2 ) {
//                    yDataList2.add(rms2);
//
//                    LineDataSet lineDataSet2 = generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange), true);
//                    if (lineDataSet2 != null)
//                        lineData.addDataSet(lineDataSet2);
//                }
//
//                if( bShowChartPt3 ) {
//                    yDataList3.add(rms3);
//
//                    LineDataSet lineDataSet3 = generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue), true);
//                    if (lineDataSet3 != null)
//                        lineData.addDataSet(lineDataSet3);
//                }

                rmsXDataList.add(analysisData.getMeasureData1().getCaptureTime());

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


                if( bShowChartPt1 ) {
                    LineDataSet lineDataSet1 = generateLineData("pt1", yDataList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen), true);
                    if (lineDataSet1 != null)
                        lineData.addDataSet(lineDataSet1);
                }

                if( bShowChartPt2 ) {
                    LineDataSet lineDataSet2 = generateLineData("pt2", yDataList2, ContextCompat.getColor(getBaseContext(), R.color.myorange), true);
                    if (lineDataSet2 != null)
                        lineData.addDataSet(lineDataSet2);
                }

                if( bShowChartPt3 ) {
                    LineDataSet lineDataSet3 = generateLineData("pt3", yDataList3, ContextCompat.getColor(getBaseContext(), R.color.myblue), true);
                    if (lineDataSet3 != null)
                        lineData.addDataSet(lineDataSet3);
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int i = (int) e.getX();
        drawChartRawData(i);
    }

    @Override
    public void onNothingSelected() {

    }
}
