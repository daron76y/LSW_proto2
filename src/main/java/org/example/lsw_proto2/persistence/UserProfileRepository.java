package org.example.lsw_proto2.persistence;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository {
    //CRUD OPERATIONS FOR USER PROFILES
    void saveUser(UserProfile userProfile);
    Optional<UserProfile> getUserByName(String username);
    boolean exists(String username);
}
