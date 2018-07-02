package com.ubs.db.tablesync.persistence;

import com.ubs.db.tablesync.model.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Base repository class for TaskDefinition entity.
 */
public abstract class BaseTaskDefinitionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public abstract String getTableName();

    public TaskDefinition findById(long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM " + getTableName() + " WHERE id=?", new Object[]{id},
                new BeanPropertyRowMapper<>(TaskDefinition.class));
    }

    public TaskDefinition findByNameAndDescription(TaskDefinition task) {
        return jdbcTemplate.queryForObject("SELECT * FROM " + getTableName() + " WHERE name=? AND description=?",
                new Object[]{task.getName(), task.getDescription()},
                new BeanPropertyRowMapper<>(TaskDefinition.class));
    }

    public List<Long> findAllIds() {
        return jdbcTemplate.query("SELECT id FROM " + getTableName() + " ORDER BY id ASC",
                (rs, i) -> rs.getLong("id"));
    }

    public int insert(TaskDefinition task) {
        return jdbcTemplate.update("INSERT INTO " + getTableName() + " (name, description) " + "values(?, ?)",
                task.getName(), task.getDescription());
    }

    public int update(TaskDefinition task) {
        return jdbcTemplate.update("UPDATE " + getTableName() + " SET name = ?, description = ? " + " where id = ?",
                task.getName(), task.getDescription(), task.getId());
    }

    public int deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM " + getTableName() + " where id=?", id);
    }
}
