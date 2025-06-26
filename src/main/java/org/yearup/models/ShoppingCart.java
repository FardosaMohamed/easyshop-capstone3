package org.yearup.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart
{
    private Map<Integer, ShoppingCartItem> items; // Map of productId to ShoppingCartItem
    private BigDecimal total; // Added to match JSON output format

    public ShoppingCart()
    {
        this.items = new HashMap<>();
        this.total = BigDecimal.ZERO; // Initialize total
    }

    public Map<Integer, ShoppingCartItem> getItems()
    {
        return items;
    }

    public void setItems(Map<Integer, ShoppingCartItem> items)
    {
        this.items = items;
        calculateTotal(); // Recalculate total if the items map is directly replaced
    }

    // This getTotal will now return the stored calculated total
    public BigDecimal getTotal()
    {
        return total;
    }

    // Setter for total (mostly for internal use by calculateTotal)
    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean contains(int productId)
    {
        return items.containsKey(productId);
    }

    /**
     * Adds a new item or updates an existing item in the cart.
     * The key for the map is the product's ID.
     * @param item The ShoppingCartItem to add or update.
     */
    public void addOrUpdateItem(ShoppingCartItem item) // Renamed from 'add' for clarity with upsert behavior
    {
        if (item != null && item.getProduct() != null) {
            items.put(item.getProduct().getProductId(), item); // Use product's ID as the key
            calculateTotal(); // Recalculate total after item change
        }
    }

    /**
     * Removes an item from the cart by product ID.
     * @param productId The ID of the product to remove.
     */
    public void removeItem(int productId) {
        items.remove(productId);
        calculateTotal(); // Recalculate total after item removal
    }

    public ShoppingCartItem get(int productId)
    {
        return items.get(productId);
    }

    /**
     * Calculates the total price of all items in the shopping cart.
     * This method updates the 'total' field.
     */
    public void calculateTotal()
    {
        BigDecimal cartTotal = BigDecimal.ZERO;
        for (ShoppingCartItem item : items.values()) {
            if (item.getLineTotal() != null) {
                cartTotal = cartTotal.add(item.getLineTotal());
            }
        }
        this.total = cartTotal;
    }

    /**
     * Clears all items from the shopping cart and resets the total to zero.
     */
    public void clear() {
        this.items.clear();
        this.total = BigDecimal.ZERO;
    }
}
