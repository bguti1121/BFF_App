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

public class SetExpensesActivity extends AppCompatActivity {
    LinearLayout expenseContainer;
    Button addExpenseBtn, saveExpensesBtn;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_expenses);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        if (uid == null) {
            Log.e("SetExpenses", "User ID is null!");
            return;
        }
        // Firebase user reference
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("expenses");
        // Initialize UI elements
        expenseContainer = findViewById(R.id.expenseContainer);
        addExpenseBtn = findViewById(R.id.addExpenseBtn);
        saveExpensesBtn = findViewById(R.id.saveExpensesBtn);
        // Initializes the addRow resource
        addExpenseBtn.setOnClickListener(v -> addExpenseRow());
        saveExpensesBtn.setOnClickListener(v -> saveExpensesToFirebase());

        addExpenseRow(); // start with one row
    }
    // addExpenseRow function
    private void addExpenseRow() {
        View row = LayoutInflater.from(this).inflate(R.layout.expense_row, null);
        // Hide the delete button if it exists in the layout
        Button deleteButton = row.findViewById(R.id.deleteExpenseBtn);
        if (deleteButton != null) {
            deleteButton.setVisibility(View.GONE);
        }
        expenseContainer.addView(row);
    }
    // Saves expenses to fire base under User --> Expenses
    private void saveExpensesToFirebase() {
        userRef.removeValue(); // Optional: clear existing expenses
        // Create ExpenseManager
        ExpenseManager expenseManager = new ExpenseManager();
        for (int i = 0; i < expenseContainer.getChildCount(); i++) {
            View row = expenseContainer.getChildAt(i);
            EditText categoryInput = row.findViewById(R.id.expenseCategory);
            EditText amountInput = row.findViewById(R.id.expenseAmount);

            String category = categoryInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).
                            format(new Date());

                    // Add to local manager (optional)
                    expenseManager.addExpense(amount, category, date, true);

                    // Save to Firebase
                    Expense expense = new Expense(amount, category, date, true);
                    userRef.push().setValue(expense);
                } catch (NumberFormatException e) {
                    // Handle invalid number input
                    e.printStackTrace();
                }
            }
        }

        // Continue to SetIncomesActivity after saving
        startActivity(new Intent(SetExpensesActivity.this, SetIncomesActivity.class));
        finish();
    }
}