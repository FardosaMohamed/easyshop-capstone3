// src/main/java/org/yearup/controllers/ProfileController.java
package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal; // For getting the logged-in user's name

@RestController
@RequestMapping("/profile") // This controller handles requests to /profile
@CrossOrigin // Allow cross-origin requests
@PreAuthorize("isAuthenticated()") // Only logged-in users can access this controller
public class ProfileController {

    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public ProfileController(UserDao userDao, ProfileDao profileDao) {
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    /**
     * Retrieves the profile for the currently logged-in user.
     * URL: GET http://localhost:8080/profile
     *
     * @param principal The security principal representing the logged-in user.
     * @return The Profile object for the current user.
     */
    @GetMapping
    public Profile getProfile(Principal principal) {
        try {
            // Get the currently logged-in username
            String userName = principal.getName();
            System.out.println("DEBUG: getProfile called for username: " + userName);

            // Find database user by username to get their userId
            User user = userDao.getByUserName(userName);
            if (user == null) {
                System.err.println("ERROR: User not found in database for username: " + userName);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged-in user not found in the database.");
            }
            int userId = user.getId();
            System.out.println("DEBUG: Fetching profile for user ID: " + userId);

            // Use the profileDao to get the profile by userId
            Profile profile = profileDao.getByUserId(userId);
            if (profile == null) {
                System.err.println("ERROR: Profile not found for user ID: " + userId + ". A profile should exist upon user registration.");
                // Depending on your application's logic, you might create a default profile here
                // if it doesn't exist, rather than throwing NOT_FOUND. For now, it assumes creation on register.
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for this user. Ensure it was created during registration.");
            }

            return profile;
        } catch (ResponseStatusException ex) {
            throw ex; // Re-throw specific HTTP errors (like 404)
        } catch (Exception e) {
            System.err.println("ERROR: Error getting profile for user " + principal.getName() + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad. Could not retrieve profile.", e);
        }
    }

    /**
     * Updates the profile for the currently logged-in user.
     * URL: PUT http://localhost:8080/profile
     * Body: Profile object with updated details.
     * Only the logged-in user can update their own profile.
     *
     * @param principal The security principal representing the logged-in user.
     * @param profile The Profile object containing the updated details.
     */
    @PutMapping // Annotation to handle HTTP PUT requests to /profile
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content is a common response for successful PUT updates that don't return data
    public void updateProfile(Principal principal, @RequestBody Profile profile) {
        try {
            String userName = principal.getName();
            System.out.println("DEBUG: Attempting to update profile for username: " + userName);

            User user = userDao.getByUserName(userName);
            if (user == null) {
                System.err.println("ERROR: User not found in database for username: " + userName + " during profile update.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Logged-in user not found for profile update.");
            }
            int userId = user.getId();

            // IMPORTANT SECURITY CHECK:
            // Ensure the userId from the request body (if present and not 0) matches the logged-in user's ID.
            // This prevents a user from trying to update another user's profile by manipulating the body.
            // If the body's userId is 0, it means it was not explicitly set in the request, which is fine.
            if (profile.getUserId() != 0 && profile.getUserId() != userId) {
                System.err.println("WARN: Profile update attempt for user ID mismatch. Principal user ID: " + userId + ", Request body user ID: " + profile.getUserId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update another user's profile.");
            }

            // Always ensure the profile object sent to the DAO has the correct userId from the authenticated principal
            profile.setUserId(userId);

            // Optional: Check if the profile *actually* exists before updating.
            // If the profile doesn't exist, it should have been created during registration.
            Profile existingProfile = profileDao.getByUserId(userId);
            if (existingProfile == null) {
                System.err.println("ERROR: Profile not found for user ID " + userId + " during update. Cannot update a non-existent profile.");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile for this user was not found. Cannot update.");
            }

            // Call the DAO to perform the update operation
            profileDao.update(userId, profile);
            System.out.println("DEBUG: Profile updated successfully for user ID: " + userId);

        } catch (ResponseStatusException ex) {
            throw ex; // Re-throw specific HTTP errors
        } catch (Exception e) {
            System.err.println("ERROR: An unexpected error occurred during profile update for user " + principal.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad. Could not update profile.", e);
        }
    }
}
