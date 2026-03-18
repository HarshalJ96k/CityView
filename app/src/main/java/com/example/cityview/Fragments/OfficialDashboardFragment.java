package com.example.cityview.Fragments;
import com.example.cityview.activities.*;
import com.example.cityview.adapters.*;
import com.example.cityview.models.*;
import com.example.cityview.utils.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.R;
import com.example.cityview.models.Report;
import com.example.cityview.adapters.ReportAdapter;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficialDashboardFragment extends Fragment {

    private TextView textPending, textInProgress, textCompleted, textCitizens;
    private RecyclerView recyclerViewRecentReports;
    private ReportAdapter reportAdapter;
    private List<Report> recentReportList;
    private RequestQueue requestQueue;
    LinearLayout ll_total_citizens;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_official_dashboard, container, false);

        ll_total_citizens = view.findViewById(R.id.ll_official_fragment_total_citizens);

        textPending = view.findViewById(R.id.text_pending_count);
        textInProgress = view.findViewById(R.id.text_inprogress_count);
        textCompleted = view.findViewById(R.id.text_completed_count);
        textCitizens = view.findViewById(R.id.text_citizens_count);
        recyclerViewRecentReports = view.findViewById(R.id.recycler_view_recent_reports);

        requestQueue = Volley.newRequestQueue(requireContext());

        recentReportList = new ArrayList<>();

        // ✅ FIXED CONSTRUCTOR
        reportAdapter = new ReportAdapter(
                getContext(),
                recentReportList,
                null // dashboard doesn't need refresh callback
        );

        recyclerViewRecentReports.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecentReports.setAdapter(reportAdapter);

        fetchDashboardStats();
        fetchRecentReports();

        ll_total_citizens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllUsersFragment fragment = new AllUsersFragment();

                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_official, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void fetchDashboardStats() {

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiUrls.URL_GET_DASHBOARD_STATS,
                null,
                response -> {
                    try {
                        if (!response.getString("status").equals("success"))
                            return;

                        JSONObject data = response.getJSONObject("data");
                        textPending.setText(data.getString("pending"));
                        textInProgress.setText(data.getString("in_progress"));
                        textCompleted.setText(data.getString("completed"));
                        textCitizens.setText(data.getString("total_citizens"));

                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Dashboard parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Dashboard fetch failed", Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }

    private void fetchRecentReports() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_REPORTS,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (!json.getString("status").equals("success"))
                            return;

                        JSONArray array = json.getJSONArray("data");
                        recentReportList.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject r = array.getJSONObject(i);

                            recentReportList.add(new Report(
                                    r.getInt("report_id"),
                                    r.getString("description"),
                                    r.getString("location"),
                                    r.getString("photo_urls"),
                                    r.getString("status"),
                                    r.getString("submitted_at"),
                                    r.getString("full_name"),
                                    r.optString("category", "General")));
                        }

                        reportAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                                "Recent report parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Recent reports fetch failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("status", "Pending");
                return params;
            }
        };

        requestQueue.add(request);
    }
}

