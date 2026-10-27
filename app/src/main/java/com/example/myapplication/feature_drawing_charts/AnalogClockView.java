
package com.example.myapplication.feature_drawing_charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AnalogClockView extends View {

    private final Paint facePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint ringPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint tickPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint numberPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint hourPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint minutePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint secondPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint centerPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint titlePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint subtitlePaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint digitalPaint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private float density;

    private final Runnable clockUpdater = new Runnable() {
        @Override
        public void run() {
            invalidate();

            if (isAttachedToWindow() && isShown()) {
                postDelayed(this, 1000);
            }
        }
    };

    public AnalogClockView(Context context) {
        super(context);
        init();
    }

    public AnalogClockView(
            Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private float dp(float value) {
        return value * density;
    }

    private void init() {
        density = getResources()
                .getDisplayMetrics().density;

        setBackgroundColor(Color.WHITE);

        facePaint.setColor(Color.rgb(248, 250, 255));
        facePaint.setStyle(Paint.Style.FILL);

        ringPaint.setColor(Color.rgb(211, 222, 245));
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(dp(4));

        tickPaint.setColor(Color.rgb(67, 85, 199));
        tickPaint.setStrokeCap(Paint.Cap.ROUND);

        numberPaint.setColor(Color.rgb(25, 36, 60));
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setFakeBoldText(true);

        hourPaint.setColor(Color.rgb(25, 36, 60));
        hourPaint.setStrokeCap(Paint.Cap.ROUND);
        hourPaint.setStrokeWidth(dp(6));

        minutePaint.setColor(Color.rgb(67, 85, 199));
        minutePaint.setStrokeCap(Paint.Cap.ROUND);
        minutePaint.setStrokeWidth(dp(4));

        secondPaint.setColor(Color.rgb(228, 107, 99));
        secondPaint.setStrokeCap(Paint.Cap.ROUND);
        secondPaint.setStrokeWidth(dp(2));

        centerPaint.setColor(Color.rgb(228, 107, 99));

        titlePaint.setColor(Color.rgb(25, 36, 60));
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextSize(dp(18));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        subtitlePaint.setColor(Color.rgb(100, 116, 139));
        subtitlePaint.setTextSize(dp(12));
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        digitalPaint.setColor(Color.rgb(67, 85, 199));
        digitalPaint.setTextSize(dp(24));
        digitalPaint.setFakeBoldText(true);
        digitalPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        float centerX = width / 2f;

        // HEADER
        canvas.drawText(
                "Analog Clock",
                centerX,
                dp(32),
                titlePaint
        );

        canvas.drawText(
                "Local Device Time",
                centerX,
                dp(53),
                subtitlePaint
        );

        // KHU VUC DONG HO
        float availableTop = dp(72);
        float availableBottom = height - dp(100);

        float availableHeight =
                availableBottom - availableTop;

        if (availableHeight <= dp(50)) {
            return;
        }

        float centerY =
                (availableTop + availableBottom) / 2f;

        float radius = Math.min(
                (width - dp(36)) / 2f,
                availableHeight / 2f
        );

        if (radius <= dp(20)) {
            return;
        }

        // MAT DONG HO
        canvas.drawCircle(
                centerX, centerY,
                radius, facePaint
        );

        canvas.drawCircle(
                centerX, centerY,
                radius, ringPaint
        );

        // 60 VACH CHIA
        for (int i = 0; i < 60; i++) {
            double angle =
                    Math.toRadians(i * 6 - 90);

            boolean major = i % 5 == 0;

            float outerRadius = radius - dp(5);
            float innerRadius = radius -
                    (major ? dp(16) : dp(10));

            float x1 = centerX
                    + (float) Math.cos(angle)
                    * innerRadius;

            float y1 = centerY
                    + (float) Math.sin(angle)
                    * innerRadius;

            float x2 = centerX
                    + (float) Math.cos(angle)
                    * outerRadius;

            float y2 = centerY
                    + (float) Math.sin(angle)
                    * outerRadius;

            tickPaint.setStrokeWidth(
                    major ? dp(3) : dp(1)
            );

            canvas.drawLine(
                    x1, y1, x2, y2, tickPaint
            );
        }

        // 12 SO GIO
        numberPaint.setTextSize(
                Math.min(dp(17), radius * 0.14f)
        );

        for (int i = 1; i <= 12; i++) {
            double angle =
                    Math.toRadians(i * 30 - 90);

            float textRadius = radius * 0.72f;

            float x = centerX
                    + (float) Math.cos(angle)
                    * textRadius;

            float y = centerY
                    + (float) Math.sin(angle)
                    * textRadius;

            float baseline = y
                    - (numberPaint.ascent()
                    + numberPaint.descent()) / 2f;

            canvas.drawText(
                    String.valueOf(i),
                    x,
                    baseline,
                    numberPaint
            );
        }

        // THOI GIAN THIET BI
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        float hourAngle =
                (hour + minute / 60f
                        + second / 3600f) * 30f;

        float minuteAngle =
                (minute + second / 60f) * 6f;

        float secondAngle = second * 6f;

        // KIM GIO
        drawHand(
                canvas, centerX, centerY,
                radius * 0.48f,
                hourAngle, hourPaint
        );

        // KIM PHUT
        drawHand(
                canvas, centerX, centerY,
                radius * 0.68f,
                minuteAngle, minutePaint
        );

        // KIM GIAY
        drawHand(
                canvas, centerX, centerY,
                radius * 0.81f,
                secondAngle, secondPaint
        );

        canvas.drawCircle(
                centerX, centerY,
                dp(6), centerPaint
        );

        // GIO KY THUAT SO
        SimpleDateFormat timeFormat =
                new SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                );

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        canvas.drawText(
                timeFormat.format(calendar.getTime()),
                centerX,
                height - dp(57),
                digitalPaint
        );

        canvas.drawText(
                dateFormat.format(calendar.getTime()),
                centerX,
                height - dp(32),
                subtitlePaint
        );
    }

    private void drawHand(
            Canvas canvas,
            float cx,
            float cy,
            float length,
            float degrees,
            Paint paint) {

        double angle =
                Math.toRadians(degrees - 90);

        float endX = cx
                + (float) Math.cos(angle) * length;

        float endY = cy
                + (float) Math.sin(angle) * length;

        canvas.drawLine(
                cx, cy, endX, endY, paint
        );
    }

    private void startClock() {
        removeCallbacks(clockUpdater);

        if (isAttachedToWindow() && isShown()) {
            post(clockUpdater);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startClock();
    }

    @Override
    protected void onVisibilityChanged(
            View changedView,
            int visibility) {

        super.onVisibilityChanged(
                changedView, visibility
        );

        startClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(clockUpdater);
        super.onDetachedFromWindow();
    }
}
