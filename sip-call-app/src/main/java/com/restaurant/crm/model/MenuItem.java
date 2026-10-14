package com.restaurant.crm.model;

import java.util.Objects;

public class MenuItem {
    private String id;
    private String category;
    private String name;
    private double price;         
    private double priceFamily;   
    private double priceParty; 
    private double taxRate;


    public MenuItem(String id, String category, String name, double price, double taxRate) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.taxRate = taxRate;
        if (category.toLowerCase().contains("pizza")) {
            this.priceFamily = price; 
            this.priceParty = price;  
        } else {
            this.priceFamily = 0;
            this.priceParty = 0;
        }
    }


    public MenuItem(String id, String category, String name, double price30cm, double priceFamily, double priceParty, double taxRate) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price30cm;
        this.priceFamily = priceFamily;
        this.priceParty = priceParty;
        this.taxRate = taxRate;
    }

    // Getters
    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public double getPrice() { return price; } // Preis für 30cm
    public double getPriceFamily() { return priceFamily; }
    public double getPriceParty() { return priceParty; }
    public double getTaxRate() { return taxRate; }

    @Override
    public String toString() {
        return name + " (" + String.format("%.2f", price) + " €)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return Objects.equals(id, menuItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
