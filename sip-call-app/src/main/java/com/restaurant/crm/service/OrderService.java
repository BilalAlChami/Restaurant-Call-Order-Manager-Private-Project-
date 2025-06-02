package com.restaurant.crm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurant.crm.model.Order;
import com.restaurant.crm.model.OrderItem;
import com.restaurant.crm.model.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderService {
    private static final String ORDERS_FILE = "orders.json";
    private final ObjectMapper objectMapper;
    private List<Order> orders;
    private final MenuService menuService;

    public OrderService(MenuService menuService) {
        this.menuService = menuService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.orders = new ArrayList<>();
        loadOrders();
    }

    public List<Order> getAllOrders() {
        // Stelle sicher, dass bei jedem Abruf die RuntimeItems korrekt sind
        // (obwohl loadOrders dies bereits tun sollte)
        orders.forEach(this::ensureOrderItemsAreLoaded);
        return new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        this.orders.add(0, order); // Neueste zuerst
        saveOrders();
    }

    public Order findOrderById(String orderId) {
        return orders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .map(this::ensureOrderItemsAreLoaded) // Wichtig für korrekte OrderItems beim Bearbeiten
                .orElse(null);
    }

    public void updateOrder(Order orderToUpdate) {
        Optional<Order> existingOrderOpt = orders.stream()
                .filter(o -> o.getOrderId().equals(orderToUpdate.getOrderId()))
                .findFirst();

        if (existingOrderOpt.isPresent()) {
            int index = orders.indexOf(existingOrderOpt.get());
            orderToUpdate.recalculateTotals(); // Sicherstellen, dass die Summen aktuell sind
            orders.set(index, orderToUpdate);
            saveOrders();
            System.out.println("OrderService: Bestellung ID " + orderToUpdate.getOrderId() + " aktualisiert.");
        } else {
            System.err.println("OrderService Fehler: Bestellung mit ID " + orderToUpdate.getOrderId() + " nicht gefunden für Update.");
        }
    }

    public boolean deleteOrder(String orderId) {
        boolean removed = orders.removeIf(order -> order.getOrderId().equals(orderId));
        if (removed) {
            saveOrders();
            System.out.println("OrderService: Bestellung ID " + orderId + " gelöscht.");
        } else {
            System.err.println("OrderService Fehler: Bestellung mit ID " + orderId + " nicht gefunden zum Löschen.");
        }
        return removed;
    }

    private void loadOrders() {
        File file = new File(ORDERS_FILE);
        if (file.exists() && file.length() > 0) {
            try {
                List<Order> loadedOrders = objectMapper.readValue(file, new TypeReference<List<Order>>() {});
                for (Order order : loadedOrders) {
                    ensureOrderItemsAreLoaded(order); // Stellt OrderItems und deren Preise wieder her
                }
                this.orders.addAll(loadedOrders);
            } catch (IOException e) {
                System.err.println("Fehler beim Laden der Bestellungen: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ANGEPASST: ensureOrderItemsAreLoaded
    private Order ensureOrderItemsAreLoaded(Order order) {
        // Nur neu laden, wenn runtimeItems leer sind, aber serialisierte Items existieren
        if ((order.getOrderItemsRuntime() == null || order.getOrderItemsRuntime().isEmpty()) &&
                order.getItems() != null && !order.getItems().isEmpty()) {

            List<OrderItem> runtimeItems = new ArrayList<>();
            for (Order.OrderItemSerializationWrapper wrapper : order.getItems()) {
                MenuItem menuItem = menuService.findMenuItemById(wrapper.getMenuItemId());
                if (menuItem != null) {
                    OrderItem orderItem = new OrderItem(menuItem, wrapper.getQuantity());
                    orderItem.setCustomizations(wrapper.getCustomizations());

                    // Wende die gespeicherten Preiskomponenten an
                    orderItem.basePriceProperty().set(wrapper.getBasePriceSnapshot());
                    orderItem.setSizeMultiplier(wrapper.getSizeMultiplierUsed());
                    orderItem.setCustomizationPriceAdjustment(wrapper.getCustomizationPriceAdjustmentUsed());
                    // Die itemPrice und totalPrice Properties im OrderItem werden durch Bindings automatisch aktualisiert.

                    runtimeItems.add(orderItem);
                } else {
                    System.err.println("OrderService: Konnte MenuItem nicht finden mit ID: " +
                            wrapper.getMenuItemId() + " für Bestellung " + order.getOrderId());
                }
            }
            order.setOrderItemsRuntime(runtimeItems); // Dies ruft auch recalculateTotals in Order auf
        } else if (order.getOrderItemsRuntime() != null && !order.getOrderItemsRuntime().isEmpty()) {
            // Wenn Runtime Items schon da sind, ggf. trotzdem Summen neu berechnen lassen,
            // um Konsistenz sicherzustellen, falls sich Logik geändert hat.
            order.recalculateTotals();
        }
        return order;
    }

    private void saveOrders() {
        try {
            objectMapper.writeValue(new File(ORDERS_FILE), orders);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Bestellungen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAllData() {
        orders.clear();
        saveOrders();
        System.out.println("Bestelldaten gelöscht.");
    }
}