package com.yanming.sync.channel;

import com.yanming.sync.listener.ReplicateListener;
import com.yanming.sync.rdb.parser.RdbEntry;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class DispatchChannelHandler extends ChannelDuplexHandler {

    private final Logger logger = LoggerFactory.getLogger(DispatchChannelHandler.class);

    private ReplicateListener replicateListener;

    public DispatchChannelHandler(ReplicateListener replicateListener) {
        this.replicateListener = replicateListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof List) {
            List<byte[]> command = (List<byte[]>) msg;
            /*if (command.size() == 1) {//如果接受到服务器的PING命令,返回PONG响应
                if ("PING".equals(new String(command.get(0)))) {
                    ctx.channel().writeAndFlush(RedisRequestEncoder.encodeResponse(ctx.channel().alloc()));
                }
            }*/
            replicateListener.process(command);
        } else if (msg instanceof RdbEntry) {
            replicateListener.process((RdbEntry) msg);
        } else {
            logger.warn("未处理接受到的消息:{}", msg);
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Channel异常!", cause);
        ctx.close();
    }
}
