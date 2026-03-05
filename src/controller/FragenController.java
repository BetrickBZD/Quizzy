package controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import model.Fragen;

/**
 * Der Fragen Controller ist für die Verwaltung der Quizfragen zuständig    <br>
 * Er übernimmt das Laden, Speichern, Erstellen und Löschen von Fragen in einer externen Csv Datei
 * @author Farhan & Bayezid
 * @version 1.0
 */
public class FragenController {

    /**
     * Pfad zur CSV-Datei in dr die Fragen gespeichert werden
     */
    private static final String CSV_FILE = "quizzycsv/fragen.csv";

    /**
     * Liste der aktuell geladenen Fragen
     */
    private List<Fragen> loadedQuestions;

    /**
     * Konstruktor für FragenController
     * überprüft ob die Existenz der Verzeichnisse und der CSV-Datei
     * Initialisiert die Liste durch Laden der vorhandenen Daten
     */
    public FragenController() {
        File file = new File(CSV_FILE);

        // erstellen des Verzeichnisses wenn nicht vorhanden
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // Erstellen der Datei, falls nicht vorhanden
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.loadedQuestions = loadAllQuestions();
    }

    /**
     * Erstellt eine neue Frage, fügt sie zur Liste hinzu und speichert sie in der CSV-Datei
     * @param frage Der Text der Fragestellung
     * @param antwort1 Erste Anwortsmöglichkeit
     * @param antwort2 Zweite Antwortmöglichkeit
     * @param antwort3 Dritte Antwortmöglichkeit
     * @param antwort4 Vierte Antwortmöglichkeit
     * @param richtigeAntwortIndex Index der richtigen Antwort (0-basiert)
     */
    public void createQuestion(String frage, String antwort1, String antwort2, String antwort3, String antwort4, int richtigeAntwortIndex) {
        int newId = loadedQuestions.size() + 1;
        String[] antworten = {antwort1, antwort2, antwort3, antwort4};
        Fragen neueFrage = new Fragen(newId, frage, antworten, richtigeAntwortIndex);
        this.loadedQuestions.add(neueFrage);
        saveAllQuestions();
    }

    /**
     * Aktualisiert eine vorhandene Frage in der Liste und speichert sie in der CSV-Datei
     * @param index index der Listenindex der zu aktualisierenden Frage
     * @param frage die neue Fragestellung
     * @param antwort1 neue Antwortmöglichkeit 1
     * @param antwort2 neue Antwortmöglichkeit 2
     * @param antwort3 neue Antwortmöglichkeit 3
     * @param antwort4 neue Antwortmöglichkeit 4
     * @param richtigeAntwortIndex neuer Index der richtigen Antwort (0-basiert)
     */
    public void updateQuestion(int index, String frage, String antwort1, String antwort2, String antwort3, String antwort4, int richtigeAntwortIndex) {
        if (index >= 0 && index < loadedQuestions.size()) {
            Fragen f = loadedQuestions.get(index);
            f.setFrage(frage);
            f.setAntwort(new String[]{antwort1, antwort2, antwort3, antwort4});
            f.setRichtigeAntwort(richtigeAntwortIndex);
            saveAllQuestions();
        }
    }

    /**
     * Löscht eine Frage aus der Liste und speichert sie in der CSV-Datei
     * @param index der Listenindex der zu löschenden Frage
     */
    public void deleteQuestion(int index) {
        if (index >= 0 && index < loadedQuestions.size()) {
            loadedQuestions.remove(index);
            saveAllQuestions();
        }
    }

    /**
     * Speichert alle in der Liste vorhandenen Fragen in die CSV-Datei
     * Die Indizes der richtigen antworten werden 1-basiert gespeichert
     */
    private void saveAllQuestions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            for (Fragen f : loadedQuestions) {
                String[] antworten = f.getAntwort();
                // Format: Frage,Antwort1,Antwort2,Antwort3,Antwort4,RichtigeAntwortIndex
                // Speichere 1-basiert für CSV (1 = Antwort 1, etc.)
                pw.println(f.getFrage() + "," +
                           antworten[0] + "," +
                           antworten[1] + "," +
                           antworten[2] + "," +
                           antworten[3] + "," +
                           (f.getRichtigeAntwort() + 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * lädt alle Fragen aus der CSV-Datei in eine Liste
     * @return eine Liste mit Objekten vom Typ Fragen
     */
    private List<Fragen> loadAllQuestions() {
        List<Fragen> questions = new ArrayList<>();
        File file = new File(CSV_FILE);
        if (!file.exists()) return questions;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int idCounter = 1;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // Split by comma
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String frage = parts[0].trim();
                    String[] antworten = {parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()};
                    int richtigeAntwort = 0;
                    try {
                        // Lese 1-basiert aus CSV und konvertiere zu 0-basiert für interne Verarbeitung
                        richtigeAntwort = Integer.parseInt(parts[5].trim()) - 1;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    questions.add(new Fragen(idCounter++, frage, antworten, richtigeAntwort));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Gib die Liste der aktuell geladenen Fragen zurück
     * @return Liste der Fragen
     */
    public List<Fragen> getLoadedQuestions() {
        return loadedQuestions;
    }
}
