package kr.co.signallink.svsv2.services.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.utils.ToastUtil;

public class DDBluetoothManager extends ScanCallback {

    private BluetoothAdapter mBluetoothAdapter;
    private OnDDBluetoothListener mOnDDBluetoothListener = null;
    private List<DDBluetoothDevice> mBluetoothDevices = new ArrayList<>();
    private boolean mScanning = false;
    private boolean mFound = false;
    private String mFoundName = null;

    private Handler mTimeOutHandler = null;
    private int mTimeLimit = 5*1000; //sec

    public DDBluetoothManager(Context context)
    {
        BluetoothManager bluetoothManager = (android.bluetooth.BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtil.showShort(R.string.ble_not_supported);
            return;
        }

    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        processScanResult(result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult result : results) {
            processScanResult(result);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        if(mScanning){
            if(mOnDDBluetoothListener != null)
            {
                mOnDDBluetoothListener.stop(errorCode);
            }
        }
    }

    public void setOnDDBluetoothListener(OnDDBluetoothListener onDDBluetoothListener)
    {
        this.mOnDDBluetoothListener = onDDBluetoothListener;
    }

    public void setFoundName(String foundName){
        this.mFoundName = foundName;
    }


    public void startScan()
    {
        synchronized (this)
        {
            if(mScanning){
                return;
            }

            mScanning = true;

            mBluetoothAdapter.getBluetoothLeScanner().startScan(this);

            if(mOnDDBluetoothListener!=null)
            {
                mOnDDBluetoothListener.start();
            }

            //타임아웃 시작
            mTimeOutHandler = new Handler();
            mTimeOutHandler.postDelayed(timeOutRunnable, mTimeLimit);
        }
    }

    public void stopScan()
    {
        synchronized (this)
        {
            mScanning = false;

            //타임아웃 강제종료
            if(mTimeOutHandler != null){
                mTimeOutHandler.removeCallbacks(timeOutRunnable);
                mTimeOutHandler = null;
            }

            mBluetoothAdapter.getBluetoothLeScanner().stopScan(this);

            if(mOnDDBluetoothListener!=null)
            {
                mOnDDBluetoothListener.stop(0);
            }
        }

    }

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {

            synchronized (this)
            {
                if(mScanning)
                {
                    mScanning = false;

                    //타임아웃 객체 초기화
                    mTimeOutHandler = null;

                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(DDBluetoothManager.this);

                    //발견하지 못했다면, 타임아웃 처리.
                    if(!mFound)
                    {
                        if(mOnDDBluetoothListener!=null)
                        {
                            mOnDDBluetoothListener.timeOut();
                        }
                    }

                }
            }
        }
    };


    private void processScanResult(final ScanResult result)
    {
        //스캔중이 아닐때는 무시하는 로직.
        if(!mScanning){
            return;
        }

        BluetoothDevice btDevice = result.getDevice();
        String deviceName = btDevice.getName();
        String deviceAddress = btDevice.getAddress();
        int deviceRssi = result.getRssi();

        DDBluetoothDevice ddBluetoothDevice = new DDBluetoothDevice();
        ddBluetoothDevice.address = deviceAddress;
        ddBluetoothDevice.name = deviceName;
        ddBluetoothDevice.rssi = deviceRssi;

        //Nordic 글자가 들어간 모델만 리스트에 추가
        if(deviceName != null
                && (deviceName.toLowerCase().contains("nordic") || deviceName.toLowerCase().contains("svs"))
        ){
            addDevice(ddBluetoothDevice);
        }
    }

    private void addDevice(DDBluetoothDevice btDevice) {

        //기존에 어드레스 정보가 일치하는 기기가 있는지 찾기
        boolean alreadyAdded = false;
        for (DDBluetoothDevice addedBtDevice : mBluetoothDevices)
        {
            if (addedBtDevice.address.equals(btDevice.address))
            {
                alreadyAdded = true;

                boolean needNotify = addedBtDevice.rssi != btDevice.rssi;

                //rssi 업데이트
                addedBtDevice.rssi = btDevice.rssi;

                if(needNotify)
                {
                    refreshNotify();
                }
                break;
            }
        }

        //추가
        if (!alreadyAdded)
        {
            //이름 찾기
            if(mFoundName != null)
            {
                if(mFoundName.toLowerCase().equals(btDevice.name.toLowerCase()))
                {
                    mFound = true;
                    if(mOnDDBluetoothListener!=null && mScanning)
                    {
                        mOnDDBluetoothListener.found(btDevice);
                    }
                }
            }

            //리스트에 추가하기
            mBluetoothDevices.add(btDevice);
            refreshNotify();
        }
    }

    private void refreshNotify()
    {
        if(mOnDDBluetoothListener!=null && mScanning)
        {
            mOnDDBluetoothListener.refresh(mBluetoothDevices);
        }
    }
}
