package kr.co.signallink.svsv2.utils;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

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

    public static void setFloatArrayPref(String key, float[] values) {
        if( values == null )
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            str.append(values[i]).append(",");
        }

        editor.putString(key, str.toString());
        editor.apply();
    }

    public static float[] getFloatArrayPref(String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());
        String savedString = prefs.getString(key, null);
        if( savedString == null )
            return null;

        StringTokenizer st = new StringTokenizer(savedString, ",");
        ArrayList<Float> resultArray = new ArrayList<>();
        while (st.hasMoreTokens()) {
            resultArray.add(Float.parseFloat(st.nextToken()));
        }

        float[] result = new float[resultArray.size()];
        for (int i = 0; i<resultArray.size(); i++ ) {
            result[i] = resultArray.get(i);
        }

        return result;
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

    // csv 파일 저장
    public static String createCsv(String [] title, float [] data1, float [] data2, float [] data3) {
        if( !(data1.length == data2.length || data2.length == data3.length) ) {    // 데이터 사이즈가 맞지 않은 경우
            return null;
        }

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "SVSdata" + File.separator + "csv" + File.separator;

        File dir = new File(path);
        if( !dir.exists() ) {   // 폴더 없으면 생성
            dir.mkdirs();
        }

        // 현재 시간 가져오기
        String fileName = getCurrentTime("yyyyMMddHHmmss") + ".csv";

        try{
            BufferedWriter fw = new BufferedWriter(new FileWriter(path + fileName, false));

            // 제목 추가
            if( title != null ) {
                for( int i = 0; i<title.length; i++ ) {
                    fw.write(title[i]);

                    if( (i + 1) != title.length ) { // 마지막은 , 붙이지 않음
                        fw.write(",");
                    }
                }

                fw.newLine();
            }

            // 실 데이터 추가
            for( int i = 0; i<data1.length; i++ ) {

                fw.write(String.valueOf(data1[i]));
                fw.write(",");
                fw.write(String.valueOf(data2[i]));
                fw.write(",");
                fw.write(String.valueOf(data3[i]));

                fw.newLine();
            }

            fw.flush();
            fw.close();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return fileName;
    }
}