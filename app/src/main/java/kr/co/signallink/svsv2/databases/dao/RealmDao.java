package kr.co.signallink.svsv2.databases.dao;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import kr.co.signallink.svsv2.databases.MyDatabase;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.utils.StringUtil;

public class RealmDao<T extends RealmObject> {

    private Realm realm;
    private Class<T> clazz;

    public interface IDao<T extends RealmObject> {
        void excuteRealm(Realm realm, T obj);
    }

    public RealmDao(Class<T> clazz){
        try {
            realm = Realm.getDefaultInstance();
        }
        catch (IllegalStateException e){
            MyDatabase.getInstance(MyApplication.getInstance().getAppContext());
            realm = Realm.getDefaultInstance();
        }
        this.clazz = clazz; //(Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T first(){
        RealmResults<T> results = realm.where(clazz).findAll();
        if(results.size() > 0)
        {
            return results.first();
        }

        return null;
    }

    public RealmResults<T> loadAll(){
        return realm.where(clazz).findAll();
    }

    public RealmResults<T> loadAllByFilter(String filterName, String value){
        if(StringUtil.isEmpty(filterName) || StringUtil.isEmpty(value)){
            return null;
        }

        return realm.where(clazz).equalTo(filterName,value).findAll();
    }

    public RealmResults<T> loadAllByFilter(String filterName, boolean value){
        if(StringUtil.isEmpty(filterName)){
            return null;
        }

        return realm.where(clazz).equalTo(filterName,value).findAll();
    }

    public RealmResults<T> loadByParentUuid(String parentUuid){
        return loadAllByFilter("parentUuid",parentUuid);
    }

    public T loadByUuid(String uuid){
        RealmResults<T> realmResults = loadAllByFilter("uuid",uuid);
        if(realmResults.size() > 0){
            return realmResults.first();
        }

        return null;
    }

    public T loadById(String id){
        RealmResults<T> realmResults = loadAllByFilter("id",id);
        if(realmResults.size() > 0){
            return realmResults.first();
        }

        return null;
    }

    public void loadByUuid(final String uuid, final IDao iDao){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                T obj = loadByUuid(uuid);
                iDao.excuteRealm(realm, obj);
            }
        });
    }



    public void remove(final T realmObject){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmObject.deleteFromRealm();
            }
        });
    }

    public void remove(final RealmList<T> realmList){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realmList.deleteAllFromRealm();
                realmList.clear();
            }
        });
    }

    public void transaction(final T obj, final IDao iDao) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                iDao.excuteRealm(realm, obj);
            }
        });
        realm.refresh();
    }
}
