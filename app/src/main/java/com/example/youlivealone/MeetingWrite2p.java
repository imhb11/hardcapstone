package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONException;

public class MeetingWrite2p extends AppCompatActivity {

    private FlexboxLayout container;
    private RequestQueue queue;
    private TextView categoryText;
    private Button[] selectedButton = {null}; // 현재 선택된 버튼을 추적

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_2p);

        container = findViewById(R.id.buttonContainer);
        queue = Volley.newRequestQueue(this);
        categoryText = findViewById(R.id.categorytext);


        // 카테고리 ID 불러오기
        // 최초에 사용자 정보에서 카테고리 ID 읽기
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int categoryId = userPrefs.getInt("categoryId", -1);

        if (categoryId != -1) {
            String categoryName = getCategoryNameById(categoryId);
            categoryText.setText(categoryName);
            fetchSubcategories(categoryId);
        }

// 다음 버튼 클릭 시
        ImageButton nextBtn = findViewById(R.id.nextbtn);
        nextBtn.setOnClickListener(v -> {
            SharedPreferences meetingPrefs = getSharedPreferences("MeetingData", MODE_PRIVATE);
            int selectedSubcategoryId = meetingPrefs.getInt("subcategoryId", -1);

            if (selectedSubcategoryId != -1) {
                Intent intent = new Intent(MeetingWrite2p.this, MeetingWrite3p.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "세부 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        });


    }
    private String getCategoryNameById(int id) {
        switch (id) {
            case 1: return "운동";
            case 2: return "친목";
            case 3: return "동창회";
            case 4: return "음식";
            case 5: return "스터디";
            case 6: return "문화";
            default: return "알 수 없음";
        }
    }


    private void fetchSubcategories(int categoryId) {
        // 실제 API 주소 사용 (예시: https://your-server.com/api/categories/1/subcategories)
        String url = "http://15.165.92.121:8080/meetings/categoryId/"+categoryId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    container.removeAllViews(); // 기존 버튼 제거

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            String subcategory = response.getJSONObject(i).getString("name");
                            int subcategoryId = response.getJSONObject(i).getInt("id");
                            createSubcategoryButton(subcategory,subcategoryId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                }
        );

        queue.add(request);
    }

    private void createSubcategoryButton(String subcategory,int subcategoryId) {
        Button btn = new Button(this);
        btn.setText(subcategory);
        btn.setBackgroundResource(R.drawable.meeting_2pbtn); // selector 사용
        btn.setTextColor(Color.parseColor("#4D3005"));
        btn.setAllCaps(false);

        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(8, 8, 8, 8);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> {
            // 이미 선택된 버튼을 다시 클릭하면 선택 해제
            if (selectedButton[0] == btn) {
                btn.setBackgroundResource(R.drawable.meeting_2pbtn); // 원래 상태로
                btn.setTextColor(Color.parseColor("#4D3005"));
                selectedButton[0] = null;

                // 선택 제거
                SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
                editor.remove("selectedSubcategory");
                editor.remove("subcategoryId");
                editor.apply();
            } else {
                // 다른 버튼을 눌렀을 때 기존 선택 초기화
                if (selectedButton[0] != null) {
                    selectedButton[0].setBackgroundResource(R.drawable.meeting_2pbtn);
                    selectedButton[0].setTextColor(Color.parseColor("#4D3005"));
                }

                // 현재 버튼 선택
                btn.setBackgroundColor(Color.parseColor("#FF9861")); // 오렌지색
                btn.setTextColor(Color.WHITE);
                selectedButton[0] = btn;

                // 선택 저장
                SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
                editor.putInt("subcategoryId", subcategoryId);
                editor.apply();
            }
        });

        container.addView(btn);
    }
}
