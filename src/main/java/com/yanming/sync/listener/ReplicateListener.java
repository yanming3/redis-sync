package com.yanming.sync.listener;

import com.yanming.sync.rdb.parser.RdbEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by allan on 16/5/10.
 */
public interface ReplicateListener {

    void process(List<byte[]> cmdList);

    void process(RdbEntry entry);

    ReplicateListener DEFAULT = new ReplicateListener() {
        private Logger logger = LoggerFactory.getLogger(ReplicateListener.class);

        @Override
        public void process(List<byte[]> cmdList) {
            StringBuilder sb = new StringBuilder();
            for (byte[] b : cmdList) {
                sb.append(new String(b)).append(" ");
            }
            logger.debug("同步命令:{}", sb.toString());
        }

        @Override
        public void process(RdbEntry entry) {
            logger.debug("同步RDB Entry:{}", entry.toString());
        }
    };
}
