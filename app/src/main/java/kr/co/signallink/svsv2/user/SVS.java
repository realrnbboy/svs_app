package kr.co.signallink.svsv2.user;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmObject;
import kr.co.signallink.svsv2.command.SendCommandPacket;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.dto.BatData;
import kr.co.signallink.svsv2.dto.HelloData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.RawMeasureData;
import kr.co.signallink.svsv2.dto.RawUploadData;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.utils.SharedUtil;

/**
 * Created by nspil on 2018-01-19.
 */

public class SVS {

    private static SVS instance = null;

    private UartService uartService = null;

    private HelloData hellodata = new HelloData();
    private UploadData uploaddata = new UploadData();
    private BatData batdata = new BatData();
    private MeasureData measuredata_max = new MeasureData();

    private ArrayList<SendCommandPacket> sendCommandPackets = new ArrayList<>();
    private ArrayList<MeasureData> measureDatas = new ArrayList<>();

    private int bleConnectState = DefConstant.UART_PROFILE_DISCONNECTED;

    private boolean btrendstop = false;

    private RawUploadData rawuploaddata;
    private ArrayList<RawMeasureData> rawMeasureData = new ArrayList<>();
    private ArrayList<MeasureData> recordMeasureDatas = new ArrayList<>();
    private int recordcount = 0;
    private String recordcomment = null;
    private boolean recorded = false;

    private String svsDeviceAddress;

    public static final String rootDir = Environment.getExternalStorageDirectory().getPath() + File.separator + "SVSdata" + File.separator;

    //선택된 장비 데이터 & SVS 데이터
    private String selectedEquipmentUuid = null;
    private String selectedSvsUuid = null;

    //연결된 장비 데이터 & SVS 데이터
    private String linkedEquipmentUuid = null;
    private String linkedSvsUuid = null;

    //ScreenMode
    private DefConstant.SCREEN_MODE screenMode = DefConstant.SCREEN_MODE.UNKNOWN;


    private SVS(){

        //Init Load ScreenMode
        String strScreenMode = SharedUtil.load(SharedUtil.KEY.CURRENT_SCREEN_MODE.toString(), DefConstant.SCREEN_MODE.UNKNOWN.toString());
        screenMode = DefConstant.SCREEN_MODE.getEnumByName(strScreenMode);


    }

    public static SVS getInstance(){
        if(instance == null){
            instance = new SVS();
        }
        return instance;
    }

    public void clear() {

        Log.d("TTTT","SVS clear()");

        btrendstop = false; //12080813

        hellodata.clear();
        batdata.clear();
        measureDatas.clear();
        sendCommandPackets.clear();
        clearRawMeasureDatas();
        clearRecordMeasurDatas();

        uploaddata = new UploadData();
        measuredata_max = new MeasureData();
    }

    public HelloData getHellodata() { return hellodata; }

    public void setHellodata(HelloData hellodata) { this.hellodata = hellodata; }

    public UploadData getUploaddata() { return uploaddata; }

    public void setUploaddata(UploadData uploaddata) { this.uploaddata = uploaddata; }

    public BatData getBatdata() { return batdata; }

    public void setBatdata(BatData batdata) { this.batdata = batdata; }

    public void addSendCommand_Init(DefBLEdata.CMD type) {
        SendCommandPacket sc = new SendCommandPacket(type, DefConstant.SVSTRASACTION_INIT);

        if( sc != null && sc.getOutputStream() != null ) {  // added by hslee for test
            byte[] t = sc.getOutputStream().toByteArray();
            if (t != null && t.length > 1 && t[1] > 3) {
                int i = 3;
                i = 3;
                System.out.println(111);
            }
        }
        synchronized (sendCommandPackets) {
            sendCommandPackets.add(sc);
        }
    }

    public void addSendCommand_InitFirst(DefBLEdata.CMD type){
        SendCommandPacket sc = new SendCommandPacket(type, DefConstant.SVSTRASACTION_INIT);

        synchronized (sendCommandPackets)
        {
            if(sendCommandPackets.size() > 0)
            {
                SendCommandPacket prevSc = sendCommandPackets.get(0);
                if(prevSc.getType() != type){
                    sendCommandPackets.add(0, sc);
                }
            }
            else {
                sendCommandPackets.add(0, sc);
            }
        }
    }

    public void delSendCommand_Done() {

        synchronized (sendCommandPackets)
        {
            if(sendCommandPackets.size() > 0) {

                SendCommandPacket sendCommandPacket = sendCommandPackets.get(0);

                if(sendCommandPacket.getStatus() == DefConstant.SVSTRASACTION_DONE)
                {
                    sendCommandPackets.remove(sendCommandPacket);
                }
            }
        }
    }

    public void delSendCommand_Done(SendCommandPacket sendCommandPacket) {

        if(sendCommandPacket != null)
        {
            synchronized (sendCommandPackets)
            {
                if(sendCommandPackets.size() > 0) {

                    if(sendCommandPacket.getStatus() == DefConstant.SVSTRASACTION_DONE)
                    {
                        sendCommandPackets.remove(sendCommandPacket);
                    }
                }
            }
        }


    }

    public void delSendCommand_Ing() {

        synchronized (sendCommandPackets)
        {
            if(sendCommandPackets.size() > 0) {

                SendCommandPacket sendCommandPacket = sendCommandPackets.get(0);

                if(sendCommandPacket.getStatus() == DefConstant.SVSTRASACTION_ING)
                {
                    sendCommandPackets.remove(sendCommandPacket);
                }
            }
        }
    }

    public void setCurrentSendCommand(int status) {
        if(sendCommandPackets.size() > 0) {
            sendCommandPackets.get(0).setStatus(status);
        }
    }

    public SendCommandPacket getCurrentSendCommand() {

        SendCommandPacket sc = null;

        synchronized (sendCommandPackets)
        {
            if(sendCommandPackets.size() > 0) {
                sc = sendCommandPackets.get(0);
            }
            else {
                sc = new SendCommandPacket();
            }
        }

        if( sc != null && sc.getOutputStream() != null ) {  // added by hslee for test
            byte[] t = sc.getOutputStream().toByteArray();
            if (t != null && t.length > 2 && t[1] > 3) {
                int i = 3;
                i = 3;
                System.out.println(111);
            }
        }

        return sc;
    }

    public void addMeasureData(MeasureData measure) {
        measureDatas.add(measure);
        Log.d("TTTT","SVS addMeasureData");
    }

    public ArrayList<MeasureData> getMeasureDatas() {
        Log.d("TTTT","SVS getMeasureDatas");
        return measureDatas;
    }

    public void setMeasureDatas(ArrayList<MeasureData> measureDatas) {
        this.measureDatas = measureDatas;
    }

    public MeasureData getMeasuredata_max() {
        return measuredata_max;
    }

    public int getBleConnectState() {
        synchronized (this){
            return bleConnectState;
        }
    }

    public void setBleConnectState(int bleConnectState) {
        synchronized (this){
            this.bleConnectState = bleConnectState;
        }
    }

    public boolean isBtrendstop() {
        return btrendstop;
    }

    public void setBtrendstop(boolean btrendstop) {
        this.btrendstop = btrendstop;
    }

    public String getRootdir() {
        return rootDir;
    }

    public RealmObject getSelectedEquipmentData() {

        String uuid = this.getSelectedEquipmentUuid();
        if(uuid != null)
        {
            if(screenMode == DefConstant.SCREEN_MODE.LOCAL) {
                return new RealmDao<EquipmentEntity>(EquipmentEntity.class).loadByUuid(uuid);
            }
            else {
                return new RealmDao<WEquipmentEntity>(WEquipmentEntity.class).loadById(uuid);
            }
        }
        else
        {
            return null;
        }
    }

    public RealmObject getSelectedSvsData() {

        String uuid = this.getSelectedSvsUuid();
        if(uuid != null)
        {
            if(screenMode == DefConstant.SCREEN_MODE.LOCAL) {
                return new RealmDao<SVSEntity>(SVSEntity.class).loadByUuid(uuid);
            }
            else {
                return new RealmDao<WSVSEntity>(WSVSEntity.class).loadById(uuid);
            }
        }
        else
        {
            return null;
        }
    }

    public RealmObject getLinkedEquipmentData() {

        String uuid = this.getLinkedEquipmentUuid();
        if(uuid != null)
        {
            if(screenMode == DefConstant.SCREEN_MODE.LOCAL) {
                return new RealmDao<EquipmentEntity>(EquipmentEntity.class).loadByUuid(uuid);
            }
            else {
                return new RealmDao<WEquipmentEntity>(WEquipmentEntity.class).loadById(uuid);
            }
        }
        else
        {
            return null;
        }
    }

    public RealmObject getLinkedSvsData() {

        String uuid = this.getLinkedSvsUuid();
        if(uuid != null)
        {
            if(screenMode == DefConstant.SCREEN_MODE.LOCAL) {
                return new RealmDao<SVSEntity>(SVSEntity.class).loadByUuid(uuid);
            }
            else {
                return new RealmDao<WSVSEntity>(WSVSEntity.class).loadById(uuid);
            }
        }
        else
        {
            return null;
        }
    }

    public boolean isRecorded() {
        return recorded;
    }

    public void setRecorded(boolean recorded) {
        this.recorded = recorded;
    }

    public void addRawMeasureData(RawMeasureData rawmeasure) {
        this.recordcount++;
        rawMeasureData.add(rawmeasure);
    }

    public void clearRawMeasureDatas() {
        this.recordcount = 0;
        rawMeasureData.clear();
    }

    public ArrayList<RawMeasureData> getRawMeasureData() {
        return rawMeasureData;
    }

    public void setRawMeasureData(ArrayList<RawMeasureData> rawMeasureData) {
        this.rawMeasureData = rawMeasureData;
    }

    public int getRecordCount() {
        return recordcount;
    }

    public void addRecordMeasureData(MeasureData measure) {
        recordMeasureDatas.add(measure);
    }

    public ArrayList<MeasureData> getRecordMeasureDatas() {
        return recordMeasureDatas;
    }

    public void setRecordMeasureDatas(ArrayList<MeasureData> recordMeasureDatas) {
        this.recordMeasureDatas = recordMeasureDatas;
    }

    public void clearRecordMeasurDatas() {
        recordMeasureDatas.clear();
    }

    public RawUploadData getRawuploaddata() {
        return rawuploaddata;
    }

    public void setRawuploaddata(RawUploadData rawuploaddata) {
        this.rawuploaddata = rawuploaddata;
    }

    public String getSvsDeviceAddress() {
        return svsDeviceAddress;
    }

    public void setSvsDeviceAddress(String svsDeviceAddress) {
        this.svsDeviceAddress = svsDeviceAddress;
    }

    public String getRecordcomment() {
        return recordcomment;
    }

    public void setRecordcomment(String recordcomment) {
        this.recordcomment = recordcomment;
    }

    public UartService getUartService() {
        return uartService;
    }

    public void setUartService(UartService uartService) {
        this.uartService = uartService;
    }


    //Select Equipment, Svs
    public String getSelectedEquipmentUuid() {
        return selectedEquipmentUuid;
    }

    public void setSelectedEquipmentUuid(String selectedEquipmentUuid) {
        this.selectedEquipmentUuid = selectedEquipmentUuid;
    }

    public String getSelectedSvsUuid() {
        return selectedSvsUuid;
    }

    public void setSelectedSvsUuid(String selectedSvsUuid) {
        this.selectedSvsUuid = selectedSvsUuid;
    }

    //Link Equipment, Svs
    public String getLinkedEquipmentUuid() {
        return linkedEquipmentUuid;
    }

    public void setLinkedEquipmentUuid(String linkedEquipmentUuid) {
        this.linkedEquipmentUuid = linkedEquipmentUuid;
    }

    public String getLinkedSvsUuid() {
        return linkedSvsUuid;
    }

    public void setLinkedSvsUuid(String linkedSvsUuid) {
        this.linkedSvsUuid = linkedSvsUuid;
    }

    //Screen Mode
    public DefConstant.SCREEN_MODE getScreenMode() {
        return screenMode;
    }

    public void setScreenMode(DefConstant.SCREEN_MODE screenMode) {
        this.screenMode = screenMode;

        SharedUtil.save(SharedUtil.KEY.CURRENT_SCREEN_MODE.toString(), screenMode.toString());
    }
}
