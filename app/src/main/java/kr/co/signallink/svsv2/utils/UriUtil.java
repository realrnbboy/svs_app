package kr.co.signallink.svsv2.utils;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

import kr.co.signallink.svsv2.services.MyApplication;

public class UriUtil {

    //절대경로 -> uri
    public static Uri getUriFromPath(String filePath) {
        Cursor cursor = MyApplication.getInstance().getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }

    //uri -> 절대경로
    public static String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = MyApplication.getInstance().getAppContext().getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

        cursor.close();
        return path;
    }


    //uri -> 절대경로 + path -> uri
    public static Uri addAbsolutePath(Uri contentUri, String addFileName){
        String realPath = getRealPathFromURI(contentUri);

        String folderPath = FileUtil.getPathToFolderPath(realPath);
        String fileName = FileUtil.getPathToFileName(realPath);
        String extension = FileUtil.getPathExtension(realPath);

        String addedFilePath = folderPath + "."+fileName+addFileName + extension;

        return Uri.fromFile(new File(addedFilePath));
    }
}
