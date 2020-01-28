package kr.co.signallink.svsv2.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.Serializable;
import java.util.HashMap;

import kr.co.signallink.svsv2.commons.DefLog;

/**
 * Created by nspil on 2018-01-29.
 */

public class BaseActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_NAME = "equipment_name";
    public static final String EXTRA_EQUIPMENT_UUID = "equipment_uuid";
    public static final String EXTRA_SVS_UUID = "svs_uuid";
    public static final String EXTRA_SVS_ADDRESS = "svs_address";
    public static final String EXTRA_STR_TREND_VALUE = "str_trend_value";

    private InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null){
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void setContentView(int layout){
        super.setContentView(layout);
        DefLog.d("test", "=================================================================================");
    }


    public void goIntent(Class<?> cls){
        goIntent(cls, false);
    }


    public void goIntent(Class<?> cls, boolean isFinish){

        goIntent(cls, isFinish, null);
    }

    public void goIntent(Class<?> cls, boolean isFinish, HashMap<String, String> map){

        Intent i = new Intent(this, cls);
        if(map != null){
            for(String key : map.keySet()){
                String value = map.get(key);
                i.putExtra(key, value);
            }
        }

        startActivity(i);

        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        if(isFinish){
            this.finish();
        }
    }

    public void goIntent(Class<?> cls, boolean isFinish, String key, Serializable serializable){

        Intent i = new Intent(this, cls);
        i.putExtra(key, serializable);
        startActivity(i);

        //overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        if(isFinish){
            this.finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    //키보드 숨기기
    public void hideKeyboard() {

        View view = this.getCurrentFocus();
        if(view != null)
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
