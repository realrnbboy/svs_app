package kr.co.signallink.svsv2.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import androidx.core.content.ContextCompat;

import kr.co.signallink.svsv2.R;

public class DialogUtil {

    public static void closeApp(Context context, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNegativeListener){

        yesNo(context, R.string.popup_title, R.string.popup_message, onPositiveListener, onNegativeListener);
    }


    public static void bleClose(Context context, String title, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNegativeListener){

        yesNo(context, title, "Do you want to disconnect?", onPositiveListener, onNegativeListener);

    }


    public static void autoSave(Context context, String title, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNegativeListener) {

        yesNo(context, title, "Auto Processing ?", onPositiveListener, onNegativeListener);

    }


    public static void yesNo(Context context, int titleResoureceId, int messageResourceId, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNegativeListener) {

        Resources res = context.getResources();

        yesNo(context, res.getString(titleResoureceId), res.getString(messageResourceId), onPositiveListener, onNegativeListener);
    }


    public static void yesCancelMode(Context context, String title, String message, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNeutralListener, DialogInterface.OnClickListener onNegativeListener) {

        androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertBuilder.setTitle(title)
                .setCancelable(false);

        if(message != null){
            alertBuilder.setMessage(message);
        }

        alertBuilder.setPositiveButton("Yes", onPositiveListener);
        alertBuilder.setNeutralButton("Cancel", onNeutralListener);
        alertBuilder.setNegativeButton("Move", onNegativeListener);

        androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
        alert.show();

        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
    }


    public static void yesNo(Context context, String title, String message, DialogInterface.OnClickListener onPositiveListener, DialogInterface.OnClickListener onNegativeListener) {

        androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertBuilder.setTitle(title)
                    .setCancelable(false);

        if(message != null){
            alertBuilder.setMessage(message);
        }

        alertBuilder.setPositiveButton("Yes", onPositiveListener);
        alertBuilder.setNegativeButton("Cancel", onNegativeListener);

        androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
        alert.show();

        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
    }

    public static void confirm(Context context, String title, String message, DialogInterface.OnClickListener onPositiveListener) {

        androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertBuilder.setTitle(title)
                .setCancelable(false);

        if(message != null){
            alertBuilder.setMessage(message);
        }

        alertBuilder.setPositiveButton("Confirm", onPositiveListener);

        androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
        alert.show();

        alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
    }
}
