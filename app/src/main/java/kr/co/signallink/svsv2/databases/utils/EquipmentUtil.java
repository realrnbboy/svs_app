package kr.co.signallink.svsv2.databases.utils;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;

public class EquipmentUtil {

    public static void resetSVSEntities(final EquipmentEntity equipmentEntity, final ArrayList<SVSEntity> svsEntities){

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmList<SVSEntity> svsEntitiesFromEquipment = equipmentEntity.getSvsEntities();

                //초기화
                svsEntitiesFromEquipment.deleteAllFromRealm();

                //추가
                for(SVSEntity svsEntity : svsEntities){
                    svsEntitiesFromEquipment.add(svsEntity);
                }
            }
        });
        realm.refresh();
    }


}
