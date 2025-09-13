package com.kiszka.kiddify.models;

public class Task {
    private int taskId;
    private String title;
    private String timeRange;
    private int iconResource;
    private int iconColor;
    private String status;
    private String description;
    private String note;
    public Task(int taskId, String title, String timeRange, int iconResource, int iconColor, String status) {
        this.taskId = taskId;
        this.title = title;
        this.timeRange = timeRange;
        this.iconResource = iconResource;
        this.iconColor = iconColor;
        this.status = status;
    }
    public Task(TaskData taskData, int iconResource, int iconColor) {
        this.taskId = taskData.getTaskId();
        this.title = taskData.getTitle();
        this.timeRange = formatTimeRange(taskData.getTaskStart(), taskData.getTaskEnd());
        this.iconResource = iconResource;
        this.iconColor = iconColor;
        this.status = taskData.getStatus();
        this.description = taskData.getDescription();
        this.note = taskData.getNote();
    }
    private String formatTimeRange(String start, String end) {
        if (start != null && end != null) {
            try {
                String startTime = start.substring(11, 16);
                String endTime = end.substring(11, 16);
                return startTime + " - " + endTime;
            } catch (Exception e) {
                return "Cały dzień";
            }
        }
        return "Cały dzień";
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

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
