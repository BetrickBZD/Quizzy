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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Die Klasse Quiz_UI verwaltet die grafische Benutzeroberfläche und die Logik einer Quiz-Runde.
 * <p>
 * Diese Klasse ist verantwortlich für die Anzeige der Fragen, die Verarbeitung von Nutzerantworten
 * über den SpieleController sowie die Steuerung des Spielverlaufs.
 * Zu den Kernfunktionen gehören eine Streak-Mechanik für Bonus-Punkte, ein automatischer
 * Weiterschalt-Timer nach Antworten und die Möglichkeit, Fragen zu überspringen.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
public class Quiz_UI {

    private JPanel panel1;
    private JProgressBar progressBar1;
    private JButton answer1;
    private JButton answer2;
    private JButton answer3;
    private JButton answer4;
    private JButton zurückButton;
    private JButton back;
    private JButton forward;
    private JLabel Spielername;
    private JLabel question;
    private JLabel progressLabel;
    private JLabel score;
    private JLabel comment;

    /**
     * Array zur einfachen Verwaltung der vier Antwort-Buttons.
     */
    private JButton[] answerButtons;
    /**
     * Der aktuell angemeldete Benutzer.
     */
    private Nutzer currentUser;
    /**
     * Der Controller, der die Spiellogik und Punktewertung steuert.
     */
    private SpieleController spieleController;
    /**
     * Der Index der Frage, die momentan angezeigt wird.
     */
    private int momentaneFrageindex;
    /**
     * Gibt an, ob die aktuell angezeigte Frage bereits vom Nutzer bearbeitet wurde.
     */
    private boolean isAnswered;

    /**
     * Eine Map, die den Index der Frage auf den Index der gewählten Antwort abbildet.
     * Ermöglicht das Wiederherstellen von Antworten beim Navigieren mit dem Zurück-Button.
     */
    private Map<Integer, Integer> userAnswers = new HashMap<>();

    /**
     * Timer für das automatische Weiterschalten zur nächsten Frage.
     */
    private Timer autoAdvanceTimer;

    /**
     * Zählt die Anzahl der korrekten Antworten in Folge für den Punkte-Bonus.
     */
    private int streakcount;

    /**
     * Der aktuelle Wert für die Anzeige des Weiterschalt-Countdowns.
     */
    private int countdownValue;

    /**
     * Konstruktor der Quiz_UI.
     * <p>
     * Initialisiert die Benutzeroberfläche, verknüpft die UI mit dem SpieleController
     * und bereitet das erste Fragenpaket vor. Erstellt zudem die Event-Listener
     * für die Antwort-Buttons und die Navigationssteuerung.
     *
     * @param nutzer Der Benutzer, der die Quiz-Runde startet.
     */
    public Quiz_UI(Nutzer nutzer) {
        this.currentUser = nutzer;
        Spielername.setText("Angemeldet als: " + nutzer.getName());

        this.spieleController = new SpieleController();

        // Initialisiere das Button-Array für einfacheren Zugriff
        this.answerButtons = new JButton[]{answer1, answer2, answer3, answer4};

        spieleController.startnewGame(); // Startet ein neues Spiel
        momentaneFrageindex = 0;
        loadQuestion(momentaneFrageindex);

        ActionListener antwortlistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isAnswered) return; // beendet den listener und Verhindert somit mehrfaches Antworten

                JButton source = (JButton) e.getSource();
                int selectedAnswer = -1;

                // Finde heraus, welcher Button geklickt wurde
                for (int i = 0; i < answerButtons.length; i++) {
                    if (source == answerButtons[i]) {
                        selectedAnswer = i;
                        break;
                    }
                }

                int reward = 1;
                int totalQuestions = spieleController.gettotalefrage();
                if (totalQuestions > 0 && streakcount >= (5)) {//streak tracker ist bei 5
                    reward = 2;
                }

                boolean correct = spieleController.checkAnswer(momentaneFrageindex, selectedAnswer, reward);
                userAnswers.put(momentaneFrageindex, selectedAnswer); // Store answer
                isAnswered = true;

                if (correct) {
                    source.setBackground(new Color(214, 223, 186));
                    streakcount++;
                    if (reward > 1) {
                        comment.setText("Richtig! (Streak Bonus: +" + reward + ")");
                    } else {
                        comment.setText("Richtig!");
                    }

                } else {
                    Fragen currentFrage = spieleController.getFrageText(momentaneFrageindex);
                    source.setBackground(new Color(255, 206, 220));
                    if (currentFrage != null && currentFrage.getRichtigeAntwort() >= 0 && currentFrage.getRichtigeAntwort() < answerButtons.length) {//noch mal check ob es wirklich richtig ist und eine momentane currentfrage gibt
                        answerButtons[currentFrage.getRichtigeAntwort()].setBackground(new Color(214, 223, 186));//setzt die eigendliche auf grün
                    }
                    comment.setText("Falsch!");
                    streakcount = 0;
                }
                score.setText("Score: " + spieleController.getcurrentscore());

                forward.setEnabled(true);
                startbuttonCountdown();
            }
        };

        // Listener zu allen AntwortButtons hinzufügen
        for (JButton btn : answerButtons) {
            btn.addActionListener(antwortlistener);
        }

        forward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToNextQuestion();
            }
        });

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoAdvanceTimer != null) {
                    autoAdvanceTimer.stop();
                }
                if (momentaneFrageindex > 0) {
                    momentaneFrageindex--;
                    loadQuestion(momentaneFrageindex);
                }
            }
        });

        zurückButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (autoAdvanceTimer != null) {
                    autoAdvanceTimer.stop();
                }
                MainFrame.switchToPanel(new Spiele_UI(currentUser).getMainPanel());
            }
        });
    }

    /**
     * Startet einen Countdown von drei Sekunden.
     * <p>
     * Nach Ablauf der Zeit wird automatisch die Methode zum Laden der nächsten
     * Frage aufgerufen. Der Fortschritt wird auf dem Vorwärts-Button visualisiert.
     */
    private void startbuttonCountdown() {
        countdownValue = 3;
        forward.setText("Weiter in " + countdownValue);

        if (autoAdvanceTimer != null && autoAdvanceTimer.isRunning()) {
            autoAdvanceTimer.stop();
        }

        autoAdvanceTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdownValue--;
                if (countdownValue > 0) {
                    forward.setText("Weiter in " + countdownValue);
                } else {
                    ((Timer) e.getSource()).stop();
                    goToNextQuestion();
                }
            }
        });
        autoAdvanceTimer.setRepeats(true);
        autoAdvanceTimer.start();
    }

    /**
     * Steuert den Übergang zur nächsten Frage.
     * <p>
     * Falls die Frage nicht beantwortet wurde (Überspringen), wird eine Punktstrafe
     * über den Controller abgezogen. Falls das Ende der Fragenliste erreicht ist,
     * wird das Spiel beendet.
     */
    private void goToNextQuestion() {
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
        }

        if (!isAnswered) {
            if (spieleController.getcurrentscore() < 2) {
                return;
            }
            // Überspringen: Hälfte der Punkte abziehen (hier 5 Punkte, da 10 Punkte pro Frage)
            spieleController.deductScore(2);
            userAnswers.put(momentaneFrageindex, -1); // Store skipped
            streakcount = 0;
        }

        momentaneFrageindex++;
        if (momentaneFrageindex < spieleController.gettotalefrage()) {
            loadQuestion(momentaneFrageindex);
        } else {
            finishGame();
        }
    }

    /**
     * Lädt die Daten einer Frage an einem bestimmten Index in die UI-Elemente.
     * <p>
     * Setzt Antworttexte, Farben und den Fortschrittsbalken. Wenn die Frage bereits
     * zuvor beantwortet wurde, wird der gewählte Zustand (Richtig/Falsch Markierung)
     * aus der userAnswers-Map wiederhergestellt.
     *
     * @param index Der Listenindex der zu ladenden Frage.
     */
    public void loadQuestion(int index) {
        back.setEnabled(index != 0);

        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
        }
        isAnswered = false;
        comment.setText("");
        forward.setEnabled(spieleController.getcurrentscore() >= 2);

        forward.setText("Überspringen");

        Fragen frage = spieleController.getFrageText(index);
        score.setText("Score: " + spieleController.getcurrentscore());
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

            // Restore previous answer state
            if (userAnswers.containsKey(index)) {
                int storedAnswer = userAnswers.get(index);
                isAnswered = true;
                forward.setText("Weiter");
                forward.setEnabled(true);

                if (storedAnswer != -1) {
                    boolean correct = (storedAnswer == frage.getRichtigeAntwort());
                    if (correct) {
                        answerButtons[storedAnswer].setBackground(new Color(214, 223, 186));
                        comment.setText("Richtig!");
                    } else {
                        answerButtons[storedAnswer].setBackground(new Color(255, 206, 220));
                        if (frage.getRichtigeAntwort() >= 0 && frage.getRichtigeAntwort() < answerButtons.length) {
                            answerButtons[frage.getRichtigeAntwort()].setBackground(new Color(214, 223, 186));
                        }
                        comment.setText("Falsch!");
                    }
                } else {
                    comment.setText("Übersprungen");
                }
            }

            int totalQuestions = spieleController.gettotalefrage();
            if (totalQuestions > 0) {
                progressBar1.setValue((int) (((double) (index) / totalQuestions) * 100));
                if (progressLabel != null) {
                    progressLabel.setText((index + 1) + "/" + totalQuestions);
                }
            } else {
                progressBar1.setValue(0);
                if (progressLabel != null) {
                    progressLabel.setText("0/0");
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
     * Schließt die aktuelle Quiz-Runde ab.
     * <p>
     * Stoppt alle aktiven Timer, aktualisiert den finalen Punktestand des Nutzers
     * im System und kehrt zur Spiele-Übersicht zurück.
     */
    private void finishGame() {
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
        }
        progressBar1.setValue(100);
        spieleController.endGame(currentUser);
        JOptionPane.showMessageDialog(panel1, "Spiel beendet! Dein Score: " + spieleController.getcurrentscore());
        MainFrame.switchToPanel(new Spiele_UI(currentUser).getMainPanel());
    }



    /**
     * Gibt das Haupt-Panel der Quiz-Benutzeroberfläche zurück.
     *
     * @return Das JPanel mit allen Spiel-Komponenten.
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
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1773569));
        panel1.setEnabled(true);
        panel1.setPreferredSize(new Dimension(700, 350));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(10, 10, 0, 10), -1, -1));
        panel2.setBackground(new Color(-1773569));
        panel2.setForeground(new Color(-22606));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setBackground(new Color(-1773569));
        panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        panel3.add(progressBar1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressLabel = new JLabel();
        progressLabel.setText("100/100");
        panel3.add(progressLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        score = new JLabel();
        score.setText("Score: nan");
        panel3.add(score, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel2.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        question = new JLabel();
        Font questionFont = this.$$$getFont$$$(null, -1, 24, question.getFont());
        if (questionFont != null) question.setFont(questionFont);
        question.setForeground(new Color(-13887279));
        question.setHorizontalAlignment(0);
        question.setHorizontalTextPosition(10);
        question.setText("Question this should not be empty");
        panel2.add(question, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 5, 0), -1, -1, true, false));
        panel4.setBackground(new Color(-1773569));
        panel2.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        answer1 = new JButton();
        answer1.setBackground(new Color(-12580));
        answer1.setEnabled(true);
        answer1.setFocusable(false);
        Font answer1Font = this.$$$getFont$$$(null, -1, -1, answer1.getFont());
        if (answer1Font != null) answer1.setFont(answer1Font);
        answer1.setForeground(new Color(-16579836));
        answer1.setHorizontalTextPosition(0);
        answer1.setText("Button");
        panel4.add(answer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer2 = new JButton();
        answer2.setBackground(new Color(-3418389));
        Font answer2Font = this.$$$getFont$$$(null, -1, -1, answer2.getFont());
        if (answer2Font != null) answer2.setFont(answer2Font);
        answer2.setForeground(new Color(-16579836));
        answer2.setText("Button");
        panel4.add(answer2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer3 = new JButton();
        answer3.setBackground(new Color(-2695238));
        Font answer3Font = this.$$$getFont$$$(null, -1, -1, answer3.getFont());
        if (answer3Font != null) answer3.setFont(answer3Font);
        answer3.setForeground(new Color(-16579836));
        answer3.setText("Button");
        panel4.add(answer3, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer4 = new JButton();
        answer4.setBackground(new Color(-3418389));
        Font answer4Font = this.$$$getFont$$$(null, -1, -1, answer4.getFont());
        if (answer4Font != null) answer4.setFont(answer4Font);
        answer4.setForeground(new Color(-16579836));
        answer4.setMargin(new Insets(0, 0, 0, 0));
        answer4.setText("Button");
        panel4.add(answer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel5.setBackground(new Color(-1773569));
        panel2.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        back = new JButton();
        back.setBackground(new Color(-3418389));
        Font backFont = this.$$$getFont$$$("Arial Black", -1, -1, back.getFont());
        if (backFont != null) back.setFont(backFont);
        back.setForeground(new Color(-16579836));
        back.setText("Zurück");
        panel5.add(back, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 40), null, 0, false));
        forward = new JButton();
        forward.setBackground(new Color(-3418389));
        forward.setFocusable(true);
        Font forwardFont = this.$$$getFont$$$("Arial Black", -1, -1, forward.getFont());
        if (forwardFont != null) forward.setFont(forwardFont);
        forward.setForeground(new Color(-16579836));
        forward.setText("Weiter");
        panel5.add(forward, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 40), null, 0, false));
        comment = new JLabel();
        comment.setHorizontalAlignment(0);
        comment.setText("Richtig");
        panel5.add(comment, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel1.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(5, 10, 10, 10), -1, -1, true, false));
        panel6.setBackground(new Color(-1773569));
        panel1.add(panel6, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel6.add(zurückButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-9406850));
        label1.setText("Quiz");
        panel6.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Spielername = new JLabel();
        Spielername.setForeground(new Color(-9406850));
        Spielername.setText("Angemeldet als: shouldnotbeempty");
        panel6.add(Spielername, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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