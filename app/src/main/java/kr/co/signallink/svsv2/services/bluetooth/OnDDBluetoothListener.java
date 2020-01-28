package kr.co.signallink.svsv2.services.bluetooth;

import java.util.List;

public interface OnDDBluetoothListener {

    void start();
    void refresh(List<DDBluetoothDevice> list);
    void found(DDBluetoothDevice ddBluetoothDevice);
    void stop(int ret); //ret == 0 : success
    void timeOut();
}
