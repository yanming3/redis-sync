package com.yanming.sync.codec;

import com.yanming.sync.exception.RedisResponseException;
import com.yanming.sync.rdb.parser.RdbEntry;
import com.yanming.sync.rdb.parser.RdbParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class RedisResponseDecoder extends ByteToMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(RedisResponseDecoder.class);

    public static final Object NULL_REPLY = new Object();

    private void setReaderIndex(ByteBuf in, int index) {
        in.readerIndex(index == -1 ? in.writerIndex() : index + 1);
    }

    private String decodeString(ByteBuf in) throws ProtocolException {
        final StringBuilder buffer = new StringBuilder();
        final MutableBoolean reachCRLF = new MutableBoolean(false);
        setReaderIndex(in, in.forEachByte(new ByteBufProcessor() {

            @Override
            public boolean process(byte value) throws Exception {
                if (value == '\n') {
                    if ((byte) buffer.charAt(buffer.length() - 1) != '\r') {
                        throw new ProtocolException("Response is not ended by CRLF");
                    } else {
                        buffer.setLength(buffer.length() - 1);
                        reachCRLF.setValue(true);
                        return false;
                    }
                } else {
                    buffer.append((char) value);
                    return true;
                }
            }
        }));
        return reachCRLF.booleanValue() ? buffer.toString() : null;
    }

    private int toDigit(byte b) {
        return b - '0';
    }

    private Long decodeLong(ByteBuf in) throws ProtocolException {
        byte sign = in.readByte();
        final MutableLong l;
        boolean negative;
        if (sign == '-') {
            negative = true;
            l = new MutableLong(0);
        } else {
            negative = false;
            l = new MutableLong(toDigit(sign));
        }
        final MutableBoolean reachCR = new MutableBoolean(false);
        setReaderIndex(in, in.forEachByte(new ByteBufProcessor() {

            @Override
            public boolean process(byte value) throws Exception {
                if (value == '\r') {
                    reachCR.setValue(true);
                    return false;
                } else {
                    if (value >= '0' && value <= '9') {
                        l.setValue(l.longValue() * 10 + toDigit(value));
                    } else {
                        throw new ProtocolException("Response is not ended by CRLF");
                    }
                    return true;
                }
            }
        }));
        if (!reachCR.booleanValue()) {
            return null;
        }
        if (!in.isReadable()) {
            return null;
        }
        if (in.readByte() != '\n') {
            throw new ProtocolException("Response is not ended by CRLF");
        }
        return negative ? -l.longValue() : l.longValue();
    }

    private boolean decode(ByteBuf in, List<Object> out, Object nullValue) throws Exception {
        if (in.readableBytes() < 2) {
            return false;
        }
        byte b = in.readByte();
        switch (b) {
            case '+': {//Simple String
                String reply = decodeString(in);
                if (reply == null) {
                    return false;
                }
                out.add(reply);
                return true;
            }
            case '-': {//Errors
                String reply = decodeString(in);
                if (reply == null) {
                    return false;
                }
                out.add(new RedisResponseException(reply));
                return true;
            }
            case ':': {//Integers
                Long reply = decodeLong(in);
                if (reply == null) {
                    return false;
                }
                out.add(reply);
                return true;
            }
            case '$': {//Bulk Strings
                Long numBytes = decodeLong(in);
                if (numBytes == null) {
                    return false;
                }
                if (numBytes.intValue() == -1) {
                    out.add(nullValue);
                    return true;
                }
                if (in.readableBytes() < numBytes.intValue() + 2) {
                    return false;
                }
                if (numBytes.intValue() < 9 || !isRdbFormat(in)) {

                    if (in.getByte(in.readerIndex() + numBytes.intValue()) != '\r'
                            || in.getByte(in.readerIndex() + numBytes.intValue() + 1) != '\n') {
                        throw new ProtocolException("Response is not ended by CRLF");
                    }
                    byte[] reply = new byte[numBytes.intValue()];
                    in.readBytes(reply);
                    // skip CRLF
                    in.skipBytes(2);
                    out.add(reply);
                    return true;
                }

                RdbParser rdbParser = new RdbParser(in);
                rdbParser.header();
                RdbEntry entry = null;
                while ((entry = rdbParser.next()) != null) {
                    out.add(entry);
                }
                return true;
            }
            case '*': {//Arrays
                Long numReplies = decodeLong(in);
                if (numReplies == null) {
                    return false;
                }
                if (numReplies.intValue() == -1) {
                    out.add(nullValue);
                    return true;
                }
                List<Object> replies = new ArrayList<>();
                for (int i = 0; i < numReplies.intValue(); i++) {
                    if (!decode(in, replies, null)) {
                        return false;
                    }
                }
                out.add(replies);
                return true;
            }
            default:
                logger.warn("Unknown leading char:{},just ignore!", (char) b);
                return true;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (!decode(in, out, NULL_REPLY)) {
            in.resetReaderIndex();
        }
    }

    private boolean isRdbFormat(ByteBuf in) {
        byte[] header = new byte[9];
        in.getBytes(in.readerIndex(), header, 0, 9);
        if (!"REDIS".equals(new String(header, 0, 5))) {
            return false;
        }
        int version = Integer.parseInt(new String(header, 5, 4));
        if (version <= 0 || version > 6) {
            throw new RuntimeException(String.format("RDB文件版本号(%d)错误!", version));
        }
        return true;
    }
}
