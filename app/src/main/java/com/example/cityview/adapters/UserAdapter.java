package com.example.cityview.adapters;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cityview.R;
import com.example.cityview.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        User user = userList.get(position);

        holder.name.setText(user.getFullName());
        holder.email.setText(user.getEmail());
        holder.phone.setText("📞 " + user.getPhone());
        holder.joined.setText("Joined: " + user.getJoinedAt());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView name, email, phone, joined;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_user_name);
            email = itemView.findViewById(R.id.text_user_email);
            phone = itemView.findViewById(R.id.text_user_phone);
            joined = itemView.findViewById(R.id.text_user_joined);
        }
    }
}


