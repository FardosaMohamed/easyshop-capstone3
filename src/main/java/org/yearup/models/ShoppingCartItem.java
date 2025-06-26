package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

public class ShoppingCartItem
{
    private Product product;           // Stores the associated Product object
    private int quantity;              // Stores the quantity of this product in the cart
    private BigDecimal discountPercent; // Stores any discount percentage for this item
    private BigDecimal lineTotal;       // Stores the calculated total for this specific line item

    // Default constructor: Initializes an empty shopping cart item
    public ShoppingCartItem()
    {
        this.quantity = 0;             // Default quantity to 0 for a newly created empty item
        this.discountPercent = BigDecimal.ZERO; // Default discount to 0
        this.lineTotal = BigDecimal.ZERO;      // Default line total to 0
    }

    // Parameterized constructor: For creating an item with a product and initial quantity
    public ShoppingCartItem(Product product, int quantity) {
        this(); // Calls the default constructor to set initial values
        this.product = product;
        this.quantity = quantity;
        calculateLineTotal(); // Calculate lineTotal immediately after setting product and quantity
    }

    // Getters and Setters for Product
    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
        calculateLineTotal(); // Recalculate lineTotal whenever the product changes
    }

    // Getters and Setters for Quantity
    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
        calculateLineTotal(); // Recalculate lineTotal whenever the quantity changes
    }

    // Getters and Setters for Discount Percent
    public BigDecimal getDiscountPercent()
    {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent)
    {
        this.discountPercent = discountPercent;
        calculateLineTotal(); // Recalculate lineTotal whenever the discount changes
    }

    // Getter for Line Total (returns the stored calculated value)
    public BigDecimal getLineTotal()
    {
        return lineTotal;
    }

    // Private setter for lineTotal - it should be calculated, not set directly from outside
    private void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    // Provides the Product ID, ignoring it during JSON serialization
    @JsonIgnore
    public int getProductId()
    {
        // Added null check to prevent NullPointerException if product is not set
        if (this.product != null) {
            return this.product.getProductId();
        }
        return 0; // Return 0 or handle as appropriate if product is null
    }

    /**
     * Calculates the line total for this shopping cart item based on
     * product price, quantity, and discount. Updates the 'lineTotal' field.
     * This method should be called whenever product, quantity, or discountPercent changes.
     */
    public void calculateLineTotal()
    {
        // Ensure product and its price are available before calculating
        if (this.product != null && this.product.getPrice() != null) {
            BigDecimal basePrice = this.product.getPrice();
            BigDecimal currentQuantity = BigDecimal.valueOf(this.quantity); // Convert int quantity to BigDecimal

            BigDecimal subTotal = basePrice.multiply(currentQuantity);
            BigDecimal discountAmount = subTotal.multiply(this.discountPercent); // Use this.discountPercent

            this.lineTotal = subTotal.subtract(discountAmount);
        } else {
            this.lineTotal = BigDecimal.ZERO; // Set to zero if product or price is not available
        }
    }
}
