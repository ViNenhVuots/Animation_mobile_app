package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.myapplication.feature_physics.PhysicsEngine;

import org.junit.Test;

/**
 * Unit Test suite verifying the mathematical accuracy and stability
 * of physics algorithms implemented in the Science Lab application.
 */
public class PhysicsMathUnitTest {

    private static final float DELTA = 1e-4f;

    @Test
    public void testEulerVelocityIntegration() {
        // v = v0 + a * dt
        float v0 = 50.0f; // px/s
        float acceleration = 980.0f; // px/s^2 (gravity)
        float dt = 0.016f; // ~60 FPS frame time (s)

        float expectedV = v0 + acceleration * dt; // 50.0 + 15.68 = 65.68
        float actualV = PhysicsEngine.integrateVelocity(v0, acceleration, dt);

        assertEquals(expectedV, actualV, DELTA);

        // Position integration: p = p0 + v * dt
        float p0 = 100.0f;
        float expectedP = p0 + actualV * dt;
        float actualP = PhysicsEngine.integratePosition(p0, actualV, dt);
        assertEquals(expectedP, actualP, DELTA);
    }

    @Test
    public void testHookeSpringRestorationForce() {
        // Hooke's Law with viscous damping: a = -k * dx - c * v
        float displacement = 120.0f; // px pulled away from anchor
        float velocity = 30.0f;      // px/s moving further
        float springStrength = 15.0f; // k
        float springDamping = 4.0f;   // c

        float expectedAcc = (-springStrength * displacement) - (springDamping * velocity);
        // expected: -15 * 120 - 4 * 30 = -1800 - 120 = -1920.0
        float actualAcc = PhysicsEngine.calculateHookeSpringAcceleration(
                displacement, velocity, springStrength, springDamping);

        assertEquals(-1920.0f, actualAcc, DELTA);
        assertEquals(expectedAcc, actualAcc, DELTA);

        // At equilibrium (dx = 0, v = 0), acceleration must be 0
        float restAcc = PhysicsEngine.calculateHookeSpringAcceleration(0f, 0f, springStrength, springDamping);
        assertEquals(0.0f, restAcc, DELTA);
    }

    @Test
    public void testHookeSpringWithVariableMass() {
        // Newton's Second Law & Hooke's Law: a = (-k * dx - c * v) / m
        float displacement = 120.0f;
        float velocity = 30.0f;
        float springStrength = 15.0f;
        float springDamping = 4.0f;

        // When mass = 1.0 kg (default)
        float accMass1 = PhysicsEngine.calculateHookeSpringAcceleration(
                displacement, velocity, springStrength, springDamping, 1.0f);
        assertEquals(-1920.0f, accMass1, DELTA);

        // When mass = 2.0 kg (heavier ball -> acceleration halved)
        float accMass2 = PhysicsEngine.calculateHookeSpringAcceleration(
                displacement, velocity, springStrength, springDamping, 2.0f);
        assertEquals(-960.0f, accMass2, DELTA);

        // When mass = 4.0 kg (very heavy ball -> sluggish acceleration)
        float accMass4 = PhysicsEngine.calculateHookeSpringAcceleration(
                displacement, velocity, springStrength, springDamping, 4.0f);
        assertEquals(-480.0f, accMass4, DELTA);
    }

    @Test
    public void testBoundaryCollisionMomentumDecay() {
        // Boundary collision: velocity reverses direction and decays by restitution factor
        float incomingVx = 450.0f;
        float bounce = 0.75f;

        // When hitting right or bottom boundary (v > 0 -> v_bounce < 0)
        float bouncedVx = PhysicsEngine.resolveBoundaryBounce(incomingVx, bounce);
        assertEquals(-337.5f, bouncedVx, DELTA);
        assertTrue("Reversed velocity must oppose original motion", bouncedVx < 0);
        assertEquals(Math.abs(incomingVx) * bounce, Math.abs(bouncedVx), DELTA);

        // When hitting left or top boundary (v < 0 -> v_bounce > 0)
        float incomingVy = -320.0f;
        float bouncedVy = PhysicsEngine.resolvePositiveBoundaryBounce(incomingVy, bounce);
        assertEquals(240.0f, bouncedVy, DELTA);
        assertTrue("Reversed velocity must become positive", bouncedVy > 0);
    }

    @Test
    public void testDeltaTimeClamping() {
        // Delta time must be clamped to avoid quantum tunneling / bullet-through-wall when lag occurs
        float normalDt = 0.0166f;
        float normalClamped = PhysicsEngine.clampDeltaTime(normalDt, PhysicsEngine.MAX_DELTA_TIME);
        assertEquals(0.0166f, normalClamped, DELTA);

        // Huge freeze spike (e.g. 500ms lag)
        float spikeDt = 0.500f;
        float spikeClamped = PhysicsEngine.clampDeltaTime(spikeDt, PhysicsEngine.MAX_DELTA_TIME);
        assertEquals(PhysicsEngine.MAX_DELTA_TIME, spikeClamped, DELTA);
        assertEquals(0.032f, spikeClamped, DELTA);
    }
}

