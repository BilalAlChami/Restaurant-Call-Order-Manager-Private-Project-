package com.restaurant.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.restaurant.crm.model.Contact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ContactService {
    private static final String DATA_FILE = "contacts.json"; //
    private final ObjectMapper objectMapper; //
    private final ObservableList<Contact> contacts; //

    public ContactService() {
        this.objectMapper = new ObjectMapper(); //
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); //
        this.contacts = FXCollections.observableArrayList(); //
        loadContacts(); //
    }

    public ObservableList<Contact> getContacts() {
        return contacts; //
    }

    public void addOrUpdateContact(Contact contact) {
        boolean found = false;
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getPhoneNumber().equals(contact.getPhoneNumber())) {
                contacts.set(i, contact); //
                found = true;
                break;
            }
        }
        if (!found) {
            contacts.add(contact); //
        }
        saveContacts(); //
    }

    public void deleteContact(Contact contact) {
        contacts.remove(contact); //
        saveContacts(); //
    }

    public Contact findContactByPhoneNumber(String phoneNumber) {
        String normalizedPhoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
        return contacts.stream()
                .filter(c -> c.getPhoneNumber().replaceAll("[^0-9+]", "").equals(normalizedPhoneNumber))
                .findFirst()
                .orElse(null);
    }

    public List<Contact> findContactsByPhoneNumberPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return List.of(); //
        }
        String normalizedPrefix = prefix.replaceAll("[^0-9+]", "");
        return contacts.stream()
                .filter(c -> c.getPhoneNumber().replaceAll("[^0-9+]", "").startsWith(normalizedPrefix))
                .collect(Collectors.toList()); //
    }

    private void loadContacts() {
        File file = new File(DATA_FILE); //
        if (file.exists() && file.length() > 0) { //
            try {
                List<Contact> loadedContacts = objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Contact.class)); //
                contacts.addAll(loadedContacts); //
            } catch (IOException e) {
                System.err.println("Fehler beim Laden der Kontakte: " + e.getMessage()); //
            }
        } else if (!file.exists()) { //
            try {
                objectMapper.writeValue(file, FXCollections.emptyObservableList()); //
            } catch (IOException e) {
                System.err.println("Fehler beim Erstellen der initialen Kontaktdatenbank: " + e.getMessage()); //
            }
        }
    }

    private void saveContacts() {
        try {
            objectMapper.writeValue(new File(DATA_FILE), contacts); //
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Kontakte: " + e.getMessage()); //
        }
    }
    public void deleteAllData() {
        contacts.clear(); //
        saveContacts(); //
        System.out.println("Kontaktdaten gelöscht."); //
    }

    /**
     * Replaces all current contacts with the provided list and saves them to the default storage.
     * @param newContacts The new list of contacts to use.
     */
    public void replaceAllContacts(List<Contact> newContacts) {
        this.contacts.clear();
        if (newContacts != null) {
            this.contacts.addAll(newContacts);
        }
        saveContacts(); // Persist the new set to the default contacts.json
    }
}
