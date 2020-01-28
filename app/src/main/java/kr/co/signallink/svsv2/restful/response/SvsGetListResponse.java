package kr.co.signallink.svsv2.restful.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SvsGetListResponse extends APIResponse{

    /*
    {
        "list": [
            {
                "factory_id": "159",
                "factory_name": "IT정보센터",
                "equipment_id": "80",
                "equipment_name": "모바일",
                "id": "60",
                "company_id": "91",
                "name": "Tiny",
                "serial_no": "SVS40DB_190906000001",
                "version": null,
                "user_id": "1087",
                "picture": "1_vgN2zojqiIYu23JPVuaSiA.png",
                "created": "2019-10-17 15:45:23",
                "position": "desk",
                "model": "SVS40",
                "added": "SVS40DB_190906000001"
            }
        ],
        "list_total": "1",
        "paging": {
            "first_page": 1,
            "last_page": 1,
            "prev_page": 1,
            "next_page": 1,
            "max_page": 1,
            "page": 1,
            "search": "",
            "option": "",
            "addtional_option": null
        },
        "file_path": "/files/sensor/"
    }
     */

    @SerializedName("list")
    public List<Datum> list = null;

    public class Datum {

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("picture")
        public String picture;

        @SerializedName("model")
        public String model;

        @SerializedName("serial_no")
        public String serial_no;

        @SerializedName("position")
        public String position;

        @SerializedName("created")
        public String created;
    }

}
