package model;

import java.util.List;

/**
 * Die Klasse Nutzer repräsentiert einen Teilnehmer am Quiz-Spiel
 * Sie speichert grundlegende Benutzerinformationen wie den Namen,
 * den persönlichen Highscore sowie Berechtigungsstufen (Admin-Status)
 */
public class Nutzer {
    /** Name des Nutzers */
    private String name;
    /** Highscore des Nutzers */
    private int highscore;
    /** Admin-Status des Nutzers */
    private boolean admin;
    /** Liste der Fragen des Spielers */
    private List<Fragen> fragen;

    /**
     * Konstruktor für Nutzer
     * @param name Name des Nutzers
     * @param highscore Highscore des Nutzers
     * @param admin Admin-Status des Nutzers
     */
    public Nutzer(String name, int highscore, boolean admin) {
        this.name = name;
        this.highscore = highscore;
        this.admin = admin;
    }

    /**
     * Gibt den Namen des Nutzers zurück
     * @return Name des Nutzers als String
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt den Highscore des Nutzers zurück
     * @return Highscore des Nutzers als Ganzzahl
     */
    public int getHighscore() {
        return highscore;
    }

    /**
     * Gibt den Admin-Status des Nutzers zurück
     * @return Admin-Status des Nutzers als Wahrheitswert
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Setzt den Highscore des Nutzers
     * @param highscore neuer Highscore
     */
    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }

    /**
     * Setzt den Admin-Status des Nutzers
     * @param admin neuer Admin-Status
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    /**
     * Setzt den Namen des Nutzers
     * @param name neuer Name
     */
    public void setName(String name){
        this.name = name;
    }
}
