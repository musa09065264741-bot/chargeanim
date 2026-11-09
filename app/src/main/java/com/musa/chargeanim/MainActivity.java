package com.musa.chargeanim;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private Button button(String label, Runnable r) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(v -> r.run());
        return b;
    }

    private boolean needOverlay() {
        if (Settings.canDrawOverlays(this)) return false;
        Toast.makeText(this, "Do step 1 first", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(this);
        title.setText("Charge Anim");
        title.setTextSize(22);
        root.addView(title);

        ThemeView preview = new ThemeView(this);
        preview.setForceCharging(true);
        root.addView(preview, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(320)));

        TextView t1 = new TextView(this);
        t1.setText("Pick a theme:");
        root.addView(t1);
        for (int i = 0; i < ThemeRenderer.NAMES.length; i++) {
            final int id = i;
            root.addView(button(ThemeRenderer.NAMES[i], () -> {
                ThemeRenderer.setTheme(this, id);
                preview.setTheme(id);
            }));
        }

        TextView t2 = new TextView(this);
        t2.setText("\nCharging screen (shows on lock screen when you plug in):");
        root.addView(t2);
        root.addView(button("1. Allow display over other apps", () -> startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())))));
        root.addView(button("2. Start charging animation", () -> {
            if (needOverlay()) return;
            startForegroundService(new Intent(this, ChargingService.class));
            Toast.makeText(this, "Running. Plug in your charger.", Toast.LENGTH_SHORT).show();
        }));
        root.addView(button("Test charging screen now", () -> {
            if (needOverlay()) return;
            startForegroundService(new Intent(this, ChargingService.class)
                    .setAction(ChargingService.ACTION_TEST));
        }));

        TextView t3 = new TextView(this);
        t3.setText("\nHome screen and lock screen wallpaper:");
        root.addView(t3);
        root.addView(button("3. Set as live wallpaper", () -> startActivity(new Intent(
                WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, AnimWallpaperService.class)))));

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }
            }
