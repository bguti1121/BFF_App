package com.example.bff_app;
import java.util.ArrayList;
import java.util.List;

public class IncomeManager{
    private static List<Income> allIncomes = new ArrayList<>();

    public static void addIncome(double incomeAmount, String incomeCategory, String incomeDate){
        Income newIncome = new Income(incomeAmount, incomeCategory, incomeDate);
        allIncomes.add(newIncome);
    }

    public int numOfIncomes(){
        return allIncomes.size();
    }

    public List<Income> getAllIncomes(){ //getter method for all expenses
        return allIncomes;
    }

}