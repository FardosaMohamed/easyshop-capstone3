package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderLineItemDao;
import org.yearup.models.OrderLineItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {

    @Autowired
    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public OrderLineItem createOrderLineItem(OrderLineItem lineItem) {
        String sql = "INSERT INTO order_line_items (order_id, product_id, quantity, sales_price, discount) " +
                "VALUES (?, ?, ?, ?, ?);";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setInt(1, lineItem.getOrderId());
            statement.setInt(2, lineItem.getProductId());
            statement.setInt(3, lineItem.getQuantity());
            statement.setBigDecimal(4, lineItem.getSalesPrice());
            statement.setBigDecimal(5, lineItem.getDiscount());

            statement.executeUpdate();
            System.out.println("DEBUG: Created order line item for Order ID " + lineItem.getOrderId() + ", Product ID " + lineItem.getProductId());
            return lineItem;
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Exception creating order line item: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating order line item for order ID: " + lineItem.getOrderId(), e);
        }
    }
}

