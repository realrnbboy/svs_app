package kr.co.signallink.svsv2.user;

import java.io.Serializable;
import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.databases.HistoryData;
import kr.co.signallink.svsv2.utils.StringUtil;

import static kr.co.signallink.svsv2.commons.DefConstant.PLCState.OFF;

public class RegisterSVSData implements Serializable {

    private String uuid;

    private String battery = ""; // added by hslee 2020.04.28
    private boolean bGetBatteryInfo = false; // added by hslee 2020.04.28
    private String name;
    private String address;
    private DefFile.SVS_LOCATION svsLocation;
    private DefConstant.SVS_STATE svsState;
    private DefConstant.PLCState plcState = OFF;
    private DefConstant.MEASURE_OPTION measureOption = DefConstant.MEASURE_OPTION.RAW_NONE;
    private String imageUri;
    private String lastRecord;
    private ArrayList<HistoryData> historyData = new ArrayList<>();

    private int rssi = Integer.MIN_VALUE;
    private boolean linked = false;

    private EquipmentData parentEquipmentData;

    public RegisterSVSData(){
        uuid = StringUtil.makeUUID();
        //uuid = "dhfjhsdjhfj";
        //..
    }

    public RegisterSVSData(EquipmentData parentEquipmentData){
        uuid = StringUtil.makeUUID();
        this.parentEquipmentData = parentEquipmentData;

    }

    /////////////////////////


    public boolean isbGetBatteryInfo() {
        return bGetBatteryInfo;
    }

    public void setbGetBatteryInfo(boolean bGetBatteryInfo) {
        this.bGetBatteryInfo = bGetBatteryInfo;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DefFile.SVS_LOCATION getSvsLocation() {
        return svsLocation;
    }

    public void setSvsLocation(String svsLocation) {
        setSvsLocation(DefFile.SVS_LOCATION.findDIR_forPHOTO(svsLocation));
    }

    public void setSvsLocation(DefFile.SVS_LOCATION svsLocation){
        this.svsLocation = svsLocation;

        //임시
        //this.uuid = StringUtil.md5(svsLocation.toString());   // deleted by hslee 2020.04.29
    }

    public DefConstant.PLCState getPlcState() {
        return plcState;
    }

    public void setPlcState(DefConstant.PLCState plcState) {
        this.plcState = plcState;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(String lastRecord) {
        this.lastRecord = lastRecord;
    }

    public DefConstant.SVS_STATE getSvsState() {
        return svsState;
    }

    public void setSvsState(DefConstant.SVS_STATE svsState) {
        this.svsState = svsState;
    }

    public DefConstant.MEASURE_OPTION getMeasureOption() {
        return measureOption;
    }

    public void setMeasureOption(DefConstant.MEASURE_OPTION measureOption) {
        this.measureOption = measureOption;
    }

    public ArrayList<HistoryData> getHistoryData() {
        return historyData;
    }

    public EquipmentData getParentEquipmentData() {
        return parentEquipmentData;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }
}
