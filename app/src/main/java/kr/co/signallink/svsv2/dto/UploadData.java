package kr.co.signallink.svsv2.dto;

/**
 * Created by nspil on 2018-02-09.
 */

public class UploadData {

    private SVSParam svsParam = new SVSParam();
    private byte[] rawUploadData;

    public SVSParam getSvsParam() {
        return svsParam;
    }

    public void setSvsParam(SVSParam svsParam) {
        this.svsParam = svsParam;
    }

    public byte[] getRawUploadData() {
        return rawUploadData;
    }

    public void setRawUploadData(byte[] rawUploadData) {
        this.rawUploadData = rawUploadData;
    }
}


