package com.example.youlivealone;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ViewPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleListAdapter adapter;
    private List<String> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        recyclerView = findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 임시 데이터
        postList = new ArrayList<>();
        postList.add("첫 번째 게시글");
        postList.add("두 번째 게시글");

        adapter = new SimpleListAdapter(postList, "post");
        recyclerView.setAdapter(adapter);
    }
}
