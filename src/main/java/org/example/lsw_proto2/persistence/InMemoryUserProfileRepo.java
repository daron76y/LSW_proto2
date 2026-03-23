package org.example.lsw_proto2.persistence;

import java.util.*;

/**
 * In-memory implementation of UserProfileRepository.
 * all data is lost when the application closes.
 */
public class InMemoryUserProfileRepo implements UserProfileRepository {
    private final Map<String, UserProfile> store = new HashMap<>();

    @Override
    public void saveUser(UserProfile userProfile) {
        store.put(userProfile.getUsername(), userProfile);
    }

    @Override
    public Optional<UserProfile> getUserByName(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public boolean exists(String username) {
        return store.containsKey(username);
    }
}