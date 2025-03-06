package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.DatabaseManager;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Handles the transaction logs menu
 */
public class TransactionLogsMenuHandler {

    // Maximum logs per page
    private static final int LOGS_PER_PAGE = 45;
    
    /**
     * Open the transaction logs menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openTransactionLogsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        openTransactionLogsMenu(guiManager, plugin, player, 1, null, null, null);
    }
    
    /**
     * Open the transaction logs menu for a player with filters
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param page The page number
     * @param transactionType The transaction type filter (buy/sell/all)
     * @param shopFilter The shop filter (shop name)
     * @param playerFilter The player filter (player name)
     */
    public static void openTransactionLogsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, 
                                              int page, String transactionType, String shopFilter, String playerFilter) {
        // Check if player has permission
        if (!player.hasPermission("frizzlenshop.admin.logs")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to view transaction logs.");
            return;
        }
        
        // Create inventory
        String title = "Transaction Logs - Page " + page;
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get logs (using dummy data for now since we need to implement a database system)
        List<TransactionLog> logs = getDummyLogs(50);
        
        // Apply filters if present
        if (transactionType != null) {
            logs = logs.stream()
                    .filter(log -> transactionType.equalsIgnoreCase("all") || log.getType().equalsIgnoreCase(transactionType))
                    .toList();
        }
        
        if (shopFilter != null) {
            logs = logs.stream()
                    .filter(log -> log.getShopName().toLowerCase().contains(shopFilter.toLowerCase()))
                    .toList();
        }
        
        if (playerFilter != null) {
            logs = logs.stream()
                    .filter(log -> log.getPlayerName().toLowerCase().contains(playerFilter.toLowerCase()))
                    .toList();
        }
        
        // Calculate total pages
        int totalPages = Math.max(1, (int) Math.ceil((double) logs.size() / LOGS_PER_PAGE));
        
        // Ensure page is within range
        page = Math.max(1, Math.min(page, totalPages));
        
        // Calculate log range for this page
        int startIndex = (page - 1) * LOGS_PER_PAGE;
        int endIndex = Math.min(startIndex + LOGS_PER_PAGE, logs.size());
        
        // Display logs for this page
        if (startIndex < logs.size()) {
            for (int i = startIndex; i < endIndex; i++) {
                TransactionLog log = logs.get(i);
                int slot = i - startIndex;
                
                // Create log item
                ItemStack logItem = createLogItem(log, plugin);
                
                inventory.setItem(slot, logItem);
            }
        }
        
        // Add filter options
        ItemStack typeFilterItem = guiManager.createGuiItem(Material.HOPPER, "&e&lFilter by Type", 
                Arrays.asList(
                    "&7Current filter: &f" + (transactionType == null ? "All" : transactionType),
                    "",
                    "&7Left-click: Buy",
                    "&7Right-click: Sell",
                    "&7Shift-click: All"
                ));
        inventory.setItem(47, typeFilterItem);
        
        ItemStack shopFilterItem = guiManager.createGuiItem(Material.CHEST, "&e&lFilter by Shop", 
                Arrays.asList(
                    "&7Current filter: &f" + (shopFilter == null ? "All" : shopFilter),
                    "",
                    "&7Click to enter shop name"
                ));
        inventory.setItem(48, shopFilterItem);
        
        ItemStack playerFilterItem = guiManager.createGuiItem(Material.PLAYER_HEAD, "&e&lFilter by Player", 
                Arrays.asList(
                    "&7Current filter: &f" + (playerFilter == null ? "All" : playerFilter),
                    "",
                    "&7Click to enter player name"
                ));
        inventory.setItem(49, playerFilterItem);
        
        // Add navigation buttons
        
        // Previous page button (if not on first page)
        if (page > 1) {
            ItemStack prevButton = guiManager.createGuiItem(Material.ARROW, "&7&lPrevious Page", 
                    Collections.singletonList("&7Go to page " + (page - 1)));
            inventory.setItem(45, prevButton);
        }
        
        // Next page button (if not on last page)
        if (page < totalPages) {
            ItemStack nextButton = guiManager.createGuiItem(Material.ARROW, "&7&lNext Page", 
                    Collections.singletonList("&7Go to page " + (page + 1)));
            inventory.setItem(53, nextButton);
        }
        
        // Back button
        ItemStack backButton = guiManager.createGuiItem(Material.BARRIER, "&c&lBack", 
                Collections.singletonList("&7Return to admin menu"));
        inventory.setItem(52, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("totalPages", totalPages);
        if (transactionType != null) data.put("transactionType", transactionType);
        if (shopFilter != null) data.put("shopFilter", shopFilter);
        if (playerFilter != null) data.put("playerFilter", playerFilter);
        
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TRANSACTION_LOGS, data));
    }
    
    /**
     * Create an item stack to display a transaction log
     *
     * @param log The transaction log
     * @param plugin The plugin instance
     * @return The item stack
     */
    private static ItemStack createLogItem(TransactionLog log, FrizzlenShop plugin) {
        // Choose material based on transaction type
        Material material = log.getType().equalsIgnoreCase("buy") ? Material.EMERALD : Material.GOLD_INGOT;
        
        // Format timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = dateFormat.format(new Date(log.getTimestamp()));
        
        // Create item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = "&" + (log.getType().equalsIgnoreCase("buy") ? "a" : "6") + 
                log.getType().toUpperCase() + ": " + log.getItemName();
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Time: &f" + formattedTime);
        lore.add("&7Player: &f" + log.getPlayerName());
        lore.add("&7Shop: &f" + log.getShopName() + (log.isAdminShop() ? " &8(Admin)" : ""));
        lore.add("&7Amount: &f" + log.getAmount() + "x");
        lore.add("&7Price: &f" + plugin.getEconomyManager().formatCurrency(log.getPrice(), log.getCurrency()));
        lore.add("");
        lore.add("&7Click for more options");
        
        // Apply formatting
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(line.replace("&", "§"));
        }
        meta.setLore(coloredLore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Get a list of dummy transaction logs for testing
     *
     * @param count The number of logs to generate
     * @return The list of logs
     */
    private static List<TransactionLog> getDummyLogs(int count) {
        List<TransactionLog> logs = new ArrayList<>();
        String[] players = {"Alex", "Steve", "Herobrine", "Notch", "Jeb"};
        String[] shops = {"Central Market", "Wilderness Outpost", "Mining Supplies", "Farm Tools"};
        String[] items = {"Diamond Pickaxe", "Iron Sword", "Golden Apple", "Enchanted Book", "Bread", "Steak"};
        String[] types = {"buy", "sell"};
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            TransactionLog log = new TransactionLog();
            log.setId(UUID.randomUUID());
            log.setTimestamp(System.currentTimeMillis() - random.nextInt(1000000000)); // Random time in the past
            log.setType(types[random.nextInt(types.length)]);
            log.setAmount(random.nextInt(64) + 1);
            log.setItemName(items[random.nextInt(items.length)]);
            log.setPrice(random.nextDouble() * 100);
            log.setCurrency("coin");
            log.setShopName(shops[random.nextInt(shops.length)]);
            log.setAdminShop(random.nextBoolean());
            log.setPlayerName(players[random.nextInt(players.length)]);
            logs.add(log);
        }
        
        // Sort by timestamp (newest first)
        logs.sort(Comparator.comparing(TransactionLog::getTimestamp).reversed());
        
        return logs;
    }
    
    /**
     * A transaction log entry
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
} 