package ca.xef5000.talkGroups;

import ca.xef5000.talkGroups.command.AliasCommand;
import ca.xef5000.talkGroups.command.TalkGroupCommand;
import ca.xef5000.talkGroups.config.ConfigManager;
import ca.xef5000.talkGroups.database.DatabaseManager;
import ca.xef5000.talkGroups.gui.GUIManager;
import ca.xef5000.talkGroups.listener.PlayerListener;
import ca.xef5000.talkGroups.manager.PlayerDataManager;
import ca.xef5000.talkGroups.model.TalkGroup;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for TalkGroups.
 * Manages custom chat channels with configurable properties.
 *
 * @author xef5000
 * @version 1.0.0
 */
public final class TalkGroups extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        getLogger().info("Enabling TalkGroups v" + getDescription().getVersion());

        try {
            // Initialize configuration
            configManager = new ConfigManager(this);
            configManager.loadConfig();

            // Initialize database
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();

            // Initialize managers
            playerDataManager = new PlayerDataManager(this, databaseManager);
            guiManager = new GUIManager(this);

            // Register commands
            registerCommands();

            // Register listeners
            registerListeners();

            // Load data for online players (in case of reload)
            Bukkit.getOnlinePlayers().forEach(playerDataManager::loadPlayerData);

            getLogger().info("TalkGroups enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable TalkGroups", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TalkGroups...");

        try {
            // Close all GUIs
            if (guiManager != null) {
                guiManager.closeAll();
            }

            // Save all player data
            if (playerDataManager != null) {
                playerDataManager.saveAll().join();
                playerDataManager.clearCache();
            }

            // Close database connection
            if (databaseManager != null) {
                databaseManager.close();
            }

            getLogger().info("TalkGroups disabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        }
    }

    /**
     * Registers all commands.
     */
    private void registerCommands() {
        // Register main command
        TalkGroupCommand mainCommand = new TalkGroupCommand(this);
        PluginCommand tgCommand = getCommand("talkgroups");
        if (tgCommand != null) {
            tgCommand.setExecutor(mainCommand);
            tgCommand.setTabCompleter(mainCommand);
        }

        // Register alias commands
        registerAliasCommands();
    }

    /**
     * Registers alias commands for all TalkGroups.
     */
    private void registerAliasCommands() {
        for (TalkGroup group : configManager.getAllTalkGroups().values()) {
            String alias = group.getAlias();

            try {
                // Register command dynamically
                org.bukkit.command.CommandMap commandMap = getServer().getCommandMap();
                AliasCommand aliasCommand = new AliasCommand(this, alias);

                org.bukkit.command.Command command = new org.bukkit.command.Command(alias) {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender,
                                         String commandLabel,
                                         String[] args) {
                        return aliasCommand.onCommand(sender, this, commandLabel, args);
                    }
                };

                command.setDescription("Send a message to the " + group.getName() + " channel");
                command.setUsage("/<command> <message>");
                command.setPermission(group.getPermission());

                commandMap.register("talkgroups", command);

                getLogger().info("Registered alias command: /" + alias);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to register alias command: /" + alias, e);
            }
        }
    }

    /**
     * Re-registers all commands (used after config reload).
     */
    public void reregisterCommands() {
        registerAliasCommands();
    }

    /**
     * Registers all event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
    }

    /**
     * Gets the ConfigManager instance.
     *
     * @return The ConfigManager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the DatabaseManager instance.
     *
     * @return The DatabaseManager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the PlayerDataManager instance.
     *
     * @return The PlayerDataManager
     */
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * Gets the GUIManager instance.
     *
     * @return The GUIManager
     */
    public GUIManager getGUIManager() {
        return guiManager;
    }
}
