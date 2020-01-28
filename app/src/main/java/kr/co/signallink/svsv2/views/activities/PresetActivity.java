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
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-01-15
// preset 추가 및 수정 화면
public class PresetActivity extends BaseActivity {

    private static final String TAG = "PresetActivity";

    String bModeCreate = "1"; // create or update

    PresetData presetData = null;

    ArrayList arrayListEquipmentCode = new ArrayList<>();
    ArrayList arrayListLineFrequency = new ArrayList<>();
    ArrayList arrayListEquipmentType = new ArrayList<>();
    ArrayList arrayListBearingType = new ArrayList<>();

    EditText editTextPresetName;
    EditText editTextSiteCode;
    EditText editTextEquipmentName;
    EditText editTextInputPower;
    EditText editTextEquipmentRpm;
    EditText editTextBladeVane;
    EditText editTextNoOfBalls;
    EditText editTextTagNo;
    EditText editTextPitchDiameter;
    EditText editTextBallDiameter;
    EditText editTextRps;
    EditText editTextContactAngle;
    EditText editTextProjectVibSpec;

    Spinner spinnerEquipmentCode;
    Spinner spinnerLineFrequency;
    Spinner spinnerEquipmentType;
    Spinner spinnerBearingType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Preset Add");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

        Intent intent = getIntent();

        editTextPresetName = findViewById(R.id.editTextPresetName);
        editTextSiteCode = findViewById(R.id.editTextSiteCode);
        editTextEquipmentName = findViewById(R.id.editTextEquipmentName);
        editTextInputPower = findViewById(R.id.editTextInputPower);
        editTextEquipmentRpm = findViewById(R.id.editTextEquipmentRpm);
        editTextBladeVane = findViewById(R.id.editTextBladeVane);
        editTextNoOfBalls = findViewById(R.id.editTextNoOfBalls);
        editTextTagNo = findViewById(R.id.editTextTagNo);
        editTextPitchDiameter = findViewById(R.id.editTextPitchDiameter);
        editTextBallDiameter = findViewById(R.id.editTextBallDiameter);
        editTextRps = findViewById(R.id.editTextRps);
        editTextContactAngle = findViewById(R.id.editTextContactAngle);
        editTextProjectVibSpec = findViewById(R.id.editTextProjectVibSpec);

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSave();
            }
        });

        spinnerEquipmentCode = findViewById(R.id.spinnerEquipmentCode);
        spinnerLineFrequency = findViewById(R.id.spinnerLineFrequency);
        spinnerEquipmentType = findViewById(R.id.spinnerEquipmentType);
        spinnerBearingType = findViewById(R.id.spinnerBearingType);

        initDefaultValue();

        ArrayAdapter arrayAdapterEquipmentCode = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentCode);
        ArrayAdapter arrayAdapterLineFrequency = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListLineFrequency);
        ArrayAdapter arrayAdapterEquipmentType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentType);
        ArrayAdapter arrayAdapterBearingType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListBearingType);

        spinnerEquipmentCode.setAdapter(arrayAdapterEquipmentCode);
        spinnerLineFrequency.setAdapter(arrayAdapterLineFrequency);
        spinnerEquipmentType.setAdapter(arrayAdapterEquipmentType);
        spinnerBearingType.setAdapter(arrayAdapterBearingType);

        bModeCreate = intent.getStringExtra("bModeCreate");
        if( "0".equals(bModeCreate) ) {    // update 모드이면 이름 입력 부분 숨김
            //findViewById(R.id.linearLayoutPresetName).setVisibility(View.GONE);
            toolbarTitle.setText("Preset Update");

            // PresetListActivity 에서 전달받은 데이터 가져오기
            presetData = (PresetData)intent.getSerializableExtra("presetData");
            if( presetData != null ) {
                editTextPresetName.setText(presetData.getName());
                editTextSiteCode.setText(presetData.getSiteCode());
                editTextEquipmentName.setText(presetData.getEquipmentName());
                editTextInputPower.setText(String.valueOf(presetData.getInputPower()));
                editTextEquipmentRpm.setText(String.valueOf(presetData.getRpm()));
                editTextBladeVane.setText(String.valueOf(presetData.getBladeCount()));
                editTextNoOfBalls.setText(String.valueOf(presetData.getBallCount()));
                editTextTagNo.setText(presetData.getTagNo());
                editTextPitchDiameter.setText(String.valueOf(presetData.getPitchDiameter()));
                editTextBallDiameter.setText(String.valueOf(presetData.getBallDiameter()));
                editTextRps.setText(String.valueOf(presetData.getRps()));
                editTextContactAngle.setText(String.valueOf(presetData.getContactAngle()));
                editTextProjectVibSpec.setText(String.valueOf(presetData.getProjectVibSpec()));

                spinnerEquipmentCode.setSelection(presetData.getCode());
                spinnerLineFrequency.setSelection(presetData.getLineFreq());
                spinnerEquipmentType.setSelection(presetData.getEquipmentType());
                spinnerBearingType.setSelection(presetData.getBearingType());
            }
            else {
                ToastUtil.showShort("failed to preset data load");
            }
        }
    }

    void initDefaultValue() {

        final String strDate = Utils.getCurrentTime("dd-MMM-yyyy HH:mm", new Locale("en", "US"));

        TextView textViewTime = findViewById(R.id.textViewTime);
        textViewTime.setText(strDate);

        arrayListEquipmentCode.add("ANSI HI 9.6.4");
        arrayListEquipmentCode.add("API 610");
        arrayListEquipmentCode.add("ISO 10816 Cat.1");
        arrayListEquipmentCode.add("ISO 10816 Cat.2");
        arrayListEquipmentCode.add("Project VIB Spec");

        arrayListLineFrequency.add("50 Hz");
        arrayListLineFrequency.add("60 Hz");

        arrayListEquipmentType.add("[1] Horizontal (BB&OH)");
        arrayListEquipmentType.add("[2] Vertical (VS)");
        arrayListEquipmentType.add("[3] ETC");

        arrayListBearingType.add("[1] Ball");
        arrayListBearingType.add("[2] Roller");
        arrayListBearingType.add("[3] Journal");
        arrayListBearingType.add("[4] ETC");
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

    //저장전에 체크하는 것들
    private boolean validateForm() {

        String siteCode = editTextSiteCode.getText().toString().trim();
        if(siteCode.isEmpty()){
            ToastUtil.showShort("site code is empty");
            return false;
        }

        String presetName = editTextPresetName.getText().toString().trim();
        if(presetName.isEmpty()){
            ToastUtil.showShort("preset name is empty");
            return false;
        }

        String equipmentName = editTextEquipmentName.getText().toString().trim();
        if(equipmentName.isEmpty()){
            ToastUtil.showShort("equipment name is empty");
            return false;
        }

        String inputPower = editTextInputPower.getText().toString().trim();
        if(inputPower.isEmpty()){
            ToastUtil.showShort("input power is empty");
            return false;
        }

        String equipmentRpm = editTextEquipmentRpm.getText().toString().trim();
        if(equipmentRpm.isEmpty()){
            ToastUtil.showShort("equipment rpm is empty");
            return false;
        }

        String bladeVane = editTextBladeVane.getText().toString().trim();
        if(bladeVane.isEmpty()){
            ToastUtil.showShort("blade/vane is empty");
            return false;
        }

        String noOfBalls = editTextNoOfBalls.getText().toString().trim();
        if(noOfBalls.isEmpty()){
            ToastUtil.showShort("No. of Balls is empty");
            return false;
        }

        String tagNo = editTextTagNo.getText().toString().trim();
        if(tagNo.isEmpty()){
            ToastUtil.showShort("tag No is empty");
            return false;
        }

        String pitchDiameter = editTextPitchDiameter.getText().toString().trim();
        if(pitchDiameter.isEmpty()){
            ToastUtil.showShort("pitch diameter is empty");
            return false;
        }

        String ballDiameter = editTextBallDiameter.getText().toString().trim();
        if(ballDiameter.isEmpty()){
            ToastUtil.showShort("ball diameter is empty");
            return false;
        }

        String rps = editTextRps.getText().toString().trim();
        if(rps.isEmpty()){
            ToastUtil.showShort("rps is empty");
            return false;
        }

        String contactAngle = editTextContactAngle.getText().toString().trim();
        if(contactAngle.isEmpty()){
            ToastUtil.showShort("contact angle is empty");
            return false;
        }

        return true;
    }


    private void save() {

        if(validateForm()) {

            //Preset 객체 만들기
            final PresetEntity presetEntity = new PresetEntity();

            presetEntity.setName(editTextPresetName.getText().toString());

            int code = (int)spinnerEquipmentCode.getSelectedItemId();
//            if( spinnerEquipmentCode.getSelectedItemId() == 0 ) code = "ANSI HI 9.6.4";
//            else if( spinnerEquipmentCode.getSelectedItemId() == 1 ) code = "API 610";
//            else if( spinnerEquipmentCode.getSelectedItemId() == 2 ) code = "ISO 10816 Cat.1";
//            else if( spinnerEquipmentCode.getSelectedItemId() == 3 ) code = "ISO 10816 Cat.2";
//            else if( spinnerEquipmentCode.getSelectedItemId() == 4 ) code = "Project VIB Spec";

            int equipmentType = (int)spinnerEquipmentType.getSelectedItemId();
//            if( spinnerEquipmentType.getSelectedItemId() == 0 ) equipmentType = "Horizontal (BB&OH)";
//            else if( spinnerEquipmentType.getSelectedItemId() == 1 ) equipmentType = "Vertical (VS)";
//            else if( spinnerEquipmentType.getSelectedItemId() == 2 ) equipmentType = "ETC";

            int bearingType = (int)spinnerBearingType.getSelectedItemId();
//            if( spinnerBearingType.getSelectedItemId() == 0 ) bearingType = "Ball";
//            else if( spinnerBearingType.getSelectedItemId() == 1 ) bearingType = "Roller";
//            else if( spinnerBearingType.getSelectedItemId() == 2 ) bearingType = "Journal";
//            else if( spinnerBearingType.getSelectedItemId() == 3 ) bearingType = "ETC";

            int lineFrequency = (int)spinnerLineFrequency.getSelectedItemId();// == 0 ? 50 : 60;

            int projectVibSpec = Integer.parseInt(editTextProjectVibSpec.getText().toString());
            String siteCode = editTextSiteCode.getText().toString();
            String equipmentName = editTextEquipmentName.getText().toString();
            String tagNo = editTextTagNo.getText().toString();
            int inputPower = Integer.parseInt(editTextInputPower.getText().toString());
            int equipmentRpm = Integer.parseInt(editTextEquipmentRpm.getText().toString());
            int bladeVane = Integer.parseInt(editTextBladeVane.getText().toString());
            int noOfBalls = Integer.parseInt(editTextNoOfBalls.getText().toString());
            int pitchDiameter = Integer.parseInt(editTextPitchDiameter.getText().toString());
            int ballDiameter = Integer.parseInt(editTextBallDiameter.getText().toString());
            int rps = Integer.parseInt(editTextRps.getText().toString());
            int contactAngle = Integer.parseInt(editTextContactAngle.getText().toString());

            presetEntity.setCode(code);
            presetEntity.setEquipmentType(equipmentType);
            presetEntity.setBearingType(bearingType);
            presetEntity.setLineFreq(lineFrequency);
            presetEntity.setProjectVibSpec(projectVibSpec);
            presetEntity.setSiteCode(siteCode);
            presetEntity.setEquipmentName(equipmentName);
            presetEntity.setTagNo(tagNo);
            presetEntity.setInputPower(inputPower);
            presetEntity.setRpm(equipmentRpm);
            presetEntity.setBladeCount(bladeVane);
            presetEntity.setBallCount(noOfBalls);
            presetEntity.setPitchDiameter(pitchDiameter);
            presetEntity.setBallDiameter(ballDiameter);
            presetEntity.setRps(rps);
            presetEntity.setContactAngle(contactAngle);


            //기록
            DatabaseUtil.transaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    int no = 0;
                    if( "0".equals(bModeCreate) ) {
                        no = presetData.getNo();
                    }
                    else {  // 추가일 경우 index값 증가
                        Number currentNo = realm.where(PresetEntity.class).max("no");

                        if (currentNo == null) {
                            no = 1;
                        } else {
                            no = currentNo.intValue() + 1;
                        }
                    }

                    presetEntity.setNo(no);
                    realm.copyToRealmOrUpdate(presetEntity);
                }
            });

            //토스트
            ToastUtil.showShort("Save Success");

            //화면 종료
            finish();
        }
    }


    private void dialogSave(){

        DialogUtil.yesNo(this,
                getResources().getString(R.string.update_screen),
                "Do you want to save?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        save();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }


}
