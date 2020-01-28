package kr.co.signallink.svsv2.dto;

/**
 * Created by nspil on 2018-02-09.
 */

public class SVSParam {

    private int     nIntervalTime;
    private int     nMesRange;
    private int	    nMesAxis;
    private int	    nOfsRemoval;
    private float	fOfsAdjust;
    private int	    nDataConv;
    private float	fSensitivity;
    private int	    nSplFreq;
    private int	    nFftAvg;
    private long    lLearnCnt;

    private float	fLearnOffset;
    private long	lLearnDev;
    private float	fFftCurveOffset;
    private long	lLimitResolution;
    private long	lDanLimit;
    private long	lWrnCnt;
    private long	lDanCnt;
    private long	lPSaveCon;
    private long	lPSaveValue;

    private float	fPSaveLevel;
    private long	lPSaveCnt;
    private long	lWrnLog;
    private long	lDanLog;
    private long	lTpEna;
    private long	lTpWrn;
    private long	lTpDan;
    private long	lFcEna;
    private long	lFcWrn;

    private long	lFcDan;
    private long    nAndFlag = 0;

    private SVSCode Code = new SVSCode();

    public int getnIntervalTime() {
        return nIntervalTime;
    }

    public void setnIntervalTime(int nIntervalTime) {
        this.nIntervalTime = nIntervalTime;
    }

    public int getnMesRange() {
        return nMesRange;
    }

    public void setnMesRange(int nMesRange) {
        this.nMesRange = nMesRange;
    }

    public int getnMesAxis() {
        return nMesAxis;
    }

    public void setnMesAxis(int nMesAxis) {
        this.nMesAxis = nMesAxis;
    }

    public int getnOfsRemoval() {
        return nOfsRemoval;
    }

    public void setnOfsRemoval(int nOfsRemoval) {
        this.nOfsRemoval = nOfsRemoval;
    }

    public float getfOfsAdjust() {
        return fOfsAdjust;
    }

    public void setfOfsAdjust(float fOfsAdjust) {
        this.fOfsAdjust = fOfsAdjust;
    }

    public int getnDataConv() {
        return nDataConv;
    }

    public void setnDataConv(int nDataConv) {
        this.nDataConv = nDataConv;
    }

    public float getfSensitivity() {
        return fSensitivity;
    }

    public void setfSensitivity(float fSensitivity) {
        this.fSensitivity = fSensitivity;
    }

    public int getnSplFreq() {
        return nSplFreq;
    }

    public void setnSplFreq(int nSplFreq) {
        this.nSplFreq = nSplFreq;
    }

    public int getnFftAvg() {
        return nFftAvg;
    }

    public void setnFftAvg(int nFftAvg) {
        this.nFftAvg = nFftAvg;
    }

    public long getlLearnCnt() {
        return lLearnCnt;
    }

    public void setlLearnCnt(long lLearnCnt) {
        this.lLearnCnt = lLearnCnt;
    }

    public float getfLearnOffset() {
        return fLearnOffset;
    }

    public void setfLearnOffset(float fLearnOffset) {
        this.fLearnOffset = fLearnOffset;
    }

    public long getlLearnDev() {
        return lLearnDev;
    }

    public void setlLearnDev(long lLearnDev) {
        this.lLearnDev = lLearnDev;
    }

    public float getfFftCurveOffset() {
        return fFftCurveOffset;
    }

    public void setfFftCurveOffset(float fFftCurveOffset) { this.fFftCurveOffset = fFftCurveOffset; }

    public long getlLimitResolution() {
        return lLimitResolution;
    }

    public void setlLimitResolution(long lLimitResolution) { this.lLimitResolution = lLimitResolution;}

    public long getlDanLimit() {
        return lDanLimit;
    }

    public void setlDanLimit(long lDanLimit) {
        this.lDanLimit = lDanLimit;
    }

    public long getlWrnCnt() {
        return lWrnCnt;
    }

    public void setlWrnCnt(long lWrnCnt) {
        this.lWrnCnt = lWrnCnt;
    }

    public long getlDanCnt() {
        return lDanCnt;
    }

    public void setlDanCnt(long lDanCnt) {
        this.lDanCnt = lDanCnt;
    }

    public long getlPSaveCon() {
        return lPSaveCon;
    }

    public void setlPSaveCon(long lPSaveCon) {
        this.lPSaveCon = lPSaveCon;
    }

    public long getlPSaveValue() {
        return lPSaveValue;
    }

    public void setlPSaveValue(long lPSaveValue) {
        this.lPSaveValue = lPSaveValue;
    }

    public float getfPSaveLevel() {
        return fPSaveLevel;
    }

    public void setfPSaveLevel(float fPSaveLevel) {
        this.fPSaveLevel = fPSaveLevel;
    }

    public long getlPSaveCnt() {
        return lPSaveCnt;
    }

    public void setlPSaveCnt(long lPSaveCnt) {
        this.lPSaveCnt = lPSaveCnt;
    }

    public long getlWrnLog() {
        return lWrnLog;
    }

    public void setlWrnLog(long lWrnLog) {
        this.lWrnLog = lWrnLog;
    }

    public long getlDanLog() {
        return lDanLog;
    }

    public void setlDanLog(long lDanLog) {
        this.lDanLog = lDanLog;
    }

    public long getlTpEna() {
        //return lTpEna;
        if(lTpEna > 0)
        {
            return 1;
        }
        return 0;
    }

    public void setlTpEna(long lTpEna) {
        this.lTpEna = lTpEna;
    }

    public long getlTpWrn() {
        return lTpWrn;
    }

    public void setlTpWrn(long lTpWrn) {
        this.lTpWrn = lTpWrn;
    }

    public long getlTpDan() {
        return lTpDan;
    }

    public void setlTpDan(long lTpDan) {
        this.lTpDan = lTpDan;
    }

    public long getlFcEna() {
        //return lFcEna;
        if(lFcEna > 0)
        {
            return 1;
        }
        return 0;
    }

    public void setlFcEna(long lFcEna) {
        this.lFcEna = lFcEna;
    }

    public long getlFcWrn() {
        return lFcWrn;
    }

    public void setlFcWrn(long lFcWrn) {
        this.lFcWrn = lFcWrn;
    }

    public long getlFcDan() {
        return lFcDan;
    }

    public void setlFcDan(long lFcDan) {
        this.lFcDan = lFcDan;
    }

    public long getnAndFlag() {
        return nAndFlag;
    }

    public void setnAndFlag(long nAndFlag) {
        this.nAndFlag = nAndFlag;
    }

    public SVSCode getCode() {
        return Code;
    }

    public void setCode(SVSCode code) {
        Code = code;
    }
}
