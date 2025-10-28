package ui;

import models.Account;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardWindow extends JFrame {
    private JButton productsButton;
    private JButton salesButton;
    private JButton usersButton;
    private JButton reportsButton;
    private JButton inventoryButton;
    private JButton settingsButton;
    private JButton logoutButton;
    private final Account userAccount;
    private JLabel statusLabel;
    private JLabel clockLabel;
    private Timer clockTimer;

    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color HOVER_COLOR = new Color(52, 152, 219);

    public DashboardWindow(Account account) {
        this.userAccount = account;

        // --- Frame Setup ---
        setTitle("POS Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Main Container ---
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(236, 240, 241));

        // --- 1. Top Navigation Bar ---
        mainContainer.add(createTopNavBar(), BorderLayout.NORTH);

        // --- 2. Main Content Area ---
        mainContainer.add(createMainContent(), BorderLayout.CENTER);

        // --- 3. Bottom Status Bar ---
        mainContainer.add(createStatusBar(), BorderLayout.SOUTH);

        add(mainContainer);

        // Setup action listeners AFTER all buttons are created
        setupActionListeners();

        // Start clock timer
        startClock();

        // Apply RBAC
        configureAccess();
    }

    /**
     * Creates the top navigation bar with branding and user info
     */
    private JPanel createTopNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(SECONDARY_COLOR);
        navBar.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Left side - Branding
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel brandLabel = new JLabel("🏪 POS System");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        brandLabel.setForeground(Color.WHITE);
        leftPanel.add(brandLabel);

        // Right side - User info and clock
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clockLabel.setForeground(new Color(189, 195, 199));
        rightPanel.add(clockLabel);

        JLabel userLabel = new JLabel("👤 " + userAccount.getFirstName() + " " + userAccount.getLastName());
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);
        rightPanel.add(userLabel);

        JLabel roleLabel = new JLabel(userAccount.getRole().toUpperCase());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(ACCENT_COLOR);
        roleLabel.setBorder(new CompoundBorder(
                new LineBorder(ACCENT_COLOR, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        rightPanel.add(roleLabel);

        navBar.add(leftPanel, BorderLayout.WEST);
        navBar.add(rightPanel, BorderLayout.EAST);

        return navBar;
    }

    /**
     * Creates the main content area with dashboard cards
     */
    private JPanel createMainContent() {
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(236, 240, 241));
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Welcome header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + userAccount.getFirstName() + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcomeLabel.setForeground(SECONDARY_COLOR);
        headerPanel.add(welcomeLabel);

        JLabel subtitleLabel = new JLabel("What would you like to do today?");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.add(welcomeLabel, BorderLayout.NORTH);
        headerWrapper.add(subtitleLabel, BorderLayout.CENTER);

        // Dashboard cards grid
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setOpaque(false);

        // Create dashboard cards
        salesButton = createDashboardCard("🛒", "New Sale", "Process customer transactions", PRIMARY_COLOR);
        productsButton = createDashboardCard("📦", "Products", "Manage inventory & pricing", new Color(155, 89, 182));
        reportsButton = createDashboardCard("📊", "Reports", "Sales analytics & insights", new Color(230, 126, 34));
        usersButton = createDashboardCard("👥", "Users", "Manage user accounts", new Color(52, 152, 219));
        inventoryButton = createDashboardCard("📋", "Inventory", "Stock levels & alerts", new Color(26, 188, 156));
        settingsButton = createDashboardCard("⚙️", "Settings", "System configuration", new Color(149, 165, 166));

        cardsPanel.add(salesButton);
        cardsPanel.add(productsButton);
        cardsPanel.add(reportsButton);
        cardsPanel.add(usersButton);
        cardsPanel.add(inventoryButton);
        cardsPanel.add(settingsButton);

        contentPanel.add(headerWrapper, BorderLayout.NORTH);
        contentPanel.add(cardsPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    /**
     * Creates an enhanced dashboard card button
     */
    private JButton createDashboardCard(String icon, String title, String description, Color accentColor) {
        JButton card = new JButton();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setFocusPainted(false);

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descLabel.setForeground(new Color(127, 140, 141));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (card.isEnabled()) {
                    card.setBackground(new Color(248, 249, 250));
                    card.setBorder(new CompoundBorder(
                            new LineBorder(accentColor, 2, true),
                            new EmptyBorder(20, 20, 20, 20)
                    ));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (card.isEnabled()) {
                    card.setBackground(CARD_BG);
                    card.setBorder(new CompoundBorder(
                            new LineBorder(new Color(220, 220, 220), 1, true),
                            new EmptyBorder(20, 20, 20, 20)
                    ));
                }
            }
        });

        return card;
    }

    /**
     * Creates the bottom status bar
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(SECONDARY_COLOR);
        statusBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        logoutButton = new JButton("🚪 Logout");
        logoutButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(DANGER_COLOR);
        logoutButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(192, 57, 43));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(DANGER_COLOR);
            }
        });

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(logoutButton, BorderLayout.EAST);

        return statusBar;
    }

    /**
     * Setup action listeners for all buttons
     */
    private void setupActionListeners() {
        salesButton.addActionListener(e -> {
            updateStatus("Opening Sales Module...");
            openWindow(new SalesWindow(userAccount));
        });

        productsButton.addActionListener(e -> {
            updateStatus("Opening Product Management...");
            openWindow(new ProductManagementWindow());
        });

        reportsButton.addActionListener(e -> {
            updateStatus("Loading Reports...");
            openWindow(new ReportsWindow());
        });

        usersButton.addActionListener(e -> {
            updateStatus("Opening User Management...");
            openWindow(new UsersWindow());
        });

        inventoryButton.addActionListener(e -> {
            updateStatus("Opening Inventory Module...");
            JOptionPane.showMessageDialog(this, "Inventory module coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        settingsButton.addActionListener(e -> {
            updateStatus("Opening Settings...");
            JOptionPane.showMessageDialog(this, "Settings module coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        logoutButton.addActionListener(e -> handleLogout());
    }

    /**
     * Handle window opening
     */
    private void openWindow(JFrame newWindow) {
        newWindow.setVisible(true);
        updateStatus("Window opened successfully");
    }

    /**
     * Handle logout with confirmation
     */
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            stopClock();
            dispose();
            new LoginWindow().setVisible(true);
        }
    }

    /**
     * Apply Role-Based Access Control
     */
    private void configureAccess() {
        String role = userAccount.getRole().toUpperCase();

        if ("STAFF".equals(role)) {
            // Staff: Can process sales and view products
            salesButton.setEnabled(true);
            productsButton.setEnabled(true);
            inventoryButton.setEnabled(true);

            reportsButton.setEnabled(false);
            usersButton.setEnabled(false);
            settingsButton.setEnabled(false);

            setCardDisabled(reportsButton, "Admin Only");
            setCardDisabled(usersButton, "Admin Only");
            setCardDisabled(settingsButton, "Admin Only");

        } else if ("ADMIN".equals(role)) {
            // Admin: Full access
            salesButton.setEnabled(true);
            productsButton.setEnabled(true);
            reportsButton.setEnabled(true);
            usersButton.setEnabled(true);
            inventoryButton.setEnabled(true);
            settingsButton.setEnabled(true);

        } else {
            // Unknown role: Lock everything
            disableAllCards();
            JOptionPane.showMessageDialog(
                    this,
                    "Unknown role assigned. Please contact your administrator.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        updateStatus("Access configured for " + role + " role");
    }

    /**
     * Disable a card and show reason
     */
    private void setCardDisabled(JButton card, String reason) {
        card.setEnabled(false);
        card.setBackground(new Color(245, 245, 245));
        card.setToolTipText("🔒 " + reason);
    }

    /**
     * Disable all dashboard cards
     */
    private void disableAllCards() {
        setCardDisabled(salesButton, "Access Denied");
        setCardDisabled(productsButton, "Access Denied");
        setCardDisabled(reportsButton, "Access Denied");
        setCardDisabled(usersButton, "Access Denied");
        setCardDisabled(inventoryButton, "Access Denied");
        setCardDisabled(settingsButton, "Access Denied");
    }

    /**
     * Update status bar message
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Start the clock timer
     */
    private void startClock() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
    }

    /**
     * Stop the clock timer
     */
    private void stopClock() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
    }

    /**
     * Update clock display
     */
    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm:ss");
        clockLabel.setText(now.format(formatter));
    }
}