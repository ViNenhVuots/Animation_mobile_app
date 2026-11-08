package com.example.myapplication.feature_simulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
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
    private static final int MAX_POOL_CAPACITY = 400;
    private static final int SPLASH_PARTICLES_PER_DROP = 5;
    private static final int SPLASH_RESERVED_SLOTS = 32;
    private static final float FLOOR_PADDING_PX = 12f;

    // FPS & Diagnostics Overlay
    private final Paint paintFpsBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFpsText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF fpsBgRect = new RectF();
    private long lastFpsUpdateTime = 0L;
    private int fpsFrameCount = 0;
    private int currentFps = 60;
    private String fpsDisplayString = "FPS: -- | Active: 0 | Pool: 120";

    private static final int[] WATER_COLORS = {
            Color.CYAN,
            Color.rgb(3, 169, 244),
            Color.rgb(33, 150, 243)
    };

    private static class Particle {
        float x, y;
        float vx, vy;
        float alpha;
        float size;
        float life;
        float maxLife;
        int color;
        boolean splash;

        void reset() {
            x = 0f;
            y = 0f;
            vx = 0f;
            vy = 0f;
            alpha = 1f;
            size = 0f;
            life = 0f;
            maxLife = 0f;
            color = 0;
            splash = false;
        }
    }

    private final List<Particle> particles = new ArrayList<>(MAX_PARTICLES);
    private final List<Particle> particlePool = new ArrayList<>(MAX_POOL_CAPACITY);
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
        setHapticFeedbackEnabled(true);

        paintFpsBg.setColor(0xB319243C);
        paintFpsBg.setStyle(Paint.Style.FILL);

        paintFpsText.setColor(0xFF38BDF8); // Sky blue
        paintFpsText.setTextSize(26f);
        paintFpsText.setTextAlign(Paint.Align.LEFT);
        paintFpsText.setFakeBoldText(true);

        // Pre-warm for reuse. A depleted pool may still allocate particles.
        for (int i = 0; i < 120; i++) {
            particlePool.add(new Particle());
        }
    }

    private Particle obtainParticle() {
        if (!particlePool.isEmpty()) {
            return particlePool.remove(particlePool.size() - 1);
        }
        return new Particle();
    }

    private void releaseParticle(Particle p) {
        if (p != null && particlePool.size() < MAX_POOL_CAPACITY) {
            p.reset();
            particlePool.add(p);
        }
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
        recycleAllParticles();
        spawnAccumulator = 0f;
        invalidate();
    }

    public int getMode() {
        return mode;
    }

    public void clearParticles() {
        recycleAllParticles();
        spawnAccumulator = 0f;
        invalidate();
    }

    private void recycleAllParticles() {
        for (int i = 0; i < particles.size(); i++) {
            releaseParticle(particles.get(i));
        }
        particles.clear();
    }

    private void createParticle(float x, float y) {
        int emissionLimit = mode == MODE_WATER
                ? MAX_PARTICLES - SPLASH_RESERVED_SLOTS
                : MAX_PARTICLES;
        if (particles.size() >= emissionLimit) {
            return;
        }

        Particle p = obtainParticle();

        p.x = x;
        p.y = y;
        p.alpha = 1f;
        p.splash = false;

        if (mode == MODE_FIRE) {
            p.vx = (random.nextFloat() - 0.5f) * 100f;
            p.vy = -120f - random.nextFloat() * 180f;

            p.size = 5f + random.nextFloat() * 13f;
            p.maxLife = 0.7f + random.nextFloat() * 0.9f;
            p.color = ParticleMath.fireColor(0f);
        } else {
            p.vx = (random.nextFloat() - 0.5f) * 100f;
            p.vy = 40f + random.nextFloat() * 100f;

            p.size = 4f + random.nextFloat() * 8f;
            p.maxLife = 1.5f + random.nextFloat() * 1.5f;
            p.color = WATER_COLORS[random.nextInt(WATER_COLORS.length)];
        }

        p.life = p.maxLife;
        particles.add(p);
    }

    private float waterFloorY() {
        return Math.max(0f, getHeight() - FLOOR_PADDING_PX);
    }

    private void createSplash(float impactX, float floorY, float dropSize) {
        for (int j = 0; j < SPLASH_PARTICLES_PER_DROP
                && particles.size() < MAX_PARTICLES; j++) {
            Particle child = obtainParticle();
            child.splash = true;
            child.size = Math.max(1.5f, dropSize * (0.25f + random.nextFloat() * 0.2f));
            child.x = Math.max(child.size,
                    Math.min(getWidth() - child.size, impactX));
            child.y = floorY - child.size - 1f;
            // Spread in both horizontal directions, initially moving upward.
            child.vx = ParticleMath.splashVelocityX(j, random.nextFloat());
            child.vy = ParticleMath.splashVelocityY(random.nextFloat());
            child.maxLife = 0.35f + random.nextFloat() * 0.35f;
            child.life = child.maxLife;
            child.alpha = 1f;
            child.color = WATER_COLORS[random.nextInt(WATER_COLORS.length)];
            particles.add(child);
        }
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
            p.alpha = ParticleMath.lifeAlpha(p.life, p.maxLife);

            if (mode == MODE_FIRE) {
                p.size = Math.max(0f, p.size - 5f * dt);
                p.color = ParticleMath.fireColor(1f - p.life / p.maxLife);
            }

            // Consume the falling drop once. Splash children never create more splashes.
            if (mode == MODE_WATER && getHeight() > 0 && p.life > 0f
                    && ParticleMath.hitsFloor(p.y, p.size, p.vy, waterFloorY())) {
                boolean shouldSplash = !p.splash;
                float impactX = p.x;
                float dropSize = p.size;
                float floorY = waterFloorY();
                particles.remove(i);
                releaseParticle(p);
                if (ParticleMath.canCreateSplash(!shouldSplash, impactX, getWidth())) {
                    createSplash(impactX, floorY, dropSize);
                }
                continue;
            }

            if (p.life <= 0f
                    || p.y < -30f
                    || p.y > getHeight() + 30f
                    || p.x < -30f
                    || p.x > getWidth() + 30f) {

                releaseParticle(p);
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

        if (mode == MODE_WATER && getHeight() > 0) {
            paint.setColor(Color.rgb(38, 80, 110));
            paint.setAlpha(160);
            paint.setStrokeWidth(2f);
            canvas.drawLine(0f, waterFloorY(), getWidth(), waterFloorY(), paint);
        }

        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            paint.setColor(p.color);
            paint.setAlpha((int) (p.alpha * 255));

            canvas.drawCircle(
                    p.x,
                    p.y,
                    p.size,
                    paint
            );
        }

        // FPS diagnostics: refreshed strings can allocate memory.
        fpsFrameCount++;
        if (now - lastFpsUpdateTime >= 500L) {
            if (lastFpsUpdateTime > 0L) {
                currentFps = (int) (fpsFrameCount * 1000L / (now - lastFpsUpdateTime));
            }
            fpsFrameCount = 0;
            lastFpsUpdateTime = now;
            fpsDisplayString = "FPS: " + currentFps + " | Active: " + particles.size() + " | Pool: " + particlePool.size();
        }

        float textWidth = paintFpsText.measureText(fpsDisplayString);
        fpsBgRect.set(16f, 16f, 16f + textWidth + 24f, 16f + 36f);
        canvas.drawRoundRect(fpsBgRect, 10f, 10f, paintFpsBg);
        canvas.drawText(fpsDisplayString, 28f, 16f + 25f, paintFpsText);

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
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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
        recycleAllParticles();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(
            View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        lastFrameTime = 0L;
    }
}
