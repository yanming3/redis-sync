package com.yanming.sync.client;

import com.yanming.sync.channel.DispatchChannelHandler;
import com.yanming.sync.codec.RedisRequestEncoder;
import com.yanming.sync.codec.RedisResponseDecoder;
import com.yanming.sync.listener.ReplicateListener;
import com.yanming.sync.utils.ByteUtils;
import com.yanming.sync.constant.Commands;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by allan on 16/5/10.
 */
public class NettyClient {
    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private String host;

    private int port;

    private Channel channel;

    private final NioEventLoopGroup group = new NioEventLoopGroup(1);

    private final ReplicateListener replicateListener;

    public NettyClient(String host, int port, ReplicateListener replicateListener) {
        this.host = host;
        this.port = port;
        if (replicateListener == null) {
            logger.warn("未注册同步Listener,默认使用Echo!");
            this.replicateListener = ReplicateListener.DEFAULT;
        } else {
            this.replicateListener = replicateListener;
        }
    }

    public NettyClient(String host, int port) {
        this(host, port, null);
    }

    public void start() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("logger", new LoggingHandler());
                            socketChannel.pipeline().addLast("codec", new RedisResponseDecoder());
                            socketChannel.pipeline().addLast("sync", new DispatchChannelHandler(replicateListener));
                        }
                    }
            );
            ChannelFuture future = b.connect(host, port).sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            logger.error("Interrupted exception!", e);
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        group.shutdownGracefully();
    }


    /**
     * 调用select命令
     *
     * @param db db序号,从0开始
     */
    public void select(int db) {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), Commands.SELECT.raw, ByteUtils.toBytes(db)));
    }

    public void auth(String account) {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), Commands.AUTH.raw, account.getBytes()));
    }

    /**
     * 调用同步命令
     */
    public void sync() {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), Commands.SYNC.raw));
    }

    /**
     * 增量同步,命令格式为:PSYNC runid offset
     * @param runId 如果为?,表示不限定master
     * @param offset -1表示全量复制
     */
    public void psync(String runId,int offset) {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), Commands.PSYNC.raw, ByteUtils.toBytes(runId), ByteUtils.toBytes(offset)));
    }

    /**
     * 调用restore命令
     *
     * @param key
     * @param ttl
     * @param serializedValue
     */
    public void restore(byte[] key, long ttl, byte[] serializedValue) {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), Commands.RESTORE.raw, key, ByteUtils.toBytes(ttl), serializedValue));
    }

    /**
     * 调用命令
     *
     * @param cmd    命令
     * @param params 命令参数
     */
    public void exeCmd(byte[] cmd, List<byte[]> params) {
        channel.writeAndFlush(RedisRequestEncoder.encode(channel.alloc(), cmd, params));
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
