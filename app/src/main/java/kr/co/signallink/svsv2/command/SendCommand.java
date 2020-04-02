package kr.co.signallink.svsv2.command;

import android.util.Log;

import kr.co.signallink.svsv2.commons.DefBLEdata;
import kr.co.signallink.svsv2.services.UartService;

import static java.lang.Thread.sleep;

/**
 * Created by nspil on 2018-02-07.
 */

public class SendCommand {

    private static final byte NEED_CHECK_SUM = 0x00;

    public static boolean canLearning = true;

    public static boolean send(DefBLEdata.CMD type, UartService service) {

        Log.d("TTTT","SendCommand type:"+type);

        //Default Delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Processing
        boolean ret = false;
        byte[] cmd = null;

        switch (type) {
            case HELLO:
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                canLearning = true;
                cmd = hello(service);
                break;
            case UPLOAD:
                cmd = request_upload(service);
                break;
            case MEASURE_V4:
                cmd = measure_v4(service);
                break;
            case MEASURE_OPTION_NONE:
                cmd = measure_option_none(service);
                break;
            case MEASURE_OPTION_WITH_FREQ:
                cmd = measure_option_with_freq(service);
                break;
            case MEASURE_OPTION_WITH_TIME:
                cmd = measure_option_with_time(service);
                break;
            case MEASURE_OPTION_WITH_TIME_FREQ:
                cmd = measure_option_with_time_freq(service);
                break;
            case EVENT_WARNING:
                cmd = event_warning(service);
                break;
            case EVENT_DANGER:
                cmd = event_danger(service);
                break;
            case PLC_ON:
                cmd = plc_on(service);
                break;
            case PLC_OFF:
                cmd = plc_off(service);
                break;
            case BAT:
                cmd = bat(service);
                break;
            case LEARNING:
                if(canLearning)
                {
                    ret = learning(service);
                    if(ret)
                    {
                        canLearning = false;
                    }
                }
                break;
            default:
                break;
        }

        if(cmd != null && cmd.length > 0)
        {
            try {
                ParserCommand.AutoCheckSumProcessing(cmd);
                ret = service.writeRXCharacteristic(cmd);
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            }
        }

        return ret;
    }

    private static byte[] hello(UartService service) {
        byte[] cmd = {(byte)0x5a, (byte)0x01, (byte)0x05, (byte)0x00, (byte)0xfb};
        return cmd;
    }

    private static byte[] request_upload(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x03, (byte)0x05, (byte)0x00, (byte)0xf9};
        return cmd;
    }

    private static byte[] measure_v4(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x04, (byte)0x05, (byte)0x00, (byte)0xfe};
        return cmd;
    }

    private static byte[] measure_option_none(UartService service) {
        byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0xf0, NEED_CHECK_SUM};
        return cmd;
    }

    private static byte[] measure_option_with_time(UartService service){
        byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0xf1, NEED_CHECK_SUM};
        //byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0x1f, NEED_CHECK_SUM};  // added by hslee 2020.04.02
        return cmd;
    }

    private static byte[] measure_option_with_freq(UartService service){
        byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0xf2, NEED_CHECK_SUM};
        //byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0x2f, NEED_CHECK_SUM};  // added by hslee 2020.04.02
        return cmd;
    }
    
    private static byte[] measure_option_with_time_freq(UartService service){
        byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0xf3, NEED_CHECK_SUM};
        //byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0x0f, NEED_CHECK_SUM};  // added by hslee 2020.04.02
        //byte[] cmd = {(byte)0x5a, (byte)0x04, (byte)0x06, (byte)0x00, (byte)0xf0, NEED_CHECK_SUM};  // added by hslee 2020.04.02
        return cmd;
    }

    private static byte[] event_warning(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x0d, (byte)0x06, (byte)0x00, (byte)0x0f, (byte)0xfb};
        return cmd;
    }

    private static byte[] event_danger(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x0d, (byte)0x06, (byte)0x00, (byte)0xf0, (byte)0x04};
        return cmd;
    }

    private static byte[] trg_start(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x08, (byte)0x05, (byte)0x00, (byte) 0xf2};
        return cmd;
    }

    private static byte[] plc_on(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x0b, (byte)0x06, (byte)0x00, (byte)0x0f, (byte)0xfd};
        return cmd;
    }

    private static byte[] plc_off(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x0b, (byte)0x06, (byte)0x00, (byte)0xf0, (byte)0x02};
        return cmd;
    }


    private static byte[] bat(UartService service) {
        byte [] cmd = {(byte)0x5a, (byte)0x0e, (byte)0x05, (byte)0x00, (byte)0xf4};
        return cmd;
    }

    private static boolean learning(UartService service) {
        try {
            byte [] cmd = {(byte)0x5a, (byte)0x0f, (byte)0x05, (byte)0x00, NEED_CHECK_SUM};
            ParserCommand.AutoCheckSumProcessing(cmd);

            return service.writeRXCharacteristic(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
