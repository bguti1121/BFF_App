package com.example.bff_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EditExpenses extends AppCompatActivity {
    LinearLayout expenseContainer;
    Button addExpenseBtn, saveExpensesBtn;
    ImageButton backButton;
    FirebaseAuth mAuth;
    DatabaseReference userRef;
    String uid;
    double totalExpense = 0.0;
    Set<String> deletedExpenseIds = new HashSet<>();
    boolean isMonthly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        if (uid == null) {
            Log.e("SetExpenses", "User ID is null!");
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("expenses");

        expenseContainer = findViewById(R.id.expenseContainer);
        addExpenseBtn = findViewById(R.id.addExpenseBtn);
        saveExpensesBtn = findViewById(R.id.saveExpensesBtn);
        backButton = findViewById(R.id.backButton);

        addExpenseBtn.setOnClickListener(v -> addExpenseRow());
        saveExpensesBtn.setOnClickListener(v -> saveExpensesToFirebase());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditExpenses.this, Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        loadExistingExpenses();
    }

    private void loadExistingExpenses() {
        expenseContainer.removeAllViews();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Expense expense = snapshot.getValue(Expense.class);
                    String expenseId = snapshot.getKey();

                    if (expense != null && expenseId != null) {
                        boolean isMonthly = expense.getIsMonthly();
                        String expenseDate = expense.getExpenseDate();

                        // Show if monthly OR if this month's non-recurring
                        boolean showExpense = isMonthly || (expenseDate != null && expenseDate.startsWith(currentMonth));

                        if (showExpense) {
                            View row = LayoutInflater.from(this).inflate(R.layout.expense_row, null);
                            EditText categoryInput = row.findViewById(R.id.expenseCategory);
                            EditText amountInput = row.findViewById(R.id.expenseAmount);
                            CheckBox monthlyCheckbox = row.findViewById(R.id.checkbox);

                            categoryInput.setText(expense.getCategory());
                            amountInput.setText(String.valueOf(expense.getAmount()));
                            monthlyCheckbox.setChecked(isMonthly);

                            row.setTag(R.id.expenseIdTag, expenseId);
                            row.setTag(R.id.expenseDateTag, expenseDate);

                            Button deleteButton = row.findViewById(R.id.deleteExpenseBtn);
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setOnClickListener(v -> {
                                userRef.child(expenseId).removeValue();
                                expenseContainer.removeView(row);
                            });

                            expenseContainer.addView(row);
                        }
                    }
                }
            }
        });
    }
    private void addExpenseRow() {
        // Check if the last row is blank before adding a new one
        if (expenseContainer.getChildCount() > 0) {
            View lastRow = expenseContainer.getChildAt(expenseContainer.getChildCount() - 1);
            EditText lastCategory = lastRow.findViewById(R.id.expenseCategory);
            EditText lastAmount = lastRow.findViewById(R.id.expenseAmount);

            if (lastCategory.getText().toString().trim().isEmpty() &&
                    lastAmount.getText().toString().trim().isEmpty()) {
                lastCategory.setError("Fill this before adding another");
                lastAmount.setError("Fill this before adding another");
                return;
            }
        }
        View row = LayoutInflater.from(this).inflate(R.layout.expense_row, null);
        expenseContainer.addView(row); //new
        Button deleteButton = row.findViewById(R.id.deleteExpenseBtn);
        CheckBox checkBox = row.findViewById(R.id.checkbox);
        if (expenseContainer.getChildCount() > 1) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                String expenseId = (String) row.getTag();
                if (expenseId != null) deletedExpenseIds.add(expenseId);
                expenseContainer.removeView(row);
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
        isMonthly = checkBox.isChecked();

    }

    private void saveExpensesToFirebase() {
        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        DatabaseReference expensesRef = baseRef.child("expenses");

        for (String expenseId : deletedExpenseIds) {
            expensesRef.child(expenseId).removeValue();
        }

        totalExpense = 0.0;
        boolean hasError = false;


        for (int i = 0; i < expenseContainer.getChildCount(); i++) {
            View row = expenseContainer.getChildAt(i);
            EditText categoryInput = row.findViewById(R.id.expenseCategory);
            EditText amountInput = row.findViewById(R.id.expenseAmount);
            CheckBox monthlyCheckbox = row.findViewById(R.id.checkbox); // <-- fetch it here
            boolean isRowMonthly = monthlyCheckbox.isChecked();
            String category = categoryInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();

            if (category.isEmpty()) {
                categoryInput.setError("Category is required");
                hasError = true;
            }

            if (amountStr.isEmpty()) {
                amountInput.setError("Amount is required");
                hasError = true;
            }

            if (!category.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    totalExpense += amount;
                    String expenseId = (String) row.getTag(R.id.expenseIdTag);
                    String expenseDate = (String) row.getTag(R.id.expenseDateTag);
                    if(expenseId!=null){
                        Expense updatedExpense = new Expense(amount, category, expenseDate, isRowMonthly);
                        expensesRef.child(expenseId).setValue(updatedExpense);
                    }
                    else{
                        String newDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        Expense newExpense = new Expense(amount, category, newDate, isRowMonthly);
                        expensesRef.push().setValue(newExpense);
                    }
                } catch (NumberFormatException e) {
                    amountInput.setError("Enter a valid number");
                    hasError = true;
                }
            }
        }

        for (String expenseId : deletedExpenseIds) {
            expensesRef.child(expenseId).removeValue();
        }

        if (hasError) {
            // Stop execution if any input was invalid
            return;
        }

        // Continue to MainActivity after saving
        startActivity(new Intent(EditExpenses.this, Dashboard.class));
        finish();

    }
}