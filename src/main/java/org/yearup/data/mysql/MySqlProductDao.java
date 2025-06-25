package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    private ShoppingCartDao shoppingCartDao;

    public MySqlProductDao(DataSource dataSource,ShoppingCartDao shoppingCartDao)
    {
        super(dataSource);
        this.shoppingCartDao = shoppingCartDao;
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color)
    {
        List<Product> products = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM products WHERE 1=1 "); // Start with 1=1 for easy appending
        List<Object> params = new ArrayList<>();

        // Add Category ID condition if provided and valid
        if (categoryId != null && categoryId > 0) { // Assuming categoryId > 0 is valid
            sqlBuilder.append(" AND category_id = ? ");
            params.add(categoryId);
        }

        // Add Min Price condition if provided and valid
        // Using compareTo for BigDecimal: 0 if equal, >0 if this is greater, <0 if this is less
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) { // Ensure minPrice is 0 or positive
            sqlBuilder.append(" AND price >= ? ");
            params.add(minPrice);
        }

        // Add Max Price condition if provided and valid
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) >= 0) { // Ensure maxPrice is 0 or positive
            sqlBuilder.append(" AND price <= ? ");
            params.add(maxPrice);
        }

        // Add Color condition if provided and not empty
        if (color != null && !color.trim().isEmpty()) {
            sqlBuilder.append(" AND color = ? ");
            params.add(color.trim()); // Trim to handle accidental spaces
        }

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString());

            // Set parameters dynamically based on what was added to the list
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    statement.setInt(i + 1, (Integer) param);
                } else if (param instanceof BigDecimal) {
                    statement.setBigDecimal(i + 1, (BigDecimal) param);
                } else if (param instanceof String) {
                    statement.setString(i + 1, (String) param);
                } else {
                    // Handle unexpected parameter types if necessary
                    throw new RuntimeException("Unexpected parameter type: " + param.getClass().getName());
                }
            }

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error searching products: " + e.getMessage(), e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                " WHERE category_id = ? ";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }


    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null; // Return null if no product is found
    }

    @Override
    public Product create(Product product)
    {
        String sql = "INSERT INTO products(name, price, category_id, description, color, image_url, stock, featured) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = getConnection())
        {
            // Use PreparedStatement.RETURN_GENERATED_KEYS to get the auto-incremented ID
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newProductId = generatedKeys.getInt(1);
                    // ✅ THE FIX: Set the generated ID on the product object directly
                    product.setProductId(newProductId);
                    System.out.println("DEBUG: Product created with ID: " + newProductId); // Add debug log
                    return product; // Return the now-updated product object
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("ERROR: SQL Exception during product creation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating product: " + e.getMessage(), e);
        }
        return null; // Return null if creation failed or no ID generated
    }

    @Override
    public void update(int productId, Product product)
    {
        String sql = "UPDATE products" +
                " SET name = ? " +
                "   , price = ? " +
                "   , category_id = ? " +
                "   , description = ? " +
                "   , color = ? " +
                "   , image_url = ? " +
                "   , stock = ? " +
                "   , featured = ? " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {

        String sql = "DELETE FROM products " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    // ✅ ADD THIS NEW METHOD IMPLEMENTATION
    @Override
    public void deleteProductsByCategoryId(int categoryId)
    {
        String sql = "DELETE FROM products WHERE category_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            // This will likely throw an exception if the category doesn't exist,
            // but for foreign key cascade, we only care that existing products are deleted.
            throw new RuntimeException("Error deleting products for category ID: " + categoryId, e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
}
