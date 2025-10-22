package ui;

import dao.ProductDAO;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.List;

public class ProductManagementWindow extends JFrame {
    private JTable productTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private DefaultTableModel tableModel;

    public ProductManagementWindow() {
        setTitle("Manage Products");
        setSize(700, 400);
        setLocationRelativeTo(null);

        String[] columns = {"ID", "SKU", "Name", "Category", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(productTable);

        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(scrollPane, "Center");
        add(buttonPanel, "South");

        refreshProducts();

        refreshButton.addActionListener(e -> refreshProducts());

        deleteButton.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row >= 0) {
                int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                ProductDAO dao = new ProductDAO();
                try {
                    dao.deleteProduct(id);
                    refreshProducts();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting product.");
                }
            }
        });
    }

    private void refreshProducts() {
        tableModel.setRowCount(0);
        ProductDAO dao = new ProductDAO();
        try {
            List<Product> products = dao.getAllProducts();
            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getProductId(), p.getSku(), p.getName(),
                        p.getCategory(), p.getPrice(), p.getStockQty()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }
}
