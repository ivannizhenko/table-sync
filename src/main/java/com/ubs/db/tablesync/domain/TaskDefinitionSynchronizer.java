package com.ubs.db.tablesync.domain;

import com.ubs.db.tablesync.model.TaskDefinition;
import com.ubs.db.tablesync.service.ConflictHandler;
import com.ubs.db.tablesync.service.DataManager;
import com.ubs.db.tablesync.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Synchronizer for TaskDefinition entity. Exists to map specific HashService, DataManager and ConflictHandler
 * to base synchronization logic.
 */
@Component
public class TaskDefinitionSynchronizer extends Synchronizer<TaskDefinition> {

    @Autowired
    private HashService<TaskDefinition> hashService;

    @Autowired
    private DataManager<Long, TaskDefinition> dataManager;

    @Autowired
    private ConflictHandler conflictHandler;

    public TaskDefinitionSynchronizer() {
        super(new ConcurrentHashMap<>());
    }

    @Override
    DataManager<Long, TaskDefinition> getDataManager() {
        return this.dataManager;
    }

    @Override
    HashService<TaskDefinition> getHashService() {
        return this.hashService;
    }

    @Override
    ConflictHandler getConflictHandler() {
        return this.conflictHandler;
    }
}
