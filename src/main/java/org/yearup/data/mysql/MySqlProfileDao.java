package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*; // Import all necessary SQL classes

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile)
    {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = getConnection())
        {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            System.out.println("DEBUG: Profile created for user ID: " + profile.getUserId());
            return profile;
        }
        catch (SQLException e)
        {
            System.err.println("ERROR: SQL Exception in create profile for user ID " + profile.getUserId() + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating profile for user ID: " + profile.getUserId(), e);
        }
    }

    @Override
    public Profile getByUserId(int userId)
    {
        String sql = "SELECT user_id, first_name, last_name, phone, email, address, city, state, zip FROM profiles WHERE user_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            System.err.println("ERROR: SQL Exception in getByUserId for user ID " + userId + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting profile by user ID: " + userId, e);
        }
        return null; // Return null if no profile is found
    }

    @Override // <-- This is the missing update method implementation
    public void update(int userId, Profile profile)
    {
        String sql = "UPDATE profiles SET first_name = ?, last_name = ?, phone = ?, email = ?, " +
                "address = ?, city = ?, state = ?, zip = ? WHERE user_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            // Set parameters based on the Profile object provided
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            statement.setString(3, profile.getPhone());
            statement.setString(4, profile.getEmail());
            statement.setString(5, profile.getAddress());
            statement.setString(6, profile.getCity());
            statement.setString(7, profile.getState());
            statement.setString(8, profile.getZip());
            statement.setInt(9, userId); // The WHERE clause uses the userId from the method parameter

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("WARN: No rows affected during profile update for user ID: " + userId + ". Profile might not exist or no changes were made.");
            } else {
                System.out.println("DEBUG: Profile for user ID " + userId + " updated successfully. Rows affected: " + rowsAffected);
            }

        }
        catch (SQLException e)
        {
            System.err.println("ERROR: SQL Exception during profile update for user ID " + userId + ". Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating profile for user ID: " + userId, e);
        }
    }


    // Helper method to map a ResultSet row to a Profile object
    private Profile mapRow(ResultSet row) throws SQLException
    {
        int userId = row.getInt("user_id");
        String firstName = row.getString("first_name");
        String lastName = row.getString("last_name");
        String phone = row.getString("phone");
        String email = row.getString("email");
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");

        // Construct the Profile object. Ensure your Profile model has a constructor
        // that matches these parameters, or use setters.
        Profile profile = new Profile(userId, firstName, lastName, phone, email, address, city, state, zip);
        return profile;
    }
}
