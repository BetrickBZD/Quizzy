package view;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

import model.Nutzer;
import model.Fragen;
import controller.FragenController;
import controller.NutzerController;

/**
 * Der CSVeditor stellt die grafische Benutzeroberfläche zur Verwaltung
 * der CSV-Datenbestände (Nutzer und Fragen) bereit
 *
 * @author bayezid Farhan
 * @version 1.0
 */
public class CSVeditor {
    //UI-Komponenten
    /**
     * Haupt-Panel der Editor-Ansicht
     */
    private JPanel CSVEditorPanel;
    /**
     * Button zum Wechseln in den Nutzer-Verwaltungsmodus
     */
    private JButton nutzerButton;
    /**
     * Button zum Wechseln in die Frage-Verwaltungsmodus
     */
    private JButton fragenButton;
    /**
     * Button zum Speichern der bearbeiteten Daten
     */
    private JButton speichernButton;
    /**
     * Button zum Erstellen eines neuen Datensatzes
     */
    private JButton erstellenButton;
    /**
     * Button zum Löschen eines Datensatzes
     */
    private JButton löschenButton;
    /**
     * Liste zur Anzeige der Daten
     */
    private JList list1;
    /**
     * Button zum Zurücksprung zum Hauptmenü
     */
    private JButton zurückZumHauptmenüButton;
    /**
     * Panel, das die Eingabefelder für Fragen enthält
     */
    private JPanel FragenEdit;
    /**
     * RadioButton zur Auswahl der Antwort 1
     */
    private JRadioButton oneanswer;
    /**
     * RadioButton zur Auswahl der Antwort 2
     */
    private JRadioButton twoanswer;
    /**
     * RadioButton zur Auswahl der Antwort 3
     */
    private JRadioButton threeanswer;
    /**
     * RadioButton zur Auswahl der Antwort 4
     */
    private JRadioButton fouranswer;
    /**
     * Eingabefeld für die Frage
     */
    private JTextField textFieldname;
    /**
     * Eingabefelder für die Antwortmöglichkeit 1
     */
    private JTextField textField1;
    /**
     * Eingabefelder für die Antwortmöglichkeit 2
     */
    private JTextField textField2;
    /**
     * Eingabefelder für die Antwortmöglichkeit 3
     */
    private JTextField textField3;
    /**
     * Eingabefelder für die Antwortmöglichkeit 4
     */
    private JTextField textField4;
    /**
     * Panel, das die Eingabefelder für Nutzer enthält
     */
    private JPanel NutzerEdit;
    /**
     * Checkbox zur Auswahl des Admin-Status
     */
    private JCheckBox adminCheckBox;
    /**
     * Eingabefeld für den Nutzernamen
     */
    private JTextField nutzernameedittextfield;
    /**
     * Spinner zur Auswahl des Highscores
     */
    private JSpinner spinner1;

    /**
     * Referenz auf den aktuell angemeldeten Nutzer
     */
    private Nutzer currentUser;
    /**
     * Status für den aktuellen Bearbeitungsmodus
     * true = Nutzer-Modus, false = Frage-Modus
     */
    private boolean isNutzerMode = true;
    /**
     * Controller für die Nutzerverwaltung
     */
    private NutzerController nutzerController;
    /**
     * Controller für die Fragenverwaltung
     */
    private FragenController fragenController;

    /**
     * Konstruktor für den CSVeditor
     * Initialisiert die Controller, setzt das Button-Design für die Antwortauswahl
     * konfiguriert die Listener für Buttons und Listen-Events und startet standardmäßig
     * im Nutzer-Modus.
     *
     * @param nutzer der aktuell angemeldete Nutzer
     * @throws IllegalArgumentException Wenn das übergebene Nutzer-Objekt null ist
     */
    public CSVeditor(Nutzer nutzer) {
        this.currentUser = nutzer;

        nutzerController = new NutzerController();
        fragenController = new FragenController();

        // Grupierung der Antwortmöglichkeiten
        ButtonGroup group = new ButtonGroup();
        group.add(oneanswer);
        group.add(twoanswer);
        group.add(threeanswer);
        group.add(fouranswer);

        // Listener für die visuelle Rückmeldung der "Richtig"-Markierung
        ActionListener radioListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JRadioButton selected = (JRadioButton) e.getSource();
                oneanswer.setText(selected == oneanswer ? "richtig" : "falsch");
                twoanswer.setText(selected == twoanswer ? "richtig" : "falsch");
                threeanswer.setText(selected == threeanswer ? "richtig" : "falsch");
                fouranswer.setText(selected == fouranswer ? "richtig" : "falsch");
            }
        };

        oneanswer.addActionListener(radioListener);
        twoanswer.addActionListener(radioListener);
        threeanswer.addActionListener(radioListener);
        fouranswer.addActionListener(radioListener);
        // Startmodus festlegen
        setNutzerMode();

        // Listener für die Navigation zurück zum Hauptmenü
        zurückZumHauptmenüButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame mainFrame = new MainFrame(currentUser);
                MainFrame.switchToPanel(mainFrame.getMainPanel());
            }
        });//zurück zum Mainframe

        // Modus-Umschalter zur Nutzer-Verwaltung
        nutzerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNutzerMode();
            }
        });

        // Modus-Umschalter zur Frage-Verwaltung
        fragenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFragenMode();
            }
        });

        // Event bei Auswahl eines Elements in der Liste
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                int index = list1.getSelectedIndex();
                if (index != -1) {
                    if (isNutzerMode) {
                        String selected = (String) list1.getSelectedValue();
                        fillNutzerFields(selected);
                    } else {
                        List<Fragen> fragen = fragenController.getLoadedQuestions();
                        if (index < fragen.size()) {
                            fillFragenFields(fragen.get(index));
                        }
                    }
                }
            }
        });

        // Seichernlogik
        speichernButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list1.getSelectedIndex();


                if (index == -1) return;
                if (isNutzerMode) {
                    String name = nutzernameedittextfield.getText();

                    String oldName = ((String) list1.getSelectedValue()).split(",")[0];
                    if (name.isEmpty()) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Name darf nicht leer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!name.equalsIgnoreCase(oldName) && nutzerController.checkNutzer(name)) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Nutzer existiert bereits!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    nutzerController.updateNutzer(index, name, (int) spinner1.getValue(), adminCheckBox.isSelected());
                    refreshNutzerList();
                } else {
                    if (textFieldname.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Frage darf nicht leer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    fragenController.updateQuestion(index, textFieldname.getText(), textField1.getText(), textField2.getText(), textField3.getText(), textField4.getText(), getSelectedAnswerIndex());
                    refreshFragenList();

                }

            }
        });

        // Erstellenlogik
        erstellenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isNutzerMode) {
                    if (nutzernameedittextfield.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Name darf nicht leer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String name = nutzernameedittextfield.getText();
                    if (nutzerController.checkNutzer(name)) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Nutzer existiert bereits!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int highscore = (int) spinner1.getValue();
                    boolean admin = adminCheckBox.isSelected();
                    nutzerController.createNutzer(name, highscore, admin);
                    refreshNutzerList();
                } else {
                    if (textFieldname.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(CSVEditorPanel, "Frage darf nicht leer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    fragenController.createQuestion(textFieldname.getText(), textField1.getText(), textField2.getText(), textField3.getText(), textField4.getText(), getSelectedAnswerIndex());
                    refreshFragenList();
                    System.out.println(getSelectedAnswerIndex());
                }
            }
        });

        // Löschenlogik
        löschenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list1.getSelectedIndex();
                if (index == -1) return;
                if (isNutzerMode) {
                    nutzerController.deleteNutzer(index);
                    refreshNutzerList();
                } else {
                    fragenController.deleteQuestion(index);
                    refreshFragenList();
                }
                clearFields();
            }
        });
    }

    /**
     * Aktiviert den Modus zur Bearbeitung der Nutzerdaten
     */
    private void setNutzerMode() {
        isNutzerMode = true;
        nutzerButton.setEnabled(false);
        fragenButton.setEnabled(true);
        FragenEdit.setVisible(false);
        NutzerEdit.setVisible(true);
        refreshNutzerList();
        clearFields();
    }

    /**
     * Aktiviert den Modus zur Bearbeitung der Fragendaten
     */
    private void setFragenMode() {
        isNutzerMode = false;
        nutzerButton.setEnabled(true);
        fragenButton.setEnabled(false);
        FragenEdit.setVisible(true);
        NutzerEdit.setVisible(false);
        refreshFragenList();
        clearFields();
    }

    /**
     * leert alle Eingabefehler im Editor
     */
    private void clearFields() {
        textFieldname.setText("");
        textField1.setText("");
        textField2.setText("");
        textField3.setText("");
        textField4.setText("");

        nutzernameedittextfield.setText("");
        spinner1.setValue(0);
        adminCheckBox.setSelected(false);
    }

    /**
     * Füllt die Eingabefelder mit Daten aus einer CSV-Zeile eines Nutzers
     *
     * @param csvLine Die parsende Zeile aus der CSV-Datei
     */
    private void fillNutzerFields(String csvLine) {
        if (csvLine == null) return;
        String[] parts = csvLine.split(",");
        if (parts.length >= 3) {
            nutzernameedittextfield.setText(parts[0]);
            try {
                spinner1.setValue(Integer.parseInt(parts[1]));
            } catch (NumberFormatException ex) {
                spinner1.setValue(0);
            }
            adminCheckBox.setSelected(Boolean.parseBoolean(parts[2]));
        }
    }

    /**
     * Füllt die Eingabefelder mit Daten aus einer Fragen-Instanz
     *
     * @param f Das Fragen-Objekt
     * @throws IllegalArgumentException Wenn das übergebene Fragen-Objekt null ist
     */
    private void fillFragenFields(Fragen f) {
        textFieldname.setText(f.getFrage());
        String[] a = f.getAntwort();
        if (a.length >= 4) {
            textField1.setText(a[0]);
            textField2.setText(a[1]);
            textField3.setText(a[2]);
            textField4.setText(a[3]);
        }
        int right = f.getRichtigeAntwort();
        JRadioButton[] buttons = {oneanswer, twoanswer, threeanswer, fouranswer};
        for (int i = 0; i < buttons.length; i++) {
            if (i == right) {
                buttons[i].setSelected(true);
                buttons[i].setText("richtig");
            } else {
                buttons[i].setText("falsch");
            }
        }
    }


    /**
     * Ermittelt den Index der ausgewählten als korrekt markierten Antwortmöglichkeit
     *
     * @return Der 1-basierte Index der ausgewählten Antwort (1-4)
     */
    private int getSelectedAnswerIndex() {
        if (oneanswer.isSelected()) return 0;
        if (twoanswer.isSelected()) return 1;
        if (threeanswer.isSelected()) return 2;
        if (fouranswer.isSelected()) return 3;
        return 0; // Default
    }

    /**
     * Aktualisiert die JList mit den aktuellen Nutzerdaten
     */
    private void refreshNutzerList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        List<Nutzer> nutzer = nutzerController.getAllNutzer();
        for (Nutzer n : nutzer) {
            model.addElement(n.getName() + "," + n.getHighscore() + "," + n.isAdmin());
        }
        list1.setModel(model);
    }

    /**
     * Aktualisiert die JList mit den aktuellen Frage-Daten
     */
    private void refreshFragenList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        List<Fragen> fragen = fragenController.getLoadedQuestions();
        for (Fragen f : fragen) {
            String[] a = f.getAntwort();
            model.addElement(f.getFrage() + "," + a[0] + "," + a[1] + "," + a[2] + "," + a[3] + "," + f.getRichtigeAntwort());
        }
        list1.setModel(model);
    }

    public JPanel getMainPanel() {
        return CSVEditorPanel;
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
        CSVEditorPanel = new JPanel();
        CSVEditorPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(10, 10, 10, 10), -1, -1));
        CSVEditorPanel.setBackground(new Color(-1773569));
        CSVEditorPanel.setMinimumSize(new Dimension(700, 350));
        CSVEditorPanel.setPreferredSize(new Dimension(700, 350));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1773569));
        CSVEditorPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(280, 511), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        list1 = new JList();
        scrollPane1.setViewportView(list1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 5, 0), -1, -1, true, false));
        panel2.setBackground(new Color(-1773569));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nutzerButton = new JButton();
        nutzerButton.setBackground(new Color(-3944740));
        nutzerButton.setForeground(new Color(-16579836));
        nutzerButton.setText("Nutzer");
        panel2.add(nutzerButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        fragenButton = new JButton();
        fragenButton.setBackground(new Color(-3944740));
        Font fragenButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, fragenButton.getFont());
        if (fragenButtonFont != null) fragenButton.setFont(fragenButtonFont);
        fragenButton.setForeground(new Color(-16579836));
        fragenButton.setText("Fragen");
        panel2.add(fragenButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(225, 40), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        CSVEditorPanel.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(255, 511), null, 0, false));
        FragenEdit = new JPanel();
        FragenEdit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 10, 0, 10), -1, -1));
        panel3.add(FragenEdit, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        oneanswer = new JRadioButton();
        oneanswer.setText("richtig");
        FragenEdit.add(oneanswer, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        twoanswer = new JRadioButton();
        twoanswer.setText("falsch");
        FragenEdit.add(twoanswer, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        threeanswer = new JRadioButton();
        threeanswer.setText("falsch");
        FragenEdit.add(threeanswer, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fouranswer = new JRadioButton();
        fouranswer.setText("falsch");
        FragenEdit.add(fouranswer, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Name");
        FragenEdit.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldname = new JTextField();
        FragenEdit.add(textFieldname, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField1 = new JTextField();
        FragenEdit.add(textField1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField2 = new JTextField();
        FragenEdit.add(textField2, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField3 = new JTextField();
        FragenEdit.add(textField3, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textField4 = new JTextField();
        FragenEdit.add(textField4, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        NutzerEdit = new JPanel();
        NutzerEdit.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 10, 0, 10), -1, -1));
        panel3.add(NutzerEdit, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Name");
        NutzerEdit.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        adminCheckBox = new JCheckBox();
        adminCheckBox.setText("Admin");
        NutzerEdit.add(adminCheckBox, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nutzernameedittextfield = new JTextField();
        NutzerEdit.add(nutzernameedittextfield, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Highscore");
        NutzerEdit.add(label3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinner1 = new JSpinner();
        NutzerEdit.add(spinner1, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(5, 0, 0, 0), -1, -1, true, false));
        panel4.setBackground(new Color(-1773569));
        CSVEditorPanel.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        speichernButton = new JButton();
        speichernButton.setBackground(new Color(-3944740));
        Font speichernButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, speichernButton.getFont());
        if (speichernButtonFont != null) speichernButton.setFont(speichernButtonFont);
        speichernButton.setForeground(new Color(-16579836));
        speichernButton.setText("Speichern");
        panel4.add(speichernButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        erstellenButton = new JButton();
        erstellenButton.setBackground(new Color(-3944740));
        Font erstellenButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, erstellenButton.getFont());
        if (erstellenButtonFont != null) erstellenButton.setFont(erstellenButtonFont);
        erstellenButton.setForeground(new Color(-16579836));
        erstellenButton.setText("Erstellen");
        panel4.add(erstellenButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        löschenButton = new JButton();
        löschenButton.setBackground(new Color(-3944740));
        Font löschenButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, löschenButton.getFont());
        if (löschenButtonFont != null) löschenButton.setFont(löschenButtonFont);
        löschenButton.setForeground(new Color(-16579836));
        löschenButton.setText("Löschen");
        panel4.add(löschenButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 0, 0, 0), -1, -1));
        panel5.setBackground(new Color(-1773569));
        CSVEditorPanel.add(panel5, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_SOUTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        zurückZumHauptmenüButton = new JButton();
        zurückZumHauptmenüButton.setBackground(new Color(-12580));
        Font zurückZumHauptmenüButtonFont = this.$$$getFont$$$("Arial Black", -1, -1, zurückZumHauptmenüButton.getFont());
        if (zurückZumHauptmenüButtonFont != null) zurückZumHauptmenüButton.setFont(zurückZumHauptmenüButtonFont);
        zurückZumHauptmenüButton.setForeground(new Color(-16579836));
        zurückZumHauptmenüButton.setText("Zurück zum Hauptmenü");
        panel5.add(zurückZumHauptmenüButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
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
        return CSVEditorPanel;
    }

}