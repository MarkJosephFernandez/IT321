package dao;

import db.DatabaseConnection;
import models.Sale;
import models.SaleItem;

import java.sql.*;
import java.util.List;

public class SaleDAO {

    public int createSale(Sale sale) throws Exception {
        String saleSql = "INSERT INTO sales (account_id, total_amount, payment_method, remarks) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO sale_items (sale_id, product_id, qty, price) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Insert sale
            PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            saleStmt.setInt(1, sale.getAccountId());
            saleStmt.setDouble(2, sale.getTotalAmount());
            saleStmt.setString(3, sale.getPaymentMethod());
            saleStmt.setString(4, sale.getRemarks());
            saleStmt.executeUpdate();

            ResultSet keys = saleStmt.getGeneratedKeys();
            int saleId = 0;
            if (keys.next()) {
                saleId = keys.getInt(1);
            }

            // Insert sale items & update stock
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            PreparedStatement stockStmt = conn.prepareStatement(updateStock);

            for (SaleItem item : sale.getItems()) {
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setInt(3, item.getQty());
                itemStmt.setDouble(4, item.getPrice());
                itemStmt.addBatch();

                stockStmt.setInt(1, item.getQty());
                stockStmt.setInt(2, item.getProductId());
                stockStmt.addBatch();
            }

            itemStmt.executeBatch();
            stockStmt.executeBatch();

            conn.commit();
            return saleId;
        }
    }
}
