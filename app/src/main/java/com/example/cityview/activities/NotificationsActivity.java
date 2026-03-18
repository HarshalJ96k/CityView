package com.example.cityview.activities;
import com.example.cityview.R;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.cityview.urls.ApiUrls;
import com.google.android.material.appbar.MaterialToolbar;

import android.view.LayoutInflater;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textEmpty;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout; // ✅ NEW
    private final List<NotificationModel> list = new ArrayList<>();
    private NotificationAdapter adapter;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // ✅ FIX: Toolbar with back button
        MaterialToolbar toolbar = findViewById(R.id.toolbar_notifications);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshNotifications); // ✅ NEW
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);

        session = new SessionManager(this);

        // ✅ FIX: Guard against null user_id session
        if (session.getUserId() == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set adapter early so mark-as-read can update it
        adapter = new NotificationAdapter(
                this,
                list,
                (id, pos) -> {
                    // ✅ OPENING ANYTIME: check if unread then mark, but always fetch details
                    boolean isUnread = list.get(pos).getStatus().equalsIgnoreCase("Unread");
                    if (isUnread) {
                        markNotificationRead(id, pos);
                    }

                    // ✅ LINKED REPORT: Fetch details if complaint_id > 0
                    int complaintId = list.get(pos).getComplaintId();
                    if (complaintId > 0) {
                        fetchAndShowReportDetails(complaintId);
                    }
                },
                (id, pos) -> {
                    // ✅ DELETE LISTENER
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Notification")
                            .setMessage("Are you sure you want to delete this notification?")
                            .setPositiveButton("Delete", (d, w) -> deleteNotification(id, pos))
                            .setNegativeButton("Cancel", null)
                            .show();
                });
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true); // ✅ START REFRESH

        StringRequest req = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_NOTIFICATIONS,
                res -> {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false); // ✅ STOP REFRESH
                    try {
                        JSONObject obj = new JSONObject(res);

                        // ✅ FIX: Check "status" before accessing "data"
                        if (!obj.optString("status", "error").equals("success")) {
                            Toast.makeText(this,
                                    obj.optString("message", "Failed to load notifications."),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray arr = obj.getJSONArray("data");
                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject n = arr.getJSONObject(i);
                            list.add(new NotificationModel(
                                    n.getInt("id"),
                                    n.optInt("complaint_id", 0), // ✅ FIX: use complaint_id from PHP
                                    n.getString("message"),
                                    n.optString("status", "Read"), // ✅ FIX: null-safe status
                                    n.getString("created_at")));
                        }

                        adapter.notifyDataSetChanged();

                        if (list.isEmpty()) {
                            Toast.makeText(this, "No notifications yet.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                err -> {
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false); // ✅ STOP REFRESH
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("user_id", session.getUserId());
                return m;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    /**
     * ✅ FIX: Mark a notification as "Read" via API when tapped.
     * Requires a mark_notification_read.php endpoint (see PHP fix below).
     */
    private void markNotificationRead(int notificationId, int position) {

        StringRequest req = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_MARK_NOTIFICATION_READ,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        if (obj.optString("status").equals("success")) {
                            list.get(position).markAsRead();
                            adapter.notifyItemChanged(position);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                err -> {
                    /* silent fail — not critical */ }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("notification_id", String.valueOf(notificationId));
                return m;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    /**
     * ✅ NEW: Fetch single report details and show in dialog
     */
    private void fetchAndShowReportDetails(int reportId) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_REPORT_DETAILS,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        if (obj.optString("status").equals("success")) {
                            JSONObject d = obj.getJSONObject("data");
                            showReportDialog(d);
                        } else {
                            Toast.makeText(this, "Report details not found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                err -> Toast.makeText(this, "Failed to load report details.", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("report_id", String.valueOf(reportId));
                return m;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    private void showReportDialog(JSONObject report) throws Exception {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_details, null);

        ImageView img = dialogView.findViewById(R.id.img_report);
        TextView desc = dialogView.findViewById(R.id.txt_desc);
        TextView loc = dialogView.findViewById(R.id.txt_location);
        TextView reporter = dialogView.findViewById(R.id.txt_reporter);
        TextView statusText = dialogView.findViewById(R.id.txt_status);
        TextView date = dialogView.findViewById(R.id.txt_date);

        desc.setText(report.getString("description"));
        loc.setText("📍 " + report.getString("location"));
        reporter.setText("👤 " + report.getString("full_name"));
        statusText.setText(
                "Status: " + report.getString("status") + " | Type: " + report.optString("category", "General"));
        date.setText("🗓 " + report.getString("submitted_at"));

        String imageUrl = ApiUrls.getRootUrl() + report.getString("photo_urls");

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_report)
                .error(R.drawable.ic_report)
                .into(img);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Close", null)
                .show();
    }

    private void deleteNotification(int notificationId, int position) {
        StringRequest req = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_DELETE_NOTIFICATION,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        if (obj.optString("status").equals("success")) {
                            list.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to delete: " + obj.optString("message"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                err -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("notification_id", String.valueOf(notificationId));
                return m;
            }
        };
        Volley.newRequestQueue(this).add(req);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


