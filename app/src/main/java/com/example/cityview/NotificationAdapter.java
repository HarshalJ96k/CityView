package com.example.cityview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    public interface OnNotificationTapListener {
        void onTap(int notificationId, int position);
    }

    public interface OnNotificationDeleteListener {
        void onDelete(int notificationId, int position);
    }

    private final Context context;
    private final List<NotificationModel> list;
    private final OnNotificationTapListener tapListener;
    private final OnNotificationDeleteListener deleteListener;

    public NotificationAdapter(Context context,
            List<NotificationModel> list,
            OnNotificationTapListener tapListener,
            OnNotificationDeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.tapListener = tapListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        NotificationModel n = list.get(pos);

        h.txtMsg.setText(n.getMessage());
        h.txtDate.setText(n.getCreatedAt());

        // ✅ FIX: Actually bind the status text (was always showing placeholder
        // "Status")
        h.txtStatus.setText(n.getStatus());

        // Visual distinction — Unread = full brightness + blue label + dot, Read =
        // dimmed + grey
        boolean isUnread = n.getStatus().equalsIgnoreCase("Unread");

        h.itemView.setAlpha(isUnread ? 1f : 0.7f);
        h.unreadDot.setVisibility(isUnread ? View.VISIBLE : View.GONE);

        h.txtStatus.setTextColor(isUnread
                ? context.getColor(android.R.color.holo_blue_dark)
                : context.getColor(android.R.color.darker_gray));

        // ✅ FIX: Allow tapping any notification (open details)
        h.itemView.setOnClickListener(v -> {
            if (tapListener != null) {
                tapListener.onTap(n.getId(), h.getAdapterPosition());
            }
        });

        // ✅ NEW: Delete listener
        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(n.getId(), h.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtMsg, txtStatus, txtDate;
        View unreadDot;
        android.widget.ImageButton btnDelete;

        Holder(@NonNull View v) {
            super(v);
            txtMsg = v.findViewById(R.id.txtMessage);
            txtStatus = v.findViewById(R.id.txtStatus); // ✅ FIX: was missing
            txtDate = v.findViewById(R.id.txtDate);
            unreadDot = v.findViewById(R.id.viewUnreadIndicator);
            btnDelete = v.findViewById(R.id.btnDeleteNotification);
        }
    }
}
