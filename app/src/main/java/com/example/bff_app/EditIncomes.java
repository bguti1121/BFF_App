package com.example.bff_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

public class EditIncomes extends AppCompatActivity {

    LinearLayout incomeContainer;
    Button addIncomeBtn, saveIncomesBtn;
    ImageButton backButton;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;
    double totalIncome = 0.0;
    Set<String> deletedIncomeIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_income);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        if (uid == null) {
            Log.e("SetIncomes", "User ID is null!");
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("incomes");


        incomeContainer = findViewById(R.id.incomeContainer);
        addIncomeBtn = findViewById(R.id.addIncomeBtn);
        saveIncomesBtn = findViewById(R.id.saveIncomesBtn);
        backButton = findViewById(R.id.backButton);

        addIncomeBtn.setOnClickListener(v -> addIncomeRow());
        saveIncomesBtn.setOnClickListener(v -> {
            saveIncomesToFirebase();

            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditIncomes.this, Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        loadExistingIncomes();
    }

    private void loadExistingIncomes() { //new
        incomeContainer.removeAllViews();

        // Get current month
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Income income = snapshot.getValue(Income.class);
                    String incomeId = snapshot.getKey();

                    if (income != null && incomeId != null && income.getIncomeDate().startsWith(currentMonth)) {
                        View row = LayoutInflater.from(EditIncomes.this).inflate(R.layout.income_row, null);
                        EditText categoryInput = row.findViewById(R.id.IncomeCategory);
                        EditText amountInput = row.findViewById(R.id.IncomeAmount);

                        categoryInput.setText(income.getCategory());
                        amountInput.setText(String.valueOf(income.getAmount()));

                        incomeContainer.addView(row);
                        row.setTag(incomeId);

                        Button deleteButton = row.findViewById(R.id.deleteIncomeBtn);
                        deleteButton.setVisibility(View.VISIBLE);
                        deleteButton.setOnClickListener(v -> {
                            userRef.child(incomeId).removeValue();
                            incomeContainer.removeView(row);
                        });
                    }
                }
            } else {
                Log.e("AddIncome", "Failed to load existing incomes.");
            }
        });
    }

    private void addIncomeRow() {
        View row = LayoutInflater.from(this).inflate(R.layout.income_row, null);
        incomeContainer.addView(row);
        // Make the delete button visible for all rows except the first one
        Button deleteButton = row.findViewById(R.id.deleteIncomeBtn);
        if (incomeContainer.getChildCount() > 1) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                String incomeId = (String) row.getTag(); // Get the Firebase ID of the income
                deletedIncomeIds.add(incomeId); // Mark it for deletion
                incomeContainer.removeView(row); // Remove from the UI
            });
        } else {
            deleteButton.setVisibility(View.GONE); // No delete for the first row
        }
    }

    private void saveIncomesToFirebase() {
        DatabaseReference baseRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid);
        DatabaseReference incomesRef = baseRef.child("incomes"); //new

        // Step 1: Remove the incomes that were marked for deletion
        for (String incomeId : deletedIncomeIds) { //new
            incomesRef.child(incomeId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("AddIncome", "Deleted income ID: " + incomeId);
                } else {
                    Log.e("AddIncome", "Failed to delete income ID: " + incomeId);
                }
            });
        }
        
        for (int i = 0; i < incomeContainer.getChildCount(); i++) {
            View row = incomeContainer.getChildAt(i);
            EditText categoryInput = row.findViewById(R.id.IncomeCategory);
            EditText amountInput = row.findViewById(R.id.IncomeAmount);

            String category = categoryInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    totalIncome += amount;
                    // Saves current date
                    Object tag = row.getTag(); // Firebase ID if existing

                    if (tag != null) {
                        // Existing income - retain original date
                        String incomeId = tag.toString();
                        incomesRef.child(incomeId).get().addOnSuccessListener(snapshot -> {
                            Income existingIncome = snapshot.getValue(Income.class);
                            if (existingIncome != null) {
                                Income updatedIncome = new Income(amount, category, existingIncome.getIncomeDate());
                                incomesRef.child(incomeId).setValue(updatedIncome);
                            }
                        });
                    } else {
                        // New income - assign today's date
                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        Income newIncome = new Income(amount, category, date);
                        incomesRef.push().setValue(newIncome);
                    }
                } catch (NumberFormatException e) {
                    Log.e("AddIncome", "Invalid income amount: " + amountStr);
                }
            }
        }

        // This is what updates the budget!
        double finalTotalIncome = totalIncome;
        baseRef.child("monthlyBudget").setValue(totalIncome)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AddIncome", "Monthly budget set to: " + finalTotalIncome);
                    } else {
                        Log.e("AddIncome", "Failed to set monthly budget", task.getException());
                    }
                });
    }
}