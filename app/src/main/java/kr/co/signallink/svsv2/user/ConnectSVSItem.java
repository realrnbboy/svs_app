package kr.co.signallink.svsv2.user;

import kr.co.signallink.svsv2.commons.DefFile;

public class ConnectSVSItem {

    private String svsUuid;
    private String deviceName;
    private String address;
    private DefFile.SVS_LOCATION svsLocation;
    public String serialNo;

    //////////////////////////


    public String getSvsUuid() {
        return svsUuid;
    }

    public void setSvsUuid(String svsUuid) {
        this.svsUuid = svsUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DefFile.SVS_LOCATION getSvsLocation() {
        return svsLocation;
    }

    public void setSvsLocation(DefFile.SVS_LOCATION svsLocation) {
        this.svsLocation = svsLocation;
    }
}
