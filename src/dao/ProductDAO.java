package dao;

import db.DatabaseConnection;
import models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Add a new product (No change needed here)
    // NOTE: Ensure your addProduct also sets 'is_active = TRUE'
    // or rely on the column's default value in the DB.
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

    // Get all products (MODIFIED: Only retrieves active products)
    public List<Product> getAllProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        // ONLY select active products
        String sql = "SELECT * FROM products WHERE is_active = TRUE";
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
                // ASSUMING you added the 'is_active' field to your Product model
                p.setActive(rs.getBoolean("is_active"));
                products.add(p);
            }
        }
        return products;
    }

    // Get a product by ID (MODIFIED: Only retrieves active products)
    public Product getProductById(int productId) throws Exception {
        // Only select active products
        String sql = "SELECT * FROM products WHERE product_id = ? AND is_active = TRUE";
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
                    p.setActive(rs.getBoolean("is_active"));
                    return p;
                }
            }
        }
        return null;
    }

    // Update an existing product (No change needed here)
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

    // REPLACED physical delete with a soft delete/deactivate (NEW METHOD)
    /**
     * Logically deletes/deactivates a product, setting is_active to FALSE.
     * This avoids foreign key constraint errors and preserves historical data.
     */
    public void deactivateProduct(int productId) throws Exception {
        String sql = "UPDATE products SET is_active = FALSE WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    // NOTE: The old public void deleteProduct(int productId) method is now removed.

    // Get product by SKU (MODIFIED: Only retrieves active products)
    public Product getProductBySku(String sku) throws Exception {
        // Only select active products
        String sql = "SELECT * FROM products WHERE sku = ? AND is_active = TRUE";
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
                    p.setActive(rs.getBoolean("is_active"));
                    return p;
                }
            }
        }
        return null;
    }

    // Get products by category (MODIFIED: Only retrieves active products)
    public List<Product> getProductsByCategory(String category) throws Exception {
        List<Product> products = new ArrayList<>();
        // Only select active products
        String sql = "SELECT * FROM products WHERE category = ? AND is_active = TRUE";
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
                    p.setActive(rs.getBoolean("is_active"));
                    products.add(p);
                }
            }
        }
        return products;
    }

    // Get products with low stock (MODIFIED: Only retrieves active products)
    public List<Product> getLowStockProducts() throws Exception {
        List<Product> products = new ArrayList<>();
        // Only select active products
        String sql = "SELECT * FROM products WHERE stock_qty <= reorder_level AND is_active = TRUE";
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
                p.setActive(rs.getBoolean("is_active"));
                products.add(p);
            }
        }
        return products;
    }

    // Update stock quantity (for inventory adjustments) (No change needed here)
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