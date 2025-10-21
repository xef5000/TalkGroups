package ca.xef5000.talkGroups.gui;

import ca.xef5000.talkGroups.TalkGroups;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages GUI instances and handles inventory events.
 * Uses UUID tracking for robust GUI identification.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class GUIManager implements Listener {
    
    private final TalkGroups plugin;
    private final Map<UUID, TalkGroupGUI> activeGUIs;
    
    /**
     * Creates a new GUIManager instance.
     * 
     * @param plugin The plugin instance
     */
    public GUIManager(TalkGroups plugin) {
        this.plugin = plugin;
        this.activeGUIs = new HashMap<>();
    }
    
    /**
     * Opens the TalkGroups GUI for a player.
     * 
     * @param player The player to show the GUI to
     */
    public void openTalkGroupGUI(Player player) {
        TalkGroupGUI gui = new TalkGroupGUI(plugin, player);
        activeGUIs.put(player.getUniqueId(), gui);
        gui.open();
    }
    
    /**
     * Checks if a player has an active GUI.
     * 
     * @param player The player to check
     * @return true if the player has an active GUI, false otherwise
     */
    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets the active GUI for a player.
     * 
     * @param player The player
     * @return The TalkGroupGUI, or null if none exists
     */
    public TalkGroupGUI getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }
    
    /**
     * Removes the active GUI for a player.
     * 
     * @param player The player
     */
    public void removeActiveGUI(Player player) {
        activeGUIs.remove(player.getUniqueId());
    }
    
    /**
     * Handles inventory click events.
     * 
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        TalkGroupGUI gui = activeGUIs.get(player.getUniqueId());
        
        if (gui != null && gui.isThisInventory(event.getInventory())) {
            event.setCancelled(true);
            gui.handleClick(event);
        }
    }
    
    /**
     * Handles inventory close events.
     * 
     * @param event The InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        TalkGroupGUI gui = activeGUIs.get(player.getUniqueId());
        
        if (gui != null && gui.isThisInventory(event.getInventory())) {
            activeGUIs.remove(player.getUniqueId());
        }
    }
    
    /**
     * Closes all active GUIs.
     */
    public void closeAll() {
        for (TalkGroupGUI gui : activeGUIs.values()) {
            gui.close();
        }
        activeGUIs.clear();
    }
}

