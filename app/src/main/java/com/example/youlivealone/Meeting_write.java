package com.example.youlivealone;

import static com.google.android.material.internal.ViewUtils.dpToPx;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Meeting_write extends AppCompatActivity {
    private EditText titleInput, introductionInput, contentInput,locationInput;
    private Button uploadButton;
    LinearLayout subcategoryContainer;
    private static final String SUBCATEGORY_URL = "http://15.165.92.121:8080/meetings/categoryId/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write);

        uploadButton = findViewById(R.id.meeting_upload);
        titleInput = findViewById(R.id.meeting_write_title);
        introductionInput = findViewById(R.id.meeting_write_summary);
        contentInput = findViewById(R.id.meeting_text);
        subcategoryContainer = findViewById(R.id.linearLayout);
        locationInput = findViewById(R.id.meeting_write_location); // Add this line

        //서브카테고리 가져오기
        fetchSubcategories();



        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleInput.getText().toString();
                String introduction = introductionInput.getText().toString();
                String content = contentInput.getText().toString();
                String address = locationInput.getText().toString(); // Get the location input

                // 업로드 기능 구현

                // 유효성 검사
                if (title.isEmpty() || introduction.isEmpty() || content.isEmpty() || address.isEmpty()) { // Include location in validation
                    Toast.makeText(getApplicationContext(), "모든 필드를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 카테고리 ID를 SharedPreferences에서 가져오기
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                int categoryId = sharedPreferences.getInt("categoryId", -1); // 카테고리 ID
                int subcategoryId = sharedPreferences.getInt("subcategoryId", -1); // 서브카테고리 ID
                int maxMembers = 10; // 최대 인원 수 설정


                uploadPost(title, introduction, content,maxMembers,address, categoryId, subcategoryId);

            }
        });

    }
    private void uploadPost(String title, String introduction, String content, int maxMembers, String address, int categoryId, int subcategoryId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null);
        String authorId = sharedPreferences.getString("userID", null);

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

            StringRequest stringRequest = new StringRequest(
                    Request.Method.POST,
                    url,
                    response -> {
                        Toast.makeText(getApplicationContext(), "게시글 작성 완료", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Meeting_write.this, Meeting_category.class);
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
                public Map<String, String> getHeaders() throws AuthFailureError {
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


    private void fetchSubcategories() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int categoryId = sharedPreferences.getInt("categoryId", -1);
        String url = SUBCATEGORY_URL + categoryId; // Construct the UR

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("Response", "세부카테고리 목록 서버 응답: " + response);
                    setupLinearLayout(response);
                },
                error -> {
                    Log.e("Meeting_category", "카테고리 로드 실패: " + error.getMessage());
                    Toast.makeText(Meeting_write.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(jsonArrayRequest);
    }
    private void setupLinearLayout(JSONArray subcategoryArray) {
        subcategoryContainer.removeAllViews(); // 기존 뷰 삭제

        for (int i = 0; i < subcategoryArray.length(); i++) {
            try {
                JSONObject subcategory = subcategoryArray.getJSONObject(i);
                if (subcategory.has("name") && subcategory.has("id")) { // 키 존재 확인
                    String name = subcategory.getString("name");
                    int subcategoryId = subcategory.getInt("id");

                    TextView textView = new TextView(this);
                    textView.setText(name);
                    textView.setTextColor(Color.parseColor("#BAA681")); // 색깔 설정
                    textView.setPadding(16, 16, 16, 16); // 패딩 설정

                    // 텍스트뷰의 높이 및 너비를 설정할 수도 있습니다.
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                            dpToPx(56),
//                            dpToPx(40)                     );
//                    textView.setLayoutParams(params);

                    textView.setOnClickListener(v -> {
                        // 선택한 세부 카테고리 ID 저장
                        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("subcategoryId", subcategoryId); // 서브카테고리 ID 저장
                        editor.apply();

                        // 서버에 데이터 전송 등 추가 작업 수행
                        Log.d("SubcategorySelected", "Selected subcategory ID: " + subcategoryId);

                        Toast.makeText(Meeting_write.this, name + " 카테고리가 선택되었습니다.", Toast.LENGTH_SHORT).show();

                    });

                    subcategoryContainer.addView(textView);
                }
            } catch (JSONException e) {
                Log.e("JSONParseError", "서브카테고리 JSON 파싱 중 오류 발생: " + e.getMessage());
            }
        }
    }




}
