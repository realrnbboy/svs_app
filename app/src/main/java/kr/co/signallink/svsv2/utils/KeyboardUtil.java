package kr.co.signallink.svsv2.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import kr.co.signallink.svsv2.services.MyApplication;

public class KeyboardUtil {

    public static void showKeyboard(){
        Context context = MyApplication.getInstance().getAppContext();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void closeKeyboard(){
        Context context = MyApplication.getInstance().getAppContext();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

}
