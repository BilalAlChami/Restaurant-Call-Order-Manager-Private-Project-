package com.restaurant.crm.model;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList; // Added for wrapText

public class Invoice {
    private String invoiceId;
    private Order order;
    private Contact contact;
    private LocalDateTime invoiceTimestamp;

    private final String restaurantName = "KEBAP 98 PIZZA";
    private final String restaurantAddressLine1 = "Kiesstraße 62";
    private final String restaurantAddressLine2 = "64287 Darmstadt";
    private final String restaurantPhone = "06151/7859103";
    private final String restaurantTaxId = "StNr: 007 836 31547";

    public Invoice(Order order, Contact contact, String formattedInvoiceId) {
        this.invoiceId = formattedInvoiceId;
        this.order = order;
        this.contact = contact;
        this.invoiceTimestamp = LocalDateTime.now();
    }

    public String getInvoiceId() { return invoiceId; }
    public Order getOrder() { return order; }
    public Contact getContact() { return contact; }
    public LocalDateTime getInvoiceTimestamp() { return invoiceTimestamp; }
    public String getRestaurantName() { return restaurantName; }
    public String getRestaurantAddressLine1() { return restaurantAddressLine1; }
    public String getRestaurantAddressLine2() { return restaurantAddressLine2; }
    public String getRestaurantPhone() { return restaurantPhone; }
    public String getRestaurantTaxId() { return restaurantTaxId; }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        int textLength = text.length(); // Count actual characters for centering
        int paddingSize = (width - textLength) / 2;
        String padding = " ".repeat(Math.max(0, paddingSize));
        return padding + text;
    }

    private String padRight(String text, int width) {
        if (text == null) text = "";
        return String.format("%-" + width + "." + width + "s", text); // Ensure truncation if too long
    }

    private String padLeft(String text, int width) {
        if (text == null) text = "";
        return String.format("%" + width + "s", text);
    }

    public String generateInvoiceText() {
        int pageWidth = 42;

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

        sb.append(centerText(getRestaurantName(), pageWidth)).append("\n");
        sb.append(centerText(getRestaurantAddressLine1(), pageWidth)).append("\n");
        sb.append(centerText(getRestaurantAddressLine2(), pageWidth)).append("\n");
        sb.append(centerText("Tel: " + getRestaurantPhone(), pageWidth)).append("\n");
        sb.append(centerText(getRestaurantTaxId(), pageWidth)).append("\n");
        sb.append("-".repeat(pageWidth)).append("\n");

        sb.append("Rechnung ").append(getInvoiceId()).append("\n");
        if (getOrder().getOrderId() != null) {
            String orderIdSuffix = getOrder().getOrderId().length() > 8 ? getOrder().getOrderId().substring(getOrder().getOrderId().length() - 8) : getOrder().getOrderId();
            sb.append("Bestell ID: ...").append(orderIdSuffix).append("\n");
        }
        sb.append("Datum: ").append(getInvoiceTimestamp().format(dtf)).append("\n");

        String deliveryType = "Lieferung/Bar";
        if (getOrder().getDriverName() != null && !getOrder().getDriverName().isEmpty()){
            if (getOrder().getDriverName().equalsIgnoreCase("Selbstabholer")) {
                deliveryType = "Selbstabholer";
            } else if (!getOrder().getDriverName().startsWith("0 -")) {
                deliveryType = "Lieferung (" + getOrder().getDriverName() + ")";
            }
        }
        sb.append("Art: ").append(deliveryType).append("\n");
        sb.append("-".repeat(pageWidth)).append("\n");

        if (getContact() != null) {
            sb.append("Kunde: ").append(getContact().getName()).append("\n");
            if (getContact().getAddress() != null && !getContact().getAddress().isEmpty()) {
                sb.append(getContact().getAddress()).append("\n");
            }
            sb.append("Tel: ").append(getContact().getPhoneNumber()).append("\n");
        } else if (getOrder().getContactPhoneNumber() != null && !getOrder().getContactPhoneNumber().equals("Barverkauf")) {
            sb.append("Kunde (Tel): ").append(getOrder().getContactPhoneNumber()).append("\n");
        } else {
            sb.append("Kunde: Barverkauf\n");
        }
        sb.append("-".repeat(pageWidth)).append("\n");

        int qtyWidth = 3;
        int singlePriceWidth = 7;
        int totalItemPriceWidth = 7;
        // Adjust itemColWidth based on actual characters, not just (pageWidth - others)
        // Example: "Mge Artikel Einzel Gesamt" -> 3 + 1 + X + 1 + 7 + 1 + 7 = pageWidth
        // X = pageWidth - 3 - 1 - 1 - 7 - 1 - 7 = pageWidth - 20
        int itemColWidth = pageWidth - qtyWidth - 1 - singlePriceWidth - 1 - totalItemPriceWidth - 1;


        sb.append(padRight("Mge", qtyWidth)).append(" ")
                .append(padRight("Artikel", itemColWidth)).append(" ")
                .append(padLeft("Einzel", singlePriceWidth)).append(" ")
                .append(padLeft("Gesamt", totalItemPriceWidth)).append("\n");
        sb.append("-".repeat(pageWidth)).append("\n");

        if (getOrder().getOrderItemsRuntime() != null) {
            for (OrderItem item : getOrder().getOrderItemsRuntime()) {
                String itemName = item.getMenuItem().getName();
                String customizations = item.getCustomizations();

                List<String> itemNameLines = wrapText(itemName, itemColWidth);

                for (int i = 0; i < itemNameLines.size(); i++) {
                    if (i == 0) { // First line of item name, include quantity and prices
                        sb.append(padRight(String.valueOf(item.getQuantity()), qtyWidth)).append(" ")
                                .append(padRight(itemNameLines.get(i), itemColWidth)).append(" ")
                                .append(padLeft(String.format(Locale.GERMANY, "%.2f", item.getItemPrice()), singlePriceWidth)).append(" ")
                                .append(padLeft(String.format(Locale.GERMANY, "%.2f", item.getTotalPrice()), totalItemPriceWidth)).append("\n");
                    } else { // Subsequent lines of item name
                        sb.append(padRight("", qtyWidth)).append(" ") // Empty space for qty
                                .append(padRight(itemNameLines.get(i), itemColWidth)).append("\n");
                    }
                }

                if (customizations != null && !customizations.isEmpty()) {
                    List<String> wrappedCust = wrapText("  (" + customizations + ")", itemColWidth); // Indent customizations
                    for (String custPart : wrappedCust) {
                        sb.append(padRight("", qtyWidth)).append(" ") // Empty space for qty
                                .append(padRight(custPart, itemColWidth)).append("\n");
                    }
                }
            }
        }
        sb.append("-".repeat(pageWidth)).append("\n");

        int labelWidth = pageWidth - 9;
        sb.append(padRight("Zwischensumme:", labelWidth))
                .append(padLeft(String.format(Locale.GERMANY, "%.2f", getOrder().getSubTotal()), 8)).append("\n");
        if (getOrder().getDeliveryCharge() > 0) {
            sb.append(padRight("Lieferkosten:", labelWidth))
                    .append(padLeft(String.format(Locale.GERMANY, "%.2f", getOrder().getDeliveryCharge()), 8)).append("\n");
        }
        if (getOrder().getExtraCharge() > 0) {
            sb.append(padRight("Extra Aufschlag:", labelWidth))
                    .append(padLeft(String.format(Locale.GERMANY, "%.2f", getOrder().getExtraCharge()), 8)).append("\n");
        }
        sb.append("-".repeat(pageWidth)).append("\n");
        sb.append(padRight("GESAMT:", labelWidth))
                .append(padLeft(String.format(Locale.GERMANY, "%.2f EUR", getOrder().getTotalAmount()), 8)).append("\n");
        sb.append("=".repeat(pageWidth)).append("\n");

        double totalNet7 = 0, totalTax7 = 0;
        double totalNet19 = 0, totalTax19 = 0;

        if (getOrder().getOrderItemsRuntime() != null) {
            for (OrderItem item : getOrder().getOrderItemsRuntime()) {
                if (item.getMenuItem().getTaxRate() == 0.07) {
                    totalNet7 += item.getTotalPrice() / 1.07;
                    totalTax7 += item.getTotalPrice() - (item.getTotalPrice() / 1.07);
                } else if (item.getMenuItem().getTaxRate() == 0.19) {
                    totalNet19 += item.getTotalPrice() / 1.19;
                    totalTax19 += item.getTotalPrice() - (item.getTotalPrice() / 1.19);
                }
            }
        }
        if (getOrder().getDeliveryCharge() > 0) {
            totalNet19 += getOrder().getDeliveryCharge() / 1.19;
            totalTax19 += getOrder().getDeliveryCharge() - (getOrder().getDeliveryCharge() / 1.19);
        }
        if (getOrder().getExtraCharge() > 0) {
            totalNet19 += getOrder().getExtraCharge() / 1.19;
            totalTax19 += getOrder().getExtraCharge() - (getOrder().getExtraCharge() / 1.19);
        }

        sb.append(centerText("Alle Preise inkl. gesetzl. MwSt.", pageWidth)).append("\n");
        if (Math.abs(totalTax7) > 0.005) { // Use Math.abs for small values near zero
            sb.append(String.format(Locale.GERMANY, "Enth. MwSt.  7%%: %6.2f (Net %6.2f)\n", totalTax7, totalNet7));
        }
        if (Math.abs(totalTax19) > 0.005) {
            sb.append(String.format(Locale.GERMANY, "Enth. MwSt. 19%%: %6.2f (Net %6.2f)\n", totalTax19, totalNet19));
        }
        sb.append("-".repeat(pageWidth)).append("\n");

        if (getOrder().getNotes() != null && !getOrder().getNotes().trim().isEmpty()) {
            sb.append("Anmerkungen:\n");
            List<String> wrappedNotes = wrapText(getOrder().getNotes(), pageWidth);
            for(String notePart : wrappedNotes) {
                sb.append(notePart).append("\n");
            }
        }

        sb.append("\n").append(centerText("Barzahlung", pageWidth)).append("\n");
        sb.append(centerText("Vielen Dank!", pageWidth)).append("\n");
        sb.append(centerText("www.kebap98.de", pageWidth)).append("\n");
        sb.append("=".repeat(pageWidth)).append("\n\n\n");

        return sb.toString();
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(""); // Add an empty line if text is null or empty to maintain structure
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() == 0) { // First word on the line
                if (word.length() > maxWidth) { // Word itself is too long
                    int offset = 0;
                    while(offset < word.length()){
                        lines.add(word.substring(offset, Math.min(offset + maxWidth, word.length())));
                        offset += maxWidth;
                    }
                } else {
                    currentLine.append(word);
                }
            } else if (currentLine.length() + 1 + word.length() <= maxWidth) { // Word fits with a space
                currentLine.append(" ").append(word);
            } else { // Word doesn't fit, start new line
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
                if (word.length() > maxWidth) { // New word is too long
                    int offset = 0;
                    while(offset < word.length()){
                        lines.add(word.substring(offset, Math.min(offset + maxWidth, word.length())));
                        offset += maxWidth;
                    }
                } else {
                    currentLine.append(word);
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        if (lines.isEmpty()) { // Ensure at least one line, even if empty, if original text was not empty but only spaces
            lines.add("");
        }
        return lines;
    }
}