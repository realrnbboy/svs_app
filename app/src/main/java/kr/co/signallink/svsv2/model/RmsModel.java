package kr.co.signallink.svsv2.model;


public class RmsModel {

    public String name;
    public double rms;
    public double danger;
    public double warning;
    public String status;

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