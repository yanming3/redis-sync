package com.yanming.sync.rdb.support;

import com.yanming.sync.utils.LittleEndian;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by allan on 16/5/11.
 */
public class ReplayBuffer {

    private ByteBuf byteBuffer;

    private ByteBuf replayBuffer;

    private AtomicBoolean record = new AtomicBoolean(false);

    public ReplayBuffer(ByteBuf byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.replayBuffer = Unpooled.directBuffer(512,100*1024*1024);
    }


    private void readFull(byte[] buf, int start, int num) {
        byteBuffer.readBytes(buf, start, num);
        if (record.get()) {
            replayBuffer.writeBytes(buf);
        }
    }

    public byte[] readBytes(int n) {
        byte[] b = new byte[n];
        readFull(b, 0, n);
        return b;
    }

    public byte readByte() {
        byte[] buf = new byte[1];
        readFull(buf, 0, 1);
        return buf[0];
    }

    public long readUint8() {
        byte u = readByte();
        return LittleEndian.uint8(new byte[]{u});
    }

    public int readInt8() {
        byte[] b = new byte[1];
        readFull(b, 0, 1);
        return LittleEndian.int8(b);
    }

    public int readInt16() {
        byte[] b = new byte[2];
        readFull(b, 0, 2);
        return LittleEndian.int16(b);
    }

    public int readInt32() {
        byte[] b = new byte[4];
        readFull(b, 0, 4);
        return LittleEndian.int32(b);
    }

    public long readUint32() {
        byte[] b = new byte[4];
        readFull(b, 0, 4);
        return LittleEndian.uint32(b);
    }

    public int readUint32BigEndian() {
        byte[] b = new byte[4];
        readFull(b, 0, 4);
        return (b[3] & 0xff) | (b[2] & 0xff) << 8 | (b[1] & 0xff) << 16 | (b[0] & 0xff) << 24;
    }

    public long readUint64() {
        byte[] b = new byte[8];
        readFull(b, 0, 8);
        return LittleEndian.uint64(b);
    }

    public void startRecord() {
        if (!record.compareAndSet(false, true)) {
            throw new RuntimeException("请确认是否已经打开记录功能!");
        }
        replayBuffer.clear();
    }

    public void endRecord() {
        if (!record.compareAndSet(true, false)) {
            throw new RuntimeException("请确认是否已经关闭记录功能!");
        }
        replayBuffer.clear();
    }

    public byte[] getReplayBytes() {
        int capacity = replayBuffer.readableBytes();
        byte[] result = new byte[capacity];
        replayBuffer.readBytes(result, 0, capacity);
        return result;
    }

    public void release(){
        if(replayBuffer!=null){
            replayBuffer.release();
        }
    }
}
