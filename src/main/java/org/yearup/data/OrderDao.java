package org.yearup.data;

import org.yearup.models.Order;

public interface OrderDao {
    /**
     * Creates a new order in the database.
     * @param order The Order object to create.
     * @return The created Order object, with its auto-generated orderId.
     */
    Order createOrder(Order order);

    // Optional: Methods to get orders for a user, or specific order by ID
    // List<Order> getOrdersByUserId(int userId);
    // Order getOrderById(int orderId);
}
