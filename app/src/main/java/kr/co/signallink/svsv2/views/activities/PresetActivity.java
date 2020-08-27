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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

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

// added by hslee 2020-01-15
// preset 추가 및 수정 화면
public class PresetActivity extends BaseActivity {

    private static final String TAG = "PresetActivity";
    static final String PRESET_USER_DEFINITION = "User Definition";

    ArrayList arrayListPreset = new ArrayList<>();
    final int SAVED_PRESET_COUNT = 9;
    ArrayList arrayListEquipmentCode = new ArrayList<>();
    ArrayList arrayListEquipmentSubCode = new ArrayList<>();
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
    Spinner spinnerEquipmentSubCode;
    Spinner spinnerLineFrequency;
    Spinner spinnerEquipmentType;
    Spinner spinnerBearingType;

    int previousPositionSpinnerPreset = 0;
    int previousPositionSpinnerEquipmentCode = 0;
    int previousPositionSpinnerEquipmentSubCode = 0;
    int previousPositionSpinnerLineFrequency = 0;
    int previousPositionSpinnerEquipmentType = 0;
    int previousPositionSpinnerBearingType = 0;

    String equipmentUuid = null;
    SendMessageHandler handler;

    private String [][] preset;
    private ArrayList<String> userDefinitionPreset = null; // 2020.07.15
    public boolean bExistPreset = false;
    public boolean bResponsePreset = false;

    ArrayAdapter arrayAdapterPreset;
    ArrayAdapter arrayAdapterEquipmentCode;
    ArrayAdapter arrayAdapterEquipmentSubCode;
    ArrayAdapter arrayAdapterLineFrequency;
    ArrayAdapter arrayAdapterEquipmentType;
    ArrayAdapter arrayAdapterBearingType;

    float [] measuredFreq1 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq2 = null;  // measureActivity에서 측정된 데이터
    float [] measuredFreq3 = null;  // measureActivity에서 측정된 데이터
    AnalysisData m_analysisData = null;

    boolean bRemeasure = true;  // measureActivity 화면에서 다시 측정해야 할지 여부, 값을 변경하면 측정을 다시해야 함.
    boolean bAutoChangeSpinnerCode = false; // setPosition에 의해 자동으로 호출됐는지 여부, 자동 호출은 무시하기 위해 사용
    boolean bAutoChangeSpinnerSubCode = false;
    boolean bAutoChangeSpinnerLineFreq = false;
    boolean bAutoChangeSpinnerBearingType = false;


    // ANSI HI 9.6.4
    float[] fANSI_FactoryPOR = { 4.8f, 5.6f, 4.3f, 5.3f };
    float[] fANSI_FactoryAOR = { 6.2f, 7.3f, 5.6f, 6.9f };
    float[] fANSI_FieldPOR = { 3.8f, 4.8f, 3.3f, 4.3f };
    float[] fANSI_FieldAOR = { 4.9f, 6.2f, 4.3f, 5.6f };

    // API 610
    float[] fAPI_Overall = { 3.0f, 5.0f };

    // ISO 10816
    float[] fISOcategory_Alarm = { 5.0f, 6.3f, 6.4f, 7.6f };
    float[] fISOcategory_Trip = { 8.3f, 9.5f, 10.6f, 11.9f };
    float[] fISOcategory_SATPOR = { 2.5f, 3.5f, 3.2f, 4.2f };
    float[] fISOcategory_SATAOR = { 3.4f, 4.4f, 4.2f, 5.2f };
    float[] fISOcategory_FATPOR = { 3.3f, 4.3f, 4.2f, 5.2f };
    float[] fISOcategory_FATAOR = { 4.0f, 5.0f, 5.1f, 6.1f };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset);

        equipmentUuid = getIntent().getStringExtra("equipmentUuid");

        // 내부 저장소에서 저장된 preset 가져오기
        userDefinitionPreset = Utils.getSharedPreferencesStringArray(this, "preset", "userpreset"+equipmentUuid);

        handler = new SendMessageHandler(this);

        Toolbar svsToolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = svsToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Input");

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

        editTextSiteCode.addTextChangedListener(textWatcherInput);
        editTextEquipmentName.addTextChangedListener(textWatcherInput);
        editTextInputPower.addTextChangedListener(textWatcherInput);
        editTextEquipmentRpm.addTextChangedListener(textWatcherInputRpm);
        editTextBladeVane.addTextChangedListener(textWatcherInput);
        editTextNoOfBalls.addTextChangedListener(textWatcherInput);
        editTextTagNo.addTextChangedListener(textWatcherInput);
        editTextPitchDiameter.addTextChangedListener(textWatcherInput);
        editTextBallDiameter.addTextChangedListener(textWatcherInput);
        editTextRps.addTextChangedListener(textWatcherInput);
        editTextContactAngle.addTextChangedListener(textWatcherInput);
        editTextProjectVibSpec.addTextChangedListener(textWatcherInput);

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

        Button buttonSave = findViewById(R.id.buttonSave);  // added by hslee 2020.07.15
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        spinnerPreset = findViewById(R.id.spinnerPreset);
        spinnerEquipmentCode = findViewById(R.id.spinnerEquipmentCode);
        spinnerEquipmentSubCode = findViewById(R.id.spinnerEquipmentSubCode);
        spinnerLineFrequency = findViewById(R.id.spinnerLineFrequency);
        spinnerEquipmentType = findViewById(R.id.spinnerEquipmentType);
        spinnerBearingType = findViewById(R.id.spinnerBearingType);

        spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if( previousPositionSpinnerPreset != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);
                    setViewData(position);

                    previousPositionSpinnerPreset = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerEquipmentCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if( previousPositionSpinnerEquipmentCode != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);

                    previousPositionSpinnerEquipmentCode = position;

                    setDefaultSubCodeItem(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerEquipmentSubCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if(bAutoChangeSpinnerSubCode) {
                    bAutoChangeSpinnerSubCode = false;
                    return;
                }

                if( previousPositionSpinnerEquipmentSubCode != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);

                    previousPositionSpinnerEquipmentSubCode = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerLineFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if(bAutoChangeSpinnerLineFreq) {
                    bAutoChangeSpinnerLineFreq = false;
                    return;
                }

                if( previousPositionSpinnerLineFrequency != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);

                    previousPositionSpinnerLineFrequency = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerEquipmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if( previousPositionSpinnerEquipmentType != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);

                    previousPositionSpinnerEquipmentType = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerBearingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, final long id) {

                if(bAutoChangeSpinnerBearingType) {
                    bAutoChangeSpinnerBearingType = false;
                    return;
                }

                if( previousPositionSpinnerBearingType != position ) {
                    DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPreviousAnalysisData();  // 취소할 경우 원래의 값으로 변경
                        }
                    };
                    valueChanged(cancel);

                    previousPositionSpinnerBearingType = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        initDefaultItem();

        arrayAdapterPreset = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListPreset);
        arrayAdapterEquipmentCode = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentCode);
        arrayAdapterEquipmentSubCode = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentSubCode);
        arrayAdapterLineFrequency = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListLineFrequency);
        arrayAdapterEquipmentType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListEquipmentType);
        arrayAdapterBearingType = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item, arrayListBearingType);

        arrayAdapterPreset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterEquipmentCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterEquipmentSubCode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterLineFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterEquipmentType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapterBearingType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPreset.setAdapter(arrayAdapterPreset);
        spinnerEquipmentCode.setAdapter(arrayAdapterEquipmentCode);
        spinnerEquipmentSubCode.setAdapter(arrayAdapterEquipmentSubCode);
        spinnerLineFrequency.setAdapter(arrayAdapterLineFrequency);
        spinnerEquipmentType.setAdapter(arrayAdapterEquipmentType);
        spinnerBearingType.setAdapter(arrayAdapterBearingType);

        // 서버에서 preset 정보 불러오기
        //getPresetFromServer();    // 임시 삭제 2020.08.27
        parsePreset("");

       // mainData = new MainData(this);
    }

    // measureactivity로 전달할 데이터 구성
    AnalysisData setAnalysisData() {
        AnalysisData analysisData = new AnalysisData();

        int presetId = (int)spinnerPreset.getSelectedItemId();
        int code = (int)spinnerEquipmentCode.getSelectedItemId();
        int subCode = (int)spinnerEquipmentSubCode.getSelectedItemId();
        int equipmentType = (int)spinnerEquipmentType.getSelectedItemId();
        int bearingType = (int)spinnerBearingType.getSelectedItemId();
        int lineFrequency = (int)spinnerLineFrequency.getSelectedItemId();// == 0 ? 50 : 60;
        float projectVibSpec = Float.parseFloat(editTextProjectVibSpec.getText().toString());
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

        diagVar1.presetId = presetId;
        diagVar1.nCode = code;
        diagVar1.nSubCode = subCode;
        diagVar1.nEquipType = equipmentType;
        diagVar1.nBearingType = bearingType;
        diagVar1.nLineFreq = lineFrequency;
        diagVar1.nPrjVibSpec = projectVibSpec;    // added by hslee 2020.03 아마도 사용 안함
        diagVar1.strSiteCode = siteCode;
        diagVar1.strEquipName = equipmentName;
        diagVar1.strTagNo = tagNo;
        diagVar1.nInputPower = inputPower;
        diagVar1.nRPM = equipmentRpm;
        diagVar1.nBladeCount = bladeVane;
        diagVar1.nBallCount = noOfBalls;
        diagVar1.nPitchDiameter = pitchDiameter;
        diagVar1.nBallDiameter = ballDiameter;
        diagVar1.nRPS = rps;
        diagVar1.nContactAngle = contactAngle;

        analysisData.setDiagVar1(diagVar1);

        float rmsLimit = fnGetCodeValue();
        analysisData.rmsLimit = rmsLimit;
//        analysisData.setValueVar2(mainData.valueVar2);
//        analysisData.setRangeVar2(mainData.rangeVar2);
//        analysisData.setLowerVar2(mainData.lowerVar2);
//        analysisData.setUpperVar2(mainData.upperVar2);
//        analysisData.setFeatureInfos(mainData.featureInfos);

        return analysisData;
    }

    // 값 변경하기 전의 데이터로 변경
    void setPreviousAnalysisData() {
        if( m_analysisData == null )
            m_analysisData = setAnalysisData();

        setDefaultSubCodeItem(m_analysisData.getDiagVar1().nCode);

        spinnerPreset.setSelection(m_analysisData.getDiagVar1().presetId);
        spinnerEquipmentCode.setSelection(m_analysisData.getDiagVar1().nCode);
        spinnerEquipmentSubCode.setSelection(m_analysisData.getDiagVar1().nSubCode);
        spinnerEquipmentType.setSelection(m_analysisData.getDiagVar1().nEquipType);
        spinnerBearingType.setSelection(m_analysisData.getDiagVar1().nBearingType);
        spinnerLineFrequency.setSelection(m_analysisData.getDiagVar1().nLineFreq);

        previousPositionSpinnerPreset = spinnerPreset.getSelectedItemPosition();
        previousPositionSpinnerEquipmentCode = spinnerEquipmentCode.getSelectedItemPosition();
        previousPositionSpinnerEquipmentSubCode = spinnerEquipmentSubCode.getSelectedItemPosition();
        previousPositionSpinnerEquipmentType = spinnerEquipmentType.getSelectedItemPosition();
        previousPositionSpinnerBearingType = spinnerBearingType.getSelectedItemPosition();
        previousPositionSpinnerLineFrequency = spinnerLineFrequency.getSelectedItemPosition();

        LinearLayout linearLayoutProjectVibSpec = findViewById(R.id.linearLayoutProjectVibSpec);

        if( m_analysisData.getDiagVar1().nCode == 0 ) { // ANSI HI 9.6.4
            spinnerEquipmentSubCode.setVisibility(View.VISIBLE);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else if( m_analysisData.getDiagVar1().nCode == 1 ) {    // API 610
            spinnerEquipmentSubCode.setVisibility(View.GONE);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else if( m_analysisData.getDiagVar1().nCode == 2 || m_analysisData.getDiagVar1().nCode == 3 ) { // ISO 10816 Cat. 1, Cat. 2
            spinnerEquipmentSubCode.setVisibility(View.VISIBLE);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else {  // Project VIB Spec
            spinnerEquipmentSubCode.setVisibility(View.GONE);
            linearLayoutProjectVibSpec.setVisibility(View.VISIBLE);
        }

        arrayAdapterPreset.notifyDataSetChanged();
        arrayAdapterEquipmentCode.notifyDataSetChanged();
        arrayAdapterEquipmentSubCode.notifyDataSetChanged();
        arrayAdapterLineFrequency.notifyDataSetChanged();
        arrayAdapterEquipmentType.notifyDataSetChanged();
        arrayAdapterBearingType.notifyDataSetChanged();

        editTextProjectVibSpec.setText(String.valueOf(m_analysisData.getDiagVar1().nPrjVibSpec));
        editTextSiteCode.setText(String.valueOf(m_analysisData.getDiagVar1().strSiteCode));
        editTextEquipmentName.setText(String.valueOf(m_analysisData.getDiagVar1().strEquipName));
        editTextTagNo.setText(String.valueOf(m_analysisData.getDiagVar1().strTagNo));
        editTextInputPower.setText(String.valueOf(m_analysisData.getDiagVar1().nInputPower));
        editTextEquipmentRpm.setText(String.valueOf(m_analysisData.getDiagVar1().nRPM));
        editTextBladeVane.setText(String.valueOf(m_analysisData.getDiagVar1().nBladeCount));
        editTextNoOfBalls.setText(String.valueOf(m_analysisData.getDiagVar1().nBallCount));
        editTextPitchDiameter.setText(String.valueOf(m_analysisData.getDiagVar1().nPitchDiameter));
        editTextBallDiameter.setText(String.valueOf(m_analysisData.getDiagVar1().nBallDiameter));
        editTextRps.setText(String.valueOf(m_analysisData.getDiagVar1().nRPS));
        editTextContactAngle.setText(String.valueOf(m_analysisData.getDiagVar1().nContactAngle));

        bAutoChangeSpinnerCode = true;
        bAutoChangeSpinnerSubCode = true;
        bAutoChangeSpinnerLineFreq = true;
        bAutoChangeSpinnerBearingType = true;
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


    // EquipmentCode에 따라 subcode 아이템 세팅
    void setDefaultSubCodeItem(int position) {

        LinearLayout linearLayoutProjectVibSpec = findViewById(R.id.linearLayoutProjectVibSpec);

        arrayListEquipmentSubCode.clear();

        if( position == 0 ) { // ANSI HI 9.6.4
            spinnerEquipmentSubCode.setVisibility(View.VISIBLE);

            arrayListEquipmentSubCode.add("Factory POR");
            arrayListEquipmentSubCode.add("Factory AOR");
            arrayListEquipmentSubCode.add("Field POR");
            arrayListEquipmentSubCode.add("Field AOR");

            //spinnerEquipmentSubCode.setSelection(0);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else if( position == 1 ) {    // API 610
            spinnerEquipmentSubCode.setVisibility(View.GONE);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else if( position == 2 || position == 3 ) { // ISO 10816 Cat. 1, Cat. 2
            spinnerEquipmentSubCode.setVisibility(View.VISIBLE);

            arrayListEquipmentSubCode.add("Alarm");
            arrayListEquipmentSubCode.add("Trip");
            arrayListEquipmentSubCode.add("SAT POR");
            arrayListEquipmentSubCode.add("SAT AOR");
            arrayListEquipmentSubCode.add("FAT POR");
            arrayListEquipmentSubCode.add("FAT AOR");

            //spinnerEquipmentSubCode.setSelection(0);
            linearLayoutProjectVibSpec.setVisibility(View.GONE);
        }
        else {  // Project VIB Spec
            spinnerEquipmentSubCode.setVisibility(View.GONE);
            linearLayoutProjectVibSpec.setVisibility(View.VISIBLE);
        }

        arrayAdapterEquipmentSubCode.notifyDataSetChanged();

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


//    private void save_activity() {
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

    private void save() {   // 2020.07.15

        if(validateForm()) {

            int code = (int)spinnerEquipmentCode.getSelectedItemId();
            int subCode = (int)spinnerEquipmentSubCode.getSelectedItemId();
            int equipmentType = (int)spinnerEquipmentType.getSelectedItemId();
            int bearingType = (int)spinnerBearingType.getSelectedItemId();
            int lineFrequency = (int)spinnerLineFrequency.getSelectedItemId();

            //int projectVibSpec = Integer.parseInt(editTextProjectVibSpec.getText().toString());
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

            userDefinitionPreset = null;
            userDefinitionPreset = new ArrayList<>();
            userDefinitionPreset.add("6");
            userDefinitionPreset.add(PRESET_USER_DEFINITION);
            userDefinitionPreset.add(String.valueOf(code));
            userDefinitionPreset.add(editTextProjectVibSpec.getText().toString());
            userDefinitionPreset.add(editTextSiteCode.getText().toString());
            userDefinitionPreset.add(editTextEquipmentName.getText().toString());
            userDefinitionPreset.add(editTextTagNo.getText().toString());
            userDefinitionPreset.add(editTextInputPower.getText().toString());
            userDefinitionPreset.add(String.valueOf(lineFrequency));
            userDefinitionPreset.add(String.valueOf(equipmentType));
            userDefinitionPreset.add(editTextEquipmentRpm.getText().toString());
            userDefinitionPreset.add(editTextBladeVane.getText().toString());
            userDefinitionPreset.add(String.valueOf(bearingType));
            userDefinitionPreset.add(editTextNoOfBalls.getText().toString());
            userDefinitionPreset.add(editTextPitchDiameter.getText().toString());
            userDefinitionPreset.add(editTextBallDiameter.getText().toString());
            userDefinitionPreset.add(editTextRps.getText().toString());
            userDefinitionPreset.add(editTextContactAngle.getText().toString());
            userDefinitionPreset.add(String.valueOf(subCode));

            // 내부 저장소에 저장
            Utils.setSharedPreferencesStringArray(this, "preset", "userpreset"+equipmentUuid, userDefinitionPreset);

            preset[SAVED_PRESET_COUNT][0] = userDefinitionPreset.get(0);
            preset[SAVED_PRESET_COUNT][1] = userDefinitionPreset.get(1);
            preset[SAVED_PRESET_COUNT][2] = userDefinitionPreset.get(2);
            preset[SAVED_PRESET_COUNT][3] = userDefinitionPreset.get(3);
            preset[SAVED_PRESET_COUNT][4] = userDefinitionPreset.get(4);
            preset[SAVED_PRESET_COUNT][5] = userDefinitionPreset.get(5);
            preset[SAVED_PRESET_COUNT][6] = userDefinitionPreset.get(6);
            preset[SAVED_PRESET_COUNT][7] = userDefinitionPreset.get(7);
            preset[SAVED_PRESET_COUNT][8] = userDefinitionPreset.get(8);
            preset[SAVED_PRESET_COUNT][9] = userDefinitionPreset.get(9);
            preset[SAVED_PRESET_COUNT][10] = userDefinitionPreset.get(10);
            preset[SAVED_PRESET_COUNT][11] = userDefinitionPreset.get(11);
            preset[SAVED_PRESET_COUNT][12] = userDefinitionPreset.get(12);
            preset[SAVED_PRESET_COUNT][13] = userDefinitionPreset.get(13);
            preset[SAVED_PRESET_COUNT][14] = userDefinitionPreset.get(14);
            preset[SAVED_PRESET_COUNT][15] = userDefinitionPreset.get(15);
            preset[SAVED_PRESET_COUNT][16] = userDefinitionPreset.get(16);
            preset[SAVED_PRESET_COUNT][17] = userDefinitionPreset.get(17);
            preset[SAVED_PRESET_COUNT][18] = userDefinitionPreset.get(18);

            if( arrayListPreset.size() == SAVED_PRESET_COUNT ) { // 콤보박스에, 기존에 저장된 것이 없으면 추가

                arrayListPreset.add(PRESET_USER_DEFINITION);
            }

            ToastUtil.showShort("Save Success");
        }
    }


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
            //ToastUtil.showShort("failed get preset from server. use in local storage");

            preset = new String[][]{
                {"1","CWP_IBON","2","0","IBON","Circulating Water Pump","P2PAC10AP001","3086","0","1","425","5","2","0","0","0","0","0"},
                {"2","BFP#1_IBON","2","0","IBON","Motor Driven Boiler Feedwater Pump","P2LAC13AP010KP02","7356.8","0","0","5583","0","2","0","0","0","0","0"},
                {"3","BFP#2_IBON","2","0","IBON","Turbine Driven Boiler Feedwater Pump","P2LAC11AP010KP02","17943.7","0","0","5857","0","2","0","0","0","0","0"},
                {"4","CEP_IBON","2","0","IBON","Condensate Extraction Pump","P2LCB10AP010","1276.9","0","1","1486","0","2","0","0","0","0","0"},
                {"5","CWP_FA30","2","0","FA30","Circulating Water Pump","12PAC10AP001","439.5","1","0","893","0","0","9","0","0","0","0"},
                {"6","BFP_FA30","2","0","FA30","Boiler Feedwater Pump","12LAC11AP001","1249.4","1","0","3570","0","2","0","0","0","0","0"},
                {"7","CEP_FA30","2","0","FA30","Condensate Extraction Pump","12LCB11AP001","259.5","1","1","1784","0","1","0","0","0","0","0"},
                {"8","MCWP_RAPO","4","7.11","RAPO","Main Cooling Water Pump","P00PAC11","2264","0","1","424","5","2","0","0","0","0","0"},
                {"9","CTBDP_RAPO","4","7.11","RAPO","Cooling Tower BlowDown Pump","P00PAD11","1980","0","1","494","4","3","0","0","0","0","0"},
                {"10", PRESET_USER_DEFINITION, "4", "9", "dodo1", "pump", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "60", "0", ""}
//                    {"1", "Charge Pump #1", "1", "1", "HDO", "Charge Pump", "PP-L25-51", "980", "1", "0", "3579", "5", "3", "0", "0", "0", "0", "0", "0"},
//                    {"2", "Charge Pump #2", "2", "2", "HDO", "Charge Pump", "PP-L25-01", "1080", "1", "0", "3600", "8", "2", "4", "200", "20", "1600", "180", "0"},
//                    {"3", "test2", "3", "1", "dodo1", "motor", "p-02", "200", "1", "0", "3600", "8", "2", "8", "1", "2", "3", "4", "0"},
//                    {"4", "test3", "0", "0", "dodo1", "valve", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "4", "0"},
//                    {"5", "test4", "4", "9", "dodo1", "pump", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "60", "0"},
//                    {"6", PRESET_USER_DEFINITION, "4", "9", "dodo1", "pump", "p-001", "10", "0", "0", "8", "6", "0", "6", "1", "2", "3", "60", "0", ""}
            };

            arrayListPreset.add("CWP_IBON");
            arrayListPreset.add("BFP#1_IBON");
            arrayListPreset.add("BFP#2_IBON");
            arrayListPreset.add("CEP_IBON");
            arrayListPreset.add("CWP_FA30");
            arrayListPreset.add("BFP_FA30");
            arrayListPreset.add("CEP_FA30");
            arrayListPreset.add("MCWP_RAPO");
            arrayListPreset.add("CTBDP_RAPO");
        }
        else {

            try {
                JSONObject jsonOrgObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonOrgObject.getJSONArray("list");

                String[][] arrayPreset = new String[jsonArray.length()+1][19];

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

        // 2020.07.15   사용자가 저장한 데이터가 있으면 불러오기
        if( userDefinitionPreset != null && userDefinitionPreset.size() == 19 ) {

            preset[SAVED_PRESET_COUNT][0] = userDefinitionPreset.get(0);//no;
            preset[SAVED_PRESET_COUNT][1] = userDefinitionPreset.get(1);//preset_name;
            preset[SAVED_PRESET_COUNT][2] = userDefinitionPreset.get(2);//code;
            preset[SAVED_PRESET_COUNT][3] = userDefinitionPreset.get(3);//vib_spec;
            preset[SAVED_PRESET_COUNT][4] = userDefinitionPreset.get(4);//site_code;
            preset[SAVED_PRESET_COUNT][5] = userDefinitionPreset.get(5);//equipment_name;
            preset[SAVED_PRESET_COUNT][6] = userDefinitionPreset.get(6);//tag_no;
            preset[SAVED_PRESET_COUNT][7] = userDefinitionPreset.get(7);//input_power;
            preset[SAVED_PRESET_COUNT][8] = userDefinitionPreset.get(8);//line_freq;
            preset[SAVED_PRESET_COUNT][9] = userDefinitionPreset.get(9);//equpment_type;
            preset[SAVED_PRESET_COUNT][10] = userDefinitionPreset.get(10);//rpm;
            preset[SAVED_PRESET_COUNT][11] = userDefinitionPreset.get(11); //blade_vane;
            preset[SAVED_PRESET_COUNT][12] = userDefinitionPreset.get(12);//bearing_type;
            preset[SAVED_PRESET_COUNT][13] = userDefinitionPreset.get(13);//ball_count;
            preset[SAVED_PRESET_COUNT][14] = userDefinitionPreset.get(14);//pitch_diameter;
            preset[SAVED_PRESET_COUNT][15] = userDefinitionPreset.get(15);//ball_diameter;
            preset[SAVED_PRESET_COUNT][16] = userDefinitionPreset.get(16);//rps;
            preset[SAVED_PRESET_COUNT][17] = userDefinitionPreset.get(17);//angle;
            preset[SAVED_PRESET_COUNT][18] = userDefinitionPreset.get(18);//sub_code;

            // 기존 추가된 세트가 9개이면 추가
            if( arrayListPreset.size() < SAVED_PRESET_COUNT + 1 ) {
                arrayListPreset.add(PRESET_USER_DEFINITION);
            }
        }

        arrayAdapterPreset.notifyDataSetChanged();

        setViewData(0);
    }

    // 처음 서버에서 데이터를 받아올 때, preset spinner의 값이 변경될 때, 적절한 값을 넣어준다.
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
        editTextEquipmentRpm.setText(preset[i][10]);    // rps 값 자동 계산을 위해 여기에 위치 시킴
        editTextBladeVane.setText(preset[i][11]);
        int bearingType = Integer.parseInt(preset[i][12]);
        editTextNoOfBalls.setText(preset[i][13]);
        editTextPitchDiameter.setText(preset[i][14]);
        editTextBallDiameter.setText(preset[i][15]);
        //editTextRps.setText(preset[i][16]);

        //String rps = getRpsFromRpm(preset[i][16]);
        //editTextRps.setText(rps); // added by hslee 2020.07.13 rpm에 따라 자동 계산되므로 삭제

        spinnerEquipmentCode.setSelection(equipmentCode);
        //spinnerEquipmentSubCode.setSelection(0);
        spinnerLineFrequency.setSelection(lineFrequency);
        spinnerEquipmentType.setSelection(equipmentType);
        spinnerBearingType.setSelection(bearingType);

        setDefaultSubCodeItem(equipmentCode);

        editTextContactAngle.setText(preset[i][17]);
        if( i == SAVED_PRESET_COUNT ) {  // 사용자 정의이면
            spinnerEquipmentSubCode.setSelection(Integer.parseInt(preset[SAVED_PRESET_COUNT][18]));
            arrayAdapterEquipmentSubCode.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == DefConstant.REQUEST_MEASUREACTIVITY_RESULT) {
                measuredFreq1 = (float[]) data.getSerializableExtra("measuredFreq1");
                measuredFreq2 = (float[]) data.getSerializableExtra("measuredFreq2");
                measuredFreq3 = (float[]) data.getSerializableExtra("measuredFreq3");

                m_analysisData = (AnalysisData) data.getSerializableExtra("analysisData");


//                measuredFreq1 = new float[2048];  // for test
//                measuredFreq2 = new float[2048];  // for test
//                measuredFreq3 = new float[2048];  // for test

                // 측정된 데이터가 없는 경우
                // 측정된 데이터가 있는 경우
                bRemeasure = measuredFreq1 == null || measuredFreq2 == null || measuredFreq3 == null;
            }
        }
    }

    TextWatcher textWatcherInput = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

            //valueChanged(null);
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

    TextWatcher textWatcherInputRpm = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

            //valueChanged(null);

            String rps = getRpsFromRpm(s.toString());
            editTextRps.setText(rps);
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

    String getRpsFromRpm(String rpm) {
        try {
            if( rpm == null ) {

            }
            else {
                String rps = String.valueOf(Integer.parseInt(rpm) / 60);
                return rps;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

    public void valueChanged(DialogInterface.OnClickListener cancel) {

        // 기존에 측정한 데이터가 있고, 재측정을 안해도 되는 상태인데 값을 변경한 경우
        // 같은 창이 두 번 안뜬 경우
        if( !(measuredFreq1 == null || measuredFreq2 == null || measuredFreq3 == null) && !bRemeasure ) {

            DialogUtil.yesNo(PresetActivity.this,
                    "info",
                    "If you change the value you will have to re-measure.",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            measuredFreq1 = null;
                            measuredFreq2 = null;
                            measuredFreq3 = null;

                            m_analysisData = null;

                            bRemeasure = true;
                        }
                    },
                    cancel
            );
        }
    }

    // 2020.03
    private float fnGetCodeValue() {
        float fValue = 0;

        //if (tb_Preset_CodeValue.Text == "-")    // 최초 실행 시,
        //    return fValue;

        int nCodeIdx = (int)spinnerEquipmentCode.getSelectedItemId();
        int nTypeIdx = (int)spinnerEquipmentSubCode.getSelectedItemId();

        int nEquipTypeIdx = (int)spinnerEquipmentType.getSelectedItemId();
        int nInputPower = Integer.parseInt(editTextInputPower.getText().toString());
        int nRPM = Integer.parseInt(editTextEquipmentRpm.getText().toString());

        switch (nCodeIdx)
        {
            case 0:     // ANSI HI 9.6.4
                switch (nTypeIdx)
                {
                    case 0:     // Factory POR
                        if (nEquipTypeIdx == 0)     // Horizontal (BB & OH)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FactoryPOR[0];
                            else    // >= 200kW
                                fValue = fANSI_FactoryPOR[1];
                        }
                        else if (nEquipTypeIdx == 1)    // Vertical (VS)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FactoryPOR[2];
                            else    // >= 200kW
                                fValue = fANSI_FactoryPOR[3];
                        }
                        else    // etc
                        {
                        }
                        break;

                    case 1:     // Factory AOR
                        if (nEquipTypeIdx == 0)     // Horizontal (BB & OH)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FactoryAOR[0];
                            else    // >= 200kW
                                fValue = fANSI_FactoryAOR[1];
                        }
                        else if (nEquipTypeIdx == 1)    // Vertical (VS)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FactoryAOR[2];
                            else    // >= 200kW
                                fValue = fANSI_FactoryAOR[3];
                        }
                        else    // etc
                        {
                        }
                        break;

                    case 2:     // Field POR
                        if (nEquipTypeIdx == 0)     // Horizontal (BB & OH)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FieldPOR[0];
                            else    // >= 200kW
                                fValue = fANSI_FieldPOR[1];
                        }
                        else if (nEquipTypeIdx == 1)    // Vertical (VS)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FieldPOR[2];
                            else    // >= 200kW
                                fValue = fANSI_FieldPOR[3];
                        }
                        else    // etc
                        {
                        }
                        break;

                    case 3:     // Field AOR
                        if (nEquipTypeIdx == 0)     // Horizontal (BB & OH)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FieldAOR[0];
                            else    // >= 200kW
                                fValue = fANSI_FieldAOR[1];
                        }
                        else if (nEquipTypeIdx == 1)    // Vertical (VS)
                        {
                            if (nInputPower < 200)  // < 200kW
                                fValue = fANSI_FieldAOR[2];
                            else    // >= 200kW
                                fValue = fANSI_FieldAOR[3];
                        }
                        else    // etc
                        {
                        }
                        break;

                    default:
                        break;
                }
                break;

            case 1:     // API 610
                if (nEquipTypeIdx == 0)     // Horizontal (BB & OH)
                {
                    if (nRPM <= 3600 && nInputPower <= 300)
                        fValue = fAPI_Overall[0];   // 3
                    else
                    {
                        double value1, value2;
                        value1 = 3 * Math.pow(((double)nRPM / (double)3600), 0.3);
                        value2 = Math.pow(((double)nInputPower / (double)300), 0.21);
                        fValue = (float)(value1 * value2);
                    }
                }
                else if (nEquipTypeIdx == 1)    // Vertical (VS)
                {
                    fValue = fAPI_Overall[1];   // 5
                }
                else    // etc
                {
                }
                break;

            case 2:     // ISO 10816 Cat.1
                switch (nTypeIdx)
                {
                    case 0:     // Alarm
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_Alarm[0];
                        else    // > 200kW
                            fValue = fISOcategory_Alarm[1];
                        break;

                    case 1:     // Trip
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_Trip[0];
                        else    // > 200kW
                            fValue = fISOcategory_Trip[1];
                        break;

                    case 2:     // SAT POR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_SATPOR[0];
                        else    // > 200kW
                            fValue = fISOcategory_SATPOR[1];
                        break;

                    case 3:     // SAT AOR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_SATAOR[0];
                        else    // > 200kW
                            fValue = fISOcategory_SATAOR[1];
                        break;

                    case 4:     // FAT POR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_FATPOR[0];
                        else    // > 200kW
                            fValue = fISOcategory_FATPOR[1];
                        break;

                    case 5:     // FAT AOR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_FATAOR[0];
                        else    // > 200kW
                            fValue = fISOcategory_FATAOR[1];
                        break;

                    default:
                        break;
                }
                break;

            case 3:     // ISO 10816 Cat.2
                switch (nTypeIdx)
                {
                    case 0:     // Alarm
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_Alarm[2];
                        else    // > 200kW
                            fValue = fISOcategory_Alarm[3];
                        break;

                    case 1:     // Trip
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_Trip[2];
                        else    // > 200kW
                            fValue = fISOcategory_Trip[3];
                        break;

                    case 2:     // SAT POR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_SATPOR[2];
                        else    // > 200kW
                            fValue = fISOcategory_SATPOR[3];
                        break;

                    case 3:     // SAT AOR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_SATAOR[2];
                        else    // > 200kW
                            fValue = fISOcategory_SATAOR[3];
                        break;

                    case 4:     // FAT POR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_FATPOR[2];
                        else    // > 200kW
                            fValue = fISOcategory_FATPOR[3];
                        break;

                    case 5:     // FAT AOR
                        if (nInputPower <= 200)  // <= 200kW
                            fValue = fISOcategory_FATAOR[2];
                        else    // > 200kW
                            fValue = fISOcategory_FATAOR[3];
                        break;

                    default:
                        break;
                }
                break;

            case 4:     // Project VIB Spec.
                fValue = Float.parseFloat(editTextProjectVibSpec.getText().toString()); // added by hslee 2020.03.12
                break;

            default:
                break;
        }

        return fValue;
    }
}
