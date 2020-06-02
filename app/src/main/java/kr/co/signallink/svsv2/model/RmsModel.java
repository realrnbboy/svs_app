package kr.co.signallink.svsv2.model;


import java.io.Serializable;

public class RmsModel implements Serializable {

    public String name;
    public double rms;
    public double danger;
    public double warning;
    public String status;
    public boolean bProjectVib = false;

    public double rms1;
    public double rms2;
    public double rms3;
    private boolean bShowCause; // rms1,2,3이 good이면 cause 표시하지 않음
    private long created;
    private float[] frequency;

    public boolean isbProjectVib() {
        return bProjectVib;
    }

    public void setbProjectVib(boolean bProjectVib) {
        this.bProjectVib = bProjectVib;
    }

    public float[] getFrequency() {
        return frequency;
    }

    public void setFrequency(float[] frequency) {
        this.frequency = frequency;
    }

    public double getRms1() {
        return rms1;
    }

    public void setRms1(double rms1) {
        this.rms1 = rms1;
    }

    public double getRms2() {
        return rms2;
    }

    public void setRms2(double rms2) {
        this.rms2 = rms2;
    }

    public double getRms3() {
        return rms3;
    }

    public void setRms3(double rms3) {
        this.rms3 = rms3;
    }

    public boolean isbShowCause() {
        return bShowCause;
    }

    public void setbShowCause(boolean bShowCause) {
        this.bShowCause = bShowCause;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRms() {
        return rms;
    }

    public void setRms(double rms) {
        this.rms = rms;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getDanger() {
        return danger;
    }

    public void setDanger(double danger) {
        this.danger = danger;
    }

    public double getWarning() {
        return warning;
    }

    public void setWarning(double warning) {
        this.warning = warning;
    }
}