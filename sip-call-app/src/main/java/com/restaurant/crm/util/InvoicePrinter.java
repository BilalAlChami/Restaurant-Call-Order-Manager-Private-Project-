package com.restaurant.crm.util;

import com.restaurant.crm.model.Invoice;
import javafx.scene.control.TextArea; // Ensure this import is present

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class InvoicePrinter {

    public static void printInvoiceText(Invoice invoice, TextArea logArea) { // This is the key signature
        String invoiceText = invoice.generateInvoiceText(); //

        // Log to TextArea for preview
        if (logArea != null) { //
            logArea.appendText("\n--- RECHNUNG DRUCK (Vorschau) ---\n"); //
            logArea.appendText(invoiceText); //
            logArea.appendText("--- ENDE RECHNUNG DRUCK (Vorschau) ---\n"); //
        }

        PrinterJob job = PrinterJob.getPrinterJob(); //
        job.setPrintable(new Printable() { //
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException { //
                if (pageIndex > 0) { //
                    return NO_SUCH_PAGE; //
                }

                Graphics2D g2d = (Graphics2D) graphics; //
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY()); //

                // Use a monospaced font for better alignment of text columns
                Font font = new Font("Monospaced", Font.PLAIN, 10); //
                g2d.setFont(font); //

                String[] lines = invoiceText.split("\n"); //
                int y = 20; // Initial y position
                int lineHeight = g2d.getFontMetrics().getHeight(); //

                for (String line : lines) { //
                    g2d.drawString(line, 20, y); //
                    y += lineHeight; //
                }
                return PAGE_EXISTS; //
            }
        });

        boolean doPrint = job.printDialog(); //
        if (doPrint) { //
            try {
                job.print(); //
                if (logArea != null) logArea.appendText("Rechnung an Drucker gesendet.\n"); //
            } catch (PrinterException e) { //
                System.err.println("Fehler beim Drucken der Rechnung: " + e.getMessage()); //
                e.printStackTrace(); //
                if (logArea != null) logArea.appendText("Fehler beim Drucken: " + e.getMessage() + "\n"); //
                Dialogs.showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Druckfehler", "Rechnung konnte nicht gedruckt werden: " + e.getMessage()); //
            }
        } else {
            if (logArea != null) logArea.appendText("Druckvorgang abgebrochen.\n"); //
        }
    }
}