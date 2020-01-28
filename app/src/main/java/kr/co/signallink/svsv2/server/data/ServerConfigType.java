package kr.co.signallink.svsv2.server.data;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConvert;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.utils.ByteUtil;

import static kr.co.signallink.svsv2.utils.ByteUtil.intToByteArray;
import static kr.co.signallink.svsv2.utils.ByteUtil.parseIntToUInt16Bytes;

public class ServerConfigType extends DataSnapchot{

    public SVSParam param;
    public byte[] rawParam;
    public byte[] rawParam2;

    public void refreshLength(){

        final int byteLen = (rawParam2!=null? rawParam2.length : 0);

        super.header.lengthWithOutHeaderAndDateTime = byteLen;
        super.header.fullByteLength = byteLen
                + 32/*svsId*/
                + 1 /*msgId*/
                + 4 /*fullByteLength*/
                //+ 8 /*millisecond*/;
                + (2*6) /*millisecond to split shorts*/;
    }

    public void setSvsParamToBytes(SVSParam svsParam)
    {
        if(svsParam == null){
            return;
        }

        ByteBuffer buff = ByteBuffer.allocate(1024);
        buff.order(ByteOrder.LITTLE_ENDIAN);

        buff.putInt(svsParam.getnIntervalTime());
        buff.putInt(svsParam.getnMesRange());
        buff.putInt(svsParam.getnMesAxis());
        buff.putInt(svsParam.getnOfsRemoval());
        buff.putFloat(svsParam.getfOfsAdjust());
        buff.putInt(svsParam.getnDataConv());
        buff.putFloat(svsParam.getfSensitivity());
        buff.putInt(svsParam.getnSplFreq());
        buff.putInt(svsParam.getnFftAvg());
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlLearnCnt()));
        buff.putFloat(svsParam.getfLearnOffset());
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlLearnDev()));
        buff.putFloat(svsParam.getfFftCurveOffset());
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlLimitResolution()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlDanLimit()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlWrnCnt()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlDanCnt()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlPSaveCon()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlPSaveValue()));
        buff.putFloat(svsParam.getfPSaveLevel());
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlPSaveCnt()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlWrnLog()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlDanLog()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlTpEna()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlTpWrn()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlTpDan()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlFcEna()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlFcWrn()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getlFcDan()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsParam.getnAndFlag()));

        //SVSCode
        SVSCode svsCode = svsParam.getCode();
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsCode.getlTempEna()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsCode.getlTempWrn()));
        buff.put(ByteUtil.fromUnsignedIntLittleEndian(svsCode.getlTempDan()));
        buff.putFloat(svsCode.getTimeEna().getdPeak());
        buff.putFloat(svsCode.getTimeEna().getdRms());
        buff.putFloat(svsCode.getTimeEna().getdCrf());
        buff.putFloat(svsCode.getTimeWrn().getdPeak());
        buff.putFloat(svsCode.getTimeWrn().getdRms());
        buff.putFloat(svsCode.getTimeWrn().getdCrf());
        buff.putFloat(svsCode.getTimeDan().getdPeak());
        buff.putFloat(svsCode.getTimeDan().getdRms());
        buff.putFloat(svsCode.getTimeDan().getdCrf());
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = svsCode.getFreqEna()[i];
            buff.putFloat(svsFreq.getdPeak());
            buff.putFloat(svsFreq.getdBnd());
        }
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = svsCode.getFreqMin()[i];
            buff.putFloat(svsFreq.getdPeak());
            buff.putFloat(svsFreq.getdBnd());
        }
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = svsCode.getFreqMax()[i];
            buff.putFloat(svsFreq.getdPeak());
            buff.putFloat(svsFreq.getdBnd());
        }
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = svsCode.getFreqWrn()[i];
            buff.putFloat(svsFreq.getdPeak());
            buff.putFloat(svsFreq.getdBnd());
        }
        for(int i=0; i< DefCMDOffset.BAND_MAX; i++){
            SVSFreq svsFreq = svsCode.getFreqDan()[i];
            buff.putFloat(svsFreq.getdPeak());
            buff.putFloat(svsFreq.getdBnd());
        }

        rawParam2 = new byte[buff.position()];
        buff.flip();
        buff.get(rawParam2);

        Log.d("TTTT","buff:"+buff.toString());
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
        buff.put(rawParam2);

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
