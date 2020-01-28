package kr.co.signallink.svsv2.restful.request;

import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SvsAPIRequest extends APIRequest {

    @SerializedName("page")
    public String page;

    @SerializedName("company_id")
    public String company_id;


    @SerializedName("equipment_id")
    public String equipment_id;


    public SvsAPIRequest(String page, String company_id, String equipment_id){
        this.page = page;
        this.company_id = company_id;
        this.equipment_id = equipment_id;

    }

    public RequestBody getRequestBody(){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_key", app_key)
                .addFormDataPart("page", page)
                .addFormDataPart("company_id", company_id)
                .addFormDataPart("equipment_id", equipment_id)
                .build();

        return requestBody;
    }
}
