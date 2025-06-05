package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MeetingWrite5p  extends AppCompatActivity {
    private EditText introductionInput;
    private ImageButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_5p);

        introductionInput = findViewById(R.id.editTextSearch);
        nextBtn = findViewById(R.id.nextbtn);

        nextBtn.setOnClickListener(v -> {
            String introduction = introductionInput.getText().toString().trim();
            if (introduction.isEmpty()) {
                Toast.makeText(this, "모임 위치를 알려주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
            editor.putString("introduction", introduction);
            editor.apply();

            Intent intent = new Intent(this, MeetingWrite6p.class);
            startActivity(intent);
        });
    }
}
