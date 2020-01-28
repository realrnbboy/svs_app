package kr.co.signallink.svsv2.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.PresetEntity;
import kr.co.signallink.svsv2.dto.PresetData;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

// added by hslee 2020-01-15
// 진단 분석 결과 화면
public class AnalysisResultActivity extends BaseActivity {

    private static final String TAG = "AnalysisResultActivity";

    PresetData presetData = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_result);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Analysis Result");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김


        initView();

    }

    void initView() {

        Intent intent = getIntent();

        // PresetListActivity 에서 전달받은 데이터 가져오기
        presetData = (PresetData)intent.getSerializableExtra("presetData");

        if( presetData == null ) {
            ToastUtil.showShort("preset data is null");
            return;
        }

        TextView textViewCode = findViewById(R.id.textViewCode);

        int iCode = presetData.getCode();
        String code = "";
        if( iCode == 0 ) code = "ANSI HI 9.6.4";
        else if( iCode == 1 ) code = "API 610";
        else if( iCode == 2 ) code = "ISO 10816 Cat.1";
        else if( iCode == 3 ) code = "ISO 10816 Cat.2";
        else if( iCode == 4 ) code = "Project VIB Spec";

        textViewCode.setText(code);
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
