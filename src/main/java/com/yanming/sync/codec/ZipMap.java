package com.yanming.sync.codec;


import com.yanming.sync.utils.BigEndian;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * 自redis2.6版本开始,官方不建立再使用ZipMap编码,而建议采用ZipList编码;
 * ZipMap是一个被序列化为字符串的hasmap,按照key-value对的方式存储数据;
 * <p/>
 * <p>
 * 编码结构如下:
 * &lt;zmlen&gt;&lt;len&gt;"foo"&lt;len&gt;&lt;free&gt;"bar"&lt;len&gt;"hello"&lt;len&gt;&lt;free&gt;"world"&lt;zmend&gt;
 * </p>
 * <li>zmlen : <p>1个字节,保存hashmap的大小. 要注意的是,如果大于等于254,则该字段不起作用,必须要迭代整个字符串之后才能得到hashmap的大小.</p></li>
 * <li>len : <p>紧跟着的字符串的长度, 要么是key,要么是value. 占用是1或5个字节的存储空间;
 * 如果第一个字节是0-252之间, 直接使用该长度.如果是 253, 接下来的4个字节的无符号整数表示长度;
 * .     第一个字节的无符号整数不能为254或255;</p>
 * </li>
 * <li>free : <p>1个字节, 空余的字节数. 例如, 如果value从“America”更新到“USA”,那么free为4.</p></li>
 * <li>zmend :<p> 0xFF,结束标识符.</p></li>
 * <ii>更多,请参考<a href="https://github.com/sripathikrishnan/redis-rdb-tools/wiki/Redis-RDB-Dump-File-Format">RDB Format</a></ii>
 */
public class ZipMap implements Iterator<Map<String, String>> {
    public static final int ZIPMAP_END = 255;    //zipMap结束符

    private byte[] data;

    private int position;

    public ZipMap(byte[] originalData) {
        this.data = originalData;
        this.position = 1;
    }


    private String readString(boolean readFree) {
        int free = 0;
        int len = data[position++] & 0xff;
        switch (len) {
            case 253:
                byte[] s = Arrays.copyOfRange(data, position, position + 5);
                position += 4;
                len = BigEndian.uint32(s);
                break;
            case 254:
            case 255:
                throw new IllegalArgumentException("长度参数错误!");
        }
        if (readFree) {
            free = data[position] & 0xff;
            position++;
        }
        String key = new String(data, position, len);
        position += free;
        return key;
    }


    @Override
    public boolean hasNext() {
        return (data[position] & 0xff) == ZIPMAP_END;
    }

    @Override
    public Map<String, String> next() {
        Map<String, String> result = new HashMap<>();
        String key = readString(false);
        String value = readString(true);
        result.put(key, value);
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("未实现remove方法!");
    }
}
