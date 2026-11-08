package com.example.myapplication.feature_drawing_charts;

/** Pure chart calculations used by DynamicChartView and local JVM tests. */
public final class ChartMath {
    private ChartMath() { }

    public static float target(float current, float randomUnit) {
        float change = (randomUnit - 0.5f) * 30f;
        return Math.max(0f, Math.min(100f, current + change));
    }

    public static float interpolate(float start, float target, float progress) {
        return start + (target - start) * progress;
    }

    public static float valueToY(float value, float bottom, float chartHeight) {
        return bottom - (value / 100f) * chartHeight;
    }
}
