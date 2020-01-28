package kr.co.signallink.svsv2.restful;

import kr.co.signallink.svsv2.restful.response.APIResponse;

public interface OnApiListener {

    void success(APIResponse apiResponse);
    boolean fail(String message); //ret true is hide toast message.

}
