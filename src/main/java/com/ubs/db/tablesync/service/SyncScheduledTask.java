package com.ubs.db.tablesync.service;

import com.ubs.db.tablesync.domain.TaskDefinitionSynchronizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple scheduled task for launching table synchronization.
 */
@Component
public class SyncScheduledTask {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TaskDefinitionSynchronizer synchronizer;

    @Transactional
    @Scheduled(fixedRateString = "${tableSync.scheduledJob.fixedRate.inMillis}")
    public void sync() {
        logger.info("--- Sync operation started ---");

        synchronizer.sync();

        logger.info("--- Sync operation stopped ---");
    }
}
