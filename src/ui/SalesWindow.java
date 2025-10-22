package ui;


import dao.ProductDAO;
import dao.SaleDAO;
import models.Account;
import models.Product;
import models.Sale;
import models.SaleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesWindow extends JFrame {

    private JComboBox<String> productDropdown;
    private JTextField qtyField;
    private JButton addButton, completeSaleButton, removeItemButton;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;

    private Account currentUser;
    private List<SaleItem> cartItems = new ArrayList<>();
    private Map<String, Product> productMap = new HashMap<>();

    public SalesWindow(Account user) {
        this.currentUser = user;

        setTitle("Process Sale - " + user.getUsername());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // === Top panel: product selector ===
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder("Add Product"));

        productDropdown = new JComboBox<>();
        productDropdown.setPreferredSize(new Dimension(300, 30));

        qtyField = new JTextField(5);
        addButton = new JButton("Add to Cart");
        addButton.setBackground(new Color(46, 204, 113));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        topPanel.add(new JLabel("Product:"));
        topPanel.add(productDropdown);
        topPanel.add(new JLabel("Qty:"));
        topPanel.add(qtyField);
        topPanel.add(addButton);

        // === Cart table ===
        String[] cols = {"Product ID", "Name", "Qty", "Price", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));

        // === Bottom panel: total + buttons ===
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalLabel = new JLabel("Total: ₱0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(52, 73, 94));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        removeItemButton = new JButton("Remove Item");
        removeItemButton.setBackground(new Color(231, 76, 60));
        removeItemButton.setForeground(Color.WHITE);
        removeItemButton.setFocusPainted(false);

        completeSaleButton = new JButton("Complete Sale");
        completeSaleButton.setBackground(new Color(52, 152, 219));
        completeSaleButton.setForeground(Color.WHITE);
        completeSaleButton.setFocusPainted(false);
        completeSaleButton.setFont(new Font("Arial", Font.BOLD, 14));

        buttonPanel.add(removeItemButton);
        buttonPanel.add(completeSaleButton);

        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // === Add panels ===
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadProducts();

        // === Button actions ===
        addButton.addActionListener(e -> addToCart());
        removeItemButton.addActionListener(e -> removeFromCart());
        completeSaleButton.addActionListener(e -> completeSale());

        // Enter key to add to cart
        qtyField.addActionListener(e -> addToCart());
    }

    private void loadProducts() {
        try {
            ProductDAO dao = new ProductDAO();
            List<Product> products = dao.getAllProducts();

            productDropdown.addItem("-- Select Product --");

            for (Product p : products) {
                String displayText = p.getSku() + " - " + p.getName() + " (₱" +
                        String.format("%.2f", p.getPrice()) + ")";
                productDropdown.addItem(displayText);
                productMap.put(displayText, p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        String selected = (String) productDropdown.getSelectedItem();

        if (selected == null || selected.equals("-- Select Product --")) {
            JOptionPane.showMessageDialog(this, "Please select a product.",
                    "Invalid Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product product = productMap.get(selected);
        if (product == null) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity (positive number).",
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if product already in cart
        boolean found = false;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            int productId = (int) cartModel.getValueAt(i, 0);
            if (productId == product.getProductId()) {
                // Update quantity
                int oldQty = (int) cartModel.getValueAt(i, 2);
                int newQty = oldQty + qty;
                double subtotal = product.getPrice() * newQty;

                cartModel.setValueAt(newQty, i, 2);
                cartModel.setValueAt(subtotal, i, 4);

                // Update cart items list
                for (SaleItem item : cartItems) {
                    if (item.getProductId() == product.getProductId()) {
                        item.setQty(newQty);
                        break;
                    }
                }

                found = true;
                break;
            }
        }

        if (!found) {
            // Add new item
            double subtotal = product.getPrice() * qty;
            cartModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    qty,
                    String.format("%.2f", product.getPrice()),
                    String.format("%.2f", subtotal)
            });

            SaleItem item = new SaleItem();
            item.setProductId(product.getProductId());
            item.setQty(qty);
            item.setPrice(product.getPrice());
            cartItems.add(item);
        }

        updateTotal();
        qtyField.setText("");
        qtyField.requestFocus();
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (int) cartModel.getValueAt(selectedRow, 0);

        // Remove from table
        cartModel.removeRow(selectedRow);

        // Remove from cart items
        cartItems.removeIf(item -> item.getProductId() == productId);

        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (SaleItem item : cartItems) {
            total += item.getQty() * item.getPrice();
        }
        totalLabel.setText("Total: ₱" + String.format("%.2f", total));
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty! Please add items before completing sale.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask for payment method
        String[] paymentOptions = {"CASH", "CREDIT CARD", "DEBIT CARD", "GCASH", "PAYMAYA"};
        String paymentMethod = (String) JOptionPane.showInputDialog(
                this,
                "Select payment method:",
                "Payment Method",
                JOptionPane.QUESTION_MESSAGE,
                null,
                paymentOptions,
                paymentOptions[0]
        );

        if (paymentMethod == null) {
            return; // User cancelled
        }

        try {
            SaleDAO saleDAO = new SaleDAO();

            Sale sale = new Sale();
            sale.setAccountId(currentUser.getAccountId());
            sale.setItems(cartItems);

            double total = 0;
            for (SaleItem item : cartItems) {
                total += item.getQty() * item.getPrice();
            }
            sale.setTotalAmount(total);
            sale.setPaymentMethod(paymentMethod);

            saleDAO.addSale(sale); // save to DB

            JOptionPane.showMessageDialog(this,
                    "Sale completed successfully!\nTotal: ₱" + String.format("%.2f", total),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Clear cart
            cartModel.setRowCount(0);
            cartItems.clear();
            updateTotal();

            // Optionally close window
            // dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error completing sale: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}