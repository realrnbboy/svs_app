package kr.co.signallink.svsv2.dto;

import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;

/**
 * Created by nspil on 2018-02-08.
 */

public class RawMeasureData {

    private Date captureDate;
    private byte[] data = null;

    public RawMeasureData() {};

    public RawMeasureData(Date captureDate, byte [] bytes) {
        this.captureDate = captureDate;
        data = new byte[DefCMDOffset.CMD_MEASURE_LENGTH_SIZE];
        this.data = bytes;
    };

    public Date getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public byte[] getData() { return data; }

    public void setData(byte[] data) { this.data = data; }


}
