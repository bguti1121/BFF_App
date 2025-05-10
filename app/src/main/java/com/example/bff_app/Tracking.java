package com.example.bff_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Tracking extends AppCompatActivity {
    private BarChart barChart;
    private ImageButton backButton;
    private RelativeLayout popupContainer;
    private TextView categoryText;
    private TextView dateText;
    private RecyclerView recyclerView;

    // Data lists
    private ArrayList<Expense> trackingList = new ArrayList<>();
    private ExpenseManager expenseManager = new ExpenseManager();
    private IncomeManager incomeManager = new IncomeManager();
    private List<TransactionItem> allTransactions = new ArrayList<>();

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
        setContentView(R.layout.activity_tracking);

        // Gets and sorts all the transactions but will be overwritten by Firebase data
        List<Expense> expenseList = expenseManager.getAllExpenses();
        List<Income> incomeList = incomeManager.getAllIncomes();
        allTransactions.addAll(expenseList);
        allTransactions.addAll(incomeList);
        allTransactions.sort(Comparator.comparing(TransactionItem::getExpenseDate));

        // Initialize UI components
        barChart = findViewById(R.id.barChart);
        recyclerView = findViewById(R.id.trackingLogRecyclerView);
        TransactionAdapter adapter = new TransactionAdapter(allTransactions);
        backButton = findViewById(R.id.backButton);
        popupContainer = findViewById(R.id.popup_container);
        categoryText = findViewById(R.id.category);
        dateText = findViewById(R.id.popup_date);

        // Sets up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Hides the popup initially
        popupContainer.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(Tracking.this, Summary.class));
            finish();
        });

        // Handles bar chart clicks
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null || h == null || e.getY() == 0) {
                    popupContainer.setVisibility(View.GONE); // Hides the popup if no bar clicked
                    return;
                }

                int dataSetIndex = h.getDataSetIndex();  // 0 = income, 1 = expense
                int xIndex = (int) e.getX();

                // Checks if xIndex is within bounds
                if (xIndex < 0 || xIndex >= categories.size()) {
                    popupContainer.setVisibility(View.GONE); // Hide the popup if invalid xIndex
                    return;
                }

                String category = categories.get(xIndex);
                String date = "";

                // Shows the popup if the user clicks on Income Bar
                if (dataSetIndex == 0) { // Income
                    for (Income i : incomeManager.getAllIncomes()) {
                        if (i.getCategory().equals(category)) {
                            date = i.getExpenseDate();
                            break;
                        }
                    }
                    showPopup(category, date, true);

                }
                // Shows the popup if the user clicks on Expense Bar
                else if (dataSetIndex == 1) { // Expense
                    for (Expense ex : expenseManager.getAllExpenses()) {
                        if (ex.getCategory().equals(category)) {
                            date = ex.getExpenseDate();
                            break;
                        }
                    }
                    showPopup(category, date, false);
                }
            }

            @Override
            // Hides the popup when nothing is selected (no bar clicked)
            public void onNothingSelected() {
                popupContainer.setVisibility(View.GONE);
            }
        });

        loadDataFromFirebase();
    }
    private void loadDataFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Loads expenses from database
        rootRef.child("expenses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot expSnap) {
                // Clears previously stored data to avoid duplicates
                expenseManager.getAllExpenses().clear();

                // Loops through each expense entry and add to expense list if its part of this month
                for (DataSnapshot snap : expSnap.getChildren()) {
                    Expense e = snap.getValue(Expense.class);
                    if (e != null) {
                        if (isInCurrentMonth(e.getExpenseDate())) {
                            expenseManager.getAllExpenses().add(e);
                        } else if (e.getIsMonthly() && isRecurringInCurrentMonth(e.getExpenseDate())) {
                            // Create a copy with the current month date for display purposes
                            Expense recurringCopy = new Expense(
                                    e.getAmount(),
                                    e.getCategory(),
                                    getCurrentMonthDate(),  // helper method to override with current date
                                    true // isMonthly
                            );
                            expenseManager.getAllExpenses().add(recurringCopy);
                        }
                    }
                }

                // Loads incomes from database
                rootRef.child("incomes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot incSnap) {
                        // Clears previously stored data
                        incomeManager.getAllIncomes().clear();

                        // Loops through each income entry and add to income list if its part of this month
                        for (DataSnapshot snap2 : incSnap.getChildren()) {
                            Income i = snap2.getValue(Income.class);
                            if (i != null && isInCurrentMonth(i.getIncomeDate())) {
                                incomeManager.getAllIncomes().add(i);
                            }
                        }

                        // Combines the expenses and incomes into one list (transaction list) so they could both show together
                        allTransactions.clear();
                        allTransactions.addAll(expenseManager.getAllExpenses());
                        allTransactions.addAll(incomeManager.getAllIncomes());

                        // Notify adapter to display the transaction list
                        TransactionAdapter adapter = new TransactionAdapter(allTransactions);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        // Update categories and chart
                        updateCategoriesList();
                        sortByDate(expenseManager.getAllExpenses());
                        sortByDateIncome(incomeManager.getAllIncomes());
                        updateBarChart();
                    }

                    // Error handling if income load fails
                    @Override
                    public void onCancelled(DatabaseError e) {
                        Log.e("Tracking", "Income load failed: " + e.getMessage());
                    }
                });
            }

            // Error handling if expense load fails
            @Override
            public void onCancelled(DatabaseError e) {
                Log.e("Tracking", "Expense load failed: " + e.getMessage());
            }
        });
    }

    // Checks if an old recurring expense should appear this month
    private boolean isRecurringInCurrentMonth(String originalDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(originalDate);
            if (date == null) return false;

            // Get current month/year
            Date now = new Date();
            SimpleDateFormat ym = new SimpleDateFormat("yyyy-MM", Locale.US);
            return ym.format(now).equals(ym.format(new Date()));
        } catch (ParseException e) {
            return false;
        }
    }

    // Returns today's date in yyyy-MM-dd format for display
    private String getCurrentMonthDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    private void updateCategoriesList() {

        // Initializes a LinkedHashSet to store all the categories
        Set<String> allCategories = new LinkedHashSet<>();


        for (Income i : incomeManager.getAllIncomes()) allCategories.add(i.getCategory());
        for (Expense e : expenseManager.getAllExpenses()) allCategories.add(e.getCategory());

        categories = new ArrayList<>(allCategories);

        // For display purposes
        int groupCount = Math.max(categories.size(), 5);
        while (categories.size() < groupCount) categories.add(""); // Padding if less than 5 categories
    }

    // List of categories
    private List<String> categories;

    // Helper method to format date (eg: May 7, 2025)
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = inputFormat.parse(dateStr);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr; // In case formatting fails
        }
    }

    private void updateBarChart() {

        // Initialize a LinkedHashSet to store all the categories
        Set<String> allCategories = new LinkedHashSet<>();

        for (Income i : incomeManager.getAllIncomes()) allCategories.add(i.getCategory());
        for (Expense e : expenseManager.getAllExpenses()) allCategories.add(e.getCategory());

        categories = new ArrayList<>(allCategories);

        // For display
        int groupCount = Math.max(categories.size(), 5);
        while (categories.size() < groupCount) categories.add("");

        // Creates two maps to store the category to the amount for both incomes and expenses
        Map<String, Float> incMap = new HashMap<>(), expMap = new HashMap<>();

        for (Income i : incomeManager.getAllIncomes())
            incMap.put(i.getCategory(), (float) i.getAmount());
        for (Expense e : expenseManager.getAllExpenses())
            expMap.put(e.getCategory(), (float) -e.getAmount());

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();

        // Gets the income and expense values for each category and creates BarEntries for them
        for (int i = 0; i < groupCount; i++) {
            String category = categories.get(i);
            float inc = incMap.getOrDefault(category, 0f);
            float exp = expMap.getOrDefault(category, 0f);
            incomeEntries.add(new BarEntry(i, inc));
            expenseEntries.add(new BarEntry(i, exp));
        }

        // Creates BarDataSets for both income and expense entries
        BarDataSet incSet = new BarDataSet(incomeEntries, "Income");
        BarDataSet expSet = new BarDataSet(expenseEntries, "Expenses");

        // Sets colors for the bars and hides values
        incSet.setColor(ContextCompat.getColor(this, R.color.income_bar_color));
        expSet.setColor(ContextCompat.getColor(this, R.color.expense_bar_color));
        incSet.setDrawValues(false);
        expSet.setDrawValues(false);

        // Spacing
        float barWidth = 0.50f; // Width of each bar
        float barSpace = 0.05f; // Space between bars

        // This formula makes sure there's enough space between bar groups and keeps the chart layout balanced.
        // Without it, tapping on the last category will not trigger the expected popup because of insufficient space.
        float groupSpace = 1f - 2 * (barWidth + barSpace); // Space between groups

        // Combines income and expense data into a BarData object
        BarData data = new BarData(incSet, expSet);

        // Sets bar width and applies data to the chart
        data.setBarWidth(barWidth);
        barChart.setData(data);
        barChart.setExtraOffsets(10, 10, 10, 30); // in case a category is very small

        // X-Axis setup
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        // Starting X position for bar groups
        float startX = 0f;

        // Calculates the total width of a group
        float groupWidth = data.getGroupWidth(groupSpace, barSpace);

        // Sets the end of the X-Axis based on group amounts
        float endX = startX + groupWidth * groupCount;

        xAxis.setAxisMinimum(startX);
        xAxis.setAxisMaximum(endX);

        // Apply bar grouping on chart
        barChart.groupBars(startX, groupSpace, barSpace);

        barChart.setVisibleXRangeMaximum(5f);
        barChart.moveViewToX(startX);

        // Y-Axis setup
        float maxInc = maxEntryY(incomeEntries);
        float minExp = minEntryY(expenseEntries);
        float topPad = Math.max(maxInc * 0.1f, 5f);
        float botPad = Math.max(Math.abs(minExp) * 0.1f, 5f);
        barChart.getAxisLeft().setAxisMaximum(maxInc + topPad);
        barChart.getAxisLeft().setAxisMinimum(minExp - botPad);

        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawZeroLine(true);
        barChart.getAxisLeft().setZeroLineColor(Color.BLACK); // Sets the color of the zero line
        barChart.getAxisLeft().setZeroLineWidth(1f); // Sets the width of the zero line
        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setDragEnabled(true);
        barChart.animateY(1000, Easing.EaseInOutQuad);
        barChart.invalidate();
    }

    // Used for Y-Axis setup, returns largest Y value from Bar Entry objects (income)
    private float maxEntryY(List<BarEntry> list) {
        float m = Float.MIN_VALUE;
        for (BarEntry e : list) m = Math.max(m, e.getY());
        return m < 0 ? 0 : m;
    }

    // Used for Y-Axis setup, returns smallest Y value from Bar Entry objects (expense)
    private float minEntryY(List<BarEntry> list) {
        float m = Float.MAX_VALUE;
        for (BarEntry e : list) m = Math.min(m, e.getY());
        return m > 0 ? 0 : m;
    }

    // Displays a popup with the category name and formatted date,
    // and sets the background depending on whether it's income or expense
    private void showPopup(String category, String date, boolean isIncome) {
        categoryText.setText(category);
        dateText.setText(formatDate(date));
        popupContainer.setBackgroundResource(
                isIncome ? R.drawable.income_popup : R.drawable.expense_popup
        );
        popupContainer.setVisibility(View.VISIBLE);
    }

    // Sorts the list of expenses by date
    private void sortByDate(List<Expense> list) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.US);
        Collections.sort(list, (a, b) -> {
            try {
                return fmt.parse(a.getExpenseDate())
                        .compareTo(fmt.parse(b.getExpenseDate()));
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    // Sorts the list of incomes by date
    private void sortByDateIncome(List<Income> list) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d", Locale.US);
        Collections.sort(list, (a, b) -> {
            try {
                return fmt.parse(a.getExpenseDate())
                        .compareTo(fmt.parse(b.getExpenseDate()));
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    // Adapter for RecyclerView to display transactions
    private class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.VH> {

        // ViewHolder holds references to the TextViews in each row
        class VH extends RecyclerView.ViewHolder {
            TextView category, amount, date;

            VH(View v) {
                super(v);
                category = v.findViewById(R.id.trackingCategory);
                amount = v.findViewById(R.id.trackingAmount);
                date = v.findViewById(R.id.trackingDate);
            }
        }

        // Inflates the row layout and returns a ViewHolder
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(Tracking.this)
                    .inflate(R.layout.tracking_log_row, parent, false);
            return new VH(v);
        }

        // Binds data to the ViewHolder (based on expense or income)
        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {

            Object entry = trackingList.get(pos);

            if (entry instanceof Expense) {
                Expense expense = (Expense) entry;
                holder.category.setText(expense.getCategory());
                holder.amount.setText(String.format("$%.2f", expense.getAmount()));
                holder.date.setText(expense.getExpenseDate());
            } else if (entry instanceof Income) {
                Income income = (Income) entry;
                holder.category.setText(income.getCategory());
                holder.amount.setText(String.format("$%.2f", income.getAmount()));
                holder.date.setText(income.getExpenseDate());
            }
        }

        // Returns the number of items in tracking list
        @Override
        public int getItemCount() {
            return trackingList.size();
        }
    }
    private boolean isInCurrentMonth(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            if (date == null) return false;

            Date currentDate = new Date();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM-yyyy", Locale.US);

            return monthFormat.format(date).equals(monthFormat.format(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}