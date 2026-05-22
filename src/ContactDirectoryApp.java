import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Swing GUI: sidebar inputs + contact table. */
public class ContactDirectoryApp extends JFrame {
    private static final int WINDOW_WIDTH = 980;
    private static final int WINDOW_HEIGHT = 560;
    private static final String[] TABLE_COLUMNS = {
            "Name", "Phone", "Ext.", "Email", "Department", "Organization", "Group"
    };

    private final List<Contact> contacts = new ArrayList<>();
    private final List<Integer> displayedIds = new ArrayList<>();
    private Integer editingId = null;

    private final JTextField nameField = new JTextField(16);
    private final JTextField phoneField = new JTextField(16);
    private final JTextField extensionField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);
    private final JTextField departmentField = new JTextField(16);
    private final JTextField organizationField = new JTextField(16);
    private final JTextField searchField = new JTextField(16);
    private final JComboBox<String> groupCombo = new JComboBox<>(
            ContactStorage.CONTACT_GROUPS.toArray(new String[0]));
    private final JComboBox<String> filterCombo = new JComboBox<>(
            new String[]{"All", "Work", "Family", "Friends"});
    private final JComboBox<String> sortCombo = new JComboBox<>(
            new String[]{"Name (A-Z)", "Name (Z-A)", "Group (A-Z)", "Group (Z-A)",
                    "Email (A-Z)", "Phone (A-Z)", "Department (A-Z)", "Organization (A-Z)"});
    private final JButton saveButton = new JButton("Add");
    private final DefaultTableModel tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public ContactDirectoryApp() {
        setTitle(AppEnvironment.windowTitle());
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(new Dimension(800, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            reloadContacts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not load contacts: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        groupCombo.setSelectedItem(ContactStorage.DEFAULT_GROUP);
        buildLayout();
        applyFilters();
    }

    private void buildLayout() {
        JPanel main = new JPanel(new BorderLayout());
        main.add(buildSidebar(), BorderLayout.WEST);
        main.add(buildContentPane(), BorderLayout.CENTER);
        setContentPane(main);
    }

    private JScrollPane buildSidebar() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(new Color(0xF0F0F0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 12, 2, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        gbc.gridy = row++;
        gbc.insets = new Insets(10, 12, 6, 12);
        JLabel title = new JLabel("Contact Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        sidebar.add(title, gbc);

        gbc.insets = new Insets(2, 12, 2, 12);
        row = addField(sidebar, gbc, row, "Name", nameField);
        row = addField(sidebar, gbc, row, "Phone", phoneField);
        row = addField(sidebar, gbc, row, "Extension", extensionField);
        row = addField(sidebar, gbc, row, "Email", emailField);
        row = addField(sidebar, gbc, row, "Department", departmentField);
        row = addField(sidebar, gbc, row, "Organization", organizationField);

        gbc.gridy = row++;
        sidebar.add(new JLabel("Group"), gbc);
        gbc.gridy = row++;
        sidebar.add(groupCombo, gbc);

        gbc.gridy = row++;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);
        saveButton.addActionListener(e -> saveContact());
        btnRow.add(saveButton);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearForm());
        btnRow.add(clearBtn);
        sidebar.add(btnRow, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(10, 12, 4, 12);
        JLabel filterTitle = new JLabel("Filter by Group");
        filterTitle.setFont(filterTitle.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(filterTitle, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 12, 2, 12);
        filterCombo.addActionListener(e -> applyFilters());
        sidebar.add(filterCombo, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(10, 12, 4, 12);
        JLabel sortTitle = new JLabel("Sort by");
        sortTitle.setFont(sortTitle.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(sortTitle, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(2, 12, 2, 12);
        sortCombo.addActionListener(e -> applyFilters());
        sidebar.add(sortCombo, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 12, 4, 12);
        JLabel searchTitle = new JLabel("Search");
        searchTitle.setFont(searchTitle.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(searchTitle, gbc);

        gbc.gridy = row++;
        sidebar.add(searchField, gbc);

        gbc.gridy = row++;
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        searchRow.setOpaque(false);
        JButton findBtn = new JButton("Find");
        findBtn.addActionListener(e -> searchContacts());
        searchRow.add(findBtn);
        JButton showAllBtn = new JButton("Show All");
        showAllBtn.addActionListener(e -> showAll());
        searchRow.add(showAllBtn);
        sidebar.add(searchRow, gbc);

        gbc.gridy = row++;
        JPanel ioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ioRow.setOpaque(false);
        JButton importBtn = new JButton("Import CSV");
        importBtn.addActionListener(e -> importCsv());
        ioRow.add(importBtn);
        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportCsv());
        ioRow.add(exportBtn);
        sidebar.add(ioRow, gbc);

        gbc.gridy = row++;
        gbc.weighty = 1;
        JLabel hint = new JLabel("<html><small>Double-click a row to edit</small></html>");
        hint.setForeground(new Color(0x555555));
        sidebar.add(hint, gbc);

        JScrollPane scroll = new JScrollPane(sidebar);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(250, WINDOW_HEIGHT));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridy = row++;
        panel.add(new JLabel(label), gbc);
        gbc.gridy = row++;
        panel.add(field, gbc);
        return row;
    }

    private JPanel buildContentPane() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.setBackground(Color.WHITE);

        JLabel tableTitle = new JLabel("Contacts");
        tableTitle.setFont(tableTitle.getFont().deriveFont(Font.BOLD, 14f));
        content.add(tableTitle, BorderLayout.NORTH);

        table.setRowHeight(22);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(45);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(65);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onRowDoubleClick();
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        return content;
    }

    private void reloadContacts() throws SQLException {
        contacts.clear();
        contacts.addAll(ContactStorage.load());
    }

    private Contact findById(int id) {
        for (Contact c : contacts) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void refreshTable(List<Contact> displayed) {
        tableModel.setRowCount(0);
        displayedIds.clear();

        for (Contact c : displayed) {
            displayedIds.add(c.getId());
            tableModel.addRow(new Object[]{
                    c.getName(), c.getPhone(), c.getExtension(), c.getEmail(),
                    c.getDepartment(), c.getOrganization(), c.getGroup()
            });
        }
    }

    private List<Contact> buildDisplayedList() {
        String groupFilter = (String) filterCombo.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();
        List<Contact> displayed = new ArrayList<>();
        for (Contact c : contacts) {
            if (!"All".equals(groupFilter) && !c.getGroup().equals(groupFilter)) {
                continue;
            }
            if (!query.isEmpty() && !c.getName().toLowerCase().contains(query)) {
                continue;
            }
            displayed.add(c);
        }
        sortContacts(displayed);
        return displayed;
    }

    private void sortContacts(List<Contact> list) {
        String sortBy = (String) sortCombo.getSelectedItem();
        if (sortBy == null) {
            sortBy = "Name (A-Z)";
        }
        Comparator<Contact> comparator = switch (sortBy) {
            case "Name (Z-A)" -> Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER)
                    .reversed();
            case "Group (A-Z)" -> Comparator.comparing(Contact::getGroup, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            case "Group (Z-A)" -> Comparator.comparing(Contact::getGroup, String.CASE_INSENSITIVE_ORDER)
                    .reversed()
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            case "Email (A-Z)" -> Comparator.comparing(Contact::getEmail, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            case "Phone (A-Z)" -> Comparator.comparing(Contact::getPhone, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            case "Department (A-Z)" -> Comparator.comparing(Contact::getDepartment, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            case "Organization (A-Z)" -> Comparator.comparing(Contact::getOrganization, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Contact::getName, String.CASE_INSENSITIVE_ORDER);
        };
        list.sort(comparator);
    }

    private void applyFilters() {
        refreshTable(buildDisplayedList());
    }

    private void clearForm() {
        editingId = null;
        saveButton.setText("Add");
        nameField.setText("");
        phoneField.setText("");
        extensionField.setText("");
        emailField.setText("");
        departmentField.setText("");
        organizationField.setText("");
        groupCombo.setSelectedItem(ContactStorage.DEFAULT_GROUP);
    }

    private Contact readForm() {
        String group = (String) groupCombo.getSelectedItem();
        if (group == null || group.isEmpty()) {
            group = ContactStorage.DEFAULT_GROUP;
        }
        return new Contact(
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                group,
                departmentField.getText().trim(),
                organizationField.getText().trim(),
                extensionField.getText().trim());
    }

    private void saveContact() {
        Contact data = readForm();
        if (data.getName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String error = ContactValidator.validate(data.getPhone(), data.getEmail(), data.getExtension());
        if (error != null) {
            JOptionPane.showMessageDialog(this, error,
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ContactStorage.CONTACT_GROUPS.contains(data.getGroup())) {
            JOptionPane.showMessageDialog(this,
                    "Group must be one of: Work, Family, Friends.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (editingId != null) {
                data.setId(editingId);
                ContactStorage.update(data);
                JOptionPane.showMessageDialog(this,
                        "Contact '" + data.getName() + "' updated.",
                        "Update", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ContactStorage.insert(data);
                JOptionPane.showMessageDialog(this,
                        "Contact '" + data.getName() + "' added.",
                        "Add", JOptionPane.INFORMATION_MESSAGE);
            }
            reloadContacts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save contact: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        clearForm();
        applyFilters();
    }

    private void onRowDoubleClick() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || viewRow >= displayedIds.size()) {
            return;
        }

        int id = displayedIds.get(viewRow);
        Contact c = findById(id);
        if (c == null) {
            return;
        }

        editingId = id;
        saveButton.setText("Update");
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        extensionField.setText(c.getExtension());
        emailField.setText(c.getEmail());
        departmentField.setText(c.getDepartment());
        organizationField.setText(c.getOrganization());
        groupCombo.setSelectedItem(c.getGroup());
    }

    private void searchContacts() {
        if (searchField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a name to search.",
                    "Search", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Contact> matches = buildDisplayedList();
        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No contacts match your search.",
                    "Search", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        refreshTable(matches);
    }

    private void showAll() {
        searchField.setText("");
        filterCombo.setSelectedItem("All");
        applyFilters();
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Contacts");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        try {
            ContactStorage.ImportResult result = ContactStorage.importCsv(path);
            reloadContacts();
            applyFilters();

            StringBuilder message = new StringBuilder();
            message.append("Imported ").append(result.imported()).append(" contact(s).");
            if (result.skipped() > 0) {
                message.append("\nSkipped ").append(result.skipped()).append(" row(s).");
            }
            if (result.hasErrors()) {
                message.append("\n\n");
                int limit = Math.min(result.errors().size(), 8);
                for (int i = 0; i < limit; i++) {
                    message.append(result.errors().get(i)).append('\n');
                }
                if (result.errors().size() > limit) {
                    message.append("... and ").append(result.errors().size() - limit).append(" more.");
                }
            }

            int type = result.imported() > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            JOptionPane.showMessageDialog(this, message.toString(), "Import", type);
        } catch (java.io.IOException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not import file:\n" + e.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (contacts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No contacts to export.",
                    "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Contacts");
        chooser.setSelectedFile(new java.io.File("contacts.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        String filename = path.getFileName().toString();
        if (!filename.toLowerCase().endsWith(".csv")) {
            path = path.resolveSibling(filename + ".csv");
        }

        try {
            ContactStorage.exportCsv(contacts, path);
            JOptionPane.showMessageDialog(this,
                    "Exported " + contacts.size() + " contact(s) to:\n" + path,
                    "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save file:\n" + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (args.length > 0 && "--cli".equals(args[0])) {
                ContactDirectoryCli.run();
            } else {
                new ContactDirectoryApp().setVisible(true);
            }
        });
    }
}
