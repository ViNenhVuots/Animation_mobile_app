
package com.example.myapplication.feature_physics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SpringGravityView extends View {

    private Paint paintBall;
    private Paint paintSpring;
    private Paint paintAnchor;

    private float ballX = 200f;
    private float ballY = 200f;
    private float radius = 45f;

    // Van toc tinh theo pixel/giay
    private float velocityX = 0f;
    private float velocityY = 0f;

    // Cac thong so vat ly
    private float gravity = 1200f;
    private float springStrength = 15f;
    private float springDamping = 4f;
    private float bounce = 0.7f;

    // Diem neo cua lo xo
    private float anchorX;
    private float anchorY;

    private boolean isDragging = false;
    private boolean springEnabled = false;

    private long lastFrameTime = 0L;

    public SpringGravityView(Context context) {
        super(context);
        init();
    }

    public SpringGravityView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintBall = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBall.setColor(0xFF3F51B5);
        paintBall.setStyle(Paint.Style.FILL);

        paintSpring = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSpring.setColor(0xFFFF9800);
        paintSpring.setStrokeWidth(6f);
        paintSpring.setStyle(Paint.Style.STROKE);

        paintAnchor = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAnchor.setColor(0xFFE53935);
        paintAnchor.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h,
                                 int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        anchorX = w / 2f;
        anchorY = h / 3f;

        if (oldw == 0 && oldh == 0) {
            ballX = anchorX;
            ballY = anchorY;
        }
    }

    private void updatePhysics(float dt) {
        if (isDragging) {
            return;
        }

        float accelerationX = 0f;
        float accelerationY = gravity;

        if (springEnabled) {
            float dx = ballX - anchorX;
            float dy = ballY - anchorY;

            accelerationX -= springStrength * dx;
            accelerationY -= springStrength * dy;

            accelerationX -= springDamping * velocityX;
            accelerationY -= springDamping * velocityY;
        }

        velocityX += accelerationX * dt;
        velocityY += accelerationY * dt;

        ballX += velocityX * dt;
        ballY += velocityY * dt;

        // Va cham canh trai
        if (ballX < radius) {
            ballX = radius;
            velocityX = Math.abs(velocityX) * bounce;
        }

        // Va cham canh phai
        if (ballX > getWidth() - radius) {
            ballX = getWidth() - radius;
            velocityX = -Math.abs(velocityX) * bounce;
        }

        // Va cham canh tren
        if (ballY < radius) {
            ballY = radius;
            velocityY = Math.abs(velocityY) * bounce;
        }

        // Va cham canh duoi
        if (ballY > getHeight() - radius) {
            ballY = getHeight() - radius;
            velocityY = -Math.abs(velocityY) * bounce;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = SystemClock.uptimeMillis();

        if (lastFrameTime != 0L) {
            float dt = (currentTime - lastFrameTime) / 1000f;

            // Gioi han dt de tranh buoc nhay qua lon
            dt = Math.min(dt, 0.032f);

            updatePhysics(dt);
        }

        lastFrameTime = currentTime;

        if (springEnabled) {
            canvas.drawLine(
                    anchorX, anchorY,
                    ballX, ballY,
                    paintSpring
            );
        }

        canvas.drawCircle(
                anchorX, anchorY, 12f, paintAnchor
        );

        canvas.drawCircle(
                ballX, ballY, radius, paintBall
        );

        if (getVisibility() == VISIBLE &&
                isShown() &&
                getWindowVisibility() == VISIBLE) {
            postInvalidateOnAnimation();
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void allowParentScroll(boolean allow) {
        android.view.ViewParent parent = getParent();

        while (parent != null) {
            parent.requestDisallowInterceptTouchEvent(!allow);
            parent = parent.getParent();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                isDragging = true;
                springEnabled = true;
                allowParentScroll(false);

                ballX = clamp(
                        event.getX(), radius,
                        Math.max(radius, getWidth() - radius)
                );

                ballY = clamp(
                        event.getY(), radius,
                        Math.max(radius, getHeight() - radius)
                );

                velocityX = 0f;
                velocityY = 0f;

                lastFrameTime = SystemClock.uptimeMillis();
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    ballX = clamp(
                            event.getX(), radius,
                            Math.max(radius, getWidth() - radius)
                    );

                    ballY = clamp(
                            event.getY(), radius,
                            Math.max(radius, getHeight() - radius)
                    );

                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                allowParentScroll(true);

                velocityX = 0f;
                velocityY = 0f;

                lastFrameTime = SystemClock.uptimeMillis();
                postInvalidateOnAnimation();
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onVisibilityChanged(
            View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        lastFrameTime = 0L;

        if (visibility == VISIBLE) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastFrameTime = 0L;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        lastFrameTime = 0L;
        super.onDetachedFromWindow();
    }

    // Thay doi trong luc
    public void setGravity(float value) {
        gravity = value;
        invalidate();
    }

    // Thay doi do cung lo xo
    public void setSpringStrength(float value) {
        springStrength = value;
        invalidate();
    }

    // Thay doi he so bat nay
    public void setBounce(float value) {
        bounce = value;
        invalidate();
    }

    // Reset vi tri va van toc cua bong
    public void resetBall() {
        ballX = anchorX;
        ballY = anchorY;

        velocityX = 0f;
        velocityY = 0f;

        isDragging = false;
        springEnabled = false;
        lastFrameTime = 0L;

        invalidate();
    }

}
