package kr.co.signallink.svsv2.restful.request;

import com.google.gson.annotations.SerializedName;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class LoginAPIRequest extends APIRequest {

    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;

    public LoginAPIRequest(String id, String password){
        this.username = id;
        this.password = password;
    }

    public RequestBody getRequestBody(){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_key", app_key)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .build();

        return requestBody;
    }
}
