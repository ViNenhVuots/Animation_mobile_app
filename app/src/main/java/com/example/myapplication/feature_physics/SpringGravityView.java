package com.example.myapplication.feature_physics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

public class SpringGravityView extends View {

    private Paint paintBall;
    private Paint paintSpring;
    private Paint paintAnchor;

    private float ballX = 200f;
    private float ballY = 200f;
    private static final float BASE_RADIUS = 36f;
    private float radius = 45f;

    // Khoi luong qua bong (kg)
    private float mass = 1.0f;

    // FPS & Diagnostics Overlay (Zero GC in loop)
    private final Paint paintFpsBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintFpsText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF fpsBgRect = new RectF();
    private long lastFpsUpdateTime = 0L;
    private int fpsFrameCount = 0;
    private int currentFps = 60;
    private String fpsDisplayString = "FPS: 60 | Mass: 1.0kg | Zero GC";

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
        setHapticFeedbackEnabled(true);

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

        paintFpsBg.setColor(0xB319243C); // Dark blue translucent
        paintFpsBg.setStyle(Paint.Style.FILL);

        paintFpsText.setColor(0xFF4ADE80); // Neon green
        paintFpsText.setTextSize(26f);
        paintFpsText.setTextAlign(Paint.Align.LEFT);
        paintFpsText.setFakeBoldText(true);

        updateBallAppearance();
    }

    private void updateBallAppearance() {
        // Ban kinh ti le theo khoi luong
        radius = BASE_RADIUS * (float) Math.pow(mass, 0.38);

        // Chuyen mau dam dan khi qua bong nang hon
        if (paintBall != null) {
            if (mass <= 1.5f) {
                paintBall.setColor(0xFF3F51B5); // Indigo
            } else if (mass <= 3.0f) {
                paintBall.setColor(0xFF283593); // Deep Indigo
            } else {
                paintBall.setColor(0xFF263238); // Steel Grey / Lead
            }
        }
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

            accelerationX += PhysicsEngine.calculateHookeSpringAcceleration(
                    dx, velocityX, springStrength, springDamping, mass);
            accelerationY += PhysicsEngine.calculateHookeSpringAcceleration(
                    dy, velocityY, springStrength, springDamping, mass);
        }

        velocityX = PhysicsEngine.integrateVelocity(velocityX, accelerationX, dt);
        velocityY = PhysicsEngine.integrateVelocity(velocityY, accelerationY, dt);

        ballX = PhysicsEngine.integratePosition(ballX, velocityX, dt);
        ballY = PhysicsEngine.integratePosition(ballY, velocityY, dt);

        boolean collided = false;
        float impactSpeed = 0f;

        // Va cham canh trai
        if (ballX < radius) {
            ballX = radius;
            impactSpeed = Math.abs(velocityX);
            velocityX = PhysicsEngine.resolvePositiveBoundaryBounce(velocityX, bounce);
            collided = true;
        }

        // Va cham canh phai
        if (ballX > getWidth() - radius) {
            ballX = getWidth() - radius;
            impactSpeed = Math.max(impactSpeed, Math.abs(velocityX));
            velocityX = PhysicsEngine.resolveBoundaryBounce(velocityX, bounce);
            collided = true;
        }

        // Va cham canh tren
        if (ballY < radius) {
            ballY = radius;
            impactSpeed = Math.max(impactSpeed, Math.abs(velocityY));
            velocityY = PhysicsEngine.resolvePositiveBoundaryBounce(velocityY, bounce);
            collided = true;
        }

        // Va cham canh duoi
        if (ballY > getHeight() - radius) {
            ballY = getHeight() - radius;
            impactSpeed = Math.max(impactSpeed, Math.abs(velocityY));
            velocityY = PhysicsEngine.resolveBoundaryBounce(velocityY, bounce);
            collided = true;
        }

        // Haptic feedback khi va cham manh vao 4 bien
        if (collided && impactSpeed > 160f) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = SystemClock.uptimeMillis();

        if (lastFrameTime != 0L) {
            float dt = (currentTime - lastFrameTime) / 1000f;

            // Gioi han dt de tranh buoc nhay qua lon
            dt = PhysicsEngine.clampDeltaTime(dt, PhysicsEngine.MAX_DELTA_TIME);

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

        // Render FPS & Diagnostics Overlay (Zero GC per frame)
        fpsFrameCount++;
        if (currentTime - lastFpsUpdateTime >= 500L) {
            if (lastFpsUpdateTime > 0L) {
                currentFps = (int) (fpsFrameCount * 1000L / (currentTime - lastFpsUpdateTime));
            }
            fpsFrameCount = 0;
            lastFpsUpdateTime = currentTime;
            fpsDisplayString = "FPS: " + currentFps + " | Mass: " + String.format(Locale.US, "%.1f", mass) + "kg | Zero GC";
        }

        float textWidth = paintFpsText.measureText(fpsDisplayString);
        fpsBgRect.set(16f, 16f, 16f + textWidth + 24f, 16f + 36f);
        canvas.drawRoundRect(fpsBgRect, 10f, 10f, paintFpsBg);
        canvas.drawText(fpsDisplayString, 28f, 16f + 25f, paintFpsText);

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
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

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

    // Thay doi do bat nay
    public void setBounce(float value) {
        bounce = value;
        invalidate();
    }

    // Thay doi do nang (khoi luong qua bong)
    public void setMass(float value) {
        mass = Math.max(0.5f, Math.min(5.0f, value));
        updateBallAppearance();
        invalidate();
    }

    public float getMass() {
        return mass;
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
