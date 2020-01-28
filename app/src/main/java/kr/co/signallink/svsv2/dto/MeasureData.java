package kr.co.signallink.svsv2.dto;

import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;

/**
 * Created by nspil on 2018-02-09.
 */

public class MeasureData {

    private byte[] rawData;

    private Date captureTime;
    private float fSplFreqMes;
    private long lDataConve;
    private long lScaleIdx;
    private long lAlarmCur;

    //SVS_Work_Type
    private long	lTempCur;
    private SVSTime timeCur = new SVSTime();
    private SVSFreq[] freqCur = new SVSFreq[DefCMDOffset.BAND_MAX];

    //SVS_Data_Type
    private SVSAxisBuf axisBuf = new SVSAxisBuf();

    public MeasureData() {
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { freqCur[i] = new SVSFreq(); }
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public float getfSplFreqMes() {
        return fSplFreqMes;
    }

    public void setfSplFreqMes(float fSplFreqMes) {
        this.fSplFreqMes = fSplFreqMes;
    }

    public long getlDataConve() {
        return lDataConve;
    }

    public void setlDataConve(long lDataConve) {
        this.lDataConve = lDataConve;
    }

    public long getlScaleIdx() {
        return lScaleIdx;
    }

    public void setlScaleIdx(long lScaleIdx) {
        this.lScaleIdx = lScaleIdx;
    }

    public long getlAlarmCur() {
        return lAlarmCur;
    }

    public void setlAlarmCur(long lAlarmCur) {
        this.lAlarmCur = lAlarmCur;
    }

    public long getlTempCurrent() {
        return lTempCur;
    }

    public void setlTempCurrent(long lTempCur) {
        this.lTempCur = lTempCur;
    }

    public SVSTime getSvsTime() {
        return timeCur;
    }

    public void setSvsTime(SVSTime timeCur) {
        this.timeCur = timeCur;
    }

    public SVSFreq[] getSvsFreq() {
        return freqCur;
    }

    public void setSvsFreq(SVSFreq[] freqCur) {
        this.freqCur = freqCur;
    }

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public SVSAxisBuf getAxisBuf() {
        return axisBuf;
    }
}
