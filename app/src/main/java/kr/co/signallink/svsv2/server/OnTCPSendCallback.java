package kr.co.signallink.svsv2.server;

public interface OnTCPSendCallback {
    void onSuccess(String tag, Object obj);
    void onFailed(String tag, String msg);
}
