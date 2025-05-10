package com.example.bff_app;

public class Income implements TransactionItem {
    public double incomeAmount; //amount of the expense
    public double amount; // extra categories to get chart to print
    public String category; // extra categories to get chart to print
    public String incomeCategory; //category of the expense
    public String incomeDate; //string for now, just to be used for display

    public Income(){}

    public Income(double amount, String incomeCategory, String incomeDate){
        this.incomeAmount = amount;
        this.incomeCategory = incomeCategory;
        this.incomeDate = incomeDate;
    }

    public double getAmount(){ //getter for amount
        if(incomeAmount !=0) {
            return incomeAmount;
        } else {
            return amount;
        }
    }

    public String getCategory(){ //getter for category
        if(incomeAmount !=0) {
            return incomeCategory;
        } else {
            return category;
        }
    }

    public String getExpenseDate(){  //getter for expense date
        return incomeDate;
    }

    @Override
    public boolean isIncome() {
        return true;
    }

}