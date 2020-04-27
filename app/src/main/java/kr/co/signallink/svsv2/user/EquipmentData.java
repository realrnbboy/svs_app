package kr.co.signallink.svsv2.user;

import android.net.Uri;

import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.utils.StringUtil;

public class EquipmentData {

    private String uuid;

    private String name;
    private String location;
    private Uri imageUri;
    private String lastRecord;
    private DefConstant.SVS_STATE svsState;

    private ArrayList<SVSEntity> svsEntities = new ArrayList<>();

    public EquipmentData(){
        //uuid = StringUtil.makeUUID();
    }

    ////////////////////////////////


    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        uuid = StringUtil.md5(name);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public String getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(String lastRecord) {
        this.lastRecord = lastRecord;
    }

    public DefConstant.SVS_STATE getSvsState() {
        return svsState;
    }

    public void setSvsState(DefConstant.SVS_STATE svsState) {
        this.svsState = svsState;
    }

    public void setSvsEntities(ArrayList<SVSEntity> svsEntities) {
        this.svsEntities = svsEntities;
    }

    public ArrayList<SVSEntity> getSvsEntities() {
        return svsEntities;
    }


    public int getSvsLocationCount() {
        if(svsEntities != null){
            return svsEntities.size();
        }

        return 0;
    }
}
