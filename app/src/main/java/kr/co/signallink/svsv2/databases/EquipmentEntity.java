package kr.co.signallink.svsv2.databases;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.user.EquipmentData;
import kr.co.signallink.svsv2.utils.StringUtil;

@RealmClass
public class EquipmentEntity extends RealmObject {

    @Required
    @PrimaryKey
    private String uuid = StringUtil.makeUUID();
    private boolean isDeleted = false;

    private String type;    // added by hslee 2020.04.27  pump 펌프, pipe or null 배관
    private String name;
    private String location;
    private String imageUri;
    private String lastRecord;
    private String SVS_STATE = DefConstant.SVS_STATE.DEFAULT.name();

    //Child
    private RealmList<SVSEntity> svsEntities = new RealmList<>();

    public EquipmentEntity(){

    }

    public void setEquipmentData(EquipmentData equipmentData)
    {
        type = equipmentData.getType();
        name = equipmentData.getName();
        location = equipmentData.getLocation();
        imageUri = equipmentData.getImageUri() != null ? equipmentData.getImageUri().toString() : null;
        lastRecord = equipmentData.getLastRecord();
        SVS_STATE = equipmentData.getSvsState().name();

        for(SVSEntity svsEntity : equipmentData.getSvsEntities())
        {
            svsEntities.add(svsEntity);
        }
    }



    //Getter Setter


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getLastRecord() {
        return lastRecord;
    }

    public void setLastRecord(String lastRecord) {
        this.lastRecord = lastRecord;
    }

    public DefConstant.SVS_STATE getSvsState() {
        return DefConstant.SVS_STATE.getEnumByName(SVS_STATE);
    }

    public void setSvsState(DefConstant.SVS_STATE svsState) {
        this.SVS_STATE = svsState.name();
    }

    public RealmList<SVSEntity> getSvsEntities() {
        return svsEntities;
    }

    public void setSvsEntities(RealmList<SVSEntity> svsEntities) {
        this.svsEntities = svsEntities;
    }
}
