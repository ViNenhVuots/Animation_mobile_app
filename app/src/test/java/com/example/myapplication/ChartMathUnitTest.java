package com.example.myapplication;

import com.example.myapplication.feature_drawing_charts.ChartMath;
import org.junit.Test;
import static org.junit.Assert.*;

public class ChartMathUnitTest {
    private static final float EPS = 0.0001f;

    @Test public void interpolationPreservesStartAndEnd() {
        float[] starts = {20, 50, 30, 80, 60, 90, 40, 75};
        float[] targets = {35, 35, 45, 65, 75, 100, 25, 60};
        for (int i = 0; i < starts.length; i++) {
            assertEquals(starts[i], ChartMath.interpolate(starts[i], targets[i], 0f), EPS);
            assertEquals(targets[i], ChartMath.interpolate(starts[i], targets[i], 1f), EPS);
        }
    }

    @Test public void interpolationMovesGraduallyInBothDirections() {
        assertEquals(35f, ChartMath.interpolate(20f, 50f, 0.5f), EPS);
        assertEquals(65f, ChartMath.interpolate(80f, 20f, 0.25f), EPS);
        assertEquals(50f, ChartMath.interpolate(50f, 50f, 0.7f), EPS);
    }

    @Test public void targetsStayWithinBoundsAndMaximumChange() {
        assertEquals(0f, ChartMath.target(5f, 0f), EPS);
        assertEquals(100f, ChartMath.target(95f, 1f), EPS);
        for (int current = 0; current <= 100; current++) {
            for (int sample = 0; sample <= 100; sample++) {
                float target = ChartMath.target(current, sample / 100f);
                assertTrue(target >= 0f && target <= 100f);
                assertTrue(Math.abs(target - current) <= 15f + EPS);
            }
        }
    }

    @Test public void valueMappingUsesDownwardAndroidYAxis() {
        assertEquals(600f, ChartMath.valueToY(0f, 600f, 400f), EPS);
        assertEquals(400f, ChartMath.valueToY(50f, 600f, 400f), EPS);
        assertEquals(200f, ChartMath.valueToY(100f, 600f, 400f), EPS);
    }
}
