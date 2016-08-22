package com.yanming.sync.codec;

import com.yanming.sync.utils.LittleEndian;

import java.util.Arrays;
import java.util.Iterator;

/**
 * <p>当SET的所有元素都是整数时,可以采用INTSET编码,最大支持64bit整数;</p>
 * <p>
 * 编码格式:&lt;encoding&gt;&lt;length-of-contents&gt;&lt;contents&gt;
 * </p>
 * <ul>
 * <li> encoding : <p>32位无符号整数. 可选值为 2, 4 or 8,表示一个整数占用的存储字节数; 用两个bit位即可表示,目前encoding有点浪费.</p></li>
 * <li>length-of-contents :<p>32位无符号整数,表明SET中的元素个数</p></li>
 * <li>contents : <p>SET元素的二进制内容</p></li>
 * <ii>更多,请参考<a href="https://github.com/sripathikrishnan/redis-rdb-tools/wiki/Redis-RDB-Dump-File-Format">RDB Format</a></ii>
 * </ul>
 */
public final class IntSet implements Iterator<Integer> {
    private byte[] data;

    private int length;//整数数组长度

    private int bytesPerNum;//每个整数占用的字节数,2\4\8个字节

    private int index = 0;

    /**
     * 构造函数
     *
     * @param originalBytes 已编码的字节数组
     */
    public IntSet(byte[] originalBytes) {
        this.bytesPerNum = LittleEndian.int32(originalBytes);
        this.length = LittleEndian.int32(Arrays.copyOfRange(originalBytes, 4, 8));
        this.data = Arrays.copyOfRange(originalBytes, 8, originalBytes.length);
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public Integer next() {
        Integer result = null;
        switch (bytesPerNum) {
            case 2:
                result = (int) LittleEndian.uint16(Arrays.copyOfRange(data, 2 * index, 2 * index + 2));
                break;
            case 4:
                result = (int) LittleEndian.uint32(Arrays.copyOfRange(data, 4 * index, 4 * index + 4));
                break;
            case 8:
                result = (int) LittleEndian.uint64(Arrays.copyOfRange(data, 8 * index, 8 * index + 8));
                break;
            default:
                return null;
        }
        index += 1;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("未实现remove方法!");
    }
}
