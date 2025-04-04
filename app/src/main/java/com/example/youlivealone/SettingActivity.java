package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingActivity extends AppCompatActivity {

    private EditText editNickname, editEmail, editEmergencyContact;
    private Button buttonSaveNickname, buttonSaveEmail, buttonSaveEmergencyContact;
    private Switch switchGlobalNotifications, switchCommentNotifications, switchChatNotifications, switchLikeNotifications;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        TextView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, Mypage.class);
            startActivity(intent);
            finish();
        });

        editNickname = findViewById(R.id.edit_nickname);
        editEmail = findViewById(R.id.edit_email);
        editEmergencyContact = findViewById(R.id.edit_emergency_contact);
        buttonSaveNickname = findViewById(R.id.button_save_nickname);
        buttonSaveEmail = findViewById(R.id.button_save_email);
        buttonSaveEmergencyContact = findViewById(R.id.button_save_emergency_contact);

        switchGlobalNotifications = findViewById(R.id.switch_global_notifications);
        switchCommentNotifications = findViewById(R.id.switch_comment_notifications);
        switchChatNotifications = findViewById(R.id.switch_chat_notifications);
        switchLikeNotifications = findViewById(R.id.switch_like_notifications);

        // JWT 토큰에서 userId 추출
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null);

        if (token != null) {
            JWT jwt = new JWT(token);
            userId = jwt.getClaim("sub").asString();
            Log.d("SettingActivity", "Extracted userId (from sub): " + userId);
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 저장 버튼 동작 설정
        buttonSaveNickname.setOnClickListener(v -> saveUserInfo("닉네임", "http://15.165.92.121:8080/mypage/setting/nickname/" + userId, "newNickname", editNickname.getText().toString()));
        buttonSaveEmail.setOnClickListener(v -> saveUserInfo("이메일", "http://15.165.92.121:8080/mypage/setting/email/" + userId, "newEmail", editEmail.getText().toString()));
        buttonSaveEmergencyContact.setOnClickListener(v -> saveUserInfo("비상연락처", "http://15.165.92.121:8080/mypage/setting/emergencyContact/" + userId, "newContact", editEmergencyContact.getText().toString()));

        // 알림 설정 동작
        switchGlobalNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchCommentNotifications.setChecked(isChecked);
            switchChatNotifications.setChecked(isChecked);
            switchLikeNotifications.setChecked(isChecked);
        });
    }

    private void saveUserInfo(String type, String url, String jsonKey, String value) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", userId);
            jsonBody.put(jsonKey, value);

            new Thread(() -> {
                try {
                    URL requestUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(jsonBody.toString());
                    writer.flush();
                    writer.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        runOnUiThread(() -> Toast.makeText(this, type + " 저장 완료", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, type + " 저장 실패", Toast.LENGTH_SHORT).show());
                        Log.e("SaveUserInfoError", "Failed to save " + type + ": " + responseCode);
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, type + " 저장 중 오류 발생", Toast.LENGTH_SHORT).show());
                    Log.e("SaveUserInfoError", "Error saving " + type, e);
                }
            }).start();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "데이터 변환 중 오류 발생", Toast.LENGTH_SHORT).show();
        }
    }
}
