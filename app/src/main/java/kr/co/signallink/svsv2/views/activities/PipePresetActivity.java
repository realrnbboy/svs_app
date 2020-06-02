package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.VARIABLES_1_Type;
import kr.co.signallink.svsv2.server.SendPost;
import kr.co.signallink.svsv2.services.SendMessageHandler;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-03-18
// pipe preset 추가 및 수정 화면
public class PipePresetActivity extends BaseActivity {

    private static final String TAG = "PipePresetActivity";

    EditText editTextSiteCode;
    EditText editTextPumpCode;
    EditText editTextProjectVibSpec;
    EditText editTextPipeName;
    EditText editTextLocation;
    EditText editTextPipeNo;
    EditText editTextMedium;
    EditText editTextEtcOperatingCondition;

    String equipmentUuid = null;
    SendMessageHandler handler;

    float [] measuredFreq1 = null;  // measureActivity에서 측정된 데이터
    AnalysisData m_analysisData = null;

    boolean bRemeasure = true;  // measureActivity 화면에서 다시 측정해야 할지 여부, 값을 변경하면 측정을 다시해야 함

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe_preset);

        equipmentUuid = getIntent().getStringExtra("equipmentUuid");

        handler = new SendMessageHandler(this);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Pipe Input");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김


        final String strDate = Utils.getCurrentTime("dd-MMM-yyyy HH:mm", new Locale("en", "US"));

        TextView textViewTime = findViewById(R.id.textViewTime);
        textViewTime.setText(strDate);

        editTextSiteCode = findViewById(R.id.editTextSiteCode);
        editTextPumpCode = findViewById(R.id.editTextPumpCode);
        editTextProjectVibSpec = findViewById(R.id.editTextProjectVibSpec);
        editTextPipeName = findViewById(R.id.editTextPipeName);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextPipeNo = findViewById(R.id.editTextPipeNo);
        editTextMedium = findViewById(R.id.editTextMedium);
        editTextEtcOperatingCondition = findViewById(R.id.editTextEtcOperatingCondition);

        editTextSiteCode.addTextChangedListener(textWatcherInput);
        editTextPumpCode.addTextChangedListener(textWatcherInput);
        editTextProjectVibSpec.addTextChangedListener(textWatcherInput);
        editTextPipeName.addTextChangedListener(textWatcherInput);
        editTextLocation.addTextChangedListener(textWatcherInput);
        editTextPipeNo.addTextChangedListener(textWatcherInput);
        editTextMedium.addTextChangedListener(textWatcherInput);
        editTextEtcOperatingCondition.addTextChangedListener(textWatcherInput);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialogSave();
                if( validateForm() ) {

                    // 수정된 preset 확인 및 세팅
                    AnalysisData analysisData;
                    if( bRemeasure ) {  // 다시 측정할 경우, 세팅값 다시 계산
                        analysisData = setAnalysisData();
                    }
                    else {  // 아니면 기존 값 사용
                        analysisData = m_analysisData;
                    }

                    // 다음 화면으로 이동
                    Intent intent = new Intent(getBaseContext(), PipeMeasureActivity.class);
                    intent.putExtra("analysisData", analysisData);
                    intent.putExtra("equipmentUuid", equipmentUuid);
                    intent.putExtra("measuredFreq1", measuredFreq1);
                    startActivityForResult(intent, DefConstant.REQUEST_MEASUREACTIVITY_RESULT);
                    //startActivity(intent);
                }
            }
        });
    }

    // measureactivity로 전달할 데이터 구성
    AnalysisData setAnalysisData() {
        AnalysisData analysisData = new AnalysisData();

        String siteCode = editTextSiteCode.getText().toString();
        String pumpCode = editTextPumpCode.getText().toString();
        String projectVibSpec = editTextProjectVibSpec.getText().toString();
        String pipeName = editTextPipeName.getText().toString();
        String location = editTextLocation.getText().toString();
        String pipeNo = editTextPipeNo.getText().toString();
        String medium = editTextMedium.getText().toString();
        String etcOperatingCondition = editTextEtcOperatingCondition.getText().toString();


        analysisData.pipeSiteCode = siteCode;
        analysisData.pipePumpCode = pumpCode;
        analysisData.pipeProjectVibSpec = projectVibSpec;
        analysisData.pipeName = pipeName;
        analysisData.pipeLocation = location;
        analysisData.pipeNo = pipeNo;
        analysisData.pipeMedium = medium;
        analysisData.pipeEtcOperatingCondition = etcOperatingCondition;

        return analysisData;
    }

    // 값 변경하기 전의 데이터로 변경
    void setPreviousAnalysisData() {
        if( m_analysisData == null )
            m_analysisData = setAnalysisData();

        editTextSiteCode.setText(String.valueOf(m_analysisData.pipeSiteCode));
        editTextPumpCode.setText(String.valueOf(m_analysisData.pipePumpCode));
        editTextProjectVibSpec.setText(String.valueOf(m_analysisData.pipeProjectVibSpec));
        editTextPipeName.setText(String.valueOf(m_analysisData.pipeName));
        editTextLocation.setText(String.valueOf(m_analysisData.pipeLocation));
        editTextPipeNo.setText(String.valueOf(m_analysisData.pipeNo));
        editTextMedium.setText(String.valueOf(m_analysisData.pipeMedium));
        editTextEtcOperatingCondition.setText(String.valueOf(m_analysisData.pipeEtcOperatingCondition));
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

    // 값이 정상적으로 들어가 있는지 확인
    private boolean validateForm() {

        String siteCode = editTextSiteCode.getText().toString().trim();
        if(siteCode.isEmpty()){
            editTextSiteCode.requestFocus();
            ToastUtil.showShort("code is empty");
            return false;
        }

        String pumpCode = editTextPumpCode.getText().toString().trim();
        if(pumpCode.isEmpty()){
            editTextPumpCode.requestFocus();
            ToastUtil.showShort("code is empty");
            return false;
        }

        String pipeName = editTextPipeName.getText().toString().trim();
        if(pipeName.isEmpty()){
            editTextPipeName.requestFocus();
            ToastUtil.showShort("pipe name is empty");
            return false;
        }

        String pipeLocation = editTextLocation.getText().toString().trim();
        if(pipeLocation.isEmpty()){
            editTextLocation.requestFocus();
            ToastUtil.showShort("pipe location is empty");
            return false;
        }

        String pipeNo = editTextPipeNo.getText().toString().trim();
        if(pipeNo.isEmpty()){
            editTextPipeNo.requestFocus();
            ToastUtil.showShort("tag no is empty");
            return false;
        }

        String pipeMedium = editTextMedium.getText().toString().trim();
        if(pipeMedium.isEmpty()){
            editTextMedium.requestFocus();
            ToastUtil.showShort("medium is empty");
            return false;
        }

        String pipeEtcOperatingCondition = editTextEtcOperatingCondition.getText().toString().trim();
        if(pipeEtcOperatingCondition.isEmpty()){
            editTextEtcOperatingCondition.requestFocus();
            ToastUtil.showShort("etc operating condition is empty");
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == DefConstant.REQUEST_MEASUREACTIVITY_RESULT) {
                measuredFreq1 = (float[]) data.getSerializableExtra("measuredFreq1");

                m_analysisData = (AnalysisData) data.getSerializableExtra("analysisData");

                if( measuredFreq1 == null ) { // 측정된 데이터가 없는 경우
                    bRemeasure = true;
                }
                else {  // 측정된 데이터가 있는 경우
                    bRemeasure = false;
                }
            }
        }
    }

    TextWatcher textWatcherInput = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

            valueChanged(null);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
        }
    };

    public void valueChanged(DialogInterface.OnClickListener cancel) {

        // 기존에 측정한 데이터가 있고, 재측정을 안해도 되는 상태인데 값을 변경한 경우
        // 같은 창이 두 번 안뜬 경우
        if( measuredFreq1 != null && !bRemeasure ) {

            DialogUtil.yesNo(PipePresetActivity.this,
                    "info",
                    "If you change the value you will have to re-measure.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            measuredFreq1 = null;

                            m_analysisData = null;

                            bRemeasure = true;
                        }
                    },
                    cancel
            );
        }
    }
}
