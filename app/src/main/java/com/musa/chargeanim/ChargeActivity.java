package com.musa.chargeanim;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

/** Full-screen charging animation. Shows over the lock screen and wakes the screen. */
public class ChargeActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean test;
    private final BroadcastReceiver unplug = new BroadcastReceiver() {
        @Override public void onReceive(Context c, Intent i) { if (!test) finish(); }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        test = getIntent().getBooleanExtra("test", false);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        ThemeView v = new ThemeView(this);
        v.setForceCharging(true);
        v.setOnClickListener(x -> finish());
        setContentView(v);
        registerReceiver(unplug, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        handler.postDelayed(this::finish, 10000);
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        try { unregisterReceiver(unplug); } catch (Exception ignored) { }
        super.onDestroy();
    }
    }
