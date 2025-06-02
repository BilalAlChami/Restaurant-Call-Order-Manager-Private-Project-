package com.restaurant.crm.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomizationData {

    // Options-Format: "Anzeigetext :: TYP :: [RadioGruppenName] :: [ModifikatorOderWert]"

    // Bestehende Definitionen (gekürzt zur Übersicht, bitte Ihre volle Version beibehalten)
    public static final Map<String, List<String>> DOENER_OPTIONS = new LinkedHashMap<>();
    static {
        DOENER_OPTIONS.put("Soßen (Mehrfachauswahl)", Arrays.asList("Kräutersoße :: CHK", "Knoblauchsoße :: CHK", "Scharfe Soße :: CHK", "Cocktailsoße :: CHK", "Ohne Soße :: CHK"));
        DOENER_OPTIONS.put("Salat-Anpassungen (Weglassen)", Arrays.asList("Ohne Zwiebeln :: CHK", "Ohne Tomaten :: CHK", "Ohne grünen Salat :: CHK", "Ohne Rotkraut :: CHK", "Ohne Weißkraut :: CHK"));
        DOENER_OPTIONS.put("Extras", List.of("Extra Fleisch :: CHK_PRICE :: +2.00")); // Bezieht sich auf Dönerfleisch-Extra
        DOENER_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));
    }

    public static final Map<String, List<String>> FALAFEL_SAUCE_OPTIONS = new LinkedHashMap<>();
    static { FALAFEL_SAUCE_OPTIONS.put("Soßen (Mehrfachauswahl Falafel)", Arrays.asList("Knoblauchsoße :: CHK", "Joghurtsoße :: CHK", "Scharfe Soße :: CHK", "Cocktailsoße :: CHK", "Ohne Soße :: CHK")); }
    public static final Map<String, List<String>> FALAFEL_OPTIONS = new LinkedHashMap<>(FALAFEL_SAUCE_OPTIONS);
    static { FALAFEL_OPTIONS.put("Salat-Anpassungen (Weglassen)", DOENER_OPTIONS.get("Salat-Anpassungen (Weglassen)")); FALAFEL_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA")); }
    public static final Map<String, List<String>> LAHMACUN_OPTIONS = new LinkedHashMap<>(FALAFEL_SAUCE_OPTIONS); // Verwendet Falafel-Soßen
    static { LAHMACUN_OPTIONS.put("Salat-Anpassungen (Weglassen)", DOENER_OPTIONS.get("Salat-Anpassungen (Weglassen)")); LAHMACUN_OPTIONS.put("Extras für Lahmacun", List.of("Mit Dönerfleisch :: CHK_PRICE :: +1.50")); LAHMACUN_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));}

    // Optionen für Pomm Döner (Reis oder Bulgur) - ID "08"
    public static final Map<String, List<String>> POMM_DOENER_08_OPTIONS = new LinkedHashMap<>();
    static {
        POMM_DOENER_08_OPTIONS.put("Soßen (Mehrfachauswahl)", DOENER_OPTIONS.get("Soßen (Mehrfachauswahl)")); // Standard Döner Soßen
        POMM_DOENER_08_OPTIONS.put("Beilage wählen", Arrays.asList("Mit Reis :: RADIO :: BEILAGE_POMMDOENER08 :: DEFAULT", "Mit Bulgur :: RADIO :: BEILAGE_POMMDOENER08"));
        POMM_DOENER_08_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA")); // Feld für Extra
    }

    public static final Map<String, List<String>> TELLER_OPTIONS = new LinkedHashMap<>();
    static { TELLER_OPTIONS.put("Soßen (Mehrfachauswahl)", DOENER_OPTIONS.get("Soßen (Mehrfachauswahl)")); TELLER_OPTIONS.put("Beilage wählen (Teller)", Arrays.asList("Mit Pommes :: RADIO :: BEILAGE_TELLER :: DEFAULT", "Mit Reis :: RADIO :: BEILAGE_TELLER", "Mit Bulgur :: RADIO :: BEILAGE_TELLER")); TELLER_OPTIONS.put("Teller Extras", Arrays.asList("Extra Käse :: CHK_PRICE :: +1.00", "Extra Falafel (für Fal. Teller) :: CHK_PRICE :: +1.00", "Extra Dönerfleisch (für D. Teller) :: CHK_PRICE :: +2.00")); TELLER_OPTIONS.put("Notiz zur Zubereitung", List.of(" :: NOTE")); TELLER_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));}
    public static final List<String> STANDARD_EXTRA_TOPPINGS = Arrays.asList( "Broccoli :: CHK_PRICE :: +1.00", "Salami (Rind) :: CHK_PRICE :: +1.00", "Champignons :: CHK_PRICE :: +1.00", "Putenschinken :: CHK_PRICE :: +1.00", "Schafkäse :: CHK_PRICE :: +1.00", "Ei :: CHK_PRICE :: +1.00", "Spinat :: CHK_PRICE :: +1.00", "Sucuk (Knoblauchwurst) :: CHK_PRICE :: +1.00", "Paprika :: CHK_PRICE :: +1.00", "Zwiebeln :: CHK_PRICE :: +1.00", "Thunfisch :: CHK_PRICE :: +1.00", "Oliven (schwarz) :: CHK_PRICE :: +1.00", "Mais :: CHK_PRICE :: +1.00", "Jalapeños (scharf) :: CHK_PRICE :: +1.00", "Milde Peperoni :: CHK_PRICE :: +1.00", "Scharfe Peperoni :: CHK_PRICE :: +1.00", "Sauce Hollandaise :: CHK_PRICE :: +1.00", "Dönerfleisch :: CHK_PRICE :: +2.00");
    public static final Map<String, List<String>> PIDE_CALZONE_OPTIONS = new LinkedHashMap<>();
    static { PIDE_CALZONE_OPTIONS.put("Extra Beläge", STANDARD_EXTRA_TOPPINGS); PIDE_CALZONE_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));}
    public static final Map<String, List<String>> SAUCE_ORDER_OPTIONS = new LinkedHashMap<>();
    static { SAUCE_ORDER_OPTIONS.put("Soße auswählen (je 1,00€)", Arrays.asList( "Kräutersoße :: CHK_PRICE :: +1.00", "Knoblauchsoße :: CHK_PRICE :: +1.00", "Scharfe Soße :: CHK_PRICE :: +1.00", "Cocktailsoße :: CHK_PRICE :: +1.00", "Joghurtsoße :: CHK_PRICE :: +1.00", "Samuraisoße :: CHK_PRICE :: +1.00", "Andalousesoße :: CHK_PRICE :: +1.00")); SAUCE_ORDER_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));}
    public static final Map<String, List<String>> ANGEBOTE_OPTIONS = new LinkedHashMap<>(); // Allgemeine Optionen für Angebote
    static { ANGEBOTE_OPTIONS.put("Anmerkung zum Angebot", List.of(" :: NOTE")); ANGEBOTE_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));}

    // --- PIZZA OPTIONEN (Standard) ---
    public static final Map<String, List<String>> PIZZA_OPTIONS = new LinkedHashMap<>();
    static {
        PIZZA_OPTIONS.put("Größe wählen", Arrays.asList(
                "Standard (Ø 30cm) :: RADIO :: PIZZA_GROESSE :: USE_STD_PRICE :: DEFAULT",
                "Familie (Preis laut Karte) :: RADIO :: PIZZA_GROESSE :: FETCH_FAMILY_PRICE",
                "Party (Preis laut Karte) :: RADIO :: PIZZA_GROESSE :: FETCH_PARTY_PRICE"
        ));
        PIZZA_OPTIONS.put("Extra Beläge (Pizza)", STANDARD_EXTRA_TOPPINGS);
        PIZZA_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));
    }

    // --- NEU: Optionen für Döner Pizza (ID "66" und "A4") ---
    public static final Map<String, List<String>> DOENER_PIZZA_OPTIONS = new LinkedHashMap<>();
    static {
        DOENER_PIZZA_OPTIONS.put("Soßen (Mehrfachauswahl)", DOENER_OPTIONS.get("Soßen (Mehrfachauswahl)")); // Nur Soßenauswahl
        // Optional: DOENER_PIZZA_OPTIONS.put("Notiz zur Döner Pizza", List.of(" :: NOTE"));
    }

    // --- NEU: Optionen für Sigara Böreği (ID "50", "V1") ---
    public static final Map<String, List<String>> SIGARA_BOEREGI_OPTIONS = new LinkedHashMap<>();
    static {
        SIGARA_BOEREGI_OPTIONS.put("Soßen (Mehrfachauswahl)", DOENER_OPTIONS.get("Soßen (Mehrfachauswahl)")); // Standard Döner Soßen
        SIGARA_BOEREGI_OPTIONS.put("Beilage wählen", Arrays.asList(
                "Ohne Beilage :: RADIO :: BEILAGE_SIGARA :: DEFAULT",
                "Mit Reis :: RADIO :: BEILAGE_SIGARA",
                "Mit Bulgur :: RADIO :: BEILAGE_SIGARA"
        ));
        SIGARA_BOEREGI_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));
    }

    // --- NEU: Optionen für Schnitzel (ID "49", "SC1" - "SC4") ---
    public static final Map<String, List<String>> SCHNITZEL_OPTIONS = new LinkedHashMap<>();
    static {
        SCHNITZEL_OPTIONS.put("Beilage wählen", Arrays.asList(
                "Mit Pommes (Standard) :: RADIO :: BEILAGE_SCHNITZEL :: DEFAULT", // Pommes ist oft Standard
                "Mit Reis :: RADIO :: BEILAGE_SCHNITZEL",
                "Mit Bulgur :: RADIO :: BEILAGE_SCHNITZEL",
                "Mit Knoblauchkartoffeln :: RADIO :: BEILAGE_SCHNITZEL"
        ));
        // Optional: SCHNITZEL_OPTIONS.put("Soßen für Schnitzel", Arrays.asList("Ohne Soße :: CHK", "Extra Jägersoße :: CHK_PRICE :: +1.00", "Extra Rahmsoße :: CHK_PRICE :: +1.00"));
        SCHNITZEL_OPTIONS.put("Benutzerdefiniertes Extra", List.of(" :: CUSTOM_EXTRA"));
    }


    public static Map<String, List<String>> getOptionsFor(String itemIdOrCategory, MenuItem menuItem) {
        if (menuItem == null) return getOptionsForCategoryFallback(itemIdOrCategory);

        String id = menuItem.getId();
        String category = menuItem.getCategory().toLowerCase();

        // Spezifische IDs zuerst prüfen
        if ("0".equals(id)) return SAUCE_ORDER_OPTIONS;
        if ("A4".equals(id)) return DOENER_PIZZA_OPTIONS; // Pizza Döner im Angebot
        if ("66".equals(id)) return DOENER_PIZZA_OPTIONS; // Pizza Döner
        if (id.matches("^(01|02|03|04|05|06|07)$")) return DOENER_OPTIONS; // Standard Döner Varianten
        if ("08".equals(id)) return POMM_DOENER_08_OPTIONS; // Pomm Döner mit Reis/Bulgur
        if (id.matches("^(09|10|11|12|13|20)$")) return TELLER_OPTIONS; // Döner- und Falafelteller
        if (id.matches("^(50|V1)$")) return SIGARA_BOEREGI_OPTIONS; // Sigara Böreği
        if (id.equals("49") || id.startsWith("SC")) return SCHNITZEL_OPTIONS; // Alle Schnitzel

        // Fallback auf Kategorie, wenn keine spezifische ID passt
        if (category.equals("angebote")) return ANGEBOTE_OPTIONS; // Allgemeine Angebotsoptionen, wenn nicht spezifischer (wie A4)
        if (category.contains("lahmacun")) return LAHMACUN_OPTIONS;
        if (id.matches("^(18|19)$")) return FALAFEL_OPTIONS; // Falafel Sandwich/Dürüm (nicht Teller)
        if (category.contains("pide") || category.contains("calzone")) return PIDE_CALZONE_OPTIONS;
        if (category.contains("pizza")) return PIZZA_OPTIONS; // Standard Pizza Optionen für alle anderen Pizzen

        return Collections.emptyMap(); // Keine Optionen, wenn nichts zutrifft
    }

    // Fallback-Methode, falls menuItem null ist (sollte selten vorkommen)
    private static Map<String, List<String>> getOptionsForCategoryFallback(String categoryKey) {
        String k = categoryKey.toLowerCase();
        if (k.equals("angebote")) return ANGEBOTE_OPTIONS;
        if (k.contains("döner") && k.contains("teller")) return TELLER_OPTIONS; // Z.B. wenn Kategorie "Dönerteller"
        if (k.contains("falafel") && k.contains("teller")) return TELLER_OPTIONS;
        if (k.contains("döner")) return DOENER_OPTIONS;
        if (k.contains("falafel")) return FALAFEL_OPTIONS;
        if (k.contains("lahmacun")) return LAHMACUN_OPTIONS;
        if (k.contains("pide") || k.contains("calzone")) return PIDE_CALZONE_OPTIONS;
        if (k.contains("pizza")) return PIZZA_OPTIONS; // Standard Pizza, wenn als Kategorie übergeben
        if (k.contains("schnitzel")) return SCHNITZEL_OPTIONS;
        if (k.contains("vorspeisen") && (k.contains("sigara") || k.contains("böregi"))) return SIGARA_BOEREGI_OPTIONS;


        return Collections.emptyMap();
    }
}