package kr.co.signallink.svsv2.views.activities;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.fivehundredpx.android.blur.BlurringView;

import io.realm.RealmResults;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.pe.burt.android.lib.animategradientview.AnimateGradientView;

import static java.lang.System.exit;

public class ScreenModeSelectActivity extends BaseActivity {

    private ViewGroup llRoot;
    private BlurringView llMode1, llMode2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_mode);

        llRoot = findViewById(R.id.llRoot);
        llMode1 = findViewById(R.id.llMode1);
        llMode2 = findViewById(R.id.llMode2);

        //그라디언트 뷰
        AnimateGradientView agv = findViewById(R.id.agv);

        //블러효과
        llMode1.setBlurredView(llRoot);
        llMode2.setBlurredView(llRoot);


    }

    @Override
    public void onBackPressed() {

        DialogUtil.closeApp(this,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //앱 종료시 블루투스 연결 끄기
                        UartService uartService = SVS.getInstance().getUartService();
                        if(uartService != null)
                        {
                            uartService.disconnect();
                        }

                        //앱 종료
                        exit(0);
                    }
                },
                null
        );

    }
    //Mode1 클릭
    public void mode1(View view){

        SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.LOCAL);
        goIntent(MainActivity.class, true);
    }


    //Mode2 클릭
    public void mode2(View view){

        //로그인이 확인 되었다면, 메인페이지로 이동
        RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
        if (webLoginEntities.size() > 0)
        {
            WebLoginEntity webLoginEntity = webLoginEntities.get(0);

            SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.WEB);
            goIntent(MainActivity.class, true);
        }
        else
        {
            //로그인 내용이 없다면, 로그인 페이지로 이동
            SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.WEB);
            goIntent(LoginActivity.class, false);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        //버전 초기화
        initAppVersion();

        //모드 이동 자동인지 확인
        if(!SharedUtil.load(SharedUtil.KEY.AUTO_NEXT_GO_MAIN.toString(), "").equals(""))
        {
            //로그인이 확인 되었다면, 메인페이지로 이동
            RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
            if (webLoginEntities.size() > 0)
            {
                WebLoginEntity webLoginEntity = webLoginEntities.get(0);

                goIntent(MainActivity.class, true);
            }
        }
    }

    //버전 초기화
    private void initAppVersion()
    {
        TextView appVersion = findViewById(R.id.appversion);
        String verInfo = String.format("SW Ver. -");
        try {
            PackageInfo i = getPackageManager().getPackageInfo(getPackageName(), 0);
            verInfo = String.format("SW Ver. %s", i.versionName);
        } catch(PackageManager.NameNotFoundException e) { }
        appVersion.setText(verInfo);
    }
}
