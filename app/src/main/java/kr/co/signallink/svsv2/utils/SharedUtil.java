package kr.co.signallink.svsv2.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;

import kr.co.signallink.svsv2.services.MyApplication;

import static android.content.Context.MODE_PRIVATE;

public class SharedUtil {

    public enum KEY {
        INIT_BACKWARD_COMPATIBILITY_FOR_FILES, //기존 파일 저장방식.
        CONNECT_SERVER_IP,  //접속 서버 주소.
        CONNECT_SERVER_PORT, //접속 서버 포트.
        CURRENT_SCREEN_MODE,  //화면 모드 (Local, Web)
        AUTO_NEXT_GO_MAIN //메인화면으로 자동으로 이동하는 모드 켜기
        ;

        @Override
        public String toString() {
            return this.name();
        }
    }


    public static void save(String key, String value)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String load(String key, String defaultValue)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getAppContext());
        return sharedPreferences.getString(key, defaultValue);
    }

}
