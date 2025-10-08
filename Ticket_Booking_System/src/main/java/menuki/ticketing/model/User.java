package menuki.ticketing.model;

/**
 * This is the model class for User. This defines the basic structure and operations for a user in the system.
 */
public class User {
    private String username;
    private String password;
    private String role;

    //Constructor
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    //Getters
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getPassword() { return password; }

    //Setters
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    //Overriding Object methods
    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "'}";
    }
}
