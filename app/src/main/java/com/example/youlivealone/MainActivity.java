package com.example.youlivealone;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {
    private static final int TIME_INTERVAL = 1500; // 뒤로가기 버튼을 누른 시간 간격 (1.5초)
    private long mBackPressedTime; // 뒤로가기 버튼을 누른 시간을 저장할 변수
    private static final int PERMISSION_REQUEST_CODE = 1; // 권한 요청 코드

    private ActivityMainBinding mBinding;
    private Handler sliderHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 알림 권한 요청
        requestNotificationPermission();

        // 매일 알림 설정 (예: 오후 7시 44분)
        setDailyNotification(this, 8, 0);

        // 이미지 슬라이드 코드
        ViewPager2 viewPager2 = mBinding.viewPager;
        List<Integer> images = Arrays.asList(
                R.drawable.a,
                R.drawable.a2,
                R.drawable.a3,
                R.drawable.a4
        );
        ImageAdapter adapter = new ImageAdapter(images, viewPager2);
        viewPager2.setAdapter(adapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 2000);
            }
        });

        // 주간 달력 설정 및 감정 데코레이터 적용
        MaterialCalendarView weeklyCalendar = findViewById(R.id.weeklyCalendar);
        applyMoodDecorators(weeklyCalendar);

        // 주간 보기 설정
        weeklyCalendar.state().edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit();

        // 주간 달력 클릭 시 Check 화면으로 이동
        weeklyCalendar.setOnDateChangedListener((widget, date, selected) -> {
            Intent intent = new Intent(MainActivity.this, Check.class);
            startActivity(intent);
        });

        // 버튼 작동 코드들
        mBinding.notice.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Notice.class);
            startActivity(intent);
        });

        mBinding.purchase.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Purchase.class);
            startActivity(intent);
        });

        mBinding.meeting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Meeting.class);
            startActivity(intent);
        });

        mBinding.community.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Community.class);
            startActivity(intent);
        });

        mBinding.smart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Smart.class);
            startActivity(intent);
        });

        mBinding.check.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Check.class);
            startActivity(intent);
        });

        mBinding.home.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        mBinding.chat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Chat.class);
            startActivity(intent);
        });

        mBinding.mypage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Mypage.class);
            startActivity(intent);
        });
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int nextItem = mBinding.viewPager.getCurrentItem() + 1;
            if (nextItem >= mBinding.viewPager.getAdapter().getItemCount()) {
                nextItem = 0;
            }
            mBinding.viewPager.setCurrentItem(nextItem, true);
        }
    };

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

    // 추가된 메서드: 감정 데이터를 불러와 주간 달력에 적용
    private void applyMoodDecorators(MaterialCalendarView calendarView) {
        SharedPreferences sharedPreferences = getSharedPreferences("MoodPreferences", MODE_PRIVATE);
        Map<String, ?> moodEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : moodEntries.entrySet()) {
            String[] dateParts = entry.getKey().split("_");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            CalendarDay date = CalendarDay.from(year, month, day);
            String mood = (String) entry.getValue();
            calendarView.addDecorator(new MoodDecorator(date, mood));
        }
    }

    // MoodDecorator 클래스 정의
    private class MoodDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final String mood;

        public MoodDecorator(CalendarDay date, String mood) {
            this.date = date;
            this.mood = mood;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            int drawableId = getDrawableForMood(mood);
            if (drawableId != 0) {
                Drawable drawable = ContextCompat.getDrawable(MainActivity.this, drawableId);
                view.setBackgroundDrawable(drawable);
            }
        }

        private int getDrawableForMood(String mood) {
            switch (mood) {
                case "😀 행복":
                    return R.drawable.happy;
                case "😐 보통":
                    return R.drawable.just;
                case "😢 슬픔":
                    return R.drawable.sad;
                case "😠 화남":
                    return R.drawable.angry;
                default:
                    return 0;
            }
        }
    }
}
