package com.shenyn.redis.test;


/**
 * Created by allan on 16/5/9.
 */
public class TestEndian {
    public static void main(String[] args) {
       /* byte[] data=new byte[]{0x00,0x05,0x68,0x65,0x6c,0x6c,0x6f,0x06,0x00*//**,(byte)0xf5,(byte)0x9f,(byte)0xb7,(byte)0xf6,(byte)0x90,0x61,0x1c,(byte)0x99**//*};
        long crc=CRC64.digest(data)&0xffffffffffffffffL;
        System.out.println(crc);
        byte[] a=LittleEndian.uint64Bytes(crc);
        for(byte b:a){
            int c=b&0xff;
            System.out.println(c);
        }*/
        byte a=(byte)300;
        System.out.println(a);
    }
}
