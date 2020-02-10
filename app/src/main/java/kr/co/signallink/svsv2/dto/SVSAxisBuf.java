package kr.co.signallink.svsv2.dto;

import java.io.Serializable;

import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_TIME_ELE_MAX;

public class SVSAxisBuf implements Serializable {

    private float [] fTime = new float[MEASURE_AXIS_TIME_ELE_MAX];
    private float [] fFreq = new float[MEASURE_AXIS_FREQ_ELE_MAX];

    private int inputTimeLength = 0;
    private int inputFreqLength = 0;

    public float[] getfTime() {
        return fTime;
    }

    public float[] getfFreq() {
        return fFreq;
    }

    public void setfTime(float[] fTime) {   // added by hslee
        this.fTime = fTime;
    }

    public void setfFreq(float[] fFreq) {   // added by hslee
        this.fFreq = fFreq;
    }

    public int getInputTimeLength() {
        return inputTimeLength;
    }

    public void setInputTimeLength(int inputTimeLength) {
        this.inputTimeLength = inputTimeLength;
    }

    public int getInputFreqLength() {
        return inputFreqLength;
    }

    public void setInputFreqLength(int inputFreqLength) {
        this.inputFreqLength = inputFreqLength;
    }
}
