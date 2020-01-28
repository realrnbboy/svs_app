package kr.co.signallink.svsv2.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

import kr.co.signallink.svsv2.services.MyApplication;

public class ValueUtil {

    //DisplayPoint -> Pixel
    public static float dpToPixel(float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getInstance().getAppContext().getResources().getDisplayMetrics());
    }

    //Pixel -> DisplayPoint
    public static int convertDpToPx(int dp)
    {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, MyApplication.getInstance().getAppContext().getResources().getDisplayMetrics());
    }

    //Get Screen Width
    public static int getScreenWidth()
    {
        DisplayMetrics dm = MyApplication.getInstance().getAppContext().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    //Get Screen Height
    public static int getScreenHeight()
    {
        DisplayMetrics dm = MyApplication.getInstance().getAppContext().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }


}
