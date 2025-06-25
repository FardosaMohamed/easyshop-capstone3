package org.yearup.data.mysql;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder; // ✅ NEW: Import for generated keys
import org.springframework.jdbc.support.KeyHolder;       // ✅ NEW: Import for generated keys
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.PreparedStatement; // ✅ NEW: Import for PreparedStatement
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // ✅ NEW: Import for Statement
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    private JdbcTemplate jdbcTemplate;
    private ProductDao productDao;

    public MySqlCategoryDao(DataSource dataSource, JdbcTemplate jdbcTemplate, ProductDao productDao)
    {
        super(dataSource);
        this.jdbcTemplate = jdbcTemplate;
        this.productDao = productDao;
    }

    @Override
    public List<Category> getAllCategories()
    {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, (row, index) -> mapRow(row));
    }

    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT category_id, name, description FROM categories WHERE category_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new CategoryRowMapper(), categoryId);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Category with ID " + categoryId + " not found in database.");
            return null;
        }
    }

    @Override
    public Category create(Category category)
    {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder(); // Create a KeyHolder to get the generated ID

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            return ps;
        }, keyHolder); // Pass the KeyHolder to the update method

        // ✅ IMPORTANT: Retrieve the generated ID and set it on the category object
        if (keyHolder.getKey() != null) {
            int newCategoryId = keyHolder.getKey().intValue();
            category.setCategoryId(newCategoryId);
        }

        return category; // Return the category object, now with the correct ID
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        jdbcTemplate.update(sql, category.getName(), category.getDescription(), categoryId);
    }

    @Override
    public void delete(int categoryId)
    {
        System.out.println("DEBUG: MySqlCategoryDao.delete called for categoryId: " + categoryId);

        try {
            System.out.println("DEBUG: Attempting to delete products associated with categoryId: " + categoryId);
            productDao.deleteProductsByCategoryId(categoryId);
            System.out.println("DEBUG: Finished calling productDao.deleteProductsByCategoryId for categoryId: " + categoryId);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to delete associated products for categoryId " + categoryId + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete associated products.", e);
        }

        String sql = "DELETE FROM categories WHERE category_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, categoryId);
            System.out.println("DEBUG: Deleted " + rowsAffected + " category rows for ID: " + categoryId);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("ERROR: DataIntegrityViolationException during category deletion for ID " + categoryId + ". This means products might still be linked. Error: " + e.getMessage());
            throw new RuntimeException("Cannot delete category due to associated products.", e);
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error during category deletion for ID " + categoryId + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting category with ID: " + categoryId, e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(description);

        return category;
    }
}
