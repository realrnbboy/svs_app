package kr.co.signallink.svsv2.server.data;

public class ServerHeaderType {

    public byte[] svsID; //32bytes string
    public byte[] msgID; //0x03: Config Info, 0x04: Measure Data
    public int fullByteLength;
    public int lengthWithOutHeaderAndDateTime;

}
