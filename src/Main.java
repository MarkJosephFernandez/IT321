import dao.ProductDAO;
import models.Product;

public class Main {
    public static void main(String[] args) {
        try {
            ProductDAO dao = new ProductDAO();

            // Add product
            Product newProd = new Product();
            newProd.setSku("SKU123");
            newProd.setName("Coke 1L");
            newProd.setCategory("Beverages");
            newProd.setPrice(45.00);
            newProd.setCost(30.00);
            newProd.setStockQty(50);
            newProd.setReorderLevel(10);

            dao.addProduct(newProd);
            System.out.println("Product added!");

            // Get products
            for (Product p : dao.getAllProducts()) {
                System.out.println(p.getProductId() + " - " + p.getName() + " (" + p.getStockQty() + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
