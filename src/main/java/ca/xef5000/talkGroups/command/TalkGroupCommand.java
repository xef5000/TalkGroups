package ca.xef5000.talkGroups.command;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.config.ConfigManager;
import ca.xef5000.talkGroups.gui.GUIManager;
import ca.xef5000.talkGroups.manager.PlayerDataManager;
import ca.xef5000.talkGroups.model.PlayerData;
import ca.xef5000.talkGroups.model.TalkGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main command handler for /talkgroups and /tg.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class TalkGroupCommand implements CommandExecutor, TabCompleter {
    
    private final TalkGroups plugin;
    
    /**
     * Creates a new TalkGroupCommand instance.
     * 
     * @param plugin The plugin instance
     */
    public TalkGroupCommand(TalkGroups plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        ConfigManager configManager = plugin.getConfigManager();
        
        // No arguments - open GUI
        if (args.length == 0) {
            GUIManager guiManager = plugin.getGUIManager();
            guiManager.openTalkGroupGUI(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "mute":
                return handleMute(player, args);
            
            case "unmute":
                return handleUnmute(player, args);
            
            case "toggle":
                return handleToggle(player, args);
            
            case "reload":
                return handleReload(player);
            
            case "list":
                return handleList(player);
            
            case "help":
                return handleHelp(player);
            
            default:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        configManager.getMessage("command.unknown-subcommand")));
                return true;
        }
    }
    
    /**
     * Handles the mute subcommand.
     */
    private boolean handleMute(Player player, String[] args) {
        ConfigManager configManager = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.mute.usage")));
            return true;
        }
        
        String channelId = args[1];
        TalkGroup group = configManager.getTalkGroup(channelId);
        
        if (group == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.channel-not-found", "channel", channelId)));
            return true;
        }
        
        if (!player.hasPermission(group.getPermission())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.no-permission")));
            return true;
        }
        
        if (!group.isSilencable()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.not-silencable", "channel", group.getFormattedName())));
            return true;
        }
        
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        dataManager.muteChannel(player, channelId).thenRun(() -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("channel.muted", "channel", group.getFormattedName())));
        });
        
        return true;
    }
    
    /**
     * Handles the unmute subcommand.
     */
    private boolean handleUnmute(Player player, String[] args) {
        ConfigManager configManager = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.unmute.usage")));
            return true;
        }
        
        String channelId = args[1];
        TalkGroup group = configManager.getTalkGroup(channelId);
        
        if (group == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.channel-not-found", "channel", channelId)));
            return true;
        }
        
        if (!player.hasPermission(group.getPermission())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.no-permission")));
            return true;
        }
        
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        dataManager.unmuteChannel(player, channelId).thenRun(() -> {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("channel.unmuted", "channel", group.getFormattedName())));
        });
        
        return true;
    }
    
    /**
     * Handles the toggle subcommand.
     */
    private boolean handleToggle(Player player, String[] args) {
        ConfigManager configManager = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.toggle.usage")));
            return true;
        }
        
        String channelId = args[1];
        TalkGroup group = configManager.getTalkGroup(channelId);
        
        if (group == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.channel-not-found", "channel", channelId)));
            return true;
        }
        
        if (!player.hasPermission(group.getPermission())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.no-permission")));
            return true;
        }
        
        if (!group.isSilencable()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.not-silencable", "channel", group.getFormattedName())));
            return true;
        }
        
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        dataManager.toggleMute(player, channelId).thenAccept(nowMuted -> {
            String message = nowMuted ?
                    configManager.getMessage("channel.muted", "channel", group.getFormattedName()) :
                    configManager.getMessage("channel.unmuted", "channel", group.getFormattedName());
            
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        });
        
        return true;
    }
    
    /**
     * Handles the reload subcommand.
     */
    private boolean handleReload(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        
        if (!player.hasPermission("talkgroups.admin.reload")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    configManager.getMessage("command.no-permission")));
            return true;
        }
        
        configManager.reloadConfig();
        plugin.reregisterCommands();
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("command.reload.success")));
        
        return true;
    }
    
    /**
     * Handles the list subcommand.
     */
    private boolean handleList(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        PlayerData playerData = dataManager.getPlayerData(player);
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("command.list.header")));
        
        for (TalkGroup group : configManager.getAllTalkGroups().values()) {
            if (player.hasPermission(group.getPermission())) {
                boolean isMuted = playerData.isChannelMuted(group.getId());
                String status = isMuted ? ChatColor.RED + "Muted" : ChatColor.GREEN + "Unmuted";
                
                player.sendMessage(ChatColor.GRAY + "- " + group.getFormattedName() + 
                        ChatColor.GRAY + " (/" + group.getAlias() + ") - " + status);
            }
        }
        
        return true;
    }
    
    /**
     * Handles the help subcommand.
     */
    private boolean handleHelp(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                configManager.getMessage("command.help.header")));
        player.sendMessage(ChatColor.YELLOW + "/tg" + ChatColor.GRAY + " - Open TalkGroups GUI");
        player.sendMessage(ChatColor.YELLOW + "/tg mute <channel>" + ChatColor.GRAY + " - Mute a channel");
        player.sendMessage(ChatColor.YELLOW + "/tg unmute <channel>" + ChatColor.GRAY + " - Unmute a channel");
        player.sendMessage(ChatColor.YELLOW + "/tg toggle <channel>" + ChatColor.GRAY + " - Toggle channel mute");
        player.sendMessage(ChatColor.YELLOW + "/tg list" + ChatColor.GRAY + " - List all channels");
        
        if (player.hasPermission("talkgroups.admin.reload")) {
            player.sendMessage(ChatColor.YELLOW + "/tg reload" + ChatColor.GRAY + " - Reload configuration");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("mute", "unmute", "toggle", "list", "help"));
            if (sender.hasPermission("talkgroups.admin.reload")) {
                completions.add("reload");
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && (args[0].equalsIgnoreCase("mute") || 
                args[0].equalsIgnoreCase("unmute") || 
                args[0].equalsIgnoreCase("toggle"))) {
            ConfigManager configManager = plugin.getConfigManager();
            return configManager.getAllTalkGroups().keySet().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}

