package org.frizzlenpop.frizzlenShop.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
            "create", "remove", "edit", "price", "reload", "logs", "tax", "maintenance",
            "populate", "template", "globalshop"
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
            case "populate":
                return handlePopulateCommand(sender, args);
            case "template":
                return handleTemplateCommand(sender, args);
            case "globalshop":
                return handleGlobalShopCommand(sender, args);
            case "shop":
                return handleShopCommand(sender, args);
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

        // Open the shop admin menu
        plugin.getGuiManager().openShopAdminMenu(player);
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
        if (!sender.hasPermission("frizzlenshop.admin.price")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 1) {
            // Show price management GUI
            if (sender instanceof Player) {
                plugin.getGuiManager().openPriceManagementMenu((Player) sender);
            }
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "help":
                MessageUtils.sendMessage(sender, "&e===== Price Management Commands =====");
                MessageUtils.sendMessage(sender, "&7/shopadmin price &f- Open price management GUI");
                MessageUtils.sendMessage(sender, "&7/shopadmin price multiplier <value> &f- Set global price multiplier");
                MessageUtils.sendMessage(sender, "&7/shopadmin price default <buy|sell> <value> &f- Set default buy/sell price");
                MessageUtils.sendMessage(sender, "&7/shopadmin price ratio <value> &f- Set sell price ratio");
                break;
                
            case "multiplier":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin price multiplier <value>");
                    return true;
                }
                
                try {
                    double multiplier = Double.parseDouble(args[2]);
                    if (multiplier < 0.1) {
                        MessageUtils.sendErrorMessage(sender, "Multiplier must be at least 0.1");
                        return true;
                    }
                    
                    plugin.getConfigManager().setGlobalPriceMultiplier(multiplier);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Global price multiplier set to " + multiplier);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "default":
                if (args.length < 4) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin price default <buy|sell> <value>");
                    return true;
                }
                
                String priceType = args[2].toLowerCase();
                if (!priceType.equals("buy") && !priceType.equals("sell")) {
                    MessageUtils.sendErrorMessage(sender, "Price type must be 'buy' or 'sell'");
                    return true;
                }
                
                try {
                    double price = Double.parseDouble(args[3]);
                    if (price < 0.01) {
                        MessageUtils.sendErrorMessage(sender, "Price must be at least 0.01");
                        return true;
                    }
                    
                    if (priceType.equals("buy")) {
                        plugin.getConfigManager().setDefaultBuyPrice(price);
                        MessageUtils.sendSuccessMessage(sender, "Default buy price set to " + price);
                    } else {
                        plugin.getConfigManager().setDefaultSellPrice(price);
                        MessageUtils.sendSuccessMessage(sender, "Default sell price set to " + price);
                    }
                    
                    plugin.getConfigManager().saveConfig();
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "ratio":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin price ratio <value>");
                    return true;
                }
                
                try {
                    double ratio = Double.parseDouble(args[2]);
                    if (ratio < 0.1 || ratio > 1.0) {
                        MessageUtils.sendErrorMessage(sender, "Ratio must be between 0.1 and 1.0");
                        return true;
                    }
                    
                    plugin.getConfigManager().setSellPriceRatio(ratio);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Sell price ratio set to " + ratio);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Unknown price subcommand. Use /shopadmin price help for help.");
                break;
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
     * Handle the tax command
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return True if the command was handled
     */
    private boolean handleTaxCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.tax")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length == 1) {
            // Show current tax rates
            ConfigManager config = plugin.getConfigManager();
            MessageUtils.sendMessage(sender, "&e===== Current Tax Rates =====");
            MessageUtils.sendMessage(sender, "&7Global: &f" + (config.getGlobalTaxRate() * 100) + "%");
            MessageUtils.sendMessage(sender, "&7Admin Shops: &f" + (config.getAdminShopTaxRate() * 100) + "%");
            MessageUtils.sendMessage(sender, "&7Player Shops: &f" + (config.getPlayerShopTaxRate() * 100) + "%");
            MessageUtils.sendMessage(sender, "&7Minimum Tax: &f" + config.getMinimumTax());
            MessageUtils.sendMessage(sender, "&7Maximum Tax: &f" + (config.getMaximumTax() == 0 ? "No maximum" : config.getMaximumTax()));
            
            // If sender is a player, offer to open the tax management GUI
            if (sender instanceof Player) {
                MessageUtils.sendMessage(sender, "&eUse &f/shopadmin tax gui &eto open the tax management GUI");
            }
            
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "help":
                MessageUtils.sendMessage(sender, "&e===== Tax Management Commands =====");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax &f- Show current tax rates");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax gui &f- Open tax management GUI (player only)");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax global <rate> &f- Set global tax rate (percentage)");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax admin <rate> &f- Set admin shop tax rate (percentage)");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax player <rate> &f- Set player shop tax rate (percentage)");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax min <amount> &f- Set minimum tax amount");
                MessageUtils.sendMessage(sender, "&7/shopadmin tax max <amount> &f- Set maximum tax amount (0 for no maximum)");
                break;
                
            case "gui":
                if (!(sender instanceof Player)) {
                    MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
                    return true;
                }
                
                // Open tax management GUI
                plugin.getGuiManager().openTaxManagementMenu((Player) sender);
                break;
                
            case "global":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin tax global <rate>");
                    return true;
                }
                
                try {
                    double rate = Double.parseDouble(args[2]);
                    if (rate < 0 || rate > 50) {
                        MessageUtils.sendErrorMessage(sender, "Tax rate must be between 0 and 50 percent");
                        return true;
                    }
                    
                    plugin.getConfigManager().setGlobalTaxRate(rate / 100);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Global tax rate set to " + rate + "%");
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "admin":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin tax admin <rate>");
                    return true;
                }
                
                try {
                    double rate = Double.parseDouble(args[2]);
                    if (rate < 0 || rate > 50) {
                        MessageUtils.sendErrorMessage(sender, "Tax rate must be between 0 and 50 percent");
                        return true;
                    }
                    
                    plugin.getConfigManager().setAdminShopTaxRate(rate / 100);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Admin shop tax rate set to " + rate + "%");
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "player":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin tax player <rate>");
                    return true;
                }
                
                try {
                    double rate = Double.parseDouble(args[2]);
                    if (rate < 0 || rate > 50) {
                        MessageUtils.sendErrorMessage(sender, "Tax rate must be between 0 and 50 percent");
                        return true;
                    }
                    
                    plugin.getConfigManager().setPlayerShopTaxRate(rate / 100);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Player shop tax rate set to " + rate + "%");
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "min":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin tax min <amount>");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount < 0) {
                        MessageUtils.sendErrorMessage(sender, "Minimum tax amount cannot be negative");
                        return true;
                    }
                    
                    plugin.getConfigManager().setMinimumTax(amount);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Minimum tax amount set to " + amount);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            case "max":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin tax max <amount>");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount < 0) {
                        MessageUtils.sendErrorMessage(sender, "Maximum tax amount cannot be negative");
                        return true;
                    }
                    
                    plugin.getConfigManager().setMaximumTax(amount);
                    plugin.getConfigManager().saveConfig();
                    MessageUtils.sendSuccessMessage(sender, "Maximum tax amount set to " + (amount == 0 ? "no maximum" : amount));
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage(sender, "Invalid number format");
                }
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Unknown tax subcommand. Use /shopadmin tax help for help.");
                break;
        }
        
        return true;
    }

    /**
     * Handle the maintenance command
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return True if the command was handled
     */
    private boolean handleMaintenanceCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.maintenance")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use this command.");
            return true;
        }
        
        ConfigManager config = plugin.getConfigManager();
        
        if (args.length == 1) {
            // Show current maintenance status
            boolean maintenanceMode = config.isMaintenanceMode();
            MessageUtils.sendMessage(sender, "&eMaintenance mode is currently " + 
                (maintenanceMode ? "&cENABLED" : "&aOFF"));
            MessageUtils.sendMessage(sender, "&eUse &f/shopadmin maintenance <on|off> &eto change");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin maintenance <on|off>");
            return true;
        }
        
        String mode = args[1].toLowerCase();
        
        if (mode.equals("on") || mode.equals("enable") || mode.equals("true")) {
            config.setMaintenanceMode(true);
            config.saveConfig();
            MessageUtils.sendSuccessMessage(sender, "Maintenance mode enabled. Only admins can access shops.");
            
            // Broadcast to all players
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', 
                "&c&l[SHOP] &cShop system is now in maintenance mode. Please finish your transactions."));
        } else if (mode.equals("off") || mode.equals("disable") || mode.equals("false")) {
            config.setMaintenanceMode(false);
            config.saveConfig();
            MessageUtils.sendSuccessMessage(sender, "Maintenance mode disabled. All players can access shops again.");
            
            // Broadcast to all players
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', 
                "&a&l[SHOP] &aShop system is now back online!"));
        } else {
            MessageUtils.sendErrorMessage(sender, "Invalid mode. Use 'on' or 'off'.");
        }
        
        return true;
    }

    /**
     * Handles the /shopadmin populate command
     * Populates an admin shop with predefined items
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handlePopulateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.populate")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to populate admin shops.");
            return true;
        }

        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }

        if (args.length < 3) {
            MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin populate <shop-id> <category>");
            MessageUtils.sendMessage(sender, "Available categories: tools, weapons, armor, food, blocks, redstone, potions");
            return true;
        }

        try {
            UUID shopId = UUID.fromString(args[1]);
            String category = args[2].toLowerCase();
            
            // Get the shop
            Shop shop = plugin.getShopManager().getShop(shopId);
            if (shop == null) {
                MessageUtils.sendErrorMessage(sender, "Shop not found with ID: " + shopId);
                return true;
            }
            
            // Check if it's an admin shop
            if (!shop.isAdminShop()) {
                MessageUtils.sendErrorMessage(sender, "This command can only be used with admin shops.");
                return true;
            }
            
            // Cast to AdminShop to use our new methods
            AdminShop adminShop = (AdminShop) shop;
            
            // Get currency (optional argument)
            String currency = args.length > 3 ? args[3] : plugin.getEconomyManager().getDefaultCurrency();
            
            // Populate the shop with items from the specified category
            int added = adminShop.addCategoryItems(category, currency);
            
            if (added > 0) {
                MessageUtils.sendSuccessMessage(sender, "Added " + added + " items to the shop from category: " + category);
            } else {
                MessageUtils.sendErrorMessage(sender, "No items were added. Invalid category or all items already exist.");
            }
            
        } catch (IllegalArgumentException e) {
            MessageUtils.sendErrorMessage(sender, "Invalid shop ID. Please use a valid UUID.");
        }
        
        return true;
    }
    
    /**
     * Handles the /shopadmin template command
     * Saves or loads shop templates
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleTemplateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.template")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use shop templates.");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin template <save|load> [template-name] [shop-id]");
            return true;
        }

        String action = args[1].toLowerCase();
        
        if (action.equals("save")) {
            if (args.length < 4) {
                MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin template save <template-name> <shop-id>");
                return true;
            }
            
            String templateName = args[2];
            try {
                UUID shopId = UUID.fromString(args[3]);
                
                // Get the shop
                Shop shop = plugin.getShopManager().getShop(shopId);
                if (shop == null) {
                    MessageUtils.sendErrorMessage(sender, "Shop not found with ID: " + shopId);
                    return true;
                }
                
                // For now, just acknowledge the command
                // In a real implementation, you'd save the shop items to a template file/database
                MessageUtils.sendMessage(sender, "Shop template saving functionality is still in development.");
                
            } catch (IllegalArgumentException e) {
                MessageUtils.sendErrorMessage(sender, "Invalid shop ID. Please use a valid UUID.");
            }
            
        } else if (action.equals("load")) {
            if (args.length < 4) {
                MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin template load <template-name> <shop-id>");
                return true;
            }
            
            String templateName = args[2];
            try {
                UUID shopId = UUID.fromString(args[3]);
                
                // Get the shop
                Shop shop = plugin.getShopManager().getShop(shopId);
                if (shop == null) {
                    MessageUtils.sendErrorMessage(sender, "Shop not found with ID: " + shopId);
                    return true;
                }
                
                // For now, just acknowledge the command
                // In a real implementation, you'd load items from a template
                MessageUtils.sendMessage(sender, "Shop template loading functionality is still in development.");
                
            } catch (IllegalArgumentException e) {
                MessageUtils.sendErrorMessage(sender, "Invalid shop ID. Please use a valid UUID.");
            }
            
        } else {
            MessageUtils.sendErrorMessage(sender, "Invalid action. Use 'save' or 'load'.");
        }
        
        return true;
    }
    
    /**
     * Handles the /shopadmin globalshop command
     * Creates a global admin shop that can be accessed from anywhere
     *
     * @param sender The command sender
     * @param args   The command arguments
     * @return True if the command was handled, false otherwise
     */
    private boolean handleGlobalShopCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.globalshop")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to manage global shops.");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin globalshop <create|remove|list> [name]");
            return true;
        }

        String action = args[1].toLowerCase();
        
        if (action.equals("create")) {
            if (args.length < 3) {
                MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin globalshop create <name>");
                return true;
            }
            
            String name = args[2];
            
            // For now, just create a regular admin shop at the player's location
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Location location = player.getLocation();
                
                // Create the admin shop
                AdminShop shop = plugin.getShopManager().createAdminShop(name, location);
                if (shop == null) {
                    MessageUtils.sendErrorMessage(player, "Failed to create a global shop.");
                    return true;
                }
                
                MessageUtils.sendSuccessMessage(player, "Global shop '" + name + "' created successfully!");
                MessageUtils.sendMessage(player, "Shop ID: " + shop.getId());
                
                // For a real global shop implementation, you'd need to store this in a special registry
                // and add a command to access it from anywhere
                MessageUtils.sendMessage(player, "Note: True global shops that can be accessed from anywhere are still in development.");
            } else {
                MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            }
            
        } else if (action.equals("remove")) {
            if (args.length < 3) {
                MessageUtils.sendErrorMessage(sender, "Usage: /shopadmin globalshop remove <shop-id>");
                return true;
            }
            
            try {
                UUID shopId = UUID.fromString(args[2]);
                
                // Get the shop
                Shop shop = plugin.getShopManager().getShop(shopId);
                if (shop == null) {
                    MessageUtils.sendErrorMessage(sender, "Shop not found with ID: " + shopId);
                    return true;
                }
                
                // Delete the shop
                if (plugin.getShopManager().deleteShop(shopId)) {
                    MessageUtils.sendSuccessMessage(sender, "Global shop '" + shop.getName() + "' removed successfully!");
                } else {
                    MessageUtils.sendErrorMessage(sender, "Failed to remove the global shop.");
                }
                
            } catch (IllegalArgumentException e) {
                MessageUtils.sendErrorMessage(sender, "Invalid shop ID. Please use a valid UUID.");
            }
            
        } else if (action.equals("list")) {
            // List all admin shops (in a real implementation, you'd only list global shops)
            Collection<Shop> adminShops = plugin.getShopManager().getAdminShops();
            
            if (adminShops.isEmpty()) {
                MessageUtils.sendMessage(sender, "No admin shops found.");
            } else {
                MessageUtils.sendMessage(sender, "&e===== Admin Shops =====");
                for (Shop shop : adminShops) {
                    MessageUtils.sendMessage(sender, "&7ID: &f" + shop.getId() + " &7| Name: &f" + shop.getName());
                }
            }
            
        } else {
            MessageUtils.sendErrorMessage(sender, "Invalid action. Use 'create', 'remove', or 'list'.");
        }
        
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
        MessageUtils.sendMessage(sender, "&7/shopadmin populate <shop-id> <category> &f- Add items from a category");
        MessageUtils.sendMessage(sender, "&7/shopadmin template <save|load> <name> <shop-id> &f- Manage shop templates");
        MessageUtils.sendMessage(sender, "&7/shopadmin globalshop <create|remove|list> [name] &f- Manage global shops");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("populate")) {
                return Arrays.asList("tools", "weapons", "armor", "food", "blocks", "redstone", "potions").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("template")) {
                return Arrays.asList("save", "load").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("globalshop")) {
                return Arrays.asList("create", "remove", "list").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals("maintenance")) {
                return Arrays.asList("on", "off").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }

    /**
     * Handle the shop command
     *
     * @param sender The command sender
     * @param args The command arguments
     * @return True if the command was handled
     */
    private boolean handleShopCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("frizzlenshop.admin.shop")) {
            MessageUtils.sendErrorMessage(sender, "You don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            MessageUtils.sendErrorMessage(sender, "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Open shop admin menu
            plugin.getGuiManager().openShopAdminMenu(player);
            return true;
        }
        
        // Handle subcommands
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "help":
                MessageUtils.sendMessage(player, "&e===== Shop Admin Commands =====");
                MessageUtils.sendMessage(player, "&7/shopadmin shop &f- Open shop admin menu");
                MessageUtils.sendMessage(player, "&7/shopadmin shop create <name> &f- Create a new admin shop");
                MessageUtils.sendMessage(player, "&7/shopadmin shop list &f- List all admin shops");
                MessageUtils.sendMessage(player, "&7/shopadmin shop delete <id> &f- Delete an admin shop");
                break;
                
            case "create":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(player, "Usage: /shopadmin shop create <name>");
                    return true;
                }
                
                String shopName = args[2];
                
                // Create a new admin shop at the player's location
                Location location = player.getLocation();
                AdminShop shop = plugin.getShopManager().createAdminShop(shopName, location);
                
                if (shop != null) {
                    MessageUtils.sendSuccessMessage(player, "Admin shop created: " + shopName);
                    MessageUtils.sendMessage(player, "&eUse the GUI to manage this shop.");
                } else {
                    MessageUtils.sendErrorMessage(player, "Failed to create admin shop. Check if there's already a shop at this location.");
                }
                break;
                
            case "list":
                List<Shop> adminShops = new ArrayList<>(plugin.getShopManager().getAdminShops());
                
                if (adminShops.isEmpty()) {
                    MessageUtils.sendMessage(player, "&eNo admin shops found.");
                    return true;
                }
                
                MessageUtils.sendMessage(player, "&e===== Admin Shops =====");
                for (Shop adminShop : adminShops) {
                    MessageUtils.sendMessage(player, "&7ID: &f" + adminShop.getId());
                    MessageUtils.sendMessage(player, "&7Name: &f" + adminShop.getName());
                    MessageUtils.sendMessage(player, "&7Location: &f" + formatLocation(adminShop.getLocation()));
                    MessageUtils.sendMessage(player, "&7Items: &f" + adminShop.getItems().size());
                    MessageUtils.sendMessage(player, "");
                }
                break;
                
            case "delete":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(player, "Usage: /shopadmin shop delete <id>");
                    return true;
                }
                
                try {
                    UUID shopId = UUID.fromString(args[2]);
                    boolean success = plugin.getShopManager().deleteShop(shopId);
                    
                    if (success) {
                        MessageUtils.sendSuccessMessage(player, "Shop deleted successfully.");
                    } else {
                        MessageUtils.sendErrorMessage(player, "Failed to delete shop. Check if the ID is correct.");
                    }
                } catch (IllegalArgumentException e) {
                    MessageUtils.sendErrorMessage(player, "Invalid shop ID format.");
                }
                break;
                
            default:
                MessageUtils.sendErrorMessage(player, "Unknown shop subcommand. Use /shopadmin shop help for help.");
                break;
        }
        
        return true;
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }
} 