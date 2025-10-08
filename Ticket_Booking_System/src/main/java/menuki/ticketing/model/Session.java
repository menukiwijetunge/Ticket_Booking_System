package menuki.ticketing.model;

/**
 * Manages the session state for the currently logged-in user.
 */
public final class Session {
    private static String currentUsername;

    //Constructor
    private Session() {}

    //Setters
    public static void setCurrentUsername(String username) {currentUsername = username;}

    //Getters
    public static String getCurrentUsername() {return currentUsername;}

    // Utility to check if a user is logged in
    public static boolean isLoggedIn() {
        return currentUsername != null && !currentUsername.isBlank();
    }

    // Clear Session
    public static void clearSession() {
        currentUsername = null;
    }
}
