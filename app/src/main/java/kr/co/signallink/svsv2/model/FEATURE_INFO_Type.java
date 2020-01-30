package kr.co.signallink.svsv2.model;


import java.io.Serializable;

// MATRIX 1
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class FEATURE_INFO_Type implements Serializable
    {

        public int nCauseNo;
        public float[] fValues;
    }