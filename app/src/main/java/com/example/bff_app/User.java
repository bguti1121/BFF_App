package com.example.bff_app;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String email;
    public String name;
    public long registrationTimestamp;
    public double monthlyBudget;
    public double totalSaved;
    public Map<String, Object> spendingCategories;

    public User(){}

    public User(String email, String name){
        this.email = email;
        this.name = name;
        this.registrationTimestamp = System.currentTimeMillis();

        //Default values
        this.monthlyBudget = 0.0;
        this.totalSaved = 0.0;
        this.spendingCategories = new HashMap<>();
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
