package com.example.youlivealone;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ViewLikedPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleListAdapter adapter;
    private List<String> likedPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_liked_posts);

        recyclerView = findViewById(R.id.recycler_view_liked_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 임시 데이터
        likedPostList = new ArrayList<>();
        likedPostList.add("공감한 첫 번째 게시글");
        likedPostList.add("공감한 두 번째 게시글");

        adapter = new SimpleListAdapter(likedPostList, "liked post");
        recyclerView.setAdapter(adapter);
    }
}
