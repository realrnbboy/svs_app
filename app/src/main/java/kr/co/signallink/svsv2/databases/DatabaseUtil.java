package kr.co.signallink.svsv2.databases;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import io.realm.DynamicRealmObject;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.user.EquipmentData;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.AESUtil;
import kr.co.signallink.svsv2.utils.ExternalStorage;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.utils.ModifiedDate;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.StringUtil;

public class DatabaseUtil {

    public static void refresh(){
        Realm realm = Realm.getDefaultInstance();
        realm.refresh();
    }

    public static void transaction(Realm.Transaction transaction){

        Realm realm = Realm.getDefaultInstance();

        boolean isInTransaction = realm.isInTransaction();
        if(!isInTransaction)
        {
            realm.beginTransaction();
        }

        if(transaction != null)
        {
            transaction.execute(realm);
        }

        if(!isInTransaction)
        {
            realm.commitTransaction();
            realm.refresh();
        }
    }

    //파일 형식으로 저장하던 구조를 데이터베이스 구조로 변경하는 작업
    public static void initBackwardCompatibilityForFiles()
    {
        //초기화 기록 불러오기
        boolean init = Boolean.parseBoolean(SharedUtil.load(SharedUtil.KEY.INIT_BACKWARD_COMPATIBILITY_FOR_FILES.toString(), Boolean.FALSE.toString()));

        if(!init)
        {
            ArrayList<String> folders = new ArrayList<>();
            folders.add(SVS.rootDir);

            /*
            Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
            File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
            String sdCardSVSPath = sdCard.getAbsolutePath()+File.separator+"SVSdata";
            folders.add(sdCardSVSPath);
            */

            //폴더 검색
            for(String rootDir : folders)
            {
                File fRoot = new File(rootDir);
                String[] strFiles = fRoot.list();
                if(strFiles == null){
                    return;
                }

                //폴더 정렬
                if(strFiles.length > 0){
                    Arrays.sort(strFiles, new ModifiedDate(DefConstant.DESCENDING_ORDER));
                }

                //폴더 분석
                for(String strFile : strFiles)
                {
                    File file = new File(strFile);
                    if(file.isDirectory())
                    {
                        String strEquipmentDir = file + File.separator;

                        try{
                            EquipmentData equipmentData = FileUtil.readRegister(strEquipmentDir);
                            if(equipmentData != null)
                            {
                                convertRealmObject(equipmentData);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }


            //초기화 기록 저장
            SharedUtil.save(SharedUtil.KEY.INIT_BACKWARD_COMPATIBILITY_FOR_FILES.toString(), Boolean.TRUE.toString());
        }
    }

    //EquipmentData를 중복 체크후 Realm에 삽입하는 함수
    public static void convertRealmObject(final EquipmentData equipmentData)
    {
        if(equipmentData.getName() == null || equipmentData.getName().length()==0){
            return;
        }

        final EquipmentEntity equipmentEntity = new EquipmentEntity();
        equipmentEntity.setEquipmentData(equipmentData);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                boolean find = false;
                RealmResults<EquipmentEntity> equipmentEntities = new RealmDao<EquipmentEntity>(EquipmentEntity.class).loadAllByFilter("name", equipmentData.getName());
                for(EquipmentEntity entity : equipmentEntities){
                    if(entity.getName().equals(equipmentData.getName())){
                        find = true;
                        break;
                    }
                }

                if(!find){
                    realm.copyToRealmOrUpdate(equipmentEntity);
                    Log.d("TTTT","DatabaseUtil. Copy EquipmentData");
                }
                else
                {
                    Log.d("TTTT","DatabaseUtil. Exist EquipmentData");
                }

            }
        });

    }

    //중복된 EquipmentEntity들 제거
    public static void removeDuplicationEquipmentEntities(){

        final ArrayList<EquipmentEntity> deleteList = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();
        RealmResults<EquipmentEntity> equipmentEntities = realm.where(EquipmentEntity.class).findAll();
        int len = equipmentEntities.size();


        //중복된 데이터 체크
        for(int i=0; i<len-1; i++)
        {
            EquipmentEntity iEntity = equipmentEntities.get(i);
            RealmList<SVSEntity> iSvsEntities = iEntity.getSvsEntities();

            for(int j=i+1; j<len; j++)
            {
                EquipmentEntity jEntity = equipmentEntities.get(j);
                RealmList<SVSEntity> jSvsEntities = jEntity.getSvsEntities();

                //Equipment가 같은지 확인하기
                if(StringUtil.equalNotEmpty(iEntity.getName(), jEntity.getName())
                    && !StringUtil.isDiff(iEntity.getImageUri(), jEntity.getImageUri()))
                {
                    boolean same = true;

                    //SVSEntity들이 다른지 확인하기
                    if(iSvsEntities.size() == jSvsEntities.size())
                    {
                        int svsSize = iSvsEntities.size();

                        for(int k=0; k<svsSize; k++)
                        {
                            SVSEntity iSVS = iSvsEntities.get(k);
                            SVSEntity jSVS = jSvsEntities.get(k);

                            if(!StringUtil.equalNotEmpty(iSVS.getName(), jSVS.getName())
                                || !StringUtil.equalNotEmpty(iSVS.getAddress(), jSVS.getAddress())
                                || StringUtil.isDiff(iSVS.getImageUri(), jSVS.getImageUri()))
                            {
                                same = false;
                                break;
                            }
                        }

                    }
                    else
                    {
                        same = false;
                    }


                    //다 같으면, 삭제목록에 추가
                    if(same)
                    {
                        Log.d("TTTT","DatabaseUtil. Delete Duplication EquipmentEntity("+iEntity.getName()+")");
                        deleteList.add(iEntity);
                        break;
                    }
                }
            }
        }


        //중복된 데이터 삭제
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                for(EquipmentEntity equipmentEntity : deleteList)
                {
                    equipmentEntity.deleteFromRealm();
                }
            }
        });




    }


    //웹 로그인 정보 암호화
    public static void initEncryptWebLogin(){

        DatabaseUtil.transaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
                for(WebLoginEntity webLoginEntity : webLoginEntities)
                {
                    //암호화가 안되어있으면, 암호화 적용하기
                    boolean secured = webLoginEntity.secured;
                    if(!secured)
                    {
                        String id = webLoginEntity.id;
                        String password = webLoginEntity.password;

                        Log.d("TTTT","not secured id:"+id+",pw:"+password);

                        AESUtil aesUtil = new AESUtil(DefConstant.보안키);

                        String encryptedId = aesUtil.encrypt(id);
                        String encryptedPw = aesUtil.encrypt(password);

                        Log.d("TTTT","secured id:"+encryptedId+",pw:"+encryptedPw);

                        webLoginEntity.id = encryptedId;
                        webLoginEntity.password = encryptedPw;
                        webLoginEntity.secured = true;

                    }
                }
            }
        });



    }


}
