package com.example.kuetapp;

public class UserData {
    private String name, email, batch, dept, roll, imageUrl;


    public UserData(String name, String email, String batch, String dept, String roll, String imageUrl) {
        this.name = name;
        this.email = email;
        this.batch = batch;
        this.dept = dept;
        this.roll = roll;
        this.imageUrl = imageUrl;
    }

    public UserData() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
