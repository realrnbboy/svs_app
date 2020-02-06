package kr.co.signallink.svsv2.dto;

import java.io.Serializable;

/**
 * Created by nspil on 2018-02-09.
 */

public class SVSTime implements Serializable {
    private float dPeak;
    private float	dRms;
    private float	dCrf;

    public float getdPeak() {
        return dPeak;
    }

    public void setdPeak(float dPeak) {
        this.dPeak = dPeak;
    }

    public float getdRms() {
        return dRms;
    }

    public void setdRms(float dRms) {
        this.dRms = dRms;
    }

    public float getdCrf() {
        return dCrf;
    }

    public void setdCrf(float dCrf) {
        this.dCrf = dCrf;
    }



}
