// src/main/java/org/yearup/data/ProfileDao.java
package org.yearup.data;

import org.yearup.models.Profile;

public interface ProfileDao
{
    /**
     * Retrieves a user's profile by their user ID.
     * @param userId The ID of the user.
     * @return The Profile object, or null if not found.
     */
    Profile getByUserId(int userId);

    /**
     * Creates a new profile in the database.
     * This is typically called during user registration.
     * @param profile The Profile object to create.
     * @return The created Profile object (possibly with a generated ID if applicable).
     */
    Profile create(Profile profile);

    void update(int userId, Profile profile);
}