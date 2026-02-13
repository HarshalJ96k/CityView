package com.example.cityview;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.android.volley.toolbox.*;
import com.example.cityview.urls.ApiUrls;
import org.json.*;
import java.util.*;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<NotificationModel> list = new ArrayList<>();
    SessionManager session;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        session = new SessionManager(this);
        loadNotifications();
    }

    private void loadNotifications() {

        StringRequest req = new StringRequest(
                com.android.volley.Request.Method.POST,
                ApiUrls.URL_GET_NOTIFICATIONS,
                res -> {
                    try {
                        JSONObject obj = new JSONObject(res);
                        JSONArray arr = obj.getJSONArray("data");

                        list.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject n = arr.getJSONObject(i);
                            list.add(new NotificationModel(
                                    n.getInt("id"),
                                    n.getString("message"),
                                    n.getString("status"),
                                    n.getString("created_at")
                            ));
                        }

                        recyclerView.setAdapter(
                                new NotificationAdapter(this, list));

                    } catch (Exception e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override protected Map<String,String> getParams() {
                Map<String,String> m = new HashMap<>();
                m.put("user_id", session.getUserId());
                return m;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
