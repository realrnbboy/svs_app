package kr.co.signallink.svsv2.command;

import android.util.Log;

import java.nio.ByteOrder;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefConvert;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.dto.BatData;
import kr.co.signallink.svsv2.dto.HelloData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.RawMeasureData;
import kr.co.signallink.svsv2.dto.RawUploadData;
import kr.co.signallink.svsv2.dto.SVSAxisBuf;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.SVSFreq;
import kr.co.signallink.svsv2.dto.SVSParam;
import kr.co.signallink.svsv2.dto.SVSTime;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.TimeCalcUtil;

import static kr.co.signallink.svsv2.commons.DefCMDOffset.BAND_MAX;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
import static kr.co.signallink.svsv2.commons.DefCMDOffset.MEASURE_AXIS_TIME_ELE_MAX;

/**
 * Created by nspil on 2018-02-09.
 */

public class ParserCommand {

    public static void parser(ResponseCommandPacket responseCommandPacket) {

        DefBLEdata.CMD type = responseCommandPacket.getType();

        TimeCalcUtil timeCalcUtil = new TimeCalcUtil(type.toString());

        switch (type) {
            case HELLO:
                hello(responseCommandPacket);
                break;
            case UPLOAD:
                upload(responseCommandPacket);
                break;
            case MEASURE_V4:
            case MEASURE_OPTION_NONE:
            case MEASURE_OPTION_WITH_TIME:
            case MEASURE_OPTION_WITH_FREQ:
            case MEASURE_OPTION_WITH_TIME_FREQ:
                measure(responseCommandPacket);
                break;
            case EVENT_WARNING:
                //event(DefCMDOffset.EVENTDATA_WARNING, responseCommandPacket);
                break;
            case EVENT_DANGER:
                //event(DefCMDOffset.EVENTDATA_DANGER, responseCommandPacket);
                break;
            case PLC_ON:
                //none
                break;
            case PLC_OFF:
                //none
                break;
            case BAT:
                bat(responseCommandPacket);
                break;
            case BATTERY:   // added by hslee 2020.04.28
                battery(responseCommandPacket);
                break;
            case LEARNING:
                learning(responseCommandPacket);
                break;
            default:
                break;
        }

        timeCalcUtil.printGap();
    }

    // added by hslee 2020.05.07
    static void setHelloData(byte [] rsp) {
        HelloData helloData = null;
        switch (SVS.getInstance().trySensorIndex) {
            case 0:
                helloData = SVS.getInstance().helloData60_1;  // added by hslee 2020.05.06
                break;
            case 1:
                helloData = SVS.getInstance().helloData60_2;  // added by hslee 2020.05.06
                break;
            case 2:
                helloData = SVS.getInstance().helloData60_3;  // added by hslee 2020.05.06
                break;
            default:
                Log.d("error", "setHelloData - trySensorIndex invalid" + SVS.getInstance().trySensorIndex);
                return;
        }

        helloData.setuFwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_FWVER, 2, ByteOrder.LITTLE_ENDIAN));
        helloData.setuHwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_HWVER, 2, ByteOrder.LITTLE_ENDIAN));
        helloData.setuSFI(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SFI, 4, ByteOrder.LITTLE_ENDIAN));
        helloData.setuCID(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_CID, 4, ByteOrder.LITTLE_ENDIAN));
        helloData.setuSerialNo(DefConvert.byteToString(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SN, 32, ByteOrder.LITTLE_ENDIAN));
    }

    private static void hello(ResponseCommandPacket responsecommand) {

        byte [] rsp = responsecommand.getData();

        int len = responsecommand.getSize() - 1;
        if(rsp[len] == checksum(rsp, len))
        {
            final HelloData hellodata = SVS.getInstance().getHellodata();

            hellodata.setuFwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_FWVER, 2, ByteOrder.LITTLE_ENDIAN));
            hellodata.setuHwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_HWVER, 2, ByteOrder.LITTLE_ENDIAN));
            hellodata.setuSFI(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SFI, 4, ByteOrder.LITTLE_ENDIAN));
            hellodata.setuCID(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_CID, 4, ByteOrder.LITTLE_ENDIAN));
            hellodata.setuSerialNo(DefConvert.byteToString(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SN, 32, ByteOrder.LITTLE_ENDIAN));

            final HelloData helloData60 = SVS.getInstance().helloData60;  // added by hslee 2020.05.06
            helloData60.setuFwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_FWVER, 2, ByteOrder.LITTLE_ENDIAN));
            helloData60.setuHwVer(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_HELLO_OFFSET_HWVER, 2, ByteOrder.LITTLE_ENDIAN));
            helloData60.setuSFI(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SFI, 4, ByteOrder.LITTLE_ENDIAN));
            helloData60.setuCID(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_HELLO_OFFSET_CID, 4, ByteOrder.LITTLE_ENDIAN));
            helloData60.setuSerialNo(DefConvert.byteToString(rsp, DefCMDOffset.CMD_HELLO_OFFSET_SN, 32, ByteOrder.LITTLE_ENDIAN));

            setHelloData(rsp);  // added by hslee 2020.05.07

            //접속중인 SVS에 FirmwareVersion 세팅
            DatabaseUtil.transaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    RealmObject realmObject = SVS.getInstance().getLinkedSvsData();
                    if(realmObject instanceof SVSEntity)
                    {
                        SVSEntity svsEntity = (SVSEntity)realmObject;

                        if(svsEntity != null)
                        {
                            svsEntity.setFwVer(hellodata.getuFwVer());
                            svsEntity.setSerialNum(hellodata.getuSerialNo());
                        }
                    }
                    else
                    {
                        WSVSEntity wsvsEntity = (WSVSEntity)realmObject;

                        if(wsvsEntity != null)
                        {
                            wsvsEntity.setFwVer(hellodata.getuFwVer());
                            //시리얼 넘버는 서버로 부터 가져와서 재세팅할 필요가 없음.
                        }
                    }

                }
            });
            DatabaseUtil.refresh();

            //시리얼 넘버를 읽은 후에, PLC를 반영할 수 있기 때문에 코드가 여기 존재함.
            MyApplication myApplication = MyApplication.getInstance();
            myApplication.addSendCommand_forLastPLCInfo(); //PLC실행 기능을 Queue에 추가.
        } /*else {
            SVS.getInstance().addSendCommand_Init(DefBLEdata.CMD_HELLO);
            DefLog.d("checksum", "error");
        }*/



    }

    private static void upload(ResponseCommandPacket responsecommand) {

        byte [] rsp = responsecommand.getData();

        int len = responsecommand.getSize() - 1;
        if(rsp[len] == checksum(rsp, len))
        {
            UploadData uploaddata = SVS.getInstance().getUploaddata();
            uploaddata.setRawUploadData(rsp);
            SVSParam svsParam = uploaddata.getSvsParam();
            setSvsParam(svsParam, rsp);
            SVS.getInstance().setRawuploaddata(new RawUploadData(new Date(), rsp));

        } /*else {
            SVS.getInstance().addSendCommand_Init(DefBLEdata.CMD_UPLOAD);
            DefLog.d("checksum", "error");
        }*/
    }

    public static UploadData rawupload(RawUploadData rawuploaddata) {
        UploadData uploaddata = null;
        byte [] rsp = rawuploaddata.getData();

        int len = DefCMDOffset.CMD_UPLOAD_LENGTH_SIZE - 1;
        if(rsp[len] == checksum(rsp, len)) {
            uploaddata = new UploadData();

            SVSParam svsParam = uploaddata.getSvsParam();
            setSvsParam(svsParam, rsp);
        }

        return uploaddata;
    }

    private static void setSvsParam(SVSParam svsParam, byte[] rsp) {
        svsParam.setnIntervalTime(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_INTERVALTIME, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnMesRange(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_MESRANGE, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnMesAxis(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_MESAXIS, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnOfsRemoval(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_OFSREMOVAL, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setfOfsAdjust(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_OFSADJUST, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnDataConv(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_DATACONV, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setfSensitivity(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_SENSITIVITY, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnSplFreq(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_SPLFREQ, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setnFftAvg(DefConvert.byteToInt(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_FFTAVG, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlLearnCnt(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_LEARNCNT, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setfLearnOffset(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_LEARNOFFSET, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlLearnDev(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_LEARNDEV, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setfFftCurveOffset(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_FFTCURVEOFFSET, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlLimitResolution(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_LIMITRESOLUTION, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlDanLimit(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_DANLIMIT, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlWrnCnt(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_WRNCNT, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlDanCnt(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_DANCNT, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlPSaveCon(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_PSAVECON, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlPSaveValue(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_PSAVEVALUE, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setfPSaveLevel(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_PSAVELEVEL, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlWrnLog(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_WRNLOG, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlDanLog(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_DANLOG, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlTpEna(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_TPENA, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlTpWrn(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_TPWRN, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlTpDan(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_TPDAN, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlFcEna(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_FCENA, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlFcWrn(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_FCWRN, 4, ByteOrder.LITTLE_ENDIAN));
        svsParam.setlFcDan(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_FCDAN, 4, ByteOrder.LITTLE_ENDIAN));


        final HelloData hellodata = SVS.getInstance().getHellodata();
        final int fwVer = hellodata.getuFwVer();
        if(fwVer >= DefConstant.FwVer.SupportBeepAndFlag.getFwVer()){
            svsParam.setnAndFlag(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_OFFSET_ANDFLAG, 4, ByteOrder.LITTLE_ENDIAN));
        }

        SVSCode svsCode = svsParam.getCode();
        setSvsCode(svsCode, rsp);
    }

    private static void setSvsCode(SVSCode svsCode, byte[] rsp) {

        int addOffset = 0;
        if(SVS.getInstance().getHellodata().getuFwVer() >= DefConstant.FwVer.SupportBeepAndFlag.getFwVer()){
            addOffset = 4;
        }

        svsCode.setlTempEna(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_SVSCODE_OFFSET_TMEPENA+addOffset, 4, ByteOrder.LITTLE_ENDIAN));
        svsCode.setlTempWrn(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_SVSCODE_OFFSET_TMEPWRN+addOffset, 4, ByteOrder.LITTLE_ENDIAN));
        svsCode.setlTempDan(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_UPLOAD_SVSCODE_OFFSET_TMEPDAN+addOffset, 4, ByteOrder.LITTLE_ENDIAN));

        SVSTime timeEna = svsCode.getTimeEna();
        timeEna.setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEENAPEK+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeEna.setdRms(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEENARMS+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeEna.setdCrf(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEENACRF+addOffset,4, ByteOrder.LITTLE_ENDIAN));

        SVSTime timeWrn = svsCode.getTimeWrn();
        timeWrn.setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNPEK+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeWrn.setdRms(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNRMS+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeWrn.setdCrf(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEWRNCRF+addOffset,4, ByteOrder.LITTLE_ENDIAN));

        SVSTime timeDan = svsCode.getTimeDan();
        timeDan.setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANPEK+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeDan.setdRms(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANRMS+addOffset,4, ByteOrder.LITTLE_ENDIAN));
        timeDan.setdCrf(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSTIME_OFFSET_TIMEDANCRF+addOffset,4, ByteOrder.LITTLE_ENDIAN));

        SVSFreq[] freqEna = svsCode.getFreqEna();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            freqEna[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQENA+addOffset + ((int)8 * i), 4, ByteOrder.LITTLE_ENDIAN));
            freqEna[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQENA+addOffset + ((int)8 * i) + (int)4,4, ByteOrder.LITTLE_ENDIAN));
        }

        SVSFreq[] freqMin = svsCode.getFreqMin();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            freqMin[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQMIN+addOffset + ((int)8 * i), 4, ByteOrder.LITTLE_ENDIAN));
            freqMin[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQMIN+addOffset + ((int)8 * i) + (int)4,4, ByteOrder.LITTLE_ENDIAN));
        }

        SVSFreq[] freqMax = svsCode.getFreqMax();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            freqMax[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQMAX+addOffset + ((int)8 * i), 4, ByteOrder.LITTLE_ENDIAN));
            freqMax[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQMAX+addOffset + ((int)8 * i) + (int)4,4, ByteOrder.LITTLE_ENDIAN));
        }

        SVSFreq[] freqWrn = svsCode.getFreqWrn();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            freqWrn[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQWRN+addOffset + ((int)8 * i), 4, ByteOrder.LITTLE_ENDIAN));
            freqWrn[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQWRN+addOffset + ((int)8 * i) + (int)4,4, ByteOrder.LITTLE_ENDIAN));
        }

        SVSFreq[] freqDan = svsCode.getFreqDan();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            freqDan[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQDAN+addOffset + ((int)8 * i), 4, ByteOrder.LITTLE_ENDIAN));
            freqDan[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_UPLOAD_SVSFREQ_OFFSET_FREQDAN+addOffset + ((int)8 * i) + (int)4,4, ByteOrder.LITTLE_ENDIAN));
        }
    }

    private static void measure(ResponseCommandPacket responsecommand) {
        byte [] rsp = responsecommand.getData();

        int len = responsecommand.getSize() - 1;
        if(rsp[len] == checksum(rsp, len))
        {
            int convertedCount = 0;

            Date date = new Date();

            MeasureData measuredata = new MeasureData();
            measuredata.setRawData(rsp);
            measuredata.setCaptureTime(date);
            measuredata.setfSplFreqMes(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_SPLFREQMES, 4, ByteOrder.LITTLE_ENDIAN));
            measuredata.setlDataConve(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_DATACONV, 4, ByteOrder.LITTLE_ENDIAN));
            measuredata.setlScaleIdx(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_SCALEIDX, 4, ByteOrder.LITTLE_ENDIAN));
            measuredata.setlAlarmCur(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_ALARMCUR, 4, ByteOrder.LITTLE_ENDIAN));
            measuredata.setlTempCurrent(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_TEMPCUR, 4, ByteOrder.LITTLE_ENDIAN));
            convertedCount += 4 + 4 + 4 + 4 + 4;

            SVSTime timecur = measuredata.getSvsTime();
            timecur.setdPeak(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURPEK, 4, ByteOrder.LITTLE_ENDIAN));
            timecur.setdRms(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURRMS, 4, ByteOrder.LITTLE_ENDIAN));
            timecur.setdCrf(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURCRF, 4, ByteOrder.LITTLE_ENDIAN));
            convertedCount += 4 + 4 + 4;

            SVSFreq[] freqcur = measuredata.getSvsFreq();
            for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
                freqcur[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_MEASURE_SVSFREQ_OFFSET_FREQCUR + (int)8 * i, 4, ByteOrder.LITTLE_ENDIAN));
                freqcur[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_MEASURE_SVSFREQ_OFFSET_FREQCUR + (int)8 * i + (int)4,4, ByteOrder.LITTLE_ENDIAN));
            }
            convertedCount += BAND_MAX * (4 + 4);

            int remainCount = len - convertedCount;

            //RAW 데이터 처리
            DefBLEdata.CMD cmd = responsecommand.getType();
            if(cmd == DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME_FREQ && remainCount >= ((MEASURE_AXIS_TIME_ELE_MAX * 4) + (MEASURE_AXIS_FREQ_ELE_MAX *4)))
            {
                SVSAxisBuf axisBuf = measuredata.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_TIME_ELE_MAX * 4, axisBuf.getfTime());
                axisBuf.setInputTimeLength(MEASURE_AXIS_TIME_ELE_MAX);
                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISFREQ, MEASURE_AXIS_FREQ_ELE_MAX * 4, axisBuf.getfFreq());
                axisBuf.setInputFreqLength(MEASURE_AXIS_FREQ_ELE_MAX);
            }
            else if(cmd == DefBLEdata.CMD.MEASURE_OPTION_WITH_FREQ && remainCount >= ((MEASURE_AXIS_FREQ_ELE_MAX *4)))
            {
                SVSAxisBuf axisBuf = measuredata.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_FREQ_ELE_MAX * 4, axisBuf.getfFreq());
                axisBuf.setInputFreqLength(MEASURE_AXIS_FREQ_ELE_MAX);
            }
            else if(cmd == DefBLEdata.CMD.MEASURE_OPTION_WITH_TIME  && remainCount >= (MEASURE_AXIS_TIME_ELE_MAX * 4))
            {
                SVSAxisBuf axisBuf = measuredata.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_TIME_ELE_MAX * 4, axisBuf.getfTime());
                axisBuf.setInputTimeLength(MEASURE_AXIS_TIME_ELE_MAX);
            }


            check_measure_max(measuredata);

            SVS.getInstance().addMeasureData(measuredata);

            if(SVS.getInstance().isRecorded())
            {
                RawMeasureData rawMeasureData = new RawMeasureData(date, rsp);
                SVS.getInstance().addRawMeasureData(rawMeasureData);
                SVS.getInstance().addRecordMeasureData(measuredata);
            }

            if(SVS.getInstance().getRecordCount() >= DefConstant.RECORDCOUNT_MAX) {
                SVS.getInstance().setRecorded(false);
            }

        } else {
            DefLog.d("checksum", "error");
        }

        SVS.getInstance().cloneMeasureData();   // added by hslee 2020.05.06
    }

    public static MeasureData rawmeasure(RawMeasureData rawmeasuredata) {

        MeasureData measureData = null;

        byte [] rsp = rawmeasuredata.getData();

        int len = rsp.length - 1;
        if(rsp[len] == checksum(rsp, len))
        {
            int convertedBytes = 0;

            measureData = new MeasureData();
            measureData.setCaptureTime(rawmeasuredata.getCaptureDate());
            measureData.setfSplFreqMes(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_SPLFREQMES, 4, ByteOrder.LITTLE_ENDIAN));
            measureData.setlDataConve(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_DATACONV, 4, ByteOrder.LITTLE_ENDIAN));
            measureData.setlScaleIdx(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_SCALEIDX, 4, ByteOrder.LITTLE_ENDIAN));
            measureData.setlAlarmCur(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_ALARMCUR, 4, ByteOrder.LITTLE_ENDIAN));
            measureData.setlTempCurrent(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_MEASURE_OFFSET_TEMPCUR, 4, ByteOrder.LITTLE_ENDIAN));
            convertedBytes += 4 + 4 + 4 + 4 + 4;

            SVSTime timeCur = measureData.getSvsTime();
            timeCur.setdPeak(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURPEK, 4, ByteOrder.LITTLE_ENDIAN));
            timeCur.setdRms(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURRMS, 4, ByteOrder.LITTLE_ENDIAN));
            timeCur.setdCrf(DefConvert.byteToFloat(rsp, DefCMDOffset.CMD_MEASURE_SVSTIME_OFFSET_TIMECURCRF, 4, ByteOrder.LITTLE_ENDIAN));
            convertedBytes += 4 + 4 + 4;

            SVSFreq[] freqCur = measureData.getSvsFreq();
            for(int i = 0; i<DefCMDOffset.BAND_MAX; i++)
            {
                freqCur[i].setdPeak(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_MEASURE_SVSFREQ_OFFSET_FREQCUR + (int)8 * i, 4, ByteOrder.LITTLE_ENDIAN));
                freqCur[i].setdBnd(DefConvert.byteToFloat(rsp,DefCMDOffset.CMD_MEASURE_SVSFREQ_OFFSET_FREQCUR + (int)8 * i + (int)4,4, ByteOrder.LITTLE_ENDIAN));
                convertedBytes += 4 + 4;
            }

            //남은 바이트 계산
            int remainBytes = len - convertedBytes;
            if(remainBytes >= (MEASURE_AXIS_TIME_ELE_MAX * 4) + (MEASURE_AXIS_FREQ_ELE_MAX * 4))
            {
                SVSAxisBuf svsAxisBuf = measureData.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_TIME_ELE_MAX * 4, svsAxisBuf.getfTime());
                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISFREQ, MEASURE_AXIS_FREQ_ELE_MAX * 4, svsAxisBuf.getfFreq());
            }
            else if(remainBytes >= (MEASURE_AXIS_FREQ_ELE_MAX * 4))
            {
                SVSAxisBuf svsAxisBuf = measureData.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_FREQ_ELE_MAX * 4, svsAxisBuf.getfFreq());
            }
            else if(remainBytes >= (MEASURE_AXIS_TIME_ELE_MAX * 4))
            {
                SVSAxisBuf svsAxisBuf = measureData.getAxisBuf();

                DefConvert.byteArrayToFloatArray(rsp, DefCMDOffset.CMD_MEASURE_SVSAXIS_OFFSET_AXISTIME, MEASURE_AXIS_TIME_ELE_MAX * 4, svsAxisBuf.getfTime());
            }
        }
        else {
            DefLog.d("checksum", "error");
        }

        return measureData;
    }

    private static void check_measure_max(MeasureData measuredata) {
        MeasureData measuredata_max = SVS.getInstance().getMeasuredata_max();

        float fmax = 0;
        float fcur = 0;

        if(measuredata_max.getlTempCurrent() < measuredata.getlTempCurrent())
            measuredata_max.setlTempCurrent(measuredata.getlTempCurrent());

        SVSTime svsTime_max = measuredata_max.getSvsTime();
        SVSTime svsTime = measuredata.getSvsTime();

        if(svsTime_max.getdRms() < svsTime.getdRms()) {
            svsTime_max.setdRms(svsTime.getdRms());
        }
        if(svsTime_max.getdPeak() < svsTime.getdPeak()) {
            svsTime_max.setdPeak(svsTime.getdPeak());
        }
        if(svsTime_max.getdCrf() < svsTime.getdCrf()) {
            svsTime_max.setdCrf(svsTime.getdCrf());
        }

        SVSFreq[] svsFreq_max = measuredata_max.getSvsFreq();
        SVSFreq[] svsFreq = measuredata.getSvsFreq();

        for(int i = 0; i<DefCMDOffset.BAND_MAX; i++) {
            if(svsFreq_max[i].getdPeak() < svsFreq[i].getdPeak()) {
                svsFreq_max[i].setdPeak(svsFreq[i].getdPeak());
            }

            if(svsFreq_max[i].getdBnd() < svsFreq[i].getdBnd()) {
                svsFreq_max[i].setdBnd(svsFreq[i].getdBnd());
            }
        }
    }

    /*private static void event(int type, ResponseCommandPacket responsecommand) {
        byte [] rsp = responsecommand.getData();

        int len = responsecommand.getSize() - 1;
        if(rsp[len] == checksum(rsp, len)) {
            EventData eventdata = null;

            if(DefCMDOffset.EVENTDATA_WARNING == type) {
                eventdata = SVS.getInstance().getEventwrndata();
            }else {
                eventdata = SVS.getInstance().getEventdandata();
            }

            if(eventdata.getSvsEvents().size() > 0) {
                eventdata.getSvsEvents().clear();
            }

            if(responsecommand.getSize() == DefCMDOffset.EVENT_EMPTY_SIZE ) {
                return;
            }

            int count = responsecommand.getSize() / DefCMDOffset.EVENTDATA_ONESIZE;

            if(count > 0) {
                eventdata.setlFramenumber(DefConvert.byteToLong(rsp, DefCMDOffset.CMD_EVENT_OFFSET_FRAMENUMBER, 4, ByteOrder.LITTLE_ENDIAN));
                for(int i=0; i<count; i++) {
                    SVSEvent svsEvent = new SVSEvent();

                    int baseoffset = DefCMDOffset.CMD_EVENT_SVSEVENT_OFFSET + DefCMDOffset.EVENTDATA_ONESIZE * i;

                    svsEvent.setlFramenumber(DefConvert.byteToLong(rsp, baseoffset + DefCMDOffset.CMD_EVENT_SVSEVENT_OFFSET_FRAMENUMBER, 4, ByteOrder.LITTLE_ENDIAN));
                    svsEvent.setlScale(DefConvert.byteToLong(rsp, baseoffset + DefCMDOffset.CMD_EVENT_SVSEVENT_OFFSET_SCALE, 4, ByteOrder.LITTLE_ENDIAN));
                    svsEvent.setlTemp(DefConvert.byteToLong(rsp, baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_OFFSET_TEMP, 4, ByteOrder.LITTLE_ENDIAN));

                    SVSTime time = svsEvent.getTime();
                    time.setdPeak(DefConvert.byteToFloat(rsp, baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_PEK, 4, ByteOrder.LITTLE_ENDIAN));
                    time.setdRms(DefConvert.byteToFloat(rsp, baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_RMS, 4, ByteOrder.LITTLE_ENDIAN));
                    time.setdCrf(DefConvert.byteToFloat(rsp, baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_SVSTIME_OFFSET_CRF, 4, ByteOrder.LITTLE_ENDIAN));

                    SVSFreq[] freq = svsEvent.getFreq();

                    for(int j = 0; j<DefCMDOffset.BAND_MAX; j++) {
                        freq[j].setdPeak(DefConvert.byteToFloat(rsp,baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_SVSFREQ_OFFSET_FREQ + (int)8 * j, 4, ByteOrder.LITTLE_ENDIAN));
                        freq[j].setdBnd(DefConvert.byteToFloat(rsp,baseoffset + DefCMDOffset.CMD_EVNET_SVSEVENT_SVSFREQ_OFFSET_FREQ + (int)8 * j + (int)4,4, ByteOrder.LITTLE_ENDIAN));
                    }

                    eventdata.addSVSEvent(svsEvent);
                }
            }
        } else {
            DefLog.d("checksum", "error");
        }

    }*/

    private static void bat(ResponseCommandPacket responsecommand) {

        byte [] rsp = responsecommand.getData();

        int len = responsecommand.getSize() - 1;
        if(rsp[len] == checksum(rsp, len)) {
            BatData batdata = SVS.getInstance().getBatdata();
            batdata.setPercent(rsp[DefCMDOffset.CMD_BAT_OFFSET_PERCENT]);
        } /*else {
            SVS.getInstance().addSendCommand_Init(DefBLEdata.CMD_BAT);
            DefLog.d("checksum", "error");
        }*/
    }

    private static void battery(ResponseCommandPacket responsecommand) {    // added by hslee 2020.04.28

        byte [] rsp = responsecommand.getData();

        if( rsp != null && rsp.length > 4 ) {
            int batteryLevel = rsp[4];
            SVS svs = SVS.getInstance();
            svs.batteryLevel = batteryLevel;
            svs.bBatteryInfoComplete = true;
        }
    }

    private static void learning(ResponseCommandPacket responseCommandPacket){

        byte[] rsp = responseCommandPacket.getData();

        int len = responseCommandPacket.getSize() - 1;
        if(rsp[len] == checksum(rsp, len)){
            Log.d("TTTT","sdfasdf");
        }
    }

    private static byte checksum(byte[] cmd, int len) {
        byte checksum = (byte)0xA5;
        int i=0;
        while(len-- > 0) {
            checksum ^= cmd[i];
            i++;
        }
        return checksum;
    }

    public static void AutoCheckSumProcessing(byte[] cmd) {

        int checkSumIndex = cmd.length-1;

        byte checkSum = checksum(cmd, checkSumIndex);
        cmd[checkSumIndex] = checkSum;
    }
}
