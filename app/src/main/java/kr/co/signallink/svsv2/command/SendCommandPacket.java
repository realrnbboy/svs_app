package kr.co.signallink.svsv2.command;

import java.io.ByteArrayOutputStream;

import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;

/**
 * Created by nspil on 2018-02-08.
 */

public class SendCommandPacket {

    public long timeStamp = 0;
    private DefBLEdata.CMD type = DefBLEdata.CMD.NONE;
    private int status = DefConstant.SVSTRASACTION_INIT;
    private int periodCounting = 0;
    private long lastSendCommandTimeStamp;

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public SendCommandPacket() {
        timeStamp = System.currentTimeMillis();
        lastSendCommandTimeStamp = timeStamp;
    };

    public SendCommandPacket(DefBLEdata.CMD type, int status) {
        timeStamp = System.currentTimeMillis();
        this.type = type;
        this.status = status;
        lastSendCommandTimeStamp = timeStamp;
    };

    public DefBLEdata.CMD getType() {
        return type;
    }

    public void setType(DefBLEdata.CMD type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public  void addPeriodCounting() {
        this.periodCounting++;
    }

    public int getPeriodCounting() {
        return periodCounting;
    }

    public void setPeriodCounting(int periodCounting) {
        this.periodCounting = periodCounting;
    }

    public void putBytes(byte[] bytes){
        try{
            outputStream.write(bytes);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] getBytes(){
        return outputStream.toByteArray();
    }

    public int byteSize(){
        return outputStream.size();
    }

    public void byteReset(){
        outputStream.reset();
    }

    public long getLastSendCommandTimeStamp(){
        return lastSendCommandTimeStamp;
    }

    public void refreshLastSendCommandTimeStamp(){
        lastSendCommandTimeStamp = System.currentTimeMillis();
    }

}
