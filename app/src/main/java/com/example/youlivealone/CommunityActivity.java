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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommunityActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://15.165.92.121:8080/mypage/community";
    private String userId;
    private LinearLayout writtenPostList;
    private LinearLayout likedPostList;
    private LinearLayout chatroomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        TextView backButton = findViewById(R.id.back_button);
        writtenPostList = findViewById(R.id.writtenPostList);
        likedPostList = findViewById(R.id.likedPostList);
        chatroomList = findViewById(R.id.chatroomList);

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
            Intent intent = new Intent(CommunityActivity.this, Mypage.class);
            startActivity(intent);
            finish();
        });

        fetchWrittenPosts();
        fetchLikedPosts();
        fetchChatRooms();

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
            Log.e("CommunityActivity", "Error extracting userId", e);
            return null;
        }
    }

    private void fetchWrittenPosts() {
        String url = BASE_URL + "/writtenposts/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> displayWrittenPosts(response),
                error -> Log.e("CommunityActivity", "Error fetching written posts", error));

        queue.add(request);
    }

    private void fetchLikedPosts() {
        String url = BASE_URL + "/likedposts/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> displayLikedPosts(response),
                error -> Log.e("CommunityActivity", "Error fetching liked posts", error));

        queue.add(request);
    }

    private void fetchChatRooms() {
        String url = BASE_URL + "/chatrooms/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> displayChatRooms(response),
                error -> Log.e("CommunityActivity", "Error fetching chat rooms", error));

        queue.add(request);
    }

    private void displayWrittenPosts(JSONArray posts) {
        try {
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);
                String title = post.getString("title");

                TextView postView = new TextView(this);
                postView.setText(title);
                postView.setTextSize(16);
                writtenPostList.addView(postView);
            }
        } catch (JSONException e) {
            Log.e("CommunityActivity", "Error displaying written posts", e);
        }
    }

    private void displayLikedPosts(JSONArray posts) {
        try {
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);
                String title = post.getString("title");

                TextView postView = new TextView(this);
                postView.setText(title);
                postView.setTextSize(16);
                likedPostList.addView(postView);
            }
        } catch (JSONException e) {
            Log.e("CommunityActivity", "Error displaying liked posts", e);
        }
    }

    private void displayChatRooms(JSONArray rooms) {
        try {
            for (int i = 0; i < rooms.length(); i++) {
                JSONObject room = rooms.getJSONObject(i);
                String roomName = room.getString("roomName");

                TextView roomView = new TextView(this);
                roomView.setText(roomName);
                roomView.setTextSize(14);
                chatroomList.addView(roomView);
            }
        } catch (JSONException e) {
            Log.e("CommunityActivity", "Error displaying chat rooms", e);
        }
    }
}
