package kr.co.signallink.svsv2.utils;

import android.widget.Toast;
import kr.co.signallink.svsv2.services.MyApplication;

public class ToastUtil {

    public static void showShort(int stringId)
    {
        show(MyApplication.getInstance().getAppContext().getResources().getString(stringId), Toast.LENGTH_SHORT);
    }

    public static void showShort(int stringId, String addMsg)
    {
        show(MyApplication.getInstance().getAppContext().getResources().getString(stringId)+addMsg, Toast.LENGTH_SHORT);
    }

    public static void showShort(String msg)
    {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void showLong(int stringId)
    {
        show(MyApplication.getInstance().getAppContext().getResources().getString(stringId), Toast.LENGTH_LONG);
    }

    public static void showLong(String msg)
    {
        show(msg, Toast.LENGTH_LONG);
    }

    public static void show(final String msg, final int time)
    {
        ThreadUtil.runOnMainLooper(new ThreadUtil.OnBlock() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getInstance().getAppContext(), msg, time).show();
            }
        });

    }
}
