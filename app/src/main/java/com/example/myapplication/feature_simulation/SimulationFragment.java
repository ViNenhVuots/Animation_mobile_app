package com.example.myapplication.feature_simulation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

public class SimulationFragment extends Fragment {

    private ParticleFireWaterView particleView;
    private TextView txtMode;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_simulation,
                container,
                false
        );

        // Ket noi Java voi giao dien XML
        particleView = view.findViewById(R.id.particleView);
        txtMode = view.findViewById(R.id.txtMode);

        Button btnFire = view.findViewById(R.id.btnFire);
        Button btnWater = view.findViewById(R.id.btnWater);
        Button btnClear = view.findViewById(R.id.btnClear);

        // Che do lua
        btnFire.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            particleView.setMode(ParticleFireWaterView.MODE_FIRE);
            txtMode.setText(getString(R.string.lbl_current_mode_fire));
        });

        // Che do nuoc
        btnWater.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            particleView.setMode(ParticleFireWaterView.MODE_WATER);
            txtMode.setText(getString(R.string.lbl_current_mode_water));
        });

        // Xoa tat ca hat hien tai
        btnClear.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            particleView.clearParticles();
        });

        return view;
    }
}
