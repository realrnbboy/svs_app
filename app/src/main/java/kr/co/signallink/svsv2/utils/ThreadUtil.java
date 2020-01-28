package kr.co.signallink.svsv2.utils;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtil {

    public interface OnBlock {

        void run();
    }

    public static boolean isLooper(){

        return (Looper.myLooper() == Looper.getMainLooper());
    }

    public static void runOnMainLooper(final OnBlock onBlock){

        if(!isLooper()){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    onBlock.run();
                }
            });
        }else {
            onBlock.run();
        }

    }

}
