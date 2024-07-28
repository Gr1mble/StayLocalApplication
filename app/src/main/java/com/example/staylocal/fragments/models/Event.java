package com.example.staylocal.fragments.models;

import java.io.Serializable;

public class Event implements Serializable {
    String docId;
    String userId;
    String name;
    String description;
    String dates;
    String durration;

    public Event() {
    }

    public Event(String name, String description, String dates, String durration) {
        this.name = name;
        this.description = description;
        this.dates = dates;
        this.durration = durration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDurration() {
        return durration;
    }

    public void setDurration(String durration) {
        this.durration = durration;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }
}
