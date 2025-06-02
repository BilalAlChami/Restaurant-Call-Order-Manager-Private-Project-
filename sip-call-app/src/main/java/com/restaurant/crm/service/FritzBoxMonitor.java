package com.restaurant.crm.service;

import com.restaurant.crm.controller.MainController;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.IOException; // Import für IOException hinzugefügt

public class FritzBoxMonitor {
    private MainController mainController;
    private Thread monitorThread;
    private volatile boolean running = true;

    // Konfiguration für den FRITZ!Box Call Monitor
    private static final String FRITZ_BOX_IP = "192.168.178.1"; // Standard-IP, ggf. anpassen
    private static final int CALL_MONITOR_PORT = 1012;

    public FritzBoxMonitor(MainController mainController) {
        this.mainController = mainController;
    }

    public void startMonitoring() {
        running = true;
        monitorThread = new Thread(() -> {
            appendLog("FRITZ!Box Call Monitor (Socket) wird gestartet...");
            while (running) {
                try (Socket socket = new Socket(FRITZ_BOX_IP, CALL_MONITOR_PORT);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    appendLog("Verbunden mit FRITZ!Box CallMonitor auf " + FRITZ_BOX_IP + ":" + CALL_MONITOR_PORT + ". Warte auf Anrufe...");

                    String line;
                    while (running && (line = reader.readLine()) != null) {
                        appendLog("Empfangen von FRITZ!Box: " + line);
                        parseCallMonitorLine(line);
                    }
                } catch (IOException e) {
                    if (running) { // Nur loggen, wenn der Monitor noch laufen soll
                        appendLog("Fehler im FRITZ!Box Call Monitor (Socket): " + e.getMessage() + ". Versuche erneut in 10 Sek.");
                        // e.printStackTrace(); // Für detailliertere Fehlersuche
                        try {
                            Thread.sleep(10000); // 10 Sekunden warten vor dem nächsten Verbindungsversuch
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            running = false; // Schleife beenden, wenn unterbrochen
                        }
                    }
                } catch (Exception e) { // Fängt andere mögliche Fehler ab
                    if (running) {
                        appendLog("Unerwarteter Fehler im FRITZ!Box Call Monitor: " + e.getMessage());
                        e.printStackTrace();
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            running = false;
                        }
                    }
                }
            }
            appendLog("FRITZ!Box Call Monitor (Socket) gestoppt.");
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void parseCallMonitorLine(String line) {
        // Das Format der Zeile ist typischerweise:
        // Datum;CALL;ConnectionID;Nebenstelle;Angerufene MSN;Anrufer MSN;
        // Datum;RING;ConnectionID;Anrufer MSN;Angerufene MSN;
        // Datum;CONNECT;ConnectionID;Nebenstelle;Anrufer MSN;
        // Datum;DISCONNECT;ConnectionID;Dauer in Sek.;
        // Beispiel für eingehenden Anruf (RING):
        // 26.05.24 10:00:00;RING;0;0123456789;98765;SIP0;
        try {
            String[] parts = line.split(";");
            if (parts.length > 1) {
                String eventType = parts[1];

                if ("RING".equals(eventType) && parts.length >= 4) {
                    String callerNumber = parts[3]; // Die Nummer des Anrufers
                    // Manchmal sind Nummern wie "unknown" oder unterdrückt.
                    // Hier könnten Sie eine Validierung hinzufügen, ob es eine echte Nummer ist.
                    if (callerNumber != null && !callerNumber.isEmpty() && callerNumber.matches("[0-9+]+")) {
                        appendLog("📞 Eingehender Anruf von: " + callerNumber);
                        // UI-Update muss auf dem JavaFX Application Thread ausgeführt werden
                        Platform.runLater(() -> {
                            if (mainController != null) { // Sicherstellen, dass der Controller verfügbar ist
                                mainController.setIncomingPhoneNumber(callerNumber);
                            } else {
                                System.err.println("MainController ist null im FritzBoxMonitor!");
                            }
                        });
                    } else {
                        appendLog("Anruf von unterdrückter oder ungültiger Nummer: " + callerNumber);
                    }
                }
                // Hier könnten weitere Events wie CONNECT, DISCONNECT, CALL ausgewertet werden.
            }
        } catch (Exception e) {
            appendLog("Fehler beim Parsen der CallMonitor-Zeile: '" + line + "' - " + e.getMessage());
        }
    }

    public void stopMonitoring() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt(); // Thread unterbrechen, um ihn zu stoppen
            // Die Socket-Verbindung wird durch das Schließen im try-with-resources oder durch die Exception geschlossen.
        }
    }

    // Hilfsmethode zum Loggen, da die logTextArea im MainController ist
    // Diese Methode könnte an den MainController delegieren, wenn dieser eine Log-Methode hat
    private void appendLog(String message) {
        System.out.println("[FritzBoxMonitor] " + message); // Konsolenausgabe als Fallback
        if (mainController != null) {
            // Angenommen, MainController hat eine Methode zum Loggen,
            // die thread-sicher ist oder Platform.runLater intern verwendet.
            // Für dieses Beispiel wird es direkt versucht, aber es ist besser,
            // wenn MainController eine dedizierte appendLog-Methode hat.
            Platform.runLater(() -> mainController.logMessageFromExternal(message));
        }
    }
}
