package kr.co.signallink.svsv2.dto;

import java.io.Serializable;

/**
 * Created by nspil on 2018-02-09.
 */

public class SVSFreq implements Serializable {

    private float   dPeak;
    private float	dBnd;

    public float getdPeak() {
        return dPeak;
    }

    public void setdPeak(float dPeak) {
        this.dPeak = dPeak;
    }

    public float getdBnd() {
        return dBnd;
    }

    public void setdBnd(float dBnd) {
        this.dBnd = dBnd;
    }

}
