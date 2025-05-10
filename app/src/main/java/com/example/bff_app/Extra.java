package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Extra extends AppCompatActivity {
    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;
    LinearLayout logOut, settings, yearlySum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        //Dark mode if-else
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_extra);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize XML elements
        settings = findViewById(R.id.settings);
        yearlySum = findViewById(R.id.yearlySummary);
        logOut = findViewById(R.id.logout);
        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);

        //yearlySummary button onClick event handler
        yearlySum.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), YearlySummary.class);
            startActivity(intent);
            finish();
        });

        //Settings button onClick event handler
        settings.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
            finish();
        });

        //Logout button onClick event handler
        logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        //Dashboard button onClick event handler
        dashboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        });

        //Summary button onClick event handler
        summaryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Summary.class);
            startActivity(intent);
            finish();
        });

        //Goals button onClick event handler
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SavingGoalsActivity.class);
            startActivity(intent);
            finish();
        });

        //More button onClick event handler
        moreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Extra.class);
            startActivity(intent);
            finish();
        });
    }
}