package org.example.lsw_proto2.persistence;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.example.lsw_proto2.core.Party;

import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of UserProfileRepository.
 * Connects to a local MySQL server. Configures the connection URL, username,
 * and password in SceneManager.buildMySQLRepo().
 * Parties (savedParties, pvpParties) and campaign saves are stored as JSON columns.
 * Jackson handles serialization/deserialization with full polymorphism type info for
 * Ability and Effect subclasses, so all runtime state is properly reconstructed.
 * Schema (automatically made on the first run):
 *   CREATE TABLE IF NOT EXISTS user_profiles (
 *       username       VARCHAR(50)  PRIMARY KEY,
 *       password       VARCHAR(255) NOT NULL,
 *       score          INT          NOT NULL DEFAULT 0,
 *       pvp_wins       INT          NOT NULL DEFAULT 0,
 *       pvp_losses     INT          NOT NULL DEFAULT 0,
 *       saved_parties  JSON,
 *       campaign_saves JSON,
 *       pvp_parties    JSON
 *   );
 */
public class MySQLUserProfileRepo implements UserProfileRepository {
    //-----------------------------------------------------------------------
    //|                      JDBC Connection                                |
    //-----------------------------------------------------------------------
    //JDBC connection string for a local MySQL server
    //format: jdbc:mysql://<host>:<port>/<database>?<options>
    //tables are created automatically on first run.
    public static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/lsw_game" +
                    "?useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC" +
                    "&createDatabaseIfNotExist=true";

    private final Connection connection;

    /**
     * ObjectMapper configured to access private fields directly (via @JsonAutoDetect!),
     * write to final fields via java reflection (needed for Unit name, Party name, etc.),
     * and override access modifiers (needed to access private final fields)
     */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .enable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .build();

    //singleton instance for Singleton Pattern
    private static MySQLUserProfileRepo instance;

    //-----------------------------------------------------------------------
    //|                            Constructor                              |
    //-----------------------------------------------------------------------

    //private for Singleton pattern
    private MySQLUserProfileRepo(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }

    /**
     * Factory method - creates a connection to the local MySQL server and
     * returns a ready-to-use repo instance.
     *
     * @param username MySQL user (i.e, "root")
     * @param password MySQL password
     */
    public static synchronized MySQLUserProfileRepo connect(String username, String password) {
        return connect(DEFAULT_URL, username, password);
    }

    //this overloaded version follows the Singleton pattern by instantiating only if the instance is null
    public static synchronized MySQLUserProfileRepo connect(String url, String username, String password) {
        if (instance == null) {
            try {
                Connection conn = DriverManager.getConnection(url, username, password);
                instance = new MySQLUserProfileRepo(conn);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to MySQL at: " + url, e);
            }
        }
        return instance;
    }

    //-----------------------------------------------------------------------
    //|                          MySQL schema                               |
    //-----------------------------------------------------------------------

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS user_profiles (
                    username       VARCHAR(50)  PRIMARY KEY,
                    password       VARCHAR(255) NOT NULL,
                    score          INT          NOT NULL DEFAULT 0,
                    pvp_wins       INT          NOT NULL DEFAULT 0,
                    pvp_losses     INT          NOT NULL DEFAULT 0,
                    saved_parties  JSON,
                    campaign_saves JSON,
                    pvp_parties    JSON
                )
                """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user_profiles table", e);
        }
    }

    // -----------------------------------------------------------------------
    // |                        Repository Methods                           |
    // -----------------------------------------------------------------------

    @Override
    public void saveUser(UserProfile user) {
        String sql = """
                INSERT INTO user_profiles
                    (username, password, score, pvp_wins, pvp_losses, saved_parties, campaign_saves, pvp_parties)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    password       = VALUES(password),
                    score          = VALUES(score),
                    pvp_wins       = VALUES(pvp_wins),
                    pvp_losses     = VALUES(pvp_losses),
                    saved_parties  = VALUES(saved_parties),
                    campaign_saves = VALUES(campaign_saves),
                    pvp_parties    = VALUES(pvp_parties)
                """;

        //create the PreparedStatement
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt   (3, user.getScore());
            ps.setInt   (4, user.getPvpWins());
            ps.setInt   (5, user.getPvpLosses());
            ps.setString(6, toJson(user.getSavedParties()));
            ps.setString(7, toJson(user.getCampaignSaves()));
            ps.setString(8, toJson(user.getPvpParties()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user: " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<UserProfile> getUserByName(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM user_profiles WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(buildUserProfile(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user: " + username, e);
        }
    }

    @Override
    public boolean exists(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM user_profiles WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check existence of user: " + username, e);
        }
    }

    //-----------------------------------------------------------------------
    //|                              Helpers                                |
    //-----------------------------------------------------------------------

    /**
     * Reconstruct a UserProfile from the current ResultSet row.
     * each json column is deserialized back into the corresponding Java type.
     */
    private UserProfile buildUserProfile(ResultSet rs) throws Exception {
        UserProfile user = new UserProfile(
                rs.getString("username"),
                rs.getString("password")
        );
        user.increaseScore(rs.getInt("score"));

        // pvp wins/losses - stored as plain INT columns
        user.setPvpWins(rs.getInt("pvp_wins"));
        user.setPvpLosses(rs.getInt("pvp_losses"));

        //saved_parties (PvE campaign parties)
        String savedPartiesJson = rs.getString("saved_parties");
        if (savedPartiesJson != null) {
            List<Party> parties = fromJsonList(savedPartiesJson, Party.class);
            parties.forEach(user::saveParty);
        }

        //campaign_saves
        String campaignJson = rs.getString("campaign_saves");
        if (campaignJson != null) {
            List<UserProfile.CampaignProgress> saves = fromJsonList(campaignJson, UserProfile.CampaignProgress.class);
            saves.forEach(c -> user.saveCampaign(c.getCampaignName(), c.getPartyName(), c.getCurrentRoom()));
        }

        //pvp_parties
        String pvpJson = rs.getString("pvp_parties");
        if (pvpJson != null) {
            List<Party> pvpParties = fromJsonList(pvpJson, Party.class);
            pvpParties.forEach(user::addPvpParty);
        }

        return user;
    }

    /** Serialize any object to a JSON string. */
    private String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed for: " + obj, e);
        }
    }

    /** Deserialize a JSON array string into a List of the given type. */
    private <T> List<T> fromJsonList(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
        } catch (Exception e) {
            throw new RuntimeException("JSON deserialization failed for type " + type.getSimpleName(), e);
        }
    }
}