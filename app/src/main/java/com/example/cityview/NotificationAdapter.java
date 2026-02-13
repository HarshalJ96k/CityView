package com.example.cityview;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    Context context;
    List<NotificationModel> list;

    public NotificationAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder h, int pos) {
        NotificationModel n = list.get(pos);
        h.txtMsg.setText(n.getMessage());
        h.txtDate.setText(n.getCreatedAt());

        if (n.getStatus().equals("Unread")) {
            h.itemView.setAlpha(1f);
        } else {
            h.itemView.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView txtMsg, txtDate;
        Holder(View v) {
            super(v);
            txtMsg = v.findViewById(R.id.txtMessage);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }
}
