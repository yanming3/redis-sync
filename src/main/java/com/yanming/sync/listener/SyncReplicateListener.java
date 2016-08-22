package com.yanming.sync.listener;

import com.yanming.sync.client.NettyClient;
import com.yanming.sync.rdb.parser.RdbEntry;

import java.util.List;

/**
 * Created by allan on 16/5/11.
 */
public class SyncReplicateListener implements ReplicateListener {

    private int db = -1;

    private final String host;

    private final int port;

    private NettyClient client;

    public SyncReplicateListener(String host, int port) {
        this.host = host;
        this.port = port;
        client = new NettyClient(host, port);
        client.start();
    }

    @Override
    public void process(List<byte[]> cmdList) {
        byte[] cmd = cmdList.remove(0);
        client.exeCmd(cmd, cmdList);
    }

    @Override
    public void process(RdbEntry entry) {
        int oldDb = db;
        db = entry.getDb();
        if (oldDb != db) {
            client.select(db);
        }
        client.restore(entry.getKey(), entry.getExpire(), entry.getSerializedValue());
    }
}
