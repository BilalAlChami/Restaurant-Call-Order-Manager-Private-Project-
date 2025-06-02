package com.restaurant.crm.model;

import java.util.Objects;

public class MenuItem {
    private String id;
    private String category;
    private String name;
    private double price;         // Preis für 30cm
    private double priceFamily;   // Preis für Familiengröße
    private double priceParty;    // Preis für Partygröße
    private double taxRate;

    /**
     * Konstruktor für Menüpunkte, die möglicherweise keine separaten Familien- oder Partypreise haben.
     * Für diese wird priceFamily und priceParty auf den Standardpreis gesetzt oder könnte 0 sein.
     */
    public MenuItem(String id, String category, String name, double price, double taxRate) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.price = price;
        this.taxRate = taxRate;
        // Standardmäßig sind Familien- und Partypreis gleich dem Standardpreis,
        // es sei denn, es ist keine Pizza (dann 0 oder irrelevant).
        // Für Pizzen wird der spezifischere Konstruktor erwartet.
        if (category.toLowerCase().contains("pizza")) {
            this.priceFamily = price; // Fallback, falls der spezifische Konstruktor nicht verwendet wird
            this.priceParty = price;  // Fallback
        } else {
            this.priceFamily = 0;
            this.priceParty = 0;
        }
    }

    /**
     * Hauptkonstruktor für Menüpunkte mit spezifischen Preisen für 30cm, Familie und Party.
     */
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
        // Zeigt nur den Standardpreis (30cm) in der allgemeinen Menüansicht (TabPane Buttons)
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