package com.kiszka.kiddify.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskData {
    @PrimaryKey
    private int taskId;
    private String title;
    private String description;
    private String taskStart;
    private String taskEnd;
    private String status;
    private String note;
    private int parentId;

    public TaskData() {
    }
    public TaskData(int taskId, String title, String description, String taskStart, String taskEnd, String status, String note, int parentId) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.taskStart = taskStart;
        this.taskEnd = taskEnd;
        this.status = status;
        this.note = note;
        this.parentId = parentId;
    }
    public int getTaskId() {
        return taskId;
    }
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTaskEnd() {
        return taskEnd;
    }
    public void setTaskEnd(String taskEnd) {
        this.taskEnd = taskEnd;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getTaskStart() {
        return taskStart;
    }
    public void setTaskStart(String taskStart) {
        this.taskStart = taskStart;
    }
    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public int getParentId() {
        return parentId;
    }
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}