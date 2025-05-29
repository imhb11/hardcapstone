package com.example.youlivealone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Check extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private final String TAG = "CheckActivity";
    private RequestQueue queue;
    private FitnessOptions fitnessOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check);

        calendarView = findViewById(R.id.calendarView);
        queue = Volley.newRequestQueue(this);
        setFullMonthView();

        // ✅ 1. FitnessOptions 구성
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();

        // ✅ 2. GoogleSignInOptions 명시적으로 설정 + Fit 권한 요청 포함
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/fitness.activity.read"))
                .build();

        // ✅ 3. GoogleSignIn 계정 획득
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        Log.d("CheckActivity", "account = " + account);
        Log.d("CheckActivity", "hasPermissions = " + GoogleSignIn.hasPermissions(account, fitnessOptions));


        // ✅ 4. 권한 확인 및 요청
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.d(TAG, "Google Fit 권한 없음 → 요청 중");
            GoogleSignIn.requestPermissions(
                    this,
                    1001,
                    account,
                    fitnessOptions
            );
        } else {
            Log.d(TAG, "Google Fit 권한 있음 → 걸음수 요청 시작");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION},
                            2001);  // requestCode는 자유
                } else {
                    getTodayStepCountAndPost(); // 권한 있으면 실행
                }
            } else {
                getTodayStepCountAndPost(); // Android 9 이하면 바로 실행
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (resultCode == RESULT_OK) {
                getTodayStepCountAndPost(); // 사용자가 동의했을 경우
            } else {
                Toast.makeText(this, "Google Fit 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setFullMonthView() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        CalendarDay min = CalendarDay.from(cal);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        CalendarDay max = CalendarDay.from(cal);

        calendarView.state().edit().setMinimumDate(min).setMaximumDate(max).commit();
    }

    private int extractMemberIdFromJWT() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwtToken", null);
        if (token == null) return -1;

        try {
            Log.d("JWT", "Raw token: " + token);
            String[] parts = token.split("\\.");
            byte[] payload = Base64.decode(parts[1], Base64.URL_SAFE);
            String json = new String(payload, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            Log.d("JWT", "Decoded payload: " + json);  // 이거 꼭 찍어봐야 함
            return obj.getInt("memberId");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

    }

    private void getTodayStepCountAndPost() {
        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(start.getTimeInMillis(), end.getTimeInMillis(), TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    int steps = 0;

                    if (!response.getBuckets().isEmpty()) {
                        List<DataSet> dataSets = response.getBuckets().get(0).getDataSets();
                        if (!dataSets.isEmpty()) {
                            List<DataPoint> dataPoints = dataSets.get(0).getDataPoints();
                            if (!dataPoints.isEmpty()) {
                                steps = dataPoints.get(0).getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }

                    postStepsToServer(steps);
                })

                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "걸음 수 측정 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void postStepsToServer(int steps) {
        int memberId = extractMemberIdFromJWT();
        if (memberId == -1) {
            Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://15.165.92.121:8080/pedometers";

        JSONObject memberObj = new JSONObject();
        JSONObject pedometerObj = new JSONObject();
        try {
            memberObj.put("memberId", memberId);
            pedometerObj.put("pedometerId", 0);
            pedometerObj.put("steps", steps);
            pedometerObj.put("recordDate", Instant.now().toString());
            pedometerObj.put("member", memberObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, pedometerObj,
                response -> handlePedometerResponse(new JSONArray().put(response)),
                error -> Log.e(TAG, "Post failed: " + error.toString())
        );

        queue.add(request);
    }

    private void handlePedometerResponse(JSONArray response) {
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.getJSONObject(i);
                int steps = item.getInt("steps");
                String recordDate = item.getString("recordDate");

                LocalDate date = Instant.parse(recordDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                CalendarDay day = CalendarDay.from(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
                calendarView.addDecorator(new StepDecorator(day, steps, this));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class StepDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final int steps;
        private final Context context;

        public StepDecorator(CalendarDay date, int steps, Context context) {
            this.date = date;
            this.steps = steps;
            this.context = context;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StepTextSpan(context, steps));
        }
    }

    public static class StepTextSpan extends android.text.style.ReplacementSpan {
        private final int steps;
        private final Context context;

        public StepTextSpan(Context context, int steps) {
            this.steps = steps;
            this.context = context;
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            return (int) paint.measureText(text, start, end);
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, @NonNull Paint paint) {
            paint.setColor(Color.BLACK);
            paint.setTextSize(40f);
            canvas.drawText(text, start, end, x, y, paint);
            paint.setTextSize(24f);
            paint.setColor(Color.parseColor("#FF9861"));
            canvas.drawText(steps + "걸음", x, y + 30, paint);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getTodayStepCountAndPost();
            } else {
                Toast.makeText(this, "걸음 수 인식 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
