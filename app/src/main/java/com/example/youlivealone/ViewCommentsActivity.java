package com.example.youlivealone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewCommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleListAdapter adapter;
    private List<String> commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        recyclerView = findViewById(R.id.recycler_view_comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 임시 데이터
        commentList = new ArrayList<>();
        commentList.add("첫 번째 댓글");
        commentList.add("두 번째 댓글");

        adapter = new SimpleListAdapter(commentList, "comment");
        recyclerView.setAdapter(adapter);
    }
}
