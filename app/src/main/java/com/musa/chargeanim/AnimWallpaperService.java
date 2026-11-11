package com.musa.chargeanim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/** Live wallpaper: the same themes, on the home screen and/or lock screen. */
public class AnimWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new E(); }

    private class E extends Engine {
        private final ThemeRenderer renderer = new ThemeRenderer();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private float target = 0f, shown = 0f;
        private boolean charging = false, visible = false;
        private int theme;
        private long start = System.currentTimeMillis();

        private final BroadcastReceiver battery = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                int l = i.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int s = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                target = l * 100f / s;
                charging = st == BatteryManager.BATTERY_STATUS_CHARGING
                        || st == BatteryManager.BATTERY_STATUS_FULL;
            }
        };

        private final Runnable loop = new Runnable() {
            @Override public void run() {
                drawFrame();
                if (visible) handler.postDelayed(this, 33);
            }
        };

        @Override public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            registerReceiver(battery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        @Override public void onDestroy() {
            handler.removeCallbacksAndMessages(null);
            try { unregisterReceiver(battery); } catch (Exception ignored) { }
            super.onDestroy();
        }

        @Override public void onVisibilityChanged(boolean v) {
            visible = v;
            handler.removeCallbacks(loop);
            if (v) {
                theme = ThemeRenderer.getTheme(AnimWallpaperService.this);
                shown = 0f;
                handler.post(loop);
            }
        }

        @Override public void onSurfaceChanged(SurfaceHolder h, int f, int w, int hh) {
            super.onSurfaceChanged(h, f, w, hh);
            drawFrame();
        }

        private void drawFrame() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c == null) return;
                float phase = ((System.currentTimeMillis() - start) % 2000) / 2000f;
                shown += (target - shown) * 0.06f;
                renderer.draw(c, c.getWidth(), c.getHeight(), theme, shown, phase, charging);
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }
    }
      }
