package kr.co.signallink.svsv2.databases;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.utils.AESUtil;

public class MyMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        // DynamicRealm는 편집 가능한 스키마를 노출합니다
        RealmSchema schema = realm.getSchema();


        for(long step=oldVersion; step<newVersion; step++)
        {

            // 버전 1로 마이그레이션: 클래스를 생성합니다
            if (step == 0) {
                schema.get("EquipmentEntity")
                        .addField("isDeleted", boolean.class, FieldAttribute.REQUIRED);
            }

            //버전 2로 마이그레이션 : 펌웨어 버전 추가
            if (step == 1) {
                schema.get("SVSEntity")
                        .addField("fwVer", int.class, FieldAttribute.REQUIRED);
            }

            //버전 3으로 마이그레이션 : MeasureOption을 실행하는 횟수 기능 추가
            if(step == 2){
                schema.get("SVSEntity")
                        .addField("MEASURE_OPTION_COUNT", int.class, FieldAttribute.REQUIRED);
            }

            //버전 4으로 마이그레이션 : SerialNum를 추가
            if(step == 3){
                schema.get("SVSEntity")
                        .addField("serialNum", String.class);
            }

            //버전 5으로 마이그레이션 : SVSWebEntity 추가
            //SVSEntity마다 마지막으로 보고 있던 WebManager 페이지를 저장하기 위해서 만들어짐
            if(step == 4){
                schema.create("SVSWebEntity")
                        .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("lastUrl", String.class);
            }


            //버전 6으로 마이그레이션 : web관련 클래스 추가
            //(WebLoginEntity, WCompanyEntity, WEquipmentEntity, WSVSEntity)
            if(step == 5){
                schema.create("WebLoginEntity")
                        .addField("id", String.class)
                        .addField("password", String.class)
                        .addField("full_name", String.class)
                        .addField("company_id", String.class);

                schema.create("WCompanyEntity")
                        .addField("id", String.class)
                        .addField("name", String.class)
                        .addField("created", String.class);

                schema.create("WEquipmentEntity")
                        .addField("id", String.class)
                        .addField("name", String.class)
                        .addField("picture", String.class)
                        .addField("created", String.class)
                        .addField("company_id", String.class);

                schema.create("WSVSEntity")
                        .addField("id", String.class)
                        .addField("name", String.class)
                        .addField("picture", String.class)
                        .addField("created", String.class)
                        .addField("company_id", String.class)
                        .addField("equipment_id", String.class);
            }

            //버전 7으로 마이그레이션 : web관련 클래스 파라매터 추가
            //(WEquipmentEntity)
            if(step == 6) {
                schema.get("WEquipmentEntity")
                        .addField("company_name", String.class)
                        .addField("factory_name", String.class);
            }

            //버전 8으로 마이그레이션 : web관련 클래스 id를 primaryKey로 변경
            //(WEquipmentEntity)
            if(step == 7) {

                schema.get("WebLoginEntity")
                        .addPrimaryKey("id");
                schema.get("WCompanyEntity")
                        .addPrimaryKey("id");
                schema.get("WEquipmentEntity")
                        .addPrimaryKey("id");
                schema.get("WSVSEntity")
                        .addPrimaryKey("id");
            }

            //버전 9으로 마이그레이션 : web관련 클래스 파라매터 추가
            //(WSVSEntity)
            if(step == 8) {

                schema.get("WSVSEntity")
                        .addField("model", String.class)
                        .addField("serial_no", String.class)
                        .addField("position", String.class)
                        .addField("fwVer", int.class)
                        .addField("address", String.class)
                        .addField("PLCState", String.class)
                        .addField("MEASURE_OPTION", String.class)
                        .addField("MEASURE_OPTION_COUNT", int.class);
            }

            //버전 10으로 마이그레이션 : 로그인 정보 암호화
            //WebLoginEntity
            if(step == 9) {

                schema.get("WebLoginEntity")
                        .addField("secured", boolean.class);
            }

            //버전 11으로 마이그레이션 : 프라이머리키 제거
            if(step == 10){
                schema.get("WebLoginEntity")
                        .removePrimaryKey();
            }

            //버전 12으로 마이그레이션 : WEquipemntEntity에서 factory_id 추가
            if(step == 11){
                schema.get("WEquipmentEntity")
                        .addField("factory_id", String.class);
            }

            //버전 13으로 마이그레이션 : PresetEntity 추가
            if( step == 12 ) {
                schema.create("PresetEntity")
                        .addField("no", int.class)
                        .addPrimaryKey("no")
                        .addField("name", String.class)
                        .addField("code", int.class)
                        .addField("projectVibSpec", int.class)
                        .addField("siteCode", String.class)
                        .addField("equipmentName", String.class)
                        .addField("tagNo", String.class)
                        .addField("inputPower", int.class)
                        .addField("lineFreq", int.class)
                        .addField("equipmentType", int.class)
                        .addField("rpm", int.class)
                        .addField("bladeCount", int.class)
                        .addField("bearingType", int.class)
                        .addField("ballCount", int.class)
                        .addField("pitchDiameter", int.class)
                        .addField("ballDiameter", int.class)
                        .addField("rps", int.class)
                        .addField("contactAngle", int.class);
            }

        }
    }
}
