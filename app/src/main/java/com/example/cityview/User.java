package com.example.cityview;

public class User {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String joinedAt;

    public User(int userId, String fullName, String email, String phone, String joinedAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.joinedAt = joinedAt;
    }

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getJoinedAt() {
        return joinedAt;
    }
}
