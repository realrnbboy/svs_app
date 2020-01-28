package kr.co.signallink.svsv2.model;


    // Server & Manager Protocol
    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SERVER_HEADER_Type
    {
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = 32)]
        public byte[] svsID;
        public byte msgID;  // 0x03: Config Info, 0x04: Measure Data
        public int length;
    }
