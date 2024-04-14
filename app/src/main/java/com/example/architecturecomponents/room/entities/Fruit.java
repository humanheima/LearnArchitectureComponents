package com.example.architecturecomponents.room.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by DuMingwei on 2018/8/3.
 * Description:
 */
@Entity
public class Fruit {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double price;

    public Fruit(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Fruit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
