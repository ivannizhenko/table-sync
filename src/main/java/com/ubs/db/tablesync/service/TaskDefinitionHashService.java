package com.ubs.db.tablesync.service;

import com.ubs.db.tablesync.model.TaskDefinition;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Hashing service for TaskDefinition. Uses MD5 to calculate a hash.
 */
@Service
public class TaskDefinitionHashService implements HashService<TaskDefinition> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String hash(TaskDefinition task) {
        String hash = DigestUtils.md5Hex(task.hashingString());
        logger.debug("[{}] hash is generated for {}", hash, task);
        return hash;
    }
}
