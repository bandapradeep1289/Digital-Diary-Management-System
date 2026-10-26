package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import dao.EntryDAO;
import exception.EmptyEntryException;
import model.Entry;
import model.User;
import thread.AutoSaveThread;
import util.FileUtil;

/** Dialog used both to add a new entry and to edit an existing one. */
public class EntryDialog extends JDialog {
    private static final String[] TYPES = {"Personal", "Work", "Travel"};
    private static final String[] MOODS =
            {"Happy", "Sad", "Excited", "Calm", "Angry", "Tired", "Anxious", "Neutral"};

    private final User user;
    private final Entry existing;
    private final EntryDAO dao = new EntryDAO();

    private final JComboBox<String> typeBox = new JComboBox<>(TYPES);
    private final JTextField dateField = new JTextField(10);
    private final JTextField titleField = new JTextField(25);
    private final JComboBox<String> moodBox = new JComboBox<>(MOODS);
    private final JTextArea contentArea = new JTextArea(12, 40);
    private final JLabel noteLabel = new JLabel("Auto-save runs every 10 seconds.");

    private AutoSaveThread autoSave;
    private Thread autoSaveThread;
    private boolean saved = false;

    public EntryDialog(Frame owner, User user, Entry existing) {
        super(owner, existing == null ? "New Entry" : "Edit Entry", true);
        this.user = user;
        this.existing = existing;

        buildUI();
        if (existing != null) {
            typeBox.setSelectedItem(existing.getEntryType());
            dateField.setText(existing.getEntryDate().toString());
            titleField.setText(existing.getTitle());
            moodBox.setSelectedItem(existing.getMood());
            contentArea.setText(existing.getContent());
        } else {
            dateField.setText(LocalDate.now().toString());
        }

        startAutoSave();
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e)  { stopAutoSave(); }
            @Override public void windowClosed(WindowEvent e)   { stopAutoSave(); }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Entry type:"), c);
        c.gridx = 1;               form.add(typeBox, c);
        c.gridx = 2;               form.add(new JLabel("Date (yyyy-MM-dd):"), c);
        c.gridx = 3;               form.add(dateField, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Title:"), c);
        c.gridx = 1; c.gridwidth = 1; form.add(titleField, c);
        c.gridx = 2;               form.add(new JLabel("Mood:"), c);
        c.gridx = 3;               form.add(moodBox, c);
        add(form, BorderLayout.NORTH);

        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JButton saveBtn = new JButton("Save (Ctrl+S)");
        JButton importBtn = new JButton("Import Text...");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(importBtn);
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        JPanel south = new JPanel(new BorderLayout());
        south.add(noteLabel, BorderLayout.WEST);
        south.add(buttons, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> { stopAutoSave(); dispose(); });
        importBtn.addActionListener(e -> importText());

        // Ctrl+S shortcut (works from any component in the dialog)
        KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlS, "saveEntry");
        getRootPane().getActionMap().put("saveEntry", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { save(); }
        });
    }

    private void startAutoSave() {
        autoSave = new AutoSaveThread(
                () -> contentArea.getText(),
                msg -> noteLabel.setText(msg),
                new File("draft.txt"),
                10);
        autoSaveThread = new Thread(autoSave, "AutoSaveThread");
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
    }

    private void stopAutoSave() {
        if (autoSave != null) {
            autoSave.stop();
            autoSaveThread.interrupt();
            autoSave = null;
        }
    }

    private void importText() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                contentArea.append(FileUtil.importText(fc.getSelectedFile()));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read file: " + ex.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void save() {
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter the date as yyyy-MM-dd.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (existing == null) ? 0 : existing.getEntryId();
        Entry entry = Entry.create((String) typeBox.getSelectedItem(), id, user.getUserId(), date,
                titleField.getText(), contentArea.getText(), (String) moodBox.getSelectedItem());
        try {
            if (existing == null) {
                dao.addEntry(entry);
            } else {
                dao.updateEntry(entry);
            }
            saved = true;
            stopAutoSave();
            dispose();
        } catch (EmptyEntryException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Empty Entry",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
