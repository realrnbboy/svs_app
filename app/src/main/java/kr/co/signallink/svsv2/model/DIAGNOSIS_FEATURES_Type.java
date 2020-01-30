package kr.co.signallink.svsv2.model;


import java.io.Serializable;

//[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class DIAGNOSIS_FEATURES_Type implements Serializable
    {
        public int nCount;  // Cause 수
        public FEATURE_INFO_Type[] infos;
    }
