package com.example.architecturecomponents.room.entities;

import androidx.room.ColumnInfo;

public class Address {

    public String street;
    public String state;
    public String city;

    @ColumnInfo(name = "post_code")
    public int postCode;

    public Address(String street, String state, String city, int postCode) {
        this.street = street;
        this.state = state;
        this.city = city;
        this.postCode = postCode;
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + street + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", postCode=" + postCode +
                '}';
    }
}