package org.yearup.data;

import org.yearup.models.OrderLineItem;

public interface OrderLineItemDao {
    /**
     * Creates a new order line item in the database.
     * @param lineItem The OrderLineItem object to create.
     * @return The created OrderLineItem.
     */
    OrderLineItem createOrderLineItem(OrderLineItem lineItem);

    // Optional: Methods to get line items for a specific order
    // List<OrderLineItem> getOrderLineItemsByOrderId(int orderId);
}

