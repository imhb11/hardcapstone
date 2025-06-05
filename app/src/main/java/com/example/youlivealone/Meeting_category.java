package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Meeting_category extends AppCompatActivity {
    private LinearLayout categoryLayout;
    private RecyclerView categoryRecyclerView;
    private MeetingCategoryListAdapter adapter;
    private static final String CATEGORY_URL = "http://15.165.92.121:8080/meetings/categoryId/{categoryId}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_category);

        findViewById(R.id.check).setOnClickListener(v -> startActivity(new Intent(this, Check.class)));
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.chat).setOnClickListener(v -> startActivity(new Intent(this, Chat.class)));
        findViewById(R.id.mypage).setOnClickListener(v -> startActivity(new Intent(this, Mypage.class)));
        findViewById(R.id.additional_button).setOnClickListener(v -> startActivity(new Intent(this, MeetingWrite1p.class)));

        categoryLayout = findViewById(R.id.categoryLinearLayout);
        categoryRecyclerView = findViewById(R.id.meeting_all_recyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int categoryId = sharedPreferences.getInt("categoryId", -1);

        if (categoryId != -1) {
            String categoriesurl = CATEGORY_URL.replace("{categoryId}", String.valueOf(categoryId));
            loadCategories(categoriesurl);
            loadAllPosts(String.valueOf(categoryId));
        } else {
            Toast.makeText(this, "카테고리 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateListView(List<Meeting_post> posts) {
        adapter = new MeetingCategoryListAdapter(this, posts);
        categoryRecyclerView.setAdapter(adapter);
    }

    private void loadCategories(String url) {
        Log.d("Meeting_category", "카테고리 로드 시작: " + url);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        categoryLayout.removeAllViews();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject category = response.getJSONObject(i);
                            String categoryName = category.getString("name");
                            String categoryId = category.getString("id");

                            TextView categoryTextView = new TextView(this);
                            categoryTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            categoryTextView.setText(categoryName);
                            categoryTextView.setPadding(16, 16, 16, 16);

                            categoryTextView.setOnClickListener(view -> {
                                Log.d("Meeting_category", "카테고리 클릭됨: " + categoryName);
                                loadPostsBySubcategory(categoryId);
                            });

                            categoryLayout.addView(categoryTextView);
                        }
                    } catch (JSONException e) {
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("Meeting_category", "카테고리 로드 실패: " + error.getMessage());
                    Toast.makeText(this, "카테고리 로드 실패", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(jsonArrayRequest);
    }

    private void loadAllPosts(String categoryId) {
        String url = "http://15.165.92.121:8080/MeetingCategory/meetings/category/" + categoryId;
        Log.d("Meeting_category", "전체 글 목록 로드 시작: " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Meeting_post> posts = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject post = response.getJSONObject(i);
                            posts.add(parsePostFromJson(post));
                        }
                        updateListView(posts);
                    } catch (JSONException e) {
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("Meeting_category", "전체 글 목록 로드 실패: " + error.getMessage());
                    Toast.makeText(this, "전체 글 목록 로드 실패", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private void loadPostsBySubcategory(String subcategoryId) {
        String url = "http://15.165.92.121:8080/meetings/subcategory/" + subcategoryId;
        Log.d("Meeting_category", "서브카테고리 글 목록 로드 시작: " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        List<Meeting_post> posts = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject post = response.getJSONObject(i);
                            posts.add(parsePostFromJson(post));
                        }
                        updateListView(posts);
                    } catch (JSONException e) {
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("Meeting_category", "서브카테고리 로드 실패: " + error.getMessage());
                    Toast.makeText(this, "글 목록 로드 실패", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(jsonArrayRequest);
    }

    private Meeting_post parsePostFromJson(JSONObject post) throws JSONException {
        return new Meeting_post(
                post.getString("id"),
                post.getString("title"),
                post.getString("introduction"),
                post.getString("content"),
                post.getInt("memberCount"),
                post.getString("meetingCategoryId"),
                post.getString("subcategoryId"),
                post.getDouble("latitude"),
                post.getDouble("longitude")
        );
    }
}
