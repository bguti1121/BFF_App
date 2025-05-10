package com.example.bff_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private final List<TransactionItem> transactions;

    public TransactionAdapter(List<TransactionItem> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        TransactionItem item = transactions.get(position);
        holder.category.setText(item.getCategory());
        holder.amount.setText(String.format("$%.2f", item.getAmount()));
        holder.date.setText(item.getExpenseDate());

        // Color logic
        Context context = holder.itemView.getContext();

        if (item instanceof Expense) {
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.transaction_expense));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_expense));
        } else if (item instanceof Income) {
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.transaction_income));
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_income));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, amount, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.transactionCategory);
            amount = itemView.findViewById(R.id.transactionAmount);
            date = itemView.findViewById(R.id.transactionDate);
        }
    }
}