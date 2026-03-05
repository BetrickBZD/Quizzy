package test.controller;


import controller.NutzerController;
import model.Nutzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * NutzerControllerLoginBlackboxTest ist eine Testklasse, die die Login-Funktionalität
 * der Klasse NutzerController mittels Blackbox-Tests überprüft.
 *
 * Es wird getestet, ob das System korrekt auf existierende und nicht existierende
 * Nutzerprofile reagiert.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
class NutzerControllerLoginBlackboxTest {

    /**
     * Testet den Login-Vorgang für einen Nutzer, der zuvor im System angelegt wurde.
     * Der Test erstellt einen Nutzer mit einem zeitabhängigen, eindeutigen Namen,
     * ruft die Login-Methode auf und prüft, ob ein gültiges Nutzer-Objekt
     * zurückgegeben wird.
     *
     * Erwartetes Ergebnis: Das Resultat des Logins ist nicht null.
     */
    @Test
    @DisplayName("Login mit existierenden Nutzer")
    void testLogin_existingUser() {
        NutzerController controller = new NutzerController();
        String name = "TestUser" + System.currentTimeMillis();
        controller.createNutzer(name, 10, false);

        Nutzer result = controller.login(name);

        assertNotNull(result);
        controller.deleteNutzer(controller.getAllNutzer().size()-1);
    }

    /**
     * Testet den Login-Vorgang für einen Namen, der nicht im System registriert ist.
     * Der Test versucht, sich mit einem zufällig generierten Namen einzuloggen,
     * der nicht zuvor erstellt wurde.
     *
     * Erwartetes Ergebnis: Das Resultat des Logins ist null.
     */
    @Test
    void testLogin_nonExistingUser() {
        NutzerController controller = new NutzerController();

        Nutzer result = controller.login("TestUser" + System.currentTimeMillis());

        assertNull(result);
    }


}