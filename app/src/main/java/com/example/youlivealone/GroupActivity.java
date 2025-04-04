package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://15.165.92.121:8080/mypage/meetings";
    private String userId;
    private LinearLayout groupList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        TextView backButton = findViewById(R.id.back_button);
        groupList = findViewById(R.id.group_list_container);

        // JWT 토큰에서 userId 추출
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null);

        if (token != null) {
            userId = extractUserIdFromToken(token);
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, Mypage.class);
            startActivity(intent);
            finish();
        });

        fetchGroupData();

        findViewById(R.id.check).setOnClickListener(v -> startActivity(new Intent(this, Check.class)));
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.chat).setOnClickListener(v -> startActivity(new Intent(this, Chat.class)));
        findViewById(R.id.mypage).setOnClickListener(v -> startActivity(new Intent(this, Mypage.class)));
    }

    private String extractUserIdFromToken(String token) {
        try {
            JSONObject jsonObject = new JSONObject(new String(android.util.Base64.decode(token.split("\\.")[1], android.util.Base64.DEFAULT)));
            return jsonObject.getString("sub");
        } catch (Exception e) {
            Log.e("GroupActivity", "Error extracting userId", e);
            return null;
        }
    }

    private void fetchGroupData() {
        String url = BASE_URL + "/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> displayGroupData(response),
                error -> Log.e("GroupActivity", "Error fetching group data", error));

        queue.add(request);
    }

    private void displayGroupData(JSONArray groups) {
        try {
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                String groupName = group.getString("title");
                String groupDescription = group.getString("content");

                TextView groupItemView = new TextView(this);
                groupItemView.setText(groupName + "\n" + groupDescription);
                groupItemView.setTextSize(16);
                groupItemView.setPadding(16, 16, 16, 16);
                groupList.addView(groupItemView);

                int meetingId = group.getInt("meetingId");
                groupItemView.setOnClickListener(v -> leaveGroup(meetingId));
            }
        } catch (JSONException e) {
            Log.e("GroupActivity", "Error displaying group data", e);
        }
    }

    private void leaveGroup(int meetingId) {
        String url = BASE_URL + "/leave/" + meetingId + "?id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> Toast.makeText(this, "소모임을 탈퇴했습니다.", Toast.LENGTH_SHORT).show(),
                error -> Log.e("GroupActivity", "Error leaving group", error));

        queue.add(request);
    }

    private void updateGroup(int meetingId, String newTitle, String newContent, double latitude, double longitude, int maxMembers) {
        String url = BASE_URL + "/update/" + meetingId + "?id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("title", newTitle);
            jsonBody.put("content", newContent);
            jsonBody.put("latitude", latitude);
            jsonBody.put("longitude", longitude);
            jsonBody.put("maxMembers", maxMembers);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> Toast.makeText(this, "소모임 공고를 수정했습니다.", Toast.LENGTH_SHORT).show(),
                error -> Log.e("GroupActivity", "Error updating group", error));

        queue.add(request);
    }

    private void deleteGroup(int meetingId) {
        String url = BASE_URL + "/delete/" + meetingId + "?id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> Toast.makeText(this, "소모임을 삭제했습니다.", Toast.LENGTH_SHORT).show(),
                error -> Log.e("GroupActivity", "Error deleting group", error));

        queue.add(request);
    }
}
