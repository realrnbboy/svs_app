package kr.co.signallink.svsv2.model;

    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class SVS_Hello_Type
    {
        public int uFwVer;       // F/W Version
        public int uHwVer;       // H/W Version
        public int uStatusCode;  // status code
        public int uCustomerId;  // customer ID
        //[MarshalAs(UnmanagedType.ByValArray, SizeConst = Constants.LENGTH_SERIALNO)]
        public byte[] SerialNo;
    }