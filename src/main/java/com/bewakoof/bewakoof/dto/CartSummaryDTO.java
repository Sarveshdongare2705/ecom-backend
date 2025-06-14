package com.bewakoof.bewakoof.dto;

public class CartSummaryDTO {
    private double totalPrice;
    private double totalSavings;

    public CartSummaryDTO(double totalPrice, double totalSavings) {
        this.totalPrice = totalPrice;
        this.totalSavings = totalSavings;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public double getTotalSavings() {
        return totalSavings;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalSavings(double totalSavings) {
        this.totalSavings = totalSavings;
    }
}
