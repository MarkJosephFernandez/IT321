package ui;

import models.Account;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardWindow extends JFrame {
    private JButton productsButton;
    private JButton salesButton;
    private JButton reportsButton;
    private JButton logoutButton;
    private JPanel panelMain;

    public DashboardWindow(Account account) {
        setTitle("POS Dashboard - " + account.getRole());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        panelMain = new JPanel();
        panelMain.setLayout(null);

        productsButton = new JButton("Manage Products");
        productsButton.setBounds(150, 50, 200, 40);
        panelMain.add(productsButton);

        salesButton = new JButton("Process Sale");
        salesButton.setBounds(150, 110, 200, 40);
        panelMain.add(salesButton);

        reportsButton = new JButton("Reports");
        reportsButton.setBounds(150, 170, 200, 40);
        panelMain.add(reportsButton);

        logoutButton = new JButton("Logout");
        logoutButton.setBounds(150, 230, 200, 40);
        panelMain.add(logoutButton);

        add(panelMain);

        // Button actions
        productsButton.addActionListener(e -> new ProductManagementWindow().setVisible(true));dispose();
        salesButton.addActionListener(e -> new SalesWindow(account).setVisible(true));dispose();
        reportsButton.addActionListener(e -> new ReportsWindow().setVisible(true));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginWindow().setVisible(true);
        });
    }
}
