package com.kiszka.kiddify.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "media")
public class Media {
    @PrimaryKey
    @SerializedName("mediaId")
    private int id;
    @SerializedName("mediaType")
    private String type;
    private String url;
    private String uploadedAt;
    @SerializedName("uploadByUsername")
    private String uploadedByUsername;

    public String getAndroidUrl() {
        if (url != null && url.contains("localhost")) {
            return url.replace("localhost", "192.168.100.134");
        }
        return url;
    }
    public Media(int id, String type, String url, String uploadedAt, String uploadedByUsername) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.uploadedAt = uploadedAt;
        this.uploadedByUsername = uploadedByUsername;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUploadedAt() {
        return uploadedAt;
    }
    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    public String getUploadedByUsername() {
        return uploadedByUsername;
    }
    public void setUploadedByUsername(String uploadedByUsername) {
        this.uploadedByUsername = uploadedByUsername;
    }
}
