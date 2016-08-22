package com.yanming.sync.rdb.parser;


import com.yanming.sync.codec.IntSet;
import com.yanming.sync.codec.LZFCompress;
import com.yanming.sync.codec.ZipList;
import com.yanming.sync.codec.ZipMap;
import com.yanming.sync.rdb.support.DecodedLength;
import com.yanming.sync.rdb.support.ReplayBuffer;
import com.yanming.sync.rdb.support.ValueType;
import com.yanming.sync.utils.ByteUtils;
import com.yanming.sync.utils.CRC64;
import com.yanming.sync.utils.LittleEndian;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by allan on 16/5/9.
 */
public class RdbParser {
    private final Logger logger = LoggerFactory.getLogger(RdbParser.class);

    private static final String MAGIC = "REDIS";
    /* Redis的特殊标示 */
    public static final int EXPIRE_MS = 252; /* 毫秒级过期时间,占用8个字节 */

    public static final int EXPIRE_SEC = 253;	/* 秒级过期时间 ,占用8个字节 */

    public static final int SELECT_DB = 0xFE;	/* 数据库 (后面紧接着的就是数据库编号) */

    public static final int TAIL = 0xFF;	/* 结束符 */

    /*
     * 表示长度的方式
     */
    public static final int LEN_6B = 0;
    public static final int LEN_14B = 1;
    public static final int LEN_32B = 2;
    public static final int LEN_VARIABLE = 3;

    /*
     * 整型数字的编码方式
     */
    public static final int ENC_INT8 = 0; /* 8 bit signed integer */

    public static final int ENC_INT16 = 1; /* 16 bit signed integer */

    public static final int ENC_INT32 = 2; /* 32 bit signed integer */

    public static final int ENC_LZF = 3; /* string compressed with LZF*/

    private ReplayBuffer replayBuffer = null;

    private int db;

    public RdbParser(ByteBuf data) {
        replayBuffer = new ReplayBuffer(data);
    }

    /**
     * Redis文件头部，必须以"REDIS"字符串开头
     **/
    public void header() {
        byte[] header = replayBuffer.readBytes(9);
        if (!MAGIC.equals(new String(header, 0, 5))) {
            throw new RuntimeException("Magic String校验失败,文件格式错误!");
        }
        int version = Integer.parseInt(new String(header, 5, 4));
        if (version <= 0 || version > 6) {
            throw new RuntimeException(String.format("RDB文件版本号(%d)错误!", version));
        }
    }

    /**
     * TODO:CRC 64 checksum of the entire file.
     *
     * @return
     */
    public void footer() {
        byte[] footer = replayBuffer.readBytes(8);
    }

    public RdbEntry next() {
        RdbEntry entry = new RdbEntry();
        while (true) {
            int t = replayBuffer.readByte() & 0xff;
            switch (t) {
                case SELECT_DB://切换DB
                    this.db = readLength();
                    if (db > 63) {
                        throw new RuntimeException();
                    }
                    break;
                case EXPIRE_SEC://失效时间,单位为秒
                    long ttlSecond = replayBuffer.readUint32();
                    entry.setExpire(ttlSecond * 1000);
                    break;
                case EXPIRE_MS://失效时间,单位为毫秒
                    long ttlMs = replayBuffer.readUint64();
                    entry.setExpire(ttlMs);
                    break;
                case TAIL:
                    return null;
                default:
                    ValueType vt = ValueType.getByInt(t);
                    if (vt == ValueType.UN_KNOWN) {
                        this.ERROR("Cann't recognize type %d!", t);
                    }
                    byte[] key = readStringBytes();
                    entry.setKey(key);

                    replayBuffer.startRecord();
                    Object javaObj = readObjectValue(vt);
                    byte[] values = replayBuffer.getReplayBytes();
                    replayBuffer.endRecord();

                    entry.setSerializedValue(createValueDump(key, (byte) t, values));
                    entry.setValue(javaObj);
                    entry.setDb(db);
                    entry.setType(vt);
                    return entry;
            }
        }

    }

    public int readLength() {
        DecodedLength r = readEncodedLength();
        int length = r.getLength();
        if (!r.isDecoded()) {
            throw new RuntimeException("encoded-length");
        }
        return length;
    }

    /**
     * 解析第一个字节，返回值表示此段数据占用字节的长度
     **/
    private DecodedLength readEncodedLength() {
        int u = (int) replayBuffer.readUint8();
        int length = u & 0x3F;
        int len = u >> 6;
        /**
         *  前两个bit表示编码类型
         */
        switch (u >> 6) {
            case LEN_6B://0,接下来的6bit表示长度
                return new DecodedLength(true, length);

            case LEN_14B://1,接下来的14bit表示长度
                u = (int) replayBuffer.readUint8();
                length = (length << 8) + u;
                return new DecodedLength(true, length);
            case LEN_32B://2,接下来的32bit表示长度
                length = replayBuffer.readUint32BigEndian();
                return new DecodedLength(true, length);

            case LEN_VARIABLE://3
                return new DecodedLength(false, length);

            default:
                throw new IllegalStateException("不支持的长度类型:" + len);
        }
    }

    private byte[] readStringBytes() {
        DecodedLength r = readEncodedLength();
        if (r.isDecoded()) {//如果未encoded
            return replayBuffer.readBytes(r.getLength());
        }
        int t = r.getLength();
        switch (t) {
            case ENC_LZF://3表示采用LZF压缩算法
                int encodedLen = readLength();
                int originalLen = readLength();
                return LZFCompress.expand(replayBuffer.readBytes(encodedLen), 0, 0, originalLen);
            case ENC_INT8://0表示用接下来的1个字节存储的整数做为字符串内容
                return String.valueOf(replayBuffer.readInt8()).getBytes();
            case ENC_INT16://1表示用接下来的2个字节存储的整数做为字符串内容
                return String.valueOf(replayBuffer.readInt16()).getBytes();
            case ENC_INT32://2表示用接下来的4个字节存储的整数做为字符串内容
                return String.valueOf(replayBuffer.readInt32()).getBytes();
            default:
                throw new RuntimeException(String.format("invalid encoded string %02x", t));
        }
    }


    /* double数据 **/
    private Double readScore() {
        Double val;

        byte[] lenArray = this.replayBuffer.readBytes(1);

        int len = (0xff & lenArray[0]);
        switch (len) {
            case 255:
                val = Double.NEGATIVE_INFINITY;
                return val;
            case 254:
                val = Double.POSITIVE_INFINITY;
                return val;
            case 253:
                val = Double.NaN;
                return val;
            default:
                byte[] buf = new byte[len + 1];
                byte[] d = replayBuffer.readBytes(len);
                System.arraycopy(d, 0, buf, 0, len);
                buf[len] = '\0';

                String str = "";
                try {
                    str = new String(buf, 0, len, "ASCII");
                } catch (UnsupportedEncodingException e) {
                    str = new String(buf, 0, len);
                }
                return Double.parseDouble(str);
        }
    }

    /*
     * 根据RDB中存储的Redis类型, 解析为相对应的Java数据类型
     * */
    private Object readObjectValue(ValueType t) {
        Object obj = null;
        switch (t) {
            case ZIP_MAP://Zipmap编码
                obj = new HashMap<>();
                byte[] hzBytes = readStringBytes();
                ZipMap zm = new ZipMap(hzBytes);
                while (zm.hasNext()) {
                    ((Map) obj).putAll(zm.next());
                }
                break;
            case ZIP_LIST://Ziplist编码
                obj = new ArrayList<>();
                byte[] lzBytes = readStringBytes();
                ZipList zipList = new ZipList(lzBytes);
                while (zipList.hasNext()) {
                    ((List) obj).add(zipList.next());
                }
                break;
            case ZSET_ZIP_LIST://Sorted set in Ziplist
                byte[] zzBytes = readStringBytes();
                obj = new TreeMap<>();
                ZipList zipList2 = new ZipList(zzBytes);
                while (zipList2.hasNext()) {
                    String k = zipList2.next();
                    if (zipList2.hasNext()) {
                        Double v = Double.valueOf(zipList2.next());
                        ((Map) obj).put(k, v);
                    }
                }
                break;
            case HASH_ZIP_LIST://Hash in Ziplist
                byte[] hzlBytes = readStringBytes();
                ZipList zipList3 = new ZipList(hzlBytes);
                obj = new HashMap<>();
                while (zipList3.hasNext()) {
                    String k = zipList3.next();
                    if (zipList3.hasNext()) {
                        String v = zipList3.next();
                        ((Map) obj).put(k, v);
                    }
                }
                obj = zipList3;
                break;
            case INT_SET://Intset编码,常用于存储元素都是整数的set
                obj = new ArrayList<>();
                byte[] siBytes = readStringBytes();
                IntSet intset = new IntSet(siBytes);
                while (intset.hasNext()) {
                    ((List) obj).add(intset.next());
                }
                break;
            case STRING:
                obj = ByteUtils.bytesToString(readStringBytes());
                break;
            case LIST:
            case SET:
                int nl = readLength();
                obj = new ArrayList<>();
                for (int i = 0; i < nl; i++) {
                    String val = ByteUtils.bytesToString(readStringBytes());
                    if (val == null) {
                        ERROR("Error reading element at index %d (length: %d)",
                                i, nl);
                    }
                    ((List) obj).add(val);
                }
                break;
            case ZSET:
                int nz = readLength();
                TreeMap<Double, String> zsetValues = new TreeMap<>();
                for (int i = 0; i < nz; i++) {
                    String v = ByteUtils.bytesToString(readStringBytes());
                    if (v == null) {
                        ERROR("Error reading element key at index %d (length: %d)",
                                i, nz);
                        return false;
                    }
                    Double score = readScore();
                    if (score == null) {
                        ERROR("Error reading element value at index %d (length: %d)",
                                i, nz);
                        return false;
                    }
                    zsetValues.put(score, v);
                }
                obj = zsetValues;
                break;

            case HASH:
                int nh = readLength();
                logger.debug("类型为HASH,entry数量为{}", nh);
                Map<String, String> mapValues = new HashMap<>();
                for (int i = 0; i < nh; i++) {
                    String k = ByteUtils.bytesToString(readStringBytes());
                    if (k == null) {
                        ERROR("Error reading element key at index %d (length: %d)",
                                i, nh);
                        return false;
                    }
                    String s = ByteUtils.bytesToString(readStringBytes());
                    if (s == null) {
                        ERROR("Error reading element value at index %d (length: %d)",
                                i, nh);
                        return false;
                    }
                    mapValues.put(k, s);
                }
                obj = mapValues;
                break;

            default:
                ERROR("Type not implemented");
        }
        return obj;
    }

    private byte[] createValueDump(byte[] key, byte t, byte[] value) {
        ByteBuf byteBuffer = Unpooled.buffer();
        byteBuffer.writeByte(t);
        byteBuffer.writeBytes(value);
        byteBuffer.writeBytes(LittleEndian.uint16Bytes(6L));

        byte[] result = new byte[byteBuffer.readableBytes()];
        byteBuffer.markReaderIndex();
        byteBuffer.readBytes(result, 0, byteBuffer.readableBytes());
        byteBuffer.resetReaderIndex();

        long crc = CRC64.digest(result);
        byteBuffer.writeBytes(LittleEndian.uint64Bytes(crc));
        result = new byte[byteBuffer.readableBytes()];
        byteBuffer.readBytes(result, 0, byteBuffer.readableBytes());
        byteBuffer.release();
        return result;
    }


    private static void ERROR(String msg, Object... args) {
        throw new RuntimeException(String.format(msg, args));
    }

}
