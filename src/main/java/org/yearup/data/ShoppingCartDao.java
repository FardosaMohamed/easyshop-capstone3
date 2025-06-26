package org.yearup.data;

import org.yearup.models.ShoppingCartItem;
import org.yearup.models.ShoppingCart;

import java.util.List; // Keep if you use List in any of your models/DAOs, otherwise can remove

public interface ShoppingCartDao
{
    // New method added to support cascading delete from products
    void deleteShoppingCartItemsByCategoryId(int categoryId);

    // Methods needed to support ShoppingCartController functionality

    /**
     * Retrieves the entire shopping cart for a given user ID.
     * @param userId The ID of the user.
     * @return The ShoppingCart object containing all items for the user.
     */
    ShoppingCart getByUserId(int userId);

    /**
     * Retrieves a specific shopping cart item for a user and product.
     * Used to check if an item already exists in the cart.
     * @param userId The ID of the user.
     * @param productId The ID of the product.
     * @return The ShoppingCartItem if found, null otherwise.
     */
    ShoppingCartItem getCartItemByUserIdAndProductId(int userId, int productId);

    /**
     * Adds a product to the user's shopping cart. If the product already exists,
     * its quantity is incremented by 1. If not, it's added with quantity 1.
     * @param userId The ID of the user.
     * @param productId The ID of the product to add/increment.
     */
    void addProductToCart(int userId, int productId);

    /**
     * Updates the quantity of a specific product in the user's shopping cart.
     * If the quantity is 0 or less, the item should be removed from the cart.
     * @param userId The ID of the user.
     * @param productId The ID of the product to update.
     * @param quantity The new quantity for the product.
     */
    void updateProductQuantity(int userId, int productId, int quantity);

    /**
     * Clears all items from a user's shopping cart.
     * @param userId The ID of the user whose cart should be cleared.
     */
    void clearShoppingCart(int userId);

    /**
     * Deletes a specific product from a user's shopping cart.
     * @param userId The ID of the user.
     * @param productId The ID of the product to remove from the cart.
     */
    void deleteProductFromCart(int userId, int productId);
}
