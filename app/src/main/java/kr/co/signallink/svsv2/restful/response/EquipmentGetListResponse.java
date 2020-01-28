package kr.co.signallink.svsv2.restful.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EquipmentGetListResponse extends APIResponse{

    /*
    {
        "success": 0,
        "error": 22,
        "message": "invalid app key"
    }
     */
    /*
    {
        "list": [
            {
                "id": "80",
                "user_id": "1087",
                "company_id": "91",
                "factory_id": "159",
                "equipment_type_id": "36",
                "name": "모바일",
                "descript": "..",
                "serial_no": null,
                "manufacturer": "두두원",
                "primary": "0",
                "picture": "67490365_1_1490173319_w640.jpg",
                "created": "2019-10-17 15:44:14",
                "company_name": "두두원",
                "factory_name": "IT정보센터",
                "equipment_type_name": "기타",
                "svs_count": "1"
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
            "addtional_option": ""
        },
        "file_path": "/files/company/91"
    }
     */

    @SerializedName("list")
    public List<Datum> list = null;

    public class Datum {

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("company_name")
        public String company_name;

        @SerializedName("factory_name")
        public String factory_name;

        @SerializedName("picture")
        public String picture;

        @SerializedName("created")
        public String created;

        @SerializedName("factory_id")
        public String factory_id;
    }
}
