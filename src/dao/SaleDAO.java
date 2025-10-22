package dao;

import db.DatabaseConnection;
import models.Sale;
import models.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    /**
     * Add a new sale with its items (transaction)
     */
    public void addSale(Sale sale) throws Exception {
        Connection conn = null;
        PreparedStatement saleStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert sale record
            String saleSql = "INSERT INTO sales (account_id, sale_datetime, total_amount, payment_method, remarks) VALUES (?, NOW(), ?, ?, ?)";
            saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, sale.getAccountId());
            saleStmt.setDouble(2, sale.getTotalAmount());
            saleStmt.setString(3, sale.getPaymentMethod());
            saleStmt.setString(4, sale.getRemarks());
            saleStmt.executeUpdate();

            // Get generated sale_id
            ResultSet rs = saleStmt.getGeneratedKeys();
            int saleId = 0;
            if (rs.next()) {
                saleId = rs.getInt(1);
            }

            // Insert sale items and update stock
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, qty, price) VALUES (?, ?, ?, ?)";
            itemStmt = conn.prepareStatement(itemSql);

            String stockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ?";
            stockStmt = conn.prepareStatement(stockSql);

            for (SaleItem item : sale.getItems()) {
                // Insert sale item
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQty());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.executeUpdate();

                // Update product stock
                stockStmt.setInt(1, item.getQty());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            sale.setSaleId(saleId);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (saleStmt != null) saleStmt.close();
            if (itemStmt != null) itemStmt.close();
            if (stockStmt != null) stockStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Get all sales
     */
    public List<Sale> getAllSales() throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_datetime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sale sale = new Sale();
                sale.setSaleId(rs.getInt("sale_id"));
                sale.setAccountId(rs.getInt("account_id"));
                sale.setSaleDatetime(rs.getTimestamp("sale_datetime"));
                sale.setTotalAmount(rs.getDouble("total_amount"));
                sale.setPaymentMethod(rs.getString("payment_method"));
                sale.setRemarks(rs.getString("remarks"));
                sales.add(sale);
            }
        }
        return sales;
    }

    /**
     * Get sale by ID with its items
     */
    public Sale getSaleById(int saleId) throws Exception {
        String sql = "SELECT * FROM sales WHERE sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setAccountId(rs.getInt("account_id"));
                    sale.setSaleDatetime(rs.getTimestamp("sale_datetime"));
                    sale.setTotalAmount(rs.getDouble("total_amount"));
                    sale.setPaymentMethod(rs.getString("payment_method"));
                    sale.setRemarks(rs.getString("remarks"));

                    // Load sale items
                    sale.setItems(getSaleItems(saleId));

                    return sale;
                }
            }
        }
        return null;
    }

    /**
     * Get items for a specific sale
     */
    public List<SaleItem> getSaleItems(int saleId) throws Exception {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQty(rs.getInt("qty"));
                    item.setPrice(rs.getDouble("price"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Get sales by account/user
     */
    public List<Sale> getSalesByAccount(int accountId) throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE account_id = ? ORDER BY sale_datetime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setAccountId(rs.getInt("account_id"));
                    sale.setSaleDatetime(rs.getTimestamp("sale_datetime"));
                    sale.setTotalAmount(rs.getDouble("total_amount"));
                    sale.setPaymentMethod(rs.getString("payment_method"));
                    sale.setRemarks(rs.getString("remarks"));
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    /**
     * Get sales by date range
     */
    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE sale_datetime BETWEEN ? AND ? ORDER BY sale_datetime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setSaleId(rs.getInt("sale_id"));
                    sale.setAccountId(rs.getInt("account_id"));
                    sale.setSaleDatetime(rs.getTimestamp("sale_datetime"));
                    sale.setTotalAmount(rs.getDouble("total_amount"));
                    sale.setPaymentMethod(rs.getString("payment_method"));
                    sale.setRemarks(rs.getString("remarks"));
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    /**
     * Get total sales amount for a date range
     */
    public double getTotalSalesAmount(Date startDate, Date endDate) throws Exception {
        String sql = "SELECT SUM(total_amount) as total FROM sales WHERE sale_datetime BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, startDate);
            stmt.setDate(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Delete a sale (use with caution - should restore stock)
     */
    public void deleteSale(int saleId) throws Exception {
        Connection conn = null;
        PreparedStatement itemStmt = null;
        PreparedStatement saleStmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Delete sale items first (foreign key constraint)
            String itemSql = "DELETE FROM sale_items WHERE sale_id = ?";
            itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, saleId);
            itemStmt.executeUpdate();

            // Delete sale
            String saleSql = "DELETE FROM sales WHERE sale_id = ?";
            saleStmt = conn.prepareStatement(saleSql);
            saleStmt.setInt(1, saleId);
            saleStmt.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (itemStmt != null) itemStmt.close();
            if (saleStmt != null) saleStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}