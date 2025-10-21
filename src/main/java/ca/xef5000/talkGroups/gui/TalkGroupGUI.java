package ca.xef5000.talkGroups.gui;

import ca.xef5000.talkGroups.TalkGroups;
import ca.xef5000.talkGroups.config.ConfigManager;
import ca.xef5000.talkGroups.manager.PlayerDataManager;
import ca.xef5000.talkGroups.model.PlayerData;
import ca.xef5000.talkGroups.model.TalkGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI for managing TalkGroup mute preferences.
 * Uses InventoryHolder pattern for robust identification.
 * 
 * @author TalkGroups
 * @version 1.0.0
 */
public class TalkGroupGUI implements InventoryHolder {
    
    private final TalkGroups plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, String> slotToChannelId;
    private int currentPage;
    private final List<TalkGroup> accessibleGroups;
    
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of 9
    private static final int PREVIOUS_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;
    private static final int CLOSE_SLOT = 49;
    
    /**
     * Creates a new TalkGroupGUI instance.
     * 
     * @param plugin The plugin instance
     * @param player The player viewing the GUI
     */
    public TalkGroupGUI(TalkGroups plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.slotToChannelId = new HashMap<>();
        this.currentPage = 0;
        this.accessibleGroups = new ArrayList<>();
        
        // Get all TalkGroups the player has access to
        ConfigManager configManager = plugin.getConfigManager();
        for (TalkGroup group : configManager.getAllTalkGroups().values()) {
            if (player.hasPermission(group.getPermission())) {
                accessibleGroups.add(group);
            }
        }
        
        // Sort by name for consistent display
        accessibleGroups.sort(Comparator.comparing(TalkGroup::getName));
        
        // Create inventory
        String title = ChatColor.translateAlternateColorCodes('&', 
                configManager.getMessage("gui.title"));
        this.inventory = Bukkit.createInventory(this, 54, title);
        
        // Populate inventory
        populateInventory();
    }
    
    /**
     * Populates the inventory with items.
     */
    private void populateInventory() {
        inventory.clear();
        slotToChannelId.clear();
        
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        PlayerData playerData = dataManager.getPlayerData(player);
        ConfigManager configManager = plugin.getConfigManager();
        
        // Calculate pagination
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, accessibleGroups.size());
        
        // Add TalkGroup items
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            TalkGroup group = accessibleGroups.get(i);
            boolean isMuted = playerData.isChannelMuted(group.getId());
            
            ItemStack item = createTalkGroupItem(group, isMuted);
            inventory.setItem(slot, item);
            slotToChannelId.put(slot, group.getId());
            slot++;
        }
        
        // Add navigation items
        int totalPages = (int) Math.ceil((double) accessibleGroups.size() / ITEMS_PER_PAGE);
        
        if (currentPage > 0) {
            inventory.setItem(PREVIOUS_PAGE_SLOT, createNavigationItem(
                    Material.ARROW,
                    configManager.getMessage("gui.previous-page"),
                    Collections.singletonList(ChatColor.GRAY + "Page " + currentPage + "/" + totalPages)
            ));
        }
        
        if (currentPage < totalPages - 1) {
            inventory.setItem(NEXT_PAGE_SLOT, createNavigationItem(
                    Material.ARROW,
                    configManager.getMessage("gui.next-page"),
                    Collections.singletonList(ChatColor.GRAY + "Page " + (currentPage + 2) + "/" + totalPages)
            ));
        }
        
        inventory.setItem(CLOSE_SLOT, createNavigationItem(
                Material.BARRIER,
                configManager.getMessage("gui.close"),
                Collections.emptyList()
        ));
    }
    
    /**
     * Creates an ItemStack for a TalkGroup.
     * 
     * @param group The TalkGroup
     * @param isMuted Whether the channel is muted
     * @return The ItemStack
     */
    private ItemStack createTalkGroupItem(TalkGroup group, boolean isMuted) {
        ConfigManager configManager = plugin.getConfigManager();
        
        Material material = isMuted ? Material.RED_WOOL : Material.LIME_WOOL;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(group.getFormattedName());
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Alias: " + ChatColor.WHITE + "/" + group.getAlias());
            
            if (group.getCooldown() > 0) {
                lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.WHITE + group.getCooldown() + "s");
            }
            
            if (group.isSilencable()) {
                String status = isMuted ? 
                        ChatColor.RED + "Muted" : 
                        ChatColor.GREEN + "Unmuted";
                lore.add(ChatColor.GRAY + "Status: " + status);
                
                if (isMuted && group.isNotify()) {
                    lore.add(ChatColor.GRAY + "Notifications: " + ChatColor.YELLOW + "Enabled");
                    lore.add(ChatColor.GRAY + "Notify Delay: " + ChatColor.WHITE + group.getNotifyDelay() + "s");
                }
                
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to " + (isMuted ? "unmute" : "mute"));
            } else {
                lore.add(ChatColor.GRAY + "Status: " + ChatColor.GOLD + "Cannot be muted");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a navigation item.
     * 
     * @param material The material
     * @param name The display name
     * @param lore The lore
     * @return The ItemStack
     */
    private ItemStack createNavigationItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Handles click events in the inventory.
     * 
     * @param event The InventoryClickEvent
     */
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        
        // Handle TalkGroup clicks
        if (slotToChannelId.containsKey(slot)) {
            String channelId = slotToChannelId.get(slot);
            TalkGroup group = plugin.getConfigManager().getTalkGroup(channelId);
            
            if (group != null && group.isSilencable()) {
                PlayerDataManager dataManager = plugin.getPlayerDataManager();
                ConfigManager configManager = plugin.getConfigManager();
                
                dataManager.toggleMute(player, channelId).thenAccept(nowMuted -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String message = nowMuted ?
                                configManager.getMessage("channel.muted", "channel", group.getFormattedName()) :
                                configManager.getMessage("channel.unmuted", "channel", group.getFormattedName());
                        
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        populateInventory();
                    });
                });
            }
        }
        // Handle navigation
        else if (slot == PREVIOUS_PAGE_SLOT && currentPage > 0) {
            currentPage--;
            populateInventory();
        }
        else if (slot == NEXT_PAGE_SLOT) {
            int totalPages = (int) Math.ceil((double) accessibleGroups.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                populateInventory();
            }
        }
        else if (slot == CLOSE_SLOT) {
            player.closeInventory();
        }
    }
    
    /**
     * Opens the inventory for the player.
     */
    public void open() {
        player.openInventory(inventory);
    }
    
    /**
     * Closes the inventory for the player.
     */
    public void close() {
        player.closeInventory();
    }
    
    /**
     * Checks if the given inventory is this GUI's inventory.
     * 
     * @param inv The inventory to check
     * @return true if it's this GUI's inventory, false otherwise
     */
    public boolean isThisInventory(Inventory inv) {
        return inv.equals(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

