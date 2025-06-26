package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.Profile; // Needed to get address info

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    private final OrderRowMapper orderRowMapper;

    @Autowired
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
        this.orderRowMapper = new OrderRowMapper();
    }

    @Override
    public Order createOrder(Order order) {
        // SQL for inserting a new order
        String sql = "INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount, total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            statement.setInt(1, order.getUserId());
            // Convert LocalDateTime to Timestamp for database storage
            statement.setTimestamp(2, Timestamp.valueOf(order.getDate()));
            statement.setString(3, order.getAddress());
            statement.setString(4, order.getCity());
            statement.setString(5, order.getState());
            statement.setString(6, order.getZip());
            statement.setBigDecimal(7, order.getShippingAmount());
            statement.setBigDecimal(8, order.getTotal()); // Ensure total is calculated before calling this

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newOrderId = generatedKeys.getInt(1);
                    order.setOrderId(newOrderId); // Set the auto-generated ID back to the Order object
                    System.out.println("DEBUG: Created new order with ID: " + newOrderId);
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Exception creating order: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating order for user ID: " + order.getUserId(), e);
        }
        return null; // Return null if order creation failed
    }
}
