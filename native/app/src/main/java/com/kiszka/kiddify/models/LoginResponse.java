package com.kiszka.kiddify.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class LoginResponse {
    private String token;
    private long expiresIn;
    private Kid kid;
    private List<TaskData> tasks;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Kid getKid() {
        return kid;
    }

    public void setKid(Kid kid) {
        this.kid = kid;
    }

    public List<TaskData> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskData> tasks) {
        this.tasks = tasks;
    }
}
