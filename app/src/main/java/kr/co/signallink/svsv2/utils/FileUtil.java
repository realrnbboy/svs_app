package kr.co.signallink.svsv2.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import kr.co.signallink.svsv2.command.ParserCommand;
import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.databases.HistoryEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.RawMeasureData;
import kr.co.signallink.svsv2.dto.RawUploadData;
import kr.co.signallink.svsv2.dto.SVSCode;
import kr.co.signallink.svsv2.dto.UploadData;
import kr.co.signallink.svsv2.user.EquipmentData;
import kr.co.signallink.svsv2.user.SVS;

public class FileUtil {

    public static String pathStripExtension (String str) {

        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;

        return str.substring(0, pos);
    }

    public static String getPathToFileName(String path){
        if(path == null) return null;
        return path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
    }

    public static String getPathToFolderPath(String path){
        if(path == null) return null;
        return path.substring(0, path.lastIndexOf("/")+1);
    }

    public static String getPathExtension (String str) {

        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;

        return str.substring(pos);
    }








    ////////////////////////////////////////////////

    public static void writeUpload(String pathDir) {

        File fFile_hhmmss_Dir = new File(pathDir);
        if (!fFile_hhmmss_Dir.exists()) {
            fFile_hhmmss_Dir.mkdir();
        }

        String filepath = pathDir + DefFile.NAME.SETTING + DefFile.EXT.BIN;
        try {
            SVS svs = SVS.getInstance();
            RawUploadData rawUploadData = svs.getRawuploaddata();
            byte[] bytes = rawUploadData.getData();

            FileOutputStream file = new FileOutputStream(filepath);
            file.write(bytes);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readUpload(HistoryEntity historyEntity, Date captureTime, String pathDir) {

        File uploadFile = new File(pathDir);
        if(!uploadFile.exists())
        {
            return false;
        }

        try
        {
            byte[] readUploadBuf = new byte[DefCMDOffset.CMD_UPLOAD_LENGTH_SIZE];

            FileInputStream fis = new FileInputStream(uploadFile);

            if(fis.read(readUploadBuf) != -1)
            {
                RawUploadData rawuploaddata = new RawUploadData();
                rawuploaddata.setCaptureTime(captureTime);
                rawuploaddata.setData(readUploadBuf);

                UploadData uploaddata = ParserCommand.rawupload(rawuploaddata);
                if(uploaddata != null) {
                    historyEntity.setUploaddata(uploaddata);
                    return true;
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }


    public static void writeMeasure(String pathDir) {

        ArrayList<MeasureData> measureDatas = SVS.getInstance().getMeasureDatas();
        writeMeasure(pathDir, measureDatas);
    }

    public static void writeMeasure(String pathDir, ArrayList<MeasureData> measureDatas) {

        File fPathDir = new File(pathDir);
        if (!fPathDir.exists()) {
            fPathDir.mkdirs();
        }

        String filepath = pathDir + DefFile.NAME.MEASURE + DefFile.EXT.BIN;
        try {

            FileOutputStream fos = new FileOutputStream(filepath, true);
            DataOutputStream dos = new DataOutputStream(fos);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dos));

            for(MeasureData measureData : measureDatas)
            {
                byte[] data = measureData.getRawData();
                String hexString = StringUtil.byteArrayToHexString(data);

                bw.write(hexString);
                bw.write("\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeRawMeasure(String pathDir, ArrayList<RawMeasureData> rawMeasureDatas) {

        File fPathDir = new File(pathDir);
        if (!fPathDir.exists()) {
            fPathDir.mkdirs();
        }

        String filepath = pathDir + DefFile.NAME.MEASURE + DefFile.EXT.BIN;
        try {

            FileOutputStream fos = new FileOutputStream(filepath, true);
            DataOutputStream dos = new DataOutputStream(fos);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dos));

            for(RawMeasureData rawMeasureData : rawMeasureDatas)
            {
                byte[] data = rawMeasureData.getData();
                String hexString = StringUtil.byteArrayToHexString(data);

                bw.write(hexString);
                bw.write("\n");
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean readMeasure(HistoryEntity historyEntity, Date captureTime, String pathDir) {

        File measureFile = new File(pathDir);
        if(!measureFile.exists())
        {
            return false;
        }

        try
        {
            ArrayList<MeasureData> measureDatas = new ArrayList<>();

            FileInputStream fis = new FileInputStream(measureFile);
            DataInputStream dis = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));

            String s;
            while((s = br.readLine()) != null)
            {
                byte[] readMeasureBuf = StringUtil.hexStringToByteArray(s);

                RawMeasureData rawMeasureData = new RawMeasureData();
                rawMeasureData.setCaptureDate(captureTime);
                rawMeasureData.setData(readMeasureBuf);

                measureDatas.add(ParserCommand.rawmeasure(rawMeasureData));
            }
            br.close();

            if(measureDatas.size() > 0)
            {
                historyEntity.calcAverageMeasure(measureDatas);
                return true;
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }


    public static void copyFile(String srcPath, String dstPath) {

        try {
            if(!srcPath.trim().isEmpty()) {

                try{
                    FileUtil.fileCopy(srcPath, dstPath);
                }	catch(FileNotFoundException e){
                    e.printStackTrace();
                }	catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String path) {
        if(!path.trim().isEmpty())
        {
            File file = new File(path);
            if(file.exists())
            {
                file.delete();
            }
        }
    }



    public static void writeRegister(String pathdir, String name, String location, ArrayList<SVSEntity> svsEntities) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("location", location);

        JSONArray list = new JSONArray();
        for(SVSEntity svsEntity : svsEntities)
        {
            JSONObject obj1 = new JSONObject();
            obj1.put("name", svsEntity.getName());
            obj1.put("address", svsEntity.getAddress());
            obj1.put("svsloc_photo", svsEntity.getSvsLocation().toString());
            obj1.put("plcStateOn", svsEntity.getPlcState().toString());

            list.add(obj1);
        }

        obj.put("svsconnects", list);

        try {
            String filepath = pathdir + DefFile.NAME.REGISTER + DefFile.EXT.CHAR;

            FileWriter file = new FileWriter(filepath);
            file.write(obj.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.print(obj);
    }

    public static EquipmentData readRegister(String pathdir) throws JSONException {

        JSONParser parser = new JSONParser();
        EquipmentData equipmentData = new EquipmentData();

        try {
            String filepath = pathdir + DefFile.NAME.REGISTER + DefFile.EXT.CHAR;
            Object obj = parser.parse(new FileReader(filepath));

            JSONObject jsonObject = (JSONObject) obj;

            equipmentData.setName((String) jsonObject.get("name"));
            equipmentData.setLocation((String) jsonObject.get("location"));


            String strKeyImagePath = pathdir + DefFile.NAME.KEY_IMAGE + DefFile.EXT.JPG;
            File imgFile = new File(strKeyImagePath);
            if(imgFile.exists()){
                equipmentData.setImageUri(Uri.fromFile(imgFile));
            }

            String lastRecord = FileUtil.readLastRecord(pathdir);
            String[] checks = lastRecord.split(":");
            if(checks.length > 0)
                equipmentData.setLastRecord(checks[0]);

            if(checks.length > 1) {
                String[] datechecks = lastRecord.split("\\(");
                String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                if(timeStamp.equals(datechecks[0]))
                    equipmentData.setSvsState(DefConstant.SVS_STATE.getEnumByShortState(checks[1]));
                else
                    equipmentData.setSvsState(DefConstant.SVS_STATE.DEFAULT);
            }
            else {
                equipmentData.setSvsState(DefConstant.SVS_STATE.DEFAULT);
            }



            JSONArray svsConnects = (JSONArray) jsonObject.get("svsconnects");
            for(int i=0; i<svsConnects.size(); i++)
            {
                JSONObject obj1 = (JSONObject) svsConnects.get(i);
                String name = (String)obj1.get("name");
                String address = (String)obj1.get("address");
                String svsloc_photo = (String)obj1.get("svsloc_photo");
                String plcStateOn = (String)obj1.get("plcStateOn");

                SVSEntity svsEntity = new SVSEntity();
                svsEntity.setName(name);
                svsEntity.setAddress(address);
                svsEntity.setSvsLocation(DefFile.SVS_LOCATION.findDIR_forPHOTO(svsloc_photo));
                svsEntity.setPlcState(DefConstant.PLCState.findPLCState(plcStateOn));
                svsEntity.setImageUri(pathdir + svsloc_photo + DefFile.EXT.JPG);
                svsEntity.setParentUuid(equipmentData.getUuid());






                String strSvsLocationDir = pathdir + svsloc_photo + File.separator;
                String strLastRecord = FileUtil.readLastRecord(strSvsLocationDir);

                if(strLastRecord != null)
                {
                    checks = strLastRecord.split(":");
                    if(checks.length > 0)
                        svsEntity.setLastRecord(checks[0]);

                    if(checks.length > 1)
                    {
                        String[] dateChecks = strLastRecord.split("\\(");
                        String timeStamp = DateUtil.convertDate(new Date(), "yyyyMMdd");

                        //기록된 데이터가 오늘일 경우에만 상태를 읽어옴.
                        if (timeStamp.equals(dateChecks[0]))
                            svsEntity.setSvsState(DefConstant.SVS_STATE.getEnumByShortState(checks[1]));
                        else
                            svsEntity.setSvsState(DefConstant.SVS_STATE.DEFAULT);
                    }
                    else {
                        svsEntity.setSvsState(DefConstant.SVS_STATE.DEFAULT);
                    }
                }
                else
                {
                    svsEntity.setLastRecord("Not Recorded.");
                    svsEntity.setSvsState(DefConstant.SVS_STATE.DEFAULT);
                }

                equipmentData.getSvsEntities().add(svsEntity);



                readHistoryData(strSvsLocationDir, svsEntity);




            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return equipmentData;
    }

    public static void readHistoryData(String svsDir, SVSEntity svsEntity){
        try{

            svsEntity.getHistoryEntities().clear();

            String strDate;
            String strTime;

            File fSvsDir = new File(svsDir);
            File[] orderedSvsDir = fSvsDir.listFiles();
            fileArraySort(orderedSvsDir);
            for(File fDateDir : orderedSvsDir)
            {
                if(fDateDir.isDirectory())
                {
                    strDate = fDateDir.getName();

                    File[] orderedDateDir = fDateDir.listFiles();
                    fileArraySort(orderedDateDir);
                    for(File fTimeDir : orderedDateDir)
                    {
                        if(fTimeDir.isDirectory())
                        {
                            strTime = fTimeDir.getName();

                            String strCaptureTime = strDate + strTime;
                            Date captureTime = DateUtil.convertString(strCaptureTime, "yyyyMMddhhmmss");
                            String strUploadPath = fTimeDir + File.separator + DefFile.NAME.SETTING + DefFile.EXT.BIN;
                            String strMeasurePath = fTimeDir + File.separator + DefFile.NAME.MEASURE + DefFile.EXT.BIN;

                            HistoryEntity historyEntity = new HistoryEntity();
                            boolean retReadUpload = FileUtil.readUpload(historyEntity, captureTime, strUploadPath);
                            boolean retReadMeasure = FileUtil.readMeasure(historyEntity, captureTime, strMeasurePath);

                            if(retReadUpload && retReadMeasure)
                            {
                                svsEntity.getHistoryEntities().add(historyEntity);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.d("test", "e:"+e.toString());
        }
    }


    public static void writeComment(String pathdir, String content) {
        if(content != null && !content.trim().isEmpty()) {
            try {
                String filepath = pathdir + DefFile.NAME.COMMENT + DefFile.EXT.CHAR;

                FileWriter file = new FileWriter(filepath);
                file.write(content);
                file.flush();
                file.close();

                SVS.getInstance().setRecordcomment(null);

            } catch (IOException e) {
                SVS.getInstance().setRecordcomment(null);
                e.printStackTrace();
            }
        }
    }

    public static StringBuilder readComment(String pathdir) throws FileNotFoundException {
        StringBuilder comment = new StringBuilder();

        try {
            String filepath = pathdir + DefFile.NAME.COMMENT + DefFile.EXT.CHAR;

            FileReader file = new FileReader(filepath);

            BufferedReader bufferedReader = new BufferedReader(file);

            String s;
            while ((s = bufferedReader.readLine()) != null) {
                comment.append(s).append(System.getProperty("line.separator"));
            }

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            comment.append("not commented");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return comment;
    }

    public static void writeLastRecord(String pathdir, String content) {
        if(content != null && !content.trim().isEmpty()) {
            try {
                String filepath = pathdir + DefFile.NAME.LAST_RECORD + DefFile.EXT.CHAR;

                FileWriter file = new FileWriter(filepath);
                file.write(content);
                file.flush();
                file.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readLastRecord(String pathDir) {

        String lastRecord = null;

        try {
            String filepath = pathDir + DefFile.NAME.LAST_RECORD + DefFile.EXT.CHAR;

            FileReader file = new FileReader(filepath);

            BufferedReader bufferedReader = new BufferedReader(file);

            lastRecord = bufferedReader.readLine();

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            lastRecord = "Not Recorded.";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastRecord;
    }


    public static void setDirEmpty(String pathdir){
        //String path = SVS.getInstance().getRootdir() + dirName;

        File dir    = new File(pathdir);
        File[] childFileList = dir.listFiles();

        if (dir.exists()) {
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    setDirEmpty(childFile.getAbsolutePath());    //하위 디렉토리
                } else {
                    childFile.delete();    //하위 파일
                }
            }
            dir.delete();
        }
    }

    public static String getPath(Context context, Uri selectedImageUri)
    {
        Cursor cursor = context.getContentResolver().query(selectedImageUri, null, null, null, null );

        cursor.moveToNext();

        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );

        cursor.close();

        return path;
    }

    public static void fileCopy(String from, String to) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
            in.transferTo(0, in.size(), out);
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if(out != null) out.close();
            if(in != null) in.close();
            if(fis != null) fis.close();
            if(fos != null) fos.close();
        }
    }

    public static int getSubDirCount(String pathDir) {
        int dircount = 0;
        File fList = new File(pathDir);

        if(fList.exists()) {
            File[] files = fList.listFiles();

            for(File file : files) {
                if(file.exists()) {
                    if(file.isDirectory()) {
                        File[] subfiles = file.listFiles();
                        dircount += subfiles.length;
                    }
                }
            }
        }

        return dircount;
    }

    public static int getSVSPhotoNumber(String pathDir) {
        int photocount = 0;
        File fList = new File(pathDir);

        if(fList.exists()) {
            File[] files = fList.listFiles();

            for(File file : files) {
                if(file.exists()) {
                    if(file.isFile() && file.toString().contains(DefFile.NAME.SVS_IMAGE.toString())) {
                            photocount++;
                    }
                }
            }
        }

        return photocount;
    }

    public static DefConstant.SVS_STATE calcSVSState(UploadData uploadData, MeasureData measureData) {

        ArrayList<DefConstant.SVS_STATE> svsStates = new ArrayList();

        SVSCode svsCode = uploadData.getSvsParam().getCode();

        if(svsCode.getTimeEna().getdPeak() != 0) {
            if(measureData.getSvsTime().getdPeak() >= svsCode.getTimeWrn().getdPeak()) {
                if(measureData.getSvsTime().getdPeak() >= svsCode.getTimeDan().getdPeak()) {
                    svsStates.add(DefConstant.SVS_STATE.DANGER);
                } else {
                    svsStates.add(DefConstant.SVS_STATE.WARNING);
                }
            }
        }

        if(svsCode.getTimeEna().getdRms() != 0) {
            if(measureData.getSvsTime().getdRms() >= svsCode.getTimeWrn().getdRms()) {
                if(measureData.getSvsTime().getdRms() >= svsCode.getTimeDan().getdRms()) {
                    svsStates.add(DefConstant.SVS_STATE.DANGER);
                } else {
                    svsStates.add(DefConstant.SVS_STATE.WARNING);
                }
            }
        }

        if(svsCode.getTimeEna().getdCrf() != 0) {
            if(measureData.getSvsTime().getdCrf() >= svsCode.getTimeWrn().getdCrf()) {
                if(measureData.getSvsTime().getdCrf() >= svsCode.getTimeDan().getdCrf()) {
                    svsStates.add(DefConstant.SVS_STATE.DANGER);
                } else {
                    svsStates.add(DefConstant.SVS_STATE.WARNING);
                }
            }
        }

        for(int i = 0; i< DefCMDOffset.BAND_MAX; i++){

            if(svsCode.getFreqEna()[i].getdPeak() != 0) {
                if(measureData.getSvsFreq()[i].getdPeak() >= svsCode.getFreqWrn()[i].getdPeak()) {
                    if(measureData.getSvsFreq()[i].getdPeak() >= svsCode.getFreqDan()[i].getdPeak()) {
                        svsStates.add(DefConstant.SVS_STATE.DANGER);
                    } else {
                        svsStates.add(DefConstant.SVS_STATE.WARNING);
                    }
                }
            }

            if(svsCode.getFreqEna()[i].getdBnd() != 0) {
                if(measureData.getSvsFreq()[i].getdBnd() >= svsCode.getFreqWrn()[i].getdBnd()) {
                    if(measureData.getSvsFreq()[i].getdBnd() >= svsCode.getFreqDan()[i].getdBnd()) {
                        svsStates.add(DefConstant.SVS_STATE.DANGER);
                    } else {
                        svsStates.add(DefConstant.SVS_STATE.WARNING);
                    }
                }
            }
        }



        //계산
        if(uploadData.getSvsParam().getnAndFlag() > 0)
        {
            //AndFlag On
            //중복된 State반환
            if(svsStates.size() > 0)
            {
                //Danger Check
                boolean allDanger = true;
                for(DefConstant.SVS_STATE svsState : svsStates)
                {
                    if(svsState.ordinal() < DefConstant.SVS_STATE.DANGER.ordinal())
                    {
                        allDanger = false;
                        break;
                    }
                }
                if(allDanger){
                    return DefConstant.SVS_STATE.DANGER;
                }

                //Warning Check
                boolean allWarning = true;
                for(DefConstant.SVS_STATE svsState : svsStates)
                {
                    if(svsState.ordinal() < DefConstant.SVS_STATE.WARNING.ordinal())
                    {
                        allWarning = false;
                        break;
                    }
                }
                if(allWarning){
                    return DefConstant.SVS_STATE.WARNING;
                }
            }

            return DefConstant.SVS_STATE.NORMAL;
        }
        else
        {
            //AndFlag Off
            //가장 높은 State반환

            DefConstant.SVS_STATE topState = DefConstant.SVS_STATE.NORMAL;
            if(svsStates.size() > 0)
            {
                for(DefConstant.SVS_STATE svsState : svsStates)
                {
                    if(topState.ordinal() < svsState.ordinal())
                    {
                        topState = svsState;
                    }

                    //가장 높은 STATE면, for-loop break
                    if(topState == DefConstant.SVS_STATE.DANGER){
                        break;
                    }
                }
            }

            return topState;
        }
    }

    //기존 상태보다 더 높은 단계가 있으면 그 단계를 반환해주는 함수.
    private static DefConstant.SVS_STATE compareUpState(DefConstant.SVS_STATE current, DefConstant.SVS_STATE target){

        if(current.ordinal() < target.ordinal()){
            return target;
        }
        return current;
    }


    //파일 리스트 파일 이름으로 정렬
    public static void fileArraySort(File[] filterResult){
        // 파일명으로 정렬한다.
        Arrays.sort(filterResult, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                File file1 = (File)arg0;
                File file2 = (File)arg1;
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });
    }


}
