package kr.co.signallink.svsv2.model;


import java.io.Serializable;

// MATRIX 2
    // - 진동 데이터와 Variabl 2 연산한 결과
    // - Causes x Features
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class MATRIX_2_Type implements Serializable
    {
        public double[] aData1;
        public double[] aData2;
        public double[] aData3;
        public double[] aDataMax;
        public double[] aDataMed;
        public double[] aResult;
        public float rms1; // added by hslee 2020.07.15
        public float rms2; // added by hslee 2020.07.15
        public float rms3; // added by hslee 2020.07.15
    }