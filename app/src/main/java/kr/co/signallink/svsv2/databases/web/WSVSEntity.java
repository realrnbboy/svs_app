package kr.co.signallink.svsv2.databases.web;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import kr.co.signallink.svsv2.commons.DefConstant;

public class WSVSEntity extends RealmObject {

    @PrimaryKey
    public String id;

    public String name;
    public String picture;
    public String model;
    public String serial_no;
    public String position;
    public String created;

    //기존 SVSEntity에서 사용하던 변수
    public int fwVer = -1; //펌웨어 버전
    public String address;
    public String PLCState = DefConstant.PLCState.OFF.name();
    public String MEASURE_OPTION = DefConstant.MEASURE_OPTION.RAW_NONE.name();
    public int MEASURE_OPTION_COUNT = 0; //MeasureOption을 실행할 횟수

    //parents
    public String company_id;
    public String equipment_id;

    @Ignore
    final String PARAM_ID = "id";
    @Ignore
    final String PARAM_NAME = "name";
    @Ignore
    final String PARAM_PICTURE = "picture";
    @Ignore
    final String PARAM_MODEL = "model";
    @Ignore
    final String PARAM_SERIAL_NO = "serial_no";
    @Ignore
    final String PARAM_POSITION = "position";
    @Ignore
    final String PARAM_CREATED = "created";


    public boolean putJson(JSONObject userJsonObject)
    {
        if(userJsonObject == null){
            return false;
        }
        if(!userJsonObject.has(PARAM_ID)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_NAME)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_PICTURE)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_MODEL)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_SERIAL_NO)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_POSITION)){
            return false;
        }
        else if(!userJsonObject.has(PARAM_CREATED)){
            return false;
        }

        try {
            id = userJsonObject.getString(PARAM_ID);
            name = userJsonObject.getString(PARAM_NAME);
            picture = userJsonObject.getString(PARAM_PICTURE);
            model = userJsonObject.getString(PARAM_MODEL);
            serial_no = userJsonObject.getString(PARAM_SERIAL_NO);
            position = userJsonObject.getString(PARAM_POSITION);
            created = userJsonObject.getString(PARAM_CREATED);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }



    ///////////////////////////////////
    // 아래는 기존에 사용하던 SVSEntity 함수들.

    public int getFwVer() {
        return fwVer;
    }

    public void setFwVer(int fwVer) {
        this.fwVer = fwVer;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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










}
