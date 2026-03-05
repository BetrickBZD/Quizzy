package controller;

import model.Fragen;
import model.GameSessionData;
import model.Nutzer;

import java.util.List;

/**
 * Der Spiele Controller verwaltet den Spielablauf, die Punkteberechnung und die Interaktionen
 * zwischen den Spieldaten (GameSessionData) und der Benutzerverwaltung (NutzerController)
 * Er ist für das Starten, Beenden und auswerten von Spielrunden zuständig
 *
 * @author Farhan & Bayezid
 * @version 1.0
 */
public class SpieleController {

    /**
     * Daten der aktuellen Spielsitzung
     */
    private final GameSessionData gameSessionData;
    /**
     * Controller für die Benutzerverwaltung
     */
    private final NutzerController nutzerController;

    /**
     * Konstruktor für SpieleController
     * Erstellt einen neuen GameSessionData und einen neuen NutzerController
     */
    public SpieleController() {
        this.gameSessionData = new GameSessionData();
        this.nutzerController = new NutzerController();
    }

    /**
     * Startet eine neue Spielrunde
     * lädt die Fragen über den FragenController, mischt diese und setzt den aktuellen Punktestand auf 0
     */
    public void startnewGame(){
        gameSessionData.setFragenset(new FragenController().getLoadedQuestions());
        gameSessionData.shuffelFragenset();
        gameSessionData.setCurrentScore(0);
    }

    /**
     * Beendet das aktuelle Spiel und aktualisiert den Highscire des Nutzers,
     * falls der aktuelle Punktestand höher als die bisherige Highscre ist
     * Die Änderung werden über den NutzerController dauerhaft gespeichert
     * @param nutzer der Nutzer, dessen spiel beendet wird und dessen Highscore aktualisiert werden soll
     */
    public void endGame(Nutzer nutzer){
        if (gameSessionData.getCurrentScore() > nutzer.getHighscore()){
            nutzer.setHighscore(gameSessionData.getCurrentScore());
            // Update user in the controller to save to CSV
            List<Nutzer> allUsers = nutzerController.getAllNutzer();
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getName().equals(nutzer.getName())) {
                    nutzerController.updateNutzer(i, nutzer.getName(), nutzer.getHighscore(), nutzer.isAdmin());
                    break;
                }
            }
        }
    }

    /**
     * Gibt das fragen-Objekt an einem bestimmten Index zurück
     * @param index Der Index der gewünschten Frage in der Liste
     * @return Das Fragen-Objekt oder null, wenn der Index ungültig ist
     */
    public Fragen getFrageText(int index){
        if(gameSessionData.getFragenset() != null&& index < gameSessionData.getFragensetLength()) {
            return gameSessionData.getFragenset().get(index);
        }
        return null;
    }

    /**
     * Überprüft die Antwort eines Nutzers auf Richtigkeit ohne den Punktestand zu verändern
     * @param index Der Index der aktuellen Frage
     * @param selectedAnswer Der Index der vom Nutzer ausgewählten Antwort
     * @return true, wenn die Antwort richtig ist, ansonsten false
     */
    public boolean checkAnswer(int index, int selectedAnswer){
        if(gameSessionData.getFragenset() != null && index < gameSessionData.getFragensetLength()) {
            Fragen currentFrage = gameSessionData.getFragenset().get(index);
            if(currentFrage.getRichtigeAntwort() == selectedAnswer){
                return true;
            }
        }
        return false;
    }//ohne incrementierung des scores

    /**
     * Prüft, ob eine gewählte Antwort richtig ist und erhöht gegebenenfalls (bei Erfolg) den Punktestand
     * @param index Der Index der aktuellen Frage
     * @param selectedAnswer Der Index der vom Nutzer ausgewählten Antwort
     * @param reward Die Anzahl der Punkte, die bei einer richtigen Antwort vergeben werden sollen
     * @return true, wenn die Antwort richtig ist, ansonsten false
     */
    public boolean checkAnswer(int index, int selectedAnswer, int reward){
        if(gameSessionData.getFragenset() != null && index < gameSessionData.getFragensetLength()) {
            Fragen currentFrage = gameSessionData.getFragenset().get(index);
            if(currentFrage.getRichtigeAntwort() == selectedAnswer){
                gameSessionData.incrementCurrentScore(reward);
                return true;
            }
        }
        return false;
    }// mit reward

    /**
     * Zieht eine bestimmte Anzahl von dem aktuellen Punktestand ab
     * @param points Die Anzahl der Punkte, die abgezogen werden sollen
     */
    public void deductScore(int points) {
        gameSessionData.incrementCurrentScore(-points);
    }

    /**
     * Gibt die Anzahl der Fragen zurück
     * @return Die Anzahl der verfügbaren Fragen
     */
    public int gettotalefrage(){
        return gameSessionData.getFragensetLength();
    }

    /**
     * Gibt den aktuellen Punktestand zurück
     * @return Der aktuelle Punktestand
     */
    public int getcurrentscore(){
        return gameSessionData.getCurrentScore();
    }


}