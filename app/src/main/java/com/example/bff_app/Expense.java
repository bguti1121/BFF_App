package com.example.bff_app;

public class Expense implements TransactionItem {
    public double expenseAmount; //amount of the expense
    public double amount;
    public String category;
    public String expenseCategory; //category of the expense
    public String expenseDate; //string for now, just to be used for display
    public boolean isMonthly;

    public Expense(){}

    public Expense(double expenseAmount, String expenseCategory, String expenseDate, boolean isMonthly){}

    public Expense(double expenseAmount, String expenseCategory, String expenseDate){
        this.expenseAmount = expenseAmount;
        this.expenseCategory = expenseCategory;
        this.expenseDate = expenseDate;
    }

    public double getAmount(){ //getter for amount
        if(expenseAmount !=0) {
            return expenseAmount;
        } else {
            return amount;
        }
    }

    public String getCategory(){ //getter for category
        if(expenseAmount !=0) {
            return expenseCategory;
        } else {
            return category;
        }
    }

    public String getExpenseDate(){  //getter for expense date
        return expenseDate;
    }

    @Override
    public boolean isIncome() {
        return false;
    }

    // Getter method for isMonthly
    public boolean getIsMonthly(){
        return isMonthly;
    }

}
