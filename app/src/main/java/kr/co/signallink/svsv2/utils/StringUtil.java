package kr.co.signallink.svsv2.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.UUID;

import kr.co.signallink.svsv2.services.MyApplication;

public class StringUtil {

    static String packageName;

    //패키지 이름 가져오기
    public static String getPackageName(){
        if(packageName == null){
            packageName = MyApplication.getInstance().getAppContext().getPackageName();
        }
        return packageName;
    }

    //리소스의 String배열 가져오기
    public static String[] getStringArray(int stringArrayId)
    {
        return MyApplication.getInstance().getAppContext().getResources().getStringArray(stringArrayId);
    }

    //UUID 생성
    public static String makeUUID(){
        return UUID.randomUUID().toString();
    }

    //MD5
    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //ByteArray -> HexString
    public static String byteArrayToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for(byte b: bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    //HexString -> ByteArray
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //Empty
    public static boolean isEmpty(String a){
        if(a == null) {
            return true;
        }
        else if(a.length() == 0){
            return true;
        }

        return false;
    }

    //Equal
    public static boolean equalNotEmpty(String a, String b){
        if(a == null || b == null){
            return false;
        }else if(a.length() == 0 || b.length() == 0){
            return false;
        }

        return a.equals(b);
    }
    public static boolean isDiff(String a, String b){

        if(a == null && b == null) {
            return false;
        }
        if(a == null && b != null){
            return true;
        }
        else if(a != null && b == null){
            return true;
        }
        else {
            return !a.equals(b);
        }


    }


    //소숫점 포맷 (0자리)
    public static String decimalFormatDot0(float value){

        DecimalFormat mFormat = new DecimalFormat("###,###,###");
        return mFormat.format(value);
    }

    //소숫점 포맷 (1자리)
    public static String decimalFormatDot1(float value){

        DecimalFormat mFormat = new DecimalFormat("###,###,##0.0");
        return mFormat.format(value);
    }

    //소숫점 포맷 (3자리)
    public static String decimalFormatDot3(float value){

        DecimalFormat mFormat = new DecimalFormat("###,###,##0.000");
        return mFormat.format(value);
    }
}
