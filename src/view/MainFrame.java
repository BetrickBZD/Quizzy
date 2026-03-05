package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.Locale;

import controller.FragenController;
import model.Nutzer;

/**
 * Das MainFrame fungiert als zentraler Navigationspunkt und Hauptmenü der Anwendung.
 * Die Klasse verwaltet das Hauptfenster (JFrame) der Quizzy-App und ermöglicht den
 * dynamischen Wechsel zwischen verschiedenen Ansichten (Panels). Zudem implementiert
 * sie eine rollenbasierte Benutzeroberfläche, bei der bestimmte Funktionen (z.B. der
 * CSV-Editor) nur für Administratoren zugänglich sind.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
public class MainFrame {
    private JPanel panel1;
    private JLabel Quizzy;
    private JButton creditsButton;
    private JButton spielenButton;
    private JLabel Spielername;
    private JButton beendenButton;
    private JButton highscoreButton;
    private JButton CSVeditorbutton;

    /** Das statische Hauptfenster der gesamten Anwendung. */
    private static JFrame frame;
    /** Der aktuell angemeldete Benutzer dieser Session. */
    private Nutzer currentUser; // Temporär gespeicherter Nutzer

    /**
     * Initialisiert und konfiguriert das Hauptfenster (JFrame) der Anwendung.
     * Setzt Titel, Größe, Schließverhalten und sorgt für die Zentrierung auf dem Bildschirm.
     */
    public static void start() {
        frame = new JFrame("Quizzy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 350);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /**
     * Ersetzt den aktuellen Inhalt des Hauptfensters durch ein neues Panel.
     * Diese Methode dient der zentralen Navigation zwischen den verschiedenen UIs.
     *
     * @param newPanel Das JPanel, welches als neuer Inhalt angezeigt werden soll.
     */
    public static void switchToPanel(JPanel newPanel) {
        if (frame != null) {
            frame.setContentPane(newPanel);
            frame.revalidate();
            frame.repaint();
            frame.pack();
            // frame.setLocationRelativeTo(null); // Entfernt, damit die Position beibehalten wird
        }
    }

    /**
     * Konstruktor des MainFrame.
     * Initialisiert die Menüoberfläche für den übergebenen Nutzer. Hierbei wird:
     *
     *     Der Spielername im Label angezeigt.
     *     Die Sichtbarkeit des CSV-Editors basierend auf dem Admin-Status geprüft.
     *     Die Event-Behandlung für alle Menü-Buttons registriert.
     *     Ein MouseListener für die Logout-Funktionalität des Spielernamens erstellt.
     *
     * @param nutzer Der Nutzer, der sich aktuell angemeldet hat.
     */
    public MainFrame(Nutzer nutzer) {
        this.currentUser = nutzer;
        $$$setupUI$$$();

        if (currentUser != null) {
            Spielername.setText("Angemeldet als: " + currentUser.getName());
            // CSV Button nur sichtbar für Admins
            CSVeditorbutton.setVisible(currentUser.isAdmin());
        } else {
            CSVeditorbutton.setVisible(false);
        }

        beendenButton.addActionListener(new ActionListener() {                  //Beenden Button zum beenden
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }   //ohne error beenden gng
        });//beendenbutton

        creditsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //  JOptionPane.showMessageDialog(frame, "Credits:\nQuizzy App\nVersion 1.0", "Credits", JOptionPane.INFORMATION_MESSAGE);
                Credits_UI creditsUI = new Credits_UI(currentUser);
                switchToPanel(creditsUI.getMainPanel());
            }
        });//credits

        spielenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Spiele_UI spieleUI = new Spiele_UI(currentUser);
                switchToPanel(spieleUI.getMainPanel());
            }
        });

        CSVeditorbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CSVeditor csveditor = new CSVeditor(currentUser);//currentuser wird übergeben gng
                switchToPanel(csveditor.getMainPanel());
            }
        });

        highscoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Highscore_UI highscoreui = new Highscore_UI(currentUser);
                switchToPanel(highscoreui.getMainPanel());
            }
        });

        Spielername.addMouseListener(new MouseAdapter() {//zum Abmelden
            Font originalFont;

            /** Visuelles Feedback (Unterstreichen) beim Hovern */
            @Override
            public void mouseEntered(MouseEvent e) {
                originalFont = Spielername.getFont();
                Map attributes = originalFont.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                Spielername.setFont(originalFont.deriveFont(attributes));
                Spielername.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            /** Zurücksetzen des Fonts beim Verlassen des Labels */
            @Override
            public void mouseExited(MouseEvent e) {
                if (originalFont != null) {
                    Spielername.setFont(originalFont);
                }
                Spielername.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            /**
             * Liefert das Haupt-Panel des Menüs zurück.
             *
             * @return Das JPanel, welches die UI-Komponenten des Hauptmenüs enthält.
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(frame, "Möchten Sie sich abmelden?", "Abmelden", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    Login_UI loginUI = new Login_UI();
                    switchToPanel(loginUI.getMainPanel());
                }
            }
        });
    }

    /* Default Konstruktor für Kompatibilität, falls nötig (aber besser vermeiden)
    public MainFrame() {
        this(null);
    }
    */
    public JPanel getMainPanel() {
        return panel1;
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(10, 10, 10, 10), -1, -1, true, false));
        panel1.setBackground(new Color(-1773569));
        panel1.setEnabled(true);
        panel1.setPreferredSize(new Dimension(700, 350));
        panel1.setToolTipText("");
        panel1.putClientProperty("html.disable", Boolean.FALSE);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.setBackground(new Color(-1773569));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        Quizzy = new JLabel();
        Quizzy.setEnabled(true);
        Font QuizzyFont = this.$$$getFont$$$("Arial Black", -1, 36, Quizzy.getFont());
        if (QuizzyFont != null) Quizzy.setFont(QuizzyFont);
        Quizzy.setForeground(new Color(-13887279));
        Quizzy.setHorizontalAlignment(0);
        Quizzy.setHorizontalTextPosition(0);
        Quizzy.setText("Quizzy!");
        panel2.add(Quizzy, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(126, 122), null, 0, false));
        beendenButton = new JButton();
        beendenButton.setBackground(new Color(-12580));
        Font beendenButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, beendenButton.getFont());
        if (beendenButtonFont != null) beendenButton.setFont(beendenButtonFont);
        beendenButton.setForeground(new Color(-16579836));
        beendenButton.setText("Beenden");
        panel2.add(beendenButton, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        spielenButton = new JButton();
        spielenButton.setBackground(new Color(-3944740));
        Font spielenButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, spielenButton.getFont());
        if (spielenButtonFont != null) spielenButton.setFont(spielenButtonFont);
        spielenButton.setForeground(new Color(-16579836));
        spielenButton.setText("Spielen");
        panel2.add(spielenButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        highscoreButton = new JButton();
        highscoreButton.setBackground(new Color(-3944740));
        Font highscoreButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, highscoreButton.getFont());
        if (highscoreButtonFont != null) highscoreButton.setFont(highscoreButtonFont);
        highscoreButton.setForeground(new Color(-16579836));
        highscoreButton.setText("Highscore");
        panel2.add(highscoreButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setAlignmentX(0.0f);
        panel3.setBackground(new Color(-1773569));
        panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        creditsButton = new JButton();
        creditsButton.setBackground(new Color(-3944740));
        Font creditsButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, creditsButton.getFont());
        if (creditsButtonFont != null) creditsButton.setFont(creditsButtonFont);
        creditsButton.setForeground(new Color(-16579836));
        creditsButton.setHorizontalAlignment(0);
        creditsButton.setText("Credits");
        panel3.add(creditsButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        CSVeditorbutton = new JButton();
        CSVeditorbutton.setBackground(new Color(-3944740));
        Font CSVeditorbuttonFont = this.$$$getFont$$$("Arial Black", -1, -1, CSVeditorbutton.getFont());
        if (CSVeditorbuttonFont != null) CSVeditorbutton.setFont(CSVeditorbuttonFont);
        CSVeditorbutton.setForeground(new Color(-16579836));
        CSVeditorbutton.setText("CSV");
        panel3.add(CSVeditorbutton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(95, 40), null, 0, false));
        Spielername = new JLabel();
        Spielername.setForeground(new Color(-9406850));
        Spielername.setText("Angemeldet als: shouldnotbeempty");
        Spielername.setToolTipText("Abmelden");
        panel1.add(Spielername, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-9406850));
        label1.setText("MainMenü");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}