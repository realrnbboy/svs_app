package kr.co.signallink.svsv2.model;

//#region [Protocol 구조 정의]
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_FREQUENCY_Type
    {
        ////[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.AXIS_MAX)]
        //public float[] dPeak;
        ////[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.AXIS_MAX)]
        //public float[] dBand;
        public float dPeak;
        public float dBand;
    }