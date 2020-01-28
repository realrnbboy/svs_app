package kr.co.signallink.svsv2.restful.response;

import com.google.gson.annotations.SerializedName;

public class CompanyGetResponse extends APIResponse {

    /*
    {
        "success": 1,
        "error": 0,
        "item": {
            "id": "91",
            "user_id": "1087",
            "name": "두두원",
            "status": "1",
            "phone": "010-3917-4799",
            "email": "shseo@dodo1.co.kr",
            "about": "자동 추가된 고객사",
            "picture": null,
            "created": "2019-10-11 15:44:48"
        },
        "factory_list": [
            {
                "id": "159",
                "company_id": "91",
                "name": "IT정보센터",
                "created": "2019-10-14 11:21:52"
            }
        ],
        "message": "success",
        "file_path": "/files/company/91"
    }
     */


    @SerializedName("item")
    public Datum item = null;

    public class Datum {

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("created")
        public String created;
    }

}
