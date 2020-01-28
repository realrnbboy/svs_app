package kr.co.signallink.svsv2.commons;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nspil on 2018-01-29.
 */

public class DefLog {

    public static boolean isDebug = true;
    public static void d(String tag, String msg){
        if(isDebug){
            long now = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
            Date date = new Date(now);
            String slo = format.format(date) + "  :  " + msg;

            //Log.d(tag, slo);
            Log.i(tag, slo);
        }
    }
}
