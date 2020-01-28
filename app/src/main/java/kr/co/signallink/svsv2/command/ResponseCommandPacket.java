package kr.co.signallink.svsv2.command;

import java.util.Arrays;

import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;

/**
 * Created by nspil on 2018-02-08.
 */

public class ResponseCommandPacket {

    private DefBLEdata.CMD type = DefBLEdata.CMD.NONE;
    private int size = 0;
    private byte[] data = null;

    public ResponseCommandPacket() {};

    public ResponseCommandPacket(DefBLEdata.CMD type, int size, byte [] bytes) {
        this.type = type;
        this.size = size;
        data = Arrays.copyOfRange(bytes, 0, size);
    };

    public DefBLEdata.CMD getType() {
        return type;
    }

    public void setType(DefBLEdata.CMD type) { this.type = type; }

    public int getSize() {
        return size;
    }

    public void setSize() {
        this.size = size;
    }

    public byte[] getData() { return data; }

    public void setData(byte[] data) { this.data = data; }
}
