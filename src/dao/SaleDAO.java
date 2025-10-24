package dao;

import db.DatabaseConnection;
import models.Sale;
import models.SaleItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    /**
     * Helper to map a ResultSet to a Sale object.
     */
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setAccountId(rs.getInt("account_id"));

        // Map to LocalDateTime for modern Java handling
        Timestamp timestamp = rs.getTimestamp("sale_datetime");
        if (timestamp != null) {
            sale.setSaleDatetime(Timestamp.valueOf(timestamp.toLocalDateTime()));
        }

        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setRemarks(rs.getString("remarks"));
        return sale;
    }

    /**
     * Add a new sale with its items (transaction)
     * Includes stock update and sets the generated sale ID.
     */
    public void addSale(Sale sale) throws Exception {
        Connection conn = null;
        PreparedStatement saleStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction: CRITICAL

            // 1. Insert sale record
            // Use current time from Java or DB, using NOW() is fine if DB server time is reliable.
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
            rs.close(); // Close ResultSet immediately

            // 2. Insert sale items and update stock (using Batching for efficiency)
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, qty, price) VALUES (?, ?, ?, ?)";
            itemStmt = conn.prepareStatement(itemSql);

            String stockSql = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ?";
            stockStmt = conn.prepareStatement(stockSql);

            for (SaleItem item : sale.getItems()) {
                // Insert sale item (Batch 1)
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQty());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.addBatch();

                // Update product stock (Batch 2)
                stockStmt.setInt(1, item.getQty());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch(); // Execute all item inserts
            stockStmt.executeBatch(); // Execute all stock updates

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
            // Use try-with-resources if possible, but manual close block is okay
            // if you need to set conn.setAutoCommit(true) outside the try-with-resources.
            if (saleStmt != null) saleStmt.close();
            if (itemStmt != null) itemStmt.close();
            if (stockStmt != null) stockStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
// ----------------------------------------------------------------------------------

    /**
     * Get all sales.
     */
    public List<Sale> getAllSales() throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_datetime DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        }
        return sales;
    }

    /**
     * Get sale by ID with its items.
     */
    public Sale getSaleById(int saleId) throws Exception {
        String sql = "SELECT * FROM sales WHERE sale_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, saleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    // Load sale items
                    sale.setItems(getSaleItems(saleId));
                    return sale;
                }
            }
        }
        return null;
    }

    /**
     * Get items for a specific sale. (No change needed)
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
     * Get sales by date range (Fixes the type error by accepting Strings).
     */
    public List<Sale> getSalesByDateRange(String startDateStr, String endDateStr) throws Exception {
        List<Sale> sales = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM sales WHERE 1=1");
        List<Timestamp> params = new ArrayList<>();

        // Use full day range (YYYY-MM-DD 00:00:00 to YYYY-MM-DD 23:59:59)
        if (startDateStr != null && !startDateStr.isEmpty()) {
            sqlBuilder.append(" AND sale_datetime >= ?");
            params.add(Timestamp.valueOf(startDateStr + " 00:00:00"));
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            sqlBuilder.append(" AND sale_datetime <= ?");
            params.add(Timestamp.valueOf(endDateStr + " 23:59:59"));
        }

        sqlBuilder.append(" ORDER BY sale_datetime DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setTimestamp(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
            }
        }
        return sales;
    }

    /**
     * Get total sales amount for a date range (Fixes type error).
     */
    public double getTotalSalesAmount(String startDateStr, String endDateStr) throws Exception {
        StringBuilder sqlBuilder = new StringBuilder("SELECT SUM(total_amount) as total FROM sales WHERE 1=1");
        List<Timestamp> params = new ArrayList<>();

        if (startDateStr != null && !startDateStr.isEmpty()) {
            sqlBuilder.append(" AND sale_datetime >= ?");
            params.add(Timestamp.valueOf(startDateStr + " 00:00:00"));
        }
        if (endDateStr != null && !endDateStr.isEmpty()) {
            sqlBuilder.append(" AND sale_datetime <= ?");
            params.add(Timestamp.valueOf(endDateStr + " 23:59:59"));
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setTimestamp(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }

    /**
     * Delete a sale and restore stock (CRITICAL FIX: Stock must be restored).
     */
    public void deleteSale(int saleId) throws Exception {
        Connection conn = null;
        PreparedStatement restoreStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement saleStmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Get items before deleting them (CRITICAL for stock restore)
            List<SaleItem> items = getSaleItems(saleId);
            if (items.isEmpty()) {
                // If no items, proceed with simple delete
                throw new Exception("Sale has no items or was already deleted.");
            }

            // 2. Restore product stock
            String restoreSql = "UPDATE products SET stock_qty = stock_qty + ? WHERE product_id = ?";
            restoreStmt = conn.prepareStatement(restoreSql);

            for (SaleItem item : items) {
                restoreStmt.setInt(1, item.getQty()); // Add quantity back
                restoreStmt.setInt(2, item.getProductId());
                restoreStmt.addBatch();
            }
            restoreStmt.executeBatch();

            // 3. Delete sale items
            String itemSql = "DELETE FROM sale_items WHERE sale_id = ?";
            itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, saleId);
            itemStmt.executeUpdate();

            // 4. Delete sale
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
            if (restoreStmt != null) restoreStmt.close();
            if (itemStmt != null) itemStmt.close();
            if (saleStmt != null) saleStmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}