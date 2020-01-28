package kr.co.signallink.svsv2.commons;

/**
 * Created by nspil on 2018-02-14.
 */

public class DefComboBox {

    private DefComboBox() {};

    public static String measurerange(int type) {

        String rsp = "+/-2g";

        switch (type) {
            case 0:
                rsp = "+/-2g";
                break;
            case 1:
                rsp = "+/-4g";
                break;
            case 2:
                rsp = "+/-8g";
                break;
            case 3:
                rsp = "+/-16g";
                break;
        }

        return rsp;
    }

    public static String measureaxis(int type) {

        String rsp = "X";

        switch (type) {
            case 0:
                rsp = "X";
                break;
            case 1:
                rsp = "Y";
                break;
            case 2:
                rsp = "Z";
                break;
        }

        return rsp;
    }

    public static String samplingfreq(int type) {

        String rsp = "1 Hz";

        switch (type) {
            case 0:
                rsp = "0.5 Hz";
                break;
            case 1:
                rsp = "5 Hz";
                break;
            case 2:
                rsp = "12.5 Hz";
                break;
            case 3:
                rsp = "25 Hz";
                break;
            case 4:
                rsp = "50 Hz";
                break;
            case 5:
                rsp = "100 Hz";
                break;
            case 6:
                rsp = "200 Hz";
                break;
            case 7:
                rsp = "672 Hz";
                break;
            case 8:
                rsp = "(HR)150 Hz";
                break;
            case 9:
                rsp = "(LP)2,685 Hz";
                break;
        }

        return rsp;
    }

    public static float samplingfreqToHz(int type) {

        float rsp = 1;

        switch (type) {
            case 0:
                rsp = 0.5f;
                break;
            case 1:
                rsp = 5;
                break;
            case 2:
                rsp = 12.5f;
                break;
            case 3:
                rsp = 25;
                break;
            case 4:
                rsp = 50;
                break;
            case 5:
                rsp = 100;
                break;
            case 6:
                rsp = 200;
                break;
            case 7:
                rsp = 672;
                break;
            case 8:
                rsp = 150;
                break;
            case 9:
                rsp = 2685;
                break;
        }

        return rsp;
    }

    public static String offsetremoval(int type) {

        String rsp = "Off";

        switch (type) {
            case 0:
                rsp = "Off";
                break;
            case 1:
                rsp = "Auto";
                break;
            case 2:
                rsp = "Manual";
                break;
        }

        return rsp;
    }

    public static String dataconversion(int type) {

        String rsp = "m/s^2";
        //Html.fromHtml(Character.toString((char) 0x222B) + "<sup><small>" + "3" + "</small></sup>" + "<sub><small>" + "0" + "</small></sub>")

        switch (type) {
            case 0:
                rsp = "m/s^2";
                break;
            case 1:
                rsp = "mm/s";
                break;
        }

        return rsp;
    }

    public static String mode(int type) {

        String rsp = "Auto Run";

        switch (type) {
            case 0:
                rsp = "Auto Run";
                break;
            case 1:
                rsp = "Trigger";
                break;
            case 2:
                rsp = "Data Transfer";
                break;
        }

        return rsp;
    }

    public static String trigercode(int type) {

        String rsp = "Peak";

        switch (type) {
            case 0:
                rsp = "Peak";
                break;
            case 1:
                rsp = "RMS";
                break;
        }

        return rsp;
    }
}

/*
switch (type) {
        case 0:
            rsp = "1 Hz";
            break;
        case 1:
            rsp = "10 Hz";
            break;
        case 2:
            rsp = "25 Hz";
            break;
        case 3:
            rsp = "50 Hz";
            break;
        case 4:
            rsp = "100 Hz";
            break;
        case 5:
            rsp = "200 Hz";
            break;
        case 6:
            rsp = "400 Hz";
            break;
        case 7:
            rsp = "1,344 Hz";
            break;
        case 8:
            break;
        case 9:
            break;
    }
 */