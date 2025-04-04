package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import androidx.gridlayout.widget.GridLayout;


public class Community extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs"; // SharedPreferences 파일 이름
    private static final String KEY_CATEGORY_ID = "categoryId"; // 저장할 키

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.community);


        //버튼 작동코드들
        findViewById(R.id.check).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, Check.class);
            startActivity(intent);
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, MainActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, Chat.class);
            startActivity(intent);
        });
        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Community.this, Mypage.class);
            startActivity(intent);
        });

        GridLayout populargridLayout = findViewById(R.id.populargrid);
        for(int i = 0; i < populargridLayout.getChildCount(); i++) {
            View child = populargridLayout.getChildAt(i);
            if (child instanceof ImageButton) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Community.this, Chat.class);
                        // 각 버튼에 맞는 데이터를 인텐트에 추가
                        intent.putExtra("buttonId", v.getId());
                        startActivity(intent);
                    }
                });
            }
        }


        GridLayout categorygridLayout = findViewById(R.id.categorygrid);
        for (int i = 0; i < categorygridLayout.getChildCount(); i++) {
            View child = categorygridLayout.getChildAt(i);
            if (child instanceof ImageButton) {
                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        // 각 버튼에 맞는 카테고리 ID를 지정
                        int categoryId = -1;
                        if (v.getId() == R.id.btn5) {
                            categoryId = 1; // 요리
                        } else if (v.getId() == R.id.btn6) {
                            categoryId = 2; // 운동
                        } else if (v.getId() == R.id.btn9) {
                            categoryId = 3; // 취업
                        } else if (v.getId() == R.id.btn7) {
                            categoryId = 4; // 만화
                        } else if (v.getId() == R.id.btn8) {
                            categoryId = 5; // 패션
                        } else if (v.getId() == R.id.btn10) {
                            categoryId = 6; // 여행
                        }


                        // 카테고리 ID를 SharedPreferences에 저장
                        // 카테고리 ID가 유효한 경우에만 저장
                        if (categoryId != -1) {
                            editor.putInt(KEY_CATEGORY_ID, categoryId);
                            editor.apply();
                        }

                        Intent intent = new Intent(Community.this, community_category.class);
                        // 각 버튼에 맞는 데이터를 인텐트에 추가
                        intent.putExtra("buttonId", categoryId);
                        startActivity(intent);
                    }
                });
            }
        }

    }


}