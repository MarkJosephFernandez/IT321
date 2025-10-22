package ui;

import dao.AccountDAO;
import models.Account;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent; // For mnemonic (Alt key shortcut)

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginWindow() {
        // --- Frame Setup ---
        setTitle("POS System - User Login 🔑");
        // Set minimum size and let layout manager determine preferred size
        setMinimumSize(new Dimension(350, 230));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // --- Panel Setup with Layout Managers ---
        JPanel panelMain = new JPanel(new BorderLayout(10, 10)); // Outer panel with BorderLayout
        panelMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // Center panel for login fields using GridBagLayout for flexible alignment
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title/Header
        JLabel titleLabel = new JLabel("System Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        panelMain.add(titleLabel, BorderLayout.NORTH);

        // 1. Username Label and Field
        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; // Column 0
        gbc.gridy = 0; // Row 0
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0; // Don't allow label to grow horizontally
        loginPanel.add(userLabel, gbc);

        usernameField = new JTextField(15); // Preferred column width hint
        gbc.gridx = 1; // Column 1
        gbc.gridy = 0; // Row 0
        gbc.weightx = 1.0; // Allow field to grow horizontally
        loginPanel.add(usernameField, gbc);

        // 2. Password Label and Field
        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; // Column 0
        gbc.gridy = 1; // Row 1
        gbc.weightx = 0.0;
        loginPanel.add(passLabel, gbc);

        passwordField = new JPasswordField(15); // Preferred column width hint
        gbc.gridx = 1; // Column 1
        gbc.gridy = 1; // Row 1
        gbc.weightx = 1.0;
        loginPanel.add(passwordField, gbc);

        panelMain.add(loginPanel, BorderLayout.CENTER);

        // 3. Login Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        loginButton.setMnemonic(KeyEvent.VK_L); // Alt+L to focus/click
        loginButton.setPreferredSize(new Dimension(100, 30)); // Set a standard size
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        buttonPanel.add(loginButton);

        panelMain.add(buttonPanel, BorderLayout.SOUTH);

        // --- Final Frame Assembly ---
        add(panelMain);
        pack(); // Resize the frame based on its contents' preferred sizes

        // --- UX Improvement: Default button and initial focus ---
        getRootPane().setDefaultButton(loginButton); // Enter key presses the Login button
        usernameField.requestFocusInWindow(); // Set initial focus to the username field

        // --- Login Event Handling ---
        loginButton.addActionListener(e -> attemptLogin());
    }

    /**
     * Centralized method to handle the login logic.
     */
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        // Clear sensitive password data immediately after conversion
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        // Basic input validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            // Re-focus based on which field is empty
            if (username.isEmpty()) {
                usernameField.requestFocusInWindow();
            } else {
                passwordField.requestFocusInWindow();
            }
            // Clear password field for security on failed attempt
            passwordField.setText("");
            return;
        }

        // Database logic
        try {
            AccountDAO dao = new AccountDAO();
            Account acc = dao.login(username, password);

            if (acc != null) {
                // Success
                JOptionPane.showMessageDialog(this,
                        "Welcome, " + acc.getFirstName() + "!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose(); // Close login window
                new DashboardWindow(acc).setVisible(true); // Open main application
            } else {
                // Failure
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Please try again.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);

                // Clear password field for security on failed attempt
                passwordField.setText("");
                usernameField.requestFocusInWindow(); // Focus back to username field
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "A database error occurred. See console for details.",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Securely overwrite the password string/array after use
            java.util.Arrays.fill(passwordChars, ' ');
        }
    }

    // Example main method for testing (Optional, but good practice)

    }
