package com.example.myapplication.feature_physics;

/**
 * Pure Java mathematical engine implementing Euler integration,
 * Hooke's Law with damping, restitution decay, and delta time clamping.
 * Decoupled from the Android UI framework for rigorous unit testing.
 */
public final class PhysicsEngine {

    public static final float MAX_DELTA_TIME = 0.032f;

    private PhysicsEngine() {
        // Utility class
    }

    /**
     * Clamps delta time to prevent physics tunneling or explosion
     * when frame drops occur.
     */
    public static float clampDeltaTime(float rawDt, float maxDt) {
        return Math.min(rawDt, maxDt);
    }

    /**
     * Real-time Euler numerical integration for velocity:
     * v = v0 + a * dt
     */
    public static float integrateVelocity(float v0, float acceleration, float dt) {
        return v0 + acceleration * dt;
    }

    /**
     * Real-time Euler numerical integration for position:
     * p = p0 + v * dt
     */
    public static float integratePosition(float p0, float velocity, float dt) {
        return p0 + velocity * dt;
    }

    /**
     * Computes Hooke's restoration acceleration with viscous damping:
     * a = -k * dx - c * v
     */
    public static float calculateHookeSpringAcceleration(
            float displacement,
            float velocity,
            float springStrength,
            float springDamping) {
        return (-springStrength * displacement) - (springDamping * velocity);
    }

    /**
     * Calculates inverted bounce velocity with momentum decay (restitution coefficient)
     * when hitting right or bottom boundaries:
     * v_after = -|v| * bounce
     */
    public static float resolveBoundaryBounce(float velocity, float bounce) {
        return -Math.abs(velocity) * bounce;
    }

    /**
     * Calculates positive bounce velocity with momentum decay (restitution coefficient)
     * when hitting left or top boundaries:
     * v_after = |v| * bounce
     */
    public static float resolvePositiveBoundaryBounce(float velocity, float bounce) {
        return Math.abs(velocity) * bounce;
    }
}

