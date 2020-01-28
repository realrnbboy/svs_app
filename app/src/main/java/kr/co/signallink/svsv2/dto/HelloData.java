package kr.co.signallink.svsv2.dto;

/**
 * Created by nspil on 2018-02-06.
 */

public class HelloData {

    private int uFwVer; //FirmwareVersion
    private int uHwVer; //HardwareVersion
    private long uSFI;  //
    private long uCID;  //CustomerId
    private String uSerialNo; //SerialNumber

    public void clear() {
        this.uFwVer = 0;
        this.uHwVer = 0;
        this.uSFI = 0;
        this.uCID = 0;
        this.uSerialNo = null;
    }

    public int getuFwVer() {
        return uFwVer;
    }

    public void setuFwVer(int uFwVer) {
        this.uFwVer = uFwVer;
    }

    public int getuHwVer() {
        return uHwVer;
    }

    public void setuHwVer(int uHwVer) {
        this.uHwVer = uHwVer;
    }

    public long getuSFI() {
        return uSFI;
    }

    public void setuSFI(long uSFI) {
        this.uSFI = uSFI;
    }

    public long getuCID() {
        return uCID;
    }

    public void setuCID(long uCID) {
        this.uCID = uCID;
    }

    public String getuSerialNo() {
        return uSerialNo;
    }

    public void setuSerialNo(String uSerialNo) {
        this.uSerialNo = uSerialNo;
    }
}
