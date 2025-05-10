package com.example.bff_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class MonthlyDetailActivity extends AppCompatActivity {
    private TextView monthLabel, totalExpenses, totalIncome, goalsText;
    private RecyclerView expensesList;
    private int monthIndex;
    private double totalExp = 0, totalInc = 0;
    private final List<TransactionItem> combinedTransactions = new ArrayList<>();
    private TransactionAdapter transactionAdapter;
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_monthly_detail);

        monthLabel = findViewById(R.id.monthLabel);
        totalExpenses = findViewById(R.id.totalExpenses);
        totalIncome = findViewById(R.id.totalIncome);
        goalsText = findViewById(R.id.goalsText);
        expensesList = findViewById(R.id.expensesRecycler);

        monthIndex = getIntent().getIntExtra("monthIndex", 0);
        monthLabel.setText(getMonthName(monthIndex));

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());// finishes activity and returns to YearlySummary
        // Loads the transactionAdapter which handles showing expenses
        // and income
        transactionAdapter = new TransactionAdapter(combinedTransactions);
        expensesList.setLayoutManager(new LinearLayoutManager(this));
        expensesList.setAdapter(transactionAdapter);

        loadMonthlyData();
    }

    private void loadMonthlyData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Load Expenses
        userRef.child("expenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Expense expense = snap.getValue(Expense.class);
                    if (expense == null || expense.getExpenseDate() == null) continue;

                    try {
                        String[] parts = expense.getExpenseDate().split("-");
                        int entryMonth = Integer.parseInt(parts[1]) - 1;

                        if (entryMonth == monthIndex) {
                            combinedTransactions.add(expense);
                            totalExp += expense.getAmount();
                        }
                    } catch (Exception ignored) {}
                }

                // Load Incomes *after* expenses
                loadMonthlyIncome(userRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Load Saving Goal
        userRef.child("savinggoals").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> goalTitles = new ArrayList<>();

                for (DataSnapshot goalSnap : snapshot.getChildren()) {
                    String createdDate = goalSnap.child("createdDate").getValue(String.class);
                    String title = goalSnap.child("title").getValue(String.class);

                    if (createdDate != null && title != null) {
                        // Extract month from "YYYY-MM-DD"
                        try {
                            int goalMonth = Integer.parseInt(createdDate.split("-")[1]) - 1;
                            if (goalMonth == monthIndex) {
                                goalTitles.add(title);
                            }
                        } catch (Exception e) {
                            Log.e("GoalParse", "Failed to parse date: " + createdDate);
                        }
                    }
                }

                if (goalTitles.isEmpty()) {
                    goalsText.setText("Goals: (none)");
                } else {
                    StringBuilder sb = new StringBuilder("Goals:\n");
                    for (String goal : goalTitles) {
                        sb.append("• ").append(goal).append("\n");
                    }
                    goalsText.setText(sb.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                goalsText.setText("Goals: (error)");
                Log.e("GoalLoad", "Error loading goals", error.toException());
            }
        });
    }

    private void loadMonthlyIncome(DatabaseReference userRef) {
        userRef.child("incomes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Income income = snap.getValue(Income.class);
                    if (income == null || income.getExpenseDate() == null) continue;

                    try {
                        String[] parts = income.getIncomeDate().split("-");
                        int entryMonth = Integer.parseInt(parts[1]) - 1;

                        if (entryMonth == monthIndex) {
                            combinedTransactions.add(income);
                            totalInc += income.getAmount();
                        }
                    } catch (Exception ignored) {}
                }

                totalExpenses.setText("Expenses: $" + String.format("%.2f", totalExp));
                totalIncome.setText("Income: $" + String.format("%.2f", totalInc));

                transactionAdapter.notifyDataSetChanged(); // Update RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getMonthName(int index) {
        return new DateFormatSymbols().getMonths()[index];
    }
}