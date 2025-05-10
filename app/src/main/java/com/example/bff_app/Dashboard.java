package com.example.bff_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Dashboard extends AppCompatActivity {
    FirebaseAuth auth;
    Button logOut;
    TextView manageIncomeText, leftToSpendText, remainingAmountText, manageExpenseText;
    TextView manageText,expenseAmountDisplay, incomeAmountDisplay, nameText;
    FirebaseUser user;
    DatabaseReference userRef;
    ProgressBar expensesProgressBar;
    HorizontalBarChart barChart;
    AppCompatButton dashboardBtn, summaryBtn, goalsBtn, moreBtn;
    double monthlyInc;
    double totalIncome = 0.0;
    double totalExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //android activity func
        super.onCreate(savedInstanceState); //related to AppCompatActivity
        EdgeToEdge.enable(this);
        SharedPreferences sharedPreferences = getSharedPreferences("SettingsPrefs", MODE_PRIVATE);
        boolean darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false);
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setContentView(R.layout.activity_dashboard); //sets the layout for activity using the xml

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance(); //initializes firebase authentication
        //following gets references to related parts from xml layout
        logOut = findViewById(R.id.logout);
        nameText = findViewById(R.id.nameText);
        user = FirebaseAuth.getInstance().getCurrentUser();
        expensesProgressBar = findViewById(R.id.expensesProgressBar);
        leftToSpendText = findViewById(R.id.leftToSpendText);
        remainingAmountText = findViewById(R.id.remainingAmountText);
        barChart = findViewById(R.id.barCht);
        dashboardBtn = findViewById(R.id.dashboard_btn);
        summaryBtn = findViewById(R.id.summary_btn);
        goalsBtn = findViewById(R.id.goals_btn);
        moreBtn = findViewById(R.id.more_btn);
        manageIncomeText = findViewById(R.id.manageIncomeText);
        manageExpenseText = findViewById(R.id.manageExpenseText);
        expenseAmountDisplay = findViewById(R.id.expenseAmountDisplay);
        incomeAmountDisplay = findViewById(R.id.incomeAmountDisplay);
        manageText = findViewById(R.id.manageText);

        //this if statement ensures user is signed in, if they arent they are directed to the login
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish(); //closes current activity as login is launched
        }
        //if they Are logged in, following happens:
        else {
            String userId = user.getUid(); //retrieves uid of user

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n") // Enforces setText on line 91 to avoid warnings
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User userData = snapshot.getValue(User.class);
                            if (userData != null) {
                                nameText.setText("Welcome, " + userData.name + "!"); // Displays Welcome (username)!
                                userRef = FirebaseDatabase.getInstance().getReference("Users")
                                        .child(user.getUid())
                                        .child("expenses"); // Accesses expenses stored in database
                                monthlyInc = userData.monthlyBudget;// Accesses monthlyBudget from database
                                updateDashboard(userId);
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        /*mutable string for expenses, allows for easy modifications
                                        no need to make a new string object*/
                                        StringBuilder expensesBuilder = new StringBuilder();
                                        /*Goes through each child and adds them to snapshot build
                                         to display later in our graphs*/
                                        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                                            Expense expense = expenseSnapshot.getValue(Expense.class);
                                            // Adds all expenses to a string with a new line for each expense
                                            if (expense != null) {
                                                expensesBuilder.append(expense.getCategory())
                                                        .append(": $")
                                                        .append(String.format("%.2f", expense.getAmount()))
                                                        .append(" (")
                                                        .append(expense.getExpenseDate())
                                                        .append(")\n");
                                            }
                                        }

                                        if (expensesBuilder.length() == 0) {
                                            expensesBuilder.append("No expenses found.");
                                        }
                                        loadBarData(snapshot);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("DB_ERROR", "Failed to read user expenses", error.toException());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DB_ERROR", "Failed to read user data", error.toException());
                        }
                    });
        }

        //Logout button onClick event handler
        logOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
        //addIncome button
        manageIncomeText.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), EditIncomes.class);
            startActivity(intent);
            finish();
        });
        //addExpense button
        manageExpenseText.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), EditExpenses.class);
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
    private void updateDashboard(String userId) { //new
        DatabaseReference incomeRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("incomes");
        DatabaseReference expenseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("expenses");

        // Calculate total income
        incomeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalIncome = 0.0;
                int currentYear = LocalDate.now().getYear();
                int currentMonth = LocalDate.now().getMonthValue();

                for (DataSnapshot incomeSnapshot : snapshot.getChildren()) {
                    Income income = incomeSnapshot.getValue(Income.class);
                    if (income != null) {
                        try {
                            int incomeMonth = Integer.parseInt(income.getIncomeDate().substring(5, 7));
                            int incomeYear = Integer.parseInt(income.getIncomeDate().substring(0, 4));
                            if (incomeYear == currentYear && incomeMonth == currentMonth) {
                                totalIncome += income.getAmount();
                            }
                        } catch (Exception e) {
                            Log.e("PARSING_ERROR", "Invalid date format in income: " + income.getIncomeDate());
                        }
                    }
                }

                // Update income display
                incomeAmountDisplay.setText(String.format("$%.2f", totalIncome));
                updateRemainingBudget(totalIncome,totalExpenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", "Failed to read income data", error.toException());
            }
        });

        // Calculate total expenses
        expenseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int currentYear = LocalDate.now().getYear();
                int currentMonth = LocalDate.now().getMonthValue();
                totalExpenses = 0.0;
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    Expense expense = expenseSnapshot.getValue(Expense.class);
                    if (expense != null) {
                        try {
                            int expenseMonth = Integer.parseInt(expense.getExpenseDate().substring(5, 7));
                            int expenseYear = Integer.parseInt(expense.getExpenseDate().substring(0, 4));
                            if (expense.getIsMonthly() || (expenseYear == currentYear && expenseMonth == currentMonth)) {
                                totalExpenses += expense.getAmount();
                            }
                        } catch (Exception e) {
                            Log.e("PARSING_ERROR", "Invalid date format in expense: " + expense.getExpenseDate());
                        }
                    }
                }

                // Update expense display
                expenseAmountDisplay.setText(String.format("$%.2f", totalExpenses));
                updateRemainingBudget(totalIncome, totalExpenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DB_ERROR", "Failed to read expense data", error.toException());
            }
        });
    }

    // Method to update remaining budget text and progress bar
    private void updateRemainingBudget(double monthlyIncome, double totalExpenses) {
        double remainingBudget = monthlyIncome - totalExpenses;

        remainingAmountText.setText(String.format("$%.2f out of $%.2f", remainingBudget, monthlyIncome));

        int progress = (int) ((remainingBudget / monthlyIncome) * 100);
        progress = remainingBudget <= 0 ? 100 : Math.max(progress, 0);
        expensesProgressBar.setProgress(progress);

        if (remainingBudget <= 0) {
            expensesProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        } else {
            expensesProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.forestGreen)));
            expensesProgressBar.setProgressBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.soft)));
        }

        manageText.setText("Remaining Budget: $" + String.format("%.2f", remainingBudget));
    }

    //Print out bar chart
    private void loadBarData(DataSnapshot snapshot) {
        Map<String, Float> categoryTotals = new TreeMap<>();

        for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
            Expense expense = expenseSnapshot.getValue(Expense.class);
            if (expense != null) {
                int currentYear = LocalDate.now().getYear(); // Gets year as int, format ex: 2025
                int currentMonth = LocalDate.now().getMonthValue(); //Gets month as int
                String category = expense.getCategory();
                float amount = (float) expense.getAmount();
                int expenseMonth = Integer.parseInt(expense.getExpenseDate().substring(5,7));
                int expYear = Integer.parseInt(expense.getExpenseDate().substring(0,4));
                // This if-statement checks if the expense is monthly, if it is, it will be loaded into bar chart
                if (expense.getIsMonthly() || (currentYear == expYear && expenseMonth == currentMonth)) {
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
                }
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Create the BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, "Expenses");

        // Replace single color with dynamic color assignment
        ArrayList<Integer> colors = new ArrayList<>();
        Map<String, Integer> categoryColorMap = new HashMap<>();

        int[] colorPalette = new int[]{
                ContextCompat.getColor(this, R.color.chartColor1),
                ContextCompat.getColor(this, R.color.chartColor2),
                ContextCompat.getColor(this, R.color.chartColor3),
                ContextCompat.getColor(this, R.color.chartColor4),
                ContextCompat.getColor(this, R.color.chartColor5),
                ContextCompat.getColor(this, R.color.chartColor6),
                ContextCompat.getColor(this, R.color.chartColor7),
                ContextCompat.getColor(this, R.color.chartColor8)
        };

        int colorIndex = 0;
        for (String category : labels) {
            if (!categoryColorMap.containsKey(category)) {
                categoryColorMap.put(category, colorPalette[colorIndex % colorPalette.length]);
                colorIndex++;
            }
            colors.add(categoryColorMap.get(category));
        }

        dataSet.setColors(ColorTemplate.LIBERTY_COLORS);
        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        //Axis handling
        XAxis xAxis = barChart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis leftAxis = barChart.getAxisLeft();
        YAxis rightAxis = barChart.getAxisRight();
        leftAxis.setGranularity(1f);
        rightAxis.setEnabled(false); // Optional
        //Finalize chart
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setText(" ");
        barChart.animateY(1000);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }
}