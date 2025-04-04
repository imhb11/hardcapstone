package com.example.youlivealone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private RecyclerView chatMessagesRecyclerView;
    private ChatMessageAdapter chatMessageAdapter;
    private ArrayList<ChatMessage> chatMessages;
    private EditText messageInput;
    private ImageButton sendButton;
    private String userId; // 유저 ID
    private String jwtToken; // JWT 토큰
    private String chatRoomId; // 채팅방 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 채팅방 ID와 채팅방 이름을 Intent에서 가져오기
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        String chatRoomName = getIntent().getStringExtra("chatRoomName");

        // 채팅방 ID를 Integer로 받아 String으로 변환
        int chatRoomIdInt = getIntent().getIntExtra("chatRoomId", -1);
        if (chatRoomIdInt == -1) {
            Toast.makeText(this, "채팅방 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        chatRoomId = String.valueOf(chatRoomIdInt);

        TextView chatRoomTitle = findViewById(R.id.chatRoomTitle);
        chatRoomTitle.setText(chatRoomName);

        // 뷰 초기화
        chatMessagesRecyclerView = findViewById(R.id.chatMessagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // SharedPreferences에서 사용자 ID와 JWT 토큰 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        jwtToken = sharedPreferences.getString("jwtToken", ""); // JWT 토큰

        // userId가 제대로 설정되었는지 로그로 확인해 봅니다.
        Log.d("ChatRoomActivity", "User ID: " + userId);

        // RecyclerView 설정
        chatMessages = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(chatMessages,userId);
        chatMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatMessagesRecyclerView.setAdapter(chatMessageAdapter);

        // 메시지 전송 버튼 클릭 리스너
        sendButton.setOnClickListener(v -> sendMessage());

        loadChatMessages();
    }

    private void loadChatMessages() {
        String url = "http://15.165.92.121:8080/chat/room/" + chatRoomId + "/message-list";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray messagesArray = response.getJSONArray("messages");
                        chatMessages.clear();

                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObject = messagesArray.getJSONObject(i);

                            int messageId = messageObject.getInt("id");
                            String content = messageObject.getString("content");
                            String senderId = messageObject.getString("senderId");
                            String timestamp = messageObject.getString("timestamp");

                            ChatMessage message = new ChatMessage(senderId, content, timestamp);
                            chatMessages.add(message);
                        }

                        chatMessageAdapter.notifyDataSetChanged();
                        chatMessagesRecyclerView.scrollToPosition(chatMessages.size() - 1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "메시지 로드 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "메시지 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken); // JWT 토큰 추가
                return headers;
            }
        };

        queue.add(request);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "메시지를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://15.165.92.121:8080/chat/room/" + chatRoomId + "/message";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("content", messageText); // 메시지 내용
            jsonBody.put("senderId", userId); // 발신자 ID

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            String timestamp = response.getString("timestamp");

                            // 새로운 메시지 객체 생성 후 리스트에 추가
                            ChatMessage newMessage = new ChatMessage(userId, messageText, timestamp);
                            chatMessages.add(newMessage);
                            chatMessageAdapter.notifyItemInserted(chatMessages.size() - 1);
                            chatMessagesRecyclerView.scrollToPosition(chatMessages.size() - 1);

                            messageInput.setText(""); // 메시지 전송 후 입력창 초기화
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(ChatRoomActivity.this, "메시지 전송 실패", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + jwtToken); // JWT 토큰 추가
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
