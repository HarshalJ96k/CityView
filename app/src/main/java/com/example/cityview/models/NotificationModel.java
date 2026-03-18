package com.example.cityview.models;
import com.example.cityview.R;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

public class NotificationModel {

    private final int id;
    private final int complaintId; // ✅ NEW: store complaint_id for potential report linking
    private final String message;
    private String status;
    private final String createdAt;

    public NotificationModel(int id, int complaintId, String message, String status, String createdAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.message = message;
        // ✅ FIX: Null / "null" safety — treat missing status as "Read"
        this.status = (status == null || status.equals("null") || status.isEmpty()) ? "Read" : status;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Allows marking as read locally after API call
    public void markAsRead() {
        this.status = "Read";
    }
}


