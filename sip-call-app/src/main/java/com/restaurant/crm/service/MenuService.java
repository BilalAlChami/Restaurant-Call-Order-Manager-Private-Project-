package com.restaurant.crm.service;

import com.restaurant.crm.model.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MenuService {

    private List<MenuItem> menuItems;

    public MenuService() {
        this.menuItems = new ArrayList<>();
        loadMenuItems();
    }

    private void loadMenuItems() {
        double taxFood = 0.07;
        double taxDrink = 0.19;

        menuItems.add(new MenuItem("0", "Spezial", "Extra Soße bestellen", 0.00, taxFood)); //
        menuItems.add(new MenuItem("A1", "Angebote", "Döner mit Pommes und Softdrink", 10.00, taxFood)); //
        menuItems.add(new MenuItem("A2", "Angebote", "Cheeseburger mit Pommes und Softdrink", 10.00, taxFood)); //
        menuItems.add(new MenuItem("A3", "Angebote", "Dürüm Döner mit Pommes und Softdrink", 10.00, taxFood)); //
        // Pizza in Angebote now uses 3 prices
        menuItems.add(new MenuItem("A4", "Angebote", "Pizza Döner mit Softdrink", 10.00, 18.00, 22.00, taxFood)); //

        menuItems.add(new MenuItem("1", "Döner", "Döner Kebap (Salat, Soße)", 6.50, taxFood)); //
        menuItems.add(new MenuItem("2", "Döner", "Döner Kebap mit Käse", 7.00, taxFood)); //
        menuItems.add(new MenuItem("3", "Döner", "Döner Vegetarisch (Salat, Soße, Weichkäse)", 5.50, taxFood)); //
        menuItems.add(new MenuItem("4", "Döner", "Dürüm Döner (Salat, Soße)", 7.00, taxFood)); //
        menuItems.add(new MenuItem("5", "Döner", "Dürüm Döner mit Käse", 7.50, taxFood)); //
        menuItems.add(new MenuItem("6", "Döner", "Dürüm Vegetarisch", 6.00, taxFood)); //
        menuItems.add(new MenuItem("7", "Döner", "Pomm Döner (Pommes oder Knoblauchkartoffeln)", 6.00, taxFood)); //
        menuItems.add(new MenuItem("8", "Döner", "Pomm Döner (Reis oder Bulgur)", 6.00, taxFood)); //
        menuItems.add(new MenuItem("9", "Döner", "Döner Teller (Pommes/Knoblauchkartoffeln, Salat, Soße)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("10", "Döner", "Döner Teller (Reis/Bulgur, Salat, Soße)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("11", "Döner", "Döner Teller (Nudeln, Salat, Soße)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("12", "Döner", "Hähnchen Teller (Champignons, Paprika)", 13.00, taxFood)); //
        menuItems.add(new MenuItem("13", "Döner", "Iskender Teller", 12.00, taxFood)); //
        menuItems.add(new MenuItem("14", "Lahmacun", "Lahmacun", 6.50, taxFood)); //
        menuItems.add(new MenuItem("15", "Lahmacun", "Lahmacun (Salat, Soße)", 7.50, taxFood)); //
        menuItems.add(new MenuItem("16", "Lahmacun", "Lahmacun (Dönerfleisch, Salat, Soße)", 8.00, taxFood)); //
        menuItems.add(new MenuItem("17", "Lahmacun", "Lahmacun Teller (geschnitten, Dönerfleisch, Salat, Soße)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("18", "Falafel", "Falafel Sandwich", 6.00, taxFood)); //
        menuItems.add(new MenuItem("19", "Falafel", "Falafel Dürüm", 7.00, taxFood)); //
        menuItems.add(new MenuItem("20", "Falafel", "Falafel Teller", 10.00, taxFood)); //
        menuItems.add(new MenuItem("21", "Pide", "Pide Käse", 7.00, taxFood)); //
        menuItems.add(new MenuItem("22", "Pide", "Pide Hackfleisch", 9.00, taxFood)); //
        menuItems.add(new MenuItem("23", "Pide", "Pide Spinat und Käse", 9.00, taxFood)); //
        menuItems.add(new MenuItem("24", "Pide", "Pide Dönerfleisch", 10.00, taxFood)); //
        menuItems.add(new MenuItem("25", "Pide", "Pide Sucuk und Ei", 9.00, taxFood)); //
        menuItems.add(new MenuItem("26", "Pide", "Pide Mozzarella mit Tomaten", 9.00, taxFood)); //
        menuItems.add(new MenuItem("S1", "Suppen", "Tagessuppe mit Brot", 5.00, taxFood)); //
        menuItems.add(new MenuItem("28", "Calzone", "Calzone Salami und Käse", 9.00, taxFood)); //
        menuItems.add(new MenuItem("29", "Calzone", "Calzone Schinken und Käse", 9.00, taxFood)); //
        menuItems.add(new MenuItem("30", "Calzone", "Calzone (Dönerfleisch, Pilzen, Zwiebeln, Käse)", 10.00, taxFood)); //
        menuItems.add(new MenuItem("31", "Calzone", "Calzone 98 (Schinken, Salami, Pilzen, Paprika, Döner, Käse)", 11.00, taxFood)); //
        menuItems.add(new MenuItem("32", "Flammkuchen", "Flammkuchen (Zwiebeln, Schinken)", 8.00, taxFood)); //
        menuItems.add(new MenuItem("33", "Flammkuchen", "Flammkuchen (Salami, Schinken, Pilzen)", 8.50, taxFood)); //
        menuItems.add(new MenuItem("34", "Flammkuchen", "Flammkuchen 98 (Dönerfleisch, Pilzen, Zwiebeln)", 8.50, taxFood)); //
        menuItems.add(new MenuItem("35", "Salate", "Gemischter Salat (Groß)", 7.00, taxFood)); //
        menuItems.add(new MenuItem("36", "Salate", "Käse Salat (Groß)", 7.50, taxFood)); //
        menuItems.add(new MenuItem("37", "Salate", "Hähnchen Salat (Groß)", 8.50, taxFood)); //
        menuItems.add(new MenuItem("38", "Salate", "Döner Salat (Groß)", 9.00, taxFood)); //
        menuItems.add(new MenuItem("39", "Salate", "Thunfischsalat (Groß)", 8.00, taxFood)); //
        menuItems.add(new MenuItem("40", "Salate", "98 Salat (Groß - Gemischt, Oliven, Schinken, Käse)", 9.00, taxFood)); //
        menuItems.add(new MenuItem("B1", "Burger", "Chicken Burger", 5.00, taxFood)); //
        menuItems.add(new MenuItem("B2", "Burger", "Chees Chicken Burger", 6.00, taxFood)); //
        menuItems.add(new MenuItem("B3", "Burger", "Döner Burger", 8.00, taxFood)); //
        menuItems.add(new MenuItem("41", "Snacks", "Hamburger", 5.00, taxFood)); //
        menuItems.add(new MenuItem("42", "Snacks", "Doppel Hamburger mit Pommes", 10.00, taxFood)); //
        menuItems.add(new MenuItem("43", "Snacks", "Cheeseburger", 6.00, taxFood)); //
        menuItems.add(new MenuItem("44", "Snacks", "Doppel Cheeseburger mit Pommes", 11.00, taxFood)); //
        menuItems.add(new MenuItem("45", "Snacks", "Pommes oder Knoblauchkartoffeln", 4.00, taxFood)); //
        menuItems.add(new MenuItem("46", "Snacks", "Portion Reis oder Bulgur", 4.50, taxFood)); //
        menuItems.add(new MenuItem("47", "Snacks", "Chicken Nuggets (6 Stk.)", 5.00, taxFood)); //
        menuItems.add(new MenuItem("48", "Snacks", "Chicken Nuggets (6 Stk.) mit Pommes", 8.00, taxFood)); //
        menuItems.add(new MenuItem("S02", "Snacks", "Mozzarella Sticks (6 Stk.)", 5.00, taxFood)); //
        menuItems.add(new MenuItem("49", "Schnitzel", "Putenschnitzel Wiener Art (Pommes, Salat)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("SC1", "Schnitzel", "Jägerschnitzel (Pommes, Salat)", 13.00, taxFood)); //
        menuItems.add(new MenuItem("SC2", "Schnitzel", "Rahmschnitzel (Pommes, Salat)", 13.00, taxFood)); //
        menuItems.add(new MenuItem("SC3", "Schnitzel", "Schinken-Sahne Schnitzel (Pommes, Salat)", 13.00, taxFood)); //
        menuItems.add(new MenuItem("SC4", "Schnitzel", "Bolognese Schnitzel (Pommes, Salat)", 13.00, taxFood)); //
        menuItems.add(new MenuItem("50", "Vorspeisen", "Sigara Böregi (5 Stk.)", 7.50, taxFood)); //
        menuItems.add(new MenuItem("V1", "Vorspeisen", "Sigara Böregi (1 Stk.)", 1.00, taxFood)); //
        menuItems.add(new MenuItem("51", "Vorspeisen", "Baklava (3 Stück)", 4.00, taxFood)); //
        menuItems.add(new MenuItem("V2", "Vorspeisen", "Baklava (1 Stück)", 1.50, taxFood)); //
        menuItems.add(new MenuItem("52", "Rollos", "98 Rollos (Dönerfleisch, Zwiebeln, Peperoni, Käse)", 9.00, taxFood)); //
        menuItems.add(new MenuItem("53", "Rollos", "Amigo Rollos (Schinken, Salami, Pilzen, Paprika, Käse)", 9.00, taxFood)); //

        // Pizza - Updated with three distinct prices: 30cm, Family, Party
        // Using constructor: MenuItem(id, category, name, price30cm, priceFamily, priceParty, taxRate)
        menuItems.add(new MenuItem("54", "Pizza", "Pizza Margherita",         7.50, 15.00, 22.00, taxFood)); //
        menuItems.add(new MenuItem("55", "Pizza", "Pizza Funghi",             8.50, 17.00, 24.00, taxFood)); //
        menuItems.add(new MenuItem("56", "Pizza", "Pizza Hawaii",             9.00, 18.00, 25.00, taxFood)); //
        menuItems.add(new MenuItem("57", "Pizza", "Pizza Mozzarella",         9.00, 18.00, 25.00, taxFood)); //
        menuItems.add(new MenuItem("58", "Pizza", "Pizza Tonno",              9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("59", "Pizza", "Pizza Rindersalami",       9.00, 18.00, 25.00, taxFood)); //
        menuItems.add(new MenuItem("60", "Pizza", "Pizza Putenschinken",      8.50, 17.00, 24.00, taxFood)); //
        menuItems.add(new MenuItem("61", "Pizza", "Pizza Peperoniwurst",      8.50, 17.00, 24.00, taxFood)); //
        menuItems.add(new MenuItem("62", "Pizza", "Pizza Europa (Schinken, Salami, Champ.)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("63", "Pizza", "Pizza Vegetarisch (Gemischtes Gemüse)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("64", "Pizza", "Pizza Sucuk Spezial (Sucuk, Peperoni, Zwiebel)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("65", "Pizza", "Pizza Sucuk",              9.00, 18.00, 25.00, taxFood)); //
        menuItems.add(new MenuItem("66", "Pizza", "Pizza Döner",              9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("67", "Pizza", "Pizza Mix (Salami, Schinken, Champ.)",9.00, 18.00, 25.00, taxFood)); //
        menuItems.add(new MenuItem("68", "Pizza", "Pizza Minas (Döner, Zwiebel, Peperoni, Hollandaise)", 10.00, 20.00, 27.00, taxFood)); //
        menuItems.add(new MenuItem("69", "Pizza", "Pizza Herbst (Spinat, Gorgonzola, Knoblauch)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("70", "Pizza", "Pizza Darmstadt (Döner, Champ., Zwiebel, Paprika)", 10.50, 21.00, 28.00, taxFood)); //
        menuItems.add(new MenuItem("71", "Pizza", "Pizza 98 (Döner, Peperoni, Zwiebel, fr. Tomaten, Schafskäse)", 11.00, 22.00, 29.00, taxFood)); //
        menuItems.add(new MenuItem("72", "Pizza", "Pizza Quattro Formaggi (4 Käsesorten)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("73", "Pizza", "Pizza Spezial (Alles drauf außer Fisch)", 11.50, 23.00, 30.00, taxFood)); //
        menuItems.add(new MenuItem("74", "Pizza", "Pizza Tonno Spezial (Thunfisch, Zwiebel, Oliven)", 10.00, 20.00, 27.00, taxFood)); //
        menuItems.add(new MenuItem("75", "Pizza", "Pizza Toscana (Schinken, Champ., Zwiebel, Ei)", 9.50, 19.00, 26.00, taxFood)); //
        menuItems.add(new MenuItem("76", "Pizza", "Pizza Frutti di Mare",     10.50, 21.00, 28.00, taxFood)); // Marco -> Frutti di Mare
        menuItems.add(new MenuItem("77", "Pizza", "Pizza Hähnchen (Hähnchenbrust, Broccoli, Hollandaise)", 10.00, 20.00, 27.00, taxFood)); // Mama Lucia -> Hähnchen
        menuItems.add(new MenuItem("78", "Pizza", "Pizza Scampi (Scampi, Knoblauch, fr. Tomaten)", 11.00, 22.00, 29.00, taxFood)); //

        menuItems.add(new MenuItem("81", "Überbacken", "Überbacken Pommes (Döner, Käse)", 12.00, taxFood)); //
        menuItems.add(new MenuItem("82", "Überbacken", "Überbacken Nudeln (Döner, Käse)", 12.00, taxFood)); //

        menuItems.add(new MenuItem("G1", "Getränke", "Cola 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G2", "Getränke", "Cola Zero 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G3", "Getränke", "Cola Light 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G4", "Getränke", "Fanta 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G5", "Getränke", "Sprite 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G6", "Getränke", "Mezzo Mix 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G7", "Getränke", "FANTA Exotic 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G8", "Getränke", "Uludag 0,33L", 2.00, taxDrink)); //
        menuItems.add(new MenuItem("G9", "Getränke", "Ayran 0,25L", 2.00, taxFood)); //
        menuItems.add(new MenuItem("G10", "Getränke", "Red Bull 0,25L", 3.00, taxDrink)); //
        menuItems.add(new MenuItem("G11", "Getränke", "Cola 1,0L", 3.00, taxDrink)); //
        menuItems.add(new MenuItem("G12", "Getränke", "Fanta 1,0L", 3.00, taxDrink)); //
        menuItems.add(new MenuItem("G13", "Getränke", "Sprite 1,0L", 3.00, taxDrink)); //
        menuItems.add(new MenuItem("G14", "Getränke", "Wasser 0,33L", 1.50, taxDrink));
        menuItems.add(new MenuItem("G15", "Getränke", "Wasser mit kohlensäure 0,33L", 1.50, taxDrink));
        menuItems.add(new MenuItem("E01", "Extras", "Extra Zusatzbelag (Allgemein)", 1.00, taxFood)); //
        menuItems.add(new MenuItem("E02", "Extras", "Extra Fleisch (Allgemein)", 2.00, taxFood)); //
    }

    public List<MenuItem> getAllMenuItems() { return new ArrayList<>(menuItems); } //
    public Map<String, List<MenuItem>> getMenuItemsByCategory() {
        return menuItems.stream().collect(Collectors.groupingBy(MenuItem::getCategory, TreeMap::new, Collectors.toList())); //
    }
    public MenuItem findMenuItemById(String id) {
        if (id == null || id.trim().isEmpty()) return null; //
        final String finalSearchId = id.trim().toUpperCase(); //
        return menuItems.stream().filter(item -> item.getId().toUpperCase().equals(finalSearchId)).findFirst().orElse(null); //
    }
}