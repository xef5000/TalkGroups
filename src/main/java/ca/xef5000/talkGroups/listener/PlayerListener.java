package ca.xef5000.talkGroups.listener;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for data management.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class PlayerListener implements Listener {
    
    private final TalkGroups plugin;
    
    /**
     * Creates a new PlayerListener instance.
     * 
     * @param plugin The plugin instance
     */
    public PlayerListener(TalkGroups plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player join events.
     * 
     * @param event The PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        dataManager.loadPlayerData(event.getPlayer());
    }
    
    /**
     * Handles player quit events.
     * 
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        dataManager.unloadPlayerData(event.getPlayer());
    }
}

