package com.example.youlivealone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {
    TextView back;
    EditText name, ID, PW, pw2, email, birthyear, birthdate, birthday, nickname;
    Button pwcheck, submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        submit = findViewById(R.id.signupbutton);
        ID = findViewById(R.id.signID);
        PW = findViewById(R.id.signPW);

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> onBackPressed());

        name = findViewById(R.id.signName);
        pw2 = findViewById(R.id.signPW2);
        email = findViewById(R.id.signmail);
        birthyear = findViewById(R.id.signBirth);
        birthdate = findViewById(R.id.signBirth2);
        birthday = findViewById(R.id.signBirth3);
        nickname = findViewById(R.id.nickname);

        pwcheck = findViewById(R.id.pwcheckbutton);
        pwcheck.setOnClickListener(v -> {
            if (PW.getText().toString().equals(pw2.getText().toString())) {
                pwcheck.setText("일치");
            } else {
                Toast.makeText(Register.this, "비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
            }
        });

        submit.setOnClickListener(view -> {
            String id = ID.getText().toString();
            String pw = PW.getText().toString();
            String Name = name.getText().toString();
            String Email = email.getText().toString();
            String Birthdate = birthdate.getText().toString();
            String Nickname = nickname.getText().toString();

            String url = "http://15.165.92.121:8080/members/register";

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("id", id);
                jsonBody.put("pw", pw);
                jsonBody.put("name", Name);
                jsonBody.put("email", Email);
                jsonBody.put("birthDate", Birthdate);
                jsonBody.put("nickname", Nickname);

                String requestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST,
                        url,
                        response -> {
                            Log.d("RegisterResponse", response);  // 응답 로그

                            // 서버에서 반환된 문자열을 기반으로 처리
                            if (response.contains("회원가입이 완료되었습니다!")) {
                                Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // 응답 내용에 따라 적절히 처리
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Log.e("RegisterError", "서버 요청 실패: " + error.toString());  // 에러 로그
                            Toast.makeText(getApplicationContext(), "서버 요청 실패: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ) {
                    @Override
                    public byte[] getBody() {
                        return requestBody == null ? null : requestBody.getBytes();
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json; charset=utf-8";
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "예외 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}