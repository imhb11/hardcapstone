package com.example.youlivealone;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ViewChatRoomsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SimpleListAdapter adapter;
    private List<String> chatRoomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chat_rooms);

        recyclerView = findViewById(R.id.recycler_view_chat_rooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 임시 데이터
        chatRoomList = new ArrayList<>();
        chatRoomList.add("첫 번째 채팅방");
        chatRoomList.add("두 번째 채팅방");

        adapter = new SimpleListAdapter(chatRoomList, "chat");
        recyclerView.setAdapter(adapter);
    }
}
