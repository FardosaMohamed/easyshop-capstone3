// src/main/java/org/yearup/data/mysql/ShoppingCartItemRowMapper.java
package org.yearup.data.mysql;

import org.springframework.jdbc.core.RowMapper;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCartItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;

// This class maps a row from the database ResultSet to a ShoppingCartItem object.
// It assumes the SQL query includes both shopping_cart table columns and joined product details.
public class ShoppingCartItemRowMapper implements RowMapper<ShoppingCartItem> {

    @Override
    public ShoppingCartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        ShoppingCartItem item = new ShoppingCartItem();
        Product product = new Product(); // Create a product object to hold the product details

        // 1. Map Product details from the joined 'products' table columns
        // These column names must match what your SQL query selects (e.g., from `p.name`, `p.price`, etc.)
        product.setProductId(rs.getInt("product_id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setDescription(rs.getString("description"));
        product.setColor(rs.getString("color"));
        product.setStock(rs.getInt("stock"));
        product.setFeatured(rs.getBoolean("featured"));
        product.setImageUrl(rs.getString("image_url"));

        item.setProduct(product); // Set the populated Product object into the ShoppingCartItem

        // 2. Map ShoppingCartItem-specific details from the 'shopping_cart' table columns
        // The quantity column is directly from the shopping_cart table
        item.setQuantity(rs.getInt("quantity"));

        // If you had a 'discount_percent' column in shopping_cart, you would map it here:
        // item.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        // Otherwise, it defaults to BigDecimal.ZERO from the ShoppingCartItem constructor

        // 3. Calculate the line total for this item
        // The calculateLineTotal() method in ShoppingCartItem will use the product's price,
        // quantity, and discount to compute this.
        item.calculateLineTotal();

        return item;
    }
}
