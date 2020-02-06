package kr.co.signallink.svsv2.dto;

import java.io.Serializable;

import kr.co.signallink.svsv2.model.DIAGNOSIS_FEATURES_Type;
import kr.co.signallink.svsv2.model.CauseModel;
import kr.co.signallink.svsv2.model.VARIABLES_1_Type;
import kr.co.signallink.svsv2.model.VARIABLES_2_Type;

// added by hslee
// PresetEntity와 같은 구조이나 presetEntity는 직렬화가 불가능하여
// 개발의 편의를 위해 data만 저장하는 클래스를 만듦.
public class AnalysisData implements Serializable {

    private int no; // index

    private String name;
    private int code;   // 0:ANSI HI 9.6.4, 1:API 610, 2:ISO 10816 Cat.1, 3:ISO 10816 Cat.2, 4:Project VIB Spec.
    private int projectVibSpec;
    private String siteCode;
    private String equipmentName; // Pump, Pipe Name
    private String tagNo;
    private int inputPower;
    private int lineFreq;   // 50 or 60Hz
    private int equipmentType;  // Pump type, 0:Horizontal(BB&OH), 1:Vertical(VC), 2:Etc
    private int rpm;
    private int bladeCount;
    private int bearingType;    // 0:Ball, 1:Roller, 2:Journal, 3:Etc
    private int ballCount;
    private int pitchDiameter;
    private int ballDiameter;
    private int rps;
    private int contactAngle;

    public ResultDiagnosisData[] resultDiagnosis; // 진단 rank 결과, 순위 내림 차순 정렬된 것

    public VARIABLES_1_Type diagVar1;
    public VARIABLES_2_Type valueVar2, rangeVar2, lowerVar2, upperVar2;

    public DIAGNOSIS_FEATURES_Type featureInfos;

    public MeasureData measureData1;
    public MeasureData measureData2;
    public MeasureData measureData3;

    public AnalysisData(){
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getProjectVibSpec() {
        return projectVibSpec;
    }

    public void setProjectVibSpec(int projectVibSpec) {
        this.projectVibSpec = projectVibSpec;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getTagNo() {
        return tagNo;
    }

    public void setTagNo(String tagNo) {
        this.tagNo = tagNo;
    }

    public int getInputPower() {
        return inputPower;
    }

    public void setInputPower(int inputPower) {
        this.inputPower = inputPower;
    }

    public int getLineFreq() {
        return lineFreq;
    }

    public void setLineFreq(int lineFreq) {
        this.lineFreq = lineFreq;
    }

    public int getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(int equipmentType) {
        this.equipmentType = equipmentType;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getBladeCount() {
        return bladeCount;
    }

    public void setBladeCount(int bladeCount) {
        this.bladeCount = bladeCount;
    }

    public int getBearingType() {
        return bearingType;
    }

    public void setBearingType(int bearingType) {
        this.bearingType = bearingType;
    }

    public int getBallCount() {
        return ballCount;
    }

    public void setBallCount(int ballCount) {
        this.ballCount = ballCount;
    }

    public int getPitchDiameter() {
        return pitchDiameter;
    }

    public void setPitchDiameter(int pitchDiameter) {
        this.pitchDiameter = pitchDiameter;
    }

    public int getBallDiameter() {
        return ballDiameter;
    }

    public void setBallDiameter(int ballDiameter) {
        this.ballDiameter = ballDiameter;
    }

    public int getRps() {
        return rps;
    }

    public void setRps(int rps) {
        this.rps = rps;
    }

    public int getContactAngle() {
        return contactAngle;
    }

    public void setContactAngle(int contactAngle) {
        this.contactAngle = contactAngle;
    }

    public VARIABLES_1_Type getDiagVar1() {
        return diagVar1;
    }

    public void setDiagVar1(VARIABLES_1_Type diagVar1) {
        this.diagVar1 = diagVar1;
    }

    public VARIABLES_2_Type getValueVar2() {
        return valueVar2;
    }

    public void setValueVar2(VARIABLES_2_Type valueVar2) {
        this.valueVar2 = valueVar2;
    }

    public VARIABLES_2_Type getRangeVar2() {
        return rangeVar2;
    }

    public void setRangeVar2(VARIABLES_2_Type rangeVar2) {
        this.rangeVar2 = rangeVar2;
    }

    public VARIABLES_2_Type getLowerVar2() {
        return lowerVar2;
    }

    public void setLowerVar2(VARIABLES_2_Type lowerVar2) {
        this.lowerVar2 = lowerVar2;
    }

    public VARIABLES_2_Type getUpperVar2() {
        return upperVar2;
    }

    public void setUpperVar2(VARIABLES_2_Type upperVar2) {
        this.upperVar2 = upperVar2;
    }

    public DIAGNOSIS_FEATURES_Type getFeatureInfos() {
        return featureInfos;
    }

    public void setFeatureInfos(DIAGNOSIS_FEATURES_Type featureInfos) {
        this.featureInfos = featureInfos;
    }

    public ResultDiagnosisData[] getResultDiagnosis() {
        return resultDiagnosis;
    }

    public void setResultDiagnosis(ResultDiagnosisData[] resultDiagnosis) {
        this.resultDiagnosis = resultDiagnosis;
    }

    public MeasureData getMeasureData1() {
        return measureData1;
    }

    public void setMeasureData1(MeasureData measureData1) {
        this.measureData1 = measureData1;
    }

    public MeasureData getMeasureData2() {
        return measureData2;
    }

    public void setMeasureData2(MeasureData measureData2) {
        this.measureData2 = measureData2;
    }

    public MeasureData getMeasureData3() {
        return measureData3;
    }

    public void setMeasureData3(MeasureData measureData3) {
        this.measureData3 = measureData3;
    }
}
