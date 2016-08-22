package com.yanming.sync.codec;

import com.yanming.sync.utils.BigEndian;
import com.yanming.sync.utils.LittleEndian;
import com.yanming.sync.utils.ByteUtils;

import java.util.*;

/**
 * <p>ZipList是序列化的List,编码结构如下:</p>
 * &lt;zlbytes&gt;&lt;zltail&gt;&lt;zllen&gt;&lt;entry&gt;&lt;entry&gt;&lt;zlend&gt;
 * <ul>
 * <li>zlbytes :<p>4字节的无符号整数,采用little endian格式,表示整个ZipList占用的字节数;</p></li>
 * <li>zltail : <p>4字节的无符号整数,采用little endian格式; 表示最后一个entry在整个ZipList中的偏移量;</p></li>
 * <li>zllen : <p>2字节的无符号整数,采用little endian格式; 表示entry的数量</p></li>
 * <li>entry : <p>ZipList中的元素,具体格式见下文;</p></li>
 * <li>zlend : <p>0xFF,结束区分符;</p></li>
 * </ul>
 * <p>Entry编码格式:</p>
   <p>
 * &lt;length-prev-entry&gt;&lt;special-flag&gt;&lt;raw-bytes-of-entry&gt;
 * </p>
 * <ul>
 * <li>length-prev-entry : 前一个entry的长度, 如果为0,表示第一个entry.  长度可能为1或5个字节s.
 * 如果第一个字节小于等于253, 用该字节表示Entry长度. 如果是254, 用接下来的4个字节存储Entry长度.
 * </li>
 * <li>Special flag : 表明entry的类型和长度.
 * The various encodings of this flag are shown below :
 * <p/>
 * |00pppppp| – 1 byte : String value with length less than or equal to 63 bytes (6 bits).
 * |01pppppp|qqqqqqqq| – 2 bytes : String value with length less than or equal to 16383 bytes (14 bits).
 * |10______|qqqqqqqq|rrrrrrrr|ssssssss|tttttttt| – 5 bytes : String value with length greater than or equal to 16384 bytes.
 * |1100____| – Read next 2 bytes as a 16 bit signed integer
 * |1101____| – Read next 4 bytes as a 32 bit signed integer
 * |1110____| – Read next 8 bytes as a 64 bit signed integer
 * |11110000| – Read next 3 bytes as a 24 bit signed integer
 * |11111110| – Read next byte as an 8 bit signed integer
 * |1111xxxx| – (with xxxx between 0000 and 1101) immediate 4 bit integer. Unsigned integer from 0 to 12. The encoded value is actually from 1 to 13 because 0000 and 1111 can not be used, so 1 should be subtracted from the encoded 4 bit value to obtain the right value.
 * </li>
 * <li>Raw Bytes : entry的内容.
 * </li>
 * </ul>
 * <p>
 * 关于RDB的格式,具体请参考<a href="https://github.com/sripathikrishnan/redis-rdb-tools/wiki/Redis-RDB-Dump-File-Format">RDB格式</a>
 * </p>
 */
public class ZipList implements Iterator<String> {
    public static final int ZIPLIST_PREV_ENTRY_LENGTH = 254;
    public static final int ZIPLIST_END = 255;    //zip list结束符

    public static final int FLAG_6B = 0;    //6位用于计数
    public static final int FLAG_14B = 1;
    public static final int FLAG_5BYTE = 2;    //5字节用于计数

    public static final int FLAG_INT16 = 0xc0;    //后面2字节的无符号整数就是entry值
    public static final int FLAG_INT32 = 0xd0;
    public static final int FLAG_INT64 = 0xe0;
    public static final int FLAG_INT24 = 0xf0;
    public static final int FLAG_INT8 = 0xfe;
    public static final int FLAG_INT4 = 15;


    private final byte[] data; //zip list数据

    private int index; //byte数组下标

    private int length;//entry数量

    public ZipList(byte[] origianlBytes) {
        this.data = origianlBytes;
        this.length = LittleEndian.int16(Arrays.copyOfRange(origianlBytes, 8, 10));
        /*
         * 从第11个字节开始，跳过前面10个字节，其中前4个字节表示ziplist的长度，
		 * 后4个字节表示最后一个entry在ziplist中的相对偏移量
		 * */
        this.index = 10;
    }

    /*
     * 占1或5个字节,表示前一个entry的字节长度，第一个entry是0
     * 如果第一个字节整型值等于254,则后面的4个字节表示长度,
     * 否则第一个字节整型值就是长度
     * */
    private void readPrevLength() {
        int len = data[index++];
        if (len < ZIPLIST_PREV_ENTRY_LENGTH) {
            index++;
        } else {
            index += 4;
        }
    }


    /**
     * special flag,占用字节数1到9之间, 用于表示entry数据占的字节长度或entry的整型值
     *
     * @return Integer[] [entry data bytes length , entry integer value]
     */
    private String readEntry() {
        int header = data[index++] & 0xff;
        String result = null;
        /**
         * String类型
         */
        int strLen = 0;
        switch ((header & 0xC0) >> 6) {
            case FLAG_6B:
                strLen = header & 0x3F;
                break;

            case FLAG_14B:
                strLen = (header & 0x3f) << 8 | (data[index++] & 0xff);
                break;
            case FLAG_5BYTE:
                byte[] b = Arrays.copyOfRange(data, index, index + 4);
                index += 4;
                strLen = BigEndian.uint32(b);
                break;
        }
        if (strLen != 0) {
            result = ByteUtils.bytesToString(Arrays.copyOfRange(data, index, index + strLen));
            index += strLen;
            return result;
        }
        //Integer
        switch (header) {
            case FLAG_INT16:
                long i16 = LittleEndian.uint16(Arrays.copyOfRange(data, index, index + 2));
                result = String.valueOf(i16);
                index += 2;
                break;
            case FLAG_INT32:
                long i32 = LittleEndian.uint32(Arrays.copyOfRange(data, index, index + 4));
                result = String.valueOf(i32);
                index += 4;
                break;
            case FLAG_INT64:
                long i64 = (LittleEndian.uint64(Arrays.copyOfRange(data, index, index + 8)));
                result = String.valueOf(i64);
                index += 8;
                break;
            case FLAG_INT24:
                long i24 = LittleEndian.uint32(Arrays.copyOfRange(data, index, index + 4)) >> 8;
                result = String.valueOf(i24);
                index += 4;
                break;
            case FLAG_INT8:
                long i8 = LittleEndian.uint8(Arrays.copyOfRange(data, index, index + 1));
                result = String.valueOf(i8);
                index += 1;
                break;

            default:
                if ((header & 0x00f0) >> 4 == FLAG_INT4) {
                    long io = (header & 0x0f) - 1;
                    result = String.valueOf(io);
                } else {
                    throw new RuntimeException("unknown ziplist header : " + header);
                }
        }
        return result;
    }


    @Override
    public boolean hasNext() {
        return (data[index] & 0xff) == ZIPLIST_END;
    }


    @Override
    public String next() {
        readPrevLength();
        return readEntry();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("接口未实现!");
    }
}
