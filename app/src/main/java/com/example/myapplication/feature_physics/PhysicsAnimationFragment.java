
package com.example.myapplication.feature_physics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class PhysicsAnimationFragment extends Fragment {

    private SpringGravityView physicsView;

    // Doi tuong dung cho Property Animation
    private View animationTarget;

    // Class quan ly ObjectAnimator va AnimatorSet
    private PropertyAnimationHelper animationHelper;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_physics,
                container,
                false
        );

        // Anh xa Physics View
        physicsView = view.findViewById(
                R.id.springGravityView
        );

        // Anh xa SeekBar
        SeekBar seekGravity =
                view.findViewById(R.id.seekGravity);

        SeekBar seekSpring =
                view.findViewById(R.id.seekSpring);

        SeekBar seekBounce =
                view.findViewById(R.id.seekBounce);

        // Anh xa TextView
        TextView txtGravity =
                view.findViewById(R.id.txtGravity);

        TextView txtSpring =
                view.findViewById(R.id.txtSpring);

        TextView txtBounce =
                view.findViewById(R.id.txtBounce);

        // Nut Reset Physics
        Button btnReset =
                view.findViewById(R.id.btnReset);

        // ==============================
        // PROPERTY ANIMATION
        // ==============================

        animationTarget =
                view.findViewById(R.id.animationTarget);

        Button btnStartProperty =
                view.findViewById(R.id.btnStartProperty);

        Button btnStopProperty =
                view.findViewById(R.id.btnStopProperty);

        Button btnResetProperty =
                view.findViewById(R.id.btnResetProperty);

        animationHelper = new PropertyAnimationHelper();

        // ==============================
        // PHYSICS CONTROLS
        // ==============================

        // Dieu chinh trong luc
        seekGravity.setOnSeekBarChangeListener(
                createListener(progress -> {
                    physicsView.setGravity(progress);
                    txtGravity.setText(
                            "Gravity: " + progress
                    );
                })
        );

        // Dieu chinh do cung lo xo
        seekSpring.setOnSeekBarChangeListener(
                createListener(progress -> {
                    physicsView.setSpringStrength(progress);

                    txtSpring.setText(
                            "Spring Strength: " + progress
                    );
                })
        );

        // Dieu chinh do bat nay
        seekBounce.setOnSeekBarChangeListener(
                createListener(progress -> {
                    float bounce = progress / 100f;

                    physicsView.setBounce(bounce);

                    txtBounce.setText(
                            "Bounce: " + progress + "%"
                    );
                })
        );

        // Reset bong Physics
        btnReset.setOnClickListener(v -> {
            physicsView.resetBall();
        });

        // ==============================
        // PROPERTY ANIMATION CONTROLS
        // ==============================

        // START: Di chuyen, xoay va phong to
        btnStartProperty.setOnClickListener(v -> {
            animationHelper.startAnimation(animationTarget);
        });

        // STOP: Dung hieu ung
        btnStopProperty.setOnClickListener(v -> {
            animationHelper.stopAnimation();
        });

        // RESET: Khoi phuc vi tri va kich thuoc
        btnResetProperty.setOnClickListener(v -> {
            animationHelper.resetAnimation(animationTarget);
        });

        return view;
    }

    // Interface xu ly gia tri SeekBar
    private interface ProgressCallback {
        void onProgress(int progress);
    }

    private SeekBar.OnSeekBarChangeListener createListener(
            ProgressCallback callback) {

        return new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser) {

                callback.onProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    @Override
    public void onDestroyView() {
        if (animationHelper != null) {
            animationHelper.stopAnimation();
            animationHelper = null;
        }

        animationTarget = null;
        physicsView = null;

        super.onDestroyView();
    }
}
