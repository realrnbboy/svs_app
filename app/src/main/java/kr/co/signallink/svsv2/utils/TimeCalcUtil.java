package kr.co.signallink.svsv2.utils;

import android.util.Log;

public class TimeCalcUtil {

    private String title;
    private long timeStamp;

    public TimeCalcUtil(){
        timeStamp = System.currentTimeMillis();
        Log.d("TTTT","TimeCalcUtil Init");
    }

    public TimeCalcUtil(String title){
        this.title = title;
        timeStamp = System.currentTimeMillis();
        Log.d("TTTT","TimeCalcUtil "+title+" Init");
    }

    public void printGap(){
         printGap("");
    }

    public void printGap(String msg){
        long tempTimeStamp = System.currentTimeMillis();

        Log.d("TTTT","TimeCalcUtil "+(title!=null?title:"") + " " + msg + " " + " GAP:"+(tempTimeStamp-timeStamp));

        timeStamp = tempTimeStamp;
    }

}
