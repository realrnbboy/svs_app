package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Work_Type
    {
        public int nTemp;
        public SVS_TIME_Type sTime;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.BAND_MAX)]
        public SVS_FREQUENCY_Type[] sFreq;
    }