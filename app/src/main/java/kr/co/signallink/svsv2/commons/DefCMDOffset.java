package kr.co.signallink.svsv2.commons;

public class DefCMDOffset {

    private DefCMDOffset() {};

    public static final int BAND_MAX = 6;
    public static final int EVENT_EMPTY_SIZE = 6;
    public static final int EVENTDATA_ONESIZE = 72;
    public static final int EVENTDATA_WARNING = 0;
    public static final int EVENTDATA_DANGER = 1;
    public static final int MEASURE_AXIS_RATIO = 1;
    public static final int MEASURE_AXIS_TIME_ELE_MAX = 2048;   // added by hslee svs 60
    public static final int MEASURE_AXIS_FREQ_ELE_MAX = 1024;    // added by hslee for svs 60
//    public static final int MEASURE_AXIS_TIME_ELE_MAX = 1024;
//    public static final int MEASURE_AXIS_FREQ_ELE_MAX = 512;
    public static final int CMD_HEAD_LENGTH = 4;
    public static final int CMD_LENGTH_OFFSET = 2;
    public static final int CMD_LENGTH_SIZE = 2;
    public static final int CMD_MEASURE_LENGTH_SIZE = 85;
    public static final int CMD_UPLOAD_LENGTH_SIZE = 533;
    public static final int CMD_UPLOAD_LENGTH_LEGACY_SIZE = 529;

    //hello
    public static final int CMD_HELLO_OFFSET_FWVER = 4;
    public static final int CMD_HELLO_OFFSET_HWVER = 6;
    public static final int CMD_HELLO_OFFSET_SFI = 8;
    public static final int CMD_HELLO_OFFSET_CID = 12;
    public static final int CMD_HELLO_OFFSET_SN = 16;

    //bat
    public static final int CMD_BAT_OFFSET_PERCENT = 4;

    //upload
    public static final int CMD_UPLOAD_OFFSET_INTERVALTIME = 4;
    public static final int CMD_UPLOAD_OFFSET_MESRANGE = 8;
    public static final int CMD_UPLOAD_OFFSET_MESAXIS = 12;
    public static final int CMD_UPLOAD_OFFSET_OFSREMOVAL = 16;
    public static final int CMD_UPLOAD_OFFSET_OFSADJUST = 20;
    public static final int CMD_UPLOAD_OFFSET_DATACONV = 24;
    public static final int CMD_UPLOAD_OFFSET_SENSITIVITY = 28;
    public static final int CMD_UPLOAD_OFFSET_SPLFREQ = 32;
    public static final int CMD_UPLOAD_OFFSET_FFTAVG = 36;
    public static final int CMD_UPLOAD_OFFSET_LEARNCNT = 40;
    public static final int CMD_UPLOAD_OFFSET_LEARNOFFSET = 44;
    public static final int CMD_UPLOAD_OFFSET_LEARNDEV = 48;
    public static final int CMD_UPLOAD_OFFSET_FFTCURVEOFFSET = 52;
    public static final int CMD_UPLOAD_OFFSET_LIMITRESOLUTION = 56;
    public static final int CMD_UPLOAD_OFFSET_DANLIMIT = 60;
    public static final int CMD_UPLOAD_OFFSET_WRNCNT = 64;
    public static final int CMD_UPLOAD_OFFSET_DANCNT = 68;
    public static final int CMD_UPLOAD_OFFSET_PSAVECON = 72;  //MODE
    public static final int CMD_UPLOAD_OFFSET_PSAVEVALUE = 76;  //TRIGGER CODE
    public static final int CMD_UPLOAD_OFFSET_PSAVELEVEL = 80;  //TRIGGER VALUE
    public static final int CMD_UPLOAD_OFFSET_PSAVECNT = 84;
    public static final int CMD_UPLOAD_OFFSET_WRNLOG = 88;
    public static final int CMD_UPLOAD_OFFSET_DANLOG = 92;
    public static final int CMD_UPLOAD_OFFSET_TPENA = 96;
    public static final int CMD_UPLOAD_OFFSET_TPWRN = 100;
    public static final int CMD_UPLOAD_OFFSET_TPDAN = 104;
    public static final int CMD_UPLOAD_OFFSET_FCENA = 108;
    public static final int CMD_UPLOAD_OFFSET_FCWRN = 112;
    public static final int CMD_UPLOAD_OFFSET_FCDAN = 116;
    public static final int CMD_UPLOAD_OFFSET_ANDFLAG = 120;

    public static final int CMD_UPLOAD_SVSCODE_OFFSET_TMEPENA = 120;//6264;//120 + 4096 + 2048;
    public static final int CMD_UPLOAD_SVSCODE_OFFSET_TMEPWRN = 124;//6268;
    public static final int CMD_UPLOAD_SVSCODE_OFFSET_TMEPDAN = 128;//6272;

    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEENAPEK = 132;//6276;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEENARMS = 136;//6280;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEENACRF = 140;//6284;

    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNPEK = 144;//6288;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNRMS = 148;//6292;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNCRF = 152;//6296;

    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANPEK = 156;//6300;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANRMS = 160;//6304;
    public static final int CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANCRF = 164;//6308;

    public static final int CMD_UPLOAD_SVSFREQ_OFFSET_FREQENA = 168;//6312;
    public static final int CMD_UPLOAD_SVSFREQ_OFFSET_FREQMIN = 216;//6360; //6312 + 48
    public static final int CMD_UPLOAD_SVSFREQ_OFFSET_FREQMAX = 264;//6408;
    public static final int CMD_UPLOAD_SVSFREQ_OFFSET_FREQWRN = 312;//6456;
    public static final int CMD_UPLOAD_SVSFREQ_OFFSET_FREQDAN = 360;//6504;

    //measure
    public static final int CMD_MEASURE_OFFSET_SPLFREQMES = 4;
    public static final int CMD_MEASURE_OFFSET_DATACONV = 8;
    public static final int CMD_MEASURE_OFFSET_SCALEIDX = 12;
    public static final int CMD_MEASURE_OFFSET_ALARMCUR = 16;
    public static final int CMD_MEASURE_OFFSET_TEMPCUR = 20;
    public static final int CMD_MEASURE_SVSTIME_OFFSET_TIMECURPEK = 24;
    public static final int CMD_MEASURE_SVSTIME_OFFSET_TIMECURRMS = 28;
    public static final int CMD_MEASURE_SVSTIME_OFFSET_TIMECURCRF = 32;
    public static final int CMD_MEASURE_SVSFREQ_OFFSET_FREQCUR = 36;
    public static final int CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME = 84;
    public static final int CMD_MEASURE_SVSAXIS_OFFSET_AXISFREQ = CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME + 8192;   // added by hslee 2020.04.08
    //public static final int CMD_MEASURE_SVSAXIS_OFFSET_AXISFREQ = CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME + 4096;

    //event
    public static final int CMD_EVENT_OFFSET_FRAMENUMBER = 4;
    public static final int CMD_EVENT_SVSEVENT_OFFSET = 8;
    public static final int CMD_EVENT_SVSEVENT_OFFSET_FRAMENUMBER = 0;
    public static final int CMD_EVENT_SVSEVENT_OFFSET_SCALE = 4;
    public static final int CMD_EVNET_SVSEVENT_OFFSET_TEMP = 8;
    public static final int CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_PEK = 12;
    public static final int CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_RMS = 16;
    public static final int CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_CRF = 20;
    public static final int CMD_EVNET_SVSEVENT_SVSFREQ_OFFSET_FREQ = 24;
}
