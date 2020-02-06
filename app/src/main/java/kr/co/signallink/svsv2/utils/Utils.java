package kr.co.signallink.svsv2.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.signallink.svsv2.services.MyApplication;

public class Utils {
    public static float floatFloor(float arg) {
        float value = Float.parseFloat(String.format("%.2f", arg));
        return value;
    }
    public static double doubleFloor(double arg) {
        double value = Double.parseDouble(String.format("%.2lf", arg));
        return value;
    }

    public static String getCurrentTime(String dateFormat) {
        return getCurrentTime(dateFormat, null);
    }

    public static String addDateDay(String sourceDate, int addCount, String dateFormat) {
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat(dateFormat);
        Date date = null;
        try {
            date = df.parse(sourceDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cal.setTime(date);
        System.out.println("current: " + df.format(cal.getTime()));

        cal.add(Calendar.DATE, addCount);

        return df.format(cal.getTime());
    }

    public static String getCurrentTime(String dateFormat, Locale locale) {
        // 시스템으로부터 현재시간(ms) 가져오기

        long now = System.currentTimeMillis();
        // Data 객체에 시간을 저장한다.
        Date date = new Date(now);
        // 각자 사용할 포맷을 정하고 문자열로 만든다.

        if( locale == null ) {
            SimpleDateFormat sdfNow = new SimpleDateFormat(dateFormat);
            String strNow = sdfNow.format(date);
            return strNow;
        }
        else {
            SimpleDateFormat sdfNow = new SimpleDateFormat(dateFormat, locale);
            String strNow = sdfNow.format(date);
            return strNow;
        }
    }

    public static void setStringArrayPref(String key, String[][] values) {
        if( values == null )
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < values.length; i++) {

            JSONArray b = new JSONArray();
            for( int j = 0; j<values[i].length; j++ ) {
                String t = values[i][j];
                b.put(t);
            }

            String bT = b.toString();
            a.put(bT);
        }

        editor.putString(key, a.toString());
        editor.apply();
    }

    public static String[][] getStringArrayPref(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());
        String json = prefs.getString(key, null);
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);

                String[][] returnString = new String[a.length()][];
                for (int i = 0; i < a.length(); i++) {

                    JSONArray b = new JSONArray(a.getString(i));
                    returnString[i] = new String[b.length()];

                    for( int j = 0; j<b.length(); j++ ) {
                        returnString[i][j] = b.optString(j);
                    }
                }

                return returnString;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static float[] byteToFloat(byte[] bytes) {
        if( bytes == null )    // 4byte씩 잘라서 사용
            return null;

        float [] floats = new float[bytes.length];

        int index = 0;
        for( int i = 0; i<bytes.length; i = i + 4 ) {
            if( i >= bytes.length || i+1 >= bytes.length || i+2 >= bytes.length || i+3 >= bytes.length)
                break;
            byte[] tByte = {bytes[i], bytes[i+1], bytes[i+2], bytes[i+3]};
            floats[index++] = ByteBuffer.wrap(tByte).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        }

        return floats;
    }
}