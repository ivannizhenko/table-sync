package com.ubs.db.tablesync.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Conflict manager for merge conflict situation. As of now just logs ID of rows in conflict.
 */
@Service
public class ConflictHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void handle(Long id) {
        //just log for now
        logger.error("! Row with ID: {} has conflict, please resolve it manually", id);
    }
}
