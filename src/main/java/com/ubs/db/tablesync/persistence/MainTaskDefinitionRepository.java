package com.ubs.db.tablesync.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Main repository for TaskDefinition entity.
 */
@Repository
public class MainTaskDefinitionRepository extends BaseTaskDefinitionRepository {

    @Value("${tableSync.mainTableName}")
    private String mainTableName;

    @Override
    public String getTableName() {
        return mainTableName;
    }
}
