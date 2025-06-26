// src/main/java/org/yearup/controllers/OrdersController.java
package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ProductDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*; // Import all necessary models (Order, OrderLineItem, ShoppingCart, User, Profile)

import java.security.Principal;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@RestController
@RequestMapping("/orders") // This controller will handle requests to http://localhost:8080/orders
@CrossOrigin // Allows cross-origin requests, useful for front-end development
@PreAuthorize("isAuthenticated()") // Only logged-in users are allowed to access these endpoints
public class OrdersController {

    private final UserDao userDao;
    private final ProfileDao profileDao;
    private final ShoppingCartDao shoppingCartDao;
    private final OrderDao orderDao;
    private final OrderLineItemDao orderLineItemDao;
    private final ProductDao productDao; // Needed to retrieve full Product details for OrderLineItem

    @Autowired // Spring will automatically inject these dependencies
    public OrdersController(UserDao userDao, ProfileDao profileDao, ShoppingCartDao shoppingCartDao,
                            OrderDao orderDao, OrderLineItemDao orderLineItemDao, ProductDao productDao) {
        this.userDao = userDao;
        this.profileDao = profileDao;
        this.shoppingCartDao = shoppingCartDao;
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.productDao = productDao;
    }

    // Helper method to get the ID of the currently logged-in user
    private int getLoggedInUserId(Principal principal) {
        String username = principal.getName(); // Get the username from the Principal object provided by Spring Security
        User user = userDao.getByUserName(username); // Retrieve the User object from the database
        if (user == null) {
            // This case should ideally not happen if isAuthenticated() works correctly,
            // but it's a good defensive check.
            System.err.println("ERROR: User not found in database for principal: " + username);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged-in user not found in the database.");
        }
        return user.getId();
    }

    /**
     * Handles the checkout process for the current user.
     * This method retrieves the user's shopping cart, creates a new Order record,
     * creates an OrderLineItem for each item in the cart, and then clears the cart.
     *
     * VERB: POST
     * URL: http://localhost:8080/orders
     * BODY: NO body required
     *
     * @param principal The Principal object representing the currently logged-in user.
     * @return The newly created Order object, including its populated line items.
     */
    @PostMapping // Maps POST requests to /orders
    @ResponseStatus(HttpStatus.CREATED) // Indicates that a new resource has been created
    public Order checkout(Principal principal) {
        try {
            int userId = getLoggedInUserId(principal); // Get the ID of the logged-in user
            System.out.println("DEBUG: Initiating checkout process for user ID: " + userId);

            // 1. Retrieve the user's current shopping cart
            ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);
            if (shoppingCart.getItems().isEmpty()) {
                System.err.println("ERROR: Checkout attempted with an empty shopping cart for user ID: " + userId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot checkout with an empty shopping cart.");
            }

            // 2. Retrieve the user's profile information to get address details for the order
            Profile userProfile = profileDao.getByUserId(userId);
            if (userProfile == null) {
                System.err.println("ERROR: User profile not found for user ID: " + userId + ". Necessary for order address.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile not found. Cannot complete order without address details.");
            }

            // 3. Create a new Order object and populate its details
            Order newOrder = new Order();
            newOrder.setUserId(userId);
            newOrder.setDate(LocalDateTime.now()); // Set the current date and time for the order
            newOrder.setAddress(userProfile.getAddress()); // Get address from profile
            newOrder.setCity(userProfile.getCity());
            newOrder.setState(userProfile.getState());
            newOrder.setZip(userProfile.getZip());
            newOrder.setShippingAmount(BigDecimal.ZERO); // Placeholder: Could be calculated based on shipping zones etc.
            newOrder.setTotal(shoppingCart.getTotal()); // Set the total from the shopping cart

            // 4. Insert the new Order into the 'orders' table in the database
            Order createdOrder = orderDao.createOrder(newOrder);
            if (createdOrder == null || createdOrder.getOrderId() == 0) {
                System.err.println("ERROR: Database failed to create order for user ID: " + userId);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create order in the database.");
            }

            // 5. Iterate through each item in the shopping cart to create OrderLineItems
            for (ShoppingCartItem cartItem : shoppingCart.getItems().values()) {
                Product product = cartItem.getProduct(); // Get the full Product object from the cart item

                // Create a new OrderLineItem
                OrderLineItem orderLineItem = new OrderLineItem(
                        createdOrder.getOrderId(),   // Link to the newly created order
                        product,                     // Full product object
                        cartItem.getQuantity(),      // Quantity from the cart item
                        product.getPrice(),          // Sales price is the product's current price
                        cartItem.getDiscountPercent() // Discount from the cart item
                );

                // Insert the OrderLineItem into the 'order_line_items' table
                orderLineItemDao.createOrderLineItem(orderLineItem);
                // Add the line item to the createdOrder object's list (for the API response)
                createdOrder.addLineItem(orderLineItem);
            }

            // 6. After successful order creation, clear the user's shopping cart
            shoppingCartDao.clearShoppingCart(userId);
            System.out.println("DEBUG: Shopping cart cleared for user ID: " + userId + " after successful checkout.");

            System.out.println("DEBUG: Checkout process completed successfully for order ID: " + createdOrder.getOrderId());
            return createdOrder; // Return the complete Order object, including its line items
        } catch (ResponseStatusException ex) {
            // Re-throw specific HTTP status exceptions directly
            System.err.println("ERROR: Checkout failed with HTTP Status: " + ex.getStatus() + " - " + ex.getReason());
            throw ex;
        } catch (Exception e) {
            // Catch any other unexpected exceptions and return a 500 Internal Server Error
            System.err.println("ERROR: An unexpected error occurred during checkout for user: " + principal.getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during checkout.", e);
        }
    }
}

