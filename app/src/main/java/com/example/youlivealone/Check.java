package com.example.youlivealone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.HashMap;
import java.util.Map;

public class Check extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private SharedPreferences sharedPreferences;
    private String jwtToken;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check);
        calendarView = findViewById(R.id.calendarView);

        // SharedPreferences에서 JWT 토큰 및 저장된 기분 데이터 가져오기
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        jwtToken = sharedPreferences.getString("jwtToken", null);

        // 저장된 기분 데이터를 불러와 캘린더에 표시
        loadMoodsFromPreferences();

        // Volley 요청 큐 초기화
        requestQueue = Volley.newRequestQueue(this);

        // 오늘 날짜 가져오기
        CalendarDay today = CalendarDay.today();

        // 날짜 선택 리스너 설정 (오늘 날짜 클릭 시 출석 체크 요청 전송)
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (date.equals(today)) {
                showMoodSelectionDialog(today); // 기분 선택 다이얼로그 표시
            } else {
                Toast.makeText(Check.this, "오늘 날짜만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 하단 버튼 기능 설정
        findViewById(R.id.check).setOnClickListener(v -> startActivity(new Intent(Check.this, Check.class)));
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(Check.this, MainActivity.class)));
        findViewById(R.id.chat).setOnClickListener(v -> startActivity(new Intent(Check.this, Chat.class)));
        findViewById(R.id.mypage).setOnClickListener(v -> startActivity(new Intent(Check.this, Mypage.class)));
    }

    // 기분 선택 다이얼로그를 표시하는 메서드
    private void showMoodSelectionDialog(CalendarDay date) {
        String[] moods = {"😀 행복", "😐 보통", "😢 슬픔", "😠 화남"};
        int[] moodImages = {R.drawable.happy, R.drawable.just, R.drawable.sad, R.drawable.angry};

        AlertDialog.Builder builder = new AlertDialog.Builder(Check.this);
        builder.setTitle("오늘의 기분을 선택하세요")
                .setItems(moods, (dialog, which) -> {
                    // 선택한 기분을 SharedPreferences에 저장
                    saveMoodToPreferences(date, moods[which], moodImages[which]);

                    // 선택한 기분 이미지로 데코레이터 설정
                    calendarView.addDecorator(new MoodDecorator(date, moodImages[which]));

                    // 서버에 출석 체크 요청 보내기
                    sendAttendanceCheckRequest();
                });
        builder.create().show();
    }

    // 출석 체크 요청을 보내는 메서드
    private void sendAttendanceCheckRequest() {
        String url = "http://15.165.92.121:8080/attendance/check";

        // JWT 토큰이 존재하는지 확인
        if (jwtToken == null) {
            Toast.makeText(Check.this, "로그인 후 출석 체크가 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Volley를 사용하여 POST 요청을 보내기
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // 서버 응답 성공 시 처리
                    Toast.makeText(Check.this, "출석 체크 완료: " + response, Toast.LENGTH_SHORT).show();
                    Log.d("CheckActivity", "출석 체크 서버 응답: " + response);

                },
                error -> {
                    // 오류 발생 시 처리
                    Log.e("CheckActivity", "출석 체크 실패: " + error.getMessage());
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String errorMsg = new String(error.networkResponse.data);
                        Log.e("CheckActivity", "상태 코드: " + statusCode);
                        Log.e("CheckActivity", "서버 응답 메시지: " + errorMsg);
                    }
                    Toast.makeText(Check.this, "출석 체크 실패", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + jwtToken); // JWT 토큰을 헤더에 추가
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
            }
        };

        // 요청을 요청 큐에 추가
        requestQueue.add(stringRequest);
    }

    // 감정 데이터를 SharedPreferences에 저장하는 메서드
    private void saveMoodToPreferences(CalendarDay date, String mood, int moodImageRes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = date.getYear() + "_" + date.getMonth() + "_" + date.getDay();
        editor.putString(key + "_mood", mood);
        editor.putInt(key + "_moodImageRes", moodImageRes);
        editor.apply();
    }

    // SharedPreferences에서 감정 데이터를 불러와 캘린더에 표시하는 메서드
    private void loadMoodsFromPreferences() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            String[] dateParts = key.split("_");

            if (dateParts.length == 3) { // 키가 날짜 형식인 경우만 처리
                try {
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);
                    CalendarDay date = CalendarDay.from(year, month, day);
                    int moodImageRes = sharedPreferences.getInt(key + "_moodImageRes", 0);

                    // 데코레이터 추가하여 캘린더에 감정 이미지 표시
                    if (moodImageRes != 0) {
                        calendarView.addDecorator(new MoodDecorator(date, moodImageRes));
                    }
                } catch (NumberFormatException e) {
                    Log.e("CheckActivity", "잘못된 날짜 형식 키: " + key);
                }
            }
        }
    }

    // 감정 이미지를 추가하는 MoodDecorator 클래스
    private class MoodDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final int moodImageRes;

        public MoodDecorator(CalendarDay date, int moodImageRes) {
            this.date = date;
            this.moodImageRes = moodImageRes;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            Drawable drawable = ContextCompat.getDrawable(Check.this, moodImageRes);
            view.setBackgroundDrawable(drawable);
        }
    }
}
