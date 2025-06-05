package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MeetingWrite6p extends AppCompatActivity {
    private EditText contentInput;
    private ImageButton uploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_6p);

        contentInput = findViewById(R.id.editTextSearch);
        uploadBtn = findViewById(R.id.uploadbtn);

        uploadBtn.setOnClickListener(v -> {
            String content = contentInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "모임 내용을 자세히 알려주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
            editor.putString("content", content);
            editor.apply();
            uploadPost();
        });
    }
    private void uploadPost() {
        SharedPreferences sharedPreferences = getSharedPreferences("MeetingData", MODE_PRIVATE);
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String token = userPrefs.getString("jwtToken", null);
        String authorId = userPrefs.getString("userID", null);

        String title = sharedPreferences.getString("title", "");
        String introduction = sharedPreferences.getString("introduction", "");
        String content = sharedPreferences.getString("content", "");
        String address = sharedPreferences.getString("address", "");
        int categoryId = userPrefs.getInt("categoryId", -1);
        int subcategoryId = userPrefs.getInt("subcategoryId", -1);
        int maxMembers = sharedPreferences.getInt("maxMembers", 10);

        Log.d("MeetingWrite6p", "title: " + title);
        Log.d("MeetingWrite6p", "introduction: " + introduction);
        Log.d("MeetingWrite6p", "content: " + content);
        Log.d("MeetingWrite6p", "address: " + address);
        Log.d("MeetingWrite6p", "categoryId: " + categoryId);
        Log.d("MeetingWrite6p", "subcategoryId: " + subcategoryId);
        Log.d("MeetingWrite6p", "maxMembers: " + maxMembers);


        if (token == null || subcategoryId == -1 || authorId == null) {
            Toast.makeText(getApplicationContext(), "로그인이 필요하거나 카테고리가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://15.165.92.121:8080/meetings/create";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", title);
            jsonBody.put("introduction", introduction);
            jsonBody.put("content", content);
            jsonBody.put("maxMembers", maxMembers); // 추가 확인
            jsonBody.put("address", address); // 서버에서 address 또는 location 확인

            JSONObject meetingCategory = new JSONObject();
            meetingCategory.put("id", categoryId);
            jsonBody.put("meetingCategory", meetingCategory);

            JSONObject subcategory = new JSONObject();
            subcategory.put("id", subcategoryId);
            jsonBody.put("subcategory", subcategory);

            String requestParam = jsonBody.toString();
            Log.d("MeetingWrite6p", "Request JSON: " + requestParam);

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        Toast.makeText(getApplicationContext(), "게시글 작성 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MeetingWrite6p.this, Meeting_category.class);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        Log.e("PostArticleError", "게시글 작성 요청 실패: " + error.toString());
                        if (error.networkResponse != null) {
                            Toast.makeText(getApplicationContext(), "에러 코드: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "네트워크 에러 발생: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token);
                    }
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return requestParam.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "데이터 생성 오류 발생", Toast.LENGTH_LONG).show();
        }
    }

}