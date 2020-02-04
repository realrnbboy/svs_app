package kr.co.signallink.svsv2.services;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.model.MainData;
import kr.co.signallink.svsv2.utils.ToastUtil;
import kr.co.signallink.svsv2.views.activities.PresetActivity;
import kr.co.signallink.svsv2.views.activities.PresetListActivity;

/**
 * Created by hslee on 2018-07-05.
 */

public class SendMessageHandler extends Handler {

    MainData mainData = null;
    PresetActivity presetActivity = null;

    public SendMessageHandler() {}

    public SendMessageHandler(MainData mainData) {
        this.mainData = mainData;
    }

    public SendMessageHandler(PresetActivity presetActivity) {
        this.presetActivity = presetActivity;
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        int type = msg.getData().getInt("type");
        int errorType = msg.getData().getInt("errorType");
        int message = msg.getData().getInt("message");
        String data = msg.getData().getString("data");
        int returnContext = msg.getData().getInt("returnContext");
        String jsonString = msg.getData().getString("jsonString");

        if( errorType == DefConstant.URL_TYPE_SERVER_NO_RESPONSE ) {
            //ToastUtil.showShort("server not respond. please try later.");
        }

        switch (type) {

            case DefConstant.URL_TYPE_GET_CAUSE:
                mainData.parseCause(jsonString);
                break;

            case DefConstant.URL_TYPE_GET_FEATURE:
                mainData.parseFeature(jsonString);
                break;

            case DefConstant.URL_TYPE_GET_PRESET:
                presetActivity.parsePreset(jsonString);
                break;

//            case DefConstant.URL_TYPE_SERVER_NO_RESPONSE:
//                ToastUtil.showShort("server not respond. please try later.");
//                break;

            default:
                break;
        }
    }

}
