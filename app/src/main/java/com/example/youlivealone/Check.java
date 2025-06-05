package com.example.youlivealone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.checkerframework.checker.units.qual.C;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Check extends AppCompatActivity implements SensorEventListener {

    private MaterialCalendarView calendarView;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int previousSensorValue = 0;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;
    private final Handler stepHandler = new Handler();
    private final Runnable stepUpdater = new Runnable() {
        @Override
        public void run() {
            displayStepsFromLocal();
            stepHandler.postDelayed(this, 1000); // 1초마다 갱신
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check);

        calendarView = findViewById(R.id.calendarView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            Toast.makeText(this, "걸음 수 센서를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            }
        }

        setFullMonthView();
        displayStepsFromLocal();
        scheduleDailyUpload();

        findViewById(R.id.home).setOnClickListener(v -> {
            Intent intent = new Intent(Check.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.mypage).setOnClickListener(v -> {
            Intent intent = new Intent(Check.this, Mypage.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
        stepHandler.post(stepUpdater);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stepHandler.removeCallbacks(stepUpdater);
    }

    private void setFullMonthView() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        CalendarDay min = CalendarDay.from(cal);
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 2);
        CalendarDay max = CalendarDay.from(cal);
        calendarView.state().edit().setMinimumDate(min).setMaximumDate(max).commit();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentSensorValue = (int) event.values[0];

            if (previousSensorValue == 0) {
                previousSensorValue = currentSensorValue;
                return;
            }

            int stepDiff = currentSensorValue - previousSensorValue;
            if (stepDiff > 0) {
                saveStepsToLocal(stepDiff);
            }

            previousSensorValue = currentSensorValue;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void saveStepsToLocal(int stepDiff) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences("StepPrefs", MODE_PRIVATE);
        int savedSteps = prefs.getInt(today, 0);
        int totalSteps = savedSteps + stepDiff;
        prefs.edit().putInt(today, totalSteps).apply();
        Log.d("StepTracker", "누적 저장 - 날짜: " + today + ", 기존: " + savedSteps + ", 추가: " + stepDiff + ", 총합: " + totalSteps);
    }

    private void displayStepsFromLocal() {
        calendarView.removeDecorators();
        List<DayViewDecorator> decorators = new ArrayList<>();

        Calendar startCal = Calendar.getInstance();
        startCal.add(Calendar.MONTH, -2);

        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.MONTH, 2);

        Calendar today = Calendar.getInstance();

        while (!startCal.after(endCal)) {
            int year = startCal.get(Calendar.YEAR);
            int month = startCal.get(Calendar.MONTH); // 0-based
            int day = startCal.get(Calendar.DAY_OF_MONTH);

            boolean isBeforeOrToday =
                    (year < today.get(Calendar.YEAR)) ||
                            (year == today.get(Calendar.YEAR) && month < today.get(Calendar.MONTH)) ||
                            (year == today.get(Calendar.YEAR) && month == today.get(Calendar.MONTH) && day < today.get(Calendar.DAY_OF_MONTH));

            if (isBeforeOrToday) {
                CalendarDay calendarDay = CalendarDay.from(year, month, day);
                decorators.add(new StepDecorator(calendarDay, 0));
            }

            boolean isToday =
                    (year == today.get(Calendar.YEAR)) &&
                            (month == today.get(Calendar.MONTH)) &&
                            (day == today.get(Calendar.DAY_OF_MONTH));

            if (isToday) {
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                SharedPreferences prefs = getSharedPreferences("StepPrefs", MODE_PRIVATE);
                int todaySteps = prefs.getInt(todayStr, 0);

                CalendarDay calendarDay = CalendarDay.from(year, month, day);
                decorators.add(new StepDecorator(calendarDay, todaySteps));
            }


            startCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (DayViewDecorator decorator : decorators) {
            calendarView.addDecorator(decorator);
        }

        calendarView.invalidateDecorators();
    }

    private void fetchStepsFromServer() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = userPrefs.getString("jwtToken", null);
        int memberId = extractMemberIdFromJWT(token);

        if (memberId == -1) return;

        String url = "http://15.165.92.121:8080/pedometers/" + memberId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    SharedPreferences prefs = getSharedPreferences("StepPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String date = obj.getString("recordDate").substring(0, 10);
                            int steps = obj.getInt("steps");
                            editor.putInt(date, steps);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    editor.apply();
                    displayStepsFromLocal();
                },
                error -> Log.e("StepTracker", "서버로부터 데이터 받기 실패: " + error.toString())
        );

        queue.add(request);
    }

    private void scheduleDailyUpload() {
        Intent intent = new Intent(this, StepUploadReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);
    }

    public static int extractMemberIdFromJWT(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] payload = Base64.decode(parts[1], Base64.URL_SAFE);
            String json = new String(payload, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            return obj.getInt("memberId");
        } catch (Exception e) {
            return -1;
        }
    }

    public static class StepDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final int steps;

        public StepDecorator(CalendarDay date, int steps) {
            this.date = date;
            this.steps = steps;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StepTextSpan(steps)); // 걸음 수 0이라도 모두 표시
        }

    }

    public static class StepTextSpan implements android.text.style.LineBackgroundSpan {
        private final int steps;
        private final Paint paint;

        public StepTextSpan(int steps) {
            this.steps = steps;
            this.paint = new Paint();
            paint.setColor(Color.BLACK); // 강조 색상으로
            paint.setTextSize(40f);    // 적절한 크기
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
        }

        @Override
        public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom,
                                   CharSequence text, int start, int end, int lineNumber) {
            float x = (left + right) / 2f;
            float y = (top + bottom) / 2f + 65f;
            canvas.drawText(String.valueOf(steps), x, y, this.paint);
        }
    }


    public static class StepUploadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences prefs = context.getSharedPreferences("StepPrefs", Context.MODE_PRIVATE);
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            int steps = prefs.getInt(today, 0);

            SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String token = userPrefs.getString("jwtToken", null);
            int memberId = extractMemberIdFromJWT(token);

            JSONObject body = new JSONObject();
            try {
                body.put("memberId", memberId);
                body.put("steps", steps);
                body.put("recordDate", today + "T00:00:00Z");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.POST, "http://15.165.92.121:8080/pedometers",
                    response -> Log.d("StepUpload", "Success: " + response),
                    error -> Log.e("StepUpload", "Failed: " + error.toString())) {
                @Override
                public byte[] getBody() {
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json");
                    if (token != null) headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            queue.add(request);
        }
    }
} // 클래스 끝