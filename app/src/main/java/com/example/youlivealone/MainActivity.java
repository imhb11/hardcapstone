package com.example.youlivealone;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.youlivealone.databinding.ActivityMainBinding;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int TIME_INTERVAL = 1500;
    private long mBackPressedTime;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private ActivityMainBinding mBinding;
    private Handler sliderHandler = new Handler();
    private TextView stepView;
    private final Handler stepHandler = new Handler();
    private final Runnable stepUpdater = new Runnable() {
        @Override
        public void run() {
            updateStepTextView();
            stepHandler.postDelayed(this, 1000); // 1초마다 갱신
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        requestNotificationPermission();
        setDailyNotification(this, 8, 0);

        mBinding.notice.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Notice.class);
            startActivity(intent);
        });

        mBinding.purchaseButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Purchase.class);
            startActivity(intent);
        });

        mBinding.meetingButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Meeting.class);
            startActivity(intent);
        });

        mBinding.communityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Community.class);
            startActivity(intent);
        });

        mBinding.counselButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Chatbot.class);
            startActivity(intent);
        });

        mBinding.home.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        mBinding.foots.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Check.class);
            startActivity(intent);
        });

        mBinding.mypage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Mypage.class);
            startActivity(intent);
        });

        stepView = findViewById(R.id.today_steps);
        updateStepTextView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, StepTrackingService.class));
        } else {
            startService(new Intent(this, StepTrackingService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stepHandler.post(stepUpdater);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stepHandler.removeCallbacks(stepUpdater);
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (mBackPressedTime + TIME_INTERVAL > currentTime) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            final Toast toast = Toast.makeText(this, "한 번 더 누르면 종료됩니다", Toast.LENGTH_SHORT);
            toast.show();

            new Handler().postDelayed(toast::cancel, TIME_INTERVAL);
        }
        mBackPressedTime = currentTime;
    }

    public static void setDailyNotification(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void updateStepTextView() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences("StepPrefs", MODE_PRIVATE);
        int steps = prefs.getInt(today, 0);
        stepView.setText("오늘 걸음 수: " + steps);
    }
}  // MainActivity 끝