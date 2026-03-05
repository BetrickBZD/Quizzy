package model;

/**
 * Die Klasse Fragen repräsentiert eine einzelne Frage im Quiz mit 4 Antwortsmöglichkeiten
 * Sie dient als Datenmodell innerhalb der Anwendung
 *
 * @author Farhan & Bayezid
 * @version 1.0
 */

public class Fragen {
    /** Eindeutige Identifikationsnummer der Frage */
    private int Id;
    /** Die Fragestellung Text */
    private String Frage;
    /** Die Antwortmöglichkeiten */
    private String[] Antwort;
    /** Die Richtige Antwort */
    private int RichtigeAntwort;

    /**
     * Gibt die ID der Frage zurück
     * @return die ID als Ganzzahl
     */
    public int getId() {
        return Id;
    }

    /**
     * Gibt die Fragestellung zurück
     * @return der Fragetext als String
     */
    public String getFrage() {
        return Frage;
    }

    /**
     * Gibt die Antwortmöglichkeiten zurück
     * @return die Antwortmöglichkeiten als String-Array
     */
    public String[] getAntwort() {
        return Antwort;
    }

    /**
     * Gibt die Richtige Antwort zurück
     * @return die Richtige Antwort als Ganzzahl Index für den ArrayString
     */
    public int getRichtigeAntwort() {
        return RichtigeAntwort;
    }

    /**
     * Setzt die Frage Fragestellung
     * @param frage neue Fragestellung
     */
    public void setFrage(String frage) { this.Frage = frage; }

    /**
     * Setzt die Antwortmöglichkeiten
     * @param antwort neue Antwortmöglichkeiten
     */
    public void setAntwort(String[] antwort) { this.Antwort = antwort; }

    /**
     * Setzt die Richtige Antwort
     * @param richtigeAntwort neue Richtige Antwort
     */
    public void setRichtigeAntwort(int richtigeAntwort) { this.RichtigeAntwort = richtigeAntwort; }

    /**
     * Konstruktor für Fragen
     * @param Id Die Id der Frage
     * @param Frage Die Fragestellung Text
     * @param Antwort Die Antwortmöglichkeiten
     * @param RichtigeAntwort Die Richtige Antwort
     */
    public Fragen(int Id, String Frage, String[] Antwort, int RichtigeAntwort) {
        this.Id = Id;
        this.Frage = Frage;
        this.Antwort = Antwort;
        this.RichtigeAntwort = RichtigeAntwort;
    }
}
