package kr.co.signallink.svsv2.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import kr.co.signallink.svsv2.services.MyApplication;

public class SizeUtil {

    //텍스트를 그렸을때 나오는 사이즈 반환
    public static Rect getTextRect(String text, Typeface typeface, float textSize){

        Paint paint = new Paint();
        Rect result = new Rect();

        paint.setTypeface( typeface!=null ? typeface : Typeface.DEFAULT);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        paint.getTextBounds(text, 0, text.length(), result);

        return result;
    }

    //DP to PX
    public static int dpToPx(float dp){

        DisplayMetrics dm = MyApplication.getInstance().getAppContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);

    }

    //PX to DP
    public static float pxToDp(float px){

        Resources resources = MyApplication.getInstance().getAppContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;

    }


}
