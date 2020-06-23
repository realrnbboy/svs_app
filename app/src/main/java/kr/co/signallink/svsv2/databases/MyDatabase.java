package kr.co.signallink.svsv2.databases;

import android.content.Context;
import android.util.Log;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.FileUtil;

public class MyDatabase {

    private final String REALM_FILE_NAME = "RealmSVSInfos.realm";
    private final int MigrationVersion = 20; //마이그레이션이 필요할때마다 버전을 1씩 올리고, MyMigration 클래스에 마이그레이션 항목을 정의해야한다.

    static MyDatabase myDatabase = null;

    public static MyDatabase getInstance(Context context){
        if(myDatabase == null){
            myDatabase = new MyDatabase(context);
        }

        return myDatabase;
    }

    public MyDatabase(Context context){

        Realm.init(context);

        //changeDefaultPath(context); // deleted by hslee 2020.05.11

        RealmConfiguration config = new RealmConfiguration.Builder()
                .directory(new File(SVS.rootDir))
                .name(REALM_FILE_NAME)
                .schemaVersion(MigrationVersion)
                .migration(new MyMigration())
                .build();
        Realm.setDefaultConfiguration(config);

        //초기화
        DatabaseUtil.initBackwardCompatibilityForFiles();
        //중복 제거
        DatabaseUtil.removeDuplicationEquipmentEntities();
        //웹 로그인 암호화 과정
        DatabaseUtil.initEncryptWebLogin();
    }

    //신규 위치에 기존 Realm 옮기기 (덮어쓰기는 하지 않음)
    public void changeDefaultPath(Context context){

        String defaultFilePath = context.getFilesDir().getAbsolutePath() + File.separator + REALM_FILE_NAME;
        String newDir = SVS.rootDir;
        String newFilePath = newDir + REALM_FILE_NAME;

        //신규 위치 확인
        File fNewDir = new File(newDir);
        fNewDir.mkdirs();

        //신규 위치에 파일이 없다면, 이전 파일 가져오기
        File fNewFile = new File(newFilePath);
        if(!fNewFile.exists())
        {
            FileUtil.copyFile(defaultFilePath, newFilePath);
        }

    }

}
