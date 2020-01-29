package kr.co.signallink.svsv2.commons;

import android.graphics.Color;

import java.util.Arrays;
import java.util.List;

import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;

/**
 * Created by nspil on 2018-02-07.
 */

public class DefConstant {
    private DefConstant() {};

    public static final String 보안키 = "1234520191313210";

    public static final String DEFAULT_SERVER_IP = "58.150.28.162";
    public static final String DEFAULT_SERVER_PORT = "5010";

    public static final String WEB_URL = "http://58.150.28.162";
    public static final String WEB_FILE_TEST_URL = "http://www.script-tutorials.com/demos/199/index.html";

    public static final String API_URL = "http://58.150.28.162/";


    public static final int SVSTRASACTION_INIT = 0;
    public static final int SVSTRASACTION_ING = 1;
    public static final int SVSTRASACTION_DONE = 2;

    public static final int REQUEST_SELECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    public static final int UART_PROFILE_CONNECTED = 30;
    public static final int UART_PROFILE_DISCONNECTED = 21;

    public static final int DELAYTIME = 100;
    public static final int PERIODTIME = 100;
    public static final int TIME_LIMIT = (1 * 60 * 1000) / PERIODTIME;
    public static final int RETRY_SEND_TIME = 5 * 1000;

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10001;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10002;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10003;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 10004;



    // added by hslee
    public static final int CONNECTION_WAIT_TIME = 50000;  // 연결 대기 시간

    public static final int URL_TYPE_GET_CAUSE = 1;
    public static final String URL_GET_CAUSE = "/setting/exe_get_cause";
    public static final int URL_TYPE_GET_FEATURE = 2;
    public static final String URL_GET_FEATURE = "/setting/exe_get_feature";
    public static final int URL_TYPE_GET_PRESET = 3;
    public static final String URL_GET_PRESET = "/setting/exe_get_preset";

    public static final int URL_TYPE_SERVER_NO_RESPONSE = 999;




    public static final int RECORDCOUNT_MAX = 5;

    public static final int DESCENDING_ORDER = -1;
    public static final int ASCENDING_ORDER = 1;

    public static final int EQUIPMENTNAME_MAX = 15;


    //SCREEN 모드
    public enum SCREEN_MODE {
        UNKNOWN("SCREEN_MODE_UNKNOWN"), //초기
        IDLE("SCREEN_MODE_IDEL"),//선택이 필요한 단계
        LOCAL("SCREEN_MODE_LOCAL"), //로컬 모드를 선택함.
        WEB("SCREEN_MODE_WEB"); //웹 모드를 선택함.

        private String mode;
        private SCREEN_MODE(String mode){
            this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }

        public static SCREEN_MODE getEnumByName(String mode){
            for(SCREEN_MODE screenMode : SCREEN_MODE.values()){
                if(screenMode.mode.equals(mode)){
                    return screenMode;
                }
            }

            return null;
        }

    }

    //SVS 상태
    public enum SVS_STATE {
        DEFAULT("", "#000000"),
        NORMAL ("N", "#ff61ff3d"),
        WARNING ("W", "#ffffe53d"),
        DANGER ("D", "#fff20a0a");

        private String shortState;
        private int color;
        private SVS_STATE(String shortState, String strColor){
            this.shortState = shortState;
            this.color = Color.parseColor(strColor);
        }

        public int getIntColor(){
            return color;
        }

        public String getShortState() {
            return shortState;
        }

        @Override
        public String toString() {
            return shortState;
        }

        public static SVS_STATE getEnumByName(String name){
            for(SVS_STATE svsState : SVS_STATE.values()){
                if(svsState.name().equals(name)){
                    return svsState;
                }
            }

            return null;
        }

        public static SVS_STATE getEnumByShortState(String shortState){

            for(SVS_STATE svsState : SVS_STATE.values()){
                if(svsState.shortState.equals(shortState)){
                    return svsState;
                }
            }

            return DEFAULT;
        }
    }


    //측정 카테고리
    public enum DIAGNOSIS_CATEGORY {

        SVS_SERIAL(0),
        OVERRALLSETTING(1),
        LEARNINGPARAMETER(2),
        DIAGNOSISDECISIONPARAMETERS(3),
        MODE_DIAGNOSIS(4),
        DATALOGGING(5),
        TEMPERATURE_DIAGNOSIS(6),
        TIMECODES_DIAGNOSIS(7),
        BEEP_AND_OR_CONDITION(8),
        FREQUENCYPEAKCODES_DIAGNOSIS(9),
        FREQUENCYBANDCODES_DIAGNOSIS(10),
        FFTCURVELIMIT_DIAGNOSIS(11);

        private int index;
        private DIAGNOSIS_CATEGORY(int index) {
            this.index = index;
        }

        public int getIndex(){
            return index;
        }
    }

    //트랜드 밸류
    public enum TrendValue {

        TEMPERATURE("Temperature",0),
        DPEAK("dPeak",0),
        DRMS("dRms",1),
        DCRF("dCrf",2),

        PEAK1("Peak1", 0),
        PEAK2("Peak2", 1),
        PEAK3("Peak3", 2),
        PEAK4("Peak4", 3),
        PEAK5("Peak5", 4),
        PEAK6("Peak6", 5),

        BAND1("Band1", 0),
        BAND2("Band2", 1),
        BAND3("Band3", 2),
        BAND4("Band4", 3),
        BAND5("Band5", 4),
        BAND6("Band6", 5),

        RAW_TIME("Time Domain", 0),
        RAW_FREQ("Freq Domain", 1),

        UNKNOWN("Unknown", 10);

        private String title;
        private int index;
        private TrendValue(String title, int index){
            this.title = title;
            this.index = index;
        }

        public String getTitle() {
            return title;
        }

        public int getIndex() {
            return index;
        }

        public static TrendValue findTrendValue(String title){
            for(TrendValue trendValue : TrendValue.values()){
                if(trendValue.title.equals(title)){
                    return trendValue;
                }
            }

            return UNKNOWN;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    //트렌드 타입
    public enum TrendType {

        ETCS("etc", Arrays.asList(TrendValue.TEMPERATURE, TrendValue.DPEAK, TrendValue.DRMS, TrendValue.DCRF)),
        PEAKS("peak", Arrays.asList(TrendValue.PEAK1, TrendValue.PEAK2, TrendValue.PEAK3, TrendValue.PEAK4, TrendValue.PEAK5, TrendValue.PEAK6)),
        BANDS("band", Arrays.asList(TrendValue.BAND1, TrendValue.BAND2, TrendValue.BAND3, TrendValue.BAND4, TrendValue.BAND5, TrendValue.BAND6)),
        RAWS("raw", Arrays.asList(TrendValue.RAW_TIME, TrendValue.RAW_FREQ));


        private String title;
        private List<TrendValue> valueList;
        private TrendType(String title, List<TrendValue> valueList){
            this.title = title;
            this.valueList = valueList;
        }

        public TrendValue getTrendValue(int index){
            return this.valueList.get(index);
        }

        public boolean hasTrendValue(TrendValue trendValue){

            for(TrendValue tv : valueList){
                if(tv == trendValue){
                    return true;
                }
            }

            return false;
        }

    }

    //PLC 작동 상태
    public enum PLCState {
        ON("true"),
        OFF("false");

        private String state;
        private PLCState(String state){
            this.state = state;
        }

        public PLCState toggle(){
            if(this == OFF){
                return ON;
            }

            return OFF;
        }

        public boolean getBoolean(){
            if(this == ON){
                return true;
            }

            return false;
        }

        public static PLCState getEnumByName(String name){
            for(PLCState plcState : PLCState.values()){
                if(plcState.name().equals(name)){
                    return plcState;
                }
            }
            return null;
        }

        public static PLCState findPLCState(boolean bState){

            if(bState){
                return ON;
            }

            return OFF;
        }

        public static PLCState findPLCState(String strState){
            if(strState != null && !strState.isEmpty()){
                if(strState.equals(ON.toString())) {
                    return ON;
                }
            }

            return OFF;
        }

        @Override
        public String toString() {
            return state;
        }
    }




    //Measure Raw
    public enum MEASURE_OPTION {

        MEASURE_V4(0),
        RAW_NONE(1),
        RAW_WITH_TIME(2),
        RAW_WITH_FREQ(3),
        RAW_WITH_TIME_FREQ(4);

        private int index;
        private MEASURE_OPTION(int index){
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static MEASURE_OPTION getEnumByName(String name){

            for(MEASURE_OPTION measureOption : MEASURE_OPTION.values()){
                if(measureOption.name().equals(name)){
                    return measureOption;
                }
            }

            return RAW_NONE;
        }

        @Override
        public String toString() {

            if(this == MEASURE_V4){
                return "Pre Version";
            }
            else if(this == RAW_NONE){
                return "None";
            }
            else if(this == RAW_WITH_TIME){
                return "Time";
            }
            else if(this == RAW_WITH_FREQ){
                return "Spectrum";
            }
            else if(this == RAW_WITH_TIME_FREQ){
                return "All";
            }

            return super.toString();
        }
    }

    //펌웨어
    public enum FwVer {
        SupportRawMeasure(514),     //RawMeasure를 처음 지원한 버전
        SupportBeepAndFlag(514);    //Beep And Flag를 지원하기 시작한 버전

        private int fwVer;
        private FwVer(int ver){
            this.fwVer = ver;
        }

        public int getFwVer() {
            return fwVer;
        }
    }

    //기기 모델
    public enum DeviceModel {
        Unknown(null),
        SVS40("SVS40"),
        SVS40D("SVS40D"),
        SVS40D_BT("SVS40DB"),
        SVS60("SVS60");

        private String modelName;
        private DeviceModel(String modelName){
            this.modelName = modelName;
        }

        public static DeviceModel findDeviceModel(String serialNum){

            for(DeviceModel deviceModel : DeviceModel.values()){

                if(deviceModel.modelName != null && serialNum!=null){

                    //공백이 추가되어 있거나 언더바가 포함되어 있을 경우. 찾음.
                    if(serialNum.startsWith(deviceModel.modelName+" ") || serialNum.startsWith(deviceModel.modelName+"_")) {
                        return deviceModel;
                    }
                }
            }

            return Unknown;
        }

        //PLC 기능 사용 여부
        public static boolean canPlcFunction(SVSEntity svsEntity)
        {
            String serialNum = svsEntity.getSerialNum();
            DeviceModel deviceModel = findDeviceModel(serialNum);

            //SVS40D_BT 모델만 PLC를 사용할 수 없음
            if(deviceModel == SVS40D_BT){
                return false;
            }

            return true;
        }

        //PLC 기능 사용 여부
        public static boolean canPlcFunction(WSVSEntity wsvsEntity)
        {
            String serialNum = wsvsEntity.serial_no;
            DeviceModel deviceModel = findDeviceModel(serialNum);

            //SVS40D_BT 모델만 PLC를 사용할 수 없음
            if(deviceModel == SVS40D_BT){
                return false;
            }

            return true;
        }
    }

}
