package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MeetingWrite3p extends AppCompatActivity {
    private EditText maxMemberInput;
    private ImageButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_3p);

        maxMemberInput = findViewById(R.id.peopletext);
        nextBtn = findViewById(R.id.nextbtn);

        nextBtn.setOnClickListener(v -> {
            String input = maxMemberInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "모임의 참여인원을 작성해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int maxMembers = Integer.parseInt(input); // ✅ 문자열을 int로 변환
                if (maxMembers <= 0) {
                    Toast.makeText(this, "참여 인원은 1명 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
                editor.putInt("maxMembers", maxMembers);
                editor.apply();

                Intent intent = new Intent(this, MeetingWrite4p.class);
                startActivity(intent);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "숫자만 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
