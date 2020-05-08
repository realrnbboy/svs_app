package kr.co.signallink.svsv2.databases;

import java.util.ArrayList;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.utils.StringUtil;

@RealmClass
public class SVSEntity extends RealmObject {

    @Required
    @PrimaryKey
    private String uuid = StringUtil.makeUUID();

    private int fwVer = -1; //펌웨어 버전
    private String serialNum;
    private String name;
    private String address;
    private String imageUri;
    private String lastRecord;
    private String SVS_LOCATION;
    private String SVS_STATE = DefConstant.SVS_STATE.DEFAULT.name();
    private String PLCState = DefConstant.PLCState.OFF.name();
    private String MEASURE_OPTION = DefConstant.MEASURE_OPTION.RAW_NONE.name();
    private int MEASURE_OPTION_COUNT = 0; //MeasureOption을 실행할 횟수

    //parent
    private String parentUuid;

    //history (TODO, 아직은 히스토리 기능을 넣지 않고 파일로 기록하는 기능으로 되어 있음)
    @Ignore
    private ArrayList<HistoryEntity> historyEntities = new ArrayList<>();

    //Getter Setter


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getFwVer() {
        return fwVer;
    }

    public void setFwVer(int fwVer) {
        this.fwVer = fwVer;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
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

    public DefFile.SVS_LOCATION getSvsLocation() {
        return DefFile.SVS_LOCATION.getEnumForName(SVS_LOCATION);
    }

    public void setSvsLocation(DefFile.SVS_LOCATION svsLocation) {
        this.SVS_LOCATION = svsLocation.name();
    }

    public DefConstant.SVS_STATE getSvsState() {
        return DefConstant.SVS_STATE.getEnumByName(SVS_STATE);
    }

    public void setSvsState(DefConstant.SVS_STATE svsState) {
        if( svsState != null )  // added by hslee 2020.04.28
            this.SVS_STATE = svsState.name();
    }

    public DefConstant.PLCState getPlcState() {
        return DefConstant.PLCState.getEnumByName(PLCState);
    }

    public void setPlcState(DefConstant.PLCState plcState) {
        this.PLCState = plcState.name();
    }

    public DefConstant.MEASURE_OPTION getMeasureOption() {
        DefConstant.MEASURE_OPTION measure_option = DefConstant.MEASURE_OPTION.getEnumByName(MEASURE_OPTION);
        if(measure_option != null){
            return measure_option;
        }
        else
        {
            return DefConstant.MEASURE_OPTION.RAW_NONE;
        }
    }

    public void setMeasureOption(DefConstant.MEASURE_OPTION measureOption) {
        this.MEASURE_OPTION = measureOption.name();
    }

    public int getMeasureOptionCount() {
        return MEASURE_OPTION_COUNT;
    }

    public void setMeasureOptionCount(int MEASURE_OPTION_COUNT) {
        this.MEASURE_OPTION_COUNT = MEASURE_OPTION_COUNT;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public ArrayList<HistoryEntity> getHistoryEntities() {
        return historyEntities;
    }


}
