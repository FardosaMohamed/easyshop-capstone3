package org.yearup.data;

// Assuming you have a ShoppingCartItem model
import org.yearup.models.ShoppingCartItem; // You might need to adjust this import based on your model name
import org.yearup.models.ShoppingCart; // You likely have a ShoppingCart model too

import java.util.List;

public interface ShoppingCartDao
{
    // You'll likely add more methods here later for full shopping cart functionality
    // e.g., ShoppingCart getByUserId(int userId);
    // e.g., void addProductToCart(int userId, int productId, int quantity);
    // e.g., void updateProductQuantity(int userId, int productId, int quantity);
    // e.g., void clearCart(int userId);

    // ✅ NEW METHOD: Deletes shopping cart items whose products belong to a specific category
    void deleteShoppingCartItemsByCategoryId(int categoryId);
}