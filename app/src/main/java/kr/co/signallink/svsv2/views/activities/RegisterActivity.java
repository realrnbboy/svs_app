package kr.co.signallink.svsv2.views.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefConvert;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.EquipmentEntity;
import kr.co.signallink.svsv2.databases.SVSEntity;
import kr.co.signallink.svsv2.sort.SortManager;
import kr.co.signallink.svsv2.utils.DateUtil;
import kr.co.signallink.svsv2.utils.DialogUtil;
import kr.co.signallink.svsv2.utils.FileUtil;
import kr.co.signallink.svsv2.commons.DefFile;
import kr.co.signallink.svsv2.commons.DefPhoto;
import kr.co.signallink.svsv2.commons.DefLog;
import kr.co.signallink.svsv2.user.RegisterSVSItem;
import kr.co.signallink.svsv2.utils.IntentUtil;
import kr.co.signallink.svsv2.utils.StringUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.utils.UriUtil;
import kr.co.signallink.svsv2.views.adapters.RegisterSVSValueItemsAdapter;
import kr.co.signallink.svsv2.user.SVS;
import kr.co.signallink.svsv2.views.custom.CustomListDialog;

import static kr.co.signallink.svsv2.utils.DateUtil.SIMPLE_FORMAT_DATE;
import static kr.co.signallink.svsv2.utils.DateUtil.SIMPLE_FORMAT_MILLISEC;
import static kr.co.signallink.svsv2.utils.DateUtil.SIMPLE_FORMAT_TIME;

/**
 * Created by nspil on 2018-02-19.
 */

public class RegisterActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "RegisterActivity";
    private SVS svs = SVS.getInstance();


    private final int PHOTO_COUNT = DefPhoto.PICKS.ALBUM.size();

    private Toolbar svsToolbar;
    private ImageView [] image_register = new ImageView[PHOTO_COUNT];
    private EditText name_register;
    private EditText location_register;
    private Spinner sort_spinner;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private RegisterSVSValueItemsAdapter registerSVSValueItemsAdapter;
    private Button btn_save_register;
    private Button button_record;

    private CustomListDialog customListDialog;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter checkbluetooth = null;

    private Uri fileUri;
    private String [] photoPath = new String[PHOTO_COUNT];

    private SortManager.SORT_TYPE selectSortType = SortManager.SORT_TYPE.NAME_ADDRESS;
    private ArrayAdapter<SortManager.SORT_TYPE> arrayAdapter;

    private List<RegisterSVSItem> registerSVSValueItems;
    private List<RegisterSVSItem> temp_registerSVSValueItems = new ArrayList<RegisterSVSItem>();
    private HashMap<DefFile.SVS_LOCATION, RegisterSVSItem> checkedHashMap = new HashMap<>();

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private boolean bScanning;


    private int lastSelectIdx = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        svsToolbar = (Toolbar) findViewById(R.id.toolbar);
        ((TextView)svsToolbar.findViewById(R.id.toolbar_title)).setText(R.string.register_screen);
        setSupportActionBar(svsToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        name_register = findViewById(R.id.name_register);
        location_register = findViewById(R.id.location_register);

        image_register[0] = findViewById(R.id.keyimage_register);
        image_register[1] = findViewById(R.id.subimage1_register);
        image_register[2] = findViewById(R.id.subimage2_register);
        image_register[3] = findViewById(R.id.subimage3_register);
        image_register[4] = findViewById(R.id.subimage4_register);
        for(int i = 0; i<PHOTO_COUNT; i++) {
            image_register[i].setOnClickListener(myPhotoClickListener);
        }

        btn_save_register = findViewById(R.id.btn_save_register);
        btn_save_register.setOnClickListener(myClickListener);


        checkbluetooth = BluetoothAdapter.getDefaultAdapter();
        if (checkbluetooth == null) {
            ToastUtil.showLong("Bluetooth is not available");
            finish();
            return;
        }

        final SortManager.SORT_TYPE[] sortTypes = SortManager.SORT_TYPE.values();
        arrayAdapter = new ArrayAdapter<SortManager.SORT_TYPE>(this, R.layout.spinner_item, sortTypes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sort_spinner = findViewById(R.id.spinner_how_to_sort);
        sort_spinner.setAdapter(arrayAdapter);
        sort_spinner.setSelection(selectSortType.ordinal());
        sort_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSortType = SortManager.SORT_TYPE.values()[position];
                refreshAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton ibtnSpinnerInfo = findViewById(R.id.ibtnSpinnerInfo);
        ibtnSpinnerInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showShort("received signal strength indicator (RSSI) is a measurement of the power present in a received bluetooth signal.");
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        registerSVSValueItems = new ArrayList<RegisterSVSItem>();
        registerSVSValueItemsAdapter = new RegisterSVSValueItemsAdapter(this, registerSVSValueItems);

        ListView newDevicesListView = findViewById(R.id.svs_register);
        newDevicesListView.setAdapter(registerSVSValueItemsAdapter);

        customListDialog = new CustomListDialog(this, R.array.custom_list_dialog_image);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.showShort(R.string.ble_not_supported);
            finish();
            return;
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtil.showShort(R.string.ble_not_supported);
            finish();
            return;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        DefLog.d(TAG, "onResume");

        button_record = findViewById(R.id.button_record);
        button_record.setVisibility(View.GONE);

        if (!checkbluetooth.isEnabled()) {
            DefLog.d(TAG, "onResume - BT not enabled yet");
            dialogBleOnOff();
        } else {
            scanBleDevice(true);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        scanBleDevice(false);
    }

    @Override
    public void onRefresh() {

        scanBleDevice(true);

        swipeRefreshLayout.setRefreshing(false);

    }

    private View.OnClickListener myClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            hideKeyboard();

            switch (v.getId())
            {
                case R.id.btn_save_register :
                    dialogSave();
                    break;
            }
        }
    };

    private View.OnClickListener myPhotoClickListener = new View.OnClickListener()
    {
        private int selected_idx = -1;

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.keyimage_register :
                    selected_idx = 0;
                    break;
                case R.id.subimage1_register :
                    selected_idx = 1;
                    break;
                case R.id.subimage2_register :
                    selected_idx = 2;
                    break;
                case R.id.subimage3_register :
                    selected_idx = 3;
                    break;
                case R.id.subimage4_register :
                    selected_idx = 4;
                    break;
            }

            if(selected_idx == -1)
            {
                ToastUtil.showShort("Err");
                return;
            }

            customListDialog.setDialogListener(new CustomListDialog.OnCustomListButtonDialogListener() {
                @Override
                public void onBtnClicked(int viewIdx) {

                    if(viewIdx == 1)
                    {
                        int requestCode = DefPhoto.PICKS.CAMERA.getPICK(selected_idx).getGlobalIndex();
                        fileUri = IntentUtil.captureCamera(RegisterActivity.this, requestCode);
                    }
                    else if(viewIdx == 2)
                    {
                        int requestCode = DefPhoto.PICKS.ALBUM.getPICK(selected_idx).getGlobalIndex();
                        IntentUtil.selectAlbum(RegisterActivity.this, requestCode);
                    }
                    else if(viewIdx == 3)
                    {
                        displayDeletePhoto(selected_idx);
                    }

                }

                @Override
                public void onBtnCancelClicked() {

                }
            });

            customListDialog.show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == UCrop.REQUEST_CROP)
        {
            final Uri resultUri = UCrop.getOutput(data);
            displayPhoto(lastSelectIdx, resultUri);
        }
        else if (requestCode == UCrop.RESULT_ERROR)
        {
            final Throwable cropError = UCrop.getError(data);

            ToastUtil.showShort("CropErr:"+cropError.toString());
        }
        else
        {
            int globalIndex = requestCode;
            DefPhoto.PICK pick = DefPhoto.findTarget(globalIndex);
            if(DefPhoto.PICKS.CAMERA.hasTarget(pick))
            {
                lastSelectIdx = pick.getLocalIndex();
                cropPhoto();
            }
            else if(DefPhoto.PICKS.ALBUM.hasTarget(pick))
            {
                fileUri = data.getData();

                lastSelectIdx = pick.getLocalIndex();
                cropPhoto();
            }
        }

    }

    private void cropPhoto() {

        Uri source = fileUri;
        Uri destination = UriUtil.addAbsolutePath(source, DateUtil.convertDate(new Date(), SIMPLE_FORMAT_DATE+SIMPLE_FORMAT_TIME+SIMPLE_FORMAT_MILLISEC)+"_croped");

        File file = new File(destination.getPath());
        if(file.exists())
        {
            file.delete();
        }

        //크롭 옵션
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorContent)); //스테이터스 색상
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorContent)); //툴바 배경 색상
        options.setToolbarWidgetColor(ContextCompat.getColor(this,R.color.color1)); //툴바 이름, 아이콘 색상
        options.setToolbarTitle("Photo Edit");

        //크롭
        UCrop uCrop = UCrop.of(source, destination);
        uCrop = uCrop.withAspectRatio(1, 1); //1:1 비율만 허용
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    private void displayPhoto(int index, Uri photoUri) {
        photoPath[index] = photoUri.getPath(); //FileUtil.getPath(this, photoUri);
        Glide.with(this).load(photoUri).fitCenter().into(image_register[index]);
    }

    private void displayDeletePhoto(int index) {
        photoPath[index] = null;
        Glide.with(this).load(R.drawable.btn_imgadd).fitCenter().into(image_register[index]);
    }

    private boolean checkBeforeSave() {

        //장비 이름 비어있음 확인
        String equipmentName = name_register.getText().toString().trim();
        if(equipmentName.isEmpty()){
            ToastUtil.showShort(R.string.notEquipmentName);
            return false;
        }

        //선택된 SVS 확인하기
        checkedHashMap.clear();
        for(RegisterSVSItem registerSVSValueItem : registerSVSValueItems)
        {
            if(registerSVSValueItem.isChecked())
            {
                DefFile.SVS_LOCATION svsLocation = registerSVSValueItem.getSvsLocation();

                boolean contain = checkedHashMap.containsKey(svsLocation);
                if(contain)
                {
                    ToastUtil.showLong(svsLocation.toString().toUpperCase()+" duplicated.");
                    return false;
                }
                else
                {
                    //포함 안 되어있는 SVS만 추가하기
                    checkedHashMap.put(svsLocation, registerSVSValueItem);
                }
            }
        }

        //체크된 갯수 확인
        if(checkedHashMap.size() == 0)
        {
            ToastUtil.showLong("No SVS checked.");
            return false;
        }


        //SVS 선택 확인
        ArrayList<String> svsLocNames = new ArrayList<>();
        ArrayList<RegisterSVSItem> checkedItems = new ArrayList<>();
        for(RegisterSVSItem registerSVSValueItem : registerSVSValueItems)
        {
            if(registerSVSValueItem.isChecked())
            {
                checkedItems.add(registerSVSValueItem);
                svsLocNames.add(registerSVSValueItem.getSvsLocation() + "");
            }
        }
        if(checkedItems.size() == 0)
        {
            ToastUtil.showLong("No SVS checked.");
            return false;
        }

        //중복된 SVS 확인
        if(DefConvert.hasDuplicate(svsLocNames))
        {
            ToastUtil.showLong("SVS number duplicated.");
            return false;
        }

        //사진은 있는데 SVS가 선택이 안된경우
        for(int i=1; i<PHOTO_COUNT; i++)
        {
            String path = photoPath[i];
            if(path != null)
            {
                //사진 Index에 맞는 DIR찾기
                DefFile.SVS_LOCATION svsLocation = DefFile.SVS_LOCATION.findDIR_forPHOTO(i-1);

                //사진 주소에 맞는 svs찾기
                boolean matchingPathToSVS = checkedHashMap.containsKey(svsLocation);

                //사진에 맞는 svs가 없을 경우 경고 출력
                if(!matchingPathToSVS)
                {
                    ToastUtil.showLong("Please set the SVS for the "+i+"th Photos");
                    return false;
                }
            }
        }

        return true;
    }

    private void save() {

        if(checkBeforeSave())
        {
            //썸네일 전용 폴더 생성
            String strThumbnailDir = DefFile.FOLDER.THUMBNAIL.getFullPath();
            File fDir = new File(strThumbnailDir);
            if(!fDir.exists()){
                boolean ret = fDir.mkdir();
                if(!ret){
                    ToastUtil.showShort("Unable to create image folder.");
                    return;
                }
            }


            final String strDate = DateUtil.currentDefaultDetailDate();

            //Equipment 객체 만들기
            final EquipmentEntity equipmentEntity = new EquipmentEntity();
            equipmentEntity.setName(name_register.getText().toString().trim());
            equipmentEntity.setLocation(location_register.getText().toString().trim());
            if(photoPath[0] != null){
                equipmentEntity.setImageUri(strThumbnailDir + "." + equipmentEntity.getUuid() + "_" + strDate + DefFile.EXT.JPG);
            }

            //Equipment에 SVS 추가하기
            for(RegisterSVSItem device : registerSVSValueItems)
            {
                if(device.isChecked())
                {
                    SVSEntity svsEntity = new SVSEntity();
                    svsEntity.setName(device.getName());
                    svsEntity.setAddress(device.getAddress());
                    svsEntity.setSvsLocation(device.getSvsLocation());
                    svsEntity.setParentUuid(equipmentEntity.getUuid());

                    //사진이 추가되었는지 확인
                    int photoIdx = device.getSvsLocation().getIndex()+1;
                    if(photoPath[photoIdx] != null){
                        svsEntity.setImageUri(strThumbnailDir + "." + svsEntity.getUuid() + "_" + strDate + DefFile.EXT.JPG);
                    }

                    equipmentEntity.getSvsEntities().add(svsEntity);
                }
            }

            //기록
            DatabaseUtil.transaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(equipmentEntity);
                }
            });

            //선택한 사진을 썸네일 폴더 안에 복사
            String strSrcPath;
            String strDstPath;
            {
                int photoIdx;

                //1)메인 썸네일
                photoIdx = 0;
                strSrcPath = photoPath[photoIdx];
                strDstPath = equipmentEntity.getImageUri();
                if(strSrcPath != null){
                    FileUtil.copyFile(strSrcPath, strDstPath);
                }

                //2)SVS 썸네일
                RealmList<SVSEntity> svsEntities = equipmentEntity.getSvsEntities();
                for(SVSEntity svsEntity : svsEntities)
                {
                    photoIdx = svsEntity.getSvsLocation().getIndex()+1;
                    strSrcPath = photoPath[photoIdx];
                    strDstPath = svsEntity.getImageUri();
                    if(strSrcPath != null){
                        FileUtil.copyFile(strSrcPath, strDstPath);
                    }
                }
            }

            //토스트
            ToastUtil.showShort("Success Register Equipment");

            //화면
            finish();
        }
    }

    private void scanBleDevice(final boolean enable) {
        if (enable)
        {
            if(!bScanning)
            {
                temp_registerSVSValueItems.clear();

                for(RegisterSVSItem registerSVSValueItem : registerSVSValueItems) {
                    if(registerSVSValueItem.isChecked())
                        temp_registerSVSValueItems.add(registerSVSValueItem);
                }

                registerSVSValueItems.clear();

                for(RegisterSVSItem registerSVSValueItem : temp_registerSVSValueItems) {
                    if(registerSVSValueItem.isChecked())
                        registerSVSValueItems.add(registerSVSValueItem);
                }
                refreshAdapter();

                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bScanning = false;
                        if(mBluetoothAdapter.getBluetoothLeScanner() != null)
                            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                    }
                }, SCAN_PERIOD);


                bScanning = true;
                if(mBluetoothAdapter.getBluetoothLeScanner() != null)
                    mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
            }
        } else {
            bScanning = false;
            if(mBluetoothAdapter.getBluetoothLeScanner() != null)
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        }

    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "errorCode " + errorCode);
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        BluetoothDevice device = result.getDevice();
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        int deviceRssi = result.getRssi();

                        RegisterSVSItem svsDevice = new RegisterSVSItem();
                        svsDevice.setName(deviceName);
                        svsDevice.setAddress(deviceAddress);
                        svsDevice.setRssi(deviceRssi);

                        //Nordic 글자가 들어간 모델만 리스트에 추가
                        if(deviceName != null
                                && (deviceName.toLowerCase().contains("nordic") || deviceName.toLowerCase().contains("svs"))
                        ){
                            addDevice(svsDevice);
                        }
                    }
                    catch(Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    };

    private void addDevice(RegisterSVSItem device) {

        //기존에 어드레스 정보가 일치하는 기기가 있는지 찾기
        boolean alreadyAdded = false;
        for (RegisterSVSItem registerSVSItem : registerSVSValueItems)
        {
            if (registerSVSItem.getAddress().equals(device.getAddress()))
            {
                alreadyAdded = true;

                boolean needNotify = registerSVSItem.getRssi() != device.getRssi();

                //rssi 업데이트
                registerSVSItem.setRssi(device.getRssi());

                if(needNotify){
                    refreshAdapter();
                }
                break;
            }
        }

        //추가
        if (!alreadyAdded)
        {
            registerSVSValueItems.add(device);
            refreshAdapter();
        }
    }


    private void refreshAdapter(){

        //접속중인 블루투스 어드레스
        String linkedAddress = svs.getSvsDeviceAddress();

        //접속중인 블루투스 기기 찾기
        for(RegisterSVSItem registerSVSItem : registerSVSValueItems)
        {
            if(StringUtil.equalNotEmpty(registerSVSItem.getAddress(), linkedAddress))
            {
                registerSVSItem.setLinked(true);
            }
            else
            {
                registerSVSItem.setLinked(false);
            }
        }

        //정렬 방식 적용
        Collections.sort(registerSVSValueItems, selectSortType.getComparator());

        //갱신
        registerSVSValueItemsAdapter.notifyDataSetChanged();
    }

    private void dialogSave(){

        DialogUtil.yesNo(this, getResources().getString(R.string.register_screen), "Do you want to save?",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        save();
                    }
                }, null);
    }

    private void dialogBleOnOff(){

        DialogUtil.yesNo(this, getResources().getString(R.string.register_screen), "SVS App requests to turn on Bluetooth.",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mBluetoothAdapter.enable();
                        ToastUtil.showShort(R.string.BleTurnON);
                        scanBleDevice(true);
                    }
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mBluetoothAdapter.disable();
                        ToastUtil.showShort(R.string.BleTurnOFF);
                    }
                });
    }


}
