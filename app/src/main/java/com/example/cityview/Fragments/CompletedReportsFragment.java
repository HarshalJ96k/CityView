package com.example.cityview.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.cityview.R;
import com.example.cityview.Report;
import com.example.cityview.ReportAdapter;
import com.example.cityview.urls.ApiUrls;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompletedReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView title;
    private ReportAdapter adapter;
    private List<Report> reportList;
    private LottieAnimationView lottieNoData;
    private TextView textNoData;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_reports);
        progressBar = view.findViewById(R.id.progress_bar);
        title = view.findViewById(R.id.text_fragment_title);
        lottieNoData = view.findViewById(R.id.lottie_no_data);
        textNoData = view.findViewById(R.id.text_no_data);
 

        title.setText("Completed Reports");

        reportList = new ArrayList<>();

        adapter = new ReportAdapter(
                getContext(),
                reportList,
                () -> fetchReports("Completed")
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchReports("Completed");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchReports("Completed");
    }
    private void fetchReports(String status) {

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        lottieNoData.setVisibility(View.GONE);
        textNoData.setVisibility(View.GONE);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiUrls.URL_GET_REPORTS,
                response -> {
                    progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject json = new JSONObject(response);
                        JSONArray data = json.getJSONArray("data");

                        reportList.clear();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject r = data.getJSONObject(i);

                            reportList.add(new Report(
                                    r.getInt("report_id"),
                                    r.getString("description"),
                                    r.getString("location"),
                                    r.getString("photo_urls"),
                                    r.getString("status"),
                                    r.getString("submitted_at"),
                                    r.getString("full_name")
                            ));
                        }

                        if (reportList.isEmpty()) {
                            lottieNoData.setVisibility(View.VISIBLE);
                            textNoData.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        lottieNoData.setVisibility(View.VISIBLE);
                        textNoData.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    lottieNoData.setVisibility(View.VISIBLE);
                    textNoData.setVisibility(View.VISIBLE);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("status", status);
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

}
