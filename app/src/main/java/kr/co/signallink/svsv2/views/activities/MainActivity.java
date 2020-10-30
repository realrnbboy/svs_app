package kr.co.signallink.svsv2.views.activities;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.databases.AnalysisEntity;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WCompanyEntity;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.databases.web.WebLoginEntity;
import kr.co.signallink.svsv2.model.RmsModel;
import kr.co.signallink.svsv2.restful.APIManager;
import kr.co.signallink.svsv2.restful.OnApiListener;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.restful.response.EquipmentGetListResponse;
import kr.co.signallink.svsv2.services.MyApplication;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.ItemClickUtil;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.Utils;
import kr.co.signallink.svsv2.views.adapters.EquipmentAdapter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.views.adapters.WEquipmentAdapter;
import kr.co.signallink.svsv2.views.custom.CustomListDialog;
import kr.co.signallink.svsv2.views.custom.MainNavigationView;
import kr.co.signallink.svsv2.views.custom.OnMainNavigationListener;

import static java.lang.System.exit;
import static kr.co.signallink.svsv2.commons.DefConstant.WEB_URL;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

    Context m_context;

    //View
    //private MainNavigationView mainNavigationView;
    private Button btnControlBle;
    private Button btnDetailUpdate;
    private Button btnRegisterEquipment;
    private RecyclerView equipmentRecyclerView = null;
    private CustomListDialog customListDialog;
    private SwipeRefreshLayout swipe;

    //CurrentMode View
    private LinearLayout llCurrentLocalMode, llCurrentWebMode;

    //Data
    private SVS svs = SVS.getInstance();
    private EquipmentAdapter equipmentAdapter;
    private WEquipmentAdapter wequipmentAdapter;
    private OrderedRealmCollection<EquipmentEntity> rEquipmentEntities;
    private OrderedRealmCollection<WEquipmentEntity> rWEquipmentEntities;

    String pipePumpMode;

    private final BroadcastReceiver StatusChangeReceiverOnSVSLocPhotoManualMode = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
           if (action.equals(DefBLEdata.HELLO_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        DefLog.d(TAG, "HELLO_ARRIVE");
                        btnControlBle.setVisibility(View.VISIBLE);
                    }
                });
            } else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {

               runOnUiThread(new Runnable() {
                   public void run() {
                       DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                       btnControlBle.setVisibility(View.INVISIBLE);

                       if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL) {
                           equipmentAdapter.notifyDataSetChanged();
                       } else {
                           wequipmentAdapter.notifyDataSetChanged();
                       }
                   }
               });
            }
        }
    };


    private static IntentFilter makeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(DefBLEdata.HELLO_ARRIVE);
        intentFilter.addAction(DefBLEdata.BAT_ARRIVE);
        intentFilter.addAction(DefBLEdata.UPLOAD_ARRIVE);
        intentFilter.addAction(DefBLEdata.UPLOAD_NOTARRIVE);
        intentFilter.addAction(DefBLEdata.MEASURE_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCOVERED_ARRIVE);
        intentFilter.addAction(DefBLEdata.DISCONNECTION_ARRIVE);

        return intentFilter;
    }

    private void registerReceiver(){

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnSVSLocPhotoManualMode, makeUpdateIntentFilter());

    }

    private void unregisterReceiver(){
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnSVSLocPhotoManualMode);
        } catch (Exception e) {
            DefLog.d(TAG, e.toString());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pipePumpMode = getIntent().getStringExtra("pipePumpMode");

        if( "pipe".equals(pipePumpMode) ) {
            customListDialog = new CustomListDialog(this, R.array.custom_list_dialog_equipment_pipe);
        }
        else {
            customListDialog = new CustomListDialog(this, R.array.custom_list_dialog_equipment);
        }


        initViews();

        m_context = this;


        registerReceiver();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DefLog.d(TAG, "onDestroy()");

        unregisterReceiver();
    }

    @Override
    public void onResume() {
        super.onResume();

        DefLog.d(TAG, "onResume");

        if(DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState()) {
            btnControlBle.setVisibility(View.VISIBLE);
        } else {
            btnControlBle.setVisibility(View.INVISIBLE);
        }

        resetViewsForScreenMode();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

//    @Override
//    public void onBackPressed() {
//
//        DialogUtil.closeApp(this,
//            new DialogInterface.OnClickListener()
//            {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                    //앱 종료시 블루투스 연결 끄기
//                    UartService uartService = svs.getUartService();
//                    if(uartService != null)
//                    {
//                        uartService.disconnect();
//                    }
//
//                    //앱 종료
//                    exit(0);
//                }
//            },
//            null
//        );
//
//    }



    private void initViews() {

        // added by hslee 2020.04.27
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText("quipment - " + pipePumpMode);

        //CurrentMode
        llCurrentLocalMode = findViewById(R.id.llCurrentLocalMode);
        llCurrentWebMode = findViewById(R.id.llCurrentWebMode);


        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView img_title = toolbar.findViewById(R.id.toolbar_img_title);
        img_title.setVisibility(View.VISIBLE);
        ImageView img_title_hyundai = toolbar.findViewById(R.id.toolbar_img_title_hyundai);
        img_title_hyundai.setVisibility(View.VISIBLE);

        //사용안하는 툴바의 레코드 버튼 숨기기
        Button button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        //ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Swipe
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);

//        //Drawer
//        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();

//        //NavigationView
//        mainNavigationView = findViewById(R.id.nav_view);
//        mainNavigationView.init(this, new OnMainNavigationListener() {
//            @Override
//            public void itemClick() {
//
//                //네비게이션에서 아이템이 클릭되었을때, drawer를 닫기.
//                drawer.closeDrawer(GravityCompat.START);
//            }
//        });

        Button btn_select_mode = findViewById(R.id.btn_select_mode);
        //btn_select_mode.setVisibility(View.VISIBLE);  // added by hslee signallink의 요청으로 삭제
        btn_select_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                {
                    DialogUtil.yesNo(MainActivity.this,
                            "Go to the mode selection screen?",
                            "",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //블루투스 연결 끊기
                                    ((MyApplication)getApplication()).uartDisconnect();

                                    //선택 데이터 초기화
                                    svs.setScreenMode(DefConstant.SCREEN_MODE.IDLE);

                                    //이동
                                    goIntent(ScreenModeSelectActivity.class, true);

                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                }
                else
                {
                    DialogUtil.yesCancelMode(MainActivity.this,
                            "Go to the mode selection screen?",
                            "Yes: Moved with logout.\n" +
                                    "Move: Go to the page without logging out.",

                            //로그아웃 후, 이동
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //블루투스 연결 끊기
                                    ((MyApplication)getApplication()).uartDisconnect();

                                    //자동 로그인 모드 끄기
                                    SharedUtil.save(SharedUtil.KEY.AUTO_NEXT_GO_MAIN.toString(), "");

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
                                            goIntent(ScreenModeSelectActivity.class, true);

                                        }

                                        @Override
                                        public boolean fail(String message) {

                                            //결과 출력
                                            ToastUtil.showLong("You have been logged out");

                                            //알 수 없음 모드로 변경하기
                                            SVS.getInstance().setScreenMode(DefConstant.SCREEN_MODE.UNKNOWN);

                                            //모드 선택화면으로 이동하기
                                            goIntent(ScreenModeSelectActivity.class, true);


                                            return true;
                                        }
                                    });

                                }
                            },

                            //팝업 닫기
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            },

                            //로그아웃 하지 않고, 단순히 이동
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //블루투스 연결 끊기
                                    ((MyApplication)getApplication()).uartDisconnect();

                                    //자동 로그인 모드 끄기
                                    SharedUtil.save(SharedUtil.KEY.AUTO_NEXT_GO_MAIN.toString(), "");

                                    //선택 데이터 초기화
                                    svs.setScreenMode(DefConstant.SCREEN_MODE.IDLE);

                                    //이동
                                    goIntent(ScreenModeSelectActivity.class, true);
                                }
                            }

                           );
                }




            }
        });

        //장비 수정 버튼
        btnDetailUpdate = findViewById(R.id.btnDetailUpdate);
        btnDetailUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                WebLoginEntity webLoginEntity = new RealmDao<WebLoginEntity>(WebLoginEntity.class).first();

                if(webLoginEntity == null || StringUtil.isEmpty(webLoginEntity.company_id))
                {
                    ToastUtil.showShort("Wrong Login Data. retry Login.");
                    return;
                }

                //장비 수정화면으로 이동
                String url = WEB_URL+"/dashboard/"+webLoginEntity.company_id;
                IntentUtil.goActivity(MainActivity.this, WebActivity.class, WebActivity.TARGET_URL, url);

            }
        });

        //장비 등록 버튼
        btnRegisterEquipment = findViewById(R.id.btn_register_equipment);
        btnRegisterEquipment.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap map = new HashMap();
                map.put("pipePumpMode", pipePumpMode);
                goIntent(RegisterActivity.class, false, map);
            }
        });

        //블루투스 연결 상태 버튼
        btnControlBle = findViewById(R.id.button_control_ble);
        btnControlBle.setText(R.string.button_ble_close);
        btnControlBle.setVisibility(View.INVISIBLE);
        btnControlBle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnControlBle.getText().toString().equals(getResources().getString(R.string.button_ble_close))) {
                    dialog_BleClose();
                }
            }
        });

        ImageView ivSignallinkHome = findViewById(R.id.signallinkhome);
        ivSignallinkHome.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentUtil.goHomepage(MainActivity.this);
            }
        });
    }

    public void resetViewsForScreenMode(){

        //스크린 모드 파악
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            //장비 수정 & 등록 버튼 처리
            btnDetailUpdate.setVisibility(View.GONE);
            btnRegisterEquipment.setVisibility(View.VISIBLE);

            //장비 리스트 어댑터
            //rEquipmentEntities = new RealmDao<EquipmentEntity>(EquipmentEntity.class).loadAllByFilter("isDeleted",false).sort("name");
            Realm realm = Realm.getDefaultInstance();   // // added by hslee 2020.04.27
            rEquipmentEntities = realm.where(EquipmentEntity.class)
                    .equalTo("type", pipePumpMode) // pipe, pump
                    .equalTo("isDeleted", false)
                    .findAll()
                    .sort("name", Sort.ASCENDING);

            equipmentAdapter = new EquipmentAdapter(rEquipmentEntities);

            //장비 리스트 뷰
            equipmentRecyclerView = findViewById(R.id.equipment_recycler_view);
            equipmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            equipmentRecyclerView.setAdapter(equipmentAdapter);
            ItemClickUtil.addTo(equipmentRecyclerView)
                    .setOnItemClickListener(onItemClickListener)
                    .setOnItemLongClickListener(onItemLongClickListener);

            //로컬 모드 일땐, 새로고침 없음.
            swipe.setEnabled(false);

            //현재 모드 상태 뷰
            //llCurrentLocalMode.setVisibility(View.VISIBLE);
            llCurrentLocalMode.setVisibility(View.GONE); // added by hslee
            llCurrentWebMode.setVisibility(View.GONE);
        }
        else if(svs.getScreenMode() == DefConstant.SCREEN_MODE.WEB)
        {
            //장비 수정 & 등록 버튼 처리
            btnDetailUpdate.setVisibility(View.VISIBLE);
            btnRegisterEquipment.setVisibility(View.GONE);

            //장비 리스트 어댑터
            Realm realm = Realm.getDefaultInstance();
            rWEquipmentEntities = realm.where(WEquipmentEntity.class).findAll();
            wequipmentAdapter = new WEquipmentAdapter(rWEquipmentEntities);

            //장비 리스트 뷰
            equipmentRecyclerView = findViewById(R.id.equipment_recycler_view);
            equipmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            equipmentRecyclerView.setAdapter(wequipmentAdapter);
            ItemClickUtil.addTo(equipmentRecyclerView)
                    .setOnItemClickListener(onItemClickListener)
                    .setOnItemLongClickListener(onItemLongClickListener);

            //웹 모드 일땐, 새로고침 넣기
            swipe.setEnabled(true);
            swipe.setRefreshing(true);

            //데이터 불러오기
            refreshWebDatas();

            //현재 모드 상태 뷰
            llCurrentLocalMode.setVisibility(View.GONE);
            llCurrentWebMode.setVisibility(View.VISIBLE);
        }

        //네비게이션 뷰
        //mainNavigationView.refreshViewsForScreenMode();
    }

    private void dialog_BleClose() {

        String equipmentName = "";

        RealmObject realmObject = svs.getLinkedEquipmentData();

        if(realmObject instanceof EquipmentEntity)
        {
            equipmentName = ((EquipmentEntity)realmObject).getName();
        }
        else if(realmObject instanceof WEquipmentEntity)
        {
            equipmentName = ((WEquipmentEntity)realmObject).name;
        }

        DialogUtil.bleClose(this,
                equipmentName,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //SVS 연결 끊김 정보 저장.
                        svs.setLinkedSvsUuid(null);


                        btnControlBle.setVisibility(View.INVISIBLE);
                        ((MyApplication)getApplication()).uartDisconnect();
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }

    private void dialog_AutoSave(int itemIdx){

        EquipmentEntity equipmentEntity = rEquipmentEntities.get(itemIdx);
        final String uuid = equipmentEntity.getUuid();
        final String name = equipmentEntity.getName();

        DialogUtil.autoSave(this,
                name,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        svs.setLinkedEquipmentUuid(uuid);
                        svs.setSelectedEquipmentUuid(uuid);

                        equipmentAdapter.setSelectedEquipmentUuid(uuid);
                        goIntent(SVSLocationAutoModeActivity.class);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        equipmentAdapter.setSelectedEquipmentUuid(svs.getSelectedEquipmentUuid());
                        equipmentAdapter.notifyDataSetChanged();

                        dialog.cancel();
                    }
                });
    }

    private ItemClickUtil.OnItemLongClickListener onItemLongClickListener = new ItemClickUtil.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {

            if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
            {
                EquipmentEntity equipmentEntity = rEquipmentEntities.get(position);
                final String uuid = equipmentEntity.getUuid();

                equipmentAdapter.setSelectedEquipmentUuid(uuid);
                equipmentAdapter.notifyDataSetChanged();


                dialog_AutoSave(position);
            }

            return true;
        }
    };


    private DatePickerDialog.OnDateSetListener datePickerDialoglistenerStart = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            //Toast.makeText(getApplicationContext(), year + "년" + monthOfYear + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
        }
    };

    private ItemClickUtil.OnItemClickListener onItemClickListener = new ItemClickUtil.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, final int position, View v) {

            if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
            {
                EquipmentEntity equipmentEntity = rEquipmentEntities.get(position);
                final String uuid = equipmentEntity.getUuid();
                final String name = equipmentEntity.getName();

                equipmentAdapter.setSelectedEquipmentUuid(uuid);
                equipmentAdapter.notifyDataSetChanged();

                customListDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {
                    @Override
                    public void onBtnClicked(int viewIdx) {

                        if(viewIdx == 0) {
                            svs.setSelectedEquipmentUuid(uuid);
                            goIntent(SVSLocationManualModeActivity.class);
                        }
                        else if(viewIdx == 1) {
                            svs.setSelectedEquipmentUuid(uuid);

                            HashMap map = new HashMap();
                            map.put(EXTRA_EQUIPMENT_UUID, uuid);
                            map.put("pipePumpMode", pipePumpMode);    // added by hslee 2020.06.18
                            map.put(EXTRA_EQUIPMENT_NAME, name);
                            goIntent(DetailUpdateActivity.class, false, map);
                        }
                        else if( viewIdx == 2 ) {   // added by hslee

                            svs.setLinkedEquipmentUuid(uuid);
                            svs.setSelectedEquipmentUuid(uuid);

                            equipmentAdapter.setSelectedEquipmentUuid(uuid);

                            if( "pipe".equals(pipePumpMode) ) {   // pipe 모드
                                Intent intent = new Intent(getBaseContext(), PipePresetActivity.class);
                                intent.putExtra("equipmentUuid", uuid);
                                startActivity(intent);
                            }
                            else {
                                RealmList<SVSEntity> svsEntityRealmList = ((EquipmentEntity) svs.getSelectedEquipmentData()).getSvsEntities();
                                if (svsEntityRealmList == null || svsEntityRealmList.size() != 3) {
                                    ToastUtil.showShort("3 sensor required.");
                                    return;
                                }

                                Intent intent = new Intent(getBaseContext(), PresetActivity.class);
                                intent.putExtra("equipmentUuid", uuid);
                                startActivity(intent);
                            }

//                            Intent intent = new Intent(getBaseContext(), MeasureModeSelectActivity.class);
//                            intent.putExtra("equipmentUuid", uuid);
//                            startActivity(intent);
//
                        }
                        else if( viewIdx == 3 ) {   // added by hslee 분석 내역

                            svs.setLinkedEquipmentUuid(uuid);
                            svs.setSelectedEquipmentUuid(uuid);

                            equipmentAdapter.setSelectedEquipmentUuid(uuid);

                            selectAnalysisHistory(uuid);

//                            Intent intent = new Intent(getBaseContext(), MeasureModeSelectActivity.class);
//                            intent.putExtra("type", "history");
//                            intent.putExtra("equipmentUuid", uuid);
//                            startActivity(intent);
                        }
                        else if( viewIdx == 4 ) {   // added by hslee Analysis History

                            try {
                                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                                String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                                Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                                long startLong = startDate.getTime();
                                long endLong = endDate.getTime();

                                Realm realm = Realm.getDefaultInstance();

                                RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                                        .equalTo("type", 1) // 1 rms, 2 frequency
                                        .greaterThanOrEqualTo("created", startLong)
                                        .lessThanOrEqualTo("created", endLong)
                                        .equalTo("equipmentUuid", uuid)
                                        .findAll()
                                        //.sort("created", Sort.DESCENDING);
                                        .sort("created", Sort.ASCENDING);

                                ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                                for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                                    RmsModel rmsModel = new RmsModel();
                                    rmsModel.setRms1(analysisEntity.getRms1());
                                    rmsModel.setRms2(analysisEntity.getRms2());
                                    rmsModel.setRms3(analysisEntity.getRms3());
                                    rmsModel.setbShowCause(analysisEntity.isbShowCause());
                                    rmsModel.setCreated(analysisEntity.getCreated());

                                    // added by hslee 2020.07.10
                                    if( analysisEntity.cause != null ) {
                                        rmsModel.cause = new String[analysisEntity.cause.size()];
                                        for (int i = 0; i < analysisEntity.cause.size(); i++) {
                                            rmsModel.cause[i] = analysisEntity.cause.get(i);
                                        }
                                    }
                                    if( analysisEntity.causeDesc != null ) {
                                        rmsModel.causeDesc = new String[analysisEntity.causeDesc.size()];
                                        for (int i = 0; i < analysisEntity.causeDesc.size(); i++) {
                                            rmsModel.causeDesc[i] = analysisEntity.causeDesc.get(i);
                                        }
                                    }
                                    if( analysisEntity.rank != null ) {
                                        rmsModel.rank = new double[analysisEntity.rank.size()];
                                        for (int i = 0; i < analysisEntity.rank.size(); i++) {
                                            rmsModel.rank[i] = analysisEntity.rank.get(i);
                                        }
                                    }
                                    if( analysisEntity.ratio != null ) {
                                        rmsModel.ratio = new double[analysisEntity.ratio.size()];
                                        for (int i = 0; i < analysisEntity.ratio.size(); i++) {
                                            rmsModel.ratio[i] = analysisEntity.ratio.get(i);
                                        }
                                    }

                                    rmsModelList.add(rmsModel);
                                }


                                // 다음 화면으로 이동
                                Intent intent = new Intent(getBaseContext(), RecordManagerActivity.class);
                                intent.putExtra("equipmentUuid", uuid);
                                intent.putExtra("previousRmsModelList", rmsModelList);
                                startActivity(intent);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if( viewIdx == 4 ) {   // added by hslee Pipe Analysis History

                            try {
                                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                                String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                                Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                                long startLong = startDate.getTime();
                                long endLong = endDate.getTime();

                                Realm realm = Realm.getDefaultInstance();

                                RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                                        .equalTo("type", 2) // 1 rms, 2 frequency
                                        .greaterThanOrEqualTo("created", startLong)
                                        .lessThanOrEqualTo("created", endLong)
                                        .equalTo("equipmentUuid", uuid)
                                        .findAll()
                                        //.sort("created", Sort.DESCENDING);
                                        .sort("created", Sort.ASCENDING);

                                ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                                for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                                    RmsModel rmsModel = new RmsModel();
                                    rmsModel.setRms1(analysisEntity.getRms1());
                                    rmsModel.setCreated(analysisEntity.getCreated());

                                    rmsModelList.add(rmsModel);
                                }

                                // 다음 화면으로 이동
                                Intent intent = new Intent(getBaseContext(), PipeRecordManagerActivity.class);
                                intent.putExtra("equipmentUuid", uuid);
                                intent.putExtra("previousRmsModelList", rmsModelList);
                                startActivity(intent);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if( viewIdx == 5 ) {// added by hslee for test


                            // 테스트 데이터 추가
                            save(uuid, 10.0f,11.0f,12.0f,"2020-02-01 16:11:05");
                            save(uuid, 110.0f,111.0f,112.0f,"2020-02-01 16:12:05");
                            save(uuid, 20.0f,21.0f,22.0f,"2020-02-02 16:11:05");
                            save(uuid, 70.0f,71.0f,72.0f,"2020-02-03 16:11:05");
                            save(uuid, 10.0f,31.0f,52.0f,"2020-02-04 16:11:05");
                            save(uuid, 40.0f,41.0f,42.0f,"2020-02-07 16:11:05");
                        }
                    }

                    @Override
                    public void onBtnCancelClicked() {

                    }
                });

                customListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        equipmentAdapter.setSelectedEquipmentUuid(svs.getSelectedEquipmentUuid());
                        equipmentAdapter.notifyDataSetChanged();
                    }
                });

                customListDialog.show();
            }
            else
            {
                WEquipmentEntity wequipmentEntity = rWEquipmentEntities.get(position);
                final String id = wequipmentEntity.id;
                final String company_id = wequipmentEntity.company_id;

                wequipmentAdapter.setSelectedEquipmentUuid(id);
                wequipmentAdapter.notifyDataSetChanged();

                //선택한 정보 저장
                svs.setSelectedEquipmentUuid(id);

                //화면 이동
                goIntent(SVSLocationManualModeActivity.class);


//                customListDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {
//                    @Override
//                    public void onBtnClicked(int viewIdx) {
//
//                        if(viewIdx == 1)
//                        {
//                            svs.setSelectedEquipmentUuid(id);
//                            goIntent(SVSLocationManualModeActivity.class);
//                        }
//                        else if(viewIdx == 2)
//                        {
//                            svs.setSelectedEquipmentUuid(id);
//
//                            String url = WEB_URL+"/dashboard/"+company_id;
//                            IntentUtil.goActivity(MainActivity.this, WebActivity.class, WebActivity.TARGET_URL, url);
//                        }
//                    }
//
//                    @Override
//                    public void onBtnCancelClicked() {
//
//                    }
//                });
//
//                customListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//
//                        wequipmentAdapter.setSelectedEquipmentUuid(svs.getSelectedEquipmentUuid());
//                        wequipmentAdapter.notifyDataSetChanged();
//                    }
//                });
//
//                customListDialog.show();
            }
        }
    };

    // added by hslee 2020.04.27
    void selectAnalysisHistory(String uuid) {
        if( "pipe".equals(pipePumpMode) ) { // pipe 모드
            try {
                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                long startLong = startDate.getTime();
                long endLong = endDate.getTime();

                Realm realm = Realm.getDefaultInstance();

                RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                        .equalTo("type", 2) // 1 rms, 2 frequency
                        .greaterThanOrEqualTo("created", startLong)
                        .lessThanOrEqualTo("created", endLong)
                        .equalTo("equipmentUuid", uuid)
                        .findAll()
                        //.sort("created", Sort.DESCENDING);
                        .sort("created", Sort.ASCENDING);

                ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                    RmsModel rmsModel = new RmsModel();
                    rmsModel.setRms1(analysisEntity.getRms1());
                    rmsModel.setRms2(analysisEntity.getRms2());
                    rmsModel.setRms3(analysisEntity.getRms3());
                    rmsModel.setCreated(analysisEntity.getCreated());

                    float [] newFreq1 = new float[analysisEntity.getFrequency1().size()];
                    for( int i = 0; i<analysisEntity.getFrequency1().size(); i++ ) {
                        newFreq1[i] = analysisEntity.getFrequency1().get(i).floatValue();
                    }
                    rmsModel.setFrequency1(newFreq1);

                    float [] newFreq2 = new float[analysisEntity.getFrequency2().size()];
                    for( int i = 0; i<analysisEntity.getFrequency2().size(); i++ ) {
                        newFreq2[i] = analysisEntity.getFrequency2().get(i).floatValue();
                    }
                    rmsModel.setFrequency2(newFreq2);

                    float [] newFreq3 = new float[analysisEntity.getFrequency3().size()];
                    for( int i = 0; i<analysisEntity.getFrequency3().size(); i++ ) {
                        newFreq3[i] = analysisEntity.getFrequency3().get(i).floatValue();
                    }
                    rmsModel.setFrequency3(newFreq3);

                    EquipmentEntity selectedEquipmentEntity = new RealmDao<>(EquipmentEntity.class).loadByUuid(uuid);
                    rmsModel.setPipeName(selectedEquipmentEntity.getName());
                    rmsModel.setPipeImage(selectedEquipmentEntity.getImageUri());
                    rmsModel.setPipeLocation(analysisEntity.getPipeLocation());
                    rmsModel.setPipeOperationScenario(analysisEntity.getPipeOperationScenario());

                    rmsModelList.add(rmsModel);
                }


                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), PipeRecordManagerActivity.class);
                intent.putExtra("equipmentUuid", uuid);
                intent.putExtra("previousRmsModelList", rmsModelList);
                startActivity(intent);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                String endd = Utils.getCurrentTime("yyyy-MM-dd");
                String tEndd = Utils.addDateDay(endd, 1, "yyyy-MM-dd"); // 00시부터 계산하기 때문에 다음날 0시이전의 데이터를 가져오기 위해 +1해줌
                String startd = Utils.addDateDay(endd, -7, "yyyy-MM-dd");

                Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startd);
                Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(tEndd);

                long startLong = startDate.getTime();
                long endLong = endDate.getTime();

                Realm realm = Realm.getDefaultInstance();

                RealmResults<AnalysisEntity> preiviousAnalysisEntityList = realm.where(AnalysisEntity.class)
                        .equalTo("type", 1) // 1 rms, 2 frequency
                        .greaterThanOrEqualTo("created", startLong)
                        .lessThanOrEqualTo("created", endLong)
                        .equalTo("equipmentUuid", uuid)
                        .findAll()
                        //.sort("created", Sort.DESCENDING);
                        .sort("created", Sort.ASCENDING);

                ArrayList<RmsModel> rmsModelList = new ArrayList<>();

                for( AnalysisEntity analysisEntity : preiviousAnalysisEntityList ) {
                    RmsModel rmsModel = new RmsModel();
                    rmsModel.setRms1(analysisEntity.getRms1());
                    rmsModel.setRms2(analysisEntity.getRms2());
                    rmsModel.setRms3(analysisEntity.getRms3());
                    rmsModel.setbShowCause(analysisEntity.isbShowCause());
                    rmsModel.setCreated(analysisEntity.getCreated());

                    // added by hslee 2020.07.10
                    if( analysisEntity.cause != null ) {
                        rmsModel.cause = new String[analysisEntity.cause.size()];
                        for (int i = 0; i < analysisEntity.cause.size(); i++) {
                            rmsModel.cause[i] = analysisEntity.cause.get(i);
                        }
                    }
                    if( analysisEntity.causeDesc != null ) {
                        rmsModel.causeDesc = new String[analysisEntity.causeDesc.size()];
                        for (int i = 0; i < analysisEntity.causeDesc.size(); i++) {
                            rmsModel.causeDesc[i] = analysisEntity.causeDesc.get(i);
                        }
                    }
                    if( analysisEntity.rank != null ) {
                        rmsModel.rank = new double[analysisEntity.rank.size()];
                        for (int i = 0; i < analysisEntity.rank.size(); i++) {
                            rmsModel.rank[i] = analysisEntity.rank.get(i);
                        }
                    }
                    if( analysisEntity.ratio != null ) {
                        rmsModel.ratio = new double[analysisEntity.ratio.size()];
                        for (int i = 0; i < analysisEntity.ratio.size(); i++) {
                            rmsModel.ratio[i] = analysisEntity.ratio.get(i);
                        }
                    }

                    rmsModelList.add(rmsModel);
                }

                // 다음 화면으로 이동
                Intent intent = new Intent(getBaseContext(), RecordManagerActivity.class);
                intent.putExtra("equipmentUuid", uuid);
                intent.putExtra("previousRmsModelList", rmsModelList);
                startActivity(intent);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // db에 진단 결과 데이터 저장
    void save(final String uuid, final float rms1, final float rms2, final float rms3, final String tCreated) {   // FOR TEST
        DatabaseUtil.transaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                int id = 0;
                Number currentNo = realm.where(AnalysisEntity.class).max("id");

                if (currentNo == null) {    // index 값 증가
                    id = 1;
                } else {
                    id = currentNo.intValue() + 1;
                }

                AnalysisEntity analysisEntity = new AnalysisEntity();
                analysisEntity.setId(id);
                analysisEntity.setEquipmentUuid(uuid);

                //String tCreated = Utils.getCurrentTime("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date created = null;
                try {
                    created = simpleDateFormat.parse(tCreated);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                long createdLong = created.getTime();

                analysisEntity.setCreated(createdLong);

                analysisEntity.setRms1(rms1);
                analysisEntity.setRms2(rms2);
                analysisEntity.setRms3(rms3);

                RealmList<String> cause = new RealmList<>();
                cause.add("test1"+tCreated);
                cause.add("test2"+tCreated);
                cause.add("test3"+tCreated);
                cause.add("test4"+tCreated);
                analysisEntity.setCause(cause);

                RealmList<String> causeDesc = new RealmList<>();
                causeDesc.add("causeDesctest1");
                causeDesc.add("causeDesctest2");
                causeDesc.add("causeDesctest3");
                causeDesc.add("causeDesctest4");
                analysisEntity.setCauseDesc(causeDesc);

                RealmList<Double> rank = new RealmList<>();
                rank.add(1.0);
                rank.add(2.0);
                rank.add(3.0);
                rank.add(4.0);
                analysisEntity.setRank(rank);

                RealmList<Double> ratio = new RealmList<>();
                ratio.add(0.08);
                ratio.add(0.052);
                ratio.add(0.04);
                ratio.add(0.023);
                analysisEntity.setRatio(ratio);

                realm.copyToRealmOrUpdate(analysisEntity);
            }
        });
    }

    @Override
    public void onRefresh() {

        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.WEB)
        {
            refreshWebDatas();
        }
        else
        {
            //새로고침 프로그래스 종료
            swipe.setRefreshing(false);
        }

    }



    private void refreshWebDatas()
    {
        final RealmResults<WebLoginEntity> webLoginEntities = new RealmDao<WebLoginEntity>(WebLoginEntity.class).loadAll();
        if(webLoginEntities.size() > 0)
        {
            WebLoginEntity webLoginEntity = webLoginEntities.get(0);
            final String company_id = webLoginEntity.company_id;

            //Equipment List API 호출
            APIManager.getInstance().getEquipmentList(company_id, new OnApiListener() {
                @Override
                public void success(APIResponse apiResponse) {

                    EquipmentGetListResponse equipmentGetListResponse = (EquipmentGetListResponse)apiResponse;

                    final List<EquipmentGetListResponse.Datum> list = equipmentGetListResponse.list;
                    if(list.size() == 0)
                    {
                        ToastUtil.showShort("No data. Please add it in the web manager.");

                        //이전 데이터 삭제
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                RealmResults<WEquipmentEntity> wEquipmentEntities = new RealmDao<WEquipmentEntity>(WEquipmentEntity.class).loadAll();
                                wEquipmentEntities.deleteAllFromRealm();
                            }
                        });

                    }
                    else
                    {
                        //이전 데이터 삭제
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                RealmResults<WEquipmentEntity> wEquipmentEntities = new RealmDao<WEquipmentEntity>(WEquipmentEntity.class).loadAll();
                                wEquipmentEntities.deleteAllFromRealm();
                            }
                        });

                        //신규 데이터 추가
                        DatabaseUtil.transaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                                for(EquipmentGetListResponse.Datum datum : list)
                                {
                                    WEquipmentEntity wEquipmentEntity = new WEquipmentEntity();
                                    wEquipmentEntity.id = datum.id;
                                    wEquipmentEntity.name = datum.name;
                                    wEquipmentEntity.company_name = datum.company_name;
                                    wEquipmentEntity.factory_name = datum.factory_name;
                                    wEquipmentEntity.picture = datum.picture;
                                    wEquipmentEntity.created = datum.created;

                                    wEquipmentEntity.company_id = company_id;
                                    wEquipmentEntity.factory_id = datum.factory_id;

                                    realm.copyToRealmOrUpdate(wEquipmentEntity);
                                }
                            }
                        });
                    }

                    //새로고침 프로그래스 종료
                    swipe.setRefreshing(false);
                }

                @Override
                public boolean fail(String message) {

                    //새로고침 프로그래스 종료
                    swipe.setRefreshing(false);

                    return false;
                }
            });
        }
        else
        {
            ToastUtil.showShort("Wrong Login Info.");

            //새로고침 프로그래스 종료
            swipe.setRefreshing(false);
        }
    }
}
