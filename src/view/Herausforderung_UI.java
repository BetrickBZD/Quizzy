package view;

import controller.SpieleController;
import model.Fragen;
import model.Nutzer;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/**
 * Die Klasse Herausforderung_UI stellt die Benutzeroberfläche fpr den
 * "Herausforderungs-Modus" bereit
 * In diesem Modus spielt der Nutzer gegen die Zeit
 * Besonderheiten:
 * Zeitlimit basierend auf der Anzahl der Fragen
 * Richtige Antworten geben einen Zeitbonus
 * Falsche Antworten führen zu einem Zeitabzug
 * Das Spiel endet, wenn die Zeit abläuft oder alle Fragen beantwortet wurden
 *
 * @author bayezid & Farhan
 * @version 1.0
 */
public class Herausforderung_UI {
    // UI- Komponenten
    private JPanel panel1;
    /**
     * Fortschrittsbalken zur Anzeige der Zeit
     */
    private JProgressBar progressBar1;
    /**
     * Textanzeige der verbleibenden Sekunden
     */
    private JLabel timeleft;
    /**
     * Anzeige der aktuellen Fragestellung
     */
    private JLabel question;
    /**
     * Buttons zur Auswahl der Antwortmöglichkeiten
     */
    private JButton answer1;
    private JButton answer2;
    private JButton answer3;
    private JButton answer4;
    /**
     * Feedbackanzeige zur Richtigkeit der Antwort
     */
    private JLabel comment;
    /**
     * Button zum Abbrechen und Zurückkheren zum Spielmenü
     */
    private JButton zurückButton;
    /**
     * Anzeige des aktuellen Spielers
     */
    private JLabel Spielername;
    /**
     * Anzeige des aktuellen Scores
     */
    private JLabel score;

    /**
     * Array zur einfacheren Verwaltung der Antwort-Buttons
     */
    private JButton[] answerButtons;

    /**
     * Referenz auf den aktuell angemeldeten Nutzer
     */
    private Nutzer currentUser;
    /**
     * Controller zur Steuerung der Spiellogik und Punktezählung
     */
    private SpieleController spieleController;
    /**
     * Index der aktuell aktuell angezeigten Frage im Fragenset
     */
    private int momentaneFrageindex;
    /**
     * Flag zur Sperrung von Eingaben, während eine Antwort ausgewertet wird
     */
    private boolean isAnswered;
    /**
     * Swing-Timer für den Sekunden-Countdown
     */
    private Timer gameTimer;
    /**
     * verbliebende Zeit in Sekunden
     */
    private int timeRemaining;
    /**
     * Maximale verfügbare Zeit in Sekunden
     */
    private int maxTime;

    /**
     * Konstruktor der Herausforderung_UI
     * Initialisiert die UI, berechnet das Zeitlimit, startet ein neues Spielset
     * über den SpieleController und aktiviert den Countdown
     *
     * @param nutzer der aktuell angemeldete Nutzer
     */
    public Herausforderung_UI(Nutzer nutzer) {
        this.currentUser = nutzer;
        Spielername.setText("Angemeldet als: " + nutzer.getName());

        this.spieleController = new SpieleController();
        this.answerButtons = new JButton[]{answer1, answer2, answer3, answer4};

        spieleController.startnewGame();
        momentaneFrageindex = 0;

        // Calculate time: 2 seconds per question
        int totalQuestions = spieleController.gettotalefrage();
        maxTime = totalQuestions * 3;
        timeRemaining = maxTime;

        progressBar1.setMaximum(maxTime);
        progressBar1.setValue(timeRemaining);
        timeleft.setText(timeRemaining + "s");
        score.setText("Score: 0");

        loadQuestion(momentaneFrageindex);
        startGameTimer();

        ActionListener antwortlistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isAnswered) return;

                JButton source = (JButton) e.getSource();
                int selectedAnswer = -1;

                for (int i = 0; i < answerButtons.length; i++) {
                    if (source == answerButtons[i]) {
                        selectedAnswer = i;
                        break;
                    }
                }

                // Check answer: 2 points for correct
                boolean correct = spieleController.checkAnswer(momentaneFrageindex, selectedAnswer, 5);
                isAnswered = true;

                if (correct) {
                    source.setBackground(new Color(214, 223, 186));
                    comment.setText("Richtig! +2s");
                    timeRemaining += 2;
                    if (timeRemaining > maxTime) timeRemaining = maxTime;
                } else {
                    Fragen currentFrage = spieleController.getFrageText(momentaneFrageindex);
                    source.setBackground(new Color(255, 206, 220));
                    if (currentFrage != null && currentFrage.getRichtigeAntwort() >= 0 && currentFrage.getRichtigeAntwort() < answerButtons.length) {
                        answerButtons[currentFrage.getRichtigeAntwort()].setBackground(new Color(214, 223, 186));
                    }
                    comment.setText("Falsch! -1s");
                    timeRemaining -= 1;
                }
                updateTimeUI();
                score.setText("Score: " + spieleController.getcurrentscore());

                if (timeRemaining <= 0) {
                    finishGame();
                    return;
                }

                // Short delay before next question
                Timer delay = new Timer(1000, evt -> {
                    ((Timer) evt.getSource()).stop();
                    goToNextQuestion();
                });
                delay.setRepeats(false);
                delay.start();
            }
        };

        for (JButton btn : answerButtons) {
            btn.addActionListener(antwortlistener);
        }

        zurückButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameTimer != null) gameTimer.stop();
                MainFrame.switchToPanel(new Spiele_UI(currentUser).getMainPanel());
            }
        });
    }

    /**
     * Startet den Spiel-Timer, der jede Sekunde die verbliebende Zeit dekrementiert
     */
    private void startGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                updateTimeUI();
                if (timeRemaining <= 0) {
                    finishGame();
                }
            }
        });
        gameTimer.start();
    }

    /**
     * Aktualisiert die visuelle Darstellung der Zeit (Progressbar und Text)
     */
    private void updateTimeUI() {
        if (timeRemaining < 0) timeRemaining = 0;
        progressBar1.setValue(timeRemaining);
        timeleft.setText(timeRemaining + "s");
    }

    /**
     * Navigiert zur nächsten Frage im Set oder beendet das Spiel,
     * wenn alle Fragen beantwortet wurden.
     */
    private void goToNextQuestion() {
        momentaneFrageindex++;
        if (momentaneFrageindex < spieleController.gettotalefrage()) {
            loadQuestion(momentaneFrageindex);
        } else {
            finishGame();
        }
    }

    /**
     * Lädt die Daten einer Frage aus dem Controller und bereitet die UI-Komponenten
     * auf die Anzeige vor
     *
     * @param index Der Index der zu ladenden Frage
     */
    public void loadQuestion(int index) {
        isAnswered = false;
        comment.setText("");

        Fragen frage = spieleController.getFrageText(index);
        if (frage != null) {
            String text = frage.getFrage();
            int fontSize = 24;
            if (text.length() > 60) fontSize = 20;
            if (text.length() > 100) fontSize = 16;
            if (text.length() > 160) fontSize = 12;
            question.setText("<html><div style='width: 450px; text-align: center; font-size: " + fontSize + "px;'>" + text + "</div></html>");
            String[] antworten = frage.getAntwort();

            if (antworten != null && antworten.length >= 4) {
                for (int i = 0; i < answerButtons.length; i++) {
                    answerButtons[i].setText(formatAnswerText(antworten[i]));
                    answerButtons[i].setEnabled(true);
                    answerButtons[i].setBackground(new Color(203, 214, 235));
                }
            }
        }
    }
    /**
     * Formatiert den gegeben text für die answer buttons so das die buttons sich nicht verformen
     * @param text wird am Ende mit der richtigen Formatierung ausgegeben
     * @return String
     */
    private String formatAnswerText(String text) {
        if (text == null) return "";

        int fontSize = 12;
        int maxLength = 20;

        if (text.length() > 10) {
            fontSize = 10;
            maxLength = 40;
        }
        if (text.length() > 20) {
            fontSize = 9;
            maxLength = 50;
        }

        if (text.length() > maxLength) {
            text = text.substring(0, maxLength - 3) + "...";
        }
        return "<html><div style='width: 200px; text-align: center; font-size: " + fontSize + "px;'>" + text + "</div></html>";
    }


    /**
     * Beendet die Spielsitzung
     * Stoppt den Timer, speichert den Highscore über den Controller an und kehrt
     * nach einer Benachrichtigung zum Hauptmenü zurück
     */
    private void finishGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        progressBar1.setValue(0);
        timeleft.setText("0s");
        spieleController.endGame(currentUser);
        JOptionPane.showMessageDialog(panel1, "Herausforderung beendet! Dein Score: " + spieleController.getcurrentscore());
        MainFrame.switchToPanel(new MainFrame(currentUser).getMainPanel());
    }

    /**
     * Gibt das Haupt-Panel dieser Ansicht zurück
     *
     * @return das JPanel der Herausforderungs-UI
     */
    public JPanel getMainPanel() {
        return panel1;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1773569));
        panel1.setEnabled(true);
        panel1.setPreferredSize(new Dimension(700, 350));
        panel2.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 1, new Insets(10, 10, 0, 10), -1, -1));
        panel3.setBackground(new Color(-1773569));
        panel1.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-1773569));
        panel3.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Zeit:");
        panel4.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setForeground(new Color(-9381515));
        progressBar1.setString("0 %");
        progressBar1.setValue(100);
        panel4.add(progressBar1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeleft = new JLabel();
        timeleft.setText("XXXs");
        panel4.add(timeleft, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel3.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        question = new JLabel();
        Font questionFont = this.$$$getFont$$$(null, -1, 24, question.getFont());
        if (questionFont != null) question.setFont(questionFont);
        question.setForeground(new Color(-13887279));
        question.setHorizontalAlignment(0);
        question.setText("Question this should not be empty");
        panel3.add(question, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 5, 0), -1, -1, true, false));
        panel5.setBackground(new Color(-1773569));
        panel3.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        answer1 = new JButton();
        answer1.setBackground(new Color(-12580));
        answer1.setFocusable(false);
        Font answer1Font = this.$$$getFont$$$("Arial Black", -1, 12, answer1.getFont());
        if (answer1Font != null) answer1.setFont(answer1Font);
        answer1.setForeground(new Color(-16579836));
        answer1.setText("Button");
        panel5.add(answer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer2 = new JButton();
        answer2.setBackground(new Color(-3418389));
        Font answer2Font = this.$$$getFont$$$("Arial Black", -1, 12, answer2.getFont());
        if (answer2Font != null) answer2.setFont(answer2Font);
        answer2.setForeground(new Color(-16579836));
        answer2.setText("Button");
        panel5.add(answer2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer3 = new JButton();
        answer3.setBackground(new Color(-2695238));
        Font answer3Font = this.$$$getFont$$$("Arial Black", -1, 12, answer3.getFont());
        if (answer3Font != null) answer3.setFont(answer3Font);
        answer3.setForeground(new Color(-16579836));
        answer3.setText("Button");
        panel5.add(answer3, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer4 = new JButton();
        answer4.setBackground(new Color(-3418389));
        Font answer4Font = this.$$$getFont$$$("Arial Black", -1, 12, answer4.getFont());
        if (answer4Font != null) answer4.setFont(answer4Font);
        answer4.setForeground(new Color(-16579836));
        answer4.setHorizontalAlignment(0);
        answer4.setMargin(new Insets(0, 0, 0, 0));
        answer4.setText("Button");
        panel5.add(answer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        score = new JLabel();
        score.setText("Score: xxx");
        panel3.add(score, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setBackground(new Color(-1773569));
        panel3.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        comment = new JLabel();
        comment.setHorizontalAlignment(0);
        comment.setText("Richtig");
        panel6.add(comment, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel1.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(5, 10, 10, 10), -1, -1, true, false));
        panel7.setBackground(new Color(-1773569));
        panel1.add(panel7, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        zurückButton = new JButton();
        zurückButton.setBackground(new Color(-12580));
        zurückButton.setBorderPainted(true);
        zurückButton.setContentAreaFilled(false);
        zurückButton.setDefaultCapable(true);
        zurückButton.setDoubleBuffered(false);
        zurückButton.setEnabled(true);
        zurückButton.setFocusCycleRoot(true);
        zurückButton.setFocusPainted(true);
        zurückButton.setFocusable(true);
        Font zurückButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, zurückButton.getFont());
        if (zurückButtonFont != null) zurückButton.setFont(zurückButtonFont);
        zurückButton.setForeground(new Color(-16579836));
        zurückButton.setHideActionText(false);
        zurückButton.setOpaque(true);
        zurückButton.setText("Zurück zum Spielmenü");
        zurückButton.putClientProperty("hideActionText", Boolean.FALSE);
        panel7.add(zurückButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-9406850));
        label2.setText("Herausforderung");
        panel7.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Spielername = new JLabel();
        Spielername.setForeground(new Color(-9406850));
        Spielername.setText("Angemeldet als: shouldnotbeempty");
        panel7.add(Spielername, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

}