package com.example.myapplication.feature_simulation;

/** Pure particle calculations shared by the View and local JVM tests. */
public final class ParticleMath {
    private ParticleMath() { }

    public static int fireColor(float progress) {
        float t = Math.max(0f, Math.min(1f, progress));
        int green = t < 0.5f
                ? Math.round(152f * (t * 2f))
                : Math.round(152f + 103f * ((t - 0.5f) * 2f));
        return 0xFFFF0000 | (green << 8);
    }

    public static float lifeAlpha(float life, float maxLife) {
        if (maxLife <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, life / maxLife));
    }

    public static boolean hitsFloor(float y, float radius, float vy, float floorY) {
        return vy > 0f && y + radius >= floorY;
    }

    public static boolean canCreateSplash(boolean isSplash, float x, float width) {
        return !isSplash && x >= 0f && x <= width;
    }

    public static float splashVelocityX(int index, float randomUnit) {
        return (index - 2f) * (45f + randomUnit * 25f);
    }

    public static float splashVelocityY(float randomUnit) {
        return -70f - randomUnit * 100f;
    }
}
