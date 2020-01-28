package kr.co.signallink.svsv2.dto;

/**
 * Created by nspil on 2018-02-07.
 */

public class BatData {

    private boolean retry;
    private byte percent;
    private int count;

    public void clear() {
        this.retry = true;
        this.percent = 0;
        this.count = 0;
    }

    public byte getPercent() {
        return percent;
    }

    public void setPercent(byte percent) {
        this.percent = percent;
    }

    public boolean getRetry() {
        if(count == 30) {
            this.retry = true;
            count = 0;
        }
        else {
            count++;
            this.retry = false;
        }

        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }
}
