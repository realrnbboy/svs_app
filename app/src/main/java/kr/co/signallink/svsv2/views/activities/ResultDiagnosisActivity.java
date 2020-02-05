package kr.co.signallink.svsv2.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.ResultDiagnosisListAdapter;

// added by hslee 2020-01-29
// 진단분석 결과2 화면 화면
public class ResultDiagnosisActivity extends BaseActivity {

    private static final String TAG = "ResultDiagnosisActivity";

    AnalysisData analysisData = null;

    MATRIX_2_Type matrix2;

    ListView listViewResultDiagnosis;
    ResultDiagnosisListAdapter resultDiagnosisListAdapter;
    ArrayList<CauseModel> resultDiagnosisList = new ArrayList<>();

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

        // 진단결과 값 추가
        addResultDiagnosisItem();

        listViewResultDiagnosis = findViewById(R.id.listViewResultDiagnosis);

        resultDiagnosisListAdapter = new ResultDiagnosisListAdapter(this, R.layout.list_item_result_diagnosis, resultDiagnosisList, getResources());
        listViewResultDiagnosis.setAdapter(resultDiagnosisListAdapter);

        TextView textViewMatrix4 = findViewById(R.id.textViewMatrix4);
        textViewMatrix4.setText(analysisData.resultDiagnosis[0].cause + "-" + analysisData.resultDiagnosis[0].desc);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
//                // 다음 화면으로 이동
//                Intent intent = new Intent(getBaseContext(), ResultActivity.class);
//                intent.putExtra("matrix2", matrix2);
//                intent.putExtra("analysisData", analysisData);
//                startActivity(intent);
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