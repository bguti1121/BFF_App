package com.example.bff_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View; // ← Missing
import android.widget.TextView; // ← For suggestionMessage
import android.app.DatePickerDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddGoalActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;
    EditText goalNameInput, goalAmountInput, goalDateInput;
    Button confirmAddGoalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        if (uid == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("savinggoals");

        goalNameInput = findViewById(R.id.goalNameInput);
        goalAmountInput = findViewById(R.id.goalAmountInput);
        goalDateInput = findViewById(R.id.goalDateInput);
        confirmAddGoalButton = findViewById(R.id.confirmAddGoalButton);

        goalDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddGoalActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        goalDateInput.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        confirmAddGoalButton.setOnClickListener(v -> {
            String title = goalNameInput.getText().toString();
            String amountStr = goalAmountInput.getText().toString();
            String date = goalDateInput.getText().toString();

            if (title.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            double goalAmount;
            try {
                goalAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid amount.", Toast.LENGTH_SHORT).show();
                return;
            }
            double saved = 0.0; // New goal starts with $0 saved

            // Get today's date for creation timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);

            try {
                Date selected = sdf.parse(date);
                Date today = new Date();

                if (!selected.after(today)) {
                    Toast.makeText(this, "Please choose a future date.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show();
                return;
            }

            Goal newGoal = new Goal(title, goalAmount, saved, date);

            // Push goal to Firebase
            userRef.push().setValue(newGoal).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Goal added!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to SavingGoalsActivity
                } else {
                    Toast.makeText(this, "Failed to add goal", Toast.LENGTH_SHORT).show();
                }
            });
        });

        Button showSuggestionBtn = findViewById(R.id.showSuggestionBtn);

        showSuggestionBtn.setOnClickListener(v -> {
            // Inflate custom layout
            LayoutInflater inflater = LayoutInflater.from(this);
            View popupView = inflater.inflate(R.layout.suggested_goal_popup, null);

            // Set dynamic suggestion (or static for now)
            TextView suggestionMessage = popupView.findViewById(R.id.suggestionMessage);
            suggestionMessage.setText("To reach your goal by the date, you should save $50 per pay period."); // Replace with real calc later

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(popupView)
                    .create();

            // Close button
            Button closeBtn = popupView.findViewById(R.id.closePopupBtn);
            closeBtn.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Go back to SavingGoalsActivity
        });
    }
}