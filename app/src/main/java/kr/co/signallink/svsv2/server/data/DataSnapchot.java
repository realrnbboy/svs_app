package kr.co.signallink.svsv2.server.data;

import org.joda.time.DateTime;

public abstract class DataSnapchot {

    public ServerHeaderType header;
    public long datetime;

    public byte[] getBytes() {
        return null;
    }
}
