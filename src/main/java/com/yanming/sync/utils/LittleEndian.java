package com.yanming.sync.utils;

/**
 * <p>低字节序,低位字节排放在内存的低地址端，高位字节排放在内存的高地址端</p>
 * <ul>
 * <li>X86系统采用Little Endian,而PowerPC 采用Big Endian;</li>
 * <li>java则无论平台变化，都是高字节开头;</li>
 * <li>C/C++和CPU相关</li>
 * </ul>
 */
public class LittleEndian {
    /**
     * 将字节数组转换为8位无符号整型
     *
     * @param b
     * @return
     */
    public static long uint8(byte[] b) {
        return b[0] & 0xff;
    }

    /**
     * 将字节数组转换为16位无符号整型
     *
     * @param b
     * @return
     */
    public static long uint16(byte[] b) {
        return b[0] & 0xff | (b[1] & 0xff) << 8;
    }

    /**
     * 将字节数组转换为32位无符号整型
     *
     * @param b
     * @return
     */
    public static long uint32(byte[] b) {
        return (long) (b[0] & 0xff) | ((long) (b[1] & 0xff)) << 8 | ((long) (b[2] & 0xff)) << 16 | ((long) (b[3] & 0xff)) << 24;
    }

    /**
     * 将字节数组转换为64位无符号整型,由于JAVA中LONG的范围是-2^63到2^63,因此有可能出现overflow
     *
     * @param b
     * @return
     */
    public static long uint64(byte[] b) {
        return (long) (b[0] & 0xff) | ((long) (b[1] & 0xff)) << 8 | ((long) (b[2] & 0xff)) << 16 | ((long) (b[3] & 0xff)) << 24 |
                ((long) (b[4] & 0xff)) << 32 | ((long) (b[5] & 0xff)) << 40 | ((long) (b[6] & 0xff)) << 48 | ((long) (b[7] & 0xff)) << 56;
    }

    /**
     * 将字节数组转换为有符号8位整型
     *
     * @param b
     * @return
     */
    public static int int8(byte[] b) {
        return (int) b[0];
    }

    /**
     * 将字节数组转换为有符号16位整型
     *
     * @param b
     * @return
     */
    public static int int16(byte[] b) {
        return (int) b[0] | ((int) b[1]) << 8;
    }

    /**
     * 将字节数组转换为有符号32位整型
     *
     * @param b
     * @return
     */
    public static int int32(byte[] b) {
        return ((int) b[0]) | ((int) b[1]) << 8 | ((int) b[2]) << 16 | ((int) b[3]) << 24;
    }

    /**
     * 将字节数组转换为有符号64位整型
     *
     * @param b
     * @return
     */
    public static long int64(byte[] b) {
        return (long) (b[0]) | ((long) (b[1])) << 8 | ((long) (b[2])) << 16 | ((long) (b[3])) << 24 |
                ((long) (b[4])) << 32 | ((long) (b[5])) << 40 | ((long) (b[6])) << 48 | ((long) (b[7])) << 56;
    }

    /**
     * 将整数转换为二进制数组
     *
     * @param l
     * @return
     */
    public static byte[] uint64Bytes(long l) {
        byte[] b = new byte[8];

       /* b[0] = (byte) (l & 0xff);
        b[1] = (byte) ((l >> 8) & 0xff);
        b[2] = (byte) ((l >> 16) & 0xff);
        b[3] = (byte) ((l >> 24) & 0xff);
        b[4] = (byte) ((l >> 32) & 0xff);
        b[5] = (byte) ((l >> 40) & 0xff);
        b[6] = (byte) ((l >> 48) & 0xff);
        b[7] = (byte) ((l >> 56) & 0xff);*/
        b[0] = (byte) l;
        b[1] = (byte) (l >> 8);
        b[2] = (byte) (l >> 16);
        b[3] = (byte) (l >> 24);
        b[4] = (byte) (l >> 32);
        b[5] = (byte) (l >> 40);
        b[6] = (byte) (l >> 48);
        b[7] = (byte) (l >> 56);
        return b;
    }

    /**
     * 将整数转换为little endian格式的二进制数组
     *
     * @param l
     * @return
     */
    public static byte[] uint16Bytes(long l) {
        byte[] b = new byte[2];
        b[0] = (byte) l;
        b[1] = (byte) (l >> 8);
        return b;
    }
}
