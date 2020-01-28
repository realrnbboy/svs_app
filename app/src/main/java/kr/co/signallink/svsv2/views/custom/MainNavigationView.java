package kr.co.signallink.svsv2.views.custom;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WCompanyEntity;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.restful.APIManager;
import kr.co.signallink.svsv2.restful.OnApiListener;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.activities.LoginActivity;
import kr.co.signallink.svsv2.views.activities.MainActivity;
import kr.co.signallink.svsv2.views.activities.ScreenModeSelectActivity;
import kr.co.signallink.svsv2.views.activities.WebActivity;

import static kr.co.signallink.svsv2.BuildConfig.DEBUG;
import static kr.co.signallink.svsv2.commons.DefConstant.WEB_URL;

public class MainNavigationView extends NavigationView implements NavigationView.OnNavigationItemSelectedListener {

    //Data
    private Context context;
    private Activity activity;
    private SVS svs = SVS.getInstance();
    private OnMainNavigationListener onMainNavigationListener;

    public MainNavigationView(Context context) {
        super(context);
        this.context = context;
    }

    public MainNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MainNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    //Init
    public void init(Activity activity, OnMainNavigationListener onMainNavigationListener)
    {
        this.activity = activity;
        this.onMainNavigationListener = onMainNavigationListener;

        //초기화
        initHeader();
        initAppVersion();
        refreshViewsForScreenMode();

        //리스너 등록
        this.setNavigationItemSelectedListener(this);
    }


    //헤더 초기화
    private void initHeader()
    {
        View nav_header_view = this.getHeaderView(0);

        //헤더에 있는 이미지에 홈페이지로 가는 기능 추가하기
        ImageView imageView_logo = nav_header_view.findViewById(R.id.logo_nav_header_main);
        imageView_logo.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtil.goHomepage(activity);
            }
        });
    }

    //버전 초기화
    private void initAppVersion()
    {
        TextView appVersion = findViewById(R.id.appversion);
        String verInfo = String.format("SW Ver. -");
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            verInfo = String.format("SW Ver. %s", i.versionName);
        } catch(PackageManager.NameNotFoundException e) { }
        appVersion.setText(verInfo);
    }

    //Navigation 초기화
    public void refreshViewsForScreenMode() {

        Menu menu = getMenu();

        DefConstant.SCREEN_MODE screenMode = SVS.getInstance().getScreenMode();
        if (screenMode == DefConstant.SCREEN_MODE.LOCAL)
        {
            //Local 숨기기
            menu.findItem(R.id.nav_mode_local).setVisible(false);

            //Web 보이기
            menu.findItem(R.id.nav_mode_web).setVisible(true);

            //로그아웃 버튼 숨기기
            menu.findItem(R.id.nav_mode_web_logout).setVisible(false);

            //Web Manager 이름
            menu.findItem(R.id.nav_web_manager).setTitle("Go to Web Manager");
        }
        else if (screenMode == DefConstant.SCREEN_MODE.WEB)
        {
            //Local 보이기
            menu.findItem(R.id.nav_mode_local).setVisible(true);

            //Web 숨기기
            menu.findItem(R.id.nav_mode_web).setVisible(false);

            //로그아웃 버튼 보이기
            menu.findItem(R.id.nav_mode_web_logout).setVisible(true);

            //Web Manager 이름 변경
            RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
            if (webLoginEntities.size() > 0) {
                WebLoginEntity webLoginEntity = webLoginEntities.get(0);

                //menu.findItem(R.id.nav_web_manager).setTitle("Go to \""+webLoginEntity.full_name+"\""); //회사 이름으로 변경
                menu.findItem(R.id.nav_web_manager).setTitle("Go to Dashboard"); //대쉬 보드
            }
        }


        //서버 세팅은 디버그 모드일때만 보이기
        if (!DEBUG) {
            menu.findItem(R.id.nav_server_setting).setVisible(false);
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Listener
        if(onMainNavigationListener != null){
            onMainNavigationListener.itemClick();
        }


        //Process
        int id = item.getItemId();
        if(id == R.id.nav_mode_local)
        {
            //로컬 모드가 아니라면, 팝업창을 띄워서 로컬 모드로 변경하겠냐는 문구를 띄우기
            if(SVS.getInstance().getScreenMode() != DefConstant.SCREEN_MODE.LOCAL)
            {
                DialogUtil.yesNo(activity, "Mode Change", "Do you want to change to Local mode?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //로컬 모드로 변경하기
                            SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.LOCAL);

                            //메인 액티비티 어댑터 초기화
                            if(activity instanceof MainActivity){
                                ((MainActivity)activity).resetViewsForScreenMode();
                            }

                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //..
                        }
                    }
                );
            }
            else
            {
                ToastUtil.showShort("It is already in local mode.");
            }

        }
        else if(id == R.id.nav_mode_web)
        {
            //웹 모드가 아니라면, 팝업창을 띄워서 웹 모드로 변경하겠냐는 문구를 띄우기
            if(svs.getScreenMode() != DefConstant.SCREEN_MODE.WEB)
            {
                DialogUtil.yesNo(activity, "Mode Change", "Do you want to change to Platform mode?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
                            if(webLoginEntities.size() > 0)
                            {
                                //로그인 정보가 있다면, 웹 모드로 바로 변환
                                SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.WEB);

                                //메인 액티비티 어댑터 초기화
                                if(activity instanceof MainActivity){
                                    ((MainActivity)activity).resetViewsForScreenMode();
                                }
                            }
                            else
                            {
                                //로그인 정보가 없다면, 로그인 페이지로 이동
                                IntentUtil.goActivity(activity, LoginActivity.class);
                            }


                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //..
                        }
                    }
                );
            }
            else
            {
                ToastUtil.showShort("It is already in Platform mode.");
            }

        }
        else if(id == R.id.nav_mode_web_logout)
        {
            DialogUtil.yesNo(activity, "Logout", "Do you want to log out?\n" + "Your data will only be erased from your phone.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //로그인시 사용된 데이터 삭제
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
                                webLoginEntities.deleteAllFromRealm();

                                RealmResults<WCompanyEntity> wCompanyEntities = new RealmDao<WCompanyEntity>(WCompanyEntity.class).loadAll();
                                wCompanyEntities.deleteAllFromRealm();

                                RealmResults<WEquipmentEntity> wEquipmentEntities = new RealmDao<WEquipmentEntity>(WEquipmentEntity.class).loadAll();
                                wEquipmentEntities.deleteAllFromRealm();

                                RealmResults<WSVSEntity> wsvsEntities = new RealmDao<WSVSEntity>(WSVSEntity.class).loadAll();
                                wsvsEntities.deleteAllFromRealm();
                            }
                        });

                        //로그아웃 API 호출
                        APIManager.getInstance().logout(new OnApiListener() {
                            @Override
                            public void success(APIResponse apiResponse) {

                                //결과 출력
                                ToastUtil.showLong("You have been logged out");

                                //알 수 없음 모드로 변경하기
                                SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.UNKNOWN);

                                //모드 선택화면으로 이동하기
                                if(activity instanceof MainActivity){
                                    ((MainActivity)activity).goIntent(ScreenModeSelectActivity.class, true);
                                }
                            }

                            @Override
                            public boolean fail(String message) {

                                //결과 출력
                                ToastUtil.showLong("You have been logged out");


                                //알 수 없음 모드로 변경하기
                                SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.UNKNOWN);

                                //모드 선택화면으로 이동하기
                                if(activity instanceof MainActivity){
                                    ((MainActivity)activity).goIntent(ScreenModeSelectActivity.class, true);
                                }

                                return true;
                            }
                        });

                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //..
                    }
                }
            );
        }
        else if(id == R.id.nav_web_manager)
        {
            if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
            {
                IntentUtil.goActivity(activity, WebActivity.class, WebActivity.TARGET_URL, WEB_URL);
            }
            else
            {
                RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
                if(webLoginEntities.size() > 0)
                {
                    WebLoginEntity webLoginEntity = webLoginEntities.get(0);

                    String url = WEB_URL+"/dashboard/"+webLoginEntity.company_id;
                    IntentUtil.goActivity(activity, WebActivity.class, WebActivity.TARGET_URL, url);
                }
                else
                {
                    IntentUtil.goActivity(activity, WebActivity.class, WebActivity.TARGET_URL, WEB_URL);
                }
            }

        }
        else if(id == R.id.nav_server_setting)
        {
            ServerSettingDialog serverSettingDialog = new ServerSettingDialog(activity);
            serverSettingDialog.setDialogListener(new ServerSettingDialog.OnCustomListButtonDialogListener() {
                @Override
                public void onBtnClicked(int viewIdx) {

                    ToastUtil.showShort("Success Save");
                }

                @Override
                public void onBtnCancelClicked() {
                    //..
                }
            });

            serverSettingDialog.show();
        }

        //추후에 선택된 표시 없애기
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                unSelectedAll();
            }
        });

        return true;
    }

    //메뉴의 선택된 것을 모두 취소해주는 함수 (NavigationView가 스스로 취소하지 못하기 때문)
    private void unSelectedAll()
    {
        int size = getMenu().size();
        for (int i = 0; i < size; i++) {
            getMenu().getItem(i).setCheckable(false);
        }
    }



}
