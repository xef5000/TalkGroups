package ca.xef5000.talkGroups.command;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.config.ConfigManager;
import ca.xef5000.talkGroups.manager.PlayerDataManager;
import ca.xef5000.talkGroups.model.PlayerData;
import ca.xef5000.talkGroups.model.TalkGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles alias commands for quick messaging to TalkGroups.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class AliasCommand implements CommandExecutor {
    
    private final TalkGroups plugin;
    private final String alias;
    
    /**
     * Creates a new AliasCommand instance.
     * 
     * @param plugin The plugin instance
     * @param alias The command alias
     */
    public AliasCommand(TalkGroups plugin, String alias) {
        this.plugin = plugin;
        this.alias = alias;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        ConfigManager configManager = plugin.getConfigManager();
        
        // Get the TalkGroup for this alias
        TalkGroup group = configManager.getTalkGroupByAlias(alias);
        
        if (group == null) {
            player.sendMessage(ChatColor.RED + "This TalkGroup no longer exists.");
            return true;
        }
        
        // Check permission
        if (!player.hasPermission(group.getPermission())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.no-permission")));
            return true;
        }
        
        // Check if message was provided
        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.alias.usage", "alias", alias)));
            return true;
        }
        
        // Check cooldown
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        PlayerData playerData = dataManager.getPlayerData(player);
        
        if (!player.hasPermission("talkgroups.bypass.cooldown") && 
                playerData.isOnCooldown(group.getId())) {
            int remaining = playerData.getRemainingCooldown(group.getId());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.cooldown", 
                            "seconds", String.valueOf(remaining))));
            return true;
        }
        
        // Build the message
        StringBuilder messageBuilder = new StringBuilder();
        for (String arg : args) {
            messageBuilder.append(arg).append(" ");
        }
        String message = messageBuilder.toString().trim();
        
        // Send the message to all players with permission
        sendToChannel(group, player, message);
        
        // Set cooldown
        if (group.getCooldown() > 0 && !player.hasPermission("talkgroups.bypass.cooldown")) {
            playerData.setCooldown(group.getId(), group.getCooldown());
        }
        
        return true;
    }
    
    /**
     * Sends a message to all players in the channel.
     * 
     * @param group The TalkGroup
     * @param sender The player sending the message
     * @param message The message content
     */
    private void sendToChannel(TalkGroup group, Player sender, String message) {
        ConfigManager configManager = plugin.getConfigManager();
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        
        // Format the message
        String formattedMessage = formatMessage(group, sender, message);
        
        // Send to all players with permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(group.getPermission())) {
                PlayerData playerData = dataManager.getPlayerData(player);
                
                // Check if player has muted this channel
                if (playerData.isChannelMuted(group.getId())) {
                    // Increment missed messages
                    playerData.incrementMissedMessages(group.getId());
                    
                    // Send notification if enabled
                    if (group.isNotify() && playerData.shouldNotify(group.getId(), group.getNotifyDelay())) {
                        int missedCount = playerData.getMissedMessages(group.getId());
                        int timeSince = playerData.getTimeSinceLastNotification(group.getId());
                        
                        String notifyMessage = configManager.getMessage("channel.notification",
                                "count", String.valueOf(missedCount),
                                "channel", group.getFormattedName(),
                                "time", formatTime(timeSince));
                        
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', notifyMessage));
                        playerData.updateLastNotification(group.getId());
                    }
                } else {
                    // Send the message
                    player.sendMessage(formattedMessage);
                }
            }
        }
        
        // Log to console
        plugin.getLogger().info("[" + group.getId() + "] " + sender.getName() + ": " + message);
    }
    
    /**
     * Formats a message for the channel.
     * 
     * @param group The TalkGroup
     * @param sender The player sending the message
     * @param message The message content
     * @return The formatted message
     */
    private String formatMessage(TalkGroup group, Player sender, String message) {
        String format = plugin.getConfigManager().getMessage("channel.format");
        
        format = format.replace("{prefix}", group.getFormattedPrefix());
        format = format.replace("{suffix}", group.getFormattedSuffix());
        format = format.replace("{player}", sender.getDisplayName());
        format = format.replace("{message}", message);
        
        return ChatColor.translateAlternateColorCodes('&', format);
    }
    
    /**
     * Formats time in seconds to a readable string.
     * 
     * @param seconds The time in seconds
     * @return The formatted time string
     */
    private String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        } else {
            int minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        }
    }
}

