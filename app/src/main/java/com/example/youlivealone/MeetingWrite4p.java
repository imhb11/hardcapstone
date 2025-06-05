package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MeetingWrite4p  extends AppCompatActivity {
    private EditText addressInput;
    private ImageButton nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_write_4p);

        addressInput = findViewById(R.id.addresstext);
        nextBtn = findViewById(R.id.nextbtn);

        nextBtn.setOnClickListener(v -> {
            String address = addressInput.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "모임 위치를 알려주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("MeetingData", MODE_PRIVATE).edit();
            editor.putString("address", address);
            editor.apply();

            Intent intent = new Intent(this, MeetingWrite5p.class);
            startActivity(intent);
        });
    }
}
