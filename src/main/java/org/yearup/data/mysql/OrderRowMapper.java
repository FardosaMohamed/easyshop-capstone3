package org.yearup.data.mysql;

import org.springframework.jdbc.core.RowMapper;
import org.yearup.models.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        // Assuming your 'date' column in DB is DATETIME or TIMESTAMP
        order.setDate(rs.getTimestamp("date").toLocalDateTime());
        order.setAddress(rs.getString("address"));
        order.setCity(rs.getString("city"));
        order.setState(rs.getString("state"));
        order.setZip(rs.getString("zip"));
        order.setShippingAmount(rs.getBigDecimal("shipping_amount"));
        order.setTotal(rs.getBigDecimal("total")); // Map total from DB, or calculate if not stored

        // Note: Line items are NOT mapped here. They would be fetched by a separate DAO call
        // and set into the Order object if a full order (with items) is desired.
        return order;
    }
}
