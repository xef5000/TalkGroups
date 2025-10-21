package ca.xef5000.talkGroups.model;

import org.bukkit.ChatColor;

/**
 * Represents a custom chat channel (TalkGroup) with configurable properties.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class TalkGroup {
    
    private final String id;
    private final String name;
    private final String permission;
    private final int cooldown;
    private final boolean silencable;
    private final boolean notify;
    private final int notifyDelay;
    private final String alias;
    private final String prefix;
    private final String suffix;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private TalkGroup(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.permission = builder.permission;
        this.cooldown = builder.cooldown;
        this.silencable = builder.silencable;
        this.notify = builder.notify;
        this.notifyDelay = builder.notifyDelay;
        this.alias = builder.alias;
        this.prefix = builder.prefix;
        this.suffix = builder.suffix;
    }
    
    /**
     * Gets the unique identifier for this TalkGroup.
     * 
     * @return The TalkGroup ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the display name of this TalkGroup.
     * 
     * @return The TalkGroup name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the formatted name with color codes translated.
     * 
     * @return The formatted name
     */
    public String getFormattedName() {
        return ChatColor.translateAlternateColorCodes('&', name);
    }
    
    /**
     * Gets the permission node required to access this TalkGroup.
     * 
     * @return The permission node
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Gets the cooldown in seconds between messages.
     * 
     * @return The cooldown in seconds
     */
    public int getCooldown() {
        return cooldown;
    }
    
    /**
     * Checks if this TalkGroup can be silenced by players.
     * 
     * @return true if silencable, false otherwise
     */
    public boolean isSilencable() {
        return silencable;
    }
    
    /**
     * Checks if notifications should be sent when this channel is muted.
     * 
     * @return true if notifications enabled, false otherwise
     */
    public boolean isNotify() {
        return notify;
    }
    
    /**
     * Gets the delay in seconds between notifications.
     * 
     * @return The notification delay in seconds
     */
    public int getNotifyDelay() {
        return notifyDelay;
    }
    
    /**
     * Gets the command alias for quick messaging.
     * 
     * @return The command alias
     */
    public String getAlias() {
        return alias;
    }
    
    /**
     * Gets the chat prefix for this TalkGroup.
     * 
     * @return The prefix
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * Gets the formatted prefix with color codes translated.
     * 
     * @return The formatted prefix
     */
    public String getFormattedPrefix() {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }
    
    /**
     * Gets the chat suffix for this TalkGroup.
     * 
     * @return The suffix
     */
    public String getSuffix() {
        return suffix;
    }
    
    /**
     * Gets the formatted suffix with color codes translated.
     * 
     * @return The formatted suffix
     */
    public String getFormattedSuffix() {
        return ChatColor.translateAlternateColorCodes('&', suffix);
    }
    
    /**
     * Builder class for creating TalkGroup instances.
     */
    public static class Builder {
        private String id;
        private String name;
        private String permission;
        private int cooldown = 0;
        private boolean silencable = true;
        private boolean notify = false;
        private int notifyDelay = 60;
        private String alias;
        private String prefix;
        private String suffix = "";
        
        /**
         * Sets the TalkGroup ID.
         * 
         * @param id The unique identifier
         * @return This builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        /**
         * Sets the TalkGroup name.
         * 
         * @param name The display name
         * @return This builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        /**
         * Sets the permission node.
         * 
         * @param permission The permission required
         * @return This builder
         */
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        /**
         * Sets the cooldown in seconds.
         * 
         * @param cooldown The cooldown duration
         * @return This builder
         */
        public Builder cooldown(int cooldown) {
            this.cooldown = cooldown;
            return this;
        }
        
        /**
         * Sets whether this TalkGroup is silencable.
         * 
         * @param silencable true if silencable
         * @return This builder
         */
        public Builder silencable(boolean silencable) {
            this.silencable = silencable;
            return this;
        }
        
        /**
         * Sets whether notifications are enabled.
         * 
         * @param notify true to enable notifications
         * @return This builder
         */
        public Builder notify(boolean notify) {
            this.notify = notify;
            return this;
        }
        
        /**
         * Sets the notification delay in seconds.
         * 
         * @param notifyDelay The delay between notifications
         * @return This builder
         */
        public Builder notifyDelay(int notifyDelay) {
            this.notifyDelay = notifyDelay;
            return this;
        }
        
        /**
         * Sets the command alias.
         * 
         * @param alias The command alias
         * @return This builder
         */
        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }
        
        /**
         * Sets the chat prefix.
         * 
         * @param prefix The prefix
         * @return This builder
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }
        
        /**
         * Sets the chat suffix.
         * 
         * @param suffix The suffix
         * @return This builder
         */
        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }
        
        /**
         * Builds the TalkGroup instance.
         * 
         * @return A new TalkGroup
         * @throws IllegalStateException if required fields are missing
         */
        public TalkGroup build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("TalkGroup ID cannot be null or empty");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("TalkGroup name cannot be null or empty");
            }
            if (permission == null || permission.isEmpty()) {
                throw new IllegalStateException("TalkGroup permission cannot be null or empty");
            }
            if (alias == null || alias.isEmpty()) {
                throw new IllegalStateException("TalkGroup alias cannot be null or empty");
            }
            
            // Set default prefix to name if not specified
            if (prefix == null) {
                prefix = name;
            }
            
            return new TalkGroup(this);
        }
    }
}

