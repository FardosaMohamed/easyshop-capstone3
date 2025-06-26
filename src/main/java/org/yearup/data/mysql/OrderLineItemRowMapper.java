package org.yearup.data.mysql;

import org.springframework.jdbc.core.RowMapper;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Product; // Need Product model for the line item

import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

public class OrderLineItemRowMapper implements RowMapper<OrderLineItem> {

    @Override
    public OrderLineItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderLineItem lineItem = new OrderLineItem();

        lineItem.setOrderId(rs.getInt("order_id"));
        lineItem.setProductId(rs.getInt("product_id"));
        lineItem.setQuantity(rs.getInt("quantity"));
        lineItem.setSalesPrice(rs.getBigDecimal("sales_price"));
        lineItem.setDiscount(rs.getBigDecimal("discount"));

        // If your SQL query joins the 'products' table, you can populate the Product object here
        // Example if 'products.name' etc. are in the ResultSet:
        try {
            Product product = new Product();
            product.setProductId(rs.getInt("product_id")); // Ensure this is mapped correctly from the join
            product.setName(rs.getString("product_name")); // Assuming 'product_name' alias from join
            product.setPrice(rs.getBigDecimal("product_price")); // Assuming 'product_price' alias
            // Map other product fields as needed

            lineItem.setProduct(product);
        } catch (SQLException e) {
            // If product details are not in ResultSet (no join), this part will fail.
            // That's okay if you only need productId for the line item in this context.
            System.out.println("DEBUG: Product details not found in ResultSet for OrderLineItem mapping. Proceeding without full Product object.");
        }

        // Calculate line total based on mapped values
        lineItem.calculateLineTotal();

        return lineItem;
    }
}

