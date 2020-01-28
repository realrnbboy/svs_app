package kr.co.signallink.svsv2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static final String SIMPLE_FORMAT_DATE = "yyyyMMdd";
    public static final String SIMPLE_FORMAT_TIME = "HHmmss";
    public static final String SIMPLE_FORMAT_MILLISEC = "SSS";

    public static Date convertString(String strDate, String format){

        Date date = null;

        try {
            date = new SimpleDateFormat(format).parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String convertDate(Date date, String format){
        String strDate = new SimpleDateFormat(format).format(date);
        return strDate;
    }

    public static String convertDefaultDetailDate(Date date){
        return convertDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String currentDefaultDetailDate(){
        Date date = new Date();
        return convertDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getDateStringBySimpleFormat(Date date)
    {
        return convertDate(date, SIMPLE_FORMAT_DATE);
    }

    public static String getTimeStringBySimpleFormat(Date date)
    {
        return convertDate(date, SIMPLE_FORMAT_TIME);
    }
}
