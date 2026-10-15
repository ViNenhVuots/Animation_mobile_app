package com.example.myapplication.feature_simulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleFireWaterView extends View {
    private static class Particle {
        float x, y;
        float vx, vy;
        int color;
        float alpha;
        float size;
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();

    public ParticleFireWaterView(Context context) {
        super(context);
        init();
    }

    public ParticleFireWaterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initial particles
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() > 0 && particles.size() < 100) {
            Particle p = new Particle();
            p.x = getWidth() / 2f;
            p.y = getHeight() - 50f;
            p.vx = (random.nextFloat() - 0.5f) * 10f;
            p.vy = -random.nextFloat() * 15f - 5f;
            p.color = random.nextBoolean() ? 0xFFFF5722 : 0xFF00BCD4; // Fire orange or water cyan
            p.alpha = 1.0f;
            p.size = random.nextFloat() * 20f + 10f;
            particles.add(p);
        }

        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            p.alpha -= 0.02f;

            if (p.alpha <= 0) {
                particles.remove(i);
            } else {
                paint.setColor(p.color);
                paint.setAlpha((int) (p.alpha * 255));
                canvas.drawCircle(p.x, p.y, p.size, paint);
            }
        }

        postInvalidateOnAnimation();
    }
}
