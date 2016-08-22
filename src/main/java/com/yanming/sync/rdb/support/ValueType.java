package com.yanming.sync.rdb.support;

/**
 * redis数据类型
 */
public enum ValueType {
    STRING(0),
    LIST(1),
    SET(2),
    ZSET(3),
    HASH(4),
    ZIP_MAP(9),
    ZIP_LIST(10),
    INT_SET(11),
    ZSET_ZIP_LIST(12),
    HASH_ZIP_LIST(13),
    UN_KNOWN(-1);

    private int type;

    private ValueType(int type) {
        this.type = type;
    }

    public static ValueType getByInt(int type) {
        for (ValueType t : ValueType.values()) {
            if (t.type == type) {
                return t;
            }
        }
        return UN_KNOWN;
    }
}
