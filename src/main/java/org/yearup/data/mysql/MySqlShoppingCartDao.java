package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
// import org.yearup.data.ProductDao; // <-- REMOVE THIS IMPORT
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    // private final ProductDao productDao; // <-- REMOVE THIS FIELD
    private final ShoppingCartItemRowMapper rowMapper;

    @Autowired
    // ✅ THE FIX: Remove ProductDao from the constructor parameters
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
        // this.productDao = productDao; // <-- REMOVE THIS LINE
        this.rowMapper = new ShoppingCartItemRowMapper();
    }

    @Override
    public void deleteShoppingCartItemsByCategoryId(int categoryId) {
        System.out.println("DEBUG: MySqlShoppingCartDao.deleteShoppingCartItemsByCategoryId called for categoryId: " + categoryId);

        String sql = "DELETE sc FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE p.category_id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql, categoryId);
            System.out.println("DEBUG: Deleted " + rowsAffected + " shopping cart items for products in category ID: " + categoryId);
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception during shopping cart item deletion for category ID " + categoryId + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting shopping cart items for category ID: " + categoryId, e);
        }
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        String sql = "SELECT sc.product_id, sc.quantity, " +
                "p.name, p.price, p.category_id, p.description, p.color, p.stock, p.featured, p.image_url " +
                "FROM shopping_cart sc JOIN products p ON sc.product_id = p.product_id " +
                "WHERE sc.user_id = ?;";

        try {
            // Using jdbcTemplate.query with the rowMapper to populate the cart
            jdbcTemplate.query(sql, (rs, rowNum) -> {
                ShoppingCartItem item = rowMapper.mapRow(rs, rowNum);
                cart.addOrUpdateItem(item);
                return null; // We're populating 'cart', so return null for RowMapper's list
            }, userId);

        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception getting shopping cart for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting shopping cart for user ID: " + userId, e);
        }
        cart.calculateTotal();
        return cart;
    }

    @Override
    public ShoppingCartItem getCartItemByUserIdAndProductId(int userId, int productId) {
        String sql = "SELECT sc.product_id, sc.quantity, " +
                "p.name, p.price, p.category_id, p.description, p.color, p.stock, p.featured, p.image_url " +
                "FROM shopping_cart sc JOIN products p ON sc.product_id = p.product_id " +
                "WHERE sc.user_id = ? AND sc.product_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, userId, productId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // Item not found
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception getting cart item for user " + userId + ", product " + productId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting cart item: " + e.getMessage(), e);
        }
    }

    @Override
    public void addProductToCart(int userId, int productId) {
        ShoppingCartItem existingItem = getCartItemByUserIdAndProductId(userId, productId);

        if (existingItem != null) {
            updateProductQuantity(userId, productId, existingItem.getQuantity() + 1);
        } else {
            String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1);";
            try {
                jdbcTemplate.update(sql, userId, productId);
            } catch (Exception e) {
                System.err.println("ERROR: SQL Exception adding product " + productId + " to cart for user " + userId + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error adding product to cart: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity) {
        if (quantity <= 0) {
            deleteProductFromCart(userId, productId);
            return;
        }

        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?;";
        try {
            int rowsAffected = jdbcTemplate.update(sql, quantity, userId, productId);
            if (rowsAffected == 0) {
                System.out.println("WARN: Attempted to update non-existent cart item for user " + userId + ", product " + productId);
            }
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception updating product " + productId + " quantity for user " + userId + " to " + quantity + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating product quantity in cart: " + e.getMessage(), e);
        }
    }

    @Override
    public void clearShoppingCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";
        try {
            int rowsAffected = jdbcTemplate.update(sql, userId);
            System.out.println("DEBUG: Cleared " + rowsAffected + " items from cart for user " + userId + ".");
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception clearing shopping cart for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error clearing shopping cart: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProductFromCart(int userId, int productId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?;";
        try {
            int rowsAffected = jdbcTemplate.update(sql, userId, productId);
            if (rowsAffected == 0) {
                System.out.println("WARN: Attempted to delete non-existent cart item for user " + userId + ", product " + productId);
            }
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception deleting product " + productId + " from cart for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting product from cart: " + e.getMessage(), e);
        }
    }
}
