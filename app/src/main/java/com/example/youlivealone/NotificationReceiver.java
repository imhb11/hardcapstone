package com.example.youlivealone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;

import com.example.youlivealone.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "daily_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 알림 채널 생성 (Android 8.0 이상)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Daily Notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 빌더 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.image1)
                .setContentTitle("살았음?")
                .setContentText("살았냐고")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 알림 표시
        notificationManager.notify(1001, builder.build());
    }
}

