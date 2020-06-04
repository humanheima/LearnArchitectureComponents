package com.example.android.jetpackdemo.room.entities;

import androidx.room.ColumnInfo;

public class NameTuple {
    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "lastName")
    public String lastName;

    @Override
    public String toString() {
        return "NameTuple{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}