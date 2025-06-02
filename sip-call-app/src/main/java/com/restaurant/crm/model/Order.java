package com.restaurant.crm.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    private String orderId;
    private String contactPhoneNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTimestamp;
    private List<OrderItemSerializationWrapper> items; // Verwendet den erweiterten Wrapper
    private double subTotal;
    private double taxAmount7;
    private double taxAmount19;
    private double deliveryCharge;
    private double extraCharge;
    private String notes;
    private String driverName;

    @JsonIgnore
    private transient List<OrderItem> orderItemsRuntime;
    @JsonIgnore
    private double totalAmount;


    public Order() {
        this.orderId = UUID.randomUUID().toString();
        this.orderTimestamp = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.orderItemsRuntime = new ArrayList<>();
        this.extraCharge = 0.0;
        this.driverName = "";
    }

    public Order(String contactPhoneNumber) {
        this();
        this.contactPhoneNumber = contactPhoneNumber;
    }

    // Getters and Setters (JsonProperty Annotationen sind wichtig für Jackson)
    @JsonProperty("orderId")
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    @JsonProperty("contactPhoneNumber")
    public String getContactPhoneNumber() { return contactPhoneNumber; }
    public void setContactPhoneNumber(String contactPhoneNumber) { this.contactPhoneNumber = contactPhoneNumber; }

    @JsonProperty("orderTimestamp")
    public LocalDateTime getOrderTimestamp() { return orderTimestamp; }
    public void setOrderTimestamp(LocalDateTime orderTimestamp) { this.orderTimestamp = orderTimestamp; }

    @JsonProperty("items")
    public List<OrderItemSerializationWrapper> getItems() {
        // Konvertiere runtime OrderItems zu Wrappern für die Serialisierung
        this.items.clear();
        if (this.orderItemsRuntime != null) {
            for (OrderItem oi : this.orderItemsRuntime) {
                this.items.add(new OrderItemSerializationWrapper(oi)); // Übergibt das OrderItem
            }
        }
        return items;
    }
    public void setItems(List<OrderItemSerializationWrapper> items) { this.items = items; }

    @JsonIgnore
    public List<OrderItem> getOrderItemsRuntime() {
        if (orderItemsRuntime == null) orderItemsRuntime = new ArrayList<>();
        return orderItemsRuntime;
    }
    @JsonIgnore
    public void setOrderItemsRuntime(List<OrderItem> orderItemsRuntime) {
        this.orderItemsRuntime = orderItemsRuntime;
        recalculateTotals();
    }

    @JsonProperty("subTotal")
    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }

    // ... (Restliche Getter/Setter für taxAmount, deliveryCharge, extraCharge, notes, driverName) ...
    @JsonProperty("taxAmount7")
    public double getTaxAmount7() { return taxAmount7; }
    public void setTaxAmount7(double taxAmount7) { this.taxAmount7 = taxAmount7; }

    @JsonProperty("taxAmount19")
    public double getTaxAmount19() { return taxAmount19; }
    public void setTaxAmount19(double taxAmount19) { this.taxAmount19 = taxAmount19; }

    @JsonProperty("deliveryCharge")
    public double getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
        recalculateTotals();
    }

    @JsonProperty("extraCharge")
    public double getExtraCharge() { return extraCharge; }
    public void setExtraCharge(double extraCharge) {
        this.extraCharge = extraCharge;
        recalculateTotals();
    }

    @JsonProperty("notes")
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @JsonProperty("driverName")
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }


    @JsonProperty("totalAmount")
    public double getTotalAmount() { return totalAmount; }
    // Kein öffentlicher Setter für totalAmount, wird berechnet
    private void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount;}


    @JsonIgnore
    public void addOrderItem(OrderItem item) {
        if (orderItemsRuntime == null) orderItemsRuntime = new ArrayList<>();
        orderItemsRuntime.add(item);
        recalculateTotals();
    }

    @JsonIgnore
    public void removeOrderItem(OrderItem item) {
        if (orderItemsRuntime != null) {
            orderItemsRuntime.remove(item);
            recalculateTotals();
        }
    }

    @JsonIgnore
    public void clearOrderItems() {
        if (orderItemsRuntime != null) orderItemsRuntime.clear();
        recalculateTotals(); // Wichtig, um Summen auf 0 zu setzen
    }

    @JsonIgnore
    public void recalculateTotals() {
        double currentSubTotal = 0;
        double currentTax7 = 0;
        double currentTax19 = 0;

        if (orderItemsRuntime != null) {
            for (OrderItem item : orderItemsRuntime) {
                double itemTotal = item.getTotalPrice(); // Holt den korrekten Gesamtpreis des OrderItems
                currentSubTotal += itemTotal;
                // Steuerberechnung basierend auf dem Brutto-Gesamtpreis des Items
                if (item.getMenuItem().getTaxRate() == 0.07) {
                    currentTax7 += itemTotal / 1.07 * 0.07;
                } else if (item.getMenuItem().getTaxRate() == 0.19) {
                    currentTax19 += itemTotal / 1.19 * 0.19;
                }
            }
        }

        this.subTotal = currentSubTotal;
        this.taxAmount7 = currentTax7;
        this.taxAmount19 = currentTax19;
        this.setTotalAmount(this.subTotal + this.deliveryCharge + this.extraCharge);
    }

    // ERWEITERTER OrderItemSerializationWrapper
    public static class OrderItemSerializationWrapper {
        private String menuItemId;
        private int quantity;
        private String customizations;
        // NEU: Felder zum Speichern der Preiskomponenten
        private double basePriceSnapshot; // Der Basispreis, der für dieses Item galt (kann der absolute Preis sein)
        private double sizeMultiplierUsed;
        private double customizationPriceAdjustmentUsed;

        public OrderItemSerializationWrapper() {}

        // Konstruktor, der ein OrderItem entgegennimmt
        public OrderItemSerializationWrapper(OrderItem orderItem) {
            this.menuItemId = orderItem.getMenuItem().getId();
            this.quantity = orderItem.getQuantity();
            this.customizations = orderItem.getCustomizations();
            // Speichere die Preiskomponenten, die den finalen Preis des OrderItems bestimmt haben
            this.basePriceSnapshot = orderItem.basePriceProperty().get();
            this.sizeMultiplierUsed = orderItem.getSizeMultiplier();
            this.customizationPriceAdjustmentUsed = orderItem.getCustomizationPriceAdjustment();
        }

        // Getter und Setter für alle Felder (wichtig für Jackson)
        public String getMenuItemId() { return menuItemId; }
        public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getCustomizations() { return customizations; }
        public void setCustomizations(String customizations) { this.customizations = customizations; }

        public double getBasePriceSnapshot() { return basePriceSnapshot; }
        public void setBasePriceSnapshot(double basePriceSnapshot) { this.basePriceSnapshot = basePriceSnapshot; }

        public double getSizeMultiplierUsed() { return sizeMultiplierUsed; }
        public void setSizeMultiplierUsed(double sizeMultiplierUsed) { this.sizeMultiplierUsed = sizeMultiplierUsed; }

        public double getCustomizationPriceAdjustmentUsed() { return customizationPriceAdjustmentUsed; }
        public void setCustomizationPriceAdjustmentUsed(double customizationPriceAdjustmentUsed) { this.customizationPriceAdjustmentUsed = customizationPriceAdjustmentUsed; }
    }
}