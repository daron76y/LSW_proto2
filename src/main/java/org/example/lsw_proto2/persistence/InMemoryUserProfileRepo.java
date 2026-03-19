package org.example.lsw_proto2.persistence;

import java.util.*;

/**
 * Stub in-memory implementation of UserProfileRepository.
 * All data is lost when the application closes.
 */
public class InMemoryUserProfileRepo implements UserProfileRepository {
    private final Map<String, UserProfile> store = new HashMap<>();

    @Override
    public void saveUser(UserProfile userProfile) {
        store.put(userProfile.getUsername(), userProfile);
    }

    @Override
    public void deleteUserByName(String username) {
        store.remove(username);
    }

    @Override
    public Optional<UserProfile> getUserByName(String username) {
        return Optional.ofNullable(store.get(username));
    }

    @Override
    public List<UserProfile> getAllUsers() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean exists(String username) {
        return store.containsKey(username);
    }
}