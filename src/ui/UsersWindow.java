package ui;

import dao.AccountDAO;
import models.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.regex.Pattern;

public class UsersWindow extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JTextField searchField;
    private JLabel statusLabel;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$");

    public UsersWindow() {
        // --- Frame Setup ---
        setTitle("👥 User Management System");
        setSize(950, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // === Top Panel with Search ===
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by username, name, or role");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        statusLabel = new JLabel("Loading users...");
        statusLabel.setForeground(new Color(100, 100, 100));

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);

        // === Table Setup ===
        String[] cols = {"ID", "Username", "Role", "First Name", "Last Name", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : String.class;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setRowHeight(28);
        userTable.setShowGrid(true);
        userTable.setGridColor(new Color(230, 230, 230));

        // Enable sorting
        sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(0).setMaxWidth(70);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Custom renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        userTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        userTable.getColumnModel().getColumn(2).setCellRenderer(createRoleRenderer());

        // Zebra striping
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createTitledBorder("User Directory")
        ));

        // === Button Panel ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        refreshButton = createStyledButton("🔄 Refresh", new Color(70, 130, 180));
        addButton = createStyledButton("➕ Add User", new Color(34, 139, 34));
        editButton = createStyledButton("✍️ Edit", new Color(255, 140, 0));
        deleteButton = createStyledButton("🗑️ Delete", new Color(220, 20, 60));

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // === Assembly ===
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // === Event Listeners ===
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean selected = userTable.getSelectedRow() != -1;
                editButton.setEnabled(selected);
                deleteButton.setEnabled(selected);
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });

        refreshButton.addActionListener(this::refreshData);
        addButton.addActionListener(this::addUser);
        editButton.addActionListener(this::editUser);
        deleteButton.addActionListener(this::deleteUser);

        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && userTable.getSelectedRow() != -1) {
                    editUser(null);
                }
            }
        });

        // Keyboard shortcuts
        setupKeyboardShortcuts();

        // Initial load
        refreshData(null);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setForeground(color);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private DefaultTableCellRenderer createRoleRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected && value != null) {
                    String role = value.toString();
                    if ("ADMIN".equals(role)) {
                        setForeground(new Color(220, 20, 60));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(new Color(70, 130, 180));
                    }
                }
                return c;
            }
        };
    }

    private void setupKeyboardShortcuts() {
        // Ctrl+F to focus search
        KeyStroke ctrlF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(e -> searchField.requestFocus(),
                ctrlF, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl+N for new user
        KeyStroke ctrlN = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        getRootPane().registerKeyboardAction(e -> addUser(null),
                ctrlN, JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Delete key for delete action
        KeyStroke deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        getRootPane().registerKeyboardAction(e -> {
            if (deleteButton.isEnabled()) deleteUser(null);
        }, deleteKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        int total = tableModel.getRowCount();
        int visible = userTable.getRowCount();
        if (visible == total) {
            statusLabel.setText(String.format("Total Users: %d", total));
        } else {
            statusLabel.setText(String.format("Showing: %d / %d users", visible, total));
        }
    }

    private void refreshData(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("Loading...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                tableModel.setRowCount(0);
                AccountDAO dao = new AccountDAO();
                List<Account> users = dao.getAllAccounts();

                for (Account u : users) {
                    String roleDisplay = u.getRole() != null ? u.getRole().toUpperCase() : "N/A";
                    String created = "—"; // Could add timestamp if available in Account model

                    tableModel.addRow(new Object[]{
                            u.getAccountId(),
                            u.getUsername(),
                            roleDisplay,
                            u.getFirstName() != null && !u.getFirstName().isEmpty() ? u.getFirstName() : "—",
                            u.getLastName() != null && !u.getLastName().isEmpty() ? u.getLastName() : "—",
                            created
                    });
                }

                updateStatusLabel();
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error loading data");
                JOptionPane.showMessageDialog(this,
                        "Failed to load users:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void addUser(ActionEvent e) {
        UserFormDialog dialog = new UserFormDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            refreshData(null);
        }
    }

    private void editUser(ActionEvent e) {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            try {
                int modelRow = userTable.convertRowIndexToModel(row);
                int userId = (int) tableModel.getValueAt(modelRow, 0);
                AccountDAO dao = new AccountDAO();
                Account userToEdit = dao.getAccountById(userId);

                if (userToEdit != null) {
                    UserFormDialog dialog = new UserFormDialog(this, userToEdit);
                    dialog.setVisible(true);
                    if (dialog.isSuccess()) {
                        refreshData(null);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error fetching user data:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser(ActionEvent e) {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = userTable.convertRowIndexToModel(row);
            int userId = (int) tableModel.getValueAt(modelRow, 0);
            String username = (String) tableModel.getValueAt(modelRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("<html><body style='width: 300px'>" +
                            "<h3>⚠️ Confirm Deletion</h3>" +
                            "Are you sure you want to delete user:<br><br>" +
                            "<b>%s</b> (ID: %d)<br><br>" +
                            "This action cannot be undone.</body></html>", username, userId),
                    "Delete User",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    AccountDAO dao = new AccountDAO();
                    dao.deleteAccount(userId);
                    JOptionPane.showMessageDialog(this,
                            "User '" + username + "' deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshData(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error deleting user:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Validation methods
    static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    static boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}

// ============================================================================
// USER FORM DIALOG
// ============================================================================

class UserFormDialog extends JDialog {
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> roleComboBox;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JButton saveButton;
    private JLabel passwordStrengthLabel;
    private Account account;
    private boolean success = false;

    public UserFormDialog(Frame owner, Account acc) {
        super(owner, acc == null ? "✨ Add New User" : "✍️ Edit User", true);
        this.account = acc;

        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Username
        addFormField(formPanel, gbc, row++, "Username:", usernameField = new JTextField(20));
        usernameField.setToolTipText("3-20 characters: letters, numbers, underscore");

        // First Name
        addFormField(formPanel, gbc, row++, "First Name: *", firstNameField = new JTextField(20));

        // Last Name
        addFormField(formPanel, gbc, row++, "Last Name:", lastNameField = new JTextField(20));

        // Role
        addFormField(formPanel, gbc, row++, "Role:", roleComboBox = new JComboBox<>(new String[]{"ADMIN", "STAFF"}));

        // Password
        String passwordLabel = (account == null) ? "Password: *" : "New Password:";
        addFormField(formPanel, gbc, row++, passwordLabel, passwordField = new JPasswordField(20));
        passwordField.setToolTipText("Min 6 chars: at least 1 letter and 1 number");

        // Confirm Password
        if (account == null) {
            addFormField(formPanel, gbc, row++, "Confirm Password: *", confirmPasswordField = new JPasswordField(20));
        } else {
            confirmPasswordField = new JPasswordField(20);
            addFormField(formPanel, gbc, row++, "Confirm New Password:", confirmPasswordField);
        }

        // Show Password Checkbox
        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.gridwidth = 1;
        showPasswordCheckBox = new JCheckBox("Show Password");
        formPanel.add(showPasswordCheckBox, gbc);

        // Password Strength Label
        gbc.gridx = 1;
        gbc.gridy = row++;
        passwordStrengthLabel = new JLabel(" ");
        passwordStrengthLabel.setFont(passwordStrengthLabel.getFont().deriveFont(Font.ITALIC, 11f));
        formPanel.add(passwordStrengthLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        saveButton = new JButton(account == null ? "💾 Create User" : "💾 Save Changes");
        JButton cancelButton = new JButton("❌ Cancel");

        saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Load existing data if editing
        if (account != null) {
            loadUserData();
        }

        // Event Listeners
        showPasswordCheckBox.addActionListener(e -> {
            char echoChar = showPasswordCheckBox.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePasswordStrength();
            }
        });

        saveButton.addActionListener(this::saveUser);
        cancelButton.addActionListener(e -> dispose());

        // Enter key to save
        getRootPane().setDefaultButton(saveButton);

        pack();
        setMinimumSize(new Dimension(450, getPreferredSize().height));
        setLocationRelativeTo(owner);

        if (account == null) {
            usernameField.requestFocusInWindow();
        } else {
            firstNameField.requestFocusInWindow();
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void loadUserData() {
        usernameField.setText(account.getUsername());
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        roleComboBox.setSelectedItem(account.getRole());
        usernameField.setEnabled(false);
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            passwordStrengthLabel.setText(" ");
            return;
        }

        int strength = calculatePasswordStrength(password);
        String[] labels = {"❌ Weak", "⚠️ Fair", "✅ Good", "💪 Strong"};
        Color[] colors = {Color.RED, Color.ORANGE, new Color(34, 139, 34), new Color(0, 100, 0)};

        int index = Math.min(strength, 3);
        passwordStrengthLabel.setText("Strength: " + labels[index]);
        passwordStrengthLabel.setForeground(colors[index]);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[@$!%*#?&].*")) strength++;
        return strength;
    }

    private void saveUser(ActionEvent e) {
        // Validation
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (username.isEmpty() || firstName.isEmpty()) {
            showError("Username and First Name are required.");
            return;
        }

        if (!UsersWindow.isValidUsername(username) && account == null) {
            showError("Invalid username format.\nUse 3-20 characters: letters, numbers, underscore only.");
            return;
        }

        char[] passwordChars = passwordField.getPassword();
        char[] confirmChars = confirmPasswordField.getPassword();

        try {
            AccountDAO dao = new AccountDAO();
            boolean isNewUser = (account == null);

            if (isNewUser) {
                if (passwordChars.length == 0) {
                    showError("Password is required for new users.");
                    return;
                }

                String password = new String(passwordChars);
                if (!UsersWindow.isValidPassword(password)) {
                    showError("Password must be at least 6 characters\nand contain at least 1 letter and 1 number.");
                    return;
                }

                if (!new String(passwordChars).equals(new String(confirmChars))) {
                    showError("Passwords do not match!");
                    return;
                }

                account = new Account();
                account.setUsername(username);
                account.setPassword(password);
            } else {
                // Editing existing user
                if (passwordChars.length > 0) {
                    String password = new String(passwordChars);
                    if (!UsersWindow.isValidPassword(password)) {
                        showError("Password must be at least 6 characters\nand contain at least 1 letter and 1 number.");
                        return;
                    }

                    if (!new String(passwordChars).equals(new String(confirmChars))) {
                        showError("Passwords do not match!");
                        return;
                    }

                    account.setPassword(password);
                }
            }

            account.setFirstName(firstName);
            account.setLastName(lastName);
            account.setRole((String) roleComboBox.getSelectedItem());

            if (isNewUser) {
                dao.addAccount(account);
                JOptionPane.showMessageDialog(this,
                        "User '" + username + "' created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                dao.updateAccount(account);
                JOptionPane.showMessageDialog(this,
                        "User '" + username + "' updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            success = true;
            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Database error:\n" + ex.getMessage());
        } finally {
            // Security: Clear password arrays
            if (passwordChars != null) java.util.Arrays.fill(passwordChars, ' ');
            if (confirmChars != null) java.util.Arrays.fill(confirmChars, ' ');
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSuccess() {
        return success;
    }
}