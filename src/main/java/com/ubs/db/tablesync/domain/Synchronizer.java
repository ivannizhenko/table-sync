package com.ubs.db.tablesync.domain;

import com.ubs.db.tablesync.service.ConflictHandler;
import com.ubs.db.tablesync.service.DataManager;
import com.ubs.db.tablesync.service.HashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Parametrized stateful synchronizer with basic logic. Can be extended to work with different tables, schemas and entities.
 *
 * @param <T> entity type, which is being compared to each other in order to keep both copies in sync
 */
abstract class Synchronizer<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    Map<Long, String> hashMap;

    abstract DataManager<Long, T> getDataManager();

    abstract HashService<T> getHashService();

    abstract ConflictHandler getConflictHandler();

    Synchronizer(Map<Long, String> hashMap) {
        this.hashMap = hashMap;
    }

    /**
     * Synchronizes two sources of data by comparing to stored hash of each row. Type of row, hashing method and
     * conflict resolution strategy are extracted to extending classes.
     */
    public void sync() {
        List<Long> mainIdList = getDataManager().mainIdList();
        List<Long> mirrorIdList = getDataManager().mirrorIdList();

        int mainIndex = 0;
        int mirrorIndex = 0;

        int maxSize = Math.max(mainIdList.size(), mirrorIdList.size());

        while (mainIndex < maxSize && mirrorIndex < maxSize) {

            Optional<Long> mainIdOpt = next(mainIdList, mainIndex);
            Optional<Long> mirrorIdOpt = next(mirrorIdList, mirrorIndex);

            if (mainIdOpt.isPresent() && mirrorIdOpt.isPresent()) {
                Long mainId = mainIdOpt.get();
                Long mirrorId = mirrorIdOpt.get();

                T mainRow = getDataManager().mainRow(mainId);
                T mirrorRow = getDataManager().mirrorRow(mirrorId);

                if (mainId.equals(mirrorId)) {

                    String mainCalcHash = calculateHash(mainRow);
                    String mirrorCalcHash = calculateHash(mirrorRow);
                    String storedHash = hashMap.get(mainId);

                    if (allHashesDifferent(mainCalcHash, mirrorCalcHash, storedHash)) {
                        getConflictHandler().handle(mainId);
                        hashMap.remove(mainId);
                        logger.error("! Row with ID: {} has conflict, please resolve it manually", mainId);

                    } else if (onlyMainHashMatchStored(mainCalcHash, mirrorCalcHash, storedHash)) {
                        getDataManager().updateMainRow(mirrorRow);
                        hashMap.put(mainId, mirrorCalcHash);
                        logger.debug("> {} is updated in Main", mirrorRow);

                    } else if (onlyMirrorHashMatchStored(mainCalcHash, mirrorCalcHash, storedHash)) {
                        getDataManager().updateMirrorRow(mainRow);
                        hashMap.put(mainId, mainCalcHash);
                        logger.debug("> {} is updated in Mirror", mainRow);
                    }
                    mainIndex++;
                    mirrorIndex++;

                } else {
                    if (mainId > mirrorId) {        //row removed from MAIN
                        getDataManager().removeMirrorRow(mirrorRow);
                        hashMap.remove(mirrorId);
                        logger.debug("- {} is removed from Mirror", mirrorRow);
                        mirrorIndex++;

                    } else {                        //row removed from MIRROR
                        getDataManager().removeMainRow(mainRow);
                        hashMap.remove(mainId);
                        logger.debug("- {} is removed from Main", mainRow);
                        mainIndex++;
                    }

                }

            } else if (mainIdOpt.isPresent()) {     //MIRROR is empty
                Long id = mainIdOpt.get();
                T mainRow = getDataManager().mainRow(id);
                syncRow(id, mainRow,
                        (r) -> {
                            getDataManager().removeMainRow(r);
                            logger.debug("- {} is removed from Main", mainRow);
                        },
                        (r) -> {
                            getDataManager().addMirrorRow(r);
                            logger.debug("+ {} is added to Mirror", mainRow);
                        });
                mainIndex++;

            } else if (mirrorIdOpt.isPresent()) {   //MAIN is empty
                Long id = mirrorIdOpt.get();
                T mirrorRow = getDataManager().mirrorRow(id);
                syncRow(id, mirrorRow,
                        (r) -> {
                            getDataManager().removeMirrorRow(r);
                            logger.debug("- {} is removed from Mirror", mirrorRow);
                        },
                        (r) -> {
                            getDataManager().addMainRow(r);
                            logger.debug("+ {} is added to Main", mirrorRow);
                        });
                mirrorIndex++;
            }
        }
    }

    int size() {
        return hashMap.size();
    }

    private void syncRow(Long id, T row, Consumer<T> onRemove, Consumer<T> onAdd) {
        String hash = calculateHash(row);
        String storedHash = hashMap.get(id);

        if (hash.equals(storedHash)) {
            onRemove.accept(row);
            hashMap.remove(id);
        } else {
            onAdd.accept(row);
            hashMap.put(id, hash);
        }
    }

    private boolean onlyMirrorHashMatchStored(String mainCalcHash, String mirrorCalcHash, String storedHash) {
        return !mainCalcHash.equals(storedHash) && mirrorCalcHash.equals(storedHash);
    }

    private boolean onlyMainHashMatchStored(String mainCalcHash, String mirrorCalcHash, String storedHash) {
        return mainCalcHash.equals(storedHash) && !mirrorCalcHash.equals(storedHash);
    }

    private boolean allHashesDifferent(String mainCalcHash, String mirrorCalcHash, String storedHash) {
        return !mainCalcHash.equals(storedHash) && !mirrorCalcHash.equals(storedHash);
    }

    private Optional<Long> next(List<Long> list, int index) {
        if (index < list.size()) {
            return Optional.of(list.get(index));
        }
        return Optional.empty();
    }

    private String calculateHash(T row) {
        return getHashService().hash(row);
    }

}
