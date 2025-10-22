package db;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for securely hashing and verifying passwords using BCrypt.
 */
public class PasswordUtil {

    // Define the BCrypt strength (cost factor). Higher is slower but more secure.
    private static final int WORKLOAD = 12;

    /**
     * Hashes a plain text password using BCrypt.
     * @param plainPassword The password to hash.
     * @return The secure salted and hashed password string.
     */
    public static String hashPassword(String plainPassword) {
        // Generate a salt and hash the password in one step
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORKLOAD));
    }

    /**
     * Verifies a plain text password against a stored BCrypt hash.
     * @param plainPassword The plain text password entered by the user.
     * @param storedHash The BCrypt hash retrieved from the database.
     * @return true if the password matches the hash, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        // BCrypt handles the extraction of the salt and the comparison internally
        // Returns false automatically if the hash is invalid/malformed
        return BCrypt.checkpw(plainPassword, storedHash);
    }
}