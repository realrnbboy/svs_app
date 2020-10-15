package kr.co.signallink.svsv2.model;

// using System;
// using System.Collections.Generic;
// using System.Linq;
// using System.Text;
// using System.Threading.Tasks;

// using System.Runtime.InteropServices;

// namespace hdSVSM2
public class Constants {
    public static final boolean DEBUG = false;

    // Serial 통신 관련
    public static final int MAX_RX_SIZE = 10240;
    public static final int BaudRate_DT = 500000;
    public static final int BaudRate_Normal = 921600;

    public static final byte STX = 0x5A;
    public static final byte CHECKSUM_INIT = (byte) 0xA5;
    public static final int MAX_TRANSFER_SIZE = 1024;
    public static final int XFR_MAX = 2048;
    public static final int MAX_DATA = 40960;   // 2020.10.14

    // 자료 구조 관련
    public static final int MAX_SVS = 3;
    public static final int AXIS_MAX = 1;
    public static final int BAND_MAX = 6;
    public static final int FFT_NUM = 2048; // added by hslee for svs 60
    //public static final int FFT_NUM = 1024; // added by hslee for svs 40
    public static final int TIME_ELE = FFT_NUM;
    public static final int FREQ_ELE = FFT_NUM / 2;

    public static final int LENGTH_HELLO_DATA = 49; // 2019.06
    public static final int LENGTH_SERIALNO = 32; // 2019.06
    // public static final int LENGTH_UPLOAD_DATA = 6673;
    public static final int LENGTH_UPLOAD_DATA = 12821; // 2019.08  // 2020.10.14
    public static final int LENGTH_MEASURE_DATA = 12373;    // 2020.10.14

    // 타이머 관련
    public static final int TIMEOUT = 1000;
    public static final int TIMER_DOWNLOAD = 1000;
    public static final int TIMER_RSP_BT_REPLY = 1500;
    public static final int TIMER_RSP_BT_EVENT = 20000;
    public static final int TIMER_RSP_BT_MEASURE = 2000;

    // Encryption Key
    public static final String encryptKeys = "DODO1SVS"; // 반드시 8글자

    // for HDEC
    public static final int MAX_PRESET = 100;
    public static final int FEATURE_COUNT = 25;
    public static final int RANGE_COUNT = FEATURE_COUNT - 2;    // 2020.10.14
    public static final int FEQ_LOWER_LIMIT = 5;         // 2020.09 // 2020.10.14
    public static final int FEQ_UPPER_LIMIT = 1024;    // 2020.09   // 2020.10.14

    public static final int MAX_PIPE_X_VALUE = 300; // added by hslee 2020.06.08
}
