package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MessageBoard extends AppCompatActivity {
    private ListView newlist;
    private ArrayAdapter<Post> newAdapter;
    private List<Post> newItems = new ArrayList<>();
    private static final String NEW_URL = "http://15.165.92.121:8080/categories/{categoryId}/posts";
    private static final String SEARCH_URL = "http://15.165.92.121:8080/search?query={title}";
    private static final String POPULAR_URL = "http://15.165.92.121:8080/posts/popular"; // Add your actual endpoint here


    private EditText searchBar;
    private ImageButton searchButton, additionalButton;
    private Button latestButton, popularButton;
//    private Listview listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messageboard); // messageboard.xml 레이아웃 파일 참조

        // UI 요소를 findViewById로 연결
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        additionalButton = findViewById(R.id.additional_button);
        latestButton = findViewById(R.id.latest_button);
        popularButton = findViewById(R.id.popular_button);
//        scrollView = findViewById(R.id.community_post_list);


        newlist = findViewById(R.id.community_post_list);
        newAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newItems);
        newlist.setAdapter(newAdapter);
        // SharedPreferences에서 카테고리 ID 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int categoryId = sharedPreferences.getInt("categoryId", -1); // 기본값 -1

        if (categoryId != -1) {
            // URL에 카테고리 ID 적용
            String postsUrl = NEW_URL.replace("{categoryId}", String.valueOf(categoryId));

            // API 호출하여 데이터 가져오기
            loadPostList(postsUrl);
        } else {
            Toast.makeText(this, "카테고리 ID를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            // 필요 시 액티비티 종료 또는 기본 화면 처리
        }
        // 리스트 항목 클릭 리스너 설정
        newlist.setOnItemClickListener((parent, view, position, id) -> {
            // 선택된 항목의 데이터 가져오기
            Post selectedPost = newItems.get(position);

            // Intent 생성하여 새로운 액티비티로 이동
            Intent intent = new Intent(MessageBoard.this, community_post.class);
            intent.putExtra("selectedPost", selectedPost);
            startActivity(intent);
        });




        // 검색 버튼 클릭 이벤트 처리
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchBar.getText().toString();
                if (!query.isEmpty()) {
                    try {
                        String encodedQuery = URLEncoder.encode(query, "UTF-8");
                        String searchUrl = SEARCH_URL.replace("{title}", encodedQuery);
                        loadPostList(searchUrl);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Toast.makeText(MessageBoard.this, "인코딩 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MessageBoard.this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 추가 버튼 클릭 이벤트 처리
        additionalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageBoard.this, AddMessageBoard.class);
                startActivity(intent);
                // 추가 기능 구현 (예: 새로운 게시물 추가)
            }
        });

        // 최신순 버튼 클릭 이벤트 처리
        latestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 최신순 정렬 기능 구현
            }
        });

        // 인기순 버튼 클릭 이벤트 처리
        popularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SharedPreferences에서 카테고리 ID 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                int categoryId = sharedPreferences.getInt("categoryId", -1);

                if (categoryId != -1) {
                    // URL에 카테고리 ID 적용
                    String popularPostsUrl = POPULAR_URL.replace("{categoryId}", String.valueOf(categoryId));
                    loadPostList(popularPostsUrl); // 인기 포스트 리스트를 로드
                } else {
                    Toast.makeText(MessageBoard.this, "카테고리 ID를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
                }            }
        });
    }
    private void loadPostList(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        newItems.clear();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject post = response.getJSONObject(i);
                                String postId = post.getString("id"); // postId 가져오기
                                int id = post.getInt("categoryId");
                                String title = post.getString("title");
                                String content = post.getString("content");
                                newItems.add(new Post(postId, id, title, content));
                            }

                            newAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MessageBoard.this, "Error loading posts", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        queue.add(jsonArrayRequest);
    }


}