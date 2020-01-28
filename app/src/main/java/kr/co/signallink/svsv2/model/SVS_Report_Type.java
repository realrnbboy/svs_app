package kr.co.signallink.svsv2.model;

    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Report_Type
    {
        public SVS_TIME_Type sTimeAvg;
        public SVS_TIME_Type sTimeDev;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqAvg;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreqDev;
    }