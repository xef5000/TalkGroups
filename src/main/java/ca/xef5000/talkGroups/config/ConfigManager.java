package ca.xef5000.talkGroups.config;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.model.TalkGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages plugin configuration and TalkGroup definitions.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class ConfigManager {
    
    private final TalkGroups plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final Map<String, TalkGroup> talkGroups;
    private final Map<String, String> aliasToId;
    
    /**
     * Creates a new ConfigManager instance.
     * 
     * @param plugin The plugin instance
     */
    public ConfigManager(TalkGroups plugin) {
        this.plugin = plugin;
        this.talkGroups = new HashMap<>();
        this.aliasToId = new HashMap<>();
    }
    
    /**
     * Loads all configuration files.
     */
    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load messages.yml
        loadMessages();
        
        // Load TalkGroups
        loadTalkGroups();
    }
    
    /**
     * Reloads all configuration files.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadMessages();
        
        // Clear existing TalkGroups
        talkGroups.clear();
        aliasToId.clear();
        
        // Reload TalkGroups
        loadTalkGroups();
    }
    
    /**
     * Loads the messages.yml file.
     */
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    /**
     * Loads TalkGroups from the configuration.
     */
    private void loadTalkGroups() {
        ConfigurationSection groupsSection = config.getConfigurationSection("talkgroups");
        if (groupsSection == null) {
            plugin.getLogger().warning("No talkgroups section found in config.yml!");
            return;
        }
        
        for (String groupId : groupsSection.getKeys(false)) {
            try {
                TalkGroup group = loadTalkGroup(groupId, groupsSection.getConfigurationSection(groupId));
                talkGroups.put(groupId, group);
                aliasToId.put(group.getAlias().toLowerCase(), groupId);
                plugin.getLogger().info("Loaded TalkGroup: " + groupId + " (/" + group.getAlias() + ")");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load TalkGroup: " + groupId, e);
            }
        }
        
        plugin.getLogger().info("Loaded " + talkGroups.size() + " TalkGroup(s)");
    }
    
    /**
     * Loads a single TalkGroup from configuration.
     * 
     * @param id The TalkGroup ID
     * @param section The configuration section
     * @return The loaded TalkGroup
     */
    private TalkGroup loadTalkGroup(String id, ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("Configuration section is null for TalkGroup: " + id);
        }
        
        TalkGroup.Builder builder = new TalkGroup.Builder()
                .id(id)
                .name(section.getString("name", id))
                .permission(section.getString("permission", "talkgroups." + id))
                .cooldown(section.getInt("cooldown", 0))
                .silencable(section.getBoolean("silencable", true))
                .notify(section.getBoolean("notify", false))
                .notifyDelay(section.getInt("notify-delay", 60))
                .alias(section.getString("alias", id))
                .suffix(section.getString("suffix", ""));
        
        // Handle prefix - default to name if not specified
        if (section.contains("prefix")) {
            builder.prefix(section.getString("prefix"));
        } else {
            builder.prefix(section.getString("name", id));
        }
        
        return builder.build();
    }
    
    /**
     * Gets a TalkGroup by its ID.
     * 
     * @param id The TalkGroup ID
     * @return The TalkGroup, or null if not found
     */
    public TalkGroup getTalkGroup(String id) {
        return talkGroups.get(id);
    }
    
    /**
     * Gets a TalkGroup by its alias.
     * 
     * @param alias The command alias
     * @return The TalkGroup, or null if not found
     */
    public TalkGroup getTalkGroupByAlias(String alias) {
        String id = aliasToId.get(alias.toLowerCase());
        return id != null ? talkGroups.get(id) : null;
    }
    
    /**
     * Gets all loaded TalkGroups.
     * 
     * @return Map of TalkGroup ID to TalkGroup
     */
    public Map<String, TalkGroup> getAllTalkGroups() {
        return new HashMap<>(talkGroups);
    }
    
    /**
     * Gets a message from messages.yml.
     * 
     * @param key The message key
     * @return The message, or the key if not found
     */
    public String getMessage(String key) {
        return messages.getString(key, key);
    }
    
    /**
     * Gets a message from messages.yml with placeholder replacement.
     * 
     * @param key The message key
     * @param placeholders Placeholder replacements (key-value pairs)
     * @return The formatted message
     */
    public String getMessage(String key, Object... placeholders) {
        String message = getMessage(key);
        
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String placeholder = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            message = message.replace("{" + placeholder + "}", value);
        }
        
        return message;
    }
    
    /**
     * Gets the main configuration.
     * 
     * @return The FileConfiguration
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Gets the messages configuration.
     * 
     * @return The messages FileConfiguration
     */
    public FileConfiguration getMessages() {
        return messages;
    }
    
    /**
     * Saves the messages configuration.
     */
    public void saveMessages() {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save messages.yml", e);
        }
    }
}

