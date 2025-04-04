package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    EditText ID, Password;
    Button Login;
    TextView Signup;
    private static final int TIME_INTERVAL = 1500; // 뒤로가기 버튼을 누른 시간 간격 (1.5초)
    private long mBackPressedTime; // 뒤로가기 버튼을 누른 시간을 저장할 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        ID = findViewById(R.id.editID);
        Password = findViewById(R.id.ediPassword);
        Login = findViewById(R.id.loginbutton);
        Signup = findViewById(R.id.signin);

        Login.setOnClickListener(view -> {
            String id = ID.getText().toString().trim();
            String pw = Password.getText().toString().trim();

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://15.165.92.121:8080/members/login";

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("id", id);
                jsonBody.put("pw", pw);

                String requestParam = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        url,
                        response -> {
                            // 서버에서 응답으로 JWT 토큰을 직접 받음
                            String token = response.trim(); // 토큰은 문자열 형태로 반환됨

                            // 로그인 성공 시 JWT 토큰 저장
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userID", id);
                            editor.putString("jwtToken", token); // JWT 토큰 저장
                            editor.apply();

                            // 메인 액티비티로 이동
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 로그인 액티비티 종료
                        },
                        error -> {
                            Log.e("LoginError", "서버 요청 실패: " + error.toString());
                            Toast.makeText(getApplicationContext(), "서버 요청 실패: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ) {
                    @Override
                    public byte[] getBody() {
                        return requestParam == null ? null : requestParam.getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                };

                // 설정된 타임아웃 값을 10초로 변경
                int timeoutMs = 10000; // 10초
                RetryPolicy retryPolicy = new DefaultRetryPolicy(timeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                stringRequest.setRetryPolicy(retryPolicy);

                RequestQueue queue = Volley.newRequestQueue(Login.this);
                queue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "JSON 구성 중 예외 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });





        // 회원가입 버튼 누르면 회원가입 페이지로 이동
        Signup.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (mBackPressedTime + TIME_INTERVAL > currentTime) {
            super.onBackPressed();
        } else {
            Toast toast = Toast.makeText(this, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT);
            toast.show();

            new Handler().postDelayed(toast::cancel, TIME_INTERVAL);
        }

        mBackPressedTime = currentTime;
    }
}