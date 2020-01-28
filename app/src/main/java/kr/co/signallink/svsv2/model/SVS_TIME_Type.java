package kr.co.signallink.svsv2.model;



    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_TIME_Type
    {
        ////[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.AXIS_MAX)]
        //public float[] dPeak;
        ////[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.AXIS_MAX)]
        //public float[] dRMS;
        ////[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.AXIS_MAX)]
        //public float[] dCrF;
        public float dPeak;
        public float dRMS;
        public float dCrF;
    }
