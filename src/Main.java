import view.MainFrame;
import view.Login_UI;
import javax.swing.UIManager;

/**
 * Die Klasse Main dient als zentraler Einstiegspunkt für die gesamte Quiz-Anwendung.
 *
 * Diese Klasse enthält die Hauptmethode (main), welche beim Programmstart vom
 * Java-Laufzeitsystem aufgerufen wird. Sie ist für die grundlegende Konfiguration
 * der Benutzeroberfläche und das Starten des Anmeldeprozesses verantwortlich.
 *
 * Aufgaben der Klasse:
 * - Festlegen des grafischen Erscheinungsbildes (Look and Feel).
 * - Initialisierung des Hauptfensters (MainFrame).
 * - Laden der ersten Programmansicht (Login_UI).
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
public class Main {

    /**
     * Die Hauptmethode zum Starten der Applikation.
     *
     * Der Ablauf beim Start umfasst:
     * 1. Versuch, das plattformübergreifende Java-Design (Metal) zu erzwingen,
     *    damit die App auf Windows, Mac und Linux identisch aussieht.
     * 2. Aufruf der statischen start-Methode des MainFrame, um das Fenster zu erstellen.
     * 3. Erzeugung der Login-Oberfläche.
     * 4. Einbettung des Login-Panels in das Hauptfenster.
     *
     * @param args Kommandozeilenargumente (werden in dieser Anwendung nicht verwendet).
     */
    public static void main(String[] args) {
        //Nutzer momentanangemeldet = new Nutzer();
        //Hauptfenster initialisieren

        try {
            // Erzwingt das plattformunabhängige Java-Standard-Design (Metal Look and Feel)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MainFrame.start();

        //LoginPanel erstellen
        Login_UI login = new Login_UI();

        //Login-Panel im Fenster anzeigen
        MainFrame.switchToPanel(login.getMainPanel());
    }
}
