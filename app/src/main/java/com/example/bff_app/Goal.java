package com.example.bff_app;
import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Goal implements Serializable {
    private String title;
    private double amountGoal;
    private double amountSaved;
    private String targetDate;

    private String key;//Firebase document key

    public Goal(){}

    public Goal(String title, double amountGoal, double amountSaved) {
        this.title = title;
        this.amountGoal = amountGoal;
        this.amountSaved = amountSaved;
    }

    public Goal(String title, double amountGoal, double amountSaved, String targetDate) {
        this.title = title;
        this.amountGoal = amountGoal;
        this.amountSaved = amountSaved;
        this.targetDate = targetDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
    public void setTitle(String setTitle){
        this.title = setTitle;
    }
    public void setAmountGoal(double setAmount){
        this.amountGoal = setAmount;
    }
    public void setAmountSaved(double setAmount){
        this.amountSaved = setAmount;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

}
