package com.example.bff_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditGoalActivity extends AppCompatActivity {
    EditText editGoalName, editGoalAmount;
    Button saveEditedGoalBtn;
    EditText goalDateInput;
    Goal goalToEdit;
    DatabaseReference userRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("savinggoals");

        // Bind views
        editGoalName = findViewById(R.id.editGoalName);
        editGoalAmount = findViewById(R.id.editGoalAmount);
        goalDateInput = findViewById(R.id.goalDateInput);
        saveEditedGoalBtn = findViewById(R.id.saveEditedGoalBtn);

        goalDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(EditGoalActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        goalDateInput.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        // Get goal passed in (if any)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("goal")) {
            goalToEdit = (Goal) intent.getSerializableExtra("goal");

            if (goalToEdit != null) {
                Log.d("EditGoalActivity", "Received goal: " + goalToEdit.getTitle());
                editGoalName.setText(goalToEdit.getTitle());
                editGoalAmount.setText(String.valueOf(goalToEdit.getAmountGoal()));
                goalDateInput.setText(goalToEdit.getTargetDate());
            } else {
                Log.e("EditGoalActivity", "goalToEdit was null after deserialization");
                Toast.makeText(this, "Error loading goal data.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e("EditGoalActivity", "No goal passed in intent");
            Toast.makeText(this, "No goal to edit.", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveEditedGoalBtn.setOnClickListener(v -> {
            String newTitle = editGoalName.getText().toString();
            String newAmountStr = editGoalAmount.getText().toString();

            if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(newAmountStr)) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            double newAmount = Double.parseDouble(newAmountStr);

            String newDateStr = goalDateInput.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);

            try {
                Date selected = sdf.parse(newDateStr);
                Date today = new Date();

                if (!selected.after(today)) {
                    Toast.makeText(this, "Please choose a future date.", Toast.LENGTH_SHORT).show();
                    return;
                }

                goalToEdit.setTargetDate(newDateStr); // ✅ save the new valid date

            } catch (ParseException e) {
                Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show();
                return;
            }


            // Update the goal object
            goalToEdit.setTitle(newTitle);
            goalToEdit.setAmountGoal(newAmount);

            if (goalToEdit.getKey() != null) {
                userRef.child(goalToEdit.getKey()).setValue(goalToEdit)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Goal updated!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to update goal.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Goal key missing. Cannot update.", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
    }
}