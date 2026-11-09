package com.musa.chargeanim;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ChargingService extends Service {
    static final String ACTION_TEST = "com.musa.chargeanim.TEST";
    private static final String CHANNEL = "charge";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { launch(false); }
    };

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Charge Anim",
                NotificationManager.IMPORTANCE_MIN));
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Charging animation is on")
                .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
                .build();
        startForeground(1, n);
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_TEST.equals(intent.getAction())) launch(true);
        return START_STICKY;
    }

    private void launch(boolean test) {
        startActivity(new Intent(this, ChargeActivity.class)
                .putExtra("test", test)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }

    @Override public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
          }
