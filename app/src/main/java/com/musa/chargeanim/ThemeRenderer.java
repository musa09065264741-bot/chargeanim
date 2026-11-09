package com.musa.chargeanim;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.SparseArray;

/** Draws every theme. Used by the charging screen, the preview and the live wallpaper. */
public class ThemeRenderer {
    public static final String[] NAMES = {"Neon Ring", "Solar Reactor", "Liquid Wave", "Pulse"};
    private static final int[] COLORS = {0xFF00E5FF, 0xFFFFB300, 0xFF00E676, 0xFFFF2D95};

    public static int getTheme(Context c) {
        return c.getSharedPreferences("cfg", 0).getInt("theme", 0);
    }

    public static void setTheme(Context c, int t) {
        c.getSharedPreferences("cfg", 0).edit().putInt("theme", t).apply();
    }

    private final Paint track = paint(), glow = paint(), arc = paint(), fill = paint(), text = paint();
    private final RectF box = new RectF();
    private final Path path = new Path();
    private final SparseArray<BlurMaskFilter> blurs = new SparseArray<>();

    private static Paint paint() { return new Paint(Paint.ANTI_ALIAS_FLAG); }

    public ThemeRenderer() {
        for (Paint p : new Paint[]{track, glow, arc}) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
        }
        fill.setStyle(Paint.Style.FILL);
        text.setColor(0xFFFFFFFF);
        text.setTextAlign(Paint.Align.CENTER);
    }

    private BlurMaskFilter blur(float radius) {
        int k = Math.max(1, Math.round(radius));
        BlurMaskFilter f = blurs.get(k);
        if (f == null) { f = new BlurMaskFilter(k, BlurMaskFilter.Blur.NORMAL); blurs.put(k, f); }
        return f;
    }

    /** shown = 0..100 battery percent, phase = 0..1 repeating animation clock. */
    public void draw(Canvas c, float w, float h, int theme, float shown, float phase, boolean charging) {
        c.drawColor(0xFF000000);
        float pulse = 0.5f + 0.5f * (float) Math.sin(phase * 2 * Math.PI);
        int color = COLORS[Math.abs(theme) % COLORS.length];
        String label = charging ? "CHARGING" : "BATTERY";
        switch (theme) {
            case 1: reactor(c, w, h, shown, phase, pulse, color, label); break;
            case 2: wave(c, w, h, shown, phase, color, label); break;
            case 3: pulseTheme(c, w, h, shown, phase, pulse, color, label); break;
            default: neon(c, w, h, shown, pulse, color, label);
        }
    }

    private void drawFillArc(Canvas c, float cx, float cy, float r, float stroke,
                             float shown, float pulse, int color) {
        box.set(cx - r, cy - r, cx + r, cy + r);
        track.setColor(0x33FFFFFF);
        track.setStrokeWidth(stroke);
        c.drawArc(box, 0, 360, false, track);
        float sweep = 360f * shown / 100f;
        glow.setColor(color);
        glow.setStrokeWidth(stroke * 1.6f);
        glow.setMaskFilter(blur(stroke * (1.2f + pulse)));
        glow.setAlpha((int) (120 + 135 * pulse));
        arc.setColor(color);
        arc.setStrokeWidth(stroke);
        c.drawArc(box, -90, sweep, false, glow);
        c.drawArc(box, -90, sweep, false, arc);
        double ang = Math.toRadians(-90 + sweep);
        fill.setColor(0xFFFFFFFF);
        c.drawCircle(cx + (float) Math.cos(ang) * r, cy + (float) Math.sin(ang) * r,
                stroke * (0.6f + 0.3f * pulse), fill);
    }

    private void write(Canvas c, String s, float x, float y, float size) {
        text.setTextSize(size);
        c.drawText(s, x, y, text);
    }

    private void neon(Canvas c, float w, float h, float shown, float pulse, int color, String label) {
        float cx = w / 2, cy = h / 2, r = Math.min(w, h) * 0.30f;
        drawFillArc(c, cx, cy, r, r * 0.12f, shown, pulse, color);
        text.setShadowLayer(0, 0, 0, 0);
        write(c, Math.round(shown) + "%", cx, cy + r * 0.18f, r * 0.5f);
        write(c, label, cx, cy + r * 0.55f, r * 0.14f);
    }

    private void reactor(Canvas c, float w, float h, float shown, float phase, float pulse,
                         int color, String label) {
        float cx = w / 2, cy = h / 2, r = Math.min(w, h) * 0.32f;
        // spinning dashed outer ring
        track.setColor(color);
        track.setAlpha(150);
        track.setStrokeWidth(r * 0.04f);
        track.setStrokeCap(Paint.Cap.BUTT);
        track.setPathEffect(new DashPathEffect(new float[]{r * 0.12f, r * 0.08f}, -phase * r * 0.6f));
        c.drawCircle(cx, cy, r, track);
        track.setPathEffect(null);
        track.setStrokeCap(Paint.Cap.ROUND);
        // battery ring
        drawFillArc(c, cx, cy, r * 0.78f, r * 0.14f, shown, pulse, color);
        // thin inner ring
        track.setColor(0x80FFFFFF);
        track.setStrokeWidth(r * 0.02f);
        c.drawCircle(cx, cy, r * 0.52f, track);
        // glowing core
        float cr = r * 0.4f * (0.9f + 0.1f * pulse);
        fill.setShader(new RadialGradient(cx, cy, cr,
                new int[]{0xFFFFFFFF, color, color & 0x00FFFFFF},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, cr, fill);
        fill.setShader(null);
        write(c, Math.round(shown) + "%", cx, cy + r * 1.5f, r * 0.45f);
        write(c, label, cx, cy + r * 1.75f, r * 0.14f);
    }

    private void wave(Canvas c, float w, float h, float shown, float phase, int color, String label) {
        float cx = w / 2, cy = h / 2, r = Math.min(w, h) * 0.30f;
        track.setColor(0x66FFFFFF);
        track.setStrokeWidth(r * 0.05f);
        c.drawCircle(cx, cy, r, track);
        path.reset();
        path.addCircle(cx, cy, r * 0.94f, Path.Direction.CW);
        c.save();
        c.clipPath(path);
        float level = cy + r - 2 * r * shown / 100f;
        for (int k = 0; k < 2; k++) {
            path.reset();
            path.moveTo(cx - r, cy + r);
            float amp = r * (0.05f + 0.02f * k);
            float dir = k == 0 ? 1 : -1;
            for (float x = cx - r; x <= cx + r; x += 8) {
                float y = level + amp * (float) Math.sin((x - cx) / r * 5 + phase * 2 * Math.PI * dir + k * 1.7f);
                path.lineTo(x, y);
            }
            path.lineTo(cx + r, cy + r);
            path.close();
            fill.setColor(color);
            fill.setAlpha(k == 0 ? 220 : 110);
            c.drawPath(path, fill);
        }
        c.restore();
        text.setShadowLayer(10, 0, 0, 0xFF000000);
        write(c, Math.round(shown) + "%", cx, cy + r * 0.2f, r * 0.6f);
        text.setShadowLayer(0, 0, 0, 0);
        write(c, label, cx, cy + r * 1.35f, r * 0.14f);
    }

    private void pulseTheme(Canvas c, float w, float h, float shown, float phase, float pulse,
                            int color, String label) {
        float cx = w / 2, cy = h / 2, r = Math.min(w, h) * 0.25f;
        for (int i = 0; i < 3; i++) {
            float t = (phase + i / 3f) % 1f;
            track.setColor(color);
            track.setAlpha((int) (200 * (1 - t)));
            track.setStrokeWidth(r * 0.06f);
            c.drawCircle(cx, cy, r * (0.6f + 1.4f * t), track);
        }
        fill.setColor(color);
        fill.setAlpha(200);
        fill.setMaskFilter(blur(r * (0.2f + 0.2f * pulse)));
        c.drawCircle(cx, cy, r * 0.55f, fill);
        fill.setMaskFilter(null);
        track.setColor(color);
        track.setStrokeWidth(r * 0.08f);
        c.drawCircle(cx, cy, r * 0.6f, track);
        write(c, Math.round(shown) + "%", cx, cy + r * 0.18f, r * 0.5f);
        // battery bar
        float bw = w * 0.6f, bx = (w - bw) / 2, by = cy + r * 2.4f, bh = r * 0.12f;
        box.set(bx, by, bx + bw, by + bh);
        fill.setColor(0x44FFFFFF);
        c.drawRoundRect(box, bh, bh, fill);
        box.set(bx, by, bx + bw * shown / 100f, by + bh);
        fill.setColor(color);
        c.drawRoundRect(box, bh, bh, fill);
        write(c, label, cx, by + r * 0.55f, r * 0.14f);
    }
  }
