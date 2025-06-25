package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
// You might also need imports for ShoppingCartItem and ShoppingCart models
// import org.yearup.models.ShoppingCartItem;
// import org.yearup.models.ShoppingCart;

import javax.sql.DataSource; // Needed if extending MySqlDaoBase

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private JdbcTemplate jdbcTemplate;

    // Assuming you inject DataSource via MySqlDaoBase's constructor,
    // and then JdbcTemplate is configured elsewhere and can be autowired.
    // Or, you can create JdbcTemplate here if you only inject DataSource:
    // public MySqlShoppingCartDao(DataSource dataSource) {
    //    super(dataSource);
    //    this.jdbcTemplate = new JdbcTemplate(dataSource);
    // }
    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource, JdbcTemplate jdbcTemplate) // Adjust constructor if needed
    {
        super(dataSource); // Call super constructor
        this.jdbcTemplate = jdbcTemplate;
    }

    // You would add implementations for other ShoppingCartDao methods here

    @Override
    public void deleteShoppingCartItemsByCategoryId(int categoryId) {
        System.out.println("DEBUG: MySqlShoppingCartDao.deleteShoppingCartItemsByCategoryId called for categoryId: " + categoryId);

        // SQL to delete shopping cart items where the product's category_id matches
        // This requires a JOIN to link shopping_cart items to products.
        String sql = "DELETE sc FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE p.category_id = ?";

        try {
            int rowsAffected = jdbcTemplate.update(sql, categoryId);
            System.out.println("DEBUG: Deleted " + rowsAffected + " shopping cart items for products in category ID: " + categoryId);
        } catch (Exception e) {
            System.err.println("ERROR: SQL Exception during shopping cart item deletion for category ID " + categoryId + ". Error: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            throw new RuntimeException("Error deleting shopping cart items for category ID: " + categoryId, e);
        }
    }
}

