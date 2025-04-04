package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddMessageBoard extends AppCompatActivity {

    private EditText titleInput, contentInput;
    private ImageButton uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addmessageboard); // addmessageboard.xml 레이아웃 파일 참조

        // UI 요소를 findViewById로 연결
        titleInput = findViewById(R.id.title_input);
        contentInput = findViewById(R.id.content_input);
        uploadButton = findViewById(R.id.upload_button);

        // 업로드 버튼 클릭 이벤트 처리
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleInput.getText().toString();
                String content = contentInput.getText().toString();
                // 업로드 기능 구현

                // 카테고리 ID를 SharedPreferences에서 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                int categoryId = sharedPreferences.getInt("categoryId", -1); // 카테고리 ID


                uploadPost(title, content, categoryId);

            }
        });




    }

    private void uploadPost(String title, String content, int categoryId) {
        // SharedPreferences에서 필요한 정보 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null); // JWT 토큰
        String authorId = sharedPreferences.getString("userID", null); // 작성자 ID (로그인 시 저장한 ID)

        if (token == null || categoryId == -1 || authorId == null) {
            Toast.makeText(getApplicationContext(), "로그인이 필요하거나 카테고리가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://15.165.92.121:8080/posts/create"; // 게시글 작성 API URL

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", title); // 제목 추가
            jsonBody.put("content", content); // 내용 추가
            jsonBody.put("categoryId", categoryId); // 카테고리 ID 추가

            String requestParam = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        // 게시글 작성 성공 처리
                        Toast.makeText(getApplicationContext(), "게시글 작성 완료", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AddMessageBoard.this, MessageBoard.class);
                        startActivity(intent);
                        finish(); // 현재 Activity 종료
                    },
                    error -> {
                        Log.e("PostArticleError", "게시글 작성 요청 실패: " + error.toString());
                        Toast.makeText(getApplicationContext(), "게시글 작성 실패: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");

                    // 저장된 JWT 토큰을 가져옴
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String token = sharedPreferences.getString("jwtToken", null);

                    if (token != null) {
                        headers.put("Authorization", "Bearer " + token); // Authorization 헤더에 토큰 추가
                    }

                    return headers;
                }



                @Override
                public byte[] getBody() {
                    return requestParam == null ? null : requestParam.getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            // 설정된 타임아웃 값을 10초로 변경
            int timeoutMs = 10000; // 10초
            RetryPolicy retryPolicy = new DefaultRetryPolicy(timeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(retryPolicy);

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "JSON 구성 중 예외 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}




