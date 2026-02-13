package com.example.cityview;

public class NotificationModel {

    private int id;
    private String message;
    private String status;
    private String createdAt;

    public NotificationModel(int id, String message, String status, String createdAt) {
        this.id = id;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
