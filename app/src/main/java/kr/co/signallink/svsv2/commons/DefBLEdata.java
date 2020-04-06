package kr.co.signallink.svsv2.commons;

public class DefBLEdata {

    public enum CMD {

        NONE(-1),
        HELLO(0),
        UPLOAD(1),

        EVENT_WARNING(2),
        EVENT_DANGER(3),
        PLC_ON(4),
        PLC_OFF(5),
        BAT(6),
        LEARNING(7),

        MEASURE_V4(9),
        MEASURE_OPTION_NONE(10),
        //MEASURE_OPTION_NONE(0xf0),  // for test
        //MEASURE_OPTION_WITH_TIME(0x1f),
        MEASURE_OPTION_WITH_TIME(11),
        MEASURE_OPTION_WITH_FREQ(12),
        //MEASURE_OPTION_WITH_FREQ(0x2f),
        MEASURE_OPTION_WITH_TIME_FREQ(13);
        //MEASURE_OPTION_WITH_TIME_FREQ(0x0f);

        final private int cmd;
        private CMD(int cmd){
            this.cmd = cmd;
        }
    }

    public static final int RESPONSECOMMAND_ARRIVE = 0;
    public static final int RESPONSECOMMAND_DISCONNECT = 1;

    public final static String DISCOVERED_ARRIVE = "kr.co.signallink.svs.DISCOVERED_ARRIVE";
    public final static String DISCONNECTION_ARRIVE = "kr.co.signallink.svs.DISCONNECTION_ARRIVE";
    public final static String HELLO_ARRIVE = "kr.co.signallink.svs.HELLO_ARRIVE";
    public final static String UPLOAD_ARRIVE = "kr.co.signallink.svs.UPLOAD_ARRIVE";
    public final static String UPLOAD_NOTARRIVE = "kr.co.signallink.svs.UPLOAD_NOTARRIVE";
    public final static String MEASURE_PERCENT = "kr.co.signallink.svs.MEASURE_PERCENT";
    public final static String MEASURE_ARRIVE = "kr.co.signallink.svs.MEASURE_ARRIVE";
    public final static String EVENTWARNING_ARRIVE = "kr.co.signallink.svs.EVENTWARNING_ARRIVE";
    public final static String EVENTDANGER_ARRIVE = "kr.co.signallink.svs.EVENTDANGER_ARRIVE";
    public final static String BAT_ARRIVE = "kr.co.signallink.svs.BAT_ARRIVE";
    public final static String LEARNING_ARRIVE = "kr.co.signallink.svs.LEARNING_ARRIVE";

}
