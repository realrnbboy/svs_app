package kr.co.signallink.svsv2.server;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import kr.co.signallink.svsv2.commons.DefConstant;

/**
 * Created by hslee on 2018-07-05.
 */
public class SendPost extends Thread {
    Handler handler = null;
    int sendType = 0;

    int returnContextType = 0;

    static boolean bNetworkError = false;
    static long networkErrorTime = 0;
    DataOutputStream dataStream = null;

    HashMap<String, String> sendPostData = new HashMap<String, String>();
    HashMap<String, String> sendGetData = new HashMap<String, String>();

    Resources resouces = null;

    public int getReturnContext() {
        return returnContextType;
    }

    public void setReturnContext(int returnContext) {
        this.returnContextType = returnContext;
    }

    public SendPost(int sendType) {
        this.sendType = sendType;

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        send();
        Looper.loop();
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setResource(Resources resouces) {
        this.resouces = resouces;
    }

    public void addPostData(String key, String value) {
        sendPostData.put(key, value);
    }

    public void send() {

        InputStream is = null;
        String result = "";
        try {

            String urlStr = "";
            switch( sendType ) {
                case DefConstant.URL_TYPE_GET_CAUSE :
                    urlStr = DefConstant.URL_GET_CAUSE;
                    break;
                case DefConstant.URL_TYPE_GET_FEATURE :
                    urlStr = DefConstant.URL_GET_FEATURE;
                    break;
                case DefConstant.URL_TYPE_GET_PRESET :
                    urlStr = DefConstant.URL_GET_PRESET;
                    break;

                default :
                    return;
            }

            urlStr = DefConstant.WEB_URL + urlStr;
            //urlStr = "http://192.168.1.133" + urlStr;

            URL url = new URL(urlStr);
            HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
            httpCon.setConnectTimeout(DefConstant.CONNECTION_WAIT_TIME); // 타임아웃: 5초
            httpCon.setUseCaches(false); // 캐시 사용 안 함
            httpCon.setRequestMethod("POST"); // POST로 연결
            httpCon.setDoInput(true);
            httpCon.setDoOutput(true);

            // receive response as inputStream
            try {
                OutputStream os = httpCon.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
                bw.write(getPostString(sendPostData)); // 매개변수 전송
                bw.flush();
                bw.close();
                os.flush();

                is = httpCon.getInputStream();
                // convert inputstream to string
                if(is != null) {
                    result = convertInputStreamToString(is);

                    processReceiveData(result);
                    return;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                processError();
                Log.d("boot auto test", "network error1");
            }
            finally {
                httpCon.disconnect();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            processError();
            Log.d("boot auto test", "network error2");
        }

        return;
    }

    public  void processError() {

        if( handler != null ) { // 서버연결 실패 등의 오류
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt("errorType", DefConstant.URL_TYPE_SERVER_NO_RESPONSE);
            data.putInt("type", sendType);
            msg.setData(data);


            handler.sendMessage(msg);
        }
    }

    public void processReceiveData(String result) {

        if( handler == null )
            return;

        Message msg = new Message();
        Bundle data = new Bundle();

        data.putInt("type", sendType);
        data.putString("jsonString", result);
        data.putInt("returnContext", returnContextType);
        msg.setData(data);

        handler.sendMessage(msg);
    }


    public String convertInputStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);//.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private String getPostString(HashMap<String, String> map) {
        StringBuilder result = new StringBuilder();
        boolean first = true; // 첫 번째 매개변수 여부

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if( entry.getKey() == null || entry.getValue() == null )
                continue;

            if (first)
                first = false;
            else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                result.append("&");

            try { // UTF-8로 주소에 키와 값을 붙임
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    private void sendGet() throws Exception {

        String api = sendGetData.get("api");

        String url = DefConstant.WEB_URL + api;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("contentType", "application/json");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        processReceiveData(response.toString());

    }


}