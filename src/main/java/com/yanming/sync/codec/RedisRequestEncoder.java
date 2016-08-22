package com.yanming.sync.codec;


import java.util.List;

import com.yanming.sync.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * <p>按照<a href="http://redis.io/topics/protocol">Redis协议</a>,对Redis命令进行编码.</p>
 *
 * @author allan
 */
public class RedisRequestEncoder {

    private static final byte[] CRLF = new byte[]{'\r', '\n'};
    private static byte[] PONG = new byte[]{'P', 'O', 'N', 'G'};
    private final static int[] SIZE_TABLE = {
            9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE
    };

    // Requires positive x
    private static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= SIZE_TABLE[i]) {
                return i + 1;
            }
        }
    }

    private static int paramCountSize(int paramCount) {
        // * + paramCount + CRLF
        return 1 + stringSize(paramCount) + 2;
    }

    private static int paramSize(byte[] param) {
        // $ + paramLength + CRLF + param + CRLF
        return 1 + stringSize(param.length) + 2 + param.length + 2;
    }

    private static int serializedSize(byte[] cmd, byte[][] params, byte[]... otherParams) {
        int size = paramCountSize(1 + params.length + otherParams.length) + paramSize(cmd);
        for (byte[] param : params) {
            size += paramSize(param);
        }
        for (byte[] param : otherParams) {
            size += paramSize(param);
        }
        return size;
    }

    private static int serializedSize(byte[] cmd, List<byte[]> params) {
        int size = paramCountSize(1 + params.size()) + paramSize(cmd);
        for (byte[] param : params) {
            size += paramSize(param);
        }
        return size;
    }

    private static void writeParamCount(ByteBuf buf, int paramCount) {
        buf.writeByte('*').writeBytes(ByteUtils.toBytes(paramCount)).writeBytes(CRLF);
    }

    private static void writeParam(ByteBuf buf, byte[] param) {
        buf.writeByte('$').writeBytes(ByteUtils.toBytes(param.length)).writeBytes(CRLF).writeBytes(param)
                .writeBytes(CRLF);
    }

    public static ByteBuf encode(ByteBufAllocator alloc, byte[] cmd, byte[]... params) {
        int serializedSize = serializedSize(cmd, params);
        ByteBuf buf = alloc.buffer(serializedSize, serializedSize);
        writeParamCount(buf, params.length + 1);
        writeParam(buf, cmd);
        for (byte[] param : params) {
            writeParam(buf, param);
        }
        return buf;
    }

    public static ByteBuf encodeResponse(ByteBufAllocator alloc) {
        int serializedSize = 1 + 4 + 2;//+PONG\r\n
        ByteBuf buf = alloc.buffer(serializedSize, serializedSize);
        buf.writeByte('+');
        buf.writeBytes(PONG);
        buf.writeBytes(CRLF);
        return buf;
    }

    public static ByteBuf encode(ByteBufAllocator alloc, byte[] cmd, byte[][] headParams,
                                 byte[]... tailParams) {
        int serializedSize = serializedSize(cmd, headParams, tailParams);
        ByteBuf buf = alloc.buffer(serializedSize, serializedSize);
        writeParamCount(buf, headParams.length + tailParams.length + 1);
        writeParam(buf, cmd);
        for (byte[] param : headParams) {
            writeParam(buf, param);
        }
        for (byte[] param : tailParams) {
            writeParam(buf, param);
        }
        return buf;
    }

    public static ByteBuf encodeReverse(ByteBufAllocator alloc, byte[] cmd, byte[][] tailParams,
                                        byte[]... headParams) {
        return encode(alloc, cmd, headParams, tailParams);
    }

    public static ByteBuf encode(ByteBufAllocator alloc, byte[] cmd, List<byte[]> params) {
        int serializedSize = serializedSize(cmd, params);
        ByteBuf buf = alloc.buffer(serializedSize, serializedSize);
        writeParamCount(buf, params.size() + 1);
        writeParam(buf, cmd);
        for (byte[] param : params) {
            writeParam(buf, param);
        }
        return buf;
    }
}
