package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.Locale;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.services.SendMessageHandler;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020.06.22
// report 결과1 화면
public class PipeReport2Activity extends BaseActivity {

    private static final String TAG = "PipeReport2Activity";

    TextView textViewPipeName;
    TextView textViewLocation;
    TextView textViewOperationScenario;

    private RequestManager mGlideRequestManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe_report2);

        String equipmentUuid = getIntent().getStringExtra("equipmentUuid");
        EquipmentEntity selectedEquipmentEntity = new RealmDao<>(EquipmentEntity.class).loadByUuid(equipmentUuid);

        //String pipeName = getIntent().getStringExtra("pipeName");
        String pipeName = selectedEquipmentEntity.getName();    // 현재 설정된 파이프 이름 사용
        //String pipeImage = getIntent().getStringExtra("pipeImage");
        String pipeImage = selectedEquipmentEntity.getImageUri();   // 현재 설정된 파이프 이미지 사용
        String pipeLocation = getIntent().getStringExtra("pipeLocation");
        String pipeOperationScenario = getIntent().getStringExtra("pipeOperationScenario");

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Report2");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        mGlideRequestManager = Glide.with(MyApplication.getInstance().getAppContext());

        textViewPipeName = findViewById(R.id.textViewPipeName);
        textViewLocation = findViewById(R.id.textViewLocation);
        textViewOperationScenario = findViewById(R.id.textViewOperationScenario);

        textViewPipeName.setText(pipeName);
        textViewLocation.setText(pipeLocation);
        textViewOperationScenario.setText(pipeOperationScenario);

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

        // 장비 이미지 표시
        ImageView imageViewEquipment = findViewById(R.id.imageViewEquipment);
        mGlideRequestManager.load(pipeImage).fitCenter().into(imageViewEquipment);
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
