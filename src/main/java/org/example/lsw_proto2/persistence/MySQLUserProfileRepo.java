package org.example.lsw_proto2.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lsw_proto2.core.Party;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLUserProfileRepo implements UserProfileRepository {
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();

    public MySQLUserProfileRepo(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_profiles (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                score INT NOT NULL,
                savedParties JSON,
                campaignSaves JSON
            )
        """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveUser(UserProfile userProfile) {
        try {
            String sql = """
                REPLACE INTO user_profiles
                (username, password, score, savedParties, campaignSaves)
                VALUES (?, ?, ?, ?, ?)
            """;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, userProfile.getUsername());
                ps.setString(2, userProfile.getPassword());
                ps.setInt(3, userProfile.getScore());
                ps.setString(4, mapper.writeValueAsString(userProfile.getSavedParties()));
                ps.setString(5, mapper.writeValueAsString(userProfile.getCampaignSaves()));
                ps.executeUpdate();
            }
        } catch(SQLException | JsonProcessingException e)  {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUserByName(String username) {
        try {
            String sql = "DELETE FROM user_profiles WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<UserProfile> getUserByName(String username) {
        try {
            String sql = "SELECT * FROM user_profiles WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    //no user exists with this name
                    if (!rs.next()) return Optional.empty();

                    //construct the user profile object
                    UserProfile user = new UserProfile(
                            rs.getString("username"),
                            rs.getString("password")
                    );
                    user.increaseScore(rs.getInt("score"));

                    //de-serialize saved parties
                    String partiesJson = rs.getString("savedParties");
                    if (partiesJson != null) {
                        List<Party> parties = mapper.readValue(
                            partiesJson,
                            mapper.getTypeFactory().constructCollectionType(List.class, Party.class)
                        );
                        parties.forEach(user::saveParty);
                    }

                    //de-serialize campaign saves
                    String campaignsJson = rs.getString("campaignSaves");
                    if (campaignsJson != null) {
                        List<UserProfile.CampaignProgress> campaigns = mapper.readValue(
                            campaignsJson,
                            mapper.getTypeFactory().constructCollectionType(List.class, UserProfile.CampaignProgress.class)
                        );
                        campaigns.forEach(c -> user.saveCampaign(
                            c.getCampaignName(),
                            c.getPartyName(),
                            c.getCurrentRoom()
                        ));
                    }

                    //return the final constructed user
                    return Optional.of(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<UserProfile> getAllUsers() {
        List<UserProfile> userProfiles = new ArrayList<>();
        try {
            String sql = "SELECT username FROM user_profiles";
            try (Statement statement = connection.createStatement()) {
                try (ResultSet rs = statement.executeQuery(sql)) {
                    while (rs.next()) {
                        getUserByName(rs.getString("username")).ifPresent(userProfiles::add);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userProfiles;
    }

    @Override
    public boolean exists(String username) {
        try {
            String sql = "SELECT 1 FROM user_profiles WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
