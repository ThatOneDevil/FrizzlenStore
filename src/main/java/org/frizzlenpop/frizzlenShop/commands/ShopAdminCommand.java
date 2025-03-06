package org.frizzlenpop.frizzlenShop.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the /shopadmin command
 */
public class ShopAdminCommand implements CommandExecutor, TabCompleter {

    private final FrizzlenShop plugin;
    private final List<String> subCommands = Arrays.asList(
            "create", "remove", "edit", "price", "reload", "logs", "tax", "maintenance"
    );

    /**
     * Creates a new shop admin command
     *
     * @param plugin The plugin instance
     */
    public ShopAdminCommand(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use admin commands.");
            return true;
        }

        if (args.length == 0) {
            // No arguments, open the admin menu or show help
            if (sender instanceof Player) {
                plugin.getGuiManager().openShopAdminMenu((Player) sender);
            } else {
                sendAdminHelp(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreateCommand(sender, args);
            case "remove":
                return handleRemoveCommand(sender, args);
            case "edit":
                return handleEditCommand(sender, args);
            case "price":
                return handlePriceCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "logs":
                return handleLogsCommand(sender, args);
            case "tax":
                return handleTaxCommand(sender, args);
            case "maintenance":
                return handleMaintenanceCommand(sender, args);
            default:
                MessageUtils.sendErrorMessage(sender, "Unknown sub-command. Use /shopadmin help for a list of commands.");
                return true;
        }
    }

    /**
     * Handles the /shopadmin create command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.create")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to create admin shops.");
            return true;
        }

        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            MessageUtils.sendErrorMessage(player, "Usage: /shopadmin create <name>");
            return true;
        }

        String name = args[1];
        Location location = player.getLocation();

        // Check if there's already a shop at this location
        Shop existingShop = plugin.getShopManager().getShopAtLocation(location);
        if (existingShop != null) {
            MessageUtils.sendErrorMessage(player, "There is already a shop at this location.");
            return true;
        }

        // Create the admin shop
        AdminShop shop = plugin.getShopManager().createAdminShop(name, location);
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "Failed to create an admin shop. Admin shops might be disabled.");
            return true;
        }

        MessageUtils.sendSuccessMessage(player, "Admin shop '" + name + "' created successfully!");
        return true;
    }

    /**
     * Handles the /shopadmin remove command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleRemoveCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.remove")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to remove shops.");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin remove <shop-id>");
            return true;
        }

        try {
            UUID shopId = UUID.fromString(args[1]);
            Shop shop = plugin.getShopManager().getShop(shopId);

            if (shop == null) {
                MessageUtils.sendErrorMessage(sender, "Shop not found with ID: " + shopId);
                return true;
            }

            if (plugin.getShopManager().deleteShop(shopId)) {
                MessageUtils.sendSuccessMessage(sender, "Shop '" + shop.getName() + "' removed successfully!");
                
                // Log the deletion
                if (sender instanceof Player) {
                    plugin.getLogManager().logShopDeletion((Player) sender, shopId, shop.getName());
                }
            } else {
                MessageUtils.sendErrorMessage(sender, "Failed to remove the shop.");
            }
        } catch (IllegalArgumentException e) {
            MessageUtils.sendErrorMessage(sender, "Invalid shop ID format. Use a valid UUID.");
        }

        return true;
    }

    /**
     * Handles the /shopadmin edit command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleEditCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.edit")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to edit shops.");
            return true;
        }

        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // This would open a GUI for editing shop properties
        // For now, just provide basic implementation
        MessageUtils.sendMessage(player, "&eNot fully implemented yet. Use the GUI for shop administration.");
        return true;
    }

    /**
     * Handles the /shopadmin price command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handlePriceCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.prices")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to change shop prices.");
            return true;
        }

        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 5) {
            MessageUtils.sendErrorMessage(player, "Usage: /shopadmin price <shop-id> <buy-price> <sell-price>");
            return true;
        }

        try {
            UUID shopId = UUID.fromString(args[1]);
            double buyPrice = Double.parseDouble(args[2]);
            double sellPrice = Double.parseDouble(args[3]);
            String currency = args.length > 4 ? args[4] : plugin.getEconomyManager().getDefaultCurrency();

            Shop shop = plugin.getShopManager().getShop(shopId);
            if (shop == null) {
                MessageUtils.sendErrorMessage(player, "Shop not found with ID: " + shopId);
                return true;
            }

            // For now, we don't have a specific item to modify prices for
            // In a real implementation, you'd want to have a way to specify the item
            MessageUtils.sendMessage(player, "&eNot fully implemented yet. Use the GUI for price administration.");
        } catch (IllegalArgumentException e) {
            MessageUtils.sendErrorMessage(player, "Invalid arguments. Make sure the shop ID is a valid UUID and prices are valid numbers.");
        }

        return true;
    }

    /**
     * Handles the /shopadmin reload command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.reload")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to reload the plugin.");
            return true;
        }

        // Save data before reloading
        plugin.getDataManager().saveData();

        // Reload configuration
        plugin.getConfigManager().loadConfig();

        // Load data again
        plugin.getDataManager().loadData();

        MessageUtils.sendSuccessMessage(sender, "FrizzlenShop configuration reloaded!");
        return true;
    }

    /**
     * Handles the /shopadmin logs command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleLogsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.logs")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to view logs.");
            return true;
        }

        // Process command arguments
        int page = 1;
        String filter = null;
        String shopName = null;
        String playerName = null;
        
        if (args.length > 1) {
            // Check for flags and options
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                
                // Handle page number
                if (arg.startsWith("page:")) {
                    try {
                        page = Integer.parseInt(arg.substring(5));
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage(sender, "Invalid page number format. Use page:X");
                    }
                }
                
                // Handle transaction type filter
                else if (arg.startsWith("type:")) {
                    filter = arg.substring(5).toLowerCase();
                    if (!filter.equals("buy") && !filter.equals("sell") && !filter.equals("all")) {
                        MessageUtils.sendErrorMessage(sender, "Invalid filter type. Use type:buy, type:sell, or type:all");
                        filter = "all";
                    }
                }
                
                // Handle shop filter
                else if (arg.startsWith("shop:")) {
                    shopName = arg.substring(5);
                }
                
                // Handle player filter
                else if (arg.startsWith("player:")) {
                    playerName = arg.substring(7);
                }
            }
        }
        
        // If the sender is a player and no flags specified, open the GUI
        if (sender instanceof Player && args.length <= 1) {
            Player player = (Player) sender;
            plugin.getGuiManager().openTransactionLogsMenu(player);
            return true;
        }
        
        // Retrieve the logs from the database
        List<TransactionLog> logs = getFilteredLogs(filter, shopName, playerName, page);
        
        // If no logs found
        if (logs.isEmpty()) {
            MessageUtils.sendMessage(sender, "&cNo logs found matching your criteria.");
            return true;
        }
        
        // Show header with filter info
        StringBuilder header = new StringBuilder("&6Transaction Logs - Page " + page);
        if (filter != null && !filter.equals("all")) {
            header.append(" - Type: ").append(filter);
        }
        if (shopName != null) {
            header.append(" - Shop: ").append(shopName);
        }
        if (playerName != null) {
            header.append(" - Player: ").append(playerName);
        }
        
        MessageUtils.sendMessage(sender, header.toString());
        MessageUtils.sendMessage(sender, "&8-------------------------------------------------");
        
        // Show each log entry
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (TransactionLog log : logs) {
            String typeColor = log.getType().equals("buy") ? "&a" : "&c";
            String shopColor = log.isAdminShop() ? "&c" : "&b";
            
            MessageUtils.sendMessage(sender, 
                    "&7[" + dateFormat.format(new Date(log.getTimestamp())) + "] " +
                    typeColor + log.getType().toUpperCase() + " &f" +
                    log.getAmount() + "x " + log.getItemName() + " &7- " +
                    "&eCost: &f" + plugin.getEconomyManager().formatCurrency(log.getPrice(), log.getCurrency()) + " &7- " +
                    "&7Shop: " + shopColor + log.getShopName() + " &7- " +
                    "&7Player: &f" + log.getPlayerName());
        }
        
        MessageUtils.sendMessage(sender, "&8-------------------------------------------------");
        MessageUtils.sendMessage(sender, "&7Use &f/shopadmin logs page:" + (page + 1) + "&7 to see the next page");
        
        return true;
    }
    
    /**
     * Get filtered transaction logs from the database
     * 
     * @param type The transaction type filter (buy, sell, or all)
     * @param shopName The shop name to filter by
     * @param playerName The player name to filter by
     * @param page The page number
     * @return List of matching transaction logs
     */
    private List<TransactionLog> getFilteredLogs(String type, String shopName, String playerName, int page) {
        // This would normally query the database
        // For now, return dummy data
        
        List<TransactionLog> logs = new ArrayList<>();
        
        // Add dummy data
        for (int i = 0; i < 10; i++) {
            TransactionLog log = new TransactionLog();
            log.setId(UUID.randomUUID());
            log.setTimestamp(System.currentTimeMillis() - (i * 3600000)); // Each log is 1 hour apart
            log.setType(i % 2 == 0 ? "buy" : "sell");
            log.setAmount(i + 1);
            log.setItemName(getDummyItemName(i));
            log.setPrice((i + 1) * 100.0);
            log.setCurrency("coins");
            log.setShopName(i % 3 == 0 ? "AdminShop" : "PlayerShop" + i);
            log.setAdminShop(i % 3 == 0);
            log.setPlayerName("Player" + (i % 5 + 1));
            
            // Apply filters
            boolean includeLog = true;
            
            if (type != null && !type.equals("all") && !log.getType().equals(type)) {
                includeLog = false;
            }
            
            if (shopName != null && !log.getShopName().equalsIgnoreCase(shopName)) {
                includeLog = false;
            }
            
            if (playerName != null && !log.getPlayerName().equalsIgnoreCase(playerName)) {
                includeLog = false;
            }
            
            if (includeLog) {
                logs.add(log);
            }
        }
        
        return logs;
    }
    
    /**
     * Get a dummy item name for demonstration purposes
     * 
     * @param index The index to determine the item name
     * @return A dummy item name
     */
    private String getDummyItemName(int index) {
        String[] items = {
            "Diamond Sword",
            "Iron Pickaxe",
            "Golden Apple",
            "Enchanted Book",
            "Elytra",
            "Diamond Block",
            "Emerald",
            "Netherite Ingot",
            "Trident",
            "Shulker Box"
        };
        
        return items[index % items.length];
    }
    
    /**
     * Represents a transaction log entry
     */
    private static class TransactionLog {
        private UUID id;
        private long timestamp;
        private String type;
        private int amount;
        private String itemName;
        private double price;
        private String currency;
        private String shopName;
        private boolean adminShop;
        private String playerName;
        
        public UUID getId() {
            return id;
        }
        
        public void setId(UUID id) {
            this.id = id;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public void setAmount(int amount) {
            this.amount = amount;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }
        
        public double getPrice() {
            return price;
        }
        
        public void setPrice(double price) {
            this.price = price;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getShopName() {
            return shopName;
        }
        
        public void setShopName(String shopName) {
            this.shopName = shopName;
        }
        
        public boolean isAdminShop() {
            return adminShop;
        }
        
        public void setAdminShop(boolean adminShop) {
            this.adminShop = adminShop;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }

    /**
     * Handles the /shopadmin tax command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleTaxCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.tax")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to change tax settings.");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendMessage(sender, "&eCurrent tax rates:");
            MessageUtils.sendMessage(sender, "&7Default: &e" + plugin.getConfigManager().getDefaultTaxRate() + "%");
            MessageUtils.sendMessage(sender, "&7Admin Shops: &e" + plugin.getConfigManager().getAdminShopTaxRate() + "%");
            MessageUtils.sendMessage(sender, "&7Player Shops: &e" + plugin.getConfigManager().getPlayerShopTaxRate() + "%");
            return true;
        }

        // This would modify tax rates in the config
        // For now, just provide basic feedback
        MessageUtils.sendMessage(sender, "&eChanging tax rates is not fully implemented yet.");
        return true;
    }

    /**
     * Handles the /shopadmin maintenance command
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleMaintenanceCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.maintenance")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use maintenance mode.");
            return true;
        }

        if (args.length < 2) {
            boolean currentMode = plugin.getConfigManager().isMaintenanceMode();
            MessageUtils.sendMessage(sender, "&eMaintenance mode is currently " + (currentMode ? "&aENABLED" : "&cDISABLED"));
            return true;
        }

        String mode = args[1].toLowerCase();
        boolean enable = mode.equals("on") || mode.equals("true") || mode.equals("enable");

        // This would enable/disable maintenance mode
        // For now, just provide basic feedback
        MessageUtils.sendMessage(sender, "&eChanging maintenance mode is not fully implemented yet.");
        return true;
    }

    /**
     * Sends help info to a command sender
     *
     * @param sender The command sender
     */
    private void sendAdminHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "&e===== FrizzlenShop Admin Help =====");
        MessageUtils.sendMessage(sender, "&7/shopadmin create <name> &f- Create an admin shop");
        MessageUtils.sendMessage(sender, "&7/shopadmin remove <shop-id> &f- Remove a shop");
        MessageUtils.sendMessage(sender, "&7/shopadmin edit <shop-id> &f- Edit shop settings");
        MessageUtils.sendMessage(sender, "&7/shopadmin price <shop-id> <buy> <sell> [currency] &f- Set prices");
        MessageUtils.sendMessage(sender, "&7/shopadmin reload &f- Reload configuration");
        MessageUtils.sendMessage(sender, "&7/shopadmin logs <player> [timeframe] &f- View transaction logs");
        MessageUtils.sendMessage(sender, "&7/shopadmin tax <rate> &f- Set global tax rate");
        MessageUtils.sendMessage(sender, "&7/shopadmin maintenance <on|off> &f- Toggle maintenance mode");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("frizzlenshop.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // Sub-command completion
            return subCommands.stream()
                    .filter(subCmd -> subCmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument completion
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("maintenance")) {
                // Complete on/off
                List<String> options = Arrays.asList("on", "off");
                
                return options.stream()
                        .filter(option -> option.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
} 