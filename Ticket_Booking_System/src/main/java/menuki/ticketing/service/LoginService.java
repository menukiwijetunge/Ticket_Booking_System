package menuki.ticketing.service;

import menuki.ticketing.data.dao.UserDao;
import menuki.ticketing.model.User;

/**
 * Service layer acting as the bridge between DAOs and the rest of the application
 * Handles business logic for logging in
 */
public class LoginService {
    private final UserDao userDao;

    //Constructor
    public LoginService(UserDao userDao) {  // inject!
        this.userDao = userDao;
    }

    /*
    Business logic for handling user authentication
     */
    public User authenticate(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
}
