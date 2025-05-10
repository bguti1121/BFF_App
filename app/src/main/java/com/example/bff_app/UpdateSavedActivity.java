package com.example.bff_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

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
        currentSavedText.setText("Currently Saved: $" + goal.getAmountSaved());

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

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // closes the activity

    }
}
