package com.example.youlivealone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private Button sendButton;

    private RequestQueue requestQueue;  // Volley 요청 큐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatMessageAdapter((ArrayList<ChatMessage>) chatMessages, "user");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Volley 요청큐 초기화
        requestQueue = Volley.newRequestQueue(this);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString();
        if (!message.isEmpty()) {
            chatMessages.add(new ChatMessage("user", message));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            recyclerView.scrollToPosition(chatMessages.size() - 1);
            editTextMessage.setText("");

            sendToServer(message);
        }
    }

    private void sendToServer(String message) {
        String url = "http://15.165.92.121:8080/counsel/chat"; // 실제 엔드포인트로 변경하세요

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("question", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    // 서버 응답 처리
                    try {
                        String answer = response.getString("answer"); // JSON 키명 서버에 맞게 변경
                        chatMessages.add(new ChatMessage("다락이", answer));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        chatMessages.add(new ChatMessage("bot", "응답 파싱 오류"));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    }
                },
                error -> {
                    chatMessages.add(new ChatMessage("bot", "오류: " + error.toString()));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                }

        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000, // timeout in milliseconds (10초)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsonObjectRequest);
    }
}
