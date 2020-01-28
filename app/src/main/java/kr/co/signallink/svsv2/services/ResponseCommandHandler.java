package kr.co.signallink.svsv2.services;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import kr.co.signallink.svsv2.command.ParserCommand;
import kr.co.signallink.svsv2.command.ResponseCommandPacket;
import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.server.TCPSend;
import kr.co.signallink.svsv2.server.TCPSendUtil;
import kr.co.signallink.svsv2.user.SVS;

public class ResponseCommandHandler extends Handler {
    private Context context;

    public ResponseCommandHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case DefBLEdata.RESPONSECOMMAND_ARRIVE:

                ResponseCommandPacket responseCommandPacket = (ResponseCommandPacket) msg.obj;
                if(responseCommandPacket != null)
                {
                    Log.d("TTTT","ResponseCommandHandler type:"+responseCommandPacket.getType()+",bytes:"+responseCommandPacket.getSize());

                    try
                    {
                        ParserCommand.parser(responseCommandPacket);

                        switch (responseCommandPacket.getType())
                        {
                            case HELLO:
                                broadcastUpdate(DefBLEdata.HELLO_ARRIVE);
                                break;
                            case BAT:
                                broadcastUpdate(DefBLEdata.BAT_ARRIVE);
                                break;
                            case UPLOAD:
                                broadcastUpdate(DefBLEdata.UPLOAD_ARRIVE);
                                break;
                            case MEASURE_OPTION_NONE:
                            case MEASURE_OPTION_WITH_FREQ:
                            case MEASURE_OPTION_WITH_TIME:
                            case MEASURE_OPTION_WITH_TIME_FREQ:
                                broadcastUpdate(DefBLEdata.MEASURE_ARRIVE);
//                                TCPSendUtil.sendMeasure();
                                break;
                            case EVENT_WARNING:
                                broadcastUpdate(DefBLEdata.EVENTWARNING_ARRIVE);
                                break;
                            case EVENT_DANGER:
                                broadcastUpdate(DefBLEdata.EVENTDANGER_ARRIVE);
                                break;
                            case LEARNING:
                                broadcastUpdate(DefBLEdata.LEARNING_ARRIVE);
                                break;
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }

                break;
            case DefBLEdata.RESPONSECOMMAND_DISCONNECT:
                Log.d("TTTT","ResponseCommandHandler Close");
                SVS.getInstance().setSvsDeviceAddress(null);
                break;
        }
    }

    private void broadcastUpdate(final String action) {

        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
