package com.example.bff_app;

public interface TransactionItem {
    double getAmount();
    String getCategory();
    String getExpenseDate(); // same getter name used in Income
    boolean isIncome(); // helps the adapter distinguish types
}
