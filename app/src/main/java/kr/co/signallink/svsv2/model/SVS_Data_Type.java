package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Data_Type
    {
        //..고정크기 다차원 배열 선언은 어떻게???
        // 고정크기 버퍼 선언(fixed)은 1차원 배열만 가능
        // AXIS_MAX가 1이기 때문에, 1차원 배열로 처리
        //public fixed float fTime[Constants.AXIS_MAX][Constants.TIME_ELE];
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.TIME_ELE)]
        public float[] fTime;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.FREQ_ELE)]
        public float[] fFreq;
    }