package org.yearup.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int userId;
    private LocalDateTime date; // Stores the order date and time
    private String address;
    private String city;
    private String state;
    private String zip;
    private BigDecimal shippingAmount; // Optional: could be 0 or calculated
    private BigDecimal total; // Total amount of the order
    private List<OrderLineItem> lineItems; // List of items in this order

    public Order() {
        this.lineItems = new ArrayList<>();
        this.date = LocalDateTime.now(); // Default to current time
        this.shippingAmount = BigDecimal.ZERO; // Default to no shipping cost
        this.total = BigDecimal.ZERO; // Will be calculated
    }

    public Order(int orderId, int userId, LocalDateTime date, String address, String city, String state, String zip, BigDecimal shippingAmount, BigDecimal total) {
        this(); // Call default constructor for lineItems and defaults
        this.orderId = orderId;
        this.userId = userId;
        this.date = date;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.shippingAmount = shippingAmount;
        this.total = total;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderLineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<OrderLineItem> lineItems) {
        this.lineItems = lineItems;
        // Optionally recalculate total if setting line items directly,
        // but typically total would be calculated from the DAO or controller after populating
        calculateTotalFromLineItems();
    }

    public void addLineItem(OrderLineItem item) {
        this.lineItems.add(item);
        calculateTotalFromLineItems();
    }

    // Helper to calculate total based on line items
    public void calculateTotalFromLineItems() {
        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (OrderLineItem item : lineItems) {
            if (item.getLineTotal() != null) {
                calculatedTotal = calculatedTotal.add(item.getLineTotal());
            }
        }
        this.total = calculatedTotal.add(this.shippingAmount); // Include shipping in total
    }
}
