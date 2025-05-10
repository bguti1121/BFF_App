package com.example.bff_app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateSavedActivity extends AppCompatActivity {

    TextView goalTitleText, currentSavedText;
    EditText addAmountInput;
    Button addAmountBtn;

    Goal goal;
    DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_saved);

        // Bind views
        goalTitleText = findViewById(R.id.goalTitleText);
        currentSavedText = findViewById(R.id.currentSavedText);
        addAmountInput = findViewById(R.id.addAmountInput);
        addAmountBtn = findViewById(R.id.addAmountBtn);

        // Get goal object from intent
        goal = (Goal) getIntent().getSerializableExtra("goal");

        if (goal == null || goal.getKey() == null) {
            Toast.makeText(this, "Invalid goal", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        goalTitleText.setText(goal.getTitle());
        String currentSavedDisplay = String.format(
                Locale.getDefault(),
                "Currently Saved: $%.2f / $%.2f",
                goal.getAmountSaved(),
                goal.getAmountGoal()
        );
        currentSavedText.setText(currentSavedDisplay);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("savinggoals")
                .child(goal.getKey());

        // On Add Amount button click
        addAmountBtn.setOnClickListener(v -> {
            String inputStr = addAmountInput.getText().toString().trim();
            if (inputStr.isEmpty()) {
                Toast.makeText(this, "Enter an amount to add", Toast.LENGTH_SHORT).show();
                return;
            }


            try {
                double added = Double.parseDouble(inputStr);
                double newSaved = goal.getAmountSaved() + added;

                // Update goal object
                goal.setAmountSaved(newSaved);

                // Push update to Firebase
                userRef.setValue(goal)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Amount updated!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                        });
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });

        Button showSuggestionBtn = findViewById(R.id.showSuggestionBtn);

        showSuggestionBtn.setOnClickListener(v -> {
            // Inflate custom layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View popupView = inflater.inflate(R.layout.suggested_goal_popup, null);

            // Set dynamic suggestion (or static for now)
            TextView suggestionMessage = popupView.findViewById(R.id.suggestionMessage);
// Calculate suggestion dynamically
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date today = new Date();
                Date target = sdf.parse(goal.getTargetDate());

                if (target != null && target.after(today)) {
                    long diffMillis = target.getTime() - today.getTime();
                    long weeks = diffMillis / (1000L * 60 * 60 * 24 * 7); // milliseconds in a week
                    weeks = Math.max(1, weeks); // avoid divide by 0

                    double remainingAmount = Math.max(goal.getAmountGoal() - goal.getAmountSaved(), 0);
                    double perWeek = remainingAmount / weeks;

                    String message = String.format(
                            Locale.getDefault(),
                            "To reach your goal by %s, you should save about $%.2f per week.",
                            goal.getTargetDate(),
                            perWeek
                    );

                    suggestionMessage.setText(message);
                } else {
                    suggestionMessage.setText("Your target date has already passed or is today.");
                }
            } catch (ParseException e) {
                suggestionMessage.setText("Invalid goal date format.");
            }

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(popupView)
                    .create();

            // Close button
            Button closeBtn = popupView.findViewById(R.id.closePopupBtn);
            closeBtn.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());

    }
}
