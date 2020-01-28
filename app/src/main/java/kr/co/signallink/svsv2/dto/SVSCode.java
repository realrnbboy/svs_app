package kr.co.signallink.svsv2.dto;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;

/**
 * Created by nspil on 2018-02-09.
 */

public class SVSCode {
    private long	lTempEna;
    private long	lTempWrn;
    private long	lTempDan;
    private SVSTime TimeEna = new SVSTime();
    private SVSTime TimeWrn = new SVSTime();
    private SVSTime TimeDan = new SVSTime();
    private SVSFreq[] FreqEna = new SVSFreq[DefCMDOffset.BAND_MAX];
    private SVSFreq[] FreqMin = new SVSFreq[DefCMDOffset.BAND_MAX];
    private SVSFreq[] FreqMax = new SVSFreq[DefCMDOffset.BAND_MAX];
    private SVSFreq[] FreqWrn = new SVSFreq[DefCMDOffset.BAND_MAX];
    private SVSFreq[] FreqDan = new SVSFreq[DefCMDOffset.BAND_MAX];

    public SVSCode() {
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { FreqEna[i] = new SVSFreq(); }
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { FreqMin[i] = new SVSFreq(); }
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { FreqMax[i] = new SVSFreq(); }
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { FreqWrn[i] = new SVSFreq(); }
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { FreqDan[i] = new SVSFreq(); }
    }

    public long getlTempEna() {
        return lTempEna;
    }

    public void setlTempEna(long lTempEna) {
        this.lTempEna = lTempEna;
    }

    public long getlTempWrn() {
        return lTempWrn;
    }

    public void setlTempWrn(long lTempWrn) {
        this.lTempWrn = lTempWrn;
    }

    public long getlTempDan() {
        return lTempDan;
    }

    public void setlTempDan(long lTempDan) {
        this.lTempDan = lTempDan;
    }

    public SVSTime getTimeEna() {
        return TimeEna;
    }

    public void setTimeEna(SVSTime timeEna) {
        TimeEna = timeEna;
    }

    public SVSTime getTimeWrn() {
        return TimeWrn;
    }

    public void setTimeWrn(SVSTime timeWrn) {
        TimeWrn = timeWrn;
    }

    public SVSTime getTimeDan() {
        return TimeDan;
    }

    public void setTimeDan(SVSTime timeDan) {
        TimeDan = timeDan;
    }

    public SVSFreq[] getFreqEna() {
        return FreqEna;
    }

    public void setFreqEna(SVSFreq[] freqEna) {
        FreqEna = freqEna;
    }

    public SVSFreq[] getFreqMin() {
        return FreqMin;
    }

    public void setFreqMin(SVSFreq[] freqMin) {
        FreqMin = freqMin;
    }

    public SVSFreq[] getFreqMax() {
        return FreqMax;
    }

    public void setFreqMax(SVSFreq[] freqMax) {
        FreqMax = freqMax;
    }

    public SVSFreq[] getFreqWrn() {
        return FreqWrn;
    }

    public void setFreqWrn(SVSFreq[] freqWrn) {
        FreqWrn = freqWrn;
    }

    public SVSFreq[] getFreqDan() {
        return FreqDan;
    }

    public void setFreqDan(SVSFreq[] freqDan) {
        FreqDan = freqDan;
    }

}
