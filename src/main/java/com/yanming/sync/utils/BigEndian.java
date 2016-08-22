package com.yanming.sync.utils;

/**
 * <p>高字节序,高位字节排放在内存的低地址端，低位字节排放在内存的高地址端;
 * TCP即采用该Big Endian作为网络字节序,JAVA也是采用高字节序;
 * </p>
 */
public final class BigEndian {
    /**
     * 将二进制数组转换为32位无符号整型数字
     *
     * @param b
     * @return
     */
    public static int uint32(byte[] b) {
        return uint32(b, 0);
    }

    /**
     * 将偏移量之后的二进制数组转换为32位无符号整型数字
     *
     * @param b
     * @param offset
     * @return
     */
    public static int uint32(byte[] b, int offset) {
        return b[offset + 3] & 0xff | (b[offset + 2] & 0xff) << 8 | (b[offset + 1] & 0xff) << 16 | (b[offset] & 0xff) << 24;
    }

    /**
     * 将二进制数组转换为16位无符号整型
     *
     * @param b
     * @return
     */
    public static int uint16(byte[] b) {
        return b[1] & 0xff | (b[0] & 0xff) << 8;
    }

}
