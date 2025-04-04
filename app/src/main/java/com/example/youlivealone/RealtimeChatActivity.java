package com.example.youlivealone;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RealtimeChatActivity extends AppCompatActivity {

    private RecyclerView chatRoomRecyclerView;
    private ChatRoomAdapter chatRoomAdapter;
    private PopularChatRoomAdapter popularChatRoomAdapter;
    private ArrayList<ChatRoom> chatRoomList;
    private static final String CHAT_ROOMS_URL = "http://15.165.92.121:8080/chat/rooms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_chat);

        chatRoomRecyclerView = findViewById(R.id.chatRoomRecyclerView);
        ImageButton addChatRoomButton = findViewById(R.id.addChatRoomButton);


        chatRoomList = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(chatRoomList, this);
        chatRoomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRoomRecyclerView.setAdapter(chatRoomAdapter);

        addChatRoomButton.setOnClickListener(v -> showAddChatRoomDialog());

        // 채팅방 리스트 불러오기
        loadChatRooms();
    }

    private void loadChatRooms() {
        int categoryId = getCategoryIdFromPreferences();
        if (categoryId == -1) {
            Toast.makeText(this, "카테고리 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = CHAT_ROOMS_URL + "?categoryId=" + categoryId;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    chatRoomList.clear();  // 기존 리스트 초기화
                    parseChatRooms(response);
                    chatRoomAdapter.notifyDataSetChanged();
                },
                error -> {
                    Log.e("RealtimeChatActivity", "채팅방 목록 불러오기 실패", error);
                    Toast.makeText(this, "채팅방 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsonArrayRequest);
    }

    private void parseChatRooms(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject roomObject = response.getJSONObject(i);

                int id = roomObject.getInt("id"); // 서버 응답에서 id 받아옴
                String name = roomObject.getString("name");
                String description = roomObject.getString("description");
                int maxParticipants = roomObject.getInt("maxParticipants");
                //int category = roomObject.getInt("category");
                int participantCount = roomObject.optInt("participantCount", 0);
                //String craetorId = roomObject.getString("creatorId");
                // category가 JSONObject로 들어올 때 안전하게 접근
                int categoryId = -1; // 기본값 설정
                if (roomObject.has("category")) {
                    JSONObject categoryObject = roomObject.getJSONObject("category");
                    categoryId = categoryObject.getInt("id"); // category의 id를 추출
                }

                ChatRoom chatRoom = new ChatRoom(id, name, description, maxParticipants, categoryId);
                chatRoom.setParticipantCount(participantCount);

                chatRoomList.add(chatRoom);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("RealtimeChatActivity", "JSON 파싱 오류", e);
        }
    }

    //카테고리 아이디 가져오기
    private int getCategoryIdFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getInt("categoryId", -1); // 기본값 -1, 카테고리 ID가 없는 경우
    }

    private void showAddChatRoomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_chat_room);

        EditText roomTitleInput = dialog.findViewById(R.id.roomTitleInput);
        EditText roomDescriptionInput = dialog.findViewById(R.id.roomDescriptionInput);
        EditText maxParticipantsInput = dialog.findViewById(R.id.maxParticipantsInput);
        Button confirmButton = dialog.findViewById(R.id.confirmButton);

        confirmButton.setOnClickListener(v -> {
            String roomTitle = roomTitleInput.getText().toString();
            String roomDescription = roomDescriptionInput.getText().toString();
            String maxParticipants = maxParticipantsInput.getText().toString();

            if (roomTitle.isEmpty() || roomDescription.isEmpty() || maxParticipants.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                //이건 로컬로 채팅방 add가 잘 이루어지는 지 확인을 위한 코드
////                chatRoomList.add(new ChatRoom(roomTitle, roomDescription, Integer.parseInt(maxParticipants), "농구"));
////                chatRoomAdapter.notifyDataSetChanged();
////                dialog.dismiss();
//                //카테고리도 가기는 하는 듯
//                // POST 요청 보내는 메서드 호출
//                createChatRoom(roomTitle, roomDescription, Integer.parseInt(maxParticipants), "농구");
//                dialog.dismiss();
                // SharedPreferences에서 저장된 카테고리 ID를 가져옵니다.
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                int categoryId = sharedPreferences.getInt("categoryId", -1); // 기본값 -1은 ID가 없을 경우를 대비한 것입니다.

                if (categoryId == -1) {
                    Toast.makeText(this, "카테고리를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    createChatRoom(roomTitle, roomDescription, Integer.parseInt(maxParticipants), categoryId);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private void createChatRoom(String name, String description, int maxParticipants, int category) {
        String url = "http://15.165.92.121:8080/chat/room"; // 서버 주소 설정

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null);

        if (token == null) {
            Toast.makeText(getApplicationContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("description", description);
            jsonBody.put("maxParticipants", maxParticipants);
            jsonBody.put("categoryId", category);

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, url,
                    response -> {
                        Toast.makeText(getApplicationContext(), "채팅방 생성 성공", Toast.LENGTH_SHORT).show();
                        chatRoomList.add(new ChatRoom(name, description, maxParticipants, category));
                        chatRoomAdapter.notifyDataSetChanged();
                    },
                    error -> {
                        // 서버 오류 메시지 로그 출력
                        Log.e("createChatRoom", "서버 오류 발생: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), "채팅방 생성 실패", Toast.LENGTH_LONG).show();
                    }            ) {
                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes();
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + token); // JWT 토큰을 헤더에 추가
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "JSON 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
}

