package com.ubs.db.tablesync.model;

/**
 * TaskDefinition entity.
 */
public class TaskDefinition {

    private Long id;

    private String name;

    private String description;

    public TaskDefinition() {
    }

    public TaskDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public TaskDefinition(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Simple unique entity String representation, used for hash generation.
     *
     * @return string representation of entity
     */
    public String hashingString() {
        return id + "-" + name + "-" + description;
    }

    @Override
    public String toString() {
        return "TaskDefinition [name=" + this.name + ", description=" + this.description
                + "]";
    }
}
