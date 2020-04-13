package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

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
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.CriteriaModel;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.CriteriaListAdapter;
import kr.co.signallink.svsv2.views.adapters.RmsListAdapter;
import kr.co.signallink.svsv2.views.interfaces.RmsListClickListener;

import static java.lang.Math.log10;

// added by hslee 2020-03-19
// 배관 진단분석 결과1 화면
public class PipeResultActivity extends BaseActivity {

    private static final String TAG = "PipeResultActivity";

    AnalysisData analysisData = null;

    ListView listViewCriteria;
    CriteriaListAdapter criteriaListAdapter;
    ArrayList<CriteriaModel> criteriaList = new ArrayList<>();

    LineChart lineChartRawData;

    String equipmentUuid = null;

    boolean bSaved = false; // 저장여부

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe_result);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Pipe Result");

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

        equipmentUuid = intent.getStringExtra("equipmentUuid");

        TextView textViewCode = findViewById(R.id.textViewCode);
        textViewCode.setText(analysisData.pipeCode);
        TextView textViewPipeName = findViewById(R.id.textViewPipeName);
        textViewPipeName.setText(analysisData.pipeName);
        TextView textViewLocation = findViewById(R.id.textViewLocation);
        textViewLocation.setText(analysisData.pipeLocation);
        TextView textViewTagNo = findViewById(R.id.textViewTagNo);
        textViewTagNo.setText(analysisData.pipeTagNo);
        TextView textViewMedium = findViewById(R.id.textViewMedium);
        textViewMedium.setText(analysisData.pipeMedium);
        TextView textViewEtcOperatingCondition = findViewById(R.id.textViewEtcOperatingCondition);
        textViewEtcOperatingCondition.setText(analysisData.pipeEtcOperatingCondition);

        // criteria 값 추가
        addCriteriaItem();

        listViewCriteria = findViewById(R.id.listViewCriteria);

        criteriaListAdapter = new CriteriaListAdapter(this, R.layout.list_item_criteria, criteriaList, getResources());
        listViewCriteria.setAdapter(criteriaListAdapter);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 다음 화면으로 이동
                Intent intent =new Intent(getBaseContext(), PipeRecordManagerActivity.class);

                try {
                    String endd = Utils.getCurrentTime("yyyy-MM-dd");
                    String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                    String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                    Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                    long startLong = startDate.getTime();
                    long endLong = endDate.getTime();

                    Realm realm = Realm.getDefaultInstance();

                    RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                            .equalTo("type", 2) // 1 rms, 2 frequency
                            .greaterThanOrEqualTo("created", startLong)
                            .lessThanOrEqualTo("created", endLong)
                            .equalTo("equipmentUuid", equipmentUuid)
                            .findAll()
                            //.sort("created", Sort.DESCENDING);
                            .sort("created", Sort.ASCENDING);

                    ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                    for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                        RmsModel rmsModel = new RmsModel();
                        rmsModel.setRms1(analysisEntity.getRms1());
                        rmsModel.setCreated(analysisEntity.getCreated());

                        float [] newFreq = new float[analysisEntity.getFrequency().size()];
                        for( int i = 0; i<analysisEntity.getFrequency().size(); i++ ) {
                            newFreq[i] = analysisEntity.getFrequency().get(i).floatValue();
                        }
                        rmsModel.setFrequency(newFreq);

                        rmsModelList.add(rmsModel);
                    }

                    intent.putExtra("previousRmsModelList", rmsModelList);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                intent.putExtra("analysisData", analysisData);
                intent.putExtra("equipmentUuid", equipmentUuid);
                startActivity(intent);
            }
        });

        Button buttonSaveDb = findViewById(R.id.buttonSaveDb);
        buttonSaveDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // db에 진단 결과 데이터 저장
                if( bSaved ) {
                    ToastUtil.showShort("already saved.");
                }
                else {
                    save();
                }
            }
        });

        Button buttonSaveCsv = findViewById(R.id.buttonSaveCsv);
        buttonSaveCsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                float [] data1 = analysisData.getMeasureData1().getAxisBuf().getfFreq();
                float [] data2 = Utils.getConcernDataList();
                float [] data3 = Utils.getProblemDataList();

                // csv로 raw data 데이터 저장
                String fileName = Utils.createCsv("pipe", new String [] {"PT1", "concern", "problem"}, data1, data2, data3);
                if( fileName == null ) {
                    ToastUtil.showShort("failed to save csv.");
                }
                else {
                    ToastUtil.showLong("saved as \"/SVSdata/csv/pipe/" + fileName + "\"");
                }

            }
        });
    }

    // db에 진단 결과 데이터 저장
    void save() {
        DatabaseUtil.transaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                int id = 0;
                Number currentNo = realm.where(AnalysisEntity.class).max("id");

                if (currentNo == null) {    // index 값 증가
                    id = 1;
                } else {
                    id = currentNo.intValue() + 1;
                }

                try {
                    AnalysisEntity analysisEntity = new AnalysisEntity();
                    analysisEntity.setType(2);  // 1 rms, 2 frequency
                    analysisEntity.setId(id);
                    analysisEntity.setEquipmentUuid(equipmentUuid);

                    long createdLong = analysisData.getMeasureData1().getCaptureTime().getTime();   // 측정된 시간으로 저장하기
                    analysisEntity.setCreated(createdLong);

                    float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
                    analysisEntity.setRms1(rms1);

                    RealmList<Double> frequency = new RealmList<>();
                    float[] frequencyFloat = analysisData.getMeasureData1().getAxisBuf().getfFreq();
                    if( frequencyFloat != null ) {
                        for( int i = 0; i<frequencyFloat.length; i++ ) {
                            frequency.add((double)frequencyFloat[i]);
                        }
                    }

                    analysisEntity.setFrequency(frequency);

                    AnalysisEntity result = realm.copyToRealmOrUpdate(analysisEntity);
                    if( result != null ) {
                        ToastUtil.showShort("save success.");
                        bSaved = true;
                    }
                    else {
                        ToastUtil.showShort("failed to save.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // criteria 값 추가
    private void addCriteriaItem() {
        CriteriaModel criteriaModel1 = new CriteriaModel();
        CriteriaModel criteriaModel2 = new CriteriaModel();
        CriteriaModel criteriaModel3 = new CriteriaModel();

        criteriaModel1.setStatus("PROBLEM");
        criteriaModel2.setStatus("CONCERN");
        criteriaModel3.setStatus("ACCEPTABLE");

        criteriaModel1.setCriteria("High risk of fatigue damage");
        criteriaModel2.setCriteria("Potential of fatigue damage");
        criteriaModel3.setCriteria("Allowable range");

        criteriaList.add(criteriaModel1);
        criteriaList.add(criteriaModel2);
        criteriaList.add(criteriaModel3);
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


    private void drawChart() {

        if( analysisData == null ) {
            return;
        }

        float[] data1 = analysisData.getMeasureData1().getAxisBuf().getfFreq();
        float[] data2 = Utils.getConcernDataList();
        float[] data3 = Utils.getProblemDataList();

        LineData lineData = new LineData();

        ArrayList<Float> valueList1 = new ArrayList<>();
        ArrayList<Float> valueList2 = new ArrayList<>();
        ArrayList<Float> valueList3 = new ArrayList<>();

        try {

            if (data1 != null) {
                for (float v : data1) {
                    valueList1.add(v);
                }
            }

            lineData.addDataSet(generateLineData("pt1", valueList1, ContextCompat.getColor(getBaseContext(), R.color.mygreen)));

            if (data2 != null) {
                for (float v : data2) {
                    valueList2.add(v);
                }
            }

            lineData.addDataSet(generateLineData("concern", valueList2, ContextCompat.getColor(getBaseContext(), R.color.myorange)));

            if (data3 != null) {
                for (float v : data3) {
                    valueList3.add(v);
                }
            }

            lineData.addDataSet(generateLineData("problem", valueList3, ContextCompat.getColor(getBaseContext(), R.color.myred)));

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
