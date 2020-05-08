package kr.co.signallink.svsv2.server;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import kr.co.signallink.svsv2.commons.DefConstant;
import kr.co.signallink.svsv2.utils.SharedUtil;
import kr.co.signallink.svsv2.utils.ToastUtil;

public class TCPSend extends AsyncTask<Void, Void, Void> {

    private static TCPSend instance;

    private boolean bWhile = true;
    private boolean can = true;

    private Socket socket = null;
    private String ip;
    private int port;

    private ArrayList<TCPSendData> tcpSendDatas = new ArrayList<>();

    public static TCPSend getInstance(){
        if(instance == null){
            instance = new TCPSend();
        }

        return instance;
    }

    public TCPSend(){
        can = true;
        this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    protected Void doInBackground(Void... params) {

        while(can)
        {
            synchronized (tcpSendDatas){

                if(tcpSendDatas.size() > 0)
                {
                    final TCPSendData tcpSendData = tcpSendDatas.get(0);

                    //데이터 준비
                    byte[] bytes = tcpSendData.bytes;
//                    for( int i =0; i<bytes.length; i++) {   // for test
//                        //Log.d("---", String.format("%d:%x, ", i, bytes[i]));
//                    }
                    if(bytes == null){

                        //실패
                        TCPSendUtil.fail(tcpSendData.tag, "Wrong Data", tcpSendData.callback);

                        //비정상, 데이터 리스트에서 제거
                        removeTCPSendData(tcpSendData.callback);

                        continue;
                    }


                    try{
                        //서버 세팅
                        boolean retLoad = loadServerIpPort();
                        if(!retLoad){
                            continue;
                        }

                        //데이터 보내기
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                        dos.write(bytes, 0, bytes.length);
                        dos.flush();

                        try {
                            Thread.sleep(10); // added by hslee 2020.05.08
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        //성공
                        final String msg = "Successful upload of sensor information to the server.";
                        TCPSendUtil.success(tcpSendData.tag, msg, tcpSendData.callback);
                        removeTCPSendData(tcpSendData.callback);
                    }
                    catch (SocketTimeoutException ste){

                        //실패
                        final String msg = "Please check your network environment. (TimeOut).";
                        TCPSendUtil.fail(tcpSendData.tag, msg, tcpSendData.callback);
                        removeTCPSendData(tcpSendData.callback);

                        socket = null;
                    }
                    catch (SocketException se){

                        //실패
                        final String msg = "Please check your internet connection. Could not connect to server. Please Retry.";
                        TCPSendUtil.fail(tcpSendData.tag, msg, tcpSendData.callback);
                        removeTCPSendData(tcpSendData.callback);

                        socket = null;
                    }
                    catch (IOException e) {

                        //실패
                        final String msg = "Server Send Err('"+e.toString()+"')";
                        TCPSendUtil.fail(tcpSendData.tag, msg, tcpSendData.callback);
                        removeTCPSendData(tcpSendData.callback);

                        socket = null;
                    }


                }
            }

            //sleep
            try{
                Thread.sleep(20);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        return null;
    }

    public boolean loadServerIpPort() throws IOException {

        //서버 ip, port
        String serverIp = SharedUtil.load(SharedUtil.KEY.CONNECT_SERVER_IP.toString(), DefConstant.DEFAULT_SERVER_IP);
        String strServerPort = SharedUtil.load(SharedUtil.KEY.CONNECT_SERVER_PORT.toString(), DefConstant.DEFAULT_SERVER_PORT);

        //Check Wrong Ip/Port
        if(serverIp == null || strServerPort == null
                || serverIp.length() <= 3 || strServerPort.length() <=1 )
        {
            Log.i("TTTT","TCPSend sendConfig err(1). ip:"+serverIp+",port:"+strServerPort);
            ToastUtil.showShort("The server address is invalid.");
            return false;
        }

        //Check Change
        if(socket == null || ip==null || !this.ip.equals(serverIp) || this.port!=Integer.parseInt(strServerPort))
        {
            this.ip = serverIp;
            this.port = Integer.parseInt(strServerPort);

            if(socket != null){
                socket.close();
            }

            socket = new Socket(ip, port);
            socket.setSoTimeout(1*1000);
            Log.d("TTTT","Change Server Ip,Port");
        }

        return true;
    }

    //데이터 풀에 추가하기
    public void send(TCPSendData tcpSendData) {
        synchronized (tcpSendDatas){
            tcpSendDatas.add(tcpSendData);
        }
    }

    //Check TCPSendData
    public void removeTCPSendData(OnTCPSendCallback callback){

        if(callback != null)
        {
            tcpSendDatas.remove(0);
        }

    }

}
