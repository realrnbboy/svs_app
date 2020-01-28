package kr.co.signallink.svsv2.restful.response;

import com.google.gson.annotations.SerializedName;

public class APIResponse {

    @SerializedName("error")
    public Integer error;

    @SerializedName("message")
    public String message;
}
