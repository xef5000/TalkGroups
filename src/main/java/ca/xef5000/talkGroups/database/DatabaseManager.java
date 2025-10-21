package ca.xef5000.talkGroups.database;

import ca.xef5000.talkGroups.TalkGroups;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manages SQLite database operations for persistent player data.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class DatabaseManager {
    
    private final TalkGroups plugin;
    private Connection connection;
    private final File databaseFile;
    
    /**
     * Creates a new DatabaseManager instance.
     * 
     * @param plugin The plugin instance
     */
    public DatabaseManager(TalkGroups plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "data.db");
    }
    
    /**
     * Initializes the database connection and creates tables.
     */
    public void initialize() {
        try {
            // Ensure data folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            
            // Create tables
            createTables();
            
            plugin.getLogger().info("Database initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    
    /**
     * Creates the necessary database tables.
     */
    private void createTables() throws SQLException {
        String createMutedChannelsTable = 
            "CREATE TABLE IF NOT EXISTS muted_channels (" +
            "player_uuid TEXT NOT NULL, " +
            "channel_id TEXT NOT NULL, " +
            "PRIMARY KEY (player_uuid, channel_id)" +
            ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMutedChannelsTable);
        }
    }
    
    /**
     * Loads muted channels for a player asynchronously.
     * 
     * @param playerId The player's UUID
     * @return CompletableFuture containing the set of muted channel IDs
     */
    public CompletableFuture<Set<String>> loadMutedChannels(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> mutedChannels = new HashSet<>();
            String query = "SELECT channel_id FROM muted_channels WHERE player_uuid = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerId.toString());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        mutedChannels.add(rs.getString("channel_id"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load muted channels for " + playerId, e);
            }
            
            return mutedChannels;
        });
    }
    
    /**
     * Saves a muted channel for a player asynchronously.
     * 
     * @param playerId The player's UUID
     * @param channelId The channel ID to mute
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> saveMutedChannel(UUID playerId, String channelId) {
        return CompletableFuture.runAsync(() -> {
            String insert = "INSERT OR IGNORE INTO muted_channels (player_uuid, channel_id) VALUES (?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, playerId.toString());
                stmt.setString(2, channelId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save muted channel for " + playerId, e);
            }
        });
    }
    
    /**
     * Removes a muted channel for a player asynchronously.
     * 
     * @param playerId The player's UUID
     * @param channelId The channel ID to unmute
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> removeMutedChannel(UUID playerId, String channelId) {
        return CompletableFuture.runAsync(() -> {
            String delete = "DELETE FROM muted_channels WHERE player_uuid = ? AND channel_id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                stmt.setString(1, playerId.toString());
                stmt.setString(2, channelId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to remove muted channel for " + playerId, e);
            }
        });
    }
    
    /**
     * Clears all muted channels for a player asynchronously.
     * 
     * @param playerId The player's UUID
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> clearMutedChannels(UUID playerId) {
        return CompletableFuture.runAsync(() -> {
            String delete = "DELETE FROM muted_channels WHERE player_uuid = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(delete)) {
                stmt.setString(1, playerId.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to clear muted channels for " + playerId, e);
            }
        });
    }
    
    /**
     * Closes the database connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }
    
    /**
     * Gets the database connection.
     * 
     * @return The Connection object
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Checks if the database connection is valid.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}

