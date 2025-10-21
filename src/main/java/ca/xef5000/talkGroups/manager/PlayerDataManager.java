package ca.xef5000.talkGroups.manager;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.database.DatabaseManager;
import ca.xef5000.talkGroups.model.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages player data including mute preferences and cooldowns.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class PlayerDataManager {
    
    private final TalkGroups plugin;
    private final DatabaseManager database;
    private final Map<UUID, PlayerData> playerDataCache;
    
    /**
     * Creates a new PlayerDataManager instance.
     * 
     * @param plugin The plugin instance
     * @param database The database manager
     */
    public PlayerDataManager(TalkGroups plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
        this.playerDataCache = new HashMap<>();
    }
    
    /**
     * Loads player data from the database.
     * 
     * @param player The player
     * @return CompletableFuture that completes when data is loaded
     */
    public CompletableFuture<PlayerData> loadPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Check cache first
        if (playerDataCache.containsKey(playerId)) {
            return CompletableFuture.completedFuture(playerDataCache.get(playerId));
        }
        
        // Load from database
        return database.loadMutedChannels(playerId).thenApply(mutedChannels -> {
            PlayerData data = new PlayerData(playerId);
            
            // Restore muted channels
            for (String channelId : mutedChannels) {
                data.muteChannel(channelId);
            }
            
            // Cache the data
            playerDataCache.put(playerId, data);
            
            return data;
        });
    }
    
    /**
     * Gets player data from cache or creates new instance.
     * 
     * @param playerId The player's UUID
     * @return The PlayerData instance
     */
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataCache.computeIfAbsent(playerId, PlayerData::new);
    }
    
    /**
     * Gets player data from cache or creates new instance.
     * 
     * @param player The player
     * @return The PlayerData instance
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    /**
     * Saves player data to the database.
     * 
     * @param playerId The player's UUID
     * @return CompletableFuture that completes when data is saved
     */
    public CompletableFuture<Void> savePlayerData(UUID playerId) {
        PlayerData data = playerDataCache.get(playerId);
        if (data == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        // Clear existing muted channels in database
        return database.clearMutedChannels(playerId).thenCompose(v -> {
            // Save all currently muted channels
            CompletableFuture<?>[] futures = data.getMutedChannels().stream()
                    .map(channelId -> database.saveMutedChannel(playerId, channelId))
                    .toArray(CompletableFuture[]::new);
            
            return CompletableFuture.allOf(futures);
        });
    }
    
    /**
     * Saves player data to the database.
     * 
     * @param player The player
     * @return CompletableFuture that completes when data is saved
     */
    public CompletableFuture<Void> savePlayerData(Player player) {
        return savePlayerData(player.getUniqueId());
    }
    
    /**
     * Unloads player data from cache and saves to database.
     * 
     * @param playerId The player's UUID
     * @return CompletableFuture that completes when data is saved and unloaded
     */
    public CompletableFuture<Void> unloadPlayerData(UUID playerId) {
        return savePlayerData(playerId).thenRun(() -> {
            playerDataCache.remove(playerId);
        });
    }
    
    /**
     * Unloads player data from cache and saves to database.
     * 
     * @param player The player
     * @return CompletableFuture that completes when data is saved and unloaded
     */
    public CompletableFuture<Void> unloadPlayerData(Player player) {
        return unloadPlayerData(player.getUniqueId());
    }
    
    /**
     * Mutes a channel for a player and saves to database.
     * 
     * @param player The player
     * @param channelId The channel ID to mute
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> muteChannel(Player player, String channelId) {
        PlayerData data = getPlayerData(player);
        data.muteChannel(channelId);
        return database.saveMutedChannel(player.getUniqueId(), channelId);
    }
    
    /**
     * Unmutes a channel for a player and updates database.
     * 
     * @param player The player
     * @param channelId The channel ID to unmute
     * @return CompletableFuture that completes when the operation is done
     */
    public CompletableFuture<Void> unmuteChannel(Player player, String channelId) {
        PlayerData data = getPlayerData(player);
        data.unmuteChannel(channelId);
        return database.removeMutedChannel(player.getUniqueId(), channelId);
    }
    
    /**
     * Toggles mute status for a channel and updates database.
     * 
     * @param player The player
     * @param channelId The channel ID to toggle
     * @return CompletableFuture containing true if now muted, false if now unmuted
     */
    public CompletableFuture<Boolean> toggleMute(Player player, String channelId) {
        PlayerData data = getPlayerData(player);
        boolean nowMuted = data.toggleMute(channelId);
        
        if (nowMuted) {
            return database.saveMutedChannel(player.getUniqueId(), channelId)
                    .thenApply(v -> true);
        } else {
            return database.removeMutedChannel(player.getUniqueId(), channelId)
                    .thenApply(v -> false);
        }
    }
    
    /**
     * Saves all cached player data to the database.
     * 
     * @return CompletableFuture that completes when all data is saved
     */
    public CompletableFuture<Void> saveAll() {
        CompletableFuture<?>[] futures = playerDataCache.keySet().stream()
                .map(this::savePlayerData)
                .toArray(CompletableFuture[]::new);
        
        return CompletableFuture.allOf(futures);
    }
    
    /**
     * Clears all cached player data.
     */
    public void clearCache() {
        playerDataCache.clear();
    }
}

