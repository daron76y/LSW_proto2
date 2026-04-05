package org.example.lsw_proto2.app;

import org.example.lsw_proto2.persistence.UserProfile;
import org.example.lsw_proto2.persistence.UserProfileRepository;

public class AuthService {

    private final UserProfileRepository userRepo;

    public AuthService(UserProfileRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Attempt to log in. Returns an error message on failure, or null on success.
     */
    public UserProfile login(String username, String password) {
        if (username.isBlank()) throw new IllegalArgumentException("Username cannot be empty.");
        if (password.isBlank()) throw new IllegalArgumentException("Password cannot be empty.");

        var profileOpt = userRepo.getUserByName(username);
        if (profileOpt.isEmpty()) throw new IllegalArgumentException("No account found for \"" + username + "\".");
        if (!profileOpt.get().getPassword().equals(password)) throw new IllegalArgumentException("Incorrect password.");

        return profileOpt.get();
    }

    /**
     * Attempt to create a new account. Returns an error message on failure, or null on success.
     */
    public UserProfile createAccount(String username, String password) {
        if (username.isBlank()) throw new IllegalArgumentException("Username cannot be empty.");
        if (password.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters.");
        if (userRepo.exists(username)) throw new IllegalArgumentException("Username \"" + username + "\" is already taken.");

        UserProfile newUser = new UserProfile(username, password);
        userRepo.saveUser(newUser);
        return newUser;
    }
}