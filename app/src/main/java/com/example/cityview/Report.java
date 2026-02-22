package com.example.cityview;

public class Report {

    private final int id;
    private final String description;
    private final String location;
    private final String imagePath;
    private String status;
    private final String createdAt;
    private final String reporterName;
    private final String category; // ✅ NEW

    public Report(int id,
            String description,
            String location,
            String imagePath,
            String status,
            String createdAt,
            String reporterName,
            String category) { // ✅ NEW param

        this.id = id;
        this.description = description;
        this.location = location;
        this.imagePath = imagePath;
        this.status = status;
        this.createdAt = createdAt;
        this.reporterName = reporterName;
        this.category = (category == null || category.isEmpty()) ? "General" : category;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public String getCategory() {
        return category;
    } // ✅ NEW getter

    public void setStatus(String status) {
        this.status = status;
    }
}
