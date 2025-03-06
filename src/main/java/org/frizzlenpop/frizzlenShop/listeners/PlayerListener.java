package org.frizzlenpop.frizzlenShop.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.PlayerShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

/**
 * Handles player-related events for the shop system
 */
public class PlayerListener implements Listener {

    private final FrizzlenShop plugin;

    /**
     * Creates a new player listener
     *
     * @param plugin The plugin instance
     */
    public PlayerListener(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check for expired shops
        if (player.hasPermission("frizzlenshop.create")) {
            plugin.getShopManager().checkExpiredShops();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear any menu data
        plugin.getGuiManager().clearMenuData(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null) {
            return;
        }
        
        // Check if there's a shop at this location
        Shop shop = plugin.getShopManager().getShopAtLocation(block.getLocation());
        if (shop == null) {
            return;
        }
        
        // Cancel the event to prevent normal block interaction
        event.setCancelled(true);
        
        // Check if the player has permission to use shops
        if (!player.hasPermission("frizzlenshop.use")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to use shops.");
            return;
        }
        
        // Check if the shop is in maintenance mode
        if (plugin.getConfigManager().isMaintenanceMode() && !player.hasPermission("frizzlenshop.admin")) {
            MessageUtils.sendErrorMessage(player, "Shops are currently in maintenance mode. Please try again later.");
            return;
        }
        
        // Open the shop menu for the player
        // Check if this is the player's shop
        if (shop instanceof PlayerShop) {
            PlayerShop playerShop = (PlayerShop) shop;
            if (playerShop.getOwner() != null && playerShop.getOwner().equals(player.getUniqueId())) {
                // Open shop management for owner
                plugin.getGuiManager().openMyShopsMenu(player); // First show their shops menu
                MessageUtils.sendMessage(player, "&eOpening your shop management.");
                return;
            }
        }
        
        // For customers, open the shop's item list by creating and showing a "ShopCategory" menu
        plugin.getGuiManager().openMainMenu(player); // First show the main menu
        MessageUtils.sendMessage(player, "&eOpening shop: &f" + shop.getName());
    }
} 