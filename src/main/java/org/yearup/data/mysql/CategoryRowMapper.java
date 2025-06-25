package org.yearup.data.mysql; // Or org.yearup.data.mappers if you prefer

import org.springframework.jdbc.core.RowMapper;
import org.yearup.models.Category; // Import your Category model
import java.sql.ResultSet;
import java.sql.SQLException;

// This class maps a row from the database ResultSet to a Category object
public class CategoryRowMapper implements RowMapper<Category> {

    @Override
    public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Create a new Category object
        Category category = new Category();

        // Populate the Category object with data from the ResultSet
        // Ensure column names match your database schema
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));

        // Return the populated Category object
        return category;
    }
}
