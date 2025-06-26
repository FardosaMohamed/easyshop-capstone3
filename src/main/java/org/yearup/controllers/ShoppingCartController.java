package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product; // Import Product for validation
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal; // For getting logged-in user info
import java.util.Map; // For PUT request body

// Convert this class to a REST controller
@RestController
@RequestMapping("/cart") // Base URL for shopping cart operations
@CrossOrigin // Allow cross-site origin requests
// Only logged-in users should have access to these actions
@PreAuthorize("isAuthenticated()") // Requires the user to be authenticated
public class ShoppingCartController
{
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao; // Added to verify product existence

    // Create an Autowired constructor to inject the DAOs
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    /**
     * Retrieves the shopping cart for the currently logged-in user.
     * URL: GET http://localhost:8080/cart
     *
     * @param principal The security principal representing the logged-in user.
     * @return The ShoppingCart object for the current user, including all items.
     */
    @GetMapping // add the appropriate annotation for a get action
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            // Get the currently logged-in username
            String userName = principal.getName();
            System.out.println("DEBUG: getCart called for username: " + userName);

            // Find database user by userId
            User user = userDao.getByUserName(userName);
            if (user == null) {
                System.err.println("ERROR: User not found for username: " + userName);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for the logged-in principal.");
            }
            int userId = user.getId();
            System.out.println("DEBUG: Fetching cart for user ID: " + userId);

            // Use the shoppingCartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            System.err.println("ERROR: Error getting shopping cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad. Could not retrieve shopping cart.", e);
        }
    }

    /**
     * Adds a product to the current user's shopping cart.
     * If the product is already in the cart, its quantity is incremented by 1.
     * If the product is not in the cart, it's added with a quantity of 1.
     * URL: POST http://localhost:8080/cart/products/{productId}
     *
     * @param principal The security principal.
     * @param productId The ID of the product to add.
     * @return The updated ShoppingCart.
     */
    @PostMapping("/products/{productId}") // add a POST method to add a product to the cart
    @ResponseStatus(HttpStatus.CREATED) // Or HttpStatus.OK if you prefer for an upsert
    public ShoppingCart addProductToCart(Principal principal, @PathVariable int productId) {
        try {
            int userId = userDao.getByUserName(principal.getName()).getId();
            System.out.println("DEBUG: Attempting to add product " + productId + " to cart for user ID: " + userId);

            // Verify the product actually exists in the database
            Product productToAdd = productDao.getById(productId);
            if (productToAdd == null) {
                System.err.println("ERROR: Product ID " + productId + " not found.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found.");
            }

            shoppingCartDao.addProductToCart(userId, productId);
            System.out.println("DEBUG: Product " + productId + " added/updated for user " + userId + ".");

            // Return the updated cart
            return shoppingCartDao.getByUserId(userId);
        } catch (ResponseStatusException ex) {
            throw ex; // Re-throw specific HTTP errors
        } catch (Exception e) {
            System.err.println("ERROR: Error adding product to cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding product to cart.", e);
        }
    }

    /**
     * Updates the quantity of a specific product in the user's shopping cart.
     * The product must already exist in the cart. If quantity is 0 or less, the item is removed.
     * URL: PUT http://localhost:8080/cart/products/{productId}
     * Body: {"quantity": N}
     *
     * @param principal The security principal.
     * @param productId The ID of the product to update.
     * @param requestBody A map containing the "quantity" key.
     */
    @PutMapping("/products/{productId}") // add a PUT method to update an existing product in the cart
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content for successful update with no body
    public void updateProductQuantity(Principal principal, @PathVariable int productId, @RequestBody Map<String, Integer> requestBody) {
        try {
            int userId = userDao.getByUserName(principal.getName()).getId();
            System.out.println("DEBUG: Attempting to update product " + productId + " quantity for user ID: " + userId);

            Integer quantity = requestBody.get("quantity");
            if (quantity == null) {
                System.err.println("ERROR: Quantity not provided in request body for product " + productId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity is required in the request body.");
            }

            // Check if the product is in the user's cart before attempting to update
            if (shoppingCartDao.getCartItemByUserIdAndProductId(userId, productId) == null) {
                System.err.println("ERROR: Product " + productId + " not found in cart for user " + userId + ". Cannot update.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found in user's cart.");
            }

            shoppingCartDao.updateProductQuantity(userId, productId, quantity);
            System.out.println("DEBUG: Product " + productId + " quantity updated to " + quantity + " for user " + userId + ".");

        } catch (ResponseStatusException ex) {
            throw ex; // Re-throw specific HTTP errors
        } catch (Exception e) {
            System.err.println("ERROR: Error updating product quantity in cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product quantity in cart.", e);
        }
    }

    /**
     * Clears all items from the current user's shopping cart.
     * URL: DELETE http://localhost:8080/cart
     * NOW returns 200 OK with the empty ShoppingCart object in the body.
     *
     * @param principal The security principal.
     * @return The empty ShoppingCart object for the current user.
     */
    @DeleteMapping // add a DELETE method to clear all products from the current user's cart
    @ResponseStatus(HttpStatus.OK) // <-- CHANGED from HttpStatus.NO_CONTENT to HttpStatus.OK
    public ShoppingCart clearCart(Principal principal) { // <-- CHANGED return type from void to ShoppingCart
        try {
            int userId = userDao.getByUserName(principal.getName()).getId();
            System.out.println("DEBUG: Clearing shopping cart for user ID: " + userId);
            shoppingCartDao.clearShoppingCart(userId);
            System.out.println("DEBUG: Shopping cart cleared for user " + userId + ".");

            // After clearing, fetch the (now empty) cart and return it in the response body
            return shoppingCartDao.getByUserId(userId); // <-- NEW: Fetch and return the empty cart
        } catch (Exception e) {
            System.err.println("ERROR: Error clearing shopping cart: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error clearing shopping cart.", e);
        }
    }
}
