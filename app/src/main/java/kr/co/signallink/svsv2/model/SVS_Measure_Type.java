package kr.co.signallink.svsv2.model;


    ////[StructLayout(LayoutKind.Sequential, Pack = 1)]
    //public class SVS_Measure_Type
    //{
    //    public byte headerSTX;
    //    public byte headerCMD;
    //    public short headerLength;
    //    public float fSplRate;
    //    public int nBandSel;
    //    public int nScal;
    //    public int nAlarm;
    //    public SVS_Work_Type sWork;
    //    public SVS_Data_Type sData;
    //    public byte CRC;
    //}

    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Measure_Type
    {
        public byte headerSTX;
        public byte headerCMD;
        public short headerLength;
        public float fSplRate;
        public int nBandSel;
        public int nScale;   // 2g ~ 16g
        public int nAlarm;
        public SVS_Work_Type sWork;
        public SVS_Data_Type sData;
        public byte CRC;
    }