package kr.co.signallink.svsv2.utils;

import java.nio.ByteBuffer;

public class ByteUtil {

    //UnsignedShort
    public static byte[] parseIntToUInt16Bytes(int number) {

        byte[] data = new byte[2];

        data[0] = (byte)(number & 0xff);
        data[1] = (byte)((number >> 8) & 0xff);

        return data;
    }

    //int to bytes
    public static byte[] intToByteArray(int a)
    {
        byte[] ret = new byte[4];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        ret[2] = (byte) ((a >> 16) & 0xFF);
        ret[3] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }



    public static byte[] fromUnsignedIntLittleEndian(long value)
    {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);

        return new byte[] {
                (byte)(bytes[7]),
                (byte)(bytes[6]),
                (byte)(bytes[5]),
                (byte)bytes[4]};
    }
}
