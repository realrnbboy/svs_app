package kr.co.signallink.svsv2.databases;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import kr.co.signallink.svsv2.dto.AnalysisData;
import kr.co.signallink.svsv2.dto.MeasureData;
import kr.co.signallink.svsv2.dto.ResultDiagnosisData;

@RealmClass
public class AnalysisEntity extends RealmObject {

    @PrimaryKey
    private int id; // index

    private int type;   // 1 rms, 2 frequency
    private String equipmentUuid;
    private long created;
    private float rms1;
    private float rms2;
    private float rms3;
    public RealmList<String> cause;
    public RealmList<String> causeDesc;
    public RealmList<Double> rank;
    public RealmList<Double> ratio;

    public RealmList<Double> frequency;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RealmList<Double> getFrequency() {
        return frequency;
    }

    public void setFrequency(RealmList<Double> frequency) {
        this.frequency = frequency;
    }

    public AnalysisEntity(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEquipmentUuid() {
        return equipmentUuid;
    }

    public void setEquipmentUuid(String equipmentUuid) {
        this.equipmentUuid = equipmentUuid;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public float getRms1() {
        return rms1;
    }

    public void setRms1(float rms1) {
        this.rms1 = rms1;
    }

    public float getRms2() {
        return rms2;
    }

    public void setRms2(float rms2) {
        this.rms2 = rms2;
    }

    public float getRms3() {
        return rms3;
    }

    public void setRms3(float rms3) {
        this.rms3 = rms3;
    }

    public RealmList<String> getCause() {
        return cause;
    }

    public void setCause(RealmList<String> cause) {
        this.cause = cause;
    }

    public RealmList<String> getCauseDesc() {
        return causeDesc;
    }

    public void setCauseDesc(RealmList<String> causeDesc) {
        this.causeDesc = causeDesc;
    }

    public RealmList<Double> getRank() {
        return rank;
    }

    public void setRank(RealmList<Double> rank) {
        this.rank = rank;
    }

    public RealmList<Double> getRatio() {
        return ratio;
    }

    public void setRatio(RealmList<Double> ratio) {
        this.ratio = ratio;
    }
}
