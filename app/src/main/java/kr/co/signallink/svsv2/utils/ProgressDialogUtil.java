package kr.co.signallink.svsv2.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProgressDialogUtil {

    private ProgressDialog progressDialog = null;
    private long showTime = System.currentTimeMillis();

    private final long MIN_SHOW_TIME = 300;

    public ProgressDialogUtil(Context context, @Nonnull String title, @Nullable String msg) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(title);

        if(msg != null){
            progressDialog.setMessage(msg);
        }

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }


    public void setNegativeButton(DialogInterface.OnClickListener onClickListener){

        if(onClickListener != null){
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", onClickListener);
        }
    }

    public void show(){

        if(!progressDialog.isShowing()){
            progressDialog.show();
            showTime = System.currentTimeMillis();
        }
    }

    public void hide() {

        //보여주고 닫는 시간이 최소 시간을 가질 수 있도록 개발하기

        long gapTime = System.currentTimeMillis() - showTime;
        if(gapTime > MIN_SHOW_TIME)
        {
            progressDialog.dismiss();
        }
        else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, MIN_SHOW_TIME-gapTime);
        }

    }






}
