package com.example.budgettracker;

public class UserDeets {
    public String birth, gender, mobile;


    public UserDeets(){};
    public UserDeets(String textBirth, String textGender, String textMobile){
        this.birth = textBirth;
        this.mobile = textMobile;
        this.gender = textGender;

    }
}
