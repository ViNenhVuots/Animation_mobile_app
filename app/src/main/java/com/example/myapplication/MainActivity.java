package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.feature_drawing_charts.DrawingFragment;
import com.example.myapplication.feature_physics.PhysicsAnimationFragment;
import com.example.myapplication.feature_simulation.SimulationFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Giữ 4 instance cố định của các Fragment
    private Fragment homeFragment = new HomeFragment();
    private Fragment physicsFragment = new PhysicsAnimationFragment();
    private Fragment simulationFragment = new SimulationFragment();
    private Fragment drawingFragment = new DrawingFragment();
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các Fragment vào container bằng show/hide để giữ nguyên trạng thái
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.navHostFragment, homeFragment, "HOME")
                    .add(R.id.navHostFragment, physicsFragment, "PHYSICS").hide(physicsFragment)
                    .add(R.id.navHostFragment, simulationFragment, "SIMULATION").hide(simulationFragment)
                    .add(R.id.navHostFragment, drawingFragment, "DRAWING").hide(drawingFragment)
                    .commit();
            activeFragment = homeFragment;
        } else {
            Fragment restoredHome = getSupportFragmentManager().findFragmentByTag("HOME");
            if (restoredHome != null) homeFragment = restoredHome;

            Fragment restoredPhysics = getSupportFragmentManager().findFragmentByTag("PHYSICS");
            if (restoredPhysics != null) physicsFragment = restoredPhysics;

            Fragment restoredSimulation = getSupportFragmentManager().findFragmentByTag("SIMULATION");
            if (restoredSimulation != null) simulationFragment = restoredSimulation;

            Fragment restoredDrawing = getSupportFragmentManager().findFragmentByTag("DRAWING");
            if (restoredDrawing != null) drawingFragment = restoredDrawing;

            if (homeFragment != null && !homeFragment.isHidden()) {
                activeFragment = homeFragment;
            } else if (physicsFragment != null && !physicsFragment.isHidden()) {
                activeFragment = physicsFragment;
            } else if (simulationFragment != null && !simulationFragment.isHidden()) {
                activeFragment = simulationFragment;
            } else if (drawingFragment != null && !drawingFragment.isHidden()) {
                activeFragment = drawingFragment;
            } else {
                activeFragment = homeFragment;
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment targetFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                targetFragment = homeFragment;
            } else if (itemId == R.id.nav_physics) {
                targetFragment = physicsFragment;
            } else if (itemId == R.id.nav_simulation) {
                targetFragment = simulationFragment;
            } else if (itemId == R.id.nav_drawing) {
                targetFragment = drawingFragment;
            }

            if (targetFragment != null) {
                if (targetFragment != activeFragment) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(activeFragment)
                            .show(targetFragment)
                            .commit();
                    activeFragment = targetFragment;
                }
                return true;
            }
            return false;
        });
    }

    public void navigateToTab(int itemId) {
        binding.bottomNavigationView.setSelectedItemId(itemId);
    }
}
