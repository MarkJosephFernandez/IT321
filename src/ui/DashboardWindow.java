package ui;

import models.Account;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardWindow extends JFrame {
    private JButton productsButton;
    private JButton salesButton;
    private JButton usersButton; // Added User Management button
    private JButton reportsButton;
    private JButton logoutButton;
    private final Account userAccount;

    public DashboardWindow(Account account) {
        // Store account for role checks
        this.userAccount = account;

        // --- Frame Setup ---
        setTitle("POS Dashboard - Welcome, " + account.getFirstName() + " (" + account.getRole() + ")");
        setSize(700, 500); // Larger size for a dashboard
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Use BorderLayout for the main structure
        setLayout(new BorderLayout(15, 15));

        // Add padding around the entire frame content
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. Header Panel (NORTH) ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel welcomeLabel = new JLabel("Welcome back to the POS System, " + account.getFirstName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerPanel.add(welcomeLabel);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Main Content Panel (CENTER) ---
        // Use GridLayout for an organized 2x2 or 2x3 button layout
        JPanel mainContentPanel = new JPanel(new GridLayout(3, 2, 20, 20));

        // --- Initialize Buttons ---
        productsButton = createDashboardButton("📦 Manage Products", "View, add, and update product inventory.");
        salesButton = createDashboardButton("🛒 Process New Sale", "Start a new transaction for a customer.");
        reportsButton = createDashboardButton("📊 View Reports", "Analyze sales trends and business performance.");

        // New button for User Management (RBAC)
        usersButton = createDashboardButton("👥 Manage Users", "Create, edit, or delete system user accounts.");

        logoutButton = createDashboardButton("🚪 Logout", "Sign out and return to the login screen.");

        // --- Add Buttons to Panel ---
        mainContentPanel.add(salesButton); // Most frequent action first
        mainContentPanel.add(productsButton);
        mainContentPanel.add(reportsButton);
        mainContentPanel.add(usersButton); // Place next to reports
        mainContentPanel.add(new JLabel()); // Filler cell
        mainContentPanel.add(logoutButton);

        // --- 3. Role-Based Access Control (RBAC) ---
        // Disable or hide buttons based on user role
        configureAccess();

        // Add the main content panel to the frame
        add(mainContentPanel, BorderLayout.CENTER);

        // --- 4. Actions ---
        productsButton.addActionListener(e -> openWindow(new ProductManagementWindow()));
        salesButton.addActionListener(e -> openWindow(new SalesWindow(userAccount)));
        reportsButton.addActionListener(e -> openWindow(new ReportsWindow()));
        usersButton.addActionListener(e -> openWindow(new UsersWindow())); // New window

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginWindow().setVisible(true);
            }
        });
    }

    /**
     * Helper to create visually appealing dashboard buttons.
     */
    private JButton createDashboardButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setToolTipText(tooltip);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor on hover
        // Optional: Increase button size or use different colors
        // button.setBackground(new Color(240, 240, 240));
        return button;
    }

    /**
     * Centralized method to handle window opening/closing cleanly.
     * Prevents issues like the one in your original code: productsButton.addActionListener(e -> new ProductManagementWindow().setVisible(true));dispose();
     * The `dispose()` should only happen if you intend to close the dashboard *when* opening a sub-window, which is usually not desired.
     */
    private void openWindow(JFrame newWindow) {
        // You might want to hide the dashboard instead of disposing it,
        // especially for windows that aren't the main focus.
        newWindow.setVisible(true);
        // dispose(); // Removed, as users often want to return to the dashboard
    }

    /**
     * Applies Role-Based Access Control (RBAC) to the dashboard buttons.
     */
    private void configureAccess() {
        String role = userAccount.getRole();

        if ("STAFF".equalsIgnoreCase(role)) {
            // Staff can process sales and manage products, but usually not view sensitive reports or manage users.
            reportsButton.setEnabled(false);
            usersButton.setEnabled(false);
            usersButton.setText("Access Denied (Admin Only)");
            usersButton.setToolTipText("Only Administrators can manage user accounts.");
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            // Admin has full access
            productsButton.setEnabled(true);
            salesButton.setEnabled(true);
            reportsButton.setEnabled(true);
            usersButton.setEnabled(true);
        } else {
            // Default: Lock everything down
            productsButton.setEnabled(false);
            salesButton.setEnabled(false);
            reportsButton.setEnabled(false);
            usersButton.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Unknown role assigned. Access limited.", "Security Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}