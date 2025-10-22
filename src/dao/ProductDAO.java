package dao;

import db.DatabaseConnection;
import models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Add a new product
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

    // Get all products
    public List<Product> getAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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

    // Get a product by ID
    public Product getProductById(int productId) throws Exception {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setCategory(rs.getString("category"));
                    p.setPrice(rs.getDouble("price"));
                    p.setCost(rs.getDouble("cost"));
                    p.setStockQty(rs.getInt("stock_qty"));
                    p.setReorderLevel(rs.getInt("reorder_level"));
                    return p;
                }
            }
        }
        return null;
    }

    // Update an existing product
    public void updateProduct(Product product) throws Exception {
        String sql = "UPDATE products SET sku = ?, name = ?, category = ?, price = ?, cost = ?, stock_qty = ?, reorder_level = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getSku());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getCost());
            stmt.setInt(6, product.getStockQty());
            stmt.setInt(7, product.getReorderLevel());
            stmt.setInt(8, product.getProductId());
            stmt.executeUpdate();
        }
    }

    // Delete a product by ID
    public void deleteProduct(int productId) throws Exception {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    // Get product by SKU (useful for lookups)
    public Product getProductBySku(String sku) throws Exception {
        String sql = "SELECT * FROM products WHERE sku = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product();
                    p.setProductId(rs.getInt("product_id"));
                    p.setSku(rs.getString("sku"));
                    p.setName(rs.getString("name"));
                    p.setCategory(rs.getString("category"));
                    p.setPrice(rs.getDouble("price"));
                    p.setCost(rs.getDouble("cost"));
                    p.setStockQty(rs.getInt("stock_qty"));
                    p.setReorderLevel(rs.getInt("reorder_level"));
                    return p;
                }
            }
        }
        return null;
    }

    // Get products by category
    public List<Product> getProductsByCategory(String category) throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return products;
    }

    // Get products with low stock (stock_qty <= reorder_level)
    public List<Product> getLowStockProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_qty <= reorder_level";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
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

    // Update stock quantity (for inventory adjustments)
    public void updateStock(int productId, int newQuantity) throws Exception {
        String sql = "UPDATE products SET stock_qty = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }
}