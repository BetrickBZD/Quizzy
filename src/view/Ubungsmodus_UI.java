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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Die Klasse Ubungsmodus_UI stellt die Benutzeroberfläche für den Lern- bzw. Übungsmodus bereit.
 * <p>
 * Im Gegensatz zum normalen Quiz-Modus liegt der Fokus hier auf der Wiederholung:
 * Falsch beantwortete Fragen werden in einer separaten Liste gesammelt und dem
 * Nutzer erneut vorgelegt, bis dieser sie korrekt beantwortet hat. Erst wenn alle
 * Fragen mindestens einmal richtig gelöst wurden, gilt die Übung als beendet.
 * <p>
 * Besonderheiten:
 * - Fragen werden beim Start gemischt (Shuffle).
 * - Segmentierte Fortschrittsanzeige (Aufgeteilt in: Erledigt, Offen, Zu Wiederholen).
 * - Automatisches Weiterschalten nach einer Antwort mittels Countdown-Timer.
 * - Phasen-System: Wechselt zwischen dem Durcharbeiten neuer Fragen und dem Wiederholen falscher Antworten.
 *
 * @author Farhan Bayezid
 * @version 1.0
 */
public class Ubungsmodus_UI {
    private JLabel progressLabel;
    private JLabel question;
    private JButton answer1;
    private JButton answer2;
    private JButton answer3;
    private JButton answer4;
    private JButton forward;
    private JLabel comment;
    private JButton zurückButton;
    private JLabel Spielername;
    private JPanel ubungpanelui;
    private JProgressBar tomakeprogress;
    private JProgressBar finishedprogress;
    private JProgressBar needtorepeatprogress;
    private JPanel progresspanel;

    private JButton[] answerButtons;

    private Nutzer currentUser;
    private SpieleController spieleController;

    /**
     * Liste der Fragen-Indizes, die in dieser Session noch nicht angezeigt wurden.
     */
    private List<Integer> unseenQuestions;

    /**
     * Liste der Fragen-Indizes, die vom Nutzer falsch beantwortet wurden und wiederholt werden müssen.
     */
    private List<Integer> wrongQuestions;

    private int currentQuestionindex;
    //private boolean isAnswered;
    private Timer autoAdvanceTimer;
    private int countdownValue;

    /**
     * Flag, ob sich das System gerade in der Wiederholungsphase falscher Fragen befindet.
     */
    private boolean isWrongPhase = false;//wechselt zwischen den falsch wiederholten und den schon gesehenen fragen


    /**
     * Konstruktor der Ubungsmodus_UI.
     * <p>
     * Initialisiert den SpieleController, erstellt die Listen für das Lern-Management
     * und mischt die Fragenreihenfolge. Registriert zudem die Listener für die
     * Antwort-Buttons und die Navigation.
     *
     * @param nutzer Der Benutzer, der den Übungsmodus gestartet hat.
     */
    public Ubungsmodus_UI(Nutzer nutzer) {
        this.currentUser = nutzer;
        Spielername.setText("Angemeldet als: " + currentUser.getName());

        this.spieleController = new SpieleController();
        this.answerButtons = new JButton[]{answer1, answer2, answer3, answer4};

        spieleController.startnewGame();


        unseenQuestions = new ArrayList<>();

        for (int i = 0; i < spieleController.gettotalefrage(); i++) {
            unseenQuestions.add(i);
        }

        wrongQuestions = new ArrayList<>();

        Collections.shuffle(unseenQuestions);

        loadNextQuestion();

        ActionListener antwortlistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //if (isAnswered) return;

                JButton source = (JButton) e.getSource();
                int selectedAnswer = -1;

                for (int i = 0; i < answerButtons.length; i++) {//findet heraus, welches button gedrückt worden ist
                    if (source == answerButtons[i]) {
                        selectedAnswer = i;
                        break;
                    }
                }

                boolean correct = spieleController.checkAnswer(currentQuestionindex, selectedAnswer);
                //isAnswered = true;

                if (correct) {
                    source.setBackground(new Color(214, 223, 186));
                    comment.setText("Richtig!");
                    if (unseenQuestions.contains(currentQuestionindex)) {
                        unseenQuestions.remove(Integer.valueOf(currentQuestionindex));
                    } else if (wrongQuestions.contains(currentQuestionindex)) {
                        wrongQuestions.remove(Integer.valueOf(currentQuestionindex));
                        unseenQuestions.add(currentQuestionindex);
                    }
                } else {
                    Fragen currentFrage = spieleController.getFrageText(currentQuestionindex);
                    source.setBackground(new Color(255, 206, 220));
                    if (currentFrage != null && currentFrage.getRichtigeAntwort() >= 0 && currentFrage.getRichtigeAntwort() < answerButtons.length) {
                        answerButtons[currentFrage.getRichtigeAntwort()].setBackground(new Color(214, 223, 186));
                    }
                    comment.setText("Falsch!");
                    if (unseenQuestions.contains(currentQuestionindex)) {
                        unseenQuestions.remove(Integer.valueOf(currentQuestionindex));
                        wrongQuestions.add(0, currentQuestionindex);
                    }
                }
                updateProgress();
                forward.setEnabled(true);
                startbuttonCountdown();
            }
        };

        //answer buttons bekommen actionlistener bzw "antwortlistener"
        for (JButton btn : answerButtons) {
            btn.addActionListener(antwortlistener);
        }

        forward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNextQuestion();
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
     * Ermittelt die nächste anzuzeigende Frage basierend auf dem Lernstand.
     * <p>
     * Das System prüft zuerst, ob noch neue (ungesehene) Fragen vorhanden sind.
     * Falls nicht, werden die falsch beantworteten Fragen aus der wrongQuestions-Liste
     * zur Wiederholung geladen. Sind beide Listen leer, wird das Spiel beendet.
     */
    private void loadNextQuestion() {
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
        }

        if (unseenQuestions.isEmpty() && !wrongQuestions.isEmpty()) {
            isWrongPhase = true;
        } else if (wrongQuestions.isEmpty()) {
            isWrongPhase = false;
        }

        if (isWrongPhase && !wrongQuestions.isEmpty()) {
            currentQuestionindex = wrongQuestions.get(0);
        } else if (!unseenQuestions.isEmpty()) {
            currentQuestionindex = unseenQuestions.get(0);
        } else {
            finishGame();
            return;
        }
        loadQuestionUI(currentQuestionindex);
    }

    /**
     * Lädt die Inhalte einer Frage (Text und Antwortmöglichkeiten) in die UI-Elemente.
     *
     * @param index Der Index der Frage, die aus dem SpieleController geladen werden soll.
     */
    public void loadQuestionUI(int index) {
        //isAnswered = false;
        comment.setText("");
        forward.setEnabled(false);
        forward.setText("Weiter");

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
            updateProgress();
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

    //


    /**
     * Aktiviert einen Countdown für den Weiter-Button.
     * <p>
     * Gibt dem Spieler 3 Sekunden Zeit, die korrekte Lösung nach einer Antwort zu
     * studieren, bevor automatisch die nächste Frage geladen wird.
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
                    loadNextQuestion();
                }
            }
        });
        autoAdvanceTimer.setRepeats(true);
        autoAdvanceTimer.start();
    }

    /**
     * Beendet den Übungsmodus und kehrt zum Hauptmenü zurück.
     * <p>
     * Zeigt einen Bestätigungsdialog an und informiert den Nutzer über den Lernerfolg.
     */
    private void finishGame() {
        if (autoAdvanceTimer != null) {
            autoAdvanceTimer.stop();
        }
        spieleController.endGame(currentUser);
        JOptionPane.showMessageDialog(ubungpanelui, "Übung beendet! Alles gelernt.");
        MainFrame.switchToPanel(new MainFrame(currentUser).getMainPanel());
    }

    /**
     * Verwaltet und aktualisiert die segmentierte Fortschrittsanzeige.
     * <p>
     * Berechnet die Breiten für drei verschiedene Fortschrittsbalken basierend auf
     * der Anzahl der erledigten, offenen und zu wiederholenden Fragen. Visualisiert
     * so den Lernfortschritt innerhalb eines 600-Pixel breiten Panels.
     */
    public void updateProgress() {
        int max = spieleController.gettotalefrage();
        int tomake = unseenQuestions.size();
        int tolearn = wrongQuestions.size();
        int finished = max - tomake - tolearn;


        int maxwith = 600;
        if (finished == 0) {
            finishedprogress.setVisible(false);
        } else {
            finishedprogress.setVisible(true);
            finishedprogress.setPreferredSize(new Dimension((finished * maxwith) / max, 10));
        }

        if (tomake == 0) {
            tomakeprogress.setVisible(false);
        } else {
            tomakeprogress.setVisible(true);
            tomakeprogress.setPreferredSize(new Dimension((tomake * maxwith) / max, 10));
        }

        if (tolearn == 0) {
            needtorepeatprogress.setVisible(false);
        } else {
            needtorepeatprogress.setVisible(true);
            needtorepeatprogress.setPreferredSize(new Dimension((tolearn * maxwith) / max, 10));
        }
        progresspanel.revalidate();
        progresspanel.repaint();

        progressLabel.setText(finished + "/" + max);
    }//improvesierter segmented progressbar


    /**
     * Gibt das Haupt-Panel der Übungs-Benutzeroberfläche zurück.
     *
     * @return Das JPanel mit allen Übungskomponenten und Fortschrittsbalken.
     */
    public JPanel getMainPanel() {
        return ubungpanelui;
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
        ubungpanelui = new JPanel();
        ubungpanelui.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        ubungpanelui.setBackground(new Color(-1773569));
        ubungpanelui.setPreferredSize(new Dimension(700, 350));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(10, 10, 0, 10), -1, -1, true, false));
        panel1.setBackground(new Color(-1773569));
        panel1.setForeground(new Color(-22606));
        ubungpanelui.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setBackground(new Color(-1773569));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progresspanel = new JPanel();
        progresspanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), 0, -1));
        panel2.add(progresspanel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(650, -1), null, 0, false));
        finishedprogress = new JProgressBar();
        finishedprogress.setBackground(new Color(-2695238));
        finishedprogress.setForeground(new Color(-2695238));
        finishedprogress.setValue(100);
        progresspanel.add(finishedprogress, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tomakeprogress = new JProgressBar();
        tomakeprogress.setBackground(new Color(-3025959));
        tomakeprogress.setValue(100);
        progresspanel.add(tomakeprogress, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        needtorepeatprogress = new JProgressBar();
        needtorepeatprogress.setBackground(new Color(-14169));
        needtorepeatprogress.setForeground(new Color(-14169));
        needtorepeatprogress.setValue(100);
        progresspanel.add(needtorepeatprogress, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressLabel = new JLabel();
        progressLabel.setText("100/100");
        panel2.add(progressLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        question = new JLabel();
        Font questionFont = this.$$$getFont$$$(null, -1, 24, question.getFont());
        if (questionFont != null) question.setFont(questionFont);
        question.setForeground(new Color(-13887279));
        question.setHorizontalAlignment(0);
        question.setText("Question this should not be empty");
        panel1.add(question, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 5, 0), -1, -1, true, false));
        panel3.setBackground(new Color(-1773569));
        panel1.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        answer1 = new JButton();
        answer1.setBackground(new Color(-12580));
        answer1.setEnabled(true);
        answer1.setFocusable(false);
        Font answer1Font = this.$$$getFont$$$("Arial Black", -1, 12, answer1.getFont());
        if (answer1Font != null) answer1.setFont(answer1Font);
        answer1.setForeground(new Color(-16579836));
        answer1.setText("Button");
        panel3.add(answer1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer2 = new JButton();
        answer2.setBackground(new Color(-3418389));
        Font answer2Font = this.$$$getFont$$$("Arial Black", -1, 12, answer2.getFont());
        if (answer2Font != null) answer2.setFont(answer2Font);
        answer2.setForeground(new Color(-16579836));
        answer2.setText("Button");
        panel3.add(answer2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer3 = new JButton();
        answer3.setBackground(new Color(-2695238));
        Font answer3Font = this.$$$getFont$$$("Arial Black", -1, 12, answer3.getFont());
        if (answer3Font != null) answer3.setFont(answer3Font);
        answer3.setForeground(new Color(-16579836));
        answer3.setText("Button");
        panel3.add(answer3, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        answer4 = new JButton();
        answer4.setBackground(new Color(-3418389));
        Font answer4Font = this.$$$getFont$$$("Arial Black", -1, 12, answer4.getFont());
        if (answer4Font != null) answer4.setFont(answer4Font);
        answer4.setForeground(new Color(-16579836));
        answer4.setMargin(new Insets(0, 0, 0, 0));
        answer4.setText("Button");
        panel3.add(answer4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(350, 40), new Dimension(350, 40), 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel4.setBackground(new Color(-1773569));
        panel1.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        forward = new JButton();
        forward.setBackground(new Color(-3418389));
        forward.setFocusable(true);
        Font forwardFont = this.$$$getFont$$$("Arial Black", -1, -1, forward.getFont());
        if (forwardFont != null) forward.setFont(forwardFont);
        forward.setForeground(new Color(-16579836));
        forward.setText("Weiter");
        panel4.add(forward, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_EAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 40), null, 0, false));
        comment = new JLabel();
        comment.setHorizontalAlignment(0);
        comment.setText("Richtig");
        panel4.add(comment, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel4.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(5, 10, 10, 10), -1, -1, true, false));
        panel5.setBackground(new Color(-1773569));
        ubungpanelui.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel5.add(zurückButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 40), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-9406850));
        label1.setText("Übungsmodus");
        panel5.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHEAST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Spielername = new JLabel();
        Spielername.setForeground(new Color(-9406850));
        Spielername.setText("Angemeldet als: shouldnotbeempty");
        panel5.add(Spielername, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        ubungpanelui.add(separator1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        return ubungpanelui;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
