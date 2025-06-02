package com.restaurant.crm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination; // Import for KeyCombination
import javafx.stage.Stage;
import com.restaurant.crm.controller.MainController;
import com.restaurant.crm.service.FritzBoxMonitor;
import com.restaurant.crm.service.InvoiceNumberService;

import java.io.IOException;
import java.util.Objects;

public class CallManagerApp extends Application {

    private FritzBoxMonitor fritzBoxMonitor;
    private InvoiceNumberService invoiceNumberService;

    @Override
    public void start(Stage primaryStage) throws IOException {
        invoiceNumberService = new InvoiceNumberService();

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/restaurant/crm/main_view.fxml")));
        Parent root = fxmlLoader.load();

        MainController controller = fxmlLoader.getController();
        controller.setInvoiceNumberService(invoiceNumberService);

        fritzBoxMonitor = new FritzBoxMonitor(controller);
        fritzBoxMonitor.startMonitoring();

        primaryStage.setTitle("Restaurant Call & Order Manager");

        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (fritzBoxMonitor != null) {
            fritzBoxMonitor.stopMonitoring();
        }
        if (invoiceNumberService != null) {
            invoiceNumberService.shutdownScheduler();
        }
        super.stop();
        System.out.println("Anwendung beendet.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}