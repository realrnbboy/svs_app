package kr.co.signallink.svsv2.utils;

import android.Manifest;
import android.content.Context;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import io.realm.Realm;
import kr.co.signallink.svsv2.databases.DatabaseUtil;
import kr.co.signallink.svsv2.databases.MyDatabase;
import kr.co.signallink.svsv2.services.MyApplication;

public class PermissionUtil {

    private static final String[] permissions = {
            Manifest.permission.CAMERA, //카메라
            Manifest.permission.READ_EXTERNAL_STORAGE, //외장 저장소 읽기
            Manifest.permission.WRITE_EXTERNAL_STORAGE, //외장 저장소 쓰기
            Manifest.permission.ACCESS_FINE_LOCATION, //블루투스 스캔1
            Manifest.permission.ACCESS_COARSE_LOCATION //블루투스 스캔2
    };

    public interface OnPermissionListener {
        void granted();
    }

    public static void checkAll(final Context context, final OnPermissionListener onPermissionListener){

        TedPermission.with(context)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        //수락


                        //DB초기화
                        MyDatabase.getInstance(MyApplication.getInstance().getAppContext());

                        onPermissionListener.granted();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        //거절

                        //수락할때 까지 무한 루프
                        checkAll(context, onPermissionListener);
                    }
                })
                .setRationaleMessage("Permissions are required to use the app."
                        +"\n\n"+"Camera permissions are used in device information"
                        +"\n"+"File Write permissions are used for device information management and data recording."
                        +"\n"+"The Location permission is used to search for Bluetooth.") //권한을 허용해야되는 이유
                .setDeniedMessage("Your app can not run because your permission has been denied. Permissions are required to use the app.\n\nPlease change it to Allow in [Settings]> [Permissions].") //사용자가 거부 했을 때 알려주는 메세지
                .setPermissions(permissions)
                .setRationaleConfirmText("Confirm")
                .setDeniedCloseButtonText("Close")
                .setGotoSettingButtonText("Setting")
                .check();
    }




}
