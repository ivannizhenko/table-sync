package com.ubs.db.tablesync.service;

import java.util.List;

/**
 * Implement this interface for custom DataManager.
 *
 * @param <K> key (ID) type
 * @param <R> row type
 */
public interface DataManager<K, R> {

    List<K> mainIdList();

    List<K> mirrorIdList();

    R mainRow(K key);

    R mirrorRow(K key);

    void updateMainRow(R row);

    void updateMirrorRow(R row);

    void removeMainRow(R row);

    void removeMirrorRow(R row);

    void addMainRow(R row);

    void addMirrorRow(R row);
}
