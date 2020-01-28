package kr.co.signallink.svsv2.restful.request;

import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CompanyAPIRequest extends APIRequest {

    @SerializedName("company_id")
    public String company_id;

    public CompanyAPIRequest(String company_id){
        this.company_id = company_id;
    }


    public RequestBody getRequestBody(){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_key", app_key)
                .addFormDataPart("company_id", company_id)
                .build();

        return requestBody;
    }

}
