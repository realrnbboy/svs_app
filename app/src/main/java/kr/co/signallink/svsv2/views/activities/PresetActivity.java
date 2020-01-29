package kr.co.signallink.svsv2.views.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import io.realm.Realm;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.PresetEntity;
import kr.co.signallink.svsv2.dto.PresetData;
import kr.co.signallink.svsv2.model.DIAGNOSIS_DATA_Type;
import kr.co.signallink.svsv2.model.MATRIX_2_Type;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.server.SendPost;
import kr.co.signallink.svsv2.services.DiagnosisInfo;
import kr.co.signallink.svsv2.services.SendMessageHandler;
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

    ArrayList arrayListPreset = new ArrayList<>();
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

    Spinner spinnerPreset;
    Spinner spinnerEquipmentCode;
    Spinner spinnerLineFrequency;
    Spinner spinnerEquipmentType;
    Spinner spinnerBearingType;

    MainData mainData;

    SendMessageHandler handler;

    private String [][] preset;
    public boolean bExistPreset = false;
    public boolean bResponsePreset = false;

    ArrayAdapter arrayAdapterPreset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        handler = new SendMessageHandler(this);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("INPUT");

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

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialogSave();

                // 수정된 preset 확인 및 세팅

                //matrix2 계산
                MATRIX_2_Type matrix2 = makeMatrix2();

                // 다음 화면으로 이동
            }
        });

        spinnerPreset = findViewById(R.id.spinnerPreset);
        spinnerEquipmentCode = findViewById(R.id.spinnerEquipmentCode);
        spinnerLineFrequency = findViewById(R.id.spinnerLineFrequency);
        spinnerEquipmentType = findViewById(R.id.spinnerEquipmentType);
        spinnerBearingType = findViewById(R.id.spinnerBearingType);

        initDefaultValue();

        arrayAdapterPreset = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListPreset);
        ArrayAdapter arrayAdapterEquipmentCode = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentCode);
        ArrayAdapter arrayAdapterLineFrequency = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListLineFrequency);
        ArrayAdapter arrayAdapterEquipmentType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentType);
        ArrayAdapter arrayAdapterBearingType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListBearingType);

        spinnerPreset.setAdapter(arrayAdapterPreset);
        spinnerEquipmentCode.setAdapter(arrayAdapterEquipmentCode);
        spinnerLineFrequency.setAdapter(arrayAdapterLineFrequency);
        spinnerEquipmentType.setAdapter(arrayAdapterEquipmentType);
        spinnerBearingType.setAdapter(arrayAdapterBearingType);

        spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setViewData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

//        bModeCreate = intent.getStringExtra("bModeCreate");
//        if( "0".equals(bModeCreate) ) {    // update 모드이면 이름 입력 부분 숨김
//            //findViewById(R.id.linearLayoutPresetName).setVisibility(View.GONE);
//            toolbarTitle.setText("Preset Update");
//
//            // PresetListActivity 에서 전달받은 데이터 가져오기
//            presetData = (PresetData)intent.getSerializableExtra("presetData");
//            if( presetData != null ) {
//                editTextPresetName.setText(presetData.getName());
//                editTextSiteCode.setText(presetData.getSiteCode());
//                editTextEquipmentName.setText(presetData.getEquipmentName());
//                editTextInputPower.setText(String.valueOf(presetData.getInputPower()));
//                editTextEquipmentRpm.setText(String.valueOf(presetData.getRpm()));
//                editTextBladeVane.setText(String.valueOf(presetData.getBladeCount()));
//                editTextNoOfBalls.setText(String.valueOf(presetData.getBallCount()));
//                editTextTagNo.setText(presetData.getTagNo());
//                editTextPitchDiameter.setText(String.valueOf(presetData.getPitchDiameter()));
//                editTextBallDiameter.setText(String.valueOf(presetData.getBallDiameter()));
//                editTextRps.setText(String.valueOf(presetData.getRps()));
//                editTextContactAngle.setText(String.valueOf(presetData.getContactAngle()));
//                editTextProjectVibSpec.setText(String.valueOf(presetData.getProjectVibSpec()));
//
//                spinnerEquipmentCode.setSelection(presetData.getCode());
//                spinnerLineFrequency.setSelection(presetData.getLineFreq());
//                spinnerEquipmentType.setSelection(presetData.getEquipmentType());
//                spinnerBearingType.setSelection(presetData.getBearingType());
//            }
//            else {
//                ToastUtil.showShort("failed to preset data load");
//            }
//        }

        // 서버에서 preset 정보 불러오기
        getPresetFromServer();

        mainData = new MainData(this);
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

    public MATRIX_2_Type makeMatrix2() {

        DiagnosisInfo diagnosis = new DiagnosisInfo(mainData);

        DIAGNOSIS_DATA_Type[] rawData = mainData.fnGetRawDatas();

        MATRIX_2_Type result = diagnosis.fnMakeMatrix2(rawData[0], rawData[1], rawData[2]);

        return result;
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

    // 웹서버로 preset 요청
    void getPresetFromServer() {

        SendPost sendPost = new SendPost(DefConstant.URL_TYPE_GET_PRESET);

        sendPost.setHandler(handler);
        sendPost.start();
    }

    // 서버에서 전달받은 preset 파싱
    public void parsePreset(String jsonString) {

        bResponsePreset = true;

        if( jsonString == null || "".equals(jsonString) ) {
            ToastUtil.showShort("failed get preset from server. use in local storage");

            preset = Utils.getStringArrayPref("preset");
            if( preset == null ) {   // 앱 설치 후 최초 실행인 경우 호출됨.
                preset = new String[][]{
                        {"1", "Charge Pump #1", "1", "1", "HDO", "Charge Pump", "PP-L25-51", "980", "1", "0", "3579", "5", "3", "0", "0", "0", "0", "0"},
                        {"2", "Charge Pump #2", "2", "2", "HDO", "Charge Pump", "PP-L25-01", "1080", "1", "0", "3600", "8", "2", "4", "200", "20", "1600", "180"},
                        {"3", "test2", "3", "1", "dodo1", "motor", "p-02", "200", "1", "0", "3600", "8", "2", "8", "1", "2", "3", "4"},
                        {"4", "test3", "0", "0", "dodo1", "valve", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "4"},
                        {"5", "test4", "4", "9", "dodo1", "pump", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "60"}};

                arrayListPreset.add("Charge Pump #1");
                arrayListPreset.add("Charge Pump #2");
                arrayListPreset.add("test2");
                arrayListPreset.add("test3");
                arrayListPreset.add("test4");
            }
        }
        else {

            try {
                JSONObject jsonOrgObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonOrgObject.getJSONArray("list");

                String[][] arrayPreset = new String[jsonArray.length()][18];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);

                    String no = jsonObj.getString("id");
                    String preset_name = jsonObj.getString("preset_name");
                    String code = jsonObj.getString("code");
                    String vib_spec = jsonObj.getString("vib_spec");
                    String site_code = jsonObj.getString("site_code");
                    String equipment_name = jsonObj.getString("equipment_name");
                    String tag_no = jsonObj.getString("tag_no");
                    String input_power = jsonObj.getString("input_power");
                    String line_freq = jsonObj.getString("line_freq");
                    String equpment_type = jsonObj.getString("equpment_type");
                    String rpm = jsonObj.getString("rpm");
                    String blade_vane = jsonObj.getString("blade_vane");
                    String bearing_type = jsonObj.getString("bearing_type");
                    String ball_count = jsonObj.getString("ball_count");
                    String pitch_diameter = jsonObj.getString("pitch_diameter");
                    String ball_diameter = jsonObj.getString("ball_diameter");
                    String rps = jsonObj.getString("rps");
                    String angle = jsonObj.getString("angle");

                    arrayPreset[i][0] = no;
                    arrayPreset[i][1] = preset_name;
                    arrayPreset[i][2] = code;
                    arrayPreset[i][3] = vib_spec;
                    arrayPreset[i][4] = site_code;
                    arrayPreset[i][5] = equipment_name;
                    arrayPreset[i][6] = tag_no;
                    arrayPreset[i][7] = input_power;
                    arrayPreset[i][8] = line_freq;
                    arrayPreset[i][9] = equpment_type;
                    arrayPreset[i][10] = rpm;
                    arrayPreset[i][11] = blade_vane;
                    arrayPreset[i][12] = bearing_type;
                    arrayPreset[i][13] = ball_count;
                    arrayPreset[i][14] = pitch_diameter;
                    arrayPreset[i][15] = ball_diameter;
                    arrayPreset[i][16] = rps;
                    arrayPreset[i][17] = angle;

                    arrayListPreset.add(preset_name);
                }

                preset = arrayPreset;

                bExistPreset = true;

            } catch (Exception e) {
                Log.d(TAG, "jsonString : " + jsonString);
                e.printStackTrace();
            }

            arrayAdapterPreset.notifyDataSetChanged();
        }

        setViewData(0);
        //spinnerPreset.setSelection(0);


        Utils.setStringArrayPref("preset", preset);
    }

    // 처음 서버에서 데이터를 받아올 때, preset spinner의 값이 변경될 때, 적절한 값일 넣어준다.
    void setViewData(int i) {
        if( preset == null || preset.length < i ) {
            ToastUtil.showShort("preset data is null");
            return;
        }

        editTextPresetName.setText(preset[i][1]);
        int equipmentCode = Integer.parseInt(preset[i][2]);
        editTextProjectVibSpec.setText(preset[i][3]);
        editTextSiteCode.setText(preset[i][4]);
        editTextEquipmentName.setText(preset[i][5]);
        editTextTagNo.setText(preset[i][6]);
        editTextInputPower.setText(preset[i][7]);
        int lineFrequency = Integer.parseInt(preset[i][8]);
        int equipmentType = Integer.parseInt(preset[i][9]);
        editTextEquipmentRpm.setText(preset[i][10]);
        editTextBladeVane.setText(preset[i][11]);
        int bearingType = Integer.parseInt(preset[i][12]);
        editTextNoOfBalls.setText(preset[i][13]);
        editTextPitchDiameter.setText(preset[i][14]);
        editTextBallDiameter.setText(preset[i][15]);
        editTextRps.setText(preset[i][16]);
        editTextContactAngle.setText(preset[i][17]);

        spinnerEquipmentCode.setSelection(equipmentCode);
        spinnerLineFrequency.setSelection(lineFrequency);
        spinnerEquipmentType.setSelection(equipmentType);
        spinnerBearingType.setSelection(bearingType);
    }


}
