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

        // Load default fragment (HOME)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.navHostFragment, new HomeFragment())
                    .commit();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_physics) {
                selectedFragment = new PhysicsAnimationFragment();
            } else if (itemId == R.id.nav_simulation) {
                selectedFragment = new SimulationFragment();
            } else if (itemId == R.id.nav_drawing) {
                selectedFragment = new DrawingFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.navHostFragment, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    public void navigateToTab(int itemId) {
        binding.bottomNavigationView.setSelectedItemId(itemId);
    }
}
