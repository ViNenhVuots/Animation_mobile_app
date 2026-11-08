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

        SeekBar seekMass =
                view.findViewById(R.id.seekMass);

        // Anh xa TextView
        TextView txtGravity =
                view.findViewById(R.id.txtGravity);

        TextView txtSpring =
                view.findViewById(R.id.txtSpring);

        TextView txtBounce =
                view.findViewById(R.id.txtBounce);

        TextView txtMass =
                view.findViewById(R.id.txtMass);

        // Nut Reset Physics
        Button btnReset =
                view.findViewById(R.id.btnReset);

        // ==============================
        // PHYSICS CONTROLS
        // ==============================

        // Dieu chinh trong luc
        seekGravity.setOnSeekBarChangeListener(
                createListener(progress -> {
                    physicsView.setGravity(progress);
                    txtGravity.setText(getString(R.string.lbl_gravity_format, progress));
                })
        );

        // Dieu chinh do cung lo xo
        seekSpring.setOnSeekBarChangeListener(
                createListener(progress -> {
                    physicsView.setSpringStrength(progress);
                    txtSpring.setText(getString(R.string.lbl_spring_format, progress));
                })
        );

        // Dieu chinh do bat nay
        seekBounce.setOnSeekBarChangeListener(
                createListener(progress -> {
                    float bounce = progress / 100f;
                    physicsView.setBounce(bounce);
                    txtBounce.setText(getString(R.string.lbl_bounce_format, progress));
                })
        );

        // Dieu chinh do nang (khoi luong qua bong)
        seekMass.setOnSeekBarChangeListener(
                createListener(progress -> {
                    float mass = 0.5f + (progress * 0.1f);
                    physicsView.setMass(mass);
                    txtMass.setText(getString(R.string.lbl_mass_format, mass));
                })
        );

        // Reset bong Physics
        btnReset.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            physicsView.resetBall();
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
        physicsView = null;
        super.onDestroyView();
    }
}
