package com.example.bff_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

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

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Go back to SavingGoalsActivity
        });
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
    }
}