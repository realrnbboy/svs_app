package kr.co.signallink.svsv2.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.signallink.svsv2.R;
import kr.co.signallink.svsv2.commons.DefFile;

public class IntentUtil {

    //화면 이동하기
    public static void goActivity(Activity activity, Class<?> cls){
        Intent i = new Intent(activity, cls);
        activity.startActivity(i);
    }

    //화면 이동하기 + Serializable
    public static void goActivity(Activity activity, Class<?> cls, String key, Serializable serializable){
        Intent i = new Intent(activity, cls);
        i.putExtra(key, serializable);
        activity.startActivity(i);
    }


    //홈페이지 이동하기
    public static void goHomepage(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(activity.getResources().getString(R.string.homepage)));
        activity.startActivity(intent);
    }

    //앨범 이미지 선택하기
    public static void selectAlbum(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        activity.startActivityForResult(intent, requestCode);
    }

    //카메라 촬영하기
    public static Uri captureCamera(Activity activity, int requestCode) {
        Uri fileUri = null;

        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if(takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {

                String timeStamp = DateUtil.convertDate(new Date(),"yyyyMMdd_HHmmss");
                String imageFileName = timeStamp + DefFile.EXT.JPG;
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, imageFileName);
                fileUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                activity.startActivityForResult(intent, requestCode);
            }
        }

        return fileUri;
    }
}
