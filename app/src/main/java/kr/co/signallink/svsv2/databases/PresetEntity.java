package kr.co.signallink.svsv2.databases;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass
public class PresetEntity extends RealmObject {

    @PrimaryKey
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

    public PresetEntity(){
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
}
