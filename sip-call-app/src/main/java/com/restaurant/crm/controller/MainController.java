package com.restaurant.crm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.restaurant.crm.model.Contact;
import com.restaurant.crm.model.CustomizationData;
import com.restaurant.crm.model.CustomizationResult;
import com.restaurant.crm.model.Invoice;
import com.restaurant.crm.model.MenuItem;
import com.restaurant.crm.model.Order;
import com.restaurant.crm.model.OrderItem;
import com.restaurant.crm.service.AddressSuggestionService;
import com.restaurant.crm.service.ContactService;
import com.restaurant.crm.service.InvoiceNumberService;
import com.restaurant.crm.service.MenuService;
import com.restaurant.crm.service.OrderService;
import com.restaurant.crm.ui.CustomizationDialog;
import com.restaurant.crm.util.Dialogs;
import com.restaurant.crm.util.InvoicePrinter;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays; // Import für Arrays.asList
import java.util.HashSet; // Import für HashSet
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Import für Set
import java.util.stream.Collectors;

public class MainController {

    // --- Customer Fields ---
    @FXML private TextField phoneNumberField;
    @FXML private TextField nameField;
    @FXML private TextField addressField;

    // --- Order System Fields ---
    @FXML private TextField itemNumberInput;
    @FXML private TabPane menuTabPane;
    @FXML private TableView<OrderItem> currentOrderTable;
    @FXML private TableColumn<OrderItem, String> orderItemNameColumn;
    @FXML private TableColumn<OrderItem, Integer> orderItemQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> orderItemPriceColumn;
    @FXML private TableColumn<OrderItem, Double> orderItemTotalColumn;
    @FXML private TableColumn<OrderItem, Void> orderItemActionColumn;
    @FXML private Label subTotalLabel;
    @FXML private TextField deliveryChargeField;
    @FXML private TextField extraChargeOrderField;
    @FXML private ComboBox<String> driverComboBox;
    @FXML private Label totalOrderLabel;
    @FXML private TextArea orderNotesField;
    @FXML private Button placeOrderButton;
    @FXML private Button updateOrderButton;
    @FXML private Button printInvoiceButton;
    @FXML private TextArea logTextArea;

    // --- Old Orders Management Fields ---
    @FXML private TableView<Order> oldOrdersTable;
    @FXML private TableColumn<Order, String> oldOrderIdCol;
    @FXML private TableColumn<Order, LocalDateTime> oldOrderTimestampCol;
    @FXML private TableColumn<Order, String> oldOrderCustomerCol;
    @FXML private TableColumn<Order, String> oldOrderDriverCol;
    @FXML private TableColumn<Order, Double> oldOrderTotalCol;
    @FXML private TableColumn<Order, Void> oldOrderActionsCol;

    // --- Contacts Table Fields ---
    @FXML private TableView<Contact> contactsTable;
    @FXML private TableColumn<Contact, String> contactNameCol;
    @FXML private TableColumn<Contact, String> contactPhoneCol;
    @FXML private TableColumn<Contact, String> contactAddressCol;


    // Services
    private ContactService contactService;
    private MenuService menuService;
    private OrderService orderService;
    private AddressSuggestionService addressSuggestionService;
    private InvoiceNumberService invoiceNumberService;

    // State
    private Contact currentDisplayedContact;
    private ObservableList<OrderItem> currentOrderItems;
    private Order currentOrder;
    private String editingOrderId = null;
    private NumberFormat currencyFormatter;
    private ObservableList<String> driverNames;
    private ObservableList<Order> pastOrders;
    private ObservableList<Contact> allContacts;
    private ObjectMapper fileObjectMapper;
    // Autocomplete
    private ContextMenu phoneSuggestionsPopup;
    private ContextMenu addressSuggestionsPopup;
    private PauseTransition addressDebounce;

    @FXML
    public void initialize() {
        contactService = new ContactService();
        menuService = new MenuService();
        orderService = new OrderService(menuService);
        addressSuggestionService = new AddressSuggestionService();
        this.fileObjectMapper = new ObjectMapper();
        this.fileObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

        allContacts = FXCollections.observableArrayList();

        initializeContactManagement();
        initializeOrderSystem();
        initializeOldOrdersTable();
        initializeContactsTable();

        itemNumberInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleAddItemByNumber();
            }
        });

        switchToPlacingNewOrderMode();
        logMessage("System initialisiert und bereit.");
        loadContactsTable();
    }

    // ... (Rest der Methoden bleibt gleich bis populateMenuTabs) ...
    private void logMessage(String message) {
        if (logTextArea != null && logTextArea.isVisible() && logTextArea.getScene() != null) {
            logTextArea.appendText(message + "\n");
        } else {
            System.out.println("[LOG] " + message);
        }
    }

    public void logMessageFromExternal(String message) {
        Platform.runLater(() -> {
            if (logTextArea != null && logTextArea.getScene() != null && logTextArea.isVisible()) {
                logTextArea.appendText("[FritzBox] " + message + "\n");
            }
        });
    }

    private void initializeContactsTable() {
        contactNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        contactAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        contactsTable.setItems(allContacts);

        contactsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayContact(newSelection);
            }
        });
    }

    private void loadContactsTable() {
        if(contactService != null && allContacts != null) {
            allContacts.setAll(contactService.getContacts());
            if(contactsTable != null) contactsTable.refresh();
        }
    }

    private void initializeOrderSystem() {
        currentOrderItems = FXCollections.observableArrayList();
        currentOrderTable.setItems(currentOrderItems);
        currentOrder = new Order(null);

        setupOrderTableColumns();
        populateMenuTabs(); // Hier wird die TabClosingPolicy gesetzt

        deliveryChargeField.textProperty().addListener((obs, oldVal, newVal) -> updateDeliveryCharge(newVal));
        extraChargeOrderField.textProperty().addListener((obs, oldVal, newVal) -> updateExtraCharge(newVal));

        driverNames = FXCollections.observableArrayList("Renault", "VW");
        driverComboBox.setItems(driverNames);
        driverComboBox.getSelectionModel().selectFirst();
        printInvoiceButton.setDisable(true);
    }

    private void switchToPlacingNewOrderMode() {
        editingOrderId = null;
        placeOrderButton.setVisible(true);
        placeOrderButton.setManaged(true);
        updateOrderButton.setVisible(false);
        updateOrderButton.setManaged(false);
        currentOrder = new Order(null);
    }

    private void switchToEditingOrderMode(String orderId) {
        editingOrderId = orderId;
        placeOrderButton.setVisible(false);
        placeOrderButton.setManaged(false);
        updateOrderButton.setVisible(true);
        updateOrderButton.setManaged(true);
    }

    private void initializeContactManagement() {
        phoneSuggestionsPopup = new ContextMenu();
        phoneNumberField.textProperty().addListener((obs, oldVal, newVal) -> handlePhoneNumberInputChange(newVal));
        phoneNumberField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && phoneSuggestionsPopup.isShowing()) phoneSuggestionsPopup.hide();
        });

        addressSuggestionsPopup = new ContextMenu();
        addressDebounce = new PauseTransition(Duration.millis(300));
        addressDebounce.setOnFinished(event -> fetchAddressSuggestions(addressField.getText()));
        addressField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty() && newVal.length() >=3) {
                addressDebounce.playFromStart();
            } else {
                addressSuggestionsPopup.hide();
            }
        });
        addressField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && addressSuggestionsPopup.isShowing()) addressSuggestionsPopup.hide();
        });
    }

    private void updateDeliveryCharge(String newValue) {
        try {
            currentOrder.setDeliveryCharge(Double.parseDouble(newValue.replace(",", ".")));
        } catch (NumberFormatException e) {
            currentOrder.setDeliveryCharge(0.0);
            deliveryChargeField.setText("0.00");
        }
        updateOrderTotals();
    }

    private void updateExtraCharge(String newValue) {
        try {
            currentOrder.setExtraCharge(Double.parseDouble(newValue.replace(",", ".")));
        } catch (NumberFormatException e) {
            currentOrder.setExtraCharge(0.0);
        }
        updateOrderTotals();
    }

    private void showCustomizationDialog(MenuItem menuItem, OrderItem existingOrderItem, Map<String, List<String>> options) {
        String initialCustomizationsText = (existingOrderItem != null) ? existingOrderItem.getCustomizations() : "";
        CustomizationDialog dialog = new CustomizationDialog(menuItem, options, initialCustomizationsText);
        Optional<CustomizationResult> resultOpt = dialog.showAndWait();

        resultOpt.ifPresent(customizationResult -> {
            if (existingOrderItem != null) {
                applyCustomizationResultToOrderItem(existingOrderItem, menuItem, customizationResult);
            } else {
                OrderItem newOrderItem = new OrderItem(menuItem, 1);
                applyCustomizationResultToOrderItem(newOrderItem, menuItem, customizationResult);
                addOrIncrementOrderItem(newOrderItem);
            }
            currentOrderTable.refresh();
            updateOrderTotals();
        });
    }

    private void applyCustomizationResultToOrderItem(OrderItem orderItem, MenuItem menuItemRef, CustomizationResult result) {
        if (result.hasAbsoluteItemPrice()) {
            orderItem.basePriceProperty().set(result.getAbsoluteItemPrice());
            orderItem.setSizeMultiplier(1.0);
        } else {
            orderItem.basePriceProperty().set(menuItemRef.getPrice());
            orderItem.setSizeMultiplier(result.getSizePriceMultiplier());
        }
        orderItem.setCustomizationPriceAdjustment(result.getPriceAdjustment());
        orderItem.setCustomizations(result.getFormattedCustomizations());
    }

    private void addOrIncrementOrderItem(OrderItem newItemToAdd) {
        Optional<OrderItem> existingItemOpt = currentOrderItems.stream()
                .filter(oi -> oi.getMenuItem().getId().equals(newItemToAdd.getMenuItem().getId()) &&
                        oi.getCustomizations().equals(newItemToAdd.getCustomizations()) &&
                        Math.abs(oi.getItemPrice() - newItemToAdd.getItemPrice()) < 0.001)
                .findFirst();

        if (existingItemOpt.isPresent()) {
            existingItemOpt.get().incrementQuantity();
        } else {
            currentOrderItems.add(newItemToAdd);
        }
        currentOrder.setOrderItemsRuntime(new ArrayList<>(currentOrderItems));
        currentOrderTable.refresh();
    }

    private class OrderItemActionCell extends TableCell<OrderItem, Void> {
        private final Button removeButton = new Button("X");
        private final Button plusButton = new Button("+");
        private final Button minusButton = new Button("-");
        private final Button editButton = new Button("...");
        private final HBox pane = new HBox(3, minusButton, plusButton, removeButton, editButton);

        OrderItemActionCell() {
            removeButton.setStyle("-fx-text-fill: red; -fx-font-size: 9px; -fx-padding: 2 4 2 4;");
            plusButton.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
            minusButton.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");
            editButton.setStyle("-fx-font-size: 9px; -fx-padding: 2 4 2 4;");

            removeButton.setOnAction(event -> handleRemoveAction());
            plusButton.setOnAction(event -> handlePlusAction());
            minusButton.setOnAction(event -> handleMinusAction());
            editButton.setOnAction(event -> handleEditCustomizationsAction());
            pane.setAlignment(Pos.CENTER);
        }
        private void handleRemoveAction() {
            OrderItem item = getTableView().getItems().get(getIndex());
            currentOrderItems.remove(item);
            currentOrder.removeOrderItem(item);
            updateOrderTotals();
        }
        private void handlePlusAction() {
            getTableView().getItems().get(getIndex()).incrementQuantity();
            getTableView().refresh();
            updateOrderTotals();
        }
        private void handleMinusAction() {
            OrderItem item = getTableView().getItems().get(getIndex());
            if (item.getQuantity() > 1) {
                item.decrementQuantity();
            } else {
                currentOrderItems.remove(item);
                currentOrder.removeOrderItem(item);
            }
            getTableView().refresh();
            updateOrderTotals();
        }
        private void handleEditCustomizationsAction() {
            OrderItem selectedOrderItem = getTableView().getItems().get(getIndex());
            if (selectedOrderItem != null) {
                MenuItem menuItem = selectedOrderItem.getMenuItem();
                Map<String, List<String>> options = CustomizationData.getOptionsFor(menuItem.getId(), menuItem);
                if (options == null || options.isEmpty()) {
                    options = CustomizationData.getOptionsFor(menuItem.getCategory(), menuItem);
                }
                showCustomizationDialog(menuItem, selectedOrderItem, options);
            }
        }
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : pane);
        }
    }
    @FXML
    private void handleSaveContactsToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Alle Kontakte speichern unter...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Dateien", "*.json"));
        fileChooser.setInitialFileName("kontakte_export.json");
        File file = fileChooser.showSaveDialog(contactsTable.getScene().getWindow()); // Or any other node to get the window

        if (file != null) {
            try {
                List<Contact> contactsToSave = new ArrayList<>(contactService.getContacts());
                fileObjectMapper.writeValue(file, contactsToSave);
                Dialogs.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Kontakte erfolgreich in " + file.getName() + " gespeichert.");
                logMessage("Alle Kontakte nach " + file.getAbsolutePath() + " exportiert.");
            } catch (IOException e) {
                Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler beim Speichern", "Kontakte konnten nicht gespeichert werden: " + e.getMessage());
                logMessage("Fehler beim Exportieren der Kontakte: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLoadContactsFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Kontakte von Datei laden...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Dateien", "*.json"));
        File file = fileChooser.showOpenDialog(contactsTable.getScene().getWindow()); // Or any other node to get the window

        if (file != null) {
            try {
                List<Contact> loadedContacts = fileObjectMapper.readValue(file, new TypeReference<List<Contact>>() {});
                if (loadedContacts != null) {
                    // Ask for confirmation before replacing
                    Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION,
                            "Möchten Sie alle aktuellen Kontakte durch die Kontakte aus der Datei '" + file.getName() + "' ersetzen? Dieser Vorgang kann nicht rückgängig gemacht werden.",
                            ButtonType.YES, ButtonType.NO);
                    confirmationDialog.setTitle("Kontakte laden bestätigen");
                    confirmationDialog.setHeaderText("Kontakte ersetzen?");
                    Optional<ButtonType> result = confirmationDialog.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.YES) {
                        contactService.replaceAllContacts(loadedContacts); // Use new method in ContactService
                        loadContactsTable(); // Refresh the UI table
                        Dialogs.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Kontakte erfolgreich aus " + file.getName() + " geladen und als Standard gesetzt.");
                        logMessage("Kontakte aus " + file.getAbsolutePath() + " importiert und als Standard gesetzt.");
                    } else {
                        logMessage("Laden der Kontakte aus Datei abgebrochen.");
                    }
                }
            } catch (IOException e) {
                Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler beim Laden", "Kontakte konnten nicht geladen werden: " + e.getMessage());
                logMessage("Fehler beim Importieren der Kontakte: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleAddItemByNumber() {
        String number = itemNumberInput.getText().trim();
        if (number.isEmpty()) {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Eingabe fehlt", "Bitte Gerichtsnummer eingeben.");
            return;
        }
        MenuItem menuItem = menuService.findMenuItemById(number);
        if (menuItem != null) {
            Map<String, List<String>> options = CustomizationData.getOptionsFor(menuItem.getId(), menuItem);
            if (options == null || options.isEmpty()) {
                options = CustomizationData.getOptionsFor(menuItem.getCategory(), menuItem);
            }

            if (options != null && !options.isEmpty()) {
                showCustomizationDialog(menuItem, null, options);
            } else {
                OrderItem newOrderItem = new OrderItem(menuItem, 1);
                addOrIncrementOrderItem(newOrderItem);
                updateOrderTotals();
            }
            itemNumberInput.clear(); itemNumberInput.requestFocus();
        } else {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Gericht mit Nummer '" + number + "' nicht gefunden.");
            itemNumberInput.selectAll(); itemNumberInput.requestFocus();
        }
    }

    private void populateMenuTabs() {
        menuTabPane.getTabs().clear();
        menuTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        Map<String, List<MenuItem>> menuByCategory = menuService.getMenuItemsByCategory();

        // Ihre gewünschte Reihenfolge der Kategorien
        List<String> desiredCategoryOrder = Arrays.asList(
                "Angebote", "Döner", "Lahmacun", "Falafel", "Pide", "Calzone", "Flammkuchen",
                "Salate", "Snacks", "Schnitzel", "Vorspeisen", "Rollos", "Pizza",
                "Überbacken", "Getränke"
        );

        Set<String> processedCategories = new HashSet<>();

        // Zuerst die Tabs in der gewünschten Reihenfolge hinzufügen
        for (String categoryName : desiredCategoryOrder) {
            if (menuByCategory.containsKey(categoryName)) {
                List<MenuItem> items = menuByCategory.get(categoryName);
                if (items != null && !items.isEmpty()) {
                    createAndAddTab(categoryName, items);
                    processedCategories.add(categoryName);
                }
            }
        }

        // Dann die restlichen Kategorien hinzufügen, die nicht in der gewünschten Liste waren
        // (alphabetisch sortiert, da menuByCategory von einer TreeMap kommt)
        for (Map.Entry<String, List<MenuItem>> entry : menuByCategory.entrySet()) {
            if (!processedCategories.contains(entry.getKey())) {
                List<MenuItem> items = entry.getValue();
                if (items != null && !items.isEmpty()) {
                    createAndAddTab(entry.getKey(), items);
                }
            }
        }
    }

    private void createAndAddTab(String category, List<MenuItem> items) {
        Tab tab = new Tab(category);
        FlowPane flowPane = new FlowPane(5, 5);
        flowPane.setPadding(new Insets(5));
        items.forEach(menuItem -> {
            Button itemButton = new Button(menuItem.getName() + "\n" + currencyFormatter.format(menuItem.getPrice()));
            itemButton.setPrefSize(100, 50);
            itemButton.setTextAlignment(TextAlignment.CENTER); itemButton.setWrapText(true);
            itemButton.setStyle("-fx-font-size: 10px;");
            itemButton.setOnAction(event -> {
                Map<String, List<String>> options = CustomizationData.getOptionsFor(menuItem.getId(), menuItem);
                if (options == null || options.isEmpty()) {
                    options = CustomizationData.getOptionsFor(menuItem.getCategory(), menuItem);
                }
                if (options != null && !options.isEmpty()) {
                    showCustomizationDialog(menuItem, null, options);
                } else {
                    OrderItem newOrderItem = new OrderItem(menuItem, 1);
                    addOrIncrementOrderItem(newOrderItem);
                    updateOrderTotals();
                }
            });
            flowPane.getChildren().add(itemButton);
        });
        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        tab.setContent(scrollPane);
        menuTabPane.getTabs().add(tab);
    }


    private void updateOrderTotals() {
        if (currentOrder == null) currentOrder = new Order(null);
        currentOrder.setOrderItemsRuntime(new ArrayList<>(currentOrderItems));
        currentOrder.recalculateTotals();
        subTotalLabel.setText(currencyFormatter.format(currentOrder.getSubTotal()));
        totalOrderLabel.setText(currencyFormatter.format(currentOrder.getTotalAmount()));

        boolean canPrint = !currentOrderItems.isEmpty() &&
                (editingOrderId != null || (currentOrder != null && currentOrder.getOrderId() != null && !currentOrder.getOrderId().isEmpty()));
        printInvoiceButton.setDisable(!canPrint);
        if(canPrint){
            printInvoiceButton.setUserData(currentOrder.getOrderId());
        } else {
            printInvoiceButton.setUserData(null);
        }
    }

    @FXML private void handlePhoneNumberInputChange(String newValue) {
        if (newValue == null || newValue.isEmpty()) {
            phoneSuggestionsPopup.hide();
            clearContactFieldsForNewInput();
            return;
        }
        List<Contact> suggestions = contactService.findContactsByPhoneNumberPrefix(newValue);
        if (!suggestions.isEmpty()) {
            List<CustomMenuItem> menuItems = suggestions.stream().map(contact -> {
                CustomMenuItem item = new CustomMenuItem(new Label(contact.getPhoneNumber() + " (" + contact.getName() + ")"), false);
                item.setOnAction(e -> {
                    phoneNumberField.setText(contact.getPhoneNumber());
                    phoneSuggestionsPopup.hide();
                    displayContact(contact);
                });
                return item;
            }).collect(Collectors.toList());
            phoneSuggestionsPopup.getItems().setAll(menuItems);
            if (!phoneSuggestionsPopup.isShowing() && phoneNumberField.isFocused()) {
                phoneSuggestionsPopup.show(phoneNumberField, Side.BOTTOM, 0, 0);
            }
        } else {
            phoneSuggestionsPopup.hide();
        }

        Contact foundContact = contactService.findContactByPhoneNumber(newValue);
        if (foundContact != null) {
            if (currentDisplayedContact == null || !foundContact.getPhoneNumber().equals(currentDisplayedContact.getPhoneNumber())) {
                displayContact(foundContact);
            }
        } else {
            if (currentDisplayedContact != null && !newValue.equals(currentDisplayedContact.getPhoneNumber())) {
                clearContactFieldsForNewInput();
            }
        }
    }

    private void clearContactFieldsForNewInput() {
        nameField.clear();
        addressField.clear();
        currentDisplayedContact = null;
    }

    // Existing imports in MainController.java

// ... other fields and methods ...

    private void fetchAddressSuggestions(String query) {
        if (query == null) {
            addressSuggestionsPopup.hide();
            return;
        }
        final String finalQuery = query.trim();
        final String plzPattern = "\\d{5}"; // German PLZ is 5 digits

        // If the query is exactly a 5-digit PLZ
        if (finalQuery.matches(plzPattern)) {
            new Thread(() -> {
                // Try to find the city for this PLZ
                String city = addressSuggestionService.getCityForPlz(finalQuery); // New method in AddressSuggestionService
                Platform.runLater(() -> {
                    if (city != null && !city.isEmpty()) {
                        String newText = finalQuery + " " + city + " ";
                        // Temporarily remove listener to prevent immediate re-triggering from setText
                        // This can be tricky; a flag or more sophisticated state management might be better
                        // For now, direct call to playFromStart on debounce might be simpler if setText triggers it reliably.
                        addressField.setText(newText);
                        addressField.positionCaret(newText.length());
                        addressSuggestionsPopup.hide(); // Hide any previous suggestions
                        // Re-trigger suggestions for "PLZ City "
                        addressDebounce.playFromStart(); // This will call fetchAddressSuggestions with the new query
                    } else {
                        // PLZ not found, or no specific city, show generic suggestions for the PLZ query
                        showAddressSuggestionsForQuery(finalQuery);
                    }
                });
            }).start();
        } else if (finalQuery.length() >= 3) { // General search for queries of 3+ chars
            showAddressSuggestionsForQuery(finalQuery);
        } else { // Query is too short for general search and not a full PLZ
            addressSuggestionsPopup.hide();
        }
    }

    // Helper method to show address suggestions, extracted for reusability
    private void showAddressSuggestionsForQuery(String query) {
        new Thread(() -> {
            // This existing method in AddressSuggestionService already handles case-insensitivity
            // for street names or any part of the address string "PLZ Stadt Straßenname".
            List<String> suggestions = addressSuggestionService.getAddressSuggestions(query); //
            Platform.runLater(() -> {
                if (!suggestions.isEmpty() && addressField.isFocused()) {
                    List<javafx.scene.control.MenuItem> menuItems = suggestions.stream().map(suggestion -> {
                        javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(suggestion);
                        item.setOnAction(e -> {
                            addressField.setText(suggestion);
                            addressField.positionCaret(addressField.getText().length());
                            addressSuggestionsPopup.hide();
                        });
                        return item;
                    }).collect(Collectors.toList());
                    addressSuggestionsPopup.getItems().setAll(menuItems);
                    if (!addressSuggestionsPopup.isShowing()) {
                        addressSuggestionsPopup.show(addressField, Side.BOTTOM, 0, 0);
                    }
                } else {
                    addressSuggestionsPopup.hide();
                }
            });
        }).start();
    }

// ... rest of MainController.java ...

    private void displayContact(Contact contact) {
        phoneNumberField.setText(contact.getPhoneNumber());
        nameField.setText(contact.getName());
        addressField.setText(contact.getAddress());
        currentDisplayedContact = contact;
    }

    private void clearContactFields() {
        phoneNumberField.clear();
        nameField.clear();
        addressField.clear();
        currentDisplayedContact = null;
    }

    public void setIncomingPhoneNumber(String number) {
        Platform.runLater(() -> {
            phoneNumberField.setText(number);
            handlePhoneNumberInputChange(number);
            logMessage("Eingehender Anruf: " + number);
        });
    }

    @FXML private void handleSaveContact() {
        String phoneNumber = phoneNumberField.getText();
        String name = nameField.getText();
        String address = addressField.getText();
        if (phoneNumber.isEmpty() || name.isEmpty()) {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Fehlende Daten", "Telefonnummer und Name werden benötigt, um einen Kontakt zu speichern.");
            return;
        }
        Contact contact = new Contact(phoneNumber, name, address);
        contactService.addOrUpdateContact(contact);
        currentDisplayedContact = contact;
        Dialogs.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Kontakt gespeichert: " + name);
        logMessage("Kontakt gespeichert: " + name);
        loadContactsTable();
    }

    @FXML
    private void handleDeleteContactFromTable() {
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Kontakt '" + selectedContact.getName() + " (" + selectedContact.getPhoneNumber() + ")' wirklich löschen?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Kontakt löschen");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                contactService.deleteContact(selectedContact);
                logMessage("Kontakt aus Tabelle gelöscht: " + selectedContact.getName());
                loadContactsTable();

                if (currentDisplayedContact != null && currentDisplayedContact.equals(selectedContact)) {
                    clearContactFields();
                }
            }
        } else {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Kein Kontakt ausgewählt", "Bitte wählen Sie einen Kontakt aus der Tabelle zum Löschen aus.");
        }
    }

    @FXML private void handleNewContact() {
        clearContactFields();
        phoneNumberField.requestFocus();
        logMessage("Bereit für die Eingabe eines neuen Kontakts.");
        if(contactsTable != null) contactsTable.getSelectionModel().clearSelection();
    }

    private void setupOrderTableColumns() {
        orderItemNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDisplayNameWithCustomizations())
        );
        orderItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderItemPriceColumn.setCellValueFactory(cellData -> cellData.getValue().itemPriceProperty().asObject());
        orderItemTotalColumn.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());
        orderItemPriceColumn.setCellFactory(tc -> createCurrencyCell());
        orderItemTotalColumn.setCellFactory(tc -> createCurrencyCell());
        orderItemActionColumn.setCellFactory(param -> new OrderItemActionCell());
    }

    private TableCell<OrderItem, Double> createCurrencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : currencyFormatter.format(price));
            }
        };
    }

    @FXML private void handleClearOrder() {
        currentOrderItems.clear();
        if (currentOrder != null) {
            currentOrder.clearOrderItems();
        } else {
            currentOrder = new Order(null);
        }

        if (orderNotesField != null) orderNotesField.clear();
        deliveryChargeField.setText("0.00");
        extraChargeOrderField.setText("0.00");
        driverComboBox.getSelectionModel().selectFirst();

        if (editingOrderId != null) {
            currentOrder = new Order(null);
            switchToPlacingNewOrderMode();
        }
        updateOrderTotals();
        printInvoiceButton.setDisable(true);
        printInvoiceButton.setUserData(null);
        logMessage("Aktuelle Bestellung geleert.");
    }

    private boolean prepareOrderForSaveOrUpdate() {
        if (currentOrderItems.isEmpty()) {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Leere Bestellung", "Bitte Artikel zur Bestellung hinzufügen.");
            return false;
        }
        String phone = phoneNumberField.getText().trim();
        String name = nameField.getText().trim();

        if (currentDisplayedContact != null && currentDisplayedContact.getPhoneNumber().equals(phone)) {
            currentOrder.setContactPhoneNumber(currentDisplayedContact.getPhoneNumber());
            if (!currentDisplayedContact.getName().equals(name) ||
                    (currentDisplayedContact.getAddress() != null && !currentDisplayedContact.getAddress().equals(addressField.getText().trim())) ||
                    (currentDisplayedContact.getAddress() == null && !addressField.getText().trim().isEmpty())) {
                if (!name.isEmpty()){
                    handleSaveContact();
                }
            }
        } else if (!phone.isEmpty() && !name.isEmpty()) {
            Contact existingContact = contactService.findContactByPhoneNumber(phone);
            if(existingContact != null && existingContact.getName().equals(name)) {
                displayContact(existingContact);
                currentOrder.setContactPhoneNumber(existingContact.getPhoneNumber());
            } else {
                handleSaveContact();
                if (currentDisplayedContact != null) {
                    currentOrder.setContactPhoneNumber(currentDisplayedContact.getPhoneNumber());
                } else {
                    currentOrder.setContactPhoneNumber(phone);
                }
            }
        } else if (!phone.isEmpty()) {
            currentOrder.setContactPhoneNumber(phone);
            currentDisplayedContact = null;
        } else {
            currentOrder.setContactPhoneNumber("Barverkauf");
            currentDisplayedContact = null;
        }

        currentOrder.setOrderItemsRuntime(new ArrayList<>(currentOrderItems));
        if(orderNotesField != null) currentOrder.setNotes(orderNotesField.getText());
        String selectedDriver = driverComboBox.getSelectionModel().getSelectedItem();
        if (selectedDriver == null || selectedDriver.startsWith("0 -")) {
            currentOrder.setDriverName("");
        } else {
            currentOrder.setDriverName(selectedDriver);
        }
        currentOrder.recalculateTotals();
        return true;
    }

    @FXML private void handlePlaceOrder() {
        if (!prepareOrderForSaveOrUpdate()) return;

        if (editingOrderId != null) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Interner Fehler", "Versuch, eine bearbeitete Bestellung als neu zu platzieren.");
            return;
        }
        currentOrder.setOrderId(java.util.UUID.randomUUID().toString());
        currentOrder.setOrderTimestamp(java.time.LocalDateTime.now());

        orderService.addOrder(currentOrder);
        Dialogs.showAlert(Alert.AlertType.INFORMATION, "Bestellung aufgegeben", "Bestellung ID: " + currentOrder.getOrderId() + " gespeichert.");
        logMessage("Bestellung aufgegeben: ID " + currentOrder.getOrderId() + " für Fahrer: " + (currentOrder.getDriverName().isEmpty() ? "N/A" : currentOrder.getDriverName()));

        printInvoiceButton.setDisable(false);
        printInvoiceButton.setUserData(currentOrder.getOrderId());

        finalizeOrderPlacement();
        loadOldOrders();
    }

    @FXML private void handleUpdateOrder() {
        if (editingOrderId == null || currentOrder == null) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Keine Bestellung zum Aktualisieren ausgewählt.");
            return;
        }
        if (!currentOrder.getOrderId().equals(editingOrderId)) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Inkonsistenz bei der Bestell-ID für das Update.");
            return;
        }
        if (!prepareOrderForSaveOrUpdate()) return;

        orderService.updateOrder(currentOrder);
        Dialogs.showAlert(Alert.AlertType.INFORMATION, "Bestellung aktualisiert", "Bestellung ID: " + currentOrder.getOrderId() + " wurde aktualisiert.");
        logMessage("Bestellung aktualisiert: ID " + currentOrder.getOrderId());

        printInvoiceButton.setDisable(false);
        printInvoiceButton.setUserData(currentOrder.getOrderId());

        finalizeOrderPlacement();
        loadOldOrders();
    }

    @FXML
    private void handleDeleteAllData() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Alle Daten löschen");
        confirmation.setHeaderText("Wirklich ALLE Bestelldaten, Kontaktdaten und den Rechnungszähler zurücksetzen?");
        confirmation.setContentText("Diese Aktion kann nicht rückgängig gemacht werden!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                orderService.deleteAllData();
                contactService.deleteAllData();
                if (invoiceNumberService != null) {
                    invoiceNumberService.forceResetCounterNow();
                }

                currentOrderItems.clear();
                if (currentOrder != null) currentOrder.clearOrderItems();
                clearContactFields();
                loadOldOrders();
                loadContactsTable();
                updateOrderTotals();
                switchToPlacingNewOrderMode();
                printInvoiceButton.setDisable(true);
                logMessage("Alle Daten wurden gelöscht und Zähler zurückgesetzt.");
                Dialogs.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Alle Daten wurden gelöscht.");
            } catch (Exception e) {
                logMessage("Fehler beim Löschen aller Daten: " + e.getMessage());
                e.printStackTrace();
                Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Ein Fehler ist beim Löschen der Daten aufgetreten. Überprüfen Sie die Konsolenausgabe.");
            }
        }
    }


    private void finalizeOrderPlacement() {
        currentOrder = new Order(null);
        currentOrderItems.clear();
        if(orderNotesField != null) orderNotesField.clear();
        deliveryChargeField.setText("0.00");
        extraChargeOrderField.setText("0.00");
        driverComboBox.getSelectionModel().selectFirst();
        clearContactFields();

        updateOrderTotals();
        switchToPlacingNewOrderMode();
    }

    @FXML private void handlePrintInvoice() {
        String orderIdToPrint = null;
        if (printInvoiceButton.getUserData() instanceof String) {
            orderIdToPrint = (String) printInvoiceButton.getUserData();
        }

        if (orderIdToPrint == null) {
            if (currentOrder != null && currentOrder.getOrderId() != null && !currentOrderItems.isEmpty()){
                orderIdToPrint = currentOrder.getOrderId();
            } else {
                Dialogs.showAlert(Alert.AlertType.WARNING, "Keine Bestellung", "Keine gespeicherte Bestellung zum Drucken ausgewählt oder aktiv.");
                return;
            }
        }

        Order orderToPrint = orderService.findOrderById(orderIdToPrint);
        if (orderToPrint == null || orderToPrint.getOrderItemsRuntime() == null || orderToPrint.getOrderItemsRuntime().isEmpty()) {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Fehler beim Drucken", "Bestellung nicht gefunden oder ist leer. ID: " + orderIdToPrint);
            return;
        }

        Contact customer = null;
        if (orderToPrint.getContactPhoneNumber() != null && !orderToPrint.getContactPhoneNumber().equals("Barverkauf")) {
            customer = contactService.findContactByPhoneNumber(orderToPrint.getContactPhoneNumber());
        }

        if (invoiceNumberService == null) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "InvoiceNumberService nicht initialisiert.");
            logMessage("FEHLER: InvoiceNumberService ist null in handlePrintInvoice.");
            return;
        }
        long nextNum = invoiceNumberService.getNextInvoiceNumberSequence();
        String formattedInvoiceId = "NO" + nextNum; // Korrigiertes Format: NO1, NO2 etc.

        Invoice invoice = new Invoice(orderToPrint, customer, formattedInvoiceId);

        InvoicePrinter.printInvoiceText(invoice, logTextArea);
        logMessage("Rechnung " + formattedInvoiceId + " für Bestellung ID " + orderToPrint.getOrderId() + " gedruckt/Vorschau angezeigt.");
    }

    public static class DriverReportEntry {
        private final SimpleStringProperty driverName;
        private final SimpleIntegerProperty orderCount;
        private final SimpleDoubleProperty totalValue;
        public DriverReportEntry(String driverName, int orderCount, double totalValue) {
            this.driverName = new SimpleStringProperty(driverName);
            this.orderCount = new SimpleIntegerProperty(orderCount);
            this.totalValue = new SimpleDoubleProperty(totalValue);
        }
        public String getDriverName() { return driverName.get(); }
        public Integer getOrderCount() { return orderCount.get(); }
        public Double getTotalValue() { return totalValue.get(); }
    }

    @FXML private void handleShowDriverReport() {
        List<Order> allOrders = orderService.getAllOrders();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<Order> todaysOrders = allOrders.stream()
                .filter(order -> order.getOrderTimestamp().toLocalDate().equals(today))
                .collect(Collectors.toList());
        if (todaysOrders.isEmpty()) {
            logMessage("\n--- Kein Fahrerbericht für heute (" + today.format(dateFormatter) + "): Keine Bestellungen gefunden. ---");
            Dialogs.showAlert(Alert.AlertType.INFORMATION, "Fahrerbericht", "Keine Bestellungen für heute (" + today.format(dateFormatter) + ") gefunden.");
            return;
        }
        Map<String, List<Order>> ordersByDriver = todaysOrders.stream()
                .filter(order -> order.getDriverName() != null && !order.getDriverName().isEmpty() && !order.getDriverName().startsWith("0 -"))
                .collect(Collectors.groupingBy(Order::getDriverName));
        ObservableList<DriverReportEntry> reportEntries = FXCollections.observableArrayList();
        double grandTotalOrders = 0;
        double grandTotalValue = 0;
        if (ordersByDriver.isEmpty()) {
            logMessage("Keine Bestellungen mit zugewiesenem Fahrer für heute.");
        } else {
            for (Map.Entry<String, List<Order>> entry : ordersByDriver.entrySet()) {
                String driverName = entry.getKey();
                List<Order> driverOrders = entry.getValue();
                int numOrders = driverOrders.size();
                double totalValue = driverOrders.stream().mapToDouble(Order::getTotalAmount).sum();
                reportEntries.add(new DriverReportEntry(driverName, numOrders, totalValue));
                grandTotalOrders += numOrders;
                grandTotalValue += totalValue;
            }
        }
        StringBuilder textReport = new StringBuilder();
        textReport.append("\n--- Fahrer Tagesbericht für ").append(today.format(dateFormatter)).append(" ---\n");
        reportEntries.forEach(entry ->
                textReport.append(String.format("%-20s | %10d | %12.2f €\n", entry.getDriverName(), entry.getOrderCount(), entry.getTotalValue()))
        );
        textReport.append("--------------------------------------------------\n");
        textReport.append(String.format("%-20s | %10.0f | %12.2f €\n", "GESAMT ALLE FAHRER", grandTotalOrders, grandTotalValue));
        textReport.append("--------------------------------------------------\n");
        logMessage(textReport.toString());

        Stage reportStage = new Stage();
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.setTitle("Fahrer Tagesbericht - " + today.format(dateFormatter));
        TableView<DriverReportEntry> tableView = new TableView<>(reportEntries);
        TableColumn<DriverReportEntry, String> driverCol = new TableColumn<>("Fahrer");
        driverCol.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        driverCol.setPrefWidth(180);
        TableColumn<DriverReportEntry, Integer> countCol = new TableColumn<>("Anzahl Bestellungen");
        countCol.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        countCol.setPrefWidth(150);
        countCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        TableColumn<DriverReportEntry, Double> valueCol = new TableColumn<>("Gesamtwert");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        valueCol.setCellFactory(tc -> new TableCell<DriverReportEntry, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(currencyFormatter.format(item));
            }
        });
        valueCol.setPrefWidth(120);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        tableView.getColumns().addAll(driverCol, countCol, valueCol);
        tableView.setPlaceholder(new Label("Keine Daten für den Bericht vorhanden."));
        Label totalLabel = new Label(String.format("Gesamt: %.0f Bestellungen, Wert: %s", grandTotalOrders, currencyFormatter.format(grandTotalValue)));
        totalLabel.setFont(new Font("System Bold", 14));
        totalLabel.setPadding(new Insets(10));
        VBox layout = new VBox(10, tableView, totalLabel);
        layout.setPadding(new Insets(10));
        VBox.setVgrow(tableView, javafx.scene.layout.Priority.ALWAYS);
        Scene scene = new Scene(layout, 500, 400);
        reportStage.setScene(scene);
        reportStage.showAndWait();
    }

    private void initializeOldOrdersTable() {
        pastOrders = FXCollections.observableArrayList();
        oldOrdersTable.setItems(pastOrders);

        oldOrderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        oldOrderTimestampCol.setCellValueFactory(new PropertyValueFactory<>("orderTimestamp"));
        oldOrderTimestampCol.setCellFactory(column -> new TableCell<Order, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatter.format(item));
            }
        });

        oldOrderCustomerCol.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            String phoneNum = order.getContactPhoneNumber();
            if (phoneNum != null && !phoneNum.equals("Barverkauf")) {
                Contact contact = contactService.findContactByPhoneNumber(phoneNum);
                if (contact != null && contact.getName() != null && !contact.getName().isEmpty()) {
                    return new SimpleStringProperty(contact.getName() + " (" + phoneNum + ")");
                }
                return new SimpleStringProperty(phoneNum);
            }
            return new SimpleStringProperty("Barverkauf");
        });

        oldOrderDriverCol.setCellValueFactory(new PropertyValueFactory<>("driverName"));
        oldOrderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        oldOrderTotalCol.setCellFactory(tc -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText((empty || price == null) ? null : currencyFormatter.format(price));
            }
        });

        oldOrderActionsCol.setCellFactory(param -> new OldOrderActionCell());
        loadOldOrders();
    }

    private void loadOldOrders() {
        List<Order> allOrders = orderService.getAllOrders();
        allOrders.sort((o1, o2) -> {
            if (o1.getOrderTimestamp() == null && o2.getOrderTimestamp() == null) return 0;
            if (o1.getOrderTimestamp() == null) return 1;
            if (o2.getOrderTimestamp() == null) return -1;
            return o2.getOrderTimestamp().compareTo(o1.getOrderTimestamp());
        });
        pastOrders.setAll(allOrders);
        if (oldOrdersTable != null) oldOrdersTable.refresh();
    }

    private class OldOrderActionCell extends TableCell<Order, Void> {
        private final Button editButton = new Button("Bearbeiten");
        private final Button printButton = new Button("Drucken");
        private final Button deleteButton = new Button("Löschen");
        private final HBox pane = new HBox(5, editButton, printButton, deleteButton);

        OldOrderActionCell() {
            editButton.setOnAction(event -> handleEditOldOrder(getTableView().getItems().get(getIndex())));
            printButton.setOnAction(event -> handlePrintOldOrder(getTableView().getItems().get(getIndex())));
            deleteButton.setOnAction(event -> handleDeleteOldOrder(getTableView().getItems().get(getIndex())));
            pane.setAlignment(Pos.CENTER);
            editButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 6 3 6;");
            printButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 6 3 6;");
            deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 6 3 6; -fx-text-fill: red;");
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : pane);
        }
    }

    private void handleEditOldOrder(Order orderToEdit) {
        if (orderToEdit == null) {
            Dialogs.showAlert(Alert.AlertType.WARNING, "Auswahlfehler", "Keine Bestellung zum Bearbeiten ausgewählt.");
            return;
        }
        logMessage("Lade Bestellung ID: " + orderToEdit.getOrderId() + " zur Bearbeitung.");

        Order freshOrder = orderService.findOrderById(orderToEdit.getOrderId());
        if (freshOrder == null) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler beim Laden", "Bestellung mit ID " + orderToEdit.getOrderId() + " konnte nicht geladen werden.");
            return;
        }
        currentOrder = freshOrder;

        if (currentOrder.getContactPhoneNumber() != null && !currentOrder.getContactPhoneNumber().equals("Barverkauf")) {
            Contact contact = contactService.findContactByPhoneNumber(currentOrder.getContactPhoneNumber());
            if (contact != null) displayContact(contact);
            else {
                clearContactFields();
                phoneNumberField.setText(currentOrder.getContactPhoneNumber());
            }
        } else {
            clearContactFields();
            phoneNumberField.setText("Barverkauf");
        }

        currentOrderItems.setAll(new ArrayList<>(currentOrder.getOrderItemsRuntime()));

        deliveryChargeField.setText(String.format(Locale.GERMANY, "%.2f", currentOrder.getDeliveryCharge()));
        extraChargeOrderField.setText(String.format(Locale.GERMANY, "%.2f", currentOrder.getExtraCharge()));
        if(orderNotesField != null) orderNotesField.setText(currentOrder.getNotes());
        if (currentOrder.getDriverName() != null && !currentOrder.getDriverName().isEmpty()) {
            driverComboBox.getSelectionModel().select(currentOrder.getDriverName());
        } else {
            driverComboBox.getSelectionModel().selectFirst();
        }

        updateOrderTotals();
        switchToEditingOrderMode(currentOrder.getOrderId());
        printInvoiceButton.setDisable(false);
        printInvoiceButton.setUserData(currentOrder.getOrderId());
    }

    private void handlePrintOldOrder(Order orderToPrint) {
        if (orderToPrint == null) return;

        Order fullOrderToPrint = orderService.findOrderById(orderToPrint.getOrderId());
        if (fullOrderToPrint == null || fullOrderToPrint.getOrderItemsRuntime() == null || fullOrderToPrint.getOrderItemsRuntime().isEmpty()) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Bestellung zum Drucken nicht gefunden oder ist leer. ID: " + orderToPrint.getOrderId());
            return;
        }

        Contact customer = null;
        if (fullOrderToPrint.getContactPhoneNumber() != null && !fullOrderToPrint.getContactPhoneNumber().equals("Barverkauf")) {
            customer = contactService.findContactByPhoneNumber(fullOrderToPrint.getContactPhoneNumber());
        }

        if (invoiceNumberService == null) {
            Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "InvoiceNumberService nicht initialisiert.");
            logMessage("FEHLER: InvoiceNumberService ist null in handlePrintOldOrder.");
            return;
        }
        long nextNum = invoiceNumberService.getNextInvoiceNumberSequence();
        String formattedInvoiceId = "NO" + nextNum; // Korrigiertes Format: NO1, NO2 etc.
        Invoice invoice = new Invoice(fullOrderToPrint, customer, formattedInvoiceId);
        InvoicePrinter.printInvoiceText(invoice, logTextArea);
        logMessage("Rechnung " + formattedInvoiceId + " für alte Bestellung ID " + fullOrderToPrint.getOrderId() + " gedruckt.");
    }

    private void handleDeleteOldOrder(Order orderToDelete) {
        if (orderToDelete == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Bestellung ID: " + orderToDelete.getOrderId() + " wirklich löschen?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Bestellung löschen");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean success = orderService.deleteOrder(orderToDelete.getOrderId());
            if (success) {
                logMessage("Bestellung ID: " + orderToDelete.getOrderId() + " gelöscht.");
                loadOldOrders();
                if (editingOrderId != null && editingOrderId.equals(orderToDelete.getOrderId())) {
                    handleClearOrder();
                }
            } else {
                Dialogs.showAlert(Alert.AlertType.ERROR, "Fehler", "Bestellung konnte nicht gelöscht werden.");
            }
        }
    }
    public void setInvoiceNumberService(InvoiceNumberService invoiceNumberService) {
        this.invoiceNumberService = invoiceNumberService;
    }
}