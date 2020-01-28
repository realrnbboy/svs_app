package kr.co.signallink.svsv2.model;


    // Measure
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SERVER_MEASURE_Type
    {
        public SERVER_HEADER_Type header;
        public SVS_DATETIME_Type datetime;
        public SVS_Measure_Type measure;
        public int nAnalysis;   // 0: Normal, 1: Warning, 2: Danger
    }