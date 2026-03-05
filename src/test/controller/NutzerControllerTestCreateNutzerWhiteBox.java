package test.controller;
import controller.NutzerController;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Die Klasse NutzerControllerTestCreateNutzerWhiteBox führt White-Box-Tests für die
 * Methode createNutzer des NutzerController durch.
 *
 * Im Gegensatz zum Blackbox-Test werden hier gezielt interne Validierungslogiken
 * (wie das Verbot von Kommata oder die Duplikatsprüfung) überprüft.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */

public class NutzerControllerTestCreateNutzerWhiteBox {

    NutzerController controller;

    /**
     * Initialisiert vor jedem Testlauf eine neue Instanz des NutzerControllers,
     * um eine saubere Testumgebung (Isolation) zu gewährleisten.
     */
    @BeforeEach
    void setUp() {
        controller = new NutzerController();
    }

    /**
     * Überprüft, ob die Methode die Erstellung eines Nutzers ablehnt,
     * wenn der Name ein ungültiges Zeichen (Komma) enthält.
     * Ein Komma ist oft aufgrund von CSV-Speicherformaten innerhalb der
     * internen Logik verboten.
     *
     * Erwartetes Ergebnis: Die Anzahl der Nutzer im System bleibt unverändert.
     */
    @Test
    @DisplayName("Name enthält Komma → Nutzer wird NICHT erstellt")
    void testCreateNutzerWithComma() {
        int vorher = controller.getAllNutzer().size();
        String name = "Test_User,"+ System.currentTimeMillis();

        controller.createNutzer(name,  0,false);

        assertEquals(vorher, controller.getAllNutzer().size());

    }

    /**
     * Testet die interne Duplikatsprüfung des Controllers.
     * Wenn ein Nutzername bereits im System existiert, darf kein zweiter
     * Nutzer mit identischem Namen angelegt werden.
     *
     * Erwartetes Ergebnis: Nach dem zweiten Erstellungsversuch erhöht sich
     * die Liste der Nutzer nicht.
     */
    @Test
    @DisplayName("Nutzer existiert bereits → kein Duplikat")
    void testCreateExistingNutzer() {
        String name = "Test_User"+ System.currentTimeMillis();
        controller.createNutzer(name, 0, false);
        int vorher = controller.getAllNutzer().size();

        controller.createNutzer(name,  0,true);

        assertEquals(vorher, controller.getAllNutzer().size());
        controller.deleteNutzer(controller.getAllNutzer().size()-1);
    }

    /**
     * Überprüft den Erfolgsfall beim Anlegen eines gültigen Nutzers.
     * Es wird sichergestellt, dass die Liste um eins wächst und der Nutzer
     * über die checkNutzer-Abfrage auffindbar ist. Am Ende wird
     * der erstellte Nutzer zur Bereinigung wieder entfernt.
     *
     * Erwartetes Ergebnis: Liste vergrößert sich um 1, Nutzer ist vorhanden.
     */
    @Test
    @DisplayName("Gültiger neuer Nutzer wird angelegt")
    void testCreateValidNutzer() {
        int vorher = controller.getAllNutzer().size();
        String Useerr = "Test_User"+ System.currentTimeMillis();

        controller.createNutzer(Useerr,  0,true);

        assertEquals(vorher +1 , controller.getAllNutzer().size());
        assertTrue(controller.checkNutzer(Useerr));
        controller.deleteNutzer(controller.getAllNutzer().size()-1);
    }
}
