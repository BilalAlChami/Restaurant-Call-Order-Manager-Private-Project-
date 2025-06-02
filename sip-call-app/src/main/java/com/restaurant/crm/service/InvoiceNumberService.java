package com.restaurant.crm.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InvoiceNumberService {
    private static final String COUNTER_FILE_PATH = "global_invoice_counter.txt";
    private long currentInvoiceNumber;

    public InvoiceNumberService() {
        loadCounter();
        System.out.println("InvoiceNumberService (Global Counter) initialisiert. Aktueller Zählerstand (Basis): " + this.currentInvoiceNumber);
    }

    private synchronized void loadCounter() {
        Path path = Paths.get(COUNTER_FILE_PATH);
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                if (!lines.isEmpty() && !lines.get(0).trim().isEmpty()) {
                    this.currentInvoiceNumber = Long.parseLong(lines.get(0).trim());
                } else {
                    this.currentInvoiceNumber = 0; // Start from 0 if file is empty
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Fehler beim Laden des globalen Rechnungsnummern-Zählers: " + e.getMessage() + ". Starte bei 0.");
                this.currentInvoiceNumber = 0;
            }
        } else {
            this.currentInvoiceNumber = 0; // Start from 0 if file doesn't exist
            saveCounter(); // Create the file with initial value 0
        }
    }

    private synchronized void saveCounter() {
        Path path = Paths.get(COUNTER_FILE_PATH);
        try {
            Files.writeString(path, String.valueOf(this.currentInvoiceNumber));
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern des globalen Rechnungsnummern-Zählers: " + e.getMessage());
        }
    }

    public synchronized long getNextInvoiceNumberSequence() {
        this.currentInvoiceNumber++;
        saveCounter();
        return this.currentInvoiceNumber;
    }

    public synchronized void forceResetCounterNow() {
        this.currentInvoiceNumber = 0; // Reset to 0, so the next number generated will be 1
        saveCounter();
        System.out.println("Globaler Rechnungsnummern-Zähler manuell zurückgesetzt auf 0.");
    }

    public void shutdownScheduler() {
        // No scheduler is used in this simplified global counter version.
        System.out.println("InvoiceNumberService (Global Counter) hat keinen aktiven Scheduler zum Herunterfahren.");
    }
}