package com.ubs.db.tablesync.service;

/**
 * Implement this interface for custom HashService.
 *
 * @param <T> entity type, which is used for hashing
 */
public interface HashService<T> {

    String hash(T data);
}
