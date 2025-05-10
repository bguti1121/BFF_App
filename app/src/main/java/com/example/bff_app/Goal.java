package com.example.bff_app;
import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Goal implements Serializable {
    private String title;
    private double amountGoal;
    private double amountSaved;
    private String key;//Firebase document key
    public String createdDate;
    public String endDate;

    public Goal(){}

    public Goal(String title, double goalAmount, double saved, String key, String description, SimpleDateFormat sdf, String date) {
    }

    public Goal(String title, double amountGoal, double amountSaved, String key,String createdDate, String endDate) {
        this.title = title;
        this.amountGoal = amountGoal;
        this.amountSaved = amountSaved;
        this.key = key;
        this.createdDate = createdDate;
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }
    public double getAmountGoal(){
        return amountGoal;
    }
    public double getAmountSaved() {
        return amountSaved;
    }
    public String getKey(){
        return key;
    }
    public void setTitle(String setTitle){
        this.title = setTitle;
    }
    public void setAmountGoal(double setAmount){
        this.amountGoal = setAmount;
    }
    public void setAmountSaved(double setAmount){
        this.amountSaved = setAmount;
    }

    public void setKey(String key){
        this.key = key;
    }

    public void setTargetDate(String newDateStr) {
    }
}