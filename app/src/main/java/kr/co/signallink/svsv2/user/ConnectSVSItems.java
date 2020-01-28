package kr.co.signallink.svsv2.user;

import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefFile;

public class ConnectSVSItems {

    private boolean autoSaveMode = false;
    private boolean canGo = true;
    private int index_connecting = 0;

    private ArrayList<ConnectSVSItem> connectSVSItems = new ArrayList<>();

    //////////////////////////

    public boolean isAutoSaveMode() {
        return autoSaveMode;
    }

    public void setAutoSaveMode(boolean autoSaveMode) {
        this.autoSaveMode = autoSaveMode;
    }

    public int size() {
        return connectSVSItems.size();
    }

    public void add(ConnectSVSItem connectSVSItem) {
        connectSVSItems.add(connectSVSItem);
    }

    public String getCurrentIndexUuid(){
        return connectSVSItems.get(index_connecting).getSvsUuid();
    }

    public String getCurrentIndexAddress(){
        return connectSVSItems.get(index_connecting).getAddress();
    }

    public DefFile.SVS_LOCATION getCurrentIndexSvsLocation(){
        return connectSVSItems.get(index_connecting).getSvsLocation();
    }

    public ConnectSVSItem getCurrentItem(){
        return connectSVSItems.get(index_connecting);
    }

    public int getIndex_connecting() {
        return index_connecting;
    }

    public void nextIndex() {
        this.index_connecting++;
    }

    public boolean isCanGo() {
        return canGo;
    }

    public void setCanGo(boolean canGo) {
        this.canGo = canGo;
    }

    public void removeAll(){
        this.connectSVSItems.clear();
    }
}
