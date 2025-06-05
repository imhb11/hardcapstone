package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MeetingWrite1p extends AppCompatActivity {
    private EditText titleInput;
    private ImageButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_1p);

        titleInput = findViewById(R.id.titletext);
        nextBtn = findViewById(R.id.nextbtn);

        nextBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "모임 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
            editor.putString("title", title);
            editor.apply();

            Intent intent = new Intent(this, MeetingWrite2p.class);
            startActivity(intent);
        });
    }
}
