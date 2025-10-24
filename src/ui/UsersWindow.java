package ui;

import dao.AccountDAO;
import models.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class UsersWindow extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public UsersWindow() {
        // --- Frame Setup ---
        setTitle("👥 User Management");
        setSize(700, 450); // Slightly larger
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Close this window, not the whole app
        setLayout(new BorderLayout(10, 10)); // Add spacing to the main layout

        // === Table ===
        String[] cols = {"ID", "Username", "Role", "First Name", "Last Name"};
        tableModel = new DefaultTableModel(cols, 0) {
            // Override isCellEditable to prevent direct table editing
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one row at a time
        userTable.getTableHeader().setReorderingAllowed(false); // Prevent column reordering

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("User List"));

        // === Buttons & Panel ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5)); // Align buttons to the right

        // Initialize new Edit button
        editButton = new JButton("Edit User");
        addButton = new JButton("Add New User");
        deleteButton = new JButton("Delete User");
        refreshButton = new JButton("Refresh Data");

        // Add icons for better visual appeal
        addButton.setIcon(UIManager.getIcon("FileChooser.upFolderIcon"));
        editButton.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        deleteButton.setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
        refreshButton.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));

        buttonPanel.add(refreshButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL)); // Separator for better grouping
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Add padding around the button panel
        buttonPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        // === Add components to Frame ===
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // === Actions ===
        refreshButton.addActionListener(this::refreshData);
        addButton.addActionListener(this::addUser);
        editButton.addActionListener(this::editUser); // New action
        deleteButton.addActionListener(this::deleteUser);

        // Add a mouse listener for quick double-click editing (UX improvement)
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && userTable.getSelectedRow() != -1) {
                    editUser(null);
                }
            }
        });

        // Load initial data
        refreshData(null);
    }

    // ----------------------------------------------------------------------------------
    // ACTION METHODS
    // ----------------------------------------------------------------------------------

    private void refreshData(ActionEvent e) {
        try {
            // Clear current data
            tableModel.setRowCount(0);

            // Fetch fresh data
            AccountDAO dao = new AccountDAO();
            List<Account> users = dao.getAllAccounts();

            for (Account u : users) {
                tableModel.addRow(new Object[]{
                        u.getAccountId(),
                        u.getUsername(),
                        // Display role more clearly if needed, e.g., using a switch
                        u.getRole(),
                        u.getFirstName() == null ? "-" : u.getFirstName(),
                        u.getLastName() == null ? "-" : u.getLastName()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addUser(ActionEvent e) {
        // Use a dedicated dialog for better input collection
        new UserFormDialog(this, null).setVisible(true);
        refreshData(null); // Refresh data after the dialog closes
    }

    private void editUser(ActionEvent e) {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            try {
                int userId = (int) tableModel.getValueAt(row, 0);
                AccountDAO dao = new AccountDAO();
                Account userToEdit = dao.getAccountById(userId); // Fetch the full account object

                if (userToEdit != null) {
                    // Use the dedicated dialog to edit the existing user
                    new UserFormDialog(this, userToEdit).setVisible(true);
                    refreshData(null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error fetching user data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteUser(ActionEvent e) {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            int userId = (int) tableModel.getValueAt(row, 0);
            String username = (String) tableModel.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete user: " + username + "? This action is irreversible.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    AccountDAO dao = new AccountDAO();
                    dao.deleteAccount(userId);
                    JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}


class UserFormDialog extends JDialog {
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> roleComboBox;
    private JPasswordField passwordField;
    private JButton saveButton;
    private Account account; // null for add, non-null for edit

    public UserFormDialog(Frame owner, Account acc) {
        super(owner, acc == null ? "Add New User" : "Edit User", true); // Modal dialog
        this.account = acc;

        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // 1. Username
        gbc.gridx = 0; gbc.gridy = row; gbc.anchor = GridBagConstraints.WEST; formPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(usernameField, gbc);

        // 2. First Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("First Name:"), gbc);
        firstNameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(firstNameField, gbc);

        // 3. Last Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Last Name:"), gbc);
        lastNameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(lastNameField, gbc);

        // 4. Role
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Role:"), gbc);
        roleComboBox = new JComboBox<>(new String[]{"ADMIN", "STAFF"});
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(roleComboBox, gbc);

        // 5. Password (Only required for add, or can be used for change)
        String passwordLabel = (account == null) ? "Password:" : "New Password (Leave blank to keep old):";
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel(passwordLabel), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(passwordField, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton(account == null ? "Create User" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Load existing data if editing ---
        if (account != null) {
            loadUserData();
        } else {
            // For new users, ensure password field is visible
            passwordField.setVisible(true);
        }

        // --- Listeners ---
        saveButton.addActionListener(this::saveUser);
        cancelButton.addActionListener(e -> dispose());

        // --- Final Assembly ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void loadUserData() {
        usernameField.setText(account.getUsername());
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        roleComboBox.setSelectedItem(account.getRole());

        // Prevent editing the username for security/data integrity
        usernameField.setEnabled(false);

        // Clear password field to avoid displaying hash/old password
        passwordField.setText("");
    }

    private void saveUser(ActionEvent e) {
        // Basic Validation
        if (usernameField.getText().trim().isEmpty() || firstNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and First Name cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            AccountDAO dao = new AccountDAO();

            // If adding a new user, create a new Account object
            if (account == null) {
                account = new Account();
                account.setUsername(usernameField.getText().trim());

                // New user MUST have a password
                if (passwordField.getPassword().length == 0) {
                    JOptionPane.showMessageDialog(this, "New users must have a password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Set common properties
            account.setFirstName(firstNameField.getText().trim());
            account.setLastName(lastNameField.getText().trim());
            account.setRole((String) roleComboBox.getSelectedItem());

            // Handle Password Change/Set
            char[] passwordChars = passwordField.getPassword();
            if (passwordChars.length > 0) {
                // Only update password if field is not empty
                String newPassword = new String(passwordChars);
                account.setPassword(newPassword);
            }
            // Securely erase the temporary password string
            java.util.Arrays.fill(passwordChars, ' ');

            if (account.getAccountId() == 0) { // New user (ID not set yet)
                dao.addAccount(account);
                JOptionPane.showMessageDialog(this, "User added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else { // Existing user (ID already set)
                dao.updateAccount(account);
                JOptionPane.showMessageDialog(this, "User updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose(); // Close the dialog
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}