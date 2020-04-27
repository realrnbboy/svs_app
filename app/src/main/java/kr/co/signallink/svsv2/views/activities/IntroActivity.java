package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmResults;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.PermissionUtil;

public class IntroActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


        //퍼미션 체크
        PermissionUtil.checkAll(this, new PermissionUtil.OnPermissionListener() {

            @Override
            public void granted() {

                //퍼미션 모두 수락

                //초창기 스크린 모드 선택하기
                DefConstant.SCREEN_MODE screenMode = SVS.getInstance().getScreenMode();
                if(screenMode == DefConstant.SCREEN_MODE.UNKNOWN)
                {
                    //로그인 정보 초기화
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
                            webLoginEntities.deleteAllFromRealm();
                        }
                    });

                    //goIntent(ScreenModeSelectActivity.class);
                    //goIntent(SelectPipePumpModeActivity.class); // added by hslee 2020.04.27
                }
                else if(screenMode == DefConstant.SCREEN_MODE.IDLE)
                {
                    //goIntent(ScreenModeSelectActivity.class);
                    goIntent(SelectPipePumpModeActivity.class); // added by hslee 2020.04.27
                }
                else
                {
                    //goIntent(MainActivity.class);
                    goIntent(SelectPipePumpModeActivity.class); // added by hslee 2020.04.27
                }

                //화면 닫기
                finish();


            }
        });

    }



}
