package org.yearup.models;

import java.math.BigDecimal;

public class OrderLineItem {
    // Note: order_line_items table often uses a composite primary key (order_id, product_id)
    private int orderId;    // Foreign key to the Orders table
    private Product product; // Full product details, not stored in DB, but needed for the model
    private int productId;  // Stored in the database
    private int quantity;
    private BigDecimal salesPrice; // Price at the time of purchase
    private BigDecimal discount;   // Discount at the time of purchase (can be 0)
    private BigDecimal lineTotal;  // Calculated total for this line item

    public OrderLineItem() {
        this.discount = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }

    // Constructor typically used when creating a new line item from a shopping cart item
    public OrderLineItem(int orderId, Product product, int quantity, BigDecimal salesPrice, BigDecimal discount) {
        this(); // Call default constructor
        this.orderId = orderId;
        this.product = product;
        this.productId = product.getProductId(); // Get productId from the Product object
        this.quantity = quantity;
        this.salesPrice = salesPrice;
        this.discount = discount;
        calculateLineTotal();
    }

    // Constructor for mapping from database (where product details are joined)
    public OrderLineItem(int orderId, int productId, int quantity, BigDecimal salesPrice, BigDecimal discount) {
        this(); // Call default constructor
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.salesPrice = salesPrice;
        this.discount = discount;
        calculateLineTotal();
    }


    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.productId = product.getProductId(); // Keep productId in sync
            // Re-calculate line total if product details (like price) change
            if (this.salesPrice == null) { // Only set salesPrice if not already specifically set
                this.salesPrice = product.getPrice();
            }
            calculateLineTotal();
        }
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateLineTotal(); // Recalculate if quantity changes
    }

    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
        calculateLineTotal(); // Recalculate if sales price changes
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateLineTotal(); // Recalculate if discount changes
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    // Private setter for lineTotal, as it should be calculated
    private void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    /**
     * Calculates and updates the lineTotal based on sales price, quantity, and discount.
     */
    public void calculateLineTotal() {
        if (this.salesPrice != null) {
            BigDecimal quantityDecimal = BigDecimal.valueOf(this.quantity);
            BigDecimal subtotal = this.salesPrice.multiply(quantityDecimal);
            BigDecimal discountAmount = subtotal.multiply(this.discount);

            this.lineTotal = subtotal.subtract(discountAmount);
        } else {
            this.lineTotal = BigDecimal.ZERO;
        }
    }
}
