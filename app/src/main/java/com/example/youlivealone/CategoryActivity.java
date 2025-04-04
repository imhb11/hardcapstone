package com.example.youlivealone;

import android.content.SharedPreferences;
import android.os.Bundle;
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

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView popularChatRoomsRecyclerView;
    private PopularChatRoomAdapter popularChatRoomAdapter;
    private ArrayList<ChatRoom> popularChatRoomsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category);

//        popularChatRoomsRecyclerView = findViewById(R.id.popularChatRoomsRecyclerView);

        popularChatRoomsList = new ArrayList<>();
        popularChatRoomAdapter = new PopularChatRoomAdapter(popularChatRoomsList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        popularChatRoomsRecyclerView.setLayoutManager(layoutManager);
        popularChatRoomsRecyclerView.setAdapter(popularChatRoomAdapter);

        loadPopularChatRooms();
    }

    private int getCategoryIdFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("categoryId", -1); // 기본값 -1, 카테고리 ID가 없는 경우
    }


    private void loadPopularChatRooms() {
        String url = "http://15.165.92.121:8080/chat/popular"; // 인기 채팅방을 위한 엔드포인트
        int categoryId = getCategoryIdFromPreferences();

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    popularChatRoomsList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject roomObject = response.getJSONObject(i);
                            int id = roomObject.getInt("id");
                            String name = roomObject.getString("name");
                            String description = roomObject.getString("description");
                            int participantCount = roomObject.getInt("participantCount");

                            ChatRoom chatRoom = new ChatRoom(name, description, participantCount, categoryId);
                            popularChatRoomsList.add(chatRoom);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    popularChatRoomAdapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "인기 채팅방 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonArrayRequest);
    }
}

