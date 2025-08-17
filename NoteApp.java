import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class NoteApp extends JFrame {

    public static class Note implements Serializable {
        private static final long serialVersionUID = 1L;
        UUID id = UUID.randomUUID();
        String title = "Untitled";
        String content = "";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        Color bgColor = randomPastel(); 
        boolean pinned = false;


        public String toString() { return title; }

        private static Color randomPastel() {
            Color[] colors = {
                new Color(255, 204, 153),
                new Color(255, 153, 153),
                new Color(204, 255, 153),
                new Color(153, 204, 255),
                new Color(221, 160, 221),
                new Color(255, 255, 153)
            };
            return colors[new Random().nextInt(colors.length)];
        }
    }

    private final DefaultListModel<Note> listModel = new DefaultListModel<>();
    private final JList<Note> noteList = new JList<>(listModel);
    private final JTextField titleField = new JTextField();
    private final JTextArea contentArea = new JTextArea();
    private final JTextField searchField = new JTextField();
    private List<Note> allNotes = new ArrayList<>();

    private static final String STORE = "notes.db";


    public NoteApp() {
        super("Colorful Notes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        loadNotes();

        noteList.setCellRenderer(new NoteCardRenderer());
        JScrollPane listScroll = new JScrollPane(noteList);

        JPanel editorPanel = new JPanel(new BorderLayout(10, 10));

        JPanel topFields = new JPanel(new BorderLayout(5, 5));
        topFields.add(new JLabel("Title:"), BorderLayout.WEST);
        topFields.add(titleField, BorderLayout.CENTER);

        editorPanel.add(topFields, BorderLayout.NORTH);
        editorPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newBtn = new JButton("New");
        JButton saveBtn = new JButton("Save");
        JButton delBtn = new JButton("Delete");
        JButton pinBtn = new JButton("Pin/Unpin");
        buttonsPanel.add(newBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(delBtn);
        buttonsPanel.add(pinBtn);

        editorPanel.add(buttonsPanel, BorderLayout.SOUTH);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, editorPanel);
        splitPane.setDividerLocation(350);
        add(searchPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        newBtn.addActionListener(e -> newNote());
        saveBtn.addActionListener(e -> saveCurrentNote());
        delBtn.addActionListener(e -> deleteCurrentNote());
        pinBtn.addActionListener(e -> togglePin());
        noteList.addListSelectionListener(e -> showSelectedNote());

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshList(searchField.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshList(searchField.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshList(searchField.getText()); }
        });

        KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKeyStroke, "saveNote");
        getRootPane().getActionMap().put("saveNote", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { saveCurrentNote(); }
        });

        KeyStroke newKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newKeyStroke, "newNote");
        getRootPane().getActionMap().put("newNote", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { newNote(); }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Settings");

        JMenuItem fontItem = new JMenuItem("Change Font...");
        fontItem.addActionListener(e -> changeFont());

        JMenuItem colorItem = new JMenuItem("Change Text Color...");
        colorItem.addActionListener(e -> changeTextColor());

        settingsMenu.add(fontItem);
        settingsMenu.add(colorItem);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);

        refreshList("");
    }

    private void togglePin() {
        Note n = noteList.getSelectedValue();
        if (n != null) {
            n.pinned = !n.pinned;
            persist();
            refreshList(searchField.getText());
        }
    }

    private void changeFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();

        String fontName = (String) JOptionPane.showInputDialog(
            this, "Choose Font:", "Font Selection",
            JOptionPane.PLAIN_MESSAGE, null, fonts,
            contentArea.getFont().getFamily()
        );

        if (fontName != null) {
            String sizeStr = JOptionPane.showInputDialog(this, "Enter Font Size:", contentArea.getFont().getSize());
            try {
                int size = Integer.parseInt(sizeStr);
                contentArea.setFont(new Font(fontName, Font.PLAIN, size));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid size entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeTextColor() {
        Color newColor = JColorChooser.showDialog(this, "Choose Text Color", contentArea.getForeground());
        if (newColor != null) {
            contentArea.setForeground(newColor);
        }
    }

    private void newNote() {
        Note n = new Note();
        n.title = suggestNewTitle();
        allNotes.add(0, n);
        refreshList(searchField.getText());
        noteList.setSelectedIndex(0);
        titleField.requestFocus();
        titleField.selectAll();
    }

    private void saveCurrentNote() {
    Note n = noteList.getSelectedValue();
    if (n == null) return;
    n.title = titleField.getText().isBlank() ? "Untitled" : titleField.getText().trim();
    n.content = contentArea.getText(); 
    n.updatedAt = LocalDateTime.now();
    persist();
    refreshList(searchField.getText());
    JOptionPane.showMessageDialog(this, "Note saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
}

    private void deleteCurrentNote() {
        Note n = noteList.getSelectedValue();
        if (n == null) return;
        int res = JOptionPane.showConfirmDialog(this, "Delete \"" + n.title + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            allNotes.removeIf(x -> x.id.equals(n.id));
            refreshList(searchField.getText());
            clearEditor();
            persist();
        }
    }

    private void showSelectedNote() {
        Note n = noteList.getSelectedValue();
        if (n == null) {
            clearEditor();
            return;
        }
        titleField.setText(n.title);
        contentArea.setText(n.content);
        titleField.setBackground(n.bgColor);
        contentArea.setBackground(n.bgColor);
    }

    private void clearEditor() {
        titleField.setText("");
        contentArea.setText("");
        titleField.setBackground(Color.WHITE);
        contentArea.setBackground(Color.WHITE);
    }

    private void refreshList(String filter) {
        String f = filter == null ? "" : filter.trim().toLowerCase();
        List<Note> filtered = allNotes.stream()
            .filter(n -> n.title.toLowerCase().contains(f) || n.content.toLowerCase().contains(f))
            .sorted(Comparator
                .comparing((Note n) -> !n.pinned)
                .thenComparing((Note n) -> n.updatedAt, Comparator.reverseOrder()))
            .collect(Collectors.toList());

        listModel.clear();
        for (Note n : filtered) listModel.addElement(n);
    }

    private String suggestNewTitle() {
        String base = "New Note";
        int i = 1;
        Set<String> titles = allNotes.stream().map(n -> n.title).collect(Collectors.toSet());
        String t = base;
        while (titles.contains(t)) t = base + " " + (++i);
        return t;
    }

    @SuppressWarnings("unchecked")
    private void loadNotes() {
        File f = new File(STORE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                allNotes = (List<Note>) obj;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load notes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORE))) {
            oos.writeObject(allNotes);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save notes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class NoteCardRenderer extends JPanel implements ListCellRenderer<Note> {
        private JLabel titleLabel = new JLabel();
        private JLabel previewLabel = new JLabel();
        private JLabel dateLabel = new JLabel();

        public NoteCardRenderer() {
            setLayout(new BorderLayout(5, 5));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));

            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);
            textPanel.add(titleLabel, BorderLayout.NORTH);
            textPanel.add(previewLabel, BorderLayout.CENTER);
            add(textPanel, BorderLayout.CENTER);

            add(dateLabel, BorderLayout.SOUTH);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override
public Component getListCellRendererComponent(JList<? extends Note> list, Note note, int index,
                                              boolean isSelected, boolean cellHasFocus) {
    setBackground(note.bgColor);
    setOpaque(true);

    String pinSymbol = note.pinned ? " 📌 " : "";
    JLabel pinLabel = new JLabel(pinSymbol);
    pinLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); 

    titleLabel.setText(note.title);
    titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

    String preview = note.content.length() > 80 ? note.content.substring(0, 80) + "..." : note.content;
    previewLabel.setText("<html><body style='width:200px'>" + preview + "</body></html>");

    dateLabel.setText(note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

    setLayout(new BorderLayout(5, 5));
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    topPanel.setOpaque(false);
    topPanel.add(pinLabel);
    topPanel.add(titleLabel);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.setOpaque(false);
    bottomPanel.add(previewLabel, BorderLayout.CENTER);
    bottomPanel.add(dateLabel, BorderLayout.SOUTH);

    removeAll();
    add(topPanel, BorderLayout.NORTH);
    add(bottomPanel, BorderLayout.CENTER);

    if (isSelected) {
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    } else {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    return this;
}

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NoteApp().setVisible(true));
    }
}
