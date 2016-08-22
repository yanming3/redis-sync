package com.shenyn.redis.test;


import com.yanming.sync.rdb.parser.RdbEntry;
import com.yanming.sync.rdb.parser.RdbParser;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by allan on 16/5/9.
 */
public class TestRdb {
    public static void main(String[] args) throws Exception {
        final AtomicInteger count = new AtomicInteger();

        String input = "/Users/allan/Works/backend/cute-framework-parent/redis-sync/src/test/resources";
        if (args.length == 1) {
            input = args[0];
        }
        File root=new File(input);
        for(File p:root.listFiles()) {
                System.out.println("开始处理文件:"+p.getName()+"!");
                RandomAccessFile f = new RandomAccessFile(p, "r");
                FileChannel channel = f.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                buffer.flip();

                RdbParser rdb = new RdbParser(Unpooled.wrappedBuffer(buffer));
                rdb.header();
                RdbEntry entry = rdb.next();
                while (entry != null) {
                    System.out.println(entry);
                    count.incrementAndGet();
                    entry = rdb.next();
                }
                buffer.clear();
                System.out.println("total keys : " + count.get());
        }
    }
}
