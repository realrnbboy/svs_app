package kr.co.signallink.svsv2.model;



    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class DIAGNOSIS_DATA_Type
    {
        public float fSamplingRate;
        public float fRMS;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.FREQ_ELE)]
        //public double[] dFreq;
        public float[] dFreq;  // added by hslee
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.FREQ_ELE)]
        //public double[] dPwrSpectrum;
        public float[] dPwrSpectrum;   // added by hslee
    }
    //#endregion [HDEC 자료 구조 정의]
