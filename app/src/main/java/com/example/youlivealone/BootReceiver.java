package com.example.youlivealone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 부팅 후 알림 다시 설정
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MainActivity.setDailyNotification(context, 8, 0);  // 부팅 후 알람 재설정 (8시)
        }
    }
}