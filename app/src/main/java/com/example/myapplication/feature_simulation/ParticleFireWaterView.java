
package com.example.myapplication.feature_simulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleFireWaterView extends View {

    public static final int MODE_FIRE = 0;
    public static final int MODE_WATER = 1;

    private static final int MAX_PARTICLES = 180;

    private static class Particle {
        float x, y;
        float vx, vy;
        float alpha;
        float size;
        float life;
        float maxLife;
        int color;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    private int mode = MODE_FIRE;
    private float emitterX;
    private float emitterY;
    private boolean touching = false;

    private long lastFrameTime = 0L;
    private float spawnAccumulator = 0f;

    public ParticleFireWaterView(Context context) {
        super(context);
        init();
    }

    public ParticleFireWaterView(
            Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Enable hardware acceleration for smooth particle animation
    }

    @Override
    protected void onSizeChanged(
            int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        emitterX = w / 2f;
        emitterY = h * 0.8f;
    }

    public void setMode(int newMode) {
        if (newMode != MODE_FIRE && newMode != MODE_WATER) {
            return;
        }

        mode = newMode;
        particles.clear();
        spawnAccumulator = 0f;
        invalidate();
    }

    public int getMode() {
        return mode;
    }

    public void clearParticles() {
        particles.clear();
        spawnAccumulator = 0f;
        invalidate();
    }

    private void createParticle(float x, float y) {
        if (particles.size() >= MAX_PARTICLES) {
            return;
        }

        Particle p = new Particle();

        p.x = x;
        p.y = y;
        p.alpha = 1f;

        if (mode == MODE_FIRE) {
            p.vx = (random.nextFloat() - 0.5f) * 100f;
            p.vy = -120f - random.nextFloat() * 180f;

            p.size = 5f + random.nextFloat() * 13f;
            p.maxLife = 0.7f + random.nextFloat() * 0.9f;

            int[] fireColors = {
                    Color.RED,
                    Color.rgb(255, 87, 34),
                    Color.rgb(255, 152, 0),
                    Color.YELLOW
            };

            p.color = fireColors[
                    random.nextInt(fireColors.length)
                    ];

        } else {
            p.vx = (random.nextFloat() - 0.5f) * 100f;
            p.vy = 40f + random.nextFloat() * 100f;

            p.size = 4f + random.nextFloat() * 8f;
            p.maxLife = 1.5f + random.nextFloat() * 1.5f;

            int[] waterColors = {
                    Color.CYAN,
                    Color.rgb(3, 169, 244),
                    Color.rgb(33, 150, 243)
            };

            p.color = waterColors[
                    random.nextInt(waterColors.length)
                    ];
        }

        p.life = p.maxLife;
        particles.add(p);
    }

    private void updateParticles(float dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);

            if (mode == MODE_FIRE) {
                p.vx += (random.nextFloat() - 0.5f)
                        * 90f * dt;
                p.vy -= 25f * dt;
            } else {
                // Gia toc trong truong cho giot nuoc
                p.vy += 400f * dt;
            }

            p.x += p.vx * dt;
            p.y += p.vy * dt;

            p.life -= dt;
            p.alpha = Math.max(0f, p.life / p.maxLife);

            if (mode == MODE_FIRE) {
                p.size = Math.max(0f, p.size - 5f * dt);
            }

            if (p.life <= 0f
                    || p.y < -30f
                    || p.y > getHeight() + 30f
                    || p.x < -30f
                    || p.x > getWidth() + 30f) {

                particles.remove(i);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.rgb(12, 18, 32));

        long now = SystemClock.uptimeMillis();

        float dt = 0f;

        if (lastFrameTime != 0L) {
            dt = Math.min(
                    (now - lastFrameTime) / 1000f,
                    0.033f
            );
        }

        lastFrameTime = now;

        if (getWidth() > 0 && getHeight() > 0) {
            float x = touching
                    ? emitterX
                    : getWidth() / 2f;

            float y = touching
                    ? emitterY
                    : (mode == MODE_FIRE
                       ? getHeight() * 0.8f
                       : getHeight() * 0.15f);

            // Tao khoang 60 hat moi moi giay
            spawnAccumulator += dt * 60f;

            int count = (int) spawnAccumulator;
            spawnAccumulator -= count;

            for (int i = 0; i < count; i++) {
                createParticle(x, y);
            }
        }

        updateParticles(dt);

        for (Particle p : particles) {
            paint.setColor(p.color);
            paint.setAlpha((int) (p.alpha * 255));

            canvas.drawCircle(
                    p.x,
                    p.y,
                    p.size,
                    paint
            );
        }

        if (getVisibility() == VISIBLE
                && isAttachedToWindow()) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                touching = true;
                emitterX = event.getX();
                emitterY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                emitterX = event.getX();
                emitterY = event.getY();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                performClick();
                return true;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrameTime = 0L;
    }

    @Override
    protected void onDetachedFromWindow() {
        lastFrameTime = 0L;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(
            View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        lastFrameTime = 0L;
    }
}
