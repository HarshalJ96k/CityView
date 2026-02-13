package com.example.cityview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    public interface OnStatusChangedListener {
        void onStatusChanged();
    }

    private final Context context;
    private final List<Report> reportList;
    private final OnStatusChangedListener listener;

    public ReportAdapter(Context context,
                         List<Report> reportList,
                         OnStatusChangedListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

        Report report = reportList.get(position);

        holder.textTitle.setText(report.getDescription());
        holder.textCategory.setText("Infrastructure");
        holder.textDate.setText(report.getCreatedAt());
        holder.textPriority.setText("High");

        String status = report.getStatus();

        if (status.equals("Pending")) {
            holder.buttonTakeAction.setVisibility(View.VISIBLE);
            holder.buttonTakeAction.setText("Mark In Progress");
            holder.buttonTakeAction.setOnClickListener(v ->
                    updateStatus(report.getId(), "In Progress", position));

        } else if (status.equals("In Progress")) {
            holder.buttonTakeAction.setVisibility(View.VISIBLE);
            holder.buttonTakeAction.setText("Mark Completed");
            holder.buttonTakeAction.setOnClickListener(v ->
                    updateStatus(report.getId(), "Completed", position));
        } else {
            holder.buttonTakeAction.setVisibility(View.GONE);
        }

        holder.buttonView.setOnClickListener(v -> {

            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_report_details, null);

            ImageView img = dialogView.findViewById(R.id.img_report);
            TextView desc = dialogView.findViewById(R.id.txt_desc);
            TextView loc = dialogView.findViewById(R.id.txt_location);
            TextView reporter = dialogView.findViewById(R.id.txt_reporter);
            TextView statusText = dialogView.findViewById(R.id.txt_status);
            TextView date = dialogView.findViewById(R.id.txt_date);

            desc.setText(report.getDescription());
            loc.setText("📍 " + report.getLocation());
            reporter.setText("👤 " + report.getReporterName());
            statusText.setText("Status: " + report.getStatus());
            date.setText("🗓 " + report.getCreatedAt());

            // ✅ CORRECT IMAGE LOADING
            String imageUrl = ApiUrls.getRootUrl() + report.getImagePath();

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_report)
                    .error(R.drawable.ic_report)
                    .into(img);

            new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setCancelable(true)
                    .show();
        });

        // Delete only for Completed reports
        if (status.equals("Completed")) {

            holder.buttonDelete.setVisibility(View.VISIBLE);

            holder.buttonDelete.setOnClickListener(v -> {

                new AlertDialog.Builder(context)
                        .setTitle("Delete Report")
                        .setMessage("Are you sure you want to delete this report?")
                        .setPositiveButton("Delete", (d, w) ->
                                deleteReport(report.getId(), position))
                        .setNegativeButton("Cancel", null)
                        .show();
            });

        } else {
            holder.buttonDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }
    private void deleteReport(int reportId, int position) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_DELETE_REPORT,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {

                            reportList.remove(position);
                            notifyItemRemoved(position);

                            Toast.makeText(context,
                                    "Report deleted", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context,
                        "Delete failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("report_id", String.valueOf(reportId));
                return map;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    private void updateStatus(int reportId, String newStatus, int position) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_UPDATE_REPORT_STATUS,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getString("status").equals("success")) {

                            reportList.remove(position);
                            notifyItemRemoved(position);

                            if (listener != null) listener.onStatusChanged();

                            Toast.makeText(context,
                                    "Status Updated", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(context,
                        "Update failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("report_id", String.valueOf(reportId));
                map.put("new_status", newStatus);
                return map;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView textTitle, textCategory, textDate, textPriority;
        Button buttonView, buttonTakeAction,buttonDelete;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textCategory = itemView.findViewById(R.id.text_category);
            textDate = itemView.findViewById(R.id.text_date);
            textPriority = itemView.findViewById(R.id.text_priority);
            buttonView = itemView.findViewById(R.id.button_view);
            buttonTakeAction = itemView.findViewById(R.id.button_take_action);
            buttonDelete = itemView.findViewById(R.id.button_delete);

        }
    }
}
