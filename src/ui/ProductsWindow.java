package ui;

import dao.ProductDAO;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ProductsWindow extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public ProductsWindow() {
        setTitle("Manage Products");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Table ===
        String[] cols = {"ID", "SKU", "Name", "Category", "Price", "Cost", "Stock", "Reorder Level"};
        tableModel = new DefaultTableModel(cols, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // === Buttons ===
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // === Add components ===
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // === Actions ===
        refreshButton.addActionListener(this::refreshData);
        addButton.addActionListener(this::addProduct);
        editButton.addActionListener(this::editProduct);
        deleteButton.addActionListener(this::deleteProduct);

        refreshData(null);
    }

    private void refreshData(ActionEvent e) {
        try {
            tableModel.setRowCount(0);
            ProductDAO dao = new ProductDAO();
            List<Product> products = dao.getAllProducts();

            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getSku(),
                        p.getName(),
                        p.getCategory(),
                        p.getPrice(),
                        p.getCost(),
                        p.getStockQty(),
                        p.getReorderLevel()
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }

    private void addProduct(ActionEvent e) {
        String name = JOptionPane.showInputDialog("Enter product name:");
        if (name != null && !name.isEmpty()) {
            try {
                ProductDAO dao = new ProductDAO();
                Product product = new Product();
                product.setName(name);
                product.setSku("SKU-" + System.currentTimeMillis()); // auto SKU
                product.setPrice(0.0);
                product.setCost(0.0);
                product.setStockQty(0);
                dao.addProduct(product);
                refreshData(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding product.");
            }
        }
    }

    private void editProduct(ActionEvent e) {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int productId = (int) tableModel.getValueAt(row, 0);
            String newName = JOptionPane.showInputDialog("Enter new product name:", tableModel.getValueAt(row, 2));
            if (newName != null) {
                try {
                    ProductDAO dao = new ProductDAO();
                    Product product = dao.getProductById(productId);
                    product.setName(newName);
                    dao.updateProduct(product);
                    refreshData(null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating product.");
                }
            }
        }
    }

    private void deleteProduct(ActionEvent e) {
        int row = productTable.getSelectedRow();
        if (row >= 0) {
            int productId = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete product?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ProductDAO dao = new ProductDAO();
                    dao.deleteProduct(productId);
                    refreshData(null);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting product.");
                }
            }
        }
    }
}
