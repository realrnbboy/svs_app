package kr.co.signallink.svsv2.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import kr.co.signallink.svsv2.services.MyApplication;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ClipboardUtil {

    static MyApplication myApplication = MyApplication.getInstance();

    public static void copy(String str){

        ClipboardManager clipboard = (ClipboardManager)myApplication.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(myApplication.getPackageName(), str);
        clipboard.setPrimaryClip(clip);
    }

    public static String paste(){
        MyApplication myApplication = MyApplication.getInstance();

        ClipboardManager clipboard = (ClipboardManager)myApplication.getSystemService(Context.CLIPBOARD_SERVICE);
        String pasteData = "";

        // 클립보드에 데이터가 없거나 텍스트 타입이 아닌 경우
        if (!(clipboard.hasPrimaryClip())) {
            ;
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
            ;
        }
        else {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText().toString();
        }

        return pasteData;
    }

}
