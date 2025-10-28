package ui;

import dao.ProductDAO;
import models.Product;
import java.awt.event.KeyAdapter; // <--- ADD THIS IMPORT
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProductManagementWindow extends JFrame {
    private JTable productTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private DefaultTableModel tableModel;

    public ProductManagementWindow() {
        // --- Frame Setup ---
        setTitle("📦 Product Inventory Management");
        setSize(850, 550); // Larger window for better data display
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Close this window only

        // --- Table Setup ---
        String[] columns = {"ID", "SKU", "Name", "Category", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0) {
            // Prevent direct editing in the table
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getTableHeader().setReorderingAllowed(false);

        // UX Enhancement: Double-click to edit
        productTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2 && productTable.getSelectedRow() != -1) {
                    editProduct(null);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // --- Button Panel Setup (FlowLayout.RIGHT for modern look) ---
        addButton = new JButton("➕ Add Product");
        editButton = new JButton("✏️ Edit Selected");
        deleteButton = new JButton("🗑️ Delete Selected");
        refreshButton = new JButton("🔄 Refresh Data");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        buttonPanel.add(refreshButton);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // --- Add components to Frame (BorderLayout) ---
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Actions ---
        refreshButton.addActionListener(this::refreshProducts);
        addButton.addActionListener(this::addProduct);
        editButton.addActionListener(this::editProduct);
        deleteButton.addActionListener(this::deleteProduct);

        // Initial Load
        refreshProducts(null);
    }


    private void refreshProducts(ActionEvent e) {
        tableModel.setRowCount(0);
        ProductDAO dao = new ProductDAO();
        try {
            List<Product> products = dao.getAllProducts();
            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getSku(),
                        p.getName(),
                        p.getCategory(),
                        // Format price for display
                        String.format("%.2f", p.getPrice()),
                        p.getStockQty()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProduct(ActionEvent e) {
        // Open the dialog with a null product to indicate ADD mode
        new ProductFormDialog(this, null).setVisible(true);
        refreshProducts(null); // Refresh table after dialog closes
    }

    private void editProduct(ActionEvent e) {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            try {
                // Get the ID from the first column (index 0)
                int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                ProductDAO dao = new ProductDAO();
                Product productToEdit = dao.getProductById(id); // ASSUMES ProductDAO has getProductById method

                if (productToEdit != null) {
                    // Open the dialog with the retrieved product
                    new ProductFormDialog(this, productToEdit).setVisible(true);
                    refreshProducts(null); // Refresh table after dialog closes
                }
            } catch (NumberFormatException | NullPointerException ex) {
                JOptionPane.showMessageDialog(this, "Invalid product ID selected.", "Data Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error fetching product data.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteProduct(ActionEvent e) {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String name = tableModel.getValueAt(row, 2).toString();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete product: " + name + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                ProductDAO dao = new ProductDAO();
                try {
                    dao.deactivateProduct(id);
                    JOptionPane.showMessageDialog(this, "Product deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshProducts(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void refreshProducts() {
        refreshProducts(null);
    }
}



class ProductFormDialog extends JDialog {
    private JTextField skuField, nameField, priceField, stockField;
    private JComboBox<String> categoryComboBox;
    private JButton saveButton;
    private Product product; // null for add, non-null for edit

    // Assuming you have a way to get categories (e.g., from DB or a constant list)
    private static final String[] CATEGORIES = {"Electronics", "Food", "Beverage", "Apparel", "Other"};

    public ProductFormDialog(Frame owner, Product p) {
        super(owner, p == null ? "Add New Product" : "Edit Product", true); // Modal dialog
        this.product = p;

        setLayout(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize Fields
        skuField = new JTextField(15);
        nameField = new JTextField(15);
        priceField = new JTextField(15);
        stockField = new JTextField(15);
        categoryComboBox = new JComboBox<>(CATEGORIES);

        // Add fields to form
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("SKU:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(skuField, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(categoryComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; formPanel.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 1.0; formPanel.add(stockField, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton(product == null ? "Create Product" : "Save Changes");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Load existing data if editing
        if (product != null) {
            loadProductData();
        }

        // --- Listeners ---
        saveButton.addActionListener(this::saveProduct);
        cancelButton.addActionListener(e -> dispose());

        // Ensure only numbers and decimal point in price field
        priceField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == '.' || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
                // Prevent multiple decimal points
                if (c == '.' && priceField.getText().contains(".")) {
                    e.consume();
                }
            }
        });
        // Ensure only integers in stock field
        stockField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
            }
        });

        // --- Final Assembly ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void loadProductData() {
        skuField.setText(product.getSku());
        nameField.setText(product.getName());
        priceField.setText(String.format("%.2f", product.getPrice()));
        stockField.setText(String.valueOf(product.getStockQty()));
        categoryComboBox.setSelectedItem(product.getCategory());
    }

    private void saveProduct(ActionEvent e) {
        // --- Validation ---
        if (skuField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "SKU, Name, and Price are required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price;
        int stock;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            stock = Integer.parseInt(stockField.getText().trim().isEmpty() ? "0" : stockField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Stock must be valid numbers.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ProductDAO dao = new ProductDAO();

            if (product == null) {
                product = new Product();
                product.setProductId(0); // Mark as new
            }

            // Set properties
            product.setSku(skuField.getText().trim());
            product.setName(nameField.getText().trim());
            product.setCategory((String) categoryComboBox.getSelectedItem());
            product.setPrice(price);
            product.setStockQty(stock);

            // Execute DAO operation
            if (product.getProductId() == 0) {
                dao.addProduct(product); // ASSUMES addProduct exists
                JOptionPane.showMessageDialog(this, "Product added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                dao.updateProduct(product); // ASSUMES updateProduct exists
                JOptionPane.showMessageDialog(this, "Product updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose(); // Close the dialog
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving product data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}