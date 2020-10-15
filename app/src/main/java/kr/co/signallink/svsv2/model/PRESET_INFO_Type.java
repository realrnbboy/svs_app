package kr.co.signallink.svsv2.model;


    //[StructLayout(LayoutKind.Sequential, Pack = 1)]
    public class PRESET_INFO_Type
    {
        public int nNo;
        public String strName;
        public int nCode;   // 0:ANSI HI 9.6.4, 1:API 610, 2:ISO 10816 Cat.1, 3:ISO 10816 Cat.2, 4:Project VIB Spec.
        public int nCodeType;       // 2020.03.12   // 2020.10.14
        public float fCodeValue;    // 2020.03.12   // 2020.10.14
        public String strSiteCode;
        public String strEquipName; // Pump, Pipe Name
        public String strTagNo;
        public int nInputPower;
        public int nLineFreq;   // 50 or 60Hz
        public int nEquipType;  // Pump type, 0:Horizontal(BB&OH), 1:Vertical(VC), 2:Etc
        public int nRPM;
        public int nBladeCount;
        public int nBearingType;    // 0:Ball, 1:Roller, 2:Journal, 3:Etc
        public int nBallCount;
        public int nPitchDiameter;
        public int nBallDiameter;
        public int nRPS;
        public int nContactAngle;
    }