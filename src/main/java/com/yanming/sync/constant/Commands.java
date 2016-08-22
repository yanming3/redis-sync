package com.yanming.sync.constant;

import java.nio.charset.StandardCharsets;


public enum Commands {
    APPEND, ASKING, AUTH, BGREWRITEAOF, BGSAVE, BITCOUNT, BITOP, BITPOS, BLPOP, BRPOP, BRPOPLPUSH,
    CLIENT, CLUSTER, CONFIG, DBSIZE, DEBUG, DECR, DECRBY, DEL, DISCARD, DUMP, ECHO, EVAL, EVALSHA,
    EXEC, EXISTS, EXPIRE, EXPIREAT, FLUSHALL, FLUSHDB, GET, GETBIT, GETRANGE, GETSET, HDEL,
    HEXISTS, HGET, HGETALL, HINCRBY, HINCRBYFLOAT, HKEYS, HLEN, HMGET, HMSET, HSCAN, HSET, HSETNX,
    HVALS, INCR, INCRBY, INCRBYFLOAT, INFO, KEYS, LASTSAVE, LINDEX, LINSERT, LLEN, LPOP, LPUSH,
    LPUSHX, LRANGE, LREM, LSET, LTRIM, MGET, MIGRATE, MONITOR, MOVE, MSET, MSETNX, MULTI, OBJECT,
    PERSIST, PEXPIRE, PEXPIREAT, PFADD, PFCOUNT, PFMERGE, PING, PSETEX, PSUBSCRIBE, PTTL, PUBLISH,
    PUBSUB, PUNSUBSCRIBE, QUIT, RANDOMKEY, RENAME, RENAMENX, RENAMEX, RESTORE, ROLE, RPOP,
    RPOPLPUSH, RPUSH, RPUSHX, SADD, SAVE, SCAN, SCARD, SCRIPT, SDIFF, SDIFFSTORE, SELECT, SENTINEL,
    SET, SETBIT, SETEX, SETNX, SETRANGE, SHUTDOWN, SINTER, SINTERSTORE, SISMEMBER, SLAVEOF,
    SLOWLOG, SMEMBERS, SMOVE, SORT, SPOP, SRANDMEMBER, SREM, SSCAN, STRLEN, SUBSCRIBE, SUBSTR,
    SUNION, SUNIONSTORE, SYNC, PSYNC, TIME, TTL, TYPE, UNSUBSCRIBE, UNWATCH, WAIT, WATCH, ZADD, ZCARD,
    ZCOUNT, ZINCRBY, ZINTERSTORE, ZLEXCOUNT, ZRANGE, ZRANGEBYLEX, ZRANGEBYSCORE, ZRANK, ZREM,
    ZREMRANGEBYLEX, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZREVRANGE, ZREVRANGEBYLEX, ZREVRANGEBYSCORE,
    ZREVRANK, ZSCAN, ZSCORE, ZUNIONSTORE;

    public final byte[] raw;

    Commands() {
        raw = name().getBytes(StandardCharsets.UTF_8);
    }

}
