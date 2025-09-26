package models;

import java.sql.Timestamp;
import java.util.List;

public class Sale {
    private int saleId;
    private int accountId;
    private Timestamp saleDatetime;
    private double totalAmount;
    private String paymentMethod;
    private String remarks;
    private List<SaleItem> items; // relationship

    // Getters and Setters
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public Timestamp getSaleDatetime() { return saleDatetime; }
    public void setSaleDatetime(Timestamp saleDatetime) { this.saleDatetime = saleDatetime; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}
