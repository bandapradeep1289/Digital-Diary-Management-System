package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import dao.EntryDAO;
import exception.EmptyEntryException;
import model.Entry;
import model.User;
import thread.ClockThread;
import util.EntryDateComparator;
import util.FileUtil;

public class DashboardPanel extends JPanel {
    private static final String[] COLUMNS = {"Date", "Type", "Title", "Mood"};

    private final MainFrame frame;
    private final User user;
    private final EntryDAO dao = new EntryDAO();

    private List<Entry> entries = new ArrayList<>();
    private List<Entry> searchResults = new ArrayList<>();
    private boolean newestFirst = true;

    private final JLabel clockLabel = new JLabel();
    private final ClockThread clockThread;

    private final DefaultTableModel entryModel = newModel();
    private final DefaultTableModel searchModel = newModel();
    private final JTable entryTable = new JTable(entryModel);
    private final JTable searchTable = new JTable(searchModel);
    private final JTextArea statsArea = new JTextArea();
    private final JTextField keywordField = new JTextField(20);
    private final JTextField dateField = new JTextField(10);

    public DashboardPanel(MainFrame frame, User user) {
        this.frame = frame;
        this.user = user;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- header with welcome text and live clock -----
        JLabel welcome = new JLabel("Welcome, " + user.getUsername());
        welcome.setFont(new Font("SansSerif", Font.BOLD, 20));
        JPanel header = new JPanel(new BorderLayout());
        header.add(welcome, BorderLayout.WEST);
        header.add(clockLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ----- tabs -----
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Entries", buildEntriesTab());
        tabs.addTab("Search", buildSearchTab());
        tabs.addTab("Statistics", buildStatsTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) refreshStats();
        });
        add(tabs, BorderLayout.CENTER);

        clockThread = new ClockThread(clockLabel);
        clockThread.start();
        loadEntries();
    }

    // ================= building tabs =================

    private JPanel buildEntriesTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        entryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entryTable.setRowHeight(22);
        // MouseListener: double-click a row to open it
        entryTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editSelected();
            }
        });
        p.add(new JScrollPane(entryTable), BorderLayout.CENTER);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton sortBtn = new JButton("Sort by Date");
        JButton exportBtn = new JButton("Export");
        JButton backupBtn = new JButton("Backup");
        JButton restoreBtn = new JButton("Restore");
        JButton logoutBtn = new JButton("Logout");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton b : new JButton[]{addBtn, editBtn, delBtn, sortBtn, exportBtn, backupBtn, restoreBtn, logoutBtn}) {
            btns.add(b);
        }
        p.add(btns, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addEntry());
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        sortBtn.addActionListener(e -> {
            entries.sort(new EntryDateComparator(newestFirst));
            newestFirst = !newestFirst;
            fillTable(entryModel, entries);
        });
        exportBtn.addActionListener(e -> exportSelected());
        backupBtn.addActionListener(e -> backup());
        restoreBtn.addActionListener(e -> restore());
        logoutBtn.addActionListener(e -> frame.showLogin());
        return p;
    }

    private JPanel buildSearchTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JButton keywordBtn = new JButton("Search Keyword");
        JButton dateBtn = new JButton("Search Date");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Keyword:"));
        top.add(keywordField);
        top.add(keywordBtn);
        top.add(new JLabel("   Date (yyyy-MM-dd):"));
        top.add(dateField);
        top.add(dateBtn);
        p.add(top, BorderLayout.NORTH);

        searchTable.setRowHeight(22);
        searchTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showSearchResultContent();
            }
        });
        p.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        keywordBtn.addActionListener(e -> searchKeyword());
        dateBtn.addActionListener(e -> searchDate());
        return p;
    }

    private JPanel buildStatsTab() {
        JPanel p = new JPanel(new BorderLayout());
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        p.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        return p;
    }

    private static DefaultTableModel newModel() {
        return new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    // ================= data handling =================

    private void loadEntries() {
        try {
            entries = dao.getEntriesByUser(user.getUserId());
            fillTable(entryModel, entries);
        } catch (SQLException ex) {
            showError("Could not load entries: " + ex.getMessage());
        }
    }

    private void fillTable(DefaultTableModel model, List<Entry> list) {
        model.setRowCount(0);
        for (Entry e : list) {
            model.addRow(new Object[]{e.getEntryDate(), e.getEntryType(), e.getTitle(), e.getMood()});
        }
    }

    private Entry selectedEntry() {
        int row = entryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an entry first.");
            return null;
        }
        return entries.get(row);
    }

    // ================= actions =================

    private void addEntry() {
        EntryDialog d = new EntryDialog(frame, user, null);
        d.setVisible(true);
        if (d.isSaved()) loadEntries();
    }

    private void editSelected() {
        Entry e = selectedEntry();
        if (e == null) return;
        EntryDialog d = new EntryDialog(frame, user, e);
        d.setVisible(true);
        if (d.isSaved()) loadEntries();
    }

    private void deleteSelected() {
        Entry e = selectedEntry();
        if (e == null) return;
        int ans = JOptionPane.showConfirmDialog(this, "Delete \"" + e.getTitle() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ans != JOptionPane.YES_OPTION) return;
        try {
            dao.deleteEntry(e.getEntryId());
            loadEntries();
        } catch (SQLException ex) {
            showError("Could not delete: " + ex.getMessage());
        }
    }

    private void exportSelected() {
        Entry e = selectedEntry();
        if (e == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(e.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileUtil.exportEntry(e, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Entry exported successfully.");
            } catch (IOException ex) {
                showError("Export failed: " + ex.getMessage());
            }
        }
    }

    private void backup() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("diary_backup.ser"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileUtil.backup(entries, fc.getSelectedFile());
                JOptionPane.showMessageDialog(this, entries.size() + " entries backed up.");
            } catch (IOException ex) {
                showError("Backup failed: " + ex.getMessage());
            }
        }
    }

    private void restore() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<Entry> backup = FileUtil.restore(fc.getSelectedFile());
            int added = 0;
            for (Entry b : backup) {
                if (alreadyExists(b)) continue;
                Entry copy = Entry.create(b.getEntryType(), 0, user.getUserId(),
                        b.getEntryDate(), b.getTitle(), b.getContent(), b.getMood());
                try {
                    dao.addEntry(copy);
                    added++;
                } catch (EmptyEntryException ignored) {
                    // skip invalid entries in the backup file
                }
            }
            loadEntries();
            JOptionPane.showMessageDialog(this, added + " entries restored.");
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            showError("Restore failed: " + ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private boolean alreadyExists(Entry b) {
        for (Entry e : entries) {
            if (e.getEntryDate().equals(b.getEntryDate())
                    && e.getTitle().equals(b.getTitle())
                    && e.getContent().equals(b.getContent())) {
                return true;
            }
        }
        return false;
    }

    private void searchKeyword() {
        String kw = keywordField.getText().trim();
        if (kw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a keyword to search.");
            return;
        }
        try {
            searchResults = dao.searchByKeyword(user.getUserId(), kw);
            fillTable(searchModel, searchResults);
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void searchDate() {
        try {
            LocalDate d = LocalDate.parse(dateField.getText().trim());
            searchResults = dao.searchByDate(user.getUserId(), d);
            fillTable(searchModel, searchResults);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please enter the date as yyyy-MM-dd.");
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private void showSearchResultContent() {
        int row = searchTable.getSelectedRow();
        if (row < 0) return;
        Entry e = searchResults.get(row);
        JTextArea area = new JTextArea(e.getContent(), 10, 40);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), e.getTitle(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Mood counts use a HashMap; entries grouped by date use a TreeMap (Unit III). */
    private void refreshStats() {
        loadEntries();
        Map<String, Integer> moodCount = new HashMap<>();
        Map<LocalDate, List<String>> byDate = new TreeMap<>();
        for (Entry e : entries) {
            moodCount.merge(e.getMood(), 1, Integer::sum);
            byDate.computeIfAbsent(e.getEntryDate(), k -> new ArrayList<>()).add(e.getTitle());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total entries: ").append(entries.size()).append("\n\n");
        sb.append("=== Mood Summary ===\n");
        for (Map.Entry<String, Integer> m : moodCount.entrySet()) {
            sb.append(String.format("%-12s : %d%n", m.getKey(), m.getValue()));
        }
        sb.append("\n=== Entries Grouped by Date ===\n");
        for (Map.Entry<LocalDate, List<String>> d : byDate.entrySet()) {
            sb.append(d.getKey()).append("\n");
            for (String t : d.getValue()) {
                sb.append("    - ").append(t).append("\n");
            }
        }
        statsArea.setText(sb.toString());
        statsArea.setCaretPosition(0);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Called by MainFrame when the user logs out. */
    public void stopThreads() {
        clockThread.stopClock();
    }
}
