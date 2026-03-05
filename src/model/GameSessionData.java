package model;

import java.util.List;
import java.util.Collections;

/**
 * Die Klasse GameSessionData speichert den Zustand einer laufenden Quiz-Runde
 * Sie verwaltet die aktuellen Fragenliste sowie den momentanen Punktestand des Spielers
 *
 * @author Bayezid & Farhan
 * @version 1.0
 */
public class GameSessionData {
    /** Liste der Fragen für die aktuelle Spielrunde */
    private List<Fragen> fragenset;
    /** Aktueller Punktestand des Spielers */
    private int currentscore;


    //setter/getter für fragenset sowie randomizer

    /**
     * Gibt die Liste der Fragen zurück
     * @return Liste der Fragen
     */
    public List<Fragen> getFragenset() {
        return fragenset;
    }

    /**
     * Gibt die Länge der Fragenliste zurück
     * @return Länge der Fragenliste
     */
    public int getFragensetLength(){
        return fragenset.size();
    }

    /**
     * Setzt die Liste der Fragen
     * @param fragenset neue Liste der Fragen
     * @throws IllegalArgumentException wenn die Liste leer ist
     */
    public void setFragenset(List<Fragen> fragenset) {
        this.fragenset = fragenset;
    }

    /**
     * Mischt die Reihenfolge der Fragen in der Fragenliste
     */
    public void shuffelFragenset(){
        if(fragenset != null){
            Collections.shuffle(fragenset);
        }
    }

    //setter/getter für currentscore
    /**
     * Gibt den aktuellen Punktestand zurück
     * @return aktueller Punktestand
     */
    public int getCurrentScore() {
        return currentscore;
    }

    /**
     * Setzt den aktuellen Punktestand
     * @param currentscore neuer Punktestand
     */
    public void setCurrentScore(int currentscore) {
        this.currentscore = currentscore;
    }

    /**
     * Erhöht den aktuellen Punktestand um den angegebenen Wert
     * @param score zu erhöhenden Wert
     */
    public void incrementCurrentScore(int score) {this.currentscore += score;}
}