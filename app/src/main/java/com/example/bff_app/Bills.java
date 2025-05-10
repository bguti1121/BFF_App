package com.example.bff_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.graphics.PorterDuff;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Bills extends AppCompatActivity {
    private PieChart pieChart;
    private RecyclerView recyclerView;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private double totalAmount = 0;

    private final int[] chartColors = {
            R.color.beige, R.color.pink, R.color.lavender, R.color.lightBlue,
            R.color.peach, R.color.lighterBlue, R.color.lightGreen, R.color.butter
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        // back button
        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> finish());
        // Pie chart and expenses RecyclerView
        pieChart     = findViewById(R.id.pieChart);
        recyclerView = findViewById(R.id.expensesLogRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ExpenseAdapter());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish(); // not logged in
            return;
        }
        // Reference to Firebase Database
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("Users")
                .child(user.getUid())
                .child("expenses");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseList.clear();
                totalAmount = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar now = Calendar.getInstance();
                int currentMonth = now.get(Calendar.MONTH);
                int currentYear = now.get(Calendar.YEAR);

                for (DataSnapshot child : snapshot.getChildren()) {
                    Expense e = child.getValue(Expense.class);
                    if (e != null && e.getExpenseDate() != null) {
                        try {
                            Date date = sdf.parse(e.getExpenseDate());
                            if (date != null) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);

                                int expenseMonth = cal.get(Calendar.MONTH);
                                int expenseYear = cal.get(Calendar.YEAR);

                                if ((expenseMonth == currentMonth && expenseYear == currentYear) || e.getIsMonthly()) {
                                    expenseList.add(e);
                                    totalAmount += e.getAmount();
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                // Updates pie chart
                updatePieChart();
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle errors if needed?
            }
        });
    }

    private void updatePieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        // Goes through expenseList and loads them into pie chart data
        for (int i = 0; i < expenseList.size(); i++) {
            Expense e = expenseList.get(i);
            entries.add(new PieEntry((float) e.getAmount(), e.getCategory()));
            int c = ContextCompat.getColor(this, chartColors[i % chartColors.length]);
            colors.add(c);
        }
        // Sets up pie chart
        PieDataSet set = new PieDataSet(entries, "Expenses");
        set.setColors(colors);
        pieChart.setData(new PieData(set));
        pieChart.invalidate(); // refresh
    }
    // class to adapt data to the UI
    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {
        class VH extends RecyclerView.ViewHolder {
            TextView category, amount, percentText, percentOutline;
            ProgressBar bar;

            VH(View v) {
                super(v);
                category = v.findViewById(R.id.expenseCategory);
                amount = v.findViewById(R.id.expenseAmount);
                bar = v.findViewById(R.id.progressBar);
                percentText = v.findViewById(R.id.progressPercentage);
                percentOutline = v.findViewById(R.id.progressOutline);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(Bills.this)
                    .inflate(R.layout.expense_display_row, parent, false);
            return new VH(v);
        }

        // progress bar and percentage
        @Override
        public void onBindViewHolder(@NonNull VH holder, int pos) {
            Expense e = expenseList.get(pos);
            holder.category.setText(e.getCategory());
            holder.amount.setText(String.format("$%.2f", e.getAmount()));

            int pct = totalAmount > 0 ? (int) ((e.getAmount() / totalAmount) * 100) : 0;

            holder.bar.setMax(100);
            holder.bar.setProgress(pct);
            holder.percentText.setText(pct + "%");
            holder.percentOutline.setText(pct + "%");
            int textColor = ContextCompat.getColor(Bills.this, R.color.percentageText);
            holder.percentText.setTextColor(textColor);
            holder.percentOutline.setTextColor(textColor);

            int color = ContextCompat.getColor(Bills.this,
                    chartColors[pos % chartColors.length]);
            holder.bar.getProgressDrawable()
                    .setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}