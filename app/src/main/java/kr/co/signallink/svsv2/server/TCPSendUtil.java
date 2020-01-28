package kr.co.signallink.svsv2.server;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;

import kr.co.signallink.svsv2.dto.HelloData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.RawUploadData;
import kr.co.signallink.svsv2.dto.SVSAxisBuf;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.server.data.ServerConfigType;
import kr.co.signallink.svsv2.server.data.ServerHeaderType;
import kr.co.signallink.svsv2.server.data.ServerMeasureType;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.ToastUtil;

public class TCPSendUtil {



    public static ServerHeaderType makeServerHeaderType(){

        //svs
        SVS svs = SVS.getInstance();

        //svs id (32byte)
        byte[] svsId = new byte[32];
        HelloData helloData = svs.getHellodata();
        if(helloData != null){
            String strSerialNo = helloData.getuSerialNo();
            if(strSerialNo != null || strSerialNo.length() != 0){
                System.arraycopy(strSerialNo.getBytes(), 0, svsId, 0, strSerialNo.getBytes().length);
                Log.d("TTTT","TCPSend makeServerHeaderType svsId:"+strSerialNo);
            }
            else
            {
                Log.i("TTTT","TCPSend makeServerHeaderType err(1).");
                ToastUtil.showShort("Server Send Err('Header Err.1')");
                return null;
            }
        }
        else {
            Log.i("TTTT","TCPSend makeServerHeaderType err(2).");
            ToastUtil.showShort("Server Send Err('Header Err.2')");
            return null;
        }

        //Header
        ServerHeaderType header = new ServerHeaderType();
        header.svsID = svsId;

        return header;
    }

    public static ServerConfigType makeConfig(){

        //Header
        ServerHeaderType header = makeServerHeaderType();
        if(header == null){
            return null;
        }

        //msg id
        header.msgID = new byte[]{0x03};

        //svs
        SVS svs = SVS.getInstance();

        //param
        UploadData uploadData = svs.getUploaddata();
        RawUploadData rawUploadData = svs.getRawuploaddata();
        if(uploadData == null || rawUploadData == null){
            ToastUtil.showShort("Server makeConfig Err('Get UploadData Fail.')");
            return null;
        }

        //ServerConfig
        ServerConfigType config = new ServerConfigType();
        config.header = header;
        config.datetime = System.currentTimeMillis();
        config.param = uploadData.getSvsParam();
        config.rawParam = rawUploadData.getData();
        config.setSvsParamToBytes(uploadData.getSvsParam());

        Log.d("TTTT","byte1:"+config.rawParam.length+",byte2:"+config.rawParam2.length);

        return config;
    }

    public static ServerMeasureType makeMeasure(){

        //Header
        ServerHeaderType header = makeServerHeaderType();
        if(header == null){
            return null;
        }

        //msg id
        header.msgID = new byte[]{0x04};

        //svs
        SVS svs = SVS.getInstance();

        //measure
        ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
        MeasureData measureData = (measureDatas != null && measureDatas.size() > 0) ? measureDatas.get(measureDatas.size()-1) : null;
        if(measureData == null){
            ToastUtil.showShort("Server makeMeasure Err('Get MeasureData Fail.')");
            return null;
        }

        //ServerMeasure
        ServerMeasureType measure = new ServerMeasureType();
        measure.header = header;
        measure.datetime = measureData.getCaptureTime().getTime();
        measure.measure = measureData;
        measure.rawMeasure = measureData.getRawData();
        measure.setMeasureToBytes(measureData);

        Log.d("TTTT","byte1:"+measure.rawMeasure.length+",byte2:"+measure.rawMeasure2.length);

        //anlysis
        measure.refreshAnalysis(svs.getUploaddata(), measureData);

        return measure;
    }

    public static ServerMeasureType makeRawMeasure(boolean needFreq, boolean needTime){

        //Header
        ServerHeaderType header = makeServerHeaderType();
        if(header == null){
            return null;
        }

        //msg id
        header.msgID = new byte[]{0x04};

        //svs
        SVS svs = SVS.getInstance();

        //measure
        ArrayList<MeasureData> measureDatas = svs.getMeasureDatas();
        int len = measureDatas.size();

        MeasureData measureData = null;
        for(int i=len-1; i>=0; i--)
        {
            MeasureData temp = measureDatas.get(i);

            SVSAxisBuf axisBuf = temp.getAxisBuf();
            if(needFreq && needTime)
            {
                if(axisBuf.getInputFreqLength() > 0 && axisBuf.getInputTimeLength() > 0)
                {
                    measureData = temp;
                    break;
                }
            }
            else if(needFreq)
            {
                if(axisBuf.getInputFreqLength() > 0)
                {
                    measureData = temp;
                    break;
                }
            }
            else if(needTime)
            {
                if(axisBuf.getInputTimeLength() > 0)
                {
                    measureData = temp;
                    break;
                }
            }
        }
        if(measureData == null){
            ToastUtil.showShort("Server makeMeasure Err('Get MeasureData Fail.')");
            return null;
        }

        //ServerMeasure
        ServerMeasureType measure = new ServerMeasureType();
        measure.header = header;
        measure.datetime = measureData.getCaptureTime().getTime();
        measure.measure = measureData;
        measure.rawMeasure = measureData.getRawData();
        measure.setMeasureToBytes(measureData);

        Log.d("TTTT","byte1:"+measure.rawMeasure.length+",byte2:"+measure.rawMeasure2.length);

        //anlysis
        measure.refreshAnalysis(svs.getUploaddata(), measureData);

        return measure;
    }



    public static void sendConfig(OnTCPSendCallback sendCallback){

        //Tag
        final String tag = "Config";

        //Config
        ServerConfigType config = makeConfig();
        if(config == null){

            String msg = "data is empty";
            Log.i("TTTT","TCPSend sendConfig:"+msg);

            fail(tag, msg, sendCallback);
            return;
        }

        //Bytes
        byte[] bytes = config.getBytes();

        //TCPData
        TCPSendData tcpSendData = new TCPSendData();
        tcpSendData.tag = tag;
        tcpSendData.bytes = bytes;
        tcpSendData.callback = sendCallback;

        //TCPSend
        TCPSend tcpSend = TCPSend.getInstance();
        tcpSend.send(tcpSendData);
    }

    public static void sendMeasure(OnTCPSendCallback sendCallback){

        //Tag
        final String tag = "Measure";

        //Config
        ServerMeasureType measure = makeMeasure();
        if(measure == null)
        {
            String msg = "data is empty";
            Log.i("TTTT","TCPSend sendMeasure:"+msg);

            fail(tag, msg, sendCallback);
            return;
        }

        //Bytes
        byte[] bytes = measure.getBytes();

        //TCPData
        TCPSendData tcpSendData = new TCPSendData();
        tcpSendData.tag = tag;
        tcpSendData.bytes = bytes;
        tcpSendData.callback = sendCallback;

        //TCPSend
        TCPSend tcpSend = TCPSend.getInstance();
        tcpSend.send(tcpSendData);
    }

    public static void sendRawMeasure(OnTCPSendCallback sendCallback){

        //Tag
        final String tag = "RawMeasure";

        //Config
        ServerMeasureType rawMeasure = makeRawMeasure(true, true);
        if(rawMeasure == null)
        {
            String msg = "data is empty";
            Log.i("TTTT","TCPSend sendRawMeasure:"+msg);

            fail(tag, msg, sendCallback);
            return;
        }

        //Bytes
        byte[] bytes = rawMeasure.getBytes();

        //TCPData
        TCPSendData tcpSendData = new TCPSendData();
        tcpSendData.tag = tag;
        tcpSendData.bytes = bytes;
        tcpSendData.callback = sendCallback;

        //TCPSend
        TCPSend tcpSend = TCPSend.getInstance();
        tcpSend.send(tcpSendData);
    }


    public static void success(final String tag, final String msg, final OnTCPSendCallback sendCallback){
        Log.d("TTTT","TCPSend Success:"+msg);

        if(sendCallback != null)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    sendCallback.onSuccess(tag, msg);
                }
            });
        }
        else
        {
            ToastUtil.showShort(msg);
        }
    }

    public static void fail(final String tag, final String msg, final OnTCPSendCallback sendCallback){
        Log.d("TTTT","TCPSend Fail:"+msg);

        if(sendCallback != null)
        {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    sendCallback.onFailed(tag, msg);
                }
            });
        }
        else
        {
            ToastUtil.showShort(msg);
        }
    }
}
