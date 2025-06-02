package com.restaurant.crm.model;

public class CustomizationResult {
    private final String formattedCustomizations;
    private final double priceAdjustment;       // Für preisliche Extras (Beläge etc.)
    private final double sizePriceMultiplier;   // Wird für Pizza immer 1.0 sein, wenn absoluteItemPrice genutzt wird
    private final double absoluteItemPrice;     // Hier kommt der Preis für 30cm, Familie oder Party rein. -1.0 falls nicht explizit gesetzt.
    private final String itemSpecificNote;
    private final String selectedRadioOptionInGroup; // z.B. gewählte Größe als Text ("Standard", "Familie", "Party")


    public CustomizationResult(String formattedCustomizations, double priceAdjustment,
                               double sizePriceMultiplier, double absoluteItemPrice,
                               String itemSpecificNote, String selectedRadioOptionInGroup) {
        this.formattedCustomizations = formattedCustomizations;
        this.priceAdjustment = priceAdjustment;
        this.sizePriceMultiplier = sizePriceMultiplier;
        this.absoluteItemPrice = absoluteItemPrice;
        this.itemSpecificNote = itemSpecificNote;
        this.selectedRadioOptionInGroup = selectedRadioOptionInGroup;
    }
    public String getFormattedCustomizations() { return formattedCustomizations; }
    public double getPriceAdjustment() { return priceAdjustment; }
    public double getSizePriceMultiplier() { return sizePriceMultiplier; }
    public double getAbsoluteItemPrice() { return absoluteItemPrice; }
    public String getItemSpecificNote() { return itemSpecificNote; }
    public String getSelectedRadioOptionInGroup() { return selectedRadioOptionInGroup; }
    public boolean hasAbsoluteItemPrice() { return absoluteItemPrice >= 0; } // True, wenn ein spezifischer Preis gesetzt wurde
}