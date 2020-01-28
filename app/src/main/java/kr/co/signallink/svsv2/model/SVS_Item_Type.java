package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Item_Type
    {
        public int nTemp;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 3)]
        public float[][] fTime;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 12)]
        public float[][][] fFreq;
    }