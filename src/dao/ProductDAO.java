package dao;

import db.DatabaseConnection;
import models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void addProduct(Product product) throws Exception {
        String sql = "INSERT INTO products (sku, name, category, price, cost, stock_qty, reorder_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getCost());
            stmt.setInt(6, product.getStockQty());
            stmt.setInt(7, product.getReorderLevel());
            stmt.executeUpdate();
        }
    }

    public List<Product> getAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setSku(rs.getString("sku"));
                p.setName(rs.getString("name"));
                p.setCategory(rs.getString("category"));
                p.setPrice(rs.getDouble("price"));
                p.setCost(rs.getDouble("cost"));
                p.setStockQty(rs.getInt("stock_qty"));
                p.setReorderLevel(rs.getInt("reorder_level"));
                products.add(p);
            }
        }
        return products;
    }
}
