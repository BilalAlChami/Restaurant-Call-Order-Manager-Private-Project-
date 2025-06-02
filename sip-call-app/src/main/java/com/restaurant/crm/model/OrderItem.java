package com.restaurant.crm.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderItem {
    private final MenuItem menuItem;
    private final IntegerProperty quantity;
    private final DoubleProperty basePrice;
    private final DoubleProperty itemPrice;
    private final DoubleProperty totalPrice;
    private final StringProperty itemName;
    private final StringProperty customizations;
    private final DoubleProperty customizationPriceAdjustment;
    private final DoubleProperty sizeMultiplier;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = new SimpleIntegerProperty(quantity);
        this.basePrice = new SimpleDoubleProperty(menuItem.getPrice()); // Initial mit Standardpreis
        this.customizations = new SimpleStringProperty("");
        this.customizationPriceAdjustment = new SimpleDoubleProperty(0.0);
        this.sizeMultiplier = new SimpleDoubleProperty(1.0);

        this.itemPrice = new SimpleDoubleProperty();
        this.itemPrice.bind(
                this.basePrice.multiply(this.sizeMultiplier).add(this.customizationPriceAdjustment)
        );
        this.totalPrice = new SimpleDoubleProperty();
        this.totalPrice.bind(this.itemPrice.multiply(this.quantity));
        this.itemName = new SimpleStringProperty(menuItem.getName());
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }
    public double getBasePrice() { return basePrice.get(); }
    public DoubleProperty basePriceProperty() { return basePrice; } // Wichtig, um Basispreis extern zu setzen
    public double getItemPrice() { return itemPrice.get(); }
    public DoubleProperty itemPriceProperty() { return itemPrice; }
    public double getTotalPrice() { return totalPrice.get(); }
    public DoubleProperty totalPriceProperty() { return totalPrice; }
    public String getItemName() { return itemName.get(); }
    public StringProperty itemNameProperty() { return itemName; }
    public String getCustomizations() { return customizations.get(); }
    public void setCustomizations(String customizations) { this.customizations.set(customizations); }
    public StringProperty customizationsProperty() { return customizations; }
    public double getCustomizationPriceAdjustment() { return customizationPriceAdjustment.get(); }
    public void setCustomizationPriceAdjustment(double adjustment) { this.customizationPriceAdjustment.set(adjustment); }
    public DoubleProperty customizationPriceAdjustmentProperty() { return customizationPriceAdjustment; }
    public double getSizeMultiplier() { return sizeMultiplier.get(); }
    public void setSizeMultiplier(double multiplier) { this.sizeMultiplier.set(multiplier > 0 ? multiplier : 1.0); }
    public DoubleProperty sizeMultiplierProperty() { return sizeMultiplier; }
    public void incrementQuantity() { this.quantity.set(this.quantity.get() + 1); }
    public void decrementQuantity() { if (this.quantity.get() > 0) this.quantity.set(this.quantity.get() - 1); }
    public String getDisplayNameWithCustomizations() {
        String base = getItemName(); String cust = getCustomizations();
        return (cust != null && !cust.isEmpty()) ? base + " (" + cust + ")" : base;
    }
}