package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Param_Type
    {
        public int nIntervalTime;
        public int nMesRange;
        public int nMesAxis;
        public int nOfsRemoval;
        public float fOfsAdjust;
        public int nDataConv;
        public float fSensitivity;
        public int nSampleFreq;
        public int nFftAvg;
        public int nLearnCnt;
        public float fLearnOffset;
        public int nLearnDev;
        public float fFftCurveOffset;
        public int nLimitResolution;
        public int nDanLimit;
        public int nWrnCnt;
        public int nDanCnt;
        public int nPSaveControl;
        public int nPSaveValue;
        public float fPSaveLevel;
        public int nPSaveCnt;
        public int nWrnLog;
        public int nDanLog;
        public int nTpEna;
        public int nTpWrn;
        public int nTpDan;
        public int nFcEnable;
        public int nFcWrn;
        public int nFcDan;
        public int nAndFlag; // 1: AND, 0: OR
        public SVS_CODE_Type sCode;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.TIME_ELE)]
        public float[] fTpData;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.FREQ_ELE)]
        public float[] fFcData;
    }