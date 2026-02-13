package com.example.cityview;

public class Message {
    private final String text;
    private final boolean isSentByUser;

    public Message(String text, boolean isSentByUser) {
        this.text = text;
        this.isSentByUser = isSentByUser;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }
}

