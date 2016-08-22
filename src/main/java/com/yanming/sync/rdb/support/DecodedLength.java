package com.yanming.sync.rdb.support;

/**
 * Created by allan on 16/8/22.
 */
public class DecodedLength {
    private boolean decoded;
    private int length;

    public DecodedLength(boolean decoded, int length) {
        this.decoded = decoded;
        this.length = length;
    }

    public boolean isDecoded() {
        return decoded;
    }

    public void setDecoded(boolean decoded) {
        this.decoded = decoded;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
