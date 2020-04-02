package kr.co.signallink.svsv2.dto;

import java.io.Serializable;
import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;

/**
 * Created by nspil on 2018-02-09.
 */

public class MeasureData implements Serializable {

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

    private float rmsWarning;   // added by hslee
    private float rmsDanger;   // added by hslee

    //SVS_Data_Type
    public SVSAxisBuf axisBuf = new SVSAxisBuf();

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

    public float getRmsWarning() {
        return rmsWarning;
    }

    public void setRmsWarning(float rmsWarning) {
        this.rmsWarning = rmsWarning;
    }

    public float getRmsDanger() {
        return rmsDanger;
    }

    public void setRmsDanger(float rmsDanger) {
        this.rmsDanger = rmsDanger;
    }
}
