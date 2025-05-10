package com.example.bff_app;
import java.util.ArrayList;
import java.util.List;
public class ExpenseManager {
        public List<Expense> allExpenses = new ArrayList<>();

        public void addExpense(double expenseAmount, String expenseCategory, String expenseDate, boolean isMonthly){
            Expense newExpense = new Expense(expenseAmount, expenseCategory, expenseDate, isMonthly);
            allExpenses.add(newExpense);
        }

        public int numOfExpenses(){
            return allExpenses.size();
        }

        public List<Expense> getAllExpenses(){ //getter method for all expenses
            return allExpenses;
        }

}