package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_CODE_Type
    {
        public int uTempEna;
        public int uTempWrn;
        public int uTempDan;
        public SVS_TIME_Type sTimeEna;
        public SVS_TIME_Type sTimeWrn;
        public SVS_TIME_Type sTimeDan;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqEna;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqMin;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqMax;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqWrn;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqDan;
    }