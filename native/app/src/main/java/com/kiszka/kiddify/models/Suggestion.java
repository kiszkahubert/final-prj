package com.kiszka.kiddify.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "suggestions")
public class Suggestion {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String proposedStart;
    private String proposedEnd;
    private String status;
    private String createdAt;
    private String reviewedAt;
    private Integer reviewedById;
    private int createdById;

    public Suggestion() {}

    public Suggestion(String title, String description, String proposedStart, String proposedEnd, String status, String createdAt, int createdById) {
        this.title = title;
        this.description = description;
        this.proposedStart = proposedStart;
        this.proposedEnd = proposedEnd;
        this.status = status;
        this.createdAt = createdAt;
        this.reviewedAt = null;
        this.reviewedById = null;
        this.createdById = createdById;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getProposedStart() {
        return proposedStart;
    }

    public void setProposedStart(String proposedStart) {
        this.proposedStart = proposedStart;
    }

    public String getProposedEnd() {
        return proposedEnd;
    }

    public void setProposedEnd(String proposedEnd) {
        this.proposedEnd = proposedEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Integer getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Integer reviewedById) {
        this.reviewedById = reviewedById;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }
}