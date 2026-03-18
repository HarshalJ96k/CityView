package com.example.cityview.Fragments;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.R;
import com.example.cityview.adapters.UserAdapter;
import com.example.cityview.models.User;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AllUsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<User> userList;
    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_all_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        progressBar = view.findViewById(R.id.progress_bar);

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchAllUsers();

        return view;
    }

    private void fetchAllUsers() {

        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                ApiUrls.URL_GET_ALL_USERS,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getString("status").equals("success")) return;

                        JSONArray data = json.getJSONArray("data");
                        userList.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject u = data.getJSONObject(i);

                            userList.add(new User(
                                    u.getInt("user_id"),
                                    u.getString("full_name"),
                                    u.getString("email"),
                                    u.getString("phone"),
                                    u.getString("joined_at")
                            ));
                        }

                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "User parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Failed to load users", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
}

