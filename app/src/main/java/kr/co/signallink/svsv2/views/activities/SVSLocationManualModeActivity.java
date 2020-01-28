package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.databases.dao.RealmDao;
import kr.co.signallink.svsv2.databases.web.WEquipmentEntity;
import kr.co.signallink.svsv2.databases.web.WSVSEntity;
import kr.co.signallink.svsv2.restful.APIManager;
import kr.co.signallink.svsv2.restful.OnApiListener;
import kr.co.signallink.svsv2.restful.response.APIResponse;
import kr.co.signallink.svsv2.restful.response.SvsGetListResponse;
import kr.co.signallink.svsv2.server.OnTCPSendCallback;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.services.bluetooth.DDBluetoothDevice;
import kr.co.signallink.svsv2.services.bluetooth.DDBluetoothManager;
import kr.co.signallink.svsv2.services.bluetooth.OnDDBluetoothListener;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.user.ConnectSVSItems;
import kr.co.signallink.svsv2.user.ConnectSVSItem;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.ItemClickUtil;
import kr.co.signallink.svsv2.utils.ProgressDialogUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.adapters.SVSAdapter;
import kr.co.signallink.svsv2.services.UartService;
import kr.co.signallink.svsv2.views.adapters.WSVSAdapter;
import kr.co.signallink.svsv2.views.custom.CustomListDialog;

import static kr.co.signallink.svsv2.commons.DefConstant.WEB_URL;

public class SVSLocationManualModeActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "SVSLocationManualModeActivity";

    //Service
    private SVS svs = SVS.getInstance();
    private UartService uartService = null;
    private BluetoothAdapter checkbluetooth = null;

    //View
    private TextView title_textview;
    private Button button_ble_search;
    private Button button_record;
    private SwipeRefreshLayout swipe;
    private RecyclerView svslocphotolistView = null;
    private CustomListDialog customListButtonDialog;
    private CustomListDialog customListButtonDialogWeb;
    private ProgressDialog progressDialog = null;

    //CurrentMode View
    private LinearLayout llCurrentLocalMode, llCurrentWebMode;

    //Data
    private ConnectSVSItems connectSVSItems = null;
    private String tempSvsUuid = null;

    //List
    private SVSAdapter svsAdapter;
    private WSVSAdapter wsvsAdapter;
    private OrderedRealmCollection<SVSEntity> svsEntities;
    private OrderedRealmCollection<WSVSEntity> wsvsEntities;

    //블루투스 매니저
    private DDBluetoothManager ddBluetoothManager = null;

    //Next Activities
    private int idxAutoNextActivity = -1;
    private ArrayList<AutoNextActivity> autoNextActivities = new ArrayList<>();
    class AutoNextActivity {
        Class nextActivity;
        boolean enabled;

        AutoNextActivity(Class next){
            this.nextActivity = next;
            this.enabled = true;
        }
    }

    private final BroadcastReceiver StatusChangeReceiverOnSVSLocPhotoManualMode = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //*********************//
            if (action.equals(DefBLEdata.DISCOVERED_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(svs.getLinkedEquipmentData() != null) {
                            if(!connectSVSItems.isAutoSaveMode()) {
                                connectSVSItems.nextIndex();  // when disconnect, not more connecting.
                            }
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.HELLO_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if(svs.getLinkedEquipmentData() != null)
                        {
                            String equipmentName = "";
                            String svsName = "";

                            if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                            {
                                equipmentName = ((EquipmentEntity)svs.getLinkedEquipmentData()).getName();
                                svsName = ((SVSEntity)svs.getLinkedSvsData()).getSvsLocation().toString();
                                svsName = svsName.toUpperCase();
                            }
                            else
                            {
                                equipmentName = ((WEquipmentEntity)svs.getLinkedEquipmentData()).name;
                                svsName = ((WSVSEntity)svs.getLinkedSvsData()).name;
                            }

                            String strmsg = String.format("%s (%s) \r\n\r\n%s", equipmentName, svsName, getResources().getString(R.string.requestsvsinfo));
                            progressDialog.setMessage(strmsg);
                            DefLog.d(TAG, "HELLO_ARRIVE");
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.UPLOAD_ARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "UPLOAD_ARRIVE");

                        if(svs.getLinkedEquipmentData() != null) {
                            progressDialog.dismiss();
                        }

                        if(connectSVSItems != null && connectSVSItems.size() > 0) {
                            if(!connectSVSItems.isAutoSaveMode() && connectSVSItems.isCanGo()) {

                                final AutoNextActivity autoNextActivity = autoNextActivities.get(idxAutoNextActivity);
                                if(autoNextActivity.enabled)
                                {
                                    doUploadSensorInfoAndAutoNextActivity(autoNextActivity);
                                }
                            }
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.UPLOAD_NOTARRIVE)) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        if(svs.getLinkedEquipmentData() != null) {
                            DefLog.d(TAG, "UPLOAD_NOTARRIVE");

                            progressDialog.dismiss();
                            ToastUtil.showShort(R.string.notuploaded);

                            uartService.disconnect();
                        }
                    }
                });
            } else if (action.equals(DefBLEdata.DISCONNECTION_ARRIVE)) {

                runOnUiThread(new Runnable() {
                    public void run() {

                        DefLog.d(TAG, "DISCONNECTION_ARRIVE");
                        button_ble_search.setVisibility(View.INVISIBLE);

                        if (!checkbluetooth.isEnabled()) {
                            DefLog.d(TAG, "onResume - BT not enabled yet");
                            checkbluetooth.enable();
                        }

                        progressDialog.dismiss();

                        if(connectSVSItems != null)
                        {
                            int connectSVSpairSize = connectSVSItems.size();
                            if(connectSVSItems != null && connectSVSpairSize > 0) {
                                svs.clear(); // 20180813

                                if(connectSVSItems.getIndex_connecting() < connectSVSpairSize)
                                {
                                    String equipmentName = "";
                                    String svsName = "";

                                    if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                    {
                                        equipmentName = ((EquipmentEntity)svs.getLinkedEquipmentData()).getName();
                                        svsName = ((SVSEntity)svs.getLinkedSvsData()).getSvsLocation().toString();
                                        svsName = svsName.toUpperCase();
                                    }
                                    else
                                    {
                                        equipmentName = ((WEquipmentEntity)svs.getLinkedEquipmentData()).name;
                                        svsName = ((WSVSEntity)svs.getLinkedSvsData()).name;
                                    }

                                    String strmsg = String.format("%s (%s) \r\n\r\n%s", equipmentName, svsName, getResources().getString(R.string.requestconnect));
                                    progressDialog.setMessage(strmsg);
                                    progressDialog.show();


                                    //다음 접속하게 하기
                                    if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                    {
                                        //주소로 바로 접속하기
                                        connectBluetoothAddress(connectSVSItems.getCurrentIndexAddress());
                                    }
                                    else
                                    {
                                        //이름을 찾아서 접속하기
                                        ConnectSVSItem connectSVSItem = connectSVSItems.getCurrentItem();
                                        findSvsAddressAndConnect(connectSVSItem.serialNo);
                                    }
                                }

                            } else if(connectSVSItems != null && connectSVSpairSize == 0){

                                //SVS 연결이 끊겼음을 저장
                                svs.setLinkedSvsUuid(null);

                                ToastUtil.showShort(R.string.notregistered, "1");
                            }
                        }

                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svslocphoto);


        //CurrentMode
        llCurrentLocalMode = findViewById(R.id.llCurrentLocalMode);
        llCurrentWebMode = findViewById(R.id.llCurrentWebMode);

        progressDialog = new ProgressDialog(SVSLocationManualModeActivity.this);
        progressDialog.setTitle("Requesting");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);



        customListButtonDialog = new CustomListDialog(this, R.array.custom_list_dialog_svslocation);
        customListButtonDialogWeb = new CustomListDialog(this, R.array.custom_list_dialog_wsvslocation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        title_textview = toolbar.findViewById(R.id.toolbar_title);
        title_textview.setText(R.string.manualmode_screen);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        init_service();

        button_ble_search = findViewById(R.id.button_control_ble);
        button_ble_search.setText(R.string.button_ble_close);
        button_ble_search.setVisibility(View.INVISIBLE);

        button_ble_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_ble_search.getText().toString().equals(getResources().getString(R.string.button_ble_close))) {
                    dialogBleClose();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(StatusChangeReceiverOnSVSLocPhotoManualMode, makeUpdateIntentFilter());

        checkbluetooth = BluetoothAdapter.getDefaultAdapter();
        if (checkbluetooth == null) {
            ToastUtil.showShort("Bluetooth is not available");
            finish();
            return;
        }

        button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        if(svs.getSelectedEquipmentData() == null)
        {
            //Error. reShow Acitivity catch.

            finish();
            return;
        }

        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);



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

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(StatusChangeReceiverOnSVSLocPhotoManualMode);
        } catch (Exception e) {
            DefLog.d(TAG, e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState()) {
            button_ble_search.setVisibility(View.VISIBLE);
        }

        populateList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == DefConstant.REQUEST_ENABLE_BT) {
            progressDialog.dismiss();

            if(connectSVSItems != null && connectSVSItems.size() > 0) {
                svs.clear(); // 20180813

                if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
                {
                    String equipmentName = "";
                    String svsName = "";

                    if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                    {
                        equipmentName = ((EquipmentEntity)svs.getLinkedEquipmentData()).getName();
                        svsName = ((SVSEntity)svs.getLinkedSvsData()).getSvsLocation().toString();
                        svsName = svsName.toUpperCase();
                    }
                    else
                    {
                        equipmentName = ((WEquipmentEntity)svs.getLinkedEquipmentData()).name;
                        svsName = ((WSVSEntity)svs.getLinkedSvsData()).name;
                    }

                    String strMsg = String.format("%s (%s) \r\n\r\n%s", equipmentName, svsName, getResources().getString(R.string.requestconnect));

                    progressDialog.setMessage(strMsg);
                    progressDialog.show();
                    boolean connected = uartService.connect(connectSVSItems.getCurrentIndexAddress());
                    if(!connected) {
                        progressDialog.dismiss();
                    }
                    connectSVSItems.nextIndex();
                }

            } else {
                ToastUtil.showShort(R.string.notregistered, "2");
            }
        }
    }

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

    private void init_service() {
        uartService = svs.getUartService();
    }


    private void populateList() {

        //스크린 모드 파악
        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            RealmList<SVSEntity> svsEntityRealmList = ((EquipmentEntity)svs.getSelectedEquipmentData()).getSvsEntities();
            svsEntities = svsEntityRealmList.sort("SVS_LOCATION"); //Location 정렬..
//            svsEntities = svsEntityRealmList;

            svsAdapter = new SVSAdapter(svsEntities);
            svsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
            if(svsEntities.size() > 0){
                svsAdapter.notifyDataSetChanged();
            }

            svslocphotolistView = findViewById(R.id.new_svslocphoto);
            svslocphotolistView.setLayoutManager(new LinearLayoutManager(this));
            svslocphotolistView.setAdapter(svsAdapter);
            ItemClickUtil.addTo(svslocphotolistView)
                    .setOnItemClickListener(onItemClickListener)
                    .setOnItemLongClickListener(onItemLongClickListener);

            //로컬 모드 일땐, 새로고침 없음.
            swipe.setEnabled(false);

            //현재 모드 상태 뷰
            llCurrentLocalMode.setVisibility(View.VISIBLE);
            llCurrentWebMode.setVisibility(View.GONE);
        }
        else if(svs.getScreenMode() == DefConstant.SCREEN_MODE.WEB)
        {
            String selectedEquipmentId = svs.getSelectedEquipmentUuid();

            wsvsEntities = new RealmDao<WSVSEntity>(WSVSEntity.class).loadAllByFilter("equipment_id", selectedEquipmentId);

            wsvsAdapter = new WSVSAdapter(wsvsEntities);
            wsvsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
            if(wsvsEntities.size() > 0){
                wsvsAdapter.notifyDataSetChanged();
            }

            svslocphotolistView = findViewById(R.id.new_svslocphoto);
            svslocphotolistView.setLayoutManager(new LinearLayoutManager(this));
            svslocphotolistView.setAdapter(wsvsAdapter);
            ItemClickUtil.addTo(svslocphotolistView)
                    .setOnItemClickListener(onItemClickListenerWeb)
                    .setOnItemLongClickListener(onItemLongClickListener);

            //웹 모드 일땐, 새로고침 필요.
            swipe.setEnabled(true);
            swipe.setRefreshing(true);

            //신규 데이터 요청
            refreshWebDatas();

            //현재 모드 상태 뷰
            llCurrentLocalMode.setVisibility(View.GONE);
            llCurrentWebMode.setVisibility(View.VISIBLE);
        }



    }

    private ItemClickUtil.OnItemClickListener onItemClickListener = new ItemClickUtil.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, final int position, View v) {

            Log.d("TTTT", "SVSLocationManualModeActivity onItemClick position:"+position);

            SVSEntity svsEntity = svsEntities.get(position);
            final String svsEntityUuid = svsEntity.getUuid();
            final String svsEntityName = svsEntity.getName();
            final String svsEntityAddress = svsEntity.getAddress();
            final DefFile.SVS_LOCATION svsLocation = svsEntity.getSvsLocation();

            svsAdapter.setSelectedSvsUuid(svsEntityUuid);
            svsAdapter.notifyDataSetChanged();

            customListButtonDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {

                @Override
                public void onBtnClicked(int viewIdx) {

                    if(viewIdx == 1)
                    {
                        //Monitoring

                        String msg = "START : Manual connect ";
                        DefLog.d(TAG, msg);

                        if (uartService != null && DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState())
                        {
                            if(svs.getLinkedSvsData() != null && svsEntityUuid.equals(svs.getLinkedSvsUuid())) {
                                goIntent(MonitoringTrendActivity.class);
                            }
                            else
                            {
                                autoNextActivities.add(new AutoNextActivity(MonitoringTrendActivity.class));
                                idxAutoNextActivity++;

                                connectSVSItems = new ConnectSVSItems();
                                connectSVSItems.setAutoSaveMode(false);

                                //접속용 기기 정보 만들기
                                ConnectSVSItem connectSVSPair = new ConnectSVSItem();
                                connectSVSPair.setAddress(svsEntityAddress);
                                connectSVSPair.setSvsLocation(svsLocation);
                                connectSVSItems.add(connectSVSPair);

                                //해당 기기로 접속
                                svs.setSelectedSvsUuid(svsEntityUuid);
                                svs.setLinkedSvsUuid(svsEntityUuid);
                                svs.setLinkedEquipmentUuid(svs.getSelectedEquipmentUuid());

                                msg = "START : Manual disconnect ";
                                DefLog.d(TAG, msg);
                                uartService.disconnect();

                                if(connectSVSItems.size() == 0) {
                                    ToastUtil.showShort(R.string.notregistered,"3");
                                }
                            }
                        }
                        else
                        {

                            autoNextActivities.add(new AutoNextActivity(MonitoringTrendActivity.class));
                            idxAutoNextActivity++;

                            if (!checkbluetooth.isEnabled()) {
                                DefLog.d(TAG, "onResume - BT not enabled yet");
                                dialogBleOnOff();
                                tempSvsUuid = svsEntityUuid;
                            }
                            else
                            {
                                svs.clear(); // 20180813

                                connectSVSItems = new ConnectSVSItems();
                                connectSVSItems.setAutoSaveMode(false);


                                //접속용 기기 정보 만들기
                                ConnectSVSItem connectSVSpair = new ConnectSVSItem();
                                connectSVSpair.setAddress(svsEntityAddress);
                                connectSVSpair.setSvsLocation(svsLocation);
                                connectSVSItems.add(connectSVSpair);

                                //해당 기기로 접속
                                svs.setSelectedSvsUuid(svsEntityUuid);
                                svs.setLinkedSvsUuid(svsEntityUuid);
                                svs.setLinkedEquipmentUuid(svs.getSelectedEquipmentUuid());

                                if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
                                {
                                    String equipmentName = "";
                                    String svsName = "";

                                    if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                    {
                                        equipmentName = ((EquipmentEntity)svs.getSelectedEquipmentData()).getName();
                                        svsName = ((SVSEntity)svs.getSelectedSvsData()).getSvsLocation().toString();
                                        svsName = svsName.toUpperCase();
                                    }
                                    else
                                    {
                                        equipmentName = ((WEquipmentEntity)svs.getSelectedEquipmentData()).name;
                                        svsName = ((WSVSEntity)svs.getSelectedSvsData()).name;
                                    }

                                    String strmsg = String.format("%s (%s) \r\n\r\n%s", equipmentName, svsName, getResources().getString(R.string.requestconnect));
                                    progressDialog.setMessage(strmsg);
                                    progressDialog.setOnCancelListener(null);
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();


                                    //주소로 바로 접속하기
                                    connectBluetoothAddress(connectSVSItems.getCurrentIndexAddress());

                                }

                                if(connectSVSItems.size() == 0)
                                {
                                    ToastUtil.showShort(R.string.notregistered,"4");
                                }
                            }
                        }
                    }
                    else if(viewIdx == 2)
                    {
                        //History => Trend

                        svs.setSelectedSvsUuid(svsEntityUuid);

                        //History
                        HashMap map = new HashMap();
                        map.put(EXTRA_SVS_UUID, svsEntityUuid);
                        map.put(EXTRA_SVS_ADDRESS, svsEntityAddress);

                        goIntent(ChartHistoryActivity.class, false, map);
                    }
                }


                @Override
                public void onBtnCancelClicked()
                {
                    svsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
                    svsAdapter.notifyDataSetChanged();
                }
            });

            customListButtonDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    svsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
                    svsAdapter.notifyDataSetChanged();
                }
            });

            customListButtonDialog.show();
        }
    };

    private ItemClickUtil.OnItemClickListener onItemClickListenerWeb = new ItemClickUtil.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, final int position, View v) {

            Log.d("TTTT", "SVSLocationManualModeActivity onItemClick position:"+position);

            WSVSEntity wsvsEntity = wsvsEntities.get(position);
            final String svsEntityUuid = wsvsEntity.id;
            final String svsEntityName = wsvsEntity.name;
            final String svsEntitySerialNo = wsvsEntity.serial_no;
            final String svsEntityAddress = wsvsEntity.getAddress();


            WEquipmentEntity wequipmentEntity = (WEquipmentEntity)svs.getSelectedEquipmentData();
            final String equipment_id = wequipmentEntity.id;
            final String company_id = wequipmentEntity.company_id;
            final String factory_id = wequipmentEntity.factory_id;

            wsvsAdapter.setSelectedSvsUuid(svsEntityUuid);
            wsvsAdapter.notifyDataSetChanged();

            customListButtonDialogWeb.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {

                @Override
                public void onBtnClicked(int viewIdx) {

                    if(viewIdx == 1)
                    {
                        //Monitoring & Trend

                        //ex. http://58.150.28.162/measure/91?factory_id=159&equipment_id=80
                        String url = WEB_URL+"/measure/"+company_id+"?factory_id="+factory_id+"&equipment_id="+equipment_id;
                        IntentUtil.goActivity(SVSLocationManualModeActivity.this, WebActivity.class, WebActivity.TARGET_URL, url);
                    }
                    if(viewIdx == 2)
                    {
                        //Monitoring & Raw Data

                        String msg = "START : Manual connect ";
                        DefLog.d(TAG, msg);


                        if (uartService != null && DefConstant.UART_PROFILE_CONNECTED == svs.getBleConnectState())
                        {
                            if(svs.getLinkedSvsData() != null && svsEntityUuid.equals(svs.getLinkedSvsUuid())) {
                                goIntent(MonitoringRawDataActivity.class);
                            }
                            else
                            {
                                autoNextActivities.add(new AutoNextActivity(MonitoringRawDataActivity.class));
                                idxAutoNextActivity++;

                                connectSVSItems = new ConnectSVSItems();
                                connectSVSItems.setAutoSaveMode(false);

                                //접속용 기기 정보 만들기
                                ConnectSVSItem connectSVSPair = new ConnectSVSItem();
                                connectSVSPair.setDeviceName(svsEntityName);
                                connectSVSPair.setAddress(svsEntityAddress);
                                connectSVSPair.serialNo = svsEntitySerialNo;
                                connectSVSItems.add(connectSVSPair);

                                //해당 기기로 접속
                                svs.setSelectedSvsUuid(svsEntityUuid);
                                svs.setLinkedSvsUuid(svsEntityUuid);
                                svs.setLinkedEquipmentUuid(svs.getSelectedEquipmentUuid());

                                msg = "START : Manual disconnect ";
                                DefLog.d(TAG, msg);
                                uartService.disconnect();

                                if(connectSVSItems.size() == 0) {
                                    ToastUtil.showShort(R.string.notregistered,"5");
                                }
                            }
                        }
                        else
                        {
                            autoNextActivities.add(new AutoNextActivity(MonitoringRawDataActivity.class));
                            idxAutoNextActivity++;

                            if (!checkbluetooth.isEnabled()) {
                                DefLog.d(TAG, "onResume - BT not enabled yet");
                                dialogBleOnOff();
                                tempSvsUuid = svsEntityUuid;
                            }
                            else
                            {
                                svs.clear(); // 20180813

                                connectSVSItems = new ConnectSVSItems();
                                connectSVSItems.setAutoSaveMode(false);


                                //접속용 기기 정보 만들기
                                ConnectSVSItem connectSVSpair = new ConnectSVSItem();
                                connectSVSpair.setDeviceName(svsEntityName);
                                connectSVSpair.setAddress(svsEntityAddress);
                                connectSVSpair.serialNo = svsEntitySerialNo;
                                connectSVSItems.add(connectSVSpair);

                                //해당 기기로 접속
                                svs.setSelectedSvsUuid(svsEntityUuid);
                                svs.setLinkedSvsUuid(svsEntityUuid);
                                svs.setLinkedEquipmentUuid(svs.getSelectedEquipmentUuid());

                                if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
                                {
                                    String equipmentName = "";
                                    String svsName = "";
                                    String svsSerialNo = "";

                                    if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                    {
                                        equipmentName = ((EquipmentEntity)svs.getSelectedEquipmentData()).getName();
                                        svsName = ((SVSEntity)svs.getSelectedSvsData()).getSvsLocation().toString();
                                        svsName = svsName.toUpperCase();
                                    }
                                    else
                                    {
                                        equipmentName = ((WEquipmentEntity)svs.getSelectedEquipmentData()).name;
                                        svsName = ((WSVSEntity)svs.getSelectedSvsData()).name;
                                        svsSerialNo = ((WSVSEntity)svs.getSelectedSvsData()).serial_no;
                                    }

                                    String strmsg = String.format("%s (%s)\n%s\n%s", equipmentName, svsName, svsSerialNo, "Searching for SVS devices.");
                                    progressDialog.setMessage(strmsg);
                                    progressDialog.show();


                                    //이름을 찾아서 접속하기
                                    ConnectSVSItem connectSVSItem = connectSVSItems.getCurrentItem();
                                    findSvsAddressAndConnect(connectSVSItem.serialNo);
                                }

                                if(connectSVSItems.size() == 0)
                                {
                                    ToastUtil.showShort(R.string.notregistered,"6");
                                }
                            }
                        }
                    }
                }


                @Override
                public void onBtnCancelClicked()
                {
                    wsvsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
                    wsvsAdapter.notifyDataSetChanged();
                }
            });

            customListButtonDialogWeb.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    wsvsAdapter.setSelectedSvsUuid(svs.getSelectedSvsUuid());
                    wsvsAdapter.notifyDataSetChanged();
                }
            });

            customListButtonDialogWeb.show();
        }
    };

    private ItemClickUtil.OnItemLongClickListener onItemLongClickListener = new ItemClickUtil.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {


            return false;
        }
    };


    private void findSvsAddressAndConnect(String serial_no)
    {
        //블루투스 매니저
        ddBluetoothManager = new DDBluetoothManager(this);
        ddBluetoothManager.setFoundName(serial_no);
        ddBluetoothManager.setOnDDBluetoothListener(new OnDDBluetoothListener() {
            @Override
            public void start() {
                ToastUtil.showLong("We are looking for a device that corresponds to the serial number.");
            }

            @Override
            public void refresh(List<DDBluetoothDevice> list) {

            }

            @Override
            public void found(DDBluetoothDevice ddBluetoothDevice) {

                //취소 버튼 없애기
                Button negativeButton = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if(negativeButton != null){
                    negativeButton.setVisibility(View.GONE);
                }

                //메세지
                ToastUtil.showShort("Found your device. I am trying to connect. Please wait.");

                //블루투스 연결 시도
                connectBluetoothAddress(ddBluetoothDevice.address);
            }

            @Override
            public void stop(int ret) {
                progressDialog.dismiss();
            }

            @Override
            public void timeOut() {
                ToastUtil.showShort("We haven't found it for a certain amount of time. please try again.");
                progressDialog.dismiss();
            }
        });


        //다이얼로그 세팅
        Button negativeButton = progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(negativeButton != null){
            negativeButton.setVisibility(View.VISIBLE);
        }else {
            progressDialog.setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    "취소",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ddBluetoothManager.stopScan();
                            dialog.dismiss();
                        }
                    }
            );
        }
        progressDialog.show();

        //블루투스 매니저 스캔 시작
        ddBluetoothManager.startScan();
    }

    private void connectBluetoothAddress(String address)
    {
        boolean connected = uartService.connect(address);
        if(!connected) {
            ToastUtil.showShort("Fail Connect.");
            progressDialog.dismiss();
        }
        connectSVSItems.nextIndex();
    }


    private void dialogBleOnOff()
    {
        DialogUtil.yesNo(this,
                getResources().getString(R.string.manualmode_screen),
                "SVS App requests to turn on Bluetooth.",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkbluetooth.enable();
                        ToastUtil.showShort(R.string.BleTurnON);

                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        svs.clear(); // 20180813


                        connectSVSItems = new ConnectSVSItems();
                        connectSVSItems.setAutoSaveMode(false);

                        SVSEntity svsEntity = new RealmDao<SVSEntity>(SVSEntity.class).loadByUuid(tempSvsUuid);
                        if(svsEntity != null)
                        {
                            ConnectSVSItem connectSVSpair = new ConnectSVSItem();
                            connectSVSpair.setAddress(svsEntity.getAddress());
                            connectSVSpair.setSvsLocation(svsEntity.getSvsLocation());
                            connectSVSItems.add(connectSVSpair);

                            svs.setLinkedSvsUuid(svsEntity.getUuid());

                            if(connectSVSItems.getIndex_connecting() < connectSVSItems.size())
                            {
                                String equipmentName = "";
                                String svsName = "";

                                if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                {
                                    equipmentName = ((EquipmentEntity)svs.getSelectedEquipmentData()).getName();
                                    svsName = ((SVSEntity)svs.getSelectedSvsData()).getSvsLocation().toString();
                                    svsName = svsName.toUpperCase();
                                }
                                else
                                {
                                    equipmentName = ((WEquipmentEntity)svs.getSelectedEquipmentData()).name;
                                    svsName = ((WSVSEntity)svs.getSelectedSvsData()).name;
                                }


                                String strmsg = String.format("%s (%s) \r\n\r\n%s", equipmentName, svsName, getResources().getString(R.string.requestconnect));
                                progressDialog.setMessage(strmsg);


                                //다음 접속하게 하기
                                if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
                                {
                                    progressDialog.show();

                                    //주소로 바로 접속하기
                                    connectBluetoothAddress(connectSVSItems.getCurrentIndexAddress());
                                }
                                else
                                {
                                    //이름을 찾아서 접속하기
                                    ConnectSVSItem connectSVSItem = connectSVSItems.getCurrentItem();
                                    findSvsAddressAndConnect(connectSVSItem.serialNo);
                                }
                            }
                        }
                        else {
                            ToastUtil.showShort(R.string.notSVSLocationName);
                        }

                        if(connectSVSItems.size() == 0) {
                            ToastUtil.showShort(R.string.notregistered,"7");
                        }
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkbluetooth.disable();
                        ToastUtil.showShort(R.string.BleTurnOFF);
                        dialog.cancel();
                    }
                }
        );
    }


    private void dialogBleClose(){

        if(svs.getScreenMode() == DefConstant.SCREEN_MODE.LOCAL)
        {
            //Equipment 정보 알아내기
            EquipmentEntity equipmentEntity = (EquipmentEntity)svs.getLinkedEquipmentData();
            if(equipmentEntity == null)
            {
                ToastUtil.showShort("Wrong Equipment Data.");
                return;
            }

            //SVS 정보 알아내기
            SVSEntity svsEntity = (SVSEntity)svs.getLinkedSvsData();
            if(svsEntity == null)
            {
                ToastUtil.showShort("Wrong Svs Data.");
                return;
            }

            //이름
            String equipmentName = equipmentEntity.getName();
            String svsName = svsEntity.getSvsLocation().toString();

            DialogUtil.yesNo(this,
                    equipmentName + "(" + svsName + ")",
                    "Do you want to disconnect?",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            button_ble_search.setVisibility(View.INVISIBLE);
                            uartService.disconnect();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }
            );
        }
        else if(svs.getScreenMode() == DefConstant.SCREEN_MODE.WEB)
        {
            //Equipment 정보 알아내기
            WEquipmentEntity wEquipmentEntity = (WEquipmentEntity)svs.getLinkedEquipmentData();
            if(wEquipmentEntity == null)
            {
                ToastUtil.showShort("Wrong Equipment Data.");
                return;
            }

            //SVS 정보 알아내기
            WSVSEntity wsvsEntity = (WSVSEntity)svs.getLinkedSvsData();
            if(wsvsEntity == null)
            {
                ToastUtil.showShort("Wrong Svs Data.");
                return;
            }

            //이름
            String equipmentName = wEquipmentEntity.name;
            String svsName = wsvsEntity.name;

            //연결 해제 팝업
            DialogUtil.yesNo(this,
                    equipmentName + "(" + svsName + ")",
                    "Do you want to disconnect?",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            button_ble_search.setVisibility(View.INVISIBLE);
                            uartService.disconnect();
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }
            );
        }


    }

    private void doUploadSensorInfoAndAutoNextActivity(final AutoNextActivity autoNextActivity)
    {
        final ProgressDialogUtil progressDialogUtil = new ProgressDialogUtil(this, "Uploading sensor information.","");
        //progressDialogUtil.show();

        //화면 이동전에 접속이 되어서 구성정보를 보냄.
        TCPSendUtil.sendConfig(new OnTCPSendCallback() {
            @Override
            public void onSuccess(String tag, Object obj) {

                //자동으로 구성정보 업로드 되었으니 알려줄 필요없음.
                //progressDialogUtil.hide();

                //화면 이동
                goActivity(autoNextActivity);
            }

            @Override
            public void onFailed(String tag, String msg) {

                //progressDialogUtil.hide();

                //자동으로 구성정보 업로드가 실패했으니, 다시 업로드 할건지 알려줘야함.
                DialogUtil.yesNo(SVSLocationManualModeActivity.this,
                        msg,
                        "Failed to auto-upload.\n" +
                                "At least once, you need to upload sensor information.\n" +
                                "If there is no sensor information, The measurement data cannot be checked in the web manager.\n" +
                                "Do you want to try again?\n" +
                                "If you click the \"Cancel\" button, you can upload it manually later.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doUploadSensorInfoAndAutoNextActivity(autoNextActivity);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goActivity(autoNextActivity);
                            }
                        });

            }
        });
    }

    private void goActivity(AutoNextActivity autoNextActivity)
    {
        //화면 이동
        goIntent(autoNextActivity.nextActivity);
        autoNextActivity.enabled = false;
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
        WEquipmentEntity wEquipmentEntity = (WEquipmentEntity)svs.getSelectedEquipmentData();
        final String company_id = wEquipmentEntity.company_id;
        final String equipment_id = wEquipmentEntity.id;

        //Svs List API 호출
        APIManager.getInstance().getSvsList(1, company_id, equipment_id, new OnApiListener() {
            @Override
            public void success(APIResponse apiResponse) {

                SvsGetListResponse svsGetListResponse = (SvsGetListResponse)apiResponse;

                final List<SvsGetListResponse.Datum> list = svsGetListResponse.list;
                if(list.size() == 0)
                {
                    ToastUtil.showShort("No data. Please add it in the web manager.");

                    //이전 데이터 삭제
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            RealmResults<WSVSEntity> wsvsEntities = new RealmDao<WSVSEntity>(WSVSEntity.class).loadAllByFilter("equipment_id", equipment_id);
                            wsvsEntities.deleteAllFromRealm();
                        }
                    });

                }
                else
                {
                    //이전 데이터 삭제
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            RealmResults<WSVSEntity> wsvsEntities = new RealmDao<WSVSEntity>(WSVSEntity.class).loadAllByFilter("equipment_id", equipment_id);
                            wsvsEntities.deleteAllFromRealm();
                        }
                    });

                    //신규 데이터 추가
                    DatabaseUtil.transaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            for(SvsGetListResponse.Datum datum : list)
                            {
                                WSVSEntity wsvsEntity = new WSVSEntity();
                                wsvsEntity.id = datum.id;
                                wsvsEntity.name = datum.name;
                                wsvsEntity.picture = datum.picture;
                                wsvsEntity.model = datum.model;
                                wsvsEntity.serial_no = datum.serial_no;
                                wsvsEntity.position = datum.position;
                                wsvsEntity.created = datum.created;

                                wsvsEntity.company_id = company_id;
                                wsvsEntity.equipment_id = equipment_id;

                                realm.copyToRealmOrUpdate(wsvsEntity);
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
}
