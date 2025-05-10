package com.example.bff_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;


import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

public class GoalDetailActivity extends AppCompatActivity {

    TextView goalTitleText, goalAmountText, amountSavedText;
    ProgressBar progressBar;
    Button completeGoalBtn, deleteGoalBtn;

    Goal goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);

        // Bind views
        goalTitleText = findViewById(R.id.goalTitleText);
        goalAmountText = findViewById(R.id.goalAmountText);
        amountSavedText = findViewById(R.id.amountSavedText);
        progressBar = findViewById(R.id.progressBar);
        completeGoalBtn = findViewById(R.id.completeGoalBtn);
        deleteGoalBtn = findViewById(R.id.deleteGoalBtn);

        // Get goal from intent
        goal = (Goal) getIntent().getSerializableExtra("goal");

        if (goal != null) {
            displayGoalDetails(goal);
        } else {
            Toast.makeText(this, "Goal is null", Toast.LENGTH_SHORT).show();
        }


        completeGoalBtn.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("completedGoal", goal); // ✅ different key
            setResult(RESULT_OK, resultIntent);
            Log.d("GoalDetailActivity", "Complete button clicked");

            finish();
        });

        deleteGoalBtn.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("deletedGoal", goal);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }

    private void displayGoalDetails(Goal goal) {
        double saved = goal.getAmountSaved();
        double total = goal.getAmountGoal();
        double left = Math.max(total - saved, 0);
        int progress = total > 0 ? (int) ((saved / total) * 100) : 0;

        goalTitleText.setText(goal.getTitle());
        goalAmountText.setText("Goal Amount: $" + total);
        amountSavedText.setText("Amount Saved: $" + saved);
        progressBar.setProgress(progress);

        int percent = total > 0 ? (int) ((saved / total) * 100) : 0;

        progressBar.setProgress(percent);

        if (total == 0 || percent > 100) {
            // Goal amount is 0 or overfilled — treat as red warning
            progressBar.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.budgetRed))
            );
        } else {
            // Valid and under/at goal — normal green
            progressBar.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.forestGreen))
            );
        }


        progressBar.setProgressBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft))
        );

    }
}