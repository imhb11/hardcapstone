package com.example.youlivealone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;


public class StepTrackingService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int previousSensorValue = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        startForeground(1, getNotification());
    }

    private Notification getNotification() {
        String channelId = "step_tracking_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Step Tracking", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("걸음 수 추적 중")
                .setContentText("현재 걸음 수를 측정 중입니다")
                .setSmallIcon(R.drawable.footbutton)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int current = (int) event.values[0];
            if (previousSensorValue == 0) {
                previousSensorValue = current;
                return;
            }

            int stepDiff = current - previousSensorValue;
            previousSensorValue = current;

            if (stepDiff > 0) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                SharedPreferences prefs = getSharedPreferences("StepPrefs", MODE_PRIVATE);
                int savedSteps = prefs.getInt(today, 0);
                prefs.edit().putInt(today, savedSteps + stepDiff).apply();
                Log.d("StepService", "누적 - " + today + ": " + (savedSteps + stepDiff));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 서비스에 바인딩하지 않음
    }
}