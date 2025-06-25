package org.yearup.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderUtility {

    public static void main(String[] args) {
        // Instantiate the same PasswordEncoder you use in your Spring Boot app
        // Make sure the strength (e.g., 10, 12) is the same if you specify it.
        // If not specified, the default is 10.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String plaintextPassword = "password"; // The password you want to test
        String encodedPasswordFromDb = "$2a$10$NkufUPF3V8dEPSZeo1fzHe9ScBu.LOay9S3N32M84yuUM2OJYEJ/."; // Replace with the hash from your DB

        // Encode the plaintext password
        String newEncodedPassword = passwordEncoder.encode(plaintextPassword);

        System.out.println("Plaintext Password: " + plaintextPassword);
        System.out.println("Newly Encoded Password (for " + plaintextPassword + "): " + newEncodedPassword);
        System.out.println("Encoded Password from DB: " + encodedPasswordFromDb);

        // Compare the plaintext password with the hash from the database
        boolean matches = passwordEncoder.matches(plaintextPassword, encodedPasswordFromDb);
        System.out.println("Does plaintext password match DB hash? " + matches);

    }
}
