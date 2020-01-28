package kr.co.signallink.svsv2.model;

    // SVS Config Info
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SERVER_CONFIG_Type
    {
        public SERVER_HEADER_Type header;
        public SVS_DATETIME_Type datetime;
        public SVS_Param_Type param;
    }