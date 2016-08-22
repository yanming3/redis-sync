package com.yanming.sync.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public final class ByteUtils {
    private static final byte[] TRUE = new byte[]{'1'};

    private static final byte[] FALSE = new byte[]{'0'};

    public static byte[] toBytes(double value) {
        return toBytes(Double.toString(value));
    }

    public static byte[] toBytes(int value) {
        return toBytes(Integer.toString(value));
    }

    public static byte[] toBytes(long value) {
        return toBytes(Long.toString(value));
    }

    public static byte[] toBytes(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static byte[] toBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toBytesExclusive(double value) {
        return toBytes("(" + Double.toString(value));
    }

    public static String bytesToString(byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }

    public static double bytesToDouble(byte[] value) {
        return Double.parseDouble(bytesToString(value));
    }

    public static byte[][] toParamsReverse(byte[][] tailParams, byte[]... headParams) {
        return toParams(headParams, tailParams);
    }

    public static byte[][] toParams(byte[][] headParams, byte[]... tailParams) {
        byte[][] params = Arrays.copyOf(headParams, headParams.length + tailParams.length);
        System.arraycopy(tailParams, 0, params, headParams.length, tailParams.length);
        return params;
    }

    /*
        * 对16位数据进行高低位切换,低位转为高位
        */
    public static void memrev16(byte[] p, int start) {
        byte[] x = p;
        byte t;

        t = x[start];
        x[start] = x[start + 1];
        x[start + 1] = t;
    }

    /*
     * 对32位数据进行高低位切换,低位转为高位
     */
    public static void memrev32(byte[] p, int start) {
        byte[] x = p;
        byte t;

        t = x[start];
        x[start] = x[start + 3];
        x[start + 3] = t;
        t = x[start + 1];
        x[start + 1] = x[start + 2];
        x[start + 2] = t;
    }

    /*
     * 对64位数据进行高低位切换,低位转为高位
     */
    public static void memrev64(byte[] p, int start) {
        byte[] x = p;
        byte t;

        t = x[start];
        x[start] = x[start + 7];
        x[start + 7] = t;
        t = x[start + 1];
        x[start + 1] = x[start + 6];
        x[start + 6] = t;
        t = x[start + 2];
        x[start + 2] = x[start + 5];
        x[start + 5] = t;
        t = x[start + 3];
        x[start + 3] = x[start + 4];
        x[start + 4] = t;
    }

    public static byte[] chars2bytes(String str) {
        try {
            return str.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }

    public static int strtoi(byte[] buf, int start, int radix) {
        int len = start;
        for (; len < buf.length; len++) {
            if (buf[len] == 0x00)
                break;
        }
        char[] c = new char[len - start];
        for (int i = start; i < buf.length; i++) {
            c[i - start] = (char) (0x000000ff & buf[i]);
        }
        String str = new String(c);
        return Integer.parseInt(str, radix);
    }

    /**
     * 把两个byte数组进行对比，判断是否相等
     *
     * @param buf1
     * @param start1
     * @param buf2
     * @param start2
     * @param len    对比的长度
     * @return
     */
    public static int memcmp(byte[] buf1, int start1, byte[] buf2, int start2,
                             int len) {
        int pos1 = start1;
        int pos2 = start2;
        for (int i = 0; i < len; i++) {
            pos1 = start1 + i;
            pos2 = start2 + i;
            if (buf1.length <= pos1 && buf2.length <= pos2)
                return 0;
            if (buf1.length <= pos1)
                return -1;
            if (buf2.length <= pos2)
                return 1;
            if (buf1[pos1] != buf2[pos2])
                return buf1[pos1] > buf2[pos2] ? 1 : -1;
        }
        return 0;
    }
}
