package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FoodProduct extends AbstractProduct {
    private LocalDate expirationDate;

    public FoodProduct(int id, String name, double price, int quantity, LocalDate expirationDate) {
        super(id, name, price, quantity);
        this.expirationDate = expirationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return super.toString() + ", expirationDate=" + expirationDate.format(DateTimeFormatter.ISO_DATE) + '}';
    }
}