package com.yanming.sync.rdb.support;


/**
 * Created by allan on 16/5/9.
 */
public class Tuple<C> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 是否采用特殊编码
     */
    private boolean encoded;

    private C data;

    public Tuple(){

    }

    public Tuple(boolean success, boolean encoded, C data) {
        this.success = success;
        this.encoded = encoded;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    public C getData() {
        return data;
    }

    public void setData(C data) {
        this.data = data;
    }
}
