package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.jwt.JWT;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mypage extends AppCompatActivity {

    private TextView nicknameTextView;
    private TextView pointsTextView;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage);

        nicknameTextView = findViewById(R.id.nickname);
        pointsTextView = findViewById(R.id.points);

        findViewById(R.id.settings_icon).setOnClickListener(v -> openSettingsScreen());
        findViewById(R.id.menu_item_community).setOnClickListener(v -> {
            Intent intent = new Intent(Mypage.this, CommunityActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.menu_item_group).setOnClickListener(v -> {
            Intent intent = new Intent(Mypage.this, GroupActivity.class);
            startActivity(intent);
        });

        // JWT 토큰에서 userId 추출
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("jwtToken", null);

        if (token != null) {
            JWT jwt = new JWT(token);
            userId = jwt.getClaim("sub").asString();
            Log.d("Mypage", "Extracted userId (from sub): " + userId);
        } else {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // userId로 MyPage 업데이트 요청
        updateMypageFromId(userId);

        // 닉네임과 포인트 가져오기
        fetchNickname();
        fetchPoints();

        // 네비게이션 버튼들 설정
        findViewById(R.id.check).setOnClickListener(v -> startActivity(new Intent(Mypage.this, Check.class)));
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(Mypage.this, MainActivity.class)));
        findViewById(R.id.chat).setOnClickListener(v -> startActivity(new Intent(Mypage.this, Chat.class)));
        findViewById(R.id.mypage).setOnClickListener(v -> startActivity(new Intent(Mypage.this, Mypage.class)));
    }

    private void openSettingsScreen() {
        Intent intent = new Intent(Mypage.this, SettingActivity.class);
        startActivity(intent);
    }

    private void updateMypageFromId(String id) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                URL url = new URL("http://15.165.92.121:8080/mypage/updateFromId?id=" + id);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(this, "MyPage 업데이트 성공", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("MypageUpdate", "Failed to update MyPage: " + responseCode);
                    runOnUiThread(() -> Toast.makeText(this, "MyPage 업데이트 실패", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("MypageUpdateError", "Error updating MyPage", e);
                runOnUiThread(() -> Toast.makeText(this, "MyPage 업데이트 중 오류 발생", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchNickname() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            try {
                URL url = new URL("http://15.165.92.121:8080/mypage/nickname/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // 서버 응답이 nickname 값 자체인 경우
                    String nickname = result.toString();

                    mainHandler.post(() -> nicknameTextView.setText(nickname));
                } else {
                    Log.e("NicknameAPI", "Failed to fetch nickname: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("NicknameError", "Error fetching nickname", e);
            }
        });
    }

    private void fetchPoints() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            try {
                URL url = new URL("http://15.165.92.121:8080/mypage/points/" + userId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // 서버 응답이 points 값 자체인 경우
                    String pointsText = result.toString();
                    mainHandler.post(() -> pointsTextView.setText("보유 포인트: " + pointsText + " P"));
                } else {
                    Log.e("PointsAPI", "Failed to fetch points: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("PointsError", "Error fetching points", e);
            }
        });
    }

}
