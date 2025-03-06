package org.frizzlenpop.frizzlenShop.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the /shop command
 */
public class ShopCommand implements CommandExecutor, TabCompleter {

    private final FrizzlenShop plugin;
    private final List<String> subCommands = Arrays.asList(
            "browse", "search", "create", "manage", "history", "sell", "buy", "help"
    );

    /**
     * Creates a new shop command
     *
     * @param plugin The plugin instance
     */
    public ShopCommand(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("frizzlenshop.use")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to use the shop system.");
            return true;
        }

        if (args.length == 0) {
            // No arguments, open the main shop menu
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "browse":
                return handleBrowseCommand(player, args);
            case "search":
                return handleSearchCommand(player, args);
            case "create":
                return handleCreateCommand(player, args);
            case "manage":
                return handleManageCommand(player, args);
            case "history":
                return handleHistoryCommand(player, args);
            case "sell":
                return handleSellCommand(player, args);
            case "buy":
                return handleBuyCommand(player, args);
            case "help":
                return handleHelpCommand(player, args);
            default:
                MessageUtils.sendErrorMessage(player, "Unknown sub-command. Use /shop help for a list of commands.");
                return true;
        }
    }

    /**
     * Handles the /shop browse command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleBrowseCommand(Player player, String[] args) {
        if (args.length < 2) {
            // No category specified, show a list of categories
            MessageUtils.sendMessage(player, "&eAvailable categories:");
            MessageUtils.sendMessage(player, "&7- tools");
            MessageUtils.sendMessage(player, "&7- food");
            MessageUtils.sendMessage(player, "&7- potions");
            MessageUtils.sendMessage(player, "&7- armor");
            MessageUtils.sendMessage(player, "&7- valuables");
            MessageUtils.sendMessage(player, "&7- blocks");
            MessageUtils.sendMessage(player, "&7- misc");
            return true;
        }

        String category = args[1].toLowerCase();
        plugin.getGuiManager().openCategoryMenu(player, category, 1);
        return true;
    }

    /**
     * Handles the /shop search command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleSearchCommand(Player player, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(player, "Usage: /shop search <query>");
            return true;
        }

        // Build the search query from all arguments
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                queryBuilder.append(" ");
            }
            queryBuilder.append(args[i]);
        }
        String query = queryBuilder.toString().toLowerCase();
        
        MessageUtils.sendMessage(player, "&eSearching for: &f" + query);
        
        // Collect matching items from all shops
        List<SearchResult> results = new ArrayList<>();
        
        // Get all shops
        Collection<Shop> shops = plugin.getShopManager().getAllShops();
        
        // Search in each shop
        for (Shop shop : shops) {
            // Skip shops that aren't open
            if (!shop.isOpen()) {
                continue;
            }
            
            for (ShopItem item : shop.getItems()) {
                ItemStack itemStack = item.getItem();
                Material material = itemStack.getType();
                String materialName = material.name().toLowerCase().replace("_", " ");
                
                // Check item name, material name, and lore for matches
                boolean matches = materialName.contains(query);
                
                // Check custom name if exists
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                    String itemName = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName().toLowerCase());
                    matches = matches || itemName.contains(query);
                }
                
                // Check lore if exists
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()) {
                    List<String> lore = itemStack.getItemMeta().getLore();
                    for (String line : lore) {
                        String strippedLine = ChatColor.stripColor(line.toLowerCase());
                        if (strippedLine.contains(query)) {
                            matches = true;
                            break;
                        }
                    }
                }
                
                if (matches) {
                    results.add(new SearchResult(shop, item));
                }
            }
        }
        
        // If no results found
        if (results.isEmpty()) {
            MessageUtils.sendMessage(player, "&cNo items found matching your search.");
            return true;
        }
        
        // Sort results by shop type and price
        results.sort(Comparator.comparing((SearchResult r) -> r.shop.isAdminShop() ? 0 : 1)
                .thenComparing(r -> r.item.getBuyPrice()));
        
        // Display results to player
        MessageUtils.sendMessage(player, "&6Found &f" + results.size() + "&6 items matching your search:");
        
        // Limit to 10 results to avoid spam
        int displayed = 0;
        for (SearchResult result : results) {
            if (displayed >= 10) {
                MessageUtils.sendMessage(player, "&7And " + (results.size() - 10) + " more results...");
                break;
            }
            
            Shop shop = result.shop;
            ShopItem item = result.item;
            String shopType = shop.isAdminShop() ? "&c[Admin]" : "&a[Player]";
            String itemName = item.getItem().getType().toString().toLowerCase().replace("_", " ");
            
            if (item.getItem().hasItemMeta() && item.getItem().getItemMeta().hasDisplayName()) {
                itemName = ChatColor.stripColor(item.getItem().getItemMeta().getDisplayName());
            }
            
            MessageUtils.sendMessage(player, 
                    shopType + " &f" + shop.getName() + "&8: &f" + itemName + 
                    " &8- &eBuy: &f" + plugin.getEconomyManager().formatCurrency(item.getBuyPrice(), item.getCurrency()) + 
                    " &eSell: &f" + plugin.getEconomyManager().formatCurrency(item.getSellPrice(), item.getCurrency()));
            
            displayed++;
        }
        
        MessageUtils.sendMessage(player, "&eClick on this message to view search results in GUI");
        // TODO: Implement click event to open search results in GUI
        
        return true;
    }

    /**
     * Handles the /shop create command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("frizzlenshop.create")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to create shops.");
            return true;
        }

        // Check if player has reached their shop limit
        if (plugin.getShopManager().hasReachedShopLimit(player)) {
            int maxShops = plugin.getShopManager().getMaxShopsForPlayer(player);
            MessageUtils.sendErrorMessage(player, "You have reached your shop limit of " + maxShops + ".");
            return true;
        }

        // Open the shop creation GUI
        plugin.getGuiManager().openCreateShopMenu(player, 1);
        return true;
    }

    /**
     * Handles the /shop manage command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleManageCommand(Player player, String[] args) {
        // Open the my shops menu
        plugin.getGuiManager().openMyShopsMenu(player);
        return true;
    }

    /**
     * Handles the /shop history command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleHistoryCommand(Player player, String[] args) {
        // For now, this is just a placeholder
        MessageUtils.sendMessage(player, "&eViewing transaction history...");
        return true;
    }

    /**
     * Handles the /shop sell command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleSellCommand(Player player, String[] args) {
        if (!player.hasPermission("frizzlenshop.sell")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to sell items to shops.");
            return true;
        }

        // For now, this is just a placeholder
        MessageUtils.sendMessage(player, "&eNot implemented yet. Use the GUI to sell items.");
        return true;
    }

    /**
     * Handles the /shop buy command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleBuyCommand(Player player, String[] args) {
        if (!player.hasPermission("frizzlenshop.buy")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to buy items from shops.");
            return true;
        }

        // For now, this is just a placeholder
        MessageUtils.sendMessage(player, "&eNot implemented yet. Use the GUI to buy items.");
        return true;
    }

    /**
     * Handles the /shop help command
     *
     * @param player The player
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleHelpCommand(Player player, String[] args) {
        MessageUtils.sendMessage(player, "&e===== FrizzlenShop Help =====");
        MessageUtils.sendMessage(player, "&7/shop &f- Open the shop menu");
        MessageUtils.sendMessage(player, "&7/shop browse [category] &f- Browse shops by category");
        MessageUtils.sendMessage(player, "&7/shop search <query> &f- Search for specific items");
        MessageUtils.sendMessage(player, "&7/shop create &f- Start shop creation wizard");
        MessageUtils.sendMessage(player, "&7/shop manage &f- Manage your shops");
        MessageUtils.sendMessage(player, "&7/shop history &f- View your transaction history");
        MessageUtils.sendMessage(player, "&7/shop sell <item> [amount] [price] &f- Quick-sell items");
        MessageUtils.sendMessage(player, "&7/shop buy <item> [amount] &f- Quick-buy items");
        MessageUtils.sendMessage(player, "&7/shop help &f- Show this help message");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Sub-command completion
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument completion
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("browse")) {
                // Complete categories
                List<String> categories = Arrays.asList(
                        "tools", "food", "potions", "armor", "valuables", "blocks", "misc"
                );
                
                return categories.stream()
                        .filter(category -> category.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }

    /**
     * Helper class for search results
     */
    private static class SearchResult {
        private final Shop shop;
        private final ShopItem item;
        
        public SearchResult(Shop shop, ShopItem item) {
            this.shop = shop;
            this.item = item;
        }
    }
} 