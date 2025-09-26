package dao;

import db.DatabaseConnection;
import models.StockAdjustment;

import java.sql.*;

public class StockAdjustmentDAO {

    public void addAdjustment(StockAdjustment adj) throws Exception {
        String adjSql = "INSERT INTO stock_adjustments (product_id, qty_change, reason, created_by) VALUES (?, ?, ?, ?)";
        String stockSql = "UPDATE products SET stock_qty = stock_qty + ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Insert adjustment
            PreparedStatement stmt1 = conn.prepareStatement(adjSql);
            stmt1.setInt(1, adj.getProductId());
            stmt1.setInt(2, adj.getQtyChange());
            stmt1.setString(3, adj.getReason());
            stmt1.setInt(4, adj.getCreatedBy());
            stmt1.executeUpdate();

            // Update stock
            PreparedStatement stmt2 = conn.prepareStatement(stockSql);
            stmt2.setInt(1, adj.getQtyChange());
            stmt2.setInt(2, adj.getProductId());
            stmt2.executeUpdate();

            conn.commit();
        }
    }
}
