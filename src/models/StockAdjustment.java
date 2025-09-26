package models;

import java.sql.Timestamp;

public class StockAdjustment {
    private int adjustId;
    private int productId;
    private int qtyChange;
    private String reason;
    private int createdBy;
    private Timestamp createdAt;

    // Getters and Setters
    public int getAdjustId() { return adjustId; }
    public void setAdjustId(int adjustId) { this.adjustId = adjustId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQtyChange() { return qtyChange; }
    public void setQtyChange(int qtyChange) { this.qtyChange = qtyChange; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
