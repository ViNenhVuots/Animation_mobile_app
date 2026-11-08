package com.example.myapplication;

import com.example.myapplication.feature_simulation.ParticleMath;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParticleMathUnitTest {
    private static final float EPS = 0.0001f;

    @Test public void fireColorFollowsAgeAndClamps() {
        assertEquals(0xFFFF0000, ParticleMath.fireColor(0f));
        assertEquals(0xFFFF9800, ParticleMath.fireColor(0.5f));
        assertEquals(0xFFFFFF00, ParticleMath.fireColor(1f));
        assertEquals(0xFFFF0000, ParticleMath.fireColor(-1f));
        assertEquals(0xFFFFFF00, ParticleMath.fireColor(2f));
        int previous = -1;
        for (int i = 0; i <= 100; i++) {
            int green = (ParticleMath.fireColor(i / 100f) >> 8) & 255;
            assertTrue(green >= previous);
            previous = green;
        }
    }

    @Test public void alphaFadesAndHandlesExpiredParticles() {
        assertEquals(1f, ParticleMath.lifeAlpha(2f, 2f), EPS);
        assertEquals(0.5f, ParticleMath.lifeAlpha(1f, 2f), EPS);
        assertEquals(0f, ParticleMath.lifeAlpha(-1f, 2f), EPS);
        assertEquals(1f, ParticleMath.lifeAlpha(3f, 2f), EPS);
        assertEquals(0f, ParticleMath.lifeAlpha(1f, 0f), EPS);
    }

    @Test public void floorContactRequiresDownwardMotionAndIncludesRadius() {
        assertTrue(ParticleMath.hitsFloor(785f, 3f, 100f, 788f));
        assertFalse(ParticleMath.hitsFloor(784f, 3f, 100f, 788f));
        assertFalse(ParticleMath.hitsFloor(790f, 3f, -100f, 788f));
        assertFalse(ParticleMath.hitsFloor(790f, 3f, 0f, 788f));
    }

    @Test public void splashDoesNotRecursivelyGenerateMoreSplashes() {
        assertTrue(ParticleMath.canCreateSplash(false, 200f, 400f));
        assertFalse(ParticleMath.canCreateSplash(true, 200f, 400f));
        assertFalse(ParticleMath.canCreateSplash(false, -1f, 400f));
        assertFalse(ParticleMath.canCreateSplash(false, 401f, 400f));
    }

    @Test public void splashSpreadsSymmetricallyToBothSides() {
        for (float r : new float[]{0f, 0.5f, 1f}) {
            assertTrue(ParticleMath.splashVelocityX(0, r) < 0f);
            assertEquals(0f, ParticleMath.splashVelocityX(2, r), EPS);
            assertTrue(ParticleMath.splashVelocityX(4, r) > 0f);
            assertEquals(-ParticleMath.splashVelocityX(0, r),
                    ParticleMath.splashVelocityX(4, r), EPS);
        }
    }

    @Test public void splashInitiallyMovesUpwardWithinSpeedRange() {
        for (int i = 0; i <= 100; i++) {
            float vy = ParticleMath.splashVelocityY(i / 100f);
            assertTrue(vy >= -170f && vy <= -70f);
        }
    }
}
