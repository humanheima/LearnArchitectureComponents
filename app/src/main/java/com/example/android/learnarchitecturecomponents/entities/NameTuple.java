package com.example.android.learnarchitecturecomponents.entities;

import android.arch.persistence.room.ColumnInfo;

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