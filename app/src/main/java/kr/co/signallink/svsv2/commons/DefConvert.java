package kr.co.signallink.svsv2.commons;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by nspil on 2018-02-07.
 */

public class DefConvert {

    public static int byteToInt(byte[] bytes, int offset, int length, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
        buff.order(order);

        buff.put(bytes, offset, length);
        buff.rewind();

        //System.out.println("byteToInt : " + buff);

        return buff.getInt();
    }

    public static float byteToFloat(byte[] bytes, int offset, int length, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Float.SIZE/8);
        buff.order(order);

        buff.put(bytes, offset, length);
        buff.rewind();

        //System.out.println("byteToFloat : " + buff);

        return buff.getFloat();
    }

    public static long byteToLong(byte[] bytes, int offset, int length, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Long.SIZE/8);
        buff.order(order);

        buff.put(bytes, offset, length);
        buff.rewind();

        //System.out.println("byteToLong : " + buff);

        return buff.getLong();
    }

    public static String byteToString(byte[] bytes, int bytesOffset, int byteLength, ByteOrder order) {

        int realByteLength = byteLength;
        for(int i=bytesOffset; i<bytesOffset+byteLength; i++)
        {
            if(bytes[i] == 0){
                realByteLength = i - bytesOffset;
                break;
            }
        }

        String str = new String(bytes, bytesOffset, realByteLength);
        return str;
    }

    public static float[] byteArrayToFloatArray(byte[] bytes, int bytesOffset, int byteLength, float[] floats) {

        ByteBuffer bb = ByteBuffer.allocate(byteLength);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bytes, bytesOffset, byteLength);
        bb.rewind();

        FloatBuffer fb = bb.asFloatBuffer();
        fb.get(floats);

        return null;

    }

    public static <T> boolean hasDuplicate(Iterable<T> all) {
        Set<T> set = new HashSet<T>();
        // Set#add returns false if the set does not change, which
        // indicates that a duplicate element has been added.
        for (T each: all) if (!set.add(each)) return true;
        return false;
    }


}
