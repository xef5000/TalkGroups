package ca.xef5000.talkGroups.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents player-specific data including muted channels and cooldowns.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class PlayerData {
    
    private final UUID playerId;
    private final Set<String> mutedChannels;
    private final Map<String, Long> cooldowns;
    private final Map<String, Integer> missedMessages;
    private final Map<String, Long> lastNotification;
    
    /**
     * Creates a new PlayerData instance.
     * 
     * @param playerId The player's UUID
     */
    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.mutedChannels = new HashSet<>();
        this.cooldowns = new HashMap<>();
        this.missedMessages = new HashMap<>();
        this.lastNotification = new HashMap<>();
    }
    
    /**
     * Gets the player's UUID.
     * 
     * @return The player UUID
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the set of muted channel IDs.
     * 
     * @return Set of muted channel IDs
     */
    public Set<String> getMutedChannels() {
        return new HashSet<>(mutedChannels);
    }
    
    /**
     * Checks if a channel is muted for this player.
     * 
     * @param channelId The channel ID to check
     * @return true if muted, false otherwise
     */
    public boolean isChannelMuted(String channelId) {
        return mutedChannels.contains(channelId);
    }
    
    /**
     * Mutes a channel for this player.
     * 
     * @param channelId The channel ID to mute
     */
    public void muteChannel(String channelId) {
        mutedChannels.add(channelId);
    }
    
    /**
     * Unmutes a channel for this player.
     * 
     * @param channelId The channel ID to unmute
     */
    public void unmuteChannel(String channelId) {
        mutedChannels.remove(channelId);
        missedMessages.remove(channelId);
        lastNotification.remove(channelId);
    }
    
    /**
     * Toggles the mute status of a channel.
     * 
     * @param channelId The channel ID to toggle
     * @return true if now muted, false if now unmuted
     */
    public boolean toggleMute(String channelId) {
        if (isChannelMuted(channelId)) {
            unmuteChannel(channelId);
            return false;
        } else {
            muteChannel(channelId);
            return true;
        }
    }
    
    /**
     * Checks if a player is on cooldown for a specific channel.
     * 
     * @param channelId The channel ID to check
     * @return true if on cooldown, false otherwise
     */
    public boolean isOnCooldown(String channelId) {
        Long cooldownEnd = cooldowns.get(channelId);
        if (cooldownEnd == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * Gets the remaining cooldown time in seconds.
     * 
     * @param channelId The channel ID to check
     * @return Remaining cooldown in seconds, or 0 if not on cooldown
     */
    public int getRemainingCooldown(String channelId) {
        Long cooldownEnd = cooldowns.get(channelId);
        if (cooldownEnd == null) {
            return 0;
        }
        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }
    
    /**
     * Sets a cooldown for a specific channel.
     * 
     * @param channelId The channel ID
     * @param seconds The cooldown duration in seconds
     */
    public void setCooldown(String channelId, int seconds) {
        if (seconds > 0) {
            cooldowns.put(channelId, System.currentTimeMillis() + (seconds * 1000L));
        }
    }
    
    /**
     * Clears the cooldown for a specific channel.
     * 
     * @param channelId The channel ID
     */
    public void clearCooldown(String channelId) {
        cooldowns.remove(channelId);
    }
    
    /**
     * Increments the missed message count for a channel.
     * 
     * @param channelId The channel ID
     */
    public void incrementMissedMessages(String channelId) {
        missedMessages.put(channelId, missedMessages.getOrDefault(channelId, 0) + 1);
    }
    
    /**
     * Gets the number of missed messages for a channel.
     * 
     * @param channelId The channel ID
     * @return The number of missed messages
     */
    public int getMissedMessages(String channelId) {
        return missedMessages.getOrDefault(channelId, 0);
    }
    
    /**
     * Resets the missed message count for a channel.
     * 
     * @param channelId The channel ID
     */
    public void resetMissedMessages(String channelId) {
        missedMessages.remove(channelId);
    }
    
    /**
     * Checks if a notification should be sent for a muted channel.
     * 
     * @param channelId The channel ID
     * @param notifyDelay The notification delay in seconds
     * @return true if notification should be sent, false otherwise
     */
    public boolean shouldNotify(String channelId, int notifyDelay) {
        Long lastNotify = lastNotification.get(channelId);
        if (lastNotify == null) {
            return true;
        }
        long timeSinceLastNotify = System.currentTimeMillis() - lastNotify;
        return timeSinceLastNotify >= (notifyDelay * 1000L);
    }
    
    /**
     * Updates the last notification time for a channel.
     * 
     * @param channelId The channel ID
     */
    public void updateLastNotification(String channelId) {
        lastNotification.put(channelId, System.currentTimeMillis());
    }
    
    /**
     * Gets the time since the last notification in seconds.
     * 
     * @param channelId The channel ID
     * @return Time since last notification in seconds, or 0 if never notified
     */
    public int getTimeSinceLastNotification(String channelId) {
        Long lastNotify = lastNotification.get(channelId);
        if (lastNotify == null) {
            return 0;
        }
        long timeSince = System.currentTimeMillis() - lastNotify;
        return (int) (timeSince / 1000);
    }
}

