package com.restaurant.crm.ui;

import com.restaurant.crm.model.CustomizationResult;
import com.restaurant.crm.model.MenuItem;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomizationDialog extends Dialog<CustomizationResult> {

    private final MenuItem menuItem;
    private final Map<String, List<CheckBox>> checkBoxGroups = new HashMap<>();
    private final Map<String, ToggleGroup> radioGroupsByName = new HashMap<>();
    private final List<Pair<TextField, TextField>> customExtraFieldsList = new ArrayList<>();
    private TextField itemNoteField = null;

    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "^(.*?)\\s*::\\s*([A-Z_]+)(?:\\s*::\\s*([^:]+))?(?:\\s*::\\s*([\\w_.*+-]+))?$"
    );

    public CustomizationDialog(MenuItem menuItem, Map<String, List<String>> optionsByType, String initialCustomizationsText) {
        this.menuItem = menuItem;
        setTitle("Anpassen: " + menuItem.getName());
        initModality(Modality.APPLICATION_MODAL);

        DialogPane dialogPane = getDialogPane();
        dialogPane.setHeaderText("Wählen Sie Ihre Anpassungen für " + menuItem.getName() + ":");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogPane.setPrefWidth(500);

        VBox mainContent = new VBox(12);
        mainContent.setPadding(new Insets(15));

        boolean customExtraSectionExplicitlyAdded = false; // NEUES FLAG

        if (optionsByType != null && !optionsByType.isEmpty()) {
            optionsByType.forEach((groupLabel, optionsList) -> {
                Label groupTitleLabel = new Label(groupLabel + ":");
                groupTitleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                mainContent.getChildren().add(groupTitleLabel);
                VBox groupContainer = new VBox(7);
                groupContainer.setPadding(new Insets(0, 0, 8, 0));

                boolean isSingleSpecialType = optionsList.size() == 1 && optionsList.get(0).trim().startsWith("::");
                if (isSingleSpecialType) {
                    String specialOptionString = optionsList.get(0).trim();
                    String type = specialOptionString.substring(specialOptionString.lastIndexOf("::") + 2).trim().toUpperCase();
                    if ("NOTE".equals(type)) {
                        itemNoteField = new TextField();
                        itemNoteField.setPromptText("Spezielle Anmerkungen...");
                        groupContainer.getChildren().add(itemNoteField);
                    } else if ("CUSTOM_EXTRA".equals(type)) {
                        // Dieser Block verarbeitet ":: CUSTOM_EXTRA" aus CustomizationData
                        // Wir müssen den groupLabel von CustomizationData hier nicht explizit hinzufügen,
                        // da der groupTitleLabel bereits oben für die Gruppe hinzugefügt wurde.
                        addCustomExtraRow(groupContainer, "", ""); // Fügt die erste Zeile für Extras hinzu
                        Button addMoreCustomExtraButton = new Button("Weiteres benutzerdefiniertes Extra");
                        addMoreCustomExtraButton.setOnAction(e -> addCustomExtraRow(groupContainer, "", ""));
                        groupContainer.getChildren().add(addMoreCustomExtraButton);
                        // WICHTIG: Flag setzen, da wir explizit einen Custom Extra Bereich hinzugefügt haben
                        // customExtraSectionExplicitlyAdded = true;
                        // Stattdessen markieren wir den Container, um ihn später zu identifizieren
                        groupContainer.setId("explicitCustomExtraContainer_" + groupLabel.replaceAll("\\s+", "_"));
                    }
                } else {
                    List<CheckBox> currentCheckBoxList = new ArrayList<>();
                    String implicitRadioGroupName = groupLabel.replaceAll("\\s+", "_").toUpperCase() + "_RADIO_GROUP";

                    for (String optionString : optionsList) {
                        Matcher matcher = OPTION_PATTERN.matcher(optionString);
                        String displayText, type, parsedModifierValue, parsedRadioGroupName;

                        if (matcher.matches()) {
                            displayText = matcher.group(1) != null ? matcher.group(1).trim() : "";
                            type = matcher.group(2).trim().toUpperCase();

                            String segment3 = (matcher.group(3) != null) ? matcher.group(3).trim() : null;
                            String segment4 = (matcher.group(4) != null) ? matcher.group(4).trim() : null;

                            if (type.startsWith("RADIO")) {
                                if (segment4 != null) {
                                    parsedRadioGroupName = segment3;
                                    parsedModifierValue = segment4;
                                } else if (segment3 != null) {
                                    parsedRadioGroupName = segment3;
                                    parsedModifierValue = "";
                                } else {
                                    parsedRadioGroupName = implicitRadioGroupName;
                                    parsedModifierValue = "";
                                }
                            } else if (type.equals("CHK_PRICE")) {
                                parsedModifierValue = segment3;
                                parsedRadioGroupName = segment4;
                            } else {
                                parsedModifierValue = segment3 != null ? segment3 : "";
                                parsedRadioGroupName = segment4;
                            }
                        } else {
                            displayText = optionString.trim();
                            type = "CHK";
                            parsedModifierValue = "";
                            parsedRadioGroupName = null;
                        }

                        Node control;
                        ParsedOption parsedOpt = new ParsedOption(displayText, type, parsedModifierValue, parsedRadioGroupName, optionString);

                        if (type.startsWith("RADIO")) {
                            RadioButton rb = new RadioButton(displayText);
                            String effectiveRadioGroupName = (parsedRadioGroupName != null && !parsedRadioGroupName.isEmpty()) ?
                                    parsedRadioGroupName : implicitRadioGroupName;
                            ToggleGroup tg = radioGroupsByName.computeIfAbsent(effectiveRadioGroupName, k -> new ToggleGroup());
                            rb.setToggleGroup(tg);
                            rb.setUserData(parsedOpt);
                            control = rb;
                        } else {
                            CheckBox cb = new CheckBox(displayText);
                            cb.setUserData(parsedOpt);
                            currentCheckBoxList.add(cb);
                            control = cb;
                        }
                        groupContainer.getChildren().add(control);
                    }
                    if (!currentCheckBoxList.isEmpty()) {
                        checkBoxGroups.put(groupLabel, currentCheckBoxList);
                    }
                }
                mainContent.getChildren().add(groupContainer);
                if (new ArrayList<>(optionsByType.keySet()).indexOf(groupLabel) < optionsByType.size() - 1) {
                    mainContent.getChildren().add(new Separator(Orientation.HORIZONTAL));
                }
            });
        } else {
            mainContent.getChildren().add(new Label("Keine spezifischen Anpassungsoptionen."));
        }

        // Überprüfen, ob bereits ein benutzerdefinierter Extra-Bereich durch die optionsByType hinzugefügt wurde
        // Wir prüfen, ob ein VBox mit der spezifischen ID "explicitCustomExtraContainer_..." existiert.
        customExtraSectionExplicitlyAdded = mainContent.getChildren().stream()
                .filter(node -> node instanceof VBox) // Nur VBox-Container prüfen
                .anyMatch(node -> node.getId() != null && node.getId().startsWith("explicitCustomExtraContainer_"));


        if (!customExtraSectionExplicitlyAdded) { // Nur hinzufügen, wenn noch kein expliziter Bereich da ist
            addMissingCustomExtraSection(mainContent);
        }

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        dialogPane.setContent(scrollPane);

        setResultConverter(dialogButton -> dialogButton == ButtonType.OK ? collectCustomizationResult() : null);
    }

    private void addMissingCustomExtraSection(VBox mainContent) {
        Label customExtraTitle = new Label("Benutzerdefiniertes Extra (Standard):"); // Titel geändert zur Unterscheidung
        customExtraTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        VBox customExtraContainer = new VBox(7);
        customExtraContainer.setId("customExtraMainContainer_Fallback"); // Eindeutige ID für den Fallback
        addCustomExtraRow(customExtraContainer, "", "");
        Button addMoreCustomExtraButton = new Button("Weiteres benutzerdefiniertes Extra");
        addMoreCustomExtraButton.setOnAction(e -> addCustomExtraRow(customExtraContainer, "", ""));
        customExtraContainer.getChildren().add(addMoreCustomExtraButton);
        if(!mainContent.getChildren().isEmpty() && !(mainContent.getChildren().get(mainContent.getChildren().size()-1) instanceof Separator)){
            mainContent.getChildren().add(new Separator(Orientation.HORIZONTAL));
        }
        mainContent.getChildren().addAll(customExtraTitle, customExtraContainer);
    }

    private void addCustomExtraRow(VBox container, String initialText, String initialPrice) {
        HBox customExtraRow = new HBox(5);
        customExtraRow.setAlignment(Pos.CENTER_LEFT);
        TextField extraText = new TextField(initialText);
        extraText.setPromptText("Extra Beschreibung");
        HBox.setHgrow(extraText, javafx.scene.layout.Priority.ALWAYS);
        TextField extraPriceField = new TextField(initialPrice);
        extraPriceField.setPromptText("Preis");
        extraPriceField.setPrefWidth(60);
        customExtraFieldsList.add(new Pair<>(extraText, extraPriceField));
        Button removeRowButton = new Button("X");
        removeRowButton.setStyle("-fx-text-fill:red;-fx-font-size:9px;-fx-padding:1 3 1 3;");
        removeRowButton.setOnAction(e -> {
            container.getChildren().remove(customExtraRow);
            customExtraFieldsList.removeIf(p -> p.getKey() == extraText && p.getValue() == extraPriceField);
        });
        customExtraRow.getChildren().addAll(extraText, new Label("Preis:"), extraPriceField, new Label("€"), removeRowButton);

        Node lastElementInContainer = container.getChildren().isEmpty() ? null : container.getChildren().get(container.getChildren().size() - 1);
        if (lastElementInContainer instanceof Button && ((Button) lastElementInContainer).getText().toLowerCase().contains("weiteres")) {
            container.getChildren().add(container.getChildren().size() - 1, customExtraRow);
        } else {
            container.getChildren().add(customExtraRow);
        }
    }

    private CustomizationResult collectCustomizationResult() {
        StringBuilder formattedTextAggregator = new StringBuilder();
        double currentPriceAdjustment = 0.0;
        double currentSizeMultiplier = 1.0;
        double currentAbsoluteItemPrice = -1.0;
        String currentItemSpecificNote = (itemNoteField != null) ? itemNoteField.getText().trim() : "";
        String mainRadioSelectionText = null;

        for (List<CheckBox> cbList : checkBoxGroups.values()) {
            for (CheckBox cb : cbList) {
                if (cb.isSelected()) {
                    ParsedOption opt = (ParsedOption) cb.getUserData();
                    if (formattedTextAggregator.length() > 0) formattedTextAggregator.append(", ");
                    formattedTextAggregator.append(opt.displayText);
                    if ("CHK_PRICE".equals(opt.type) && opt.modifierValue != null && !opt.modifierValue.isEmpty()) {
                        currentPriceAdjustment += parseAdditivePrice(opt.modifierValue);
                    }
                }
            }
        }

        for (ToggleGroup tg : radioGroupsByName.values()) {
            Toggle selectedToggle = tg.getSelectedToggle();
            if (selectedToggle != null) {
                ParsedOption opt = (ParsedOption) selectedToggle.getUserData();

                if (formattedTextAggregator.length() > 0) formattedTextAggregator.append(", ");
                formattedTextAggregator.append(opt.displayText);

                if ("PIZZA_GROESSE".equals(opt.radioGroupName)) {
                    mainRadioSelectionText = opt.displayText;
                    if (opt.modifierValue != null && !opt.modifierValue.isEmpty()) {
                        if ("USE_STD_PRICE".equalsIgnoreCase(opt.modifierValue)) {
                            currentAbsoluteItemPrice = this.menuItem.getPrice();
                        } else if ("FETCH_FAMILY_PRICE".equalsIgnoreCase(opt.modifierValue)) {
                            currentAbsoluteItemPrice = this.menuItem.getPriceFamily();
                        } else if ("FETCH_PARTY_PRICE".equalsIgnoreCase(opt.modifierValue)) {
                            currentAbsoluteItemPrice = this.menuItem.getPriceParty();
                        }
                    }
                } else if (opt.modifierValue != null && !opt.modifierValue.isEmpty()){
                    currentPriceAdjustment += parseAdditivePrice(opt.modifierValue);
                }
            }
        }

        for (Pair<TextField, TextField> customExtraPair : customExtraFieldsList) {
            String text = customExtraPair.getKey().getText().trim();
            String priceStr = customExtraPair.getValue().getText().trim();
            if (!text.isEmpty()) {
                double price = 0.0;
                boolean priceSpecified = false;
                if (!priceStr.isEmpty()) {
                    try {
                        price = Double.parseDouble(priceStr.replace(",","."));
                        currentPriceAdjustment += price;
                        priceSpecified = true;
                    } catch (NumberFormatException e) {}
                }
                if (formattedTextAggregator.length() > 0) formattedTextAggregator.append(", ");
                formattedTextAggregator.append(text);
                if (priceSpecified && price != 0) {
                    formattedTextAggregator.append(String.format(Locale.GERMANY, " [+%.2f€]", price));
                }
            }
        }

        if (!currentItemSpecificNote.isEmpty()) {
            if (formattedTextAggregator.length() > 0) {
                formattedTextAggregator.append(" (Notiz: ").append(currentItemSpecificNote).append(")");
            } else {
                formattedTextAggregator.append("Notiz: ").append(currentItemSpecificNote);
            }
        }

        if (currentAbsoluteItemPrice >= 0) {
            currentSizeMultiplier = 1.0;
        } else if (this.menuItem.getCategory().toLowerCase().contains("pizza") && currentAbsoluteItemPrice < 0) {
            currentAbsoluteItemPrice = this.menuItem.getPrice();
            currentSizeMultiplier = 1.0;
        }

        return new CustomizationResult(
                formattedTextAggregator.toString(), currentPriceAdjustment,
                currentSizeMultiplier, currentAbsoluteItemPrice,
                currentItemSpecificNote, mainRadioSelectionText);
    }

    private double parseAdditivePrice(String modifierValue) {
        if (modifierValue == null || modifierValue.isEmpty()) return 0.0;
        try {
            String cleanedValue = modifierValue.replace("€","").replace(",", ".").trim();
            if(cleanedValue.startsWith("+")) {
                return Double.parseDouble(cleanedValue.substring(1));
            } else if(cleanedValue.startsWith("-")) {
                return Double.parseDouble(cleanedValue);
            }
            return Double.parseDouble(cleanedValue);
        } catch (NumberFormatException e) {
            System.err.println("Fehler Parsen additiver Preis: " + modifierValue + " -> " + e.getMessage());
            return 0.0;
        }
    }

    private static class ParsedOption {
        final String displayText, type, modifierValue, radioGroupName, originalOptionString;
        ParsedOption(String dt, String ty, String mv, String rgn, String oos){
            this.displayText=dt;
            this.type=ty;
            this.modifierValue=mv;
            this.radioGroupName=rgn;
            this.originalOptionString=oos;
        }
    }
}