package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
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

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.model.VARIABLES_1_Type;
import kr.co.signallink.svsv2.server.SendPost;
import kr.co.signallink.svsv2.services.SendMessageHandler;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;

// added by hslee 2020-01-15
// preset 추가 및 수정 화면
public class PresetActivity extends BaseActivity {

    private static final String TAG = "PresetActivity";

    ArrayList arrayListPreset = new ArrayList<>();
    ArrayList arrayListEquipmentCode = new ArrayList<>();
    ArrayList arrayListLineFrequency = new ArrayList<>();
    ArrayList arrayListEquipmentType = new ArrayList<>();
    ArrayList arrayListBearingType = new ArrayList<>();

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

    String equipmentUuid = null;
    SendMessageHandler handler;

    private String [][] preset;
    public boolean bExistPreset = false;
    public boolean bResponsePreset = false;

    ArrayAdapter arrayAdapterPreset;

    float [] measuredFreq1 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq2 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq3 = null;  // measureActivity에서 측정된 데이터

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        equipmentUuid = getIntent().getStringExtra("equipmentUuid");

        handler = new SendMessageHandler(this);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("INPUT");

        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        findViewById(R.id.button_record).setVisibility(View.GONE);  // 안쓰는 버튼 숨김

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
                if( validateForm() ) {

                    // 수정된 preset 확인 및 세팅
                    AnalysisData analysisData = setAnalysisData();

                    // 다음 화면으로 이동
                    Intent intent = new Intent(getBaseContext(), MeasureActivity.class);
                    intent.putExtra("analysisData", analysisData);
                    intent.putExtra("equipmentUuid", equipmentUuid);
                    intent.putExtra("measuredFreq1", measuredFreq1);
                    intent.putExtra("measuredFreq2", measuredFreq2);
                    intent.putExtra("measuredFreq3", measuredFreq3);
                    startActivityForResult(intent, DefConstant.REQUEST_MEASUREACTIVITY_RESULT);
                    //startActivity(intent);
                }
            }
        });

        spinnerPreset = findViewById(R.id.spinnerPreset);
        spinnerEquipmentCode = findViewById(R.id.spinnerEquipmentCode);
        spinnerLineFrequency = findViewById(R.id.spinnerLineFrequency);
        spinnerEquipmentType = findViewById(R.id.spinnerEquipmentType);
        spinnerBearingType = findViewById(R.id.spinnerBearingType);

        initDefaultItem();

        arrayAdapterPreset = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListPreset);
        ArrayAdapter arrayAdapterEquipmentCode = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentCode);
        ArrayAdapter arrayAdapterLineFrequency = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListLineFrequency);
        ArrayAdapter arrayAdapterEquipmentType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentType);
        ArrayAdapter arrayAdapterBearingType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListBearingType);

        arrayAdapterPreset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterEquipmentCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterLineFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterEquipmentType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterBearingType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
//            analysisData = (AnalysisData)intent.getSerializableExtra("analysisData");
//            if( analysisData != null ) {
//                editTextPresetName.setText(analysisData.getName());
//                editTextSiteCode.setText(analysisData.getSiteCode());
//                editTextEquipmentName.setText(analysisData.getEquipmentName());
//                editTextInputPower.setText(String.valueOf(analysisData.getInputPower()));
//                editTextEquipmentRpm.setText(String.valueOf(analysisData.getRpm()));
//                editTextBladeVane.setText(String.valueOf(analysisData.getBladeCount()));
//                editTextNoOfBalls.setText(String.valueOf(analysisData.getBallCount()));
//                editTextTagNo.setText(analysisData.getTagNo());
//                editTextPitchDiameter.setText(String.valueOf(analysisData.getPitchDiameter()));
//                editTextBallDiameter.setText(String.valueOf(analysisData.getBallDiameter()));
//                editTextRps.setText(String.valueOf(analysisData.getRps()));
//                editTextContactAngle.setText(String.valueOf(analysisData.getContactAngle()));
//                editTextProjectVibSpec.setText(String.valueOf(analysisData.getProjectVibSpec()));
//
//                spinnerEquipmentCode.setSelection(analysisData.getCode());
//                spinnerLineFrequency.setSelection(analysisData.getLineFreq());
//                spinnerEquipmentType.setSelection(analysisData.getEquipmentType());
//                spinnerBearingType.setSelection(analysisData.getBearingType());
//            }
//            else {
//                ToastUtil.showShort("failed to preset data load");
//            }
//        }

        // 서버에서 preset 정보 불러오기
        getPresetFromServer();

       // mainData = new MainData(this);
    }

    // measureactivity로 전달할 데이터 구성
    AnalysisData setAnalysisData() {
        AnalysisData analysisData = new AnalysisData();

        int code = (int)spinnerEquipmentCode.getSelectedItemId();
        int equipmentType = (int)spinnerEquipmentType.getSelectedItemId();
        int bearingType = (int)spinnerBearingType.getSelectedItemId();
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

        //analysisData.setNo(presetEntity.getNo());
        //analysisData.setName(presetEntity.getName());
        // diagVar1에 세팅되어 불필요
//        analysisData.setCode(code);
//        analysisData.setEquipmentType(equipmentType);
//        analysisData.setBearingType(bearingType);
//        analysisData.setLineFreq(lineFrequency);
//        analysisData.setProjectVibSpec(projectVibSpec);
//        analysisData.setSiteCode(siteCode);
//        analysisData.setEquipmentName(equipmentName);
//        analysisData.setTagNo(tagNo);
//        analysisData.setInputPower(inputPower);
//        analysisData.setRpm(equipmentRpm);
//        analysisData.setBladeCount(bladeVane);
//        analysisData.setBallCount(noOfBalls);
//        analysisData.setPitchDiameter(pitchDiameter);
//        analysisData.setBallDiameter(ballDiameter);
//        analysisData.setRps(rps);
//        analysisData.setContactAngle(contactAngle);

        VARIABLES_1_Type diagVar1 = new VARIABLES_1_Type();

        diagVar1.nCode = code;
        diagVar1.nEquipType = equipmentType;
        diagVar1.nBearingType = bearingType;
        diagVar1.nLineFreq = lineFrequency;
        diagVar1.nPrjVibSpec = projectVibSpec;
        diagVar1.strSiteCode = siteCode;
        diagVar1.strEquipName = equipmentName;
        diagVar1.strTagNo = tagNo;
        diagVar1.nInputPower = inputPower;
        diagVar1.nRPM = equipmentRpm;
        diagVar1.nBladeCount = bladeVane;
        diagVar1.nBallCount = noOfBalls;
        diagVar1.nPitchDiameter =pitchDiameter;
        diagVar1.nBallDiameter = ballDiameter;
        diagVar1.nRPS = rps;
        diagVar1.nContactAngle = contactAngle;

        analysisData.setDiagVar1(diagVar1);
//        analysisData.setValueVar2(mainData.valueVar2);
//        analysisData.setRangeVar2(mainData.rangeVar2);
//        analysisData.setLowerVar2(mainData.lowerVar2);
//        analysisData.setUpperVar2(mainData.upperVar2);
//        analysisData.setFeatureInfos(mainData.featureInfos);

        return analysisData;
    }


    void initDefaultItem() {

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

    // 값이 정상적으로 들어가 있는지 확인
    private boolean validateForm() {

        String siteCode = editTextSiteCode.getText().toString().trim();
        if(siteCode.isEmpty()){
            editTextSiteCode.requestFocus();
            ToastUtil.showShort("site code is empty");
            return false;
        }

        String equipmentName = editTextEquipmentName.getText().toString().trim();
        if(equipmentName.isEmpty()){
            editTextEquipmentName.requestFocus();
            ToastUtil.showShort("equipment name is empty");
            return false;
        }

        String inputPower = editTextInputPower.getText().toString().trim();
        if(inputPower.isEmpty()){
            editTextInputPower.requestFocus();
            ToastUtil.showShort("input power is empty");
            return false;
        }

        String equipmentRpm = editTextEquipmentRpm.getText().toString().trim();
        if(equipmentRpm.isEmpty()){
            editTextEquipmentRpm.requestFocus();
            ToastUtil.showShort("equipment rpm is empty");
            return false;
        }

        String bladeVane = editTextBladeVane.getText().toString().trim();
        if(bladeVane.isEmpty()){
            editTextBladeVane.requestFocus();
            ToastUtil.showShort("blade/vane is empty");
            return false;
        }

        String noOfBalls = editTextNoOfBalls.getText().toString().trim();
        if(noOfBalls.isEmpty()){
            editTextNoOfBalls.requestFocus();
            ToastUtil.showShort("No. of Balls is empty");
            return false;
        }

        String tagNo = editTextTagNo.getText().toString().trim();
        if(tagNo.isEmpty()){
            editTextTagNo.requestFocus();
            ToastUtil.showShort("tag No is empty");
            return false;
        }

        String pitchDiameter = editTextPitchDiameter.getText().toString().trim();
        if(pitchDiameter.isEmpty()){
            editTextPitchDiameter.requestFocus();
            ToastUtil.showShort("pitch diameter is empty");
            return false;
        }

        String ballDiameter = editTextBallDiameter.getText().toString().trim();
        if(ballDiameter.isEmpty()){
            editTextBallDiameter.requestFocus();
            ToastUtil.showShort("ball diameter is empty");
            return false;
        }

        String rps = editTextRps.getText().toString().trim();
        if(rps.isEmpty()){
            editTextRps.requestFocus();
            ToastUtil.showShort("rps is empty");
            return false;
        }

        String contactAngle = editTextContactAngle.getText().toString().trim();
        if(contactAngle.isEmpty()){
            editTextContactAngle.requestFocus();
            ToastUtil.showShort("contact angle is empty");
            return false;
        }

        return true;
    }


//    private void save() {
//
//        if(validateForm()) {
//
//            //Preset 객체 만들기
//            final PresetEntity presetEntity = new PresetEntity();
//
//            //presetEntity.setName(editTextPresetName.getText().toString());
//
//            int code = (int)spinnerEquipmentCode.getSelectedItemId();
////            if( spinnerEquipmentCode.getSelectedItemId() == 0 ) code = "ANSI HI 9.6.4";
////            else if( spinnerEquipmentCode.getSelectedItemId() == 1 ) code = "API 610";
////            else if( spinnerEquipmentCode.getSelectedItemId() == 2 ) code = "ISO 10816 Cat.1";
////            else if( spinnerEquipmentCode.getSelectedItemId() == 3 ) code = "ISO 10816 Cat.2";
////            else if( spinnerEquipmentCode.getSelectedItemId() == 4 ) code = "Project VIB Spec";
//
//            int equipmentType = (int)spinnerEquipmentType.getSelectedItemId();
////            if( spinnerEquipmentType.getSelectedItemId() == 0 ) equipmentType = "Horizontal (BB&OH)";
////            else if( spinnerEquipmentType.getSelectedItemId() == 1 ) equipmentType = "Vertical (VS)";
////            else if( spinnerEquipmentType.getSelectedItemId() == 2 ) equipmentType = "ETC";
//
//            int bearingType = (int)spinnerBearingType.getSelectedItemId();
////            if( spinnerBearingType.getSelectedItemId() == 0 ) bearingType = "Ball";
////            else if( spinnerBearingType.getSelectedItemId() == 1 ) bearingType = "Roller";
////            else if( spinnerBearingType.getSelectedItemId() == 2 ) bearingType = "Journal";
////            else if( spinnerBearingType.getSelectedItemId() == 3 ) bearingType = "ETC";
//
//            int lineFrequency = (int)spinnerLineFrequency.getSelectedItemId();// == 0 ? 50 : 60;
//
//            int projectVibSpec = Integer.parseInt(editTextProjectVibSpec.getText().toString());
//            String siteCode = editTextSiteCode.getText().toString();
//            String equipmentName = editTextEquipmentName.getText().toString();
//            String tagNo = editTextTagNo.getText().toString();
//            int inputPower = Integer.parseInt(editTextInputPower.getText().toString());
//            int equipmentRpm = Integer.parseInt(editTextEquipmentRpm.getText().toString());
//            int bladeVane = Integer.parseInt(editTextBladeVane.getText().toString());
//            int noOfBalls = Integer.parseInt(editTextNoOfBalls.getText().toString());
//            int pitchDiameter = Integer.parseInt(editTextPitchDiameter.getText().toString());
//            int ballDiameter = Integer.parseInt(editTextBallDiameter.getText().toString());
//            int rps = Integer.parseInt(editTextRps.getText().toString());
//            int contactAngle = Integer.parseInt(editTextContactAngle.getText().toString());
//
//            presetEntity.setCode(code);
//            presetEntity.setEquipmentType(equipmentType);
//            presetEntity.setBearingType(bearingType);
//            presetEntity.setLineFreq(lineFrequency);
//            presetEntity.setProjectVibSpec(projectVibSpec);
//            presetEntity.setSiteCode(siteCode);
//            presetEntity.setEquipmentName(equipmentName);
//            presetEntity.setTagNo(tagNo);
//            presetEntity.setInputPower(inputPower);
//            presetEntity.setRpm(equipmentRpm);
//            presetEntity.setBladeCount(bladeVane);
//            presetEntity.setBallCount(noOfBalls);
//            presetEntity.setPitchDiameter(pitchDiameter);
//            presetEntity.setBallDiameter(ballDiameter);
//            presetEntity.setRps(rps);
//            presetEntity.setContactAngle(contactAngle);
//
//
//            //기록
//            DatabaseUtil.transaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//
//                    int no = 0;
//                    if( "0".equals(bModeCreate) ) {
//                        no = analysisData.getNo();
//                    }
//                    else {  // 추가일 경우 index값 증가
//                        Number currentNo = realm.where(PresetEntity.class).max("no");
//
//                        if (currentNo == null) {
//                            no = 1;
//                        } else {
//                            no = currentNo.intValue() + 1;
//                        }
//                    }
//
//                    presetEntity.setNo(no);
//                    realm.copyToRealmOrUpdate(presetEntity);
//                }
//            });
//
//            //토스트
//            ToastUtil.showShort("Save Success");
//
//            //화면 종료
//            finish();
//        }
//    }


//    private void dialogSave(){
//
//        DialogUtil.yesNo(this,
//                getResources().getString(R.string.update_screen),
//                "Do you want to save?",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                        save();
//                    }
//                },
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//    }

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

        }

        arrayAdapterPreset.notifyDataSetChanged();

        setViewData(0);
    }

    // 처음 서버에서 데이터를 받아올 때, preset spinner의 값이 변경될 때, 적절한 값일 넣어준다.
    void setViewData(int i) {
        if( preset == null || preset.length < i ) {
            //ToastUtil.showShort("preset data is null");
            return;
        }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == DefConstant.REQUEST_MEASUREACTIVITY_RESULT) {
                measuredFreq1 = (float[]) data.getSerializableExtra("measuredFreq1");
                measuredFreq2 = (float[]) data.getSerializableExtra("measuredFreq2");
                measuredFreq3 = (float[]) data.getSerializableExtra("measuredFreq3");
            }
        }
    }

}
