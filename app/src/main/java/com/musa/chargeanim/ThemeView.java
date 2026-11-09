package com.musa.chargeanim;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.os.BatteryManager;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** A view that plays the selected theme using the real battery level. */
public class ThemeView extends View {
    private final ThemeRenderer renderer = new ThemeRenderer();
    private final ValueAnimator loop = ValueAnimator.ofFloat(0f, 1f);
    private float target = 0f, shown = 0f, phase = 0f;
    private boolean charging = true;
    private boolean forceCharging = false;
    private int theme;

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

    public ThemeView(Context c) {
        super(c);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // needed for the blur glow
        theme = ThemeRenderer.getTheme(c);
        loop.setDuration(2000);
        loop.setRepeatCount(ValueAnimator.INFINITE);
        loop.setInterpolator(new LinearInterpolator());
        loop.addUpdateListener(a -> {
            phase = (float) a.getAnimatedValue();
            shown += (target - shown) * 0.06f;
            invalidate();
        });
    }

    public void setTheme(int t) { theme = t; invalidate(); }
    public void setForceCharging(boolean f) { forceCharging = f; }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().registerReceiver(battery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        loop.start();
    }

    @Override protected void onDetachedFromWindow() {
        loop.cancel();
        try { getContext().unregisterReceiver(battery); } catch (Exception ignored) { }
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        renderer.draw(canvas, getWidth(), getHeight(), theme, shown, phase, charging || forceCharging);
    }
  }
