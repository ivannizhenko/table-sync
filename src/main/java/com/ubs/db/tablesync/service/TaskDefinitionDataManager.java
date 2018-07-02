package com.ubs.db.tablesync.service;

import com.ubs.db.tablesync.model.TaskDefinition;
import com.ubs.db.tablesync.persistence.MainTaskDefinitionRepository;
import com.ubs.db.tablesync.persistence.MirrorTaskDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Data manager facade for easier data manipulation within both main and mirror TaskDefinition repositories.
 */
@Service
public class TaskDefinitionDataManager implements DataManager<Long, TaskDefinition> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MainTaskDefinitionRepository mainRepository;

    @Autowired
    private MirrorTaskDefinitionRepository mirrorRepository;

    /**
     * Returns a list of all IDs from main repository.
     *
     * @return a list of task definition IDs
     */
    @Override
    public List<Long> mainIdList() {
        List<Long> ids = mainRepository.findAllIds();
        logger.debug("< Ids {} are loaded from Main", ids);
        return ids;
    }

    /**
     * Returns a list of all IDs from mirror repository.
     *
     * @return a list of task definition IDs
     */
    @Override
    public List<Long> mirrorIdList() {
        List<Long> ids = mirrorRepository.findAllIds();
        logger.debug("< Ids {} are loaded from Mirror", ids);
        return ids;
    }

    /**
     * Returns a task definition for particular ID from main repository.
     *
     * @param id task definition ID
     * @return a task definition object
     */
    @Override
    public TaskDefinition mainRow(Long id) {
        TaskDefinition task = mainRepository.findById(id);
        logger.debug("< Task {} is loaded from Main", task);
        return task;
    }

    /**
     * Returns a task definition for particular ID from mirror repository.
     *
     * @param id task definition ID
     * @return a task definition object
     */
    @Override
    public TaskDefinition mirrorRow(Long id) {
        TaskDefinition task = mirrorRepository.findById(id);
        logger.debug("< Task {} is loaded from Mirror", task);
        return task;
    }

    /**
     * Updates old task definition with new one in main repository.
     *
     * @param task task definition
     */
    @Override
    public void updateMainRow(TaskDefinition task) {
        mainRepository.update(task);
        logger.debug("> Task {} is updated in Main", task);
    }

    /**
     * Updates old task definition with new one in mirror repository.
     *
     * @param task task definition
     */
    @Override
    public void updateMirrorRow(TaskDefinition task) {
        mirrorRepository.update(task);
        logger.debug("> Task {} is updated in Mirror", task);
    }

    /**
     * Removes task definition from main repository.
     *
     * @param task task definition
     */
    @Override
    public void removeMainRow(TaskDefinition task) {
        mainRepository.deleteById(task.getId());
        logger.debug("- Task {} is removed from Main", task);
    }

    /**
     * Removes task definition from mirror repository.
     *
     * @param task task definition
     */
    @Override
    public void removeMirrorRow(TaskDefinition task) {
        mirrorRepository.deleteById(task.getId());
        logger.debug("- Task {} is removed from Mirror", task);
    }

    /**
     * Added task definition to main repository.
     *
     * @param task task definition
     */
    @Override
    public void addMainRow(TaskDefinition task) {
        mainRepository.insert(task);
        logger.debug("+ Task {} is added to Main", task);
    }

    /**
     * Added task definition to mirror repository.
     *
     * @param task task definition
     */
    @Override
    public void addMirrorRow(TaskDefinition task) {
        mirrorRepository.insert(task);
        logger.debug("+ Task {} is added to Mirror", task);
    }
}
