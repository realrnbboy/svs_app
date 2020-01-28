package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Event_Type
    {
        public int uTime;
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 2)]
        public SVS_Alarm_Type[] sEvent;
    }