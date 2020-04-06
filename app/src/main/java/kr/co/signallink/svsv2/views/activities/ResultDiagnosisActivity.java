package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.DiagnosisImageModel;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.ResultDiagnosisImageAdapter;
import kr.co.signallink.svsv2.views.adapters.ResultDiagnosisListAdapter;

// added by hslee 2020-01-29
// 진단분석 결과2 화면 화면
public class ResultDiagnosisActivity extends BaseActivity {

    private static final String TAG = "ResultDiagnosisActivity";
    String equipmentUuid = null;

    AnalysisData analysisData = null;

    MATRIX_2_Type matrix2;

    ListView listViewResultDiagnosis;
    ListView listViewDiagnosisImage;
    ResultDiagnosisListAdapter resultDiagnosisListAdapter;
    ResultDiagnosisImageAdapter resultDiagnosisImageAdapter;
    ArrayList<CauseModel> resultDiagnosisList = new ArrayList<>();
    ArrayList<DiagnosisImageModel> resultDiagnosisImageList = new ArrayList<>();

    boolean bSaved = false; // 저장여부

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_diagnosis);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Diagnosis");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        initView();
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

        // 진단결과 값 추가
        addResultDiagnosisItem();

        listViewResultDiagnosis = findViewById(R.id.listViewCause);

        resultDiagnosisListAdapter = new ResultDiagnosisListAdapter(this, R.layout.list_item_result_diagnosis, resultDiagnosisList, getResources());
        listViewResultDiagnosis.setAdapter(resultDiagnosisListAdapter);

        TextView textViewMatrix4 = findViewById(R.id.textViewMatrix4);
        textViewMatrix4.setText(analysisData.resultDiagnosis[0].cause + "-" + analysisData.resultDiagnosis[0].desc);

        // cause 종류 확인
        listViewDiagnosisImage = findViewById(R.id.listViewDiagnosisImage);

        addResultDiagnosisImageList();  // 해당 이미지가 있으면 이미지 추가

        resultDiagnosisImageAdapter = new ResultDiagnosisImageAdapter(this, R.layout.list_item_result_diagnosis_image, resultDiagnosisImageList);
        listViewDiagnosisImage.setAdapter(resultDiagnosisImageAdapter);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), RecordManagerActivity.class);
                intent.putExtra("matrix2", matrix2);
                intent.putExtra("analysisData", analysisData);
                intent.putExtra("equipmentUuid", equipmentUuid);
                startActivity(intent);
            }
        });
    }

    // 진단 결과에 맞는 이미지가 있으면 이미지 목록에 추가
    void addResultDiagnosisImageList() {
        try {
            AssetManager assetMgr = getAssets();
            String[] list = assetMgr.list("");

            // 해당 진단 결과 번호로 시작하는 이름을 가진 파일은 이미지 목록에 추가
            for ( String fileName : list ) {
                String [] fileNameArr = fileName.split("\\.");    // ex) 7.cavitation4.png
                if( fileName.length() > 1 && fileNameArr.length == 3 ) {
                    if( fileNameArr[0].equals(String.valueOf(analysisData.resultDiagnosis[0].no)) ) {
                        DiagnosisImageModel diagnosisImageModel = new DiagnosisImageModel();
                        diagnosisImageModel.setFileName(fileName);

                        resultDiagnosisImageList.add(diagnosisImageModel);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    analysisEntity.setId(id);
                    analysisEntity.setEquipmentUuid(equipmentUuid);

                    //String tCreated = Utils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                    //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //Date created = simpleDateFormat.parse(tCreated);
                    //long createdLong = created.getTime(); // 현재 시간으로 저장하기
                    long createdLong = analysisData.getMeasureData1().getCaptureTime().getTime();   // 측정된 시간으로 저장하기
                    analysisEntity.setCreated(createdLong);

                    float rms1 = analysisData.getMeasureData1().getSvsTime().getdRms();
                    float rms2 = analysisData.getMeasureData2().getSvsTime().getdRms();
                    float rms3 = analysisData.getMeasureData3().getSvsTime().getdRms();

                    analysisEntity.setRms1(rms1);
                    analysisEntity.setRms2(rms2);
                    analysisEntity.setRms3(rms3);

                    RealmList<String> cause = new RealmList<>();
                    RealmList<String> causeDesc = new RealmList<>();
                    RealmList<Double> rank = new RealmList<>();
                    RealmList<Double> ratio = new RealmList<>();

                    for( int i = 0; i<analysisData.resultDiagnosis.length && i < 5; i++ ) {

                        cause.add(analysisData.resultDiagnosis[i].cause);
                        causeDesc.add(analysisData.resultDiagnosis[i].desc);
                        rank.add(analysisData.resultDiagnosis[i].rank);
                        ratio.add(analysisData.resultDiagnosis[i].ratio);
                    }

                    analysisEntity.setCause(cause);
                    analysisEntity.setCauseDesc(causeDesc);
                    analysisEntity.setRank(rank);
                    analysisEntity.setRatio(ratio);

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

    void addResultDiagnosisItem() {

        for( int i = 0; i<analysisData.resultDiagnosis.length && i < 5; i++ ) {

            CauseModel model = new CauseModel();

            String cause = analysisData.resultDiagnosis[i].cause + "-" + analysisData.resultDiagnosis[i].desc;
            model.setCause(cause);
            model.setRatio(analysisData.resultDiagnosis[i].ratio);

            resultDiagnosisList.add(model);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

}
