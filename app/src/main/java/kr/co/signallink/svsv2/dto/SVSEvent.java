package kr.co.signallink.svsv2.dto;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;

/**
 * Created by nspil on 2018-02-12.
 */

public class SVSEvent {
    private long lFramenumber;
    private long lScale;
    private long lTemp;
    private SVSTime time = new SVSTime();
    private SVSFreq[] freq = new SVSFreq[DefCMDOffset.BAND_MAX];

    public SVSEvent() {
        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) { freq[i] = new SVSFreq(); }
    }

    public long getlFramenumber() {
        return lFramenumber;
    }

    public void setlFramenumber(long lFramenumber) {
        this.lFramenumber = lFramenumber;
    }

    public long getlScale() {
        return lScale;
    }

    public void setlScale(long lScale) {
        this.lScale = lScale;
    }

    public long getlTemp() {
        return lTemp;
    }

    public void setlTemp(long lTemp) {
        this.lTemp = lTemp;
    }

    public SVSTime getTime() {
        return time;
    }

    public void setTime(SVSTime time) {
        this.time = time;
    }

    public SVSFreq[] getFreq() {
        return freq;
    }

    public void setFreq(SVSFreq[] freq) {
        this.freq = freq;
    }
}
