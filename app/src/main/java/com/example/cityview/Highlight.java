package com.example.cityview;

public class Highlight {
    private final String name;
    private final String imageUrl;

    public Highlight(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

