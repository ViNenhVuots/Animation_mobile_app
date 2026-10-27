
package com.example.myapplication.feature_physics;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PropertyAnimationHelper {

    private AnimatorSet animatorSet;

    // START: Bat dau hoac tiep tuc animation
    public void startAnimation(View targetView) {

        if (targetView == null) {
            return;
        }

        // Neu dang tam dung thi tiep tuc
        if (animatorSet != null && animatorSet.isPaused()) {
            animatorSet.resume();
            return;
        }

        // Neu animation dang chay thi khong tao moi
        if (animatorSet != null && animatorSet.isRunning()) {
            return;
        }

        // Chi reset ve ban dau khi tao animation moi
        targetView.setTranslationX(0f);
        targetView.setTranslationY(0f);
        targetView.setRotation(0f);
        targetView.setScaleX(1f);
        targetView.setScaleY(1f);

        // Di chuyen ngang
        ObjectAnimator moveX = ObjectAnimator.ofFloat(
                targetView,
                "translationX",
                0f, 120f, -120f, 0f
        );

        // Xoay 360 do
        ObjectAnimator rotate = ObjectAnimator.ofFloat(
                targetView,
                "rotation",
                0f, 360f
        );

        // Phong to va thu nho
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(
                targetView,
                "scaleX",
                1f, 1.5f, 1f
        );

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(
                targetView,
                "scaleY",
                1f, 1.5f, 1f
        );

        moveX.setDuration(2000);
        rotate.setDuration(2000);
        scaleX.setDuration(2000);
        scaleY.setDuration(2000);

        animatorSet = new AnimatorSet();

        animatorSet.playTogether(
                moveX,
                rotate,
                scaleX,
                scaleY
        );

        animatorSet.setInterpolator(
                new AccelerateDecelerateInterpolator()
        );

        animatorSet.start();
    }

    // STOP: Tam dung, khong huy animation
    public void stopAnimation() {

        if (animatorSet != null
                && animatorSet.isRunning()) {

            animatorSet.pause();
        }
    }

    // RESET: Huy animation va ve trang thai ban dau
    public void resetAnimation(View targetView) {

        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }

        if (targetView == null) {
            return;
        }

        targetView.setTranslationX(0f);
        targetView.setTranslationY(0f);
        targetView.setRotation(0f);
        targetView.setScaleX(1f);
        targetView.setScaleY(1f);
    }

    // Huy animation khi khong con su dung View
    public void release() {
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
    }
}
