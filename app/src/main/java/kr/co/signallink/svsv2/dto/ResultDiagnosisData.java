package kr.co.signallink.svsv2.dto;

import java.io.Serializable;

// added by hslee
public class ResultDiagnosisData implements Serializable {
    public int no;
    public String cause;
    public String desc;
    public double rank;
    public double sum;
    public double ratio;

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
