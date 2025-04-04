package com.example.youlivealone;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Notice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);

        // 기존 버튼 작동 코드
        findViewById(R.id.check).setOnClickListener(v -> {
            startActivity(new Intent(Notice.this, Check.class));
        });

        findViewById(R.id.home).setOnClickListener(v -> {
            startActivity(new Intent(Notice.this, MainActivity.class));
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            startActivity(new Intent(Notice.this, Chat.class));
        });

        findViewById(R.id.mypage).setOnClickListener(v -> {
            startActivity(new Intent(Notice.this, Mypage.class));
        });
    }
}