package kr.co.signallink.svsv2.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import kr.co.signallink.svsv2.commons.DefCMDOffset;
import kr.co.signallink.svsv2.model.Constants;
import kr.co.signallink.svsv2.services.MyApplication;

import static java.lang.Math.log10;

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
    public static String createCsv(String type, String [] title, float [] xData, float [] data1, float [] data2, float [] data3, float [] data4, float [] data5) {
        if( !(data1.length == data2.length || data2.length == data3.length) ) {    // 데이터 사이즈가 맞지 않은 경우
            return null;
        }

        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "SVSdata" + File.separator + "csv" + File.separator + type + File.separator;

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

                fw.write(String.valueOf(xData[i]));
                fw.write(",");
                fw.write(String.valueOf(data1[i]));
                fw.write(",");
                fw.write(String.valueOf(data2[i]));
                fw.write(",");
                fw.write(String.valueOf(data3[i]));
                if( data4 != null ) {
                    fw.write(",");
                    fw.write(String.valueOf(data4[i]));
                }
                if( data5 != null ) {
                    fw.write(",");
                    fw.write(String.valueOf(data5[i]));
                }

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

    static public float[] getConcernDataList() {
        //int MAX_X_VALUE = Constants.MAX_PIPE_X_VALUE;
        int MAX_X_VALUE = DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
        float[] list = new float[MAX_X_VALUE];

        for( int i = 0; i<MAX_X_VALUE; i++ ) {
            //list[i] = (float) (10 * ((Math.log(i+1) + 0.48017) / 2.127612));
            //list[i] = (float) (10 * (((i+1) + 0.48017) / 2.127612));
            //list[i] = (float) Math.pow(10, ((Math.log10(i+1) + 0.48017) / 2.127612));
            list[i] = (float) Math.pow(10, ((Math.log10(i*(400/(float)1024)+1) + 0.48017) / 2.127612));
        }

        return list;
    }

    static public float[] getProblemDataList() {
        //int MAX_X_VALUE = Constants.MAX_PIPE_X_VALUE;
        int MAX_X_VALUE = DefCMDOffset.MEASURE_AXIS_FREQ_ELE_MAX;
        float[] list = new float[MAX_X_VALUE];

        for( int i = 0; i<MAX_X_VALUE; i++ ) {
            //list[i] = (float) (10 * ((Math.log(i+1) + 1.871083) / 2.084547));
            //list[i] = (float) (10 * (((i+1) + 1.871083) / 2.084547));
            //list[i] = (float) Math.pow(10, ((Math.log10(i+1) + 1.871083) / 2.084547));
            list[i] = (float) Math.pow(10, ((Math.log10(i*(400/(float)1024)+1) + 1.871083) / 2.084547));
        }

        return list;
    }

    // 리스트뷰 크기 조절
    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setSharedPreferencesStringArray(Context context, String store, String key, ArrayList<String> data) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(store, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray a = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            a.put(data.get(i));
        }
        if (!data.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
    }


    public static ArrayList<String> getSharedPreferencesStringArray(Context context, String store, String key) {

        SharedPreferences prefs = context.getSharedPreferences(store, 0);
        String json = prefs.getString(key, null);
        ArrayList<String> ret = new ArrayList<String>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    ret.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static void setSharedPreferencesString(Context context, String store, String key, String data) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(store, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();

        edit.putString(key, data);
        edit.commit();
    }


    public static String getSharedPreferencesString(Context context, String store, String key, String defaultValue) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(store, 0);

        String ret = sharedPreferences.getString(key, defaultValue);
        return ret;
    }


    public static void copyFile(InputStream in, OutputStream out) throws Exception
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    public static double getMaxDouble(double array[]) {
        List<Double> list = new ArrayList<Double>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return Collections.max(list);
    }
}