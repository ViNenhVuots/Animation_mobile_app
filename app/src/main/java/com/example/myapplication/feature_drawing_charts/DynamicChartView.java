
package com.example.myapplication.feature_drawing_charts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.Random;

public class DynamicChartView extends View {

    private static final int PRIMARY =
            Color.rgb(67, 85, 199);

    private static final long DURATION = 850L;

    private final float[] initialValues = {
            20f, 50f, 30f, 80f,
            60f, 90f, 40f, 75f
    };

    private final float[] values = initialValues.clone();
    private final float[] startValues = new float[8];
    private final float[] targetValues = new float[8];

    private final Random random = new Random();

    private final Paint titlePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint smallPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint gridPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint axisPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint linePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint fillPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint pointPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint pointCenterPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint statusPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private ValueAnimator animator;
    private boolean isRunning = false;

    private float density;

    public DynamicChartView(Context context) {
        super(context);
        init();
    }

    public DynamicChartView(
            Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        density = getResources()
                .getDisplayMetrics().density;

        setBackgroundColor(Color.WHITE);

        titlePaint.setColor(Color.rgb(25, 36, 60));
        titlePaint.setTextSize(dp(18));
        titlePaint.setFakeBoldText(true);

        smallPaint.setColor(Color.rgb(100, 116, 139));
        smallPaint.setTextSize(dp(11));

        gridPaint.setColor(Color.rgb(226, 232, 240));
        gridPaint.setStrokeWidth(dp(1));

        axisPaint.setColor(Color.rgb(100, 116, 139));
        axisPaint.setTextSize(dp(11));
        axisPaint.setTextAlign(Paint.Align.RIGHT);

        linePaint.setColor(PRIMARY);
        linePaint.setStrokeWidth(dp(3));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint.setColor(Color.rgb(225, 232, 255));
        fillPaint.setStyle(Paint.Style.FILL);

        pointPaint.setColor(PRIMARY);
        pointPaint.setStyle(Paint.Style.FILL);

        pointCenterPaint.setColor(Color.WHITE);
        pointCenterPaint.setStyle(Paint.Style.FILL);

        statusPaint.setColor(Color.rgb(15, 158, 145));
        statusPaint.setStyle(Paint.Style.FILL);
    }

    private float dp(float value) {
        return value * density;
    }

    private void generateTargets() {
        for (int i = 0; i < values.length; i++) {
            startValues[i] = values[i];

            float change =
                    (random.nextFloat() - 0.5f) * 30f;

            targetValues[i] = Math.max(
                    0f,
                    Math.min(100f, values[i] + change)
            );
        }
    }

    private void startNextCycle() {
        if (!isRunning) {
            return;
        }

        generateTargets();

        ValueAnimator next =
                ValueAnimator.ofFloat(0f, 1f);

        animator = next;

        next.setDuration(DURATION);
        next.setInterpolator(
                new AccelerateDecelerateInterpolator()
        );

        next.addUpdateListener(animation -> {
            float progress =
                    (float) animation.getAnimatedValue();

            for (int i = 0; i < values.length; i++) {
                values[i] = startValues[i]
                        + (targetValues[i] - startValues[i])
                        * progress;
            }

            invalidate();
        });

        next.addListener(new AnimatorListenerAdapter() {
            private boolean cancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelled && isRunning
                        && animator == next) {
                    startNextCycle();
                }
            }
        });

        next.start();
    }

    public void startAnimation() {
        if (isRunning) {
            return;
        }

        isRunning = true;

        if (animator != null && animator.isPaused()) {
            animator.resume();
        } else {
            startNextCycle();
        }

        invalidate();
    }

    public void pauseAnimation() {
        isRunning = false;

        if (animator != null && animator.isRunning()) {
            animator.pause();
        }

        invalidate();
    }

    public void resetChart() {
        isRunning = false;

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        System.arraycopy(
                initialValues, 0,
                values, 0,
                values.length
        );

        invalidate();
    }

    public boolean isAnimationRunning() {
        return isRunning;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // HEADER
        canvas.drawText(
                "Live Data Chart",
                dp(16),
                dp(31),
                titlePaint
        );

        canvas.drawCircle(
                dp(21),
                dp(52),
                dp(4),
                statusPaint
        );

        canvas.drawText(
                isRunning ? "LIVE - Updating" : "PAUSED",
                dp(32),
                dp(56),
                smallPaint
        );

        // GRAPH AREA
        float left = dp(48);
        float right = width - dp(18);
        float top = dp(90);
        float bottom = height - dp(42);

        if (right <= left || bottom <= top) {
            return;
        }

        float chartHeight = bottom - top;
        float stepX = (right - left)
                / (values.length - 1);

        // GRID + Y AXIS LABELS
        for (int i = 0; i <= 4; i++) {
            int label = i * 25;

            float y = bottom -
                    (label / 100f) * chartHeight;

            canvas.drawLine(
                    left, y,
                    right, y,
                    gridPaint
            );

            canvas.drawText(
                    String.valueOf(label),
                    left - dp(9),
                    y + dp(4),
                    axisPaint
            );
        }

        // AREA FILL
        Path areaPath = new Path();
        areaPath.moveTo(left, bottom);

        // LINE PATH
        Path linePath = new Path();

        for (int i = 0; i < values.length; i++) {
            float x = left + i * stepX;
            float y = bottom -
                    (values[i] / 100f) * chartHeight;

            if (i == 0) {
                areaPath.lineTo(x, y);
                linePath.moveTo(x, y);
            } else {
                areaPath.lineTo(x, y);
                linePath.lineTo(x, y);
            }
        }

        areaPath.lineTo(right, bottom);
        areaPath.close();

        canvas.drawPath(areaPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // DATA POINTS + X LABELS
        Paint xLabelPaint = smallPaint;
        xLabelPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < values.length; i++) {
            float x = left + i * stepX;
            float y = bottom -
                    (values[i] / 100f) * chartHeight;

            canvas.drawCircle(
                    x, y, dp(5), pointPaint
            );

            canvas.drawCircle(
                    x, y, dp(2), pointCenterPaint
            );

            canvas.drawText(
                    String.valueOf(i + 1),
                    x,
                    bottom + dp(20),
                    xLabelPaint
            );
        }

        xLabelPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onDetachedFromWindow() {
        isRunning = false;

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        super.onDetachedFromWindow();
    }
}
