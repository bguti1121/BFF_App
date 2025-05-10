package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SetIncomesActivity extends AppCompatActivity {

    LinearLayout incomeContainer;
    Button addIncomeBtn, saveIncomesBtn;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;
    double totalIncome = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_incomes);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        if (uid == null) {
            Log.e("SetIncomes", "User ID is null!");
            return;
        }
        // Monthly budget user reference
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("monthlyBudget");
        // Initialize UI elements
        incomeContainer = findViewById(R.id.incomeContainer);
        addIncomeBtn = findViewById(R.id.addIncomeBtn);
        saveIncomesBtn = findViewById(R.id.saveIncomesBtn);
        // IncomeRow initialization
        addIncomeBtn.setOnClickListener(v -> addIncomeRow());
        saveIncomesBtn.setOnClickListener(v -> {
            saveIncomesToFirebase();
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
        });
        addIncomeRow(); // start with one row
    }
    // addIncomeRow function
    private void addIncomeRow() {
        View row = LayoutInflater.from(this).inflate(R.layout.income_row, null);
        // Hide the delete button if it exists in the layout
        Button deleteButton = row.findViewById(R.id.deleteIncomeBtn);
        if (deleteButton != null) {
            deleteButton.setVisibility(View.GONE);
        }
        incomeContainer.addView(row);
    }
    // Save income and total monthly budget to firebase
    private void saveIncomesToFirebase() {
        DatabaseReference baseRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid);
        DatabaseReference incomesRef = baseRef.child("incomes");

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
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    // Saves income to firebase
                    Income income = new Income(amount, category, date);
                    incomesRef.push().setValue(income);
                } catch (NumberFormatException e) {
                    Log.e("SetIncomesActivity", "Invalid income amount: " + amountStr);
                }
            }
        }

        // This is what updates the budget in firebase!
        double finalTotalIncome = totalIncome;
        baseRef.child("monthlyBudget").setValue(totalIncome)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("SetIncomesActivity", "Monthly budget set to: " + finalTotalIncome);
                    } else {
                        Log.e("SetIncomesActivity", "Failed to set monthly budget", task.getException());
                    }
                });

        // Go back to dashboard
        startActivity(new Intent(SetIncomesActivity.this, Dashboard.class));
        finish();
    }
}