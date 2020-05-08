package kr.co.signallink.svsv2.server.data;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefConvert;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.SVSAxisBuf;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.utils.ByteUtil;
import kr.co.signallink.svsv2.utils.FileUtil;

import static kr.co.signallink.svsv2.utils.ByteUtil.intToByteArray;
import static kr.co.signallink.svsv2.utils.ByteUtil.parseIntToUInt16Bytes;

public class ServerMeasureType extends DataSnapchot {

    public MeasureData measure;
    public byte[] rawMeasure;
    public byte[] rawMeasure2;
    public int nAnalysis; //0:Normal, 1:Warning, 2:Danger


    public void refreshLength(){

        final int byteLen = (rawMeasure!=null? rawMeasure.length : 0)
                + 4 /*analysis*/;

        super.header.lengthWithOutHeaderAndDateTime = byteLen;
        super.header.fullByteLength = byteLen
                + 32/*svsId*/
                + 1 /*msgId*/
                + 4 /*fullByteLength*/
                //+ 8 /*millisecond*/;
                + (2*6) /*millisecond to split shorts*/;
    }

    public void setMeasureToBytes(MeasureData measureData) {
        if (measureData == null) {
            return;
        }


        ByteBuffer buff = ByteBuffer.allocate(12426);  // added by hslee 2020.05.07 기존 1024 * 10
        buff.order(ByteOrder.LITTLE_ENDIAN);

        buff.put(new byte[]{0x0});  //headerSTX
        buff.put(new byte[]{0x0});  //headerCMD
        buff.putShort((short)0);    //headerLength

        buff.putFloat(measureData.getfSplFreqMes()); //fSplRate
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(measureData.getlDataConve()));  //nBandSel ???
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(measureData.getlScaleIdx()));   //nScale
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(measureData.getlAlarmCur()));   //nAlarm

        //SVS_Work_Type
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(measureData.getlTempCurrent()));    //nTemp
        buff.putFloat(measureData.getSvsTime().getdPeak()); //SVS_TIME_Type.dPeak
        buff.putFloat(measureData.getSvsTime().getdRms());  //SVS_TIME_Type.dRMS
        buff.putFloat(measureData.getSvsTime().getdCrf());  //SVS_TIME_Type.dCrF
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = measureData.getSvsFreq()[i];
            buff.putFloat(svsFreq.getdPeak());  //SVS_FREQUENCY_Type.dPeak
            buff.putFloat(svsFreq.getdBnd());   //SVS_FREQUENCY_Type.dBnd
        }

        //SVS_Data_Type
        SVSAxisBuf svsAxisBuf = measureData.getAxisBuf();   // for test time, freq 순서 바꿈. 원래 time 먼저
        for(int i=0; i< DefCMDOffset.MEASURE_AXIS_TIME_ELE_MAX; i++){
            buff.putFloat(svsAxisBuf.getfTime()[i]);    //SVS_Data_Type.fTime;
        }
        for (int i = 0; i < DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX; i++) {
            buff.putFloat(svsAxisBuf.getfFreq()[i]);    //SVS_Data_Type.fFreq;
        }

        //CRC
        buff.put(new byte[]{0x0});

        rawMeasure2 = new byte[buff.position()];
        buff.flip();
        buff.get(rawMeasure2);

        Log.d("TTTT","buff:"+buff.toString());
    }

    //상태(N,W,D) 판단
    public void refreshAnalysis(UploadData uploadData, MeasureData measureData) {

        DefConstant.SVS_STATE retSvsState = FileUtil.calcSVSState(uploadData, measureData);

        nAnalysis = retSvsState.ordinal() - DefConstant.SVS_STATE.NORMAL.ordinal();
    }

    @Override
    public byte[] getBytes() {

        refreshLength();

        ByteBuffer buff = ByteBuffer.allocate(super.header.fullByteLength);

        //svs id
        buff.put(super.header.svsID);

        //msg id
        buff.put(super.header.msgID);

        //fullByteLength
        buff.put(intToByteArray(super.header.lengthWithOutHeaderAndDateTime));

        //date
        putMilliToBuff(buff, datetime);

        //param
        buff.put(rawMeasure);

        //analysis
        buff.put(intToByteArray(nAnalysis));

        return buff.array();
    }


    private void putMilliToBuff(ByteBuffer buff, long millisecond)
    {
        if(buff == null){
            return;
        }

        Date date = new Date(millisecond);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        buff.put(parseIntToUInt16Bytes(year));
        buff.put(parseIntToUInt16Bytes(month));
        buff.put(parseIntToUInt16Bytes(day));
        buff.put(parseIntToUInt16Bytes(hour));
        buff.put(parseIntToUInt16Bytes(minute));
        buff.put(parseIntToUInt16Bytes(second));
    }


}
