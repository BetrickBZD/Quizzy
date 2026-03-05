package test.controller;

import controller.NutzerController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Die Klasse NutzerControllerTestGetHighscore stellt Testfälle bereit, um die
 * korrekte Rückgabe von Highscores durch den NutzerController zu überprüfen.
 *
 * Getestet wird das Verhalten bei existierenden sowie bei nicht im System
 * hinterlegten Nutzern.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
public class NutzerControllerTestGetHighscore {

    /**
     * Prüft, ob der Highscore eines existierenden Nutzers korrekt zurückgegeben wird.
     * Ein Nutzer wird mit einem spezifischen Highscore (42) angelegt. Der Test
     * verifiziert, dass die Abfrage genau diesen Wert als Zeichenkette liefert.
     *
     * Erwartetes Ergebnis: "42"
     */
    @Test
    @DisplayName("GetUserHighscore wenn Nutzer existiert")
    void testGetUserHighscore_existingUser() {
        NutzerController controller = new NutzerController();
        String name = "TEstUSer"+ System.currentTimeMillis();
        controller.createNutzer(name,42, false);

        String result = controller.getUserHighscore(name);

        assertEquals("42", result);
        controller.deleteNutzer(controller.getAllNutzer().size()-1);

    }

    /**
     * Prüft die Rückgabe der Methode, wenn ein Highscore für einen nicht
     * existierenden Nutzernamen abgefragt wird.
     * Es wird ein zufälliger Name generiert, der nicht im System registriert ist.
     * Die Methode sollte in diesem Fall eine entsprechende Fehlermeldung zurückgeben.
     *
     * Erwartetes Ergebnis: "Nutzer nicht gefunden"
     */
    @Test
    @DisplayName("GetUserHighscore wenn Nutzer nicht existiert")
    void testGetUserHighscore_nonExistingUser() {
        NutzerController controller = new NutzerController();

        String result = controller.getUserHighscore("Test_User"+ System.currentTimeMillis());
        assertEquals("Nutzer nicht gefunden", result);

    }

    @Test
    @DisplayName("Leerer Name")
    void testGetUserHighscore_emptyName() {
        NutzerController controller = new NutzerController();

        String result = controller.getUserHighscore("");
        assertEquals("Nutzer nicht gefunden", result);

    }
}
