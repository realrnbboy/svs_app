package kr.co.signallink.svsv2.restful.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AuthLoginResponse extends APIResponse {

    /*
    {
        "error": 0,
        "user": {
            "id": "1087",
            "email": "shseo@dodo1.co.kr",
            "username": "shseo1234",
            "password": "$2y$08$6U.0dvt/Kc/YO19zbi6tjebZ7gC4GPFv7RMz05jysnc13lW801Aiu",
            "active": "1",
            "last_login": "2019-11-06 01:36:35",
            "role": "1",
            "banned": "0",
            "removed": "0",
            "full_name": "서성호",
            "company_id": "91",
            "session_id": "o7ee5fv6bef7espra0lgqrmvl9tisko6"
        }
    }
    */

    @SerializedName("user")
    public Datum user = null;

    public class Datum {

        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        @SerializedName("full_name")
        public String full_name;

        @SerializedName("company_id")
        public String company_id;


    }

}
