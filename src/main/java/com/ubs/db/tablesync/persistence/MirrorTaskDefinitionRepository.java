package com.ubs.db.tablesync.persistence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Mirror repository for TaskDefinition entity.
 */
@Repository
public class MirrorTaskDefinitionRepository extends BaseTaskDefinitionRepository {

    @Value("${tableSync.mirrorTableName}")
    private String mirrorTableName;

    @Override
    public String getTableName() {
        return mirrorTableName;
    }
}
