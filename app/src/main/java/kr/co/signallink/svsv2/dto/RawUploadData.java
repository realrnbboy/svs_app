package kr.co.signallink.svsv2.dto;

import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;

/**
 * Created by nspil on 2018-02-08.
 */

public class RawUploadData {

    private Date captureTime;
    private byte[] data = null;

    public RawUploadData() {};

    public RawUploadData(Date captureTime, byte [] bytes) {
        this.captureTime = captureTime;
        data = new byte[DefCMDOffset.CMD_UPLOAD_LENGTH_SIZE];
        this.data = bytes;
    };

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public byte[] getData() { return data; }

    public void setData(byte[] data) { this.data = data; }


}
