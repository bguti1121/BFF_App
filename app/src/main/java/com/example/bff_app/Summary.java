package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.button.MaterialButton;


public class Summary extends AppCompatActivity {
    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;
    MaterialButton billsButton, trackingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_summary);

        // bills and tracking buttons
        billsButton = findViewById(R.id.billsButton);
        trackingButton = findViewById(R.id.trackingButton);

        //Bottom nav buttons
        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);

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

        billsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Summary.this, Bills.class);
            startActivity(intent);
        });

        trackingButton.setOnClickListener(v -> {
            Intent intent = new Intent(Summary.this, Tracking.class);
            startActivity(intent);
        });
    }
}