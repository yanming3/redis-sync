package com.yanming.sync.rdb.parser;

import com.alibaba.fastjson.JSON;
import com.yanming.sync.rdb.support.ValueType;

import java.io.Serializable;

/**
 * Created by allan on 16/5/9.
 */
public class RdbEntry implements Serializable {

    private static final long serialVersionUID = -4100295398165548630L;

    private int db;

    private byte[] key;

    private byte[] serializedValue;

    private Object value;

    private ValueType type = ValueType.UN_KNOWN;	/* redis数据类型 */

    private long expire; /* 过期时间 , milliseconds*/

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(byte[] serializedValue) {
        this.serializedValue = serializedValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
