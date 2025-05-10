package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YearlySummary extends AppCompatActivity {

    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;
    ImageButton backButton;
    private BarChart yearlyBarChart;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_yearly_summary);

        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);
        backButton = findViewById(R.id.backButton);

        yearlyBarChart = findViewById(R.id.yearlyBarChart);
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(mAuth.getCurrentUser().getUid());

        loadYearlyData();

        //Back button onClick event handler
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Extra.class);
            startActivity(intent);
            finish();
        });

        //Dashboard button onClick event handler
        dashboardBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
            finish();
        });

        //Summary button onClick event handler
        summaryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Summary.class);
            startActivity(intent);
            finish();
        });

        //Goals button onClick event handler
        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SavingGoalsActivity.class);
            startActivity(intent);
            finish();
        });

        //More button onClick event handler
        moreBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Extra.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadYearlyData() {
        float[] monthlyExpenses = new float[12];
        float[] monthlyIncomes = new float[12];
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        // Provides reference to database User -> expenses, then loads them
        userRef.child("expenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot expenseSnap : snapshot.getChildren()) {
                    String dateStr = expenseSnap.child("expenseDate").getValue(String.class);
                    Double amount = expenseSnap.child("amount").getValue(Double.class);
                    Boolean isMonthly = expenseSnap.child("isMonthly").getValue(Boolean.class);
                    // Error Checking
                    Log.d("FirebaseCheck", "Expense snapshot: " + snapshot.toString());

                    if (dateStr != null && amount != null) {
                        try {
                            String[] parts = dateStr.split("-");
                            int year = Integer.parseInt(parts[0]);
                            int monthIndex = Integer.parseInt(parts[1]) - 1; // 0-based

                            if (isMonthly != null && isMonthly) {
                                // Recurring monthly expense: add to all months from start month onward
                                if (year == currentYear) {
                                    for (int i = monthIndex; i < 12; i++) {
                                        monthlyExpenses[i] += amount;
                                    }
                                } else if (year < currentYear) {
                                    for (int i = 0; i < 12; i++) {
                                        monthlyExpenses[i] += amount;
                                    }
                                }
                                // If future recurring (year > current), skip
                            } else {
                                // One-time expense for this month/year only
                                if (year == currentYear) {
                                    monthlyExpenses[monthIndex] += amount;
                                }
                            }
                        } catch (Exception e) {
                            Log.e("YearlySummary", "Expense date parse error: " + e.getMessage());
                        }
                    }
                }

                // Provides reference to database User -> incomes, then loads them
                userRef.child("incomes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot incomeSnap : snapshot.getChildren()) {
                            String dateStr = incomeSnap.child("incomeDate").getValue(String.class);
                            Double amount = incomeSnap.child("amount").getValue(Double.class);
                            // Error checking
                            Log.d("FirebaseCheck", "Income snapshot: " + snapshot.toString());

                            if (dateStr != null && amount != null) {
                                try {
                                    String[] parts = dateStr.split("-");
                                    int year = Integer.parseInt(parts[0]);
                                    int monthIndex = Integer.parseInt(parts[1]) - 1;
                                    if (year == currentYear) {
                                        monthlyIncomes[monthIndex] += amount;
                                    }
                                } catch (Exception e) {
                                    Log.e("YearlySummary", "Income date parse error: " + e.getMessage());
                                }
                            }
                        }
                        // Error Checking
                        Log.d("FirebaseCheck", "monthlyExpenses: " + Arrays.toString(monthlyExpenses));
                        Log.d("FirebaseCheck", "monthlyIncomes: " + Arrays.toString(monthlyIncomes));

                        // Show the chart once both incomes and expenses are loaded
                        showBarChart(monthlyExpenses, monthlyIncomes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("YearlySummary", "Failed to read incomes: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("YearlySummary", "Failed to read expenses: " + error.getMessage());
            }
        });
    }

    private void showBarChart(float[] expenses, float[] incomes) {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        int textColor = getResources().getColor(R.color.black, getTheme());
        int valueTextColor = getResources().getColor(R.color.black, getTheme());

        for (int i = 0; i < 12; i++) {
            incomeEntries.add(new BarEntry(i, incomes[i]));
            expenseEntries.add(new BarEntry(i, expenses[i]));
        }


        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.GREEN);
        incomeDataSet.setValueTextColor(valueTextColor);

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.RED);
        expenseDataSet.setValueTextColor(valueTextColor);

        BarData data = new BarData(incomeDataSet, expenseDataSet);
        float groupSpace = 0.2f;
        float barSpace = 0.02f; // space between bars
        float barWidth = 0.38f;

        data.setBarWidth(barWidth);
        yearlyBarChart.setData(data);
        yearlyBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(months));
        yearlyBarChart.getXAxis().setGranularity(1f);
        yearlyBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        yearlyBarChart.getXAxis().setCenterAxisLabels(true);
        yearlyBarChart.getXAxis().setLabelCount(12);
        // Must be called before groupBars
        yearlyBarChart.getXAxis().setAxisMinimum(0f);
        yearlyBarChart.getXAxis().setAxisMaximum(0f + data.getGroupWidth(groupSpace, barSpace) * 12);

        yearlyBarChart.groupBars(0f, groupSpace, barSpace);

        yearlyBarChart.getAxisRight().setEnabled(false);
        yearlyBarChart.getDescription().setEnabled(false);
        yearlyBarChart.setTouchEnabled(true);
        yearlyBarChart.setDragEnabled(true);
        yearlyBarChart.setScaleEnabled(true);

        yearlyBarChart.getXAxis().setTextColor(textColor);
        yearlyBarChart.getAxisLeft().setTextColor(textColor);
        yearlyBarChart.getLegend().setTextColor(textColor);
        yearlyBarChart.invalidate();

        yearlyBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int monthIndex = (int) e.getX();
                Intent intent = new Intent(YearlySummary.this, MonthlyDetailActivity.class);
                intent.putExtra("monthIndex", monthIndex); // 0 for Jan, 11 for Dec
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {}
        });
    }
}