package kr.co.signallink.svsv2.model;

    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Alarm_Type
    {
        public int uTime;
        public int uScale;
        public SVS_Item_Type sItem;
        public SVS_Data_Type sData;
    }