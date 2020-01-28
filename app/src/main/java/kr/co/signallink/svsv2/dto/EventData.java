package kr.co.signallink.svsv2.dto;

import java.util.ArrayList;

/**
 * Created by nspil on 2018-02-09.
 */

public class EventData {
    private long lFramenumber;
    private ArrayList<SVSEvent> svsEvents = new ArrayList<SVSEvent>();

    public long getlFramenumber() {
        return lFramenumber;
    }

    public void setlFramenumber(long lFramenumber) {
        this.lFramenumber = lFramenumber;
    }

    public void addSVSEvent(SVSEvent event) {
        svsEvents.add(event);
    }

    public void delallEventwrndata() {
        if(svsEvents.size() > 0) {
            svsEvents.clear();
        }
    }

    public ArrayList<SVSEvent> getSvsEvents() {
        return svsEvents;
    }

    public void setSvsEvents(ArrayList<SVSEvent> svsEvents) {
        this.svsEvents = svsEvents;
    }

    public void clear() {
        svsEvents.clear();
    }
}
