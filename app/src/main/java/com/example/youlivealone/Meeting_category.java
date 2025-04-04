package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

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
    private ListView category_listView;
    private static final String CATEGORY_URL = "http://15.165.92.121:8080/meetings/categoryId/{categoryId}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_category);

        //하단바 버튼 작동코드들
        findViewById(R.id.check).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_category.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_category.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_category.this, Chat.class);
            startActivity(intent);
        });
        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_category.this, Mypage.class);
            startActivity(intent);
        });


        //글 작성
        findViewById(R.id.additional_button).setOnClickListener(v -> {
            Intent intent = new Intent(Meeting_category.this, Meeting_write.class);
            startActivity(intent);
        });
        categoryLayout = findViewById(R.id.categoryLinearLayout); // Use ID of the LinearLayout inside HorizontalScrollView
        category_listView = findViewById(R.id.meeting_all_listView);


        // SharedPreferences에서 categoryId 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int categoryId = sharedPreferences.getInt("categoryId", -1); // 기본값은 null

        if (categoryId != -1) {
            String categoriesurl = CATEGORY_URL.replace("{categoryId}", String.valueOf(categoryId));
            loadCategories(categoriesurl);
            loadAllPosts(String.valueOf(categoryId)); // 전체 글 목록을 로드하는 메서드 호출
        } else {
            Toast.makeText(this, "카테고리 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }




    }
    // 서버에서 세부 카테고리 목록 가져오기
    private void loadCategories(String url) {
        Log.d("Meeting_category", "카테고리 로드 시작: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("Response", "세부카테고리 목록 서버 응답: " + response);
                    try {
                        categoryLayout.removeAllViews(); // 이전 항목 삭제
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject category = response.getJSONObject(i);
                            String categoryName = category.getString("name");
                            String categoryId = category.getString("id"); // 필요시 사용

                            // TextView 생성
                            TextView categoryTextView = new TextView(this);
                            categoryTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            categoryTextView.setText(categoryName); // 카테고리 이름 설정
                            categoryTextView.setPadding(16, 16, 16, 16); // 패딩 설정

                            // 클릭 리스너 추가
                            categoryTextView.setOnClickListener(view -> {
                                Log.d("Meeting_category", "카테고리 클릭됨: " + categoryName);
                                // 클릭 시 해당 서브카테고리의 글 목록 불러오기
                                loadPostsBySubcategory(categoryId);
                            });

                            // 레이아웃에 추가
                            categoryLayout.addView(categoryTextView);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());

                    }
                },
                error -> {
                    Log.e("Meeting_category", "카테고리 로드 실패: " + error.getMessage());
                    Toast.makeText(Meeting_category.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(jsonArrayRequest);
    }


    // 전체 글 목록을 서버에서 가져오는 메서드
    private void loadAllPosts(String categoryId) {
        String url = "http://15.165.92.121:8080/MeetingCategory/meetings/category/" + categoryId; // 카테고리 ID를 URL에 포함
        Log.d("Meeting_category", "전체 글 목록 로드 시작: " + url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("Response", "카테고리 글 목록 서버 응답: " + response.toString());
                        List<Meeting_post> posts = new ArrayList<>(); // Post 객체 리스트 생성
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject post = response.getJSONObject(i);
                            String id = post.getString("id"); // 글 ID
                            String title = post.getString("title"); // 글 제목
                            String introduction = post.getString("introduction"); // 소개
                            String content = post.getString("content"); // 내용
                            int memberCount = post.getInt("memberCount"); // 멤버 수
                            String meetingCategoryId = post.getString("meetingCategoryId"); // 회의 카테고리 ID
                            String subcategoryId = post.getString("subcategoryId"); // 서브카테고리 ID
                            double latitude = post.getDouble("latitude");  // 추가된 필드
                            double longitude = post.getDouble("longitude"); // 추가된 필드


                            posts.add(new Meeting_post(id, title, introduction, content, memberCount, meetingCategoryId, subcategoryId, latitude, longitude));
                        }
                        updateListView(posts); // 리스트 업데이트
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());

                    }
                },
                error -> {
                    Log.e("Meeting_category", "전체 글 목록 로드 실패: " + error.getMessage());
                    Toast.makeText(Meeting_category.this, "전체 글 목록 로드 실패", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonArrayRequest);
    }
    // 세부 카테고리 클릭 시 해당 글 목록 불러오기
    private void loadPostsBySubcategory(String subcategoryId) {
        Log.d("Meeting_category", "서브카테고리 로드 시작: " + subcategoryId);
        String url = "http://15.165.92.121:8080/meetings/subcategory/" + subcategoryId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("Response", "세부 카테고리 글 목록 서버 응답: " + response);
                        List<Meeting_post> posts = new ArrayList<>(); // Post 객체 리스트 생성
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject post = response.getJSONObject(i);
                            String id = post.getString("id"); // 글 ID
                            String title = post.getString("title"); // 글 제목
                            String introduction = post.getString("introduction"); // 소개

                            String content = post.getString("content"); // 내용
                            int memberCount = post.getInt("memberCount"); // 멤버 수
                            String meetingCategoryId = post.getString("meetingCategoryId"); // 회의 카테고리 ID
                            String subcategoryIdFromJson  = post.getString("subcategoryId"); // 서브카테고리 ID
                            double latitude = post.getDouble("latitude");  // 추가된 필드
                            double longitude = post.getDouble("longitude"); // 추가된 필드

                            posts.add(new Meeting_post(id, title, introduction, content, memberCount, meetingCategoryId, subcategoryIdFromJson, latitude, longitude));
                        }
                        updateListView(posts); // 리스트 업데이트
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Meeting_category", "JSON 파싱 오류: " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("Meeting_category", "서브카테고리 로드 실패: " + error.getMessage());
                    Toast.makeText(Meeting_category.this, "글 목록 로드 실패", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonArrayRequest);
    }

    private void updateListView(List<Meeting_post> posts) {
        ArrayAdapter<Meeting_post> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, posts);
        category_listView.setAdapter(adapter);

        category_listView.setOnItemClickListener((parent, view, position, id) -> {
            Meeting_post selectedPost = posts.get(position);
            Intent intent = new Intent(Meeting_category.this, Meeting_detail.class);

            // Meeting_post의 세부 정보를 Intent에 담아 전달
            intent.putExtra("postId", selectedPost.getId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("introduction", selectedPost.getIntroduction());
            intent.putExtra("content", selectedPost.getContent());
            intent.putExtra("memberCount", selectedPost.getMemberCount());
            intent.putExtra("latitude", selectedPost.getLatitude());
            intent.putExtra("longitude", selectedPost.getLongitude());

            intent.putExtra("meetingCategoryId", selectedPost.getMeetingCategoryId());
            intent.putExtra("subcategoryId", selectedPost.getSubcategoryID());

            startActivity(intent);
        });
    }
}
