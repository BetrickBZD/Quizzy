package controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import model.Nutzer;

/**
 * der NutzerController verwaltet die Benutzerdaten der Quiz-Anwendung
 * Er bietet Funktionen zum Erstellen, Aktualisieren, Löschen und Authentifizierung
 * von Nutzern und Speichern in einer CSV-Datei
 *
 * @author Farhan & Bayezid
 * @version 1.0
 */
public class NutzerController {

    /**
     * Pfad zur CSV-Datei in der die model.Nutzer gespeichert werden
     */
    private static final String CSV_FILE = "quizzycsv/nutzer.csv";

    /**
     * Liste der aktuell geladenen Nutzer
     */
    private List<Nutzer> loadedUsers;

    /**
     * Konstruktor für NutzerController
     * Erstellt notwendige Dateistrukturen und lädt die Nutzer initial aus der CSV-Datei
     */
    public NutzerController() {
        File file = new File(CSV_FILE);

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.loadedUsers = loadAllNutzer();
    }


    /**
     * Erstellt einen neuen Nutzer, fügt ihn zur Liste hinzu und speichert ihn in der CSV-Datei
     * @param name der Name des neuen Nutzers (darf kein Komma enthalten)
     * @param admin admin Wahrheitswet, ob der Nutzer Administrator-Rechte hat
     */
    public void createNutzer(String name, int highscore, boolean admin) {
        if (name.contains(",")) {
            System.out.println("Namen dürfen kein Komma enthalten!");
            return;
        }

        if (checkNutzer(name)) {
            System.out.println("Nutzer existiert bereits!");
            return;
        }

        Nutzer nutzer = new Nutzer(name, highscore, admin);
        loadedUsers.add(nutzer);
        saveAllNutzer();
    }


    /**
     * aktuelsiert die Daten eienes bestehenden Nutzers an einem bestimmten Index
     * @param index Position des Nutzers in der Liste
     * @param name neuer Name des Nutzers
     * @param highscore neuer Highscore des Nutzers
     * @param admin Neuer Administrator Status
     */
    public void updateNutzer(int index, String name, int highscore, boolean admin) {
        if (index >= 0 && index < loadedUsers.size()) {
            Nutzer n = loadedUsers.get(index);
            n.setName(name);
            n.setHighscore(highscore);
            n.setAdmin(admin);
            saveAllNutzer();
        }
    }


    /**
     * Entfernt einen Nutzer
     * @param index Der index des zu löschenden Nutzers in der Liste
     */
    public void deleteNutzer(int index) {
        if (index >= 0 && index < loadedUsers.size()) {
            loadedUsers.remove(index);
            saveAllNutzer();
        }
    }


    /**
     * Sucht einen Nutzer anhand seines Namens
     * @param name der gesuchte Nutzername (Groß- und Kleinschreibung wird ignoriert)
     * @return das Nutzer-Objekt bei Erfolg, sonst null
     */
    public Nutzer login(String name) {
        for (Nutzer user : loadedUsers) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Überprüft ob ein Nutzer mit dem angegebenen Namen existiert
     * @param name der zu prüfende Name
     * @return true, wenn der Nutzer existiert sonst false
     */
    public boolean checkNutzer(String name) {
        return login(name) != null;
    }


    /**
     * Gibt den Highscore eines Nutzers zurück
     * @param name Name des Nutzers
     * @return Highscore des Nutzers als String
     */
    public String getUserHighscore(String name) {
        for (Nutzer user : loadedUsers) {
            if (user.getName().equalsIgnoreCase(name)) {
                return String.valueOf(user.getHighscore());
            }
        }
        return "Nutzer nicht gefunden";
    }


    /**
     * gibt die Liste der Nutzer zurück
     * @return loadedUsers die Liste in der alle Nutzer gespeichert sind
     */
    public List<Nutzer> getAllNutzer() {
        return loadedUsers;
    }

    /**
     * Speichert din aktuellen Stand der Nutzerliste in die Nutzer.Csv-Datei
     * @throws IOException
     */
    private void saveAllNutzer() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            for (Nutzer nutzer : loadedUsers) {
                pw.println(
                        nutzer.getName() + "," +
                                nutzer.getHighscore() + "," +
                                nutzer.isAdmin()
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * lädt alle Nutzerdaten aus der Nutzer.csv-Datei
     * @return eine Liste der geladenen Nutzer-Objekte
     * @throws FileNotFoundException wenn die Datei nicht gefunden wird
     */
    private List<Nutzer> loadAllNutzer() {
        List<Nutzer> users = new ArrayList<>();
        File file = new File(CSV_FILE);

        if (!file.exists()) {
            return users;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    int highscore = 0;

                    try {
                        highscore = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException ignored) {}

                    boolean admin = Boolean.parseBoolean(parts[2].trim());
                    users.add(new Nutzer(name, highscore, admin));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return users;
    }
}
