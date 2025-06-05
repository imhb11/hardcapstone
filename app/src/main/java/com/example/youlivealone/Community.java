package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Community extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences 파일 이름
    private static final String KEY_CATEGORY_ID = "categoryId"; // 저장할 키
    private RecyclerView recyclerView;
    private PopularPostAdapter adapter;
    private ArrayList<PopularPost> postList;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);


        //버튼 작동코드들
        findViewById(R.id.foots).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, Mypage.class);
            startActivity(intent);
        });

//        GridLayout populargridLayout = findViewById(R.id.populargrid);
//        for(int i = 0; i < populargridLayout.getChildCount(); i++) {
//            View child = populargridLayout.getChildAt(i);
//            if (child instanceof ImageButton) {
//                child.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(Community.this, Chat.class);
//                        // 각 버튼에 맞는 데이터를 인텐트에 추가
//                        intent.putExtra("buttonId", v.getId());
//                        startActivity(intent);
//                    }
//                });
//            }
//        }

        recyclerView = findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        adapter = new PopularPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        queue = Volley.newRequestQueue(this);
        fetchPopularPosts();

        GridLayout categorygridLayout = findViewById(R.id.categorygrid);
        for (int i = 0; i < categorygridLayout.getChildCount(); i++) {
            View child = categorygridLayout.getChildAt(i);
            if (child instanceof ImageButton) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // 각 버튼에 맞는 카테고리 ID를 지정
                        int categoryId = -1;
                        if (v.getId() == R.id.btn5) {
                            categoryId = 1; // 요리
                        } else if (v.getId() == R.id.btn6) {
                            categoryId = 2; // 운동
                        } else if (v.getId() == R.id.btn9) {
                            categoryId = 3; // 취업
                        } else if (v.getId() == R.id.btn7) {
                            categoryId = 4; // 만화
                        } else if (v.getId() == R.id.btn8) {
                            categoryId = 5; // 패션
                        } else if (v.getId() == R.id.btn10) {
                            categoryId = 6; // 여행
                        }


                        // 카테고리 ID를 SharedPreferences에 저장
                        // 카테고리 ID가 유효한 경우에만 저장
                        if (categoryId != -1) {
                            editor.putInt(KEY_CATEGORY_ID, categoryId);
                            editor.apply();
                        }

                        Intent intent = new Intent(Community.this, MessageBoard.class);
                        // 각 버튼에 맞는 데이터를 인텐트에 추가
                        intent.putExtra("buttonId", categoryId);
                        startActivity(intent);
                    }
                });
            }
        }

    }

    private void fetchPopularPosts() {
        String url = "http://15.165.92.121:8080/meetings/popular"; // 실제 인기 게시글 API 주소로 바꿔야 함

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    parsePostData(response);
                },
                error -> {
                    error.printStackTrace();
                }
        );

        queue.add(request);
    }

    private void parsePostData(JSONArray response) {
        try {
            postList.clear();
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                String title = obj.getString("title");
                int likeCount = obj.getInt("likeCount");
                postList.add(new PopularPost(title, likeCount));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class PopularPostAdapter extends RecyclerView.Adapter<PopularPostAdapter.ViewHolder> {

        private List<PopularPost> postList;

        public PopularPostAdapter(List<PopularPost> postList) {
            this.postList = postList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView postTitle, likeCount;
            ImageView heartIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                postTitle = itemView.findViewById(R.id.postTitle);
                likeCount = itemView.findViewById(R.id.likeCount);
                heartIcon = itemView.findViewById(R.id.heartIcon);
            }
        }

        @NonNull
        @Override
        public PopularPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.community_postlist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PopularPostAdapter.ViewHolder holder, int position) {
            PopularPost post = postList.get(position);
            holder.postTitle.setText(post.getTitle());
            holder.likeCount.setText(String.valueOf(post.getLikeCount()));
        }

        @Override
        public int getItemCount() {
            return postList.size();
        }
    }

    public class PopularPost {
        private String title;
        private int likeCount;

        public PopularPost(String title, int likeCount) {
            this.title = title;
            this.likeCount = likeCount;
        }

        public String getTitle() {
            return title;
        }

        public int getLikeCount() {
            return likeCount;
        }
    }


}

