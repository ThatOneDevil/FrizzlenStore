package org.frizzlenpop.frizzlenShop.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.PlayerShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages database operations for the plugin
 */
public class DatabaseManager {

    private final FrizzlenShop plugin;
    private Connection connection;
    private String dbType;
    private String dbPath;
    
    /**
     * Creates a new database manager
     *
     * @param plugin The plugin instance
     */
    public DatabaseManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.dbType = plugin.getConfig().getString("database.type", "sqlite");
        this.dbPath = plugin.getConfig().getString("database.path", "frizzlenshop.db");
        
        // Initialize the database
        initialize();
    }
    
    /**
     * Initialize the database
     */
    private void initialize() {
        try {
            // Connect to the database
            connect();
            
            // Create tables if they don't exist
            createTables();
            
            plugin.getLogger().info("Database initialized successfully.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    
    /**
     * Connect to the database
     *
     * @throws SQLException If an error occurs
     */
    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        
        if (dbType.equalsIgnoreCase("mysql")) {
            // MySQL connection
            String host = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.name", "frizzlenshop");
            String username = plugin.getConfig().getString("database.username", "root");
            String password = plugin.getConfig().getString("database.password", "");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, username, password);
        } else {
            // SQLite connection
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + dbPath;
            connection = DriverManager.getConnection(url);
        }
    }
    
    /**
     * Create database tables if they don't exist
     *
     * @throws SQLException If an error occurs
     */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Create shops table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS shops (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(32) NOT NULL, " +
                "type VARCHAR(10) NOT NULL, " +
                "owner VARCHAR(36), " +
                "location TEXT NOT NULL, " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "is_open BOOLEAN DEFAULT 1" +
                ")"
            );
            
            // Create shop_items table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS shop_items (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "shop_id VARCHAR(36) NOT NULL, " +
                "item_data TEXT NOT NULL, " +
                "price DOUBLE NOT NULL, " +
                "stock INT DEFAULT -1, " +
                "FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create transactions table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "shop_id VARCHAR(36) NOT NULL, " +
                "player_id VARCHAR(36) NOT NULL, " +
                "item_id VARCHAR(36) NOT NULL, " +
                "quantity INT NOT NULL, " +
                "price DOUBLE NOT NULL, " +
                "type VARCHAR(10) NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (item_id) REFERENCES shop_items(id) ON DELETE CASCADE" +
                ")"
            );
        }
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
        }
    }
    
    /**
     * Save a shop to the database
     *
     * @param shop The shop to save
     * @return True if successful, false otherwise
     */
    public boolean saveShop(Shop shop) {
        try {
            connect();
            
            // Check if the shop already exists
            boolean exists = false;
            try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM shops WHERE id = ?")) {
                ps.setString(1, shop.getId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            // Insert or update the shop
            if (exists) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE shops SET name = ?, type = ?, owner = ?, location = ?, description = ?, is_open = ? WHERE id = ?")) {
                    ps.setString(1, shop.getName());
                    ps.setString(2, shop instanceof AdminShop ? "admin" : "player");
                    ps.setString(3, shop instanceof PlayerShop ? ((PlayerShop) shop).getOwner().toString() : null);
                    ps.setString(4, serializeLocation(shop.getLocation()));
                    ps.setString(5, shop.getDescription());
                    ps.setBoolean(6, shop.isOpen());
                    ps.setString(7, shop.getId().toString());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO shops (id, name, type, owner, location, description, is_open) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                    ps.setString(1, shop.getId().toString());
                    ps.setString(2, shop.getName());
                    ps.setString(3, shop instanceof AdminShop ? "admin" : "player");
                    ps.setString(4, shop instanceof PlayerShop ? ((PlayerShop) shop).getOwner().toString() : null);
                    ps.setString(5, serializeLocation(shop.getLocation()));
                    ps.setString(6, shop.getDescription());
                    ps.setBoolean(7, shop.isOpen());
                    ps.executeUpdate();
                }
            }
            
            // Save shop items
            for (ShopItem item : shop.getItems()) {
                saveShopItem(item);
            }
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save shop: " + shop.getName(), e);
            return false;
        }
    }
    
    /**
     * Save a shop item to the database
     *
     * @param item The item to save
     * @return True if successful, false otherwise
     */
    public boolean saveShopItem(ShopItem item) {
        try {
            connect();
            
            // Check if the item already exists
            boolean exists = false;
            try (PreparedStatement ps = connection.prepareStatement("SELECT id FROM shop_items WHERE id = ?")) {
                ps.setString(1, item.getId().toString());
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }
            
            // Insert or update the item
            if (exists) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE shop_items SET item_data = ?, price = ?, stock = ? WHERE id = ?")) {
                    ps.setString(1, serializeItemStack(item.getItem()));
                    ps.setDouble(2, item.getPrice());
                    ps.setInt(3, item.getStock());
                    ps.setString(4, item.getId().toString());
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO shop_items (id, shop_id, item_data, price, stock) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, item.getId().toString());
                    ps.setString(2, item.getShopId().toString());
                    ps.setString(3, serializeItemStack(item.getItem()));
                    ps.setDouble(4, item.getPrice());
                    ps.setInt(5, item.getStock());
                    ps.executeUpdate();
                }
            }
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save shop item", e);
            return false;
        }
    }
    
    /**
     * Load all shops from the database
     *
     * @return A list of shops
     */
    public List<Shop> loadShops() {
        List<Shop> shops = new ArrayList<>();
        
        try {
            connect();
            
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM shops")) {
                
                while (rs.next()) {
                    UUID id = UUID.fromString(rs.getString("id"));
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    String ownerStr = rs.getString("owner");
                    Location location = deserializeLocation(rs.getString("location"));
                    String description = rs.getString("description");
                    boolean isOpen = rs.getBoolean("is_open");
                    
                    Shop shop;
                    if (type.equals("admin")) {
                        shop = new AdminShop(id, name, location);
                    } else {
                        UUID owner = ownerStr != null ? UUID.fromString(ownerStr) : null;
                        shop = new PlayerShop(id, name, owner, location);
                    }
                    
                    shop.setDescription(description);
                    shop.setOpen(isOpen);
                    
                    // Load shop items
                    loadShopItems(shop);
                    
                    shops.add(shop);
                }
            }
            
            return shops;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load shops", e);
            return shops;
        }
    }
    
    /**
     * Load items for a shop from the database
     *
     * @param shop The shop to load items for
     */
    private void loadShopItems(Shop shop) {
        try {
            connect();
            
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM shop_items WHERE shop_id = ?")) {
                ps.setString(1, shop.getId().toString());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        ItemStack item = deserializeItemStack(rs.getString("item_data"));
                        double price = rs.getDouble("price");
                        int stock = rs.getInt("stock");
                        
                        ShopItem shopItem = new ShopItem(id, shop.getId(), item, price);
                        shopItem.setStock(stock);
                        
                        shop.addItem(shopItem);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load shop items for shop: " + shop.getName(), e);
        }
    }
    
    /**
     * Record a transaction in the database
     *
     * @param shopId The shop ID
     * @param playerId The player ID
     * @param itemId The item ID
     * @param quantity The quantity
     * @param price The price
     * @param type The transaction type (buy/sell)
     * @return True if successful, false otherwise
     */
    public boolean recordTransaction(UUID shopId, UUID playerId, UUID itemId, int quantity, double price, String type) {
        try {
            connect();
            
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transactions (id, shop_id, player_id, item_id, quantity, price, type) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, shopId.toString());
                ps.setString(3, playerId.toString());
                ps.setString(4, itemId.toString());
                ps.setInt(5, quantity);
                ps.setDouble(6, price);
                ps.setString(7, type);
                ps.executeUpdate();
            }
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to record transaction", e);
            return false;
        }
    }
    
    /**
     * Get transactions for a shop
     *
     * @param shopId The shop ID
     * @param limit The maximum number of transactions to return
     * @param offset The offset for pagination
     * @return A list of transactions
     */
    public List<Transaction> getTransactions(UUID shopId, int limit, int offset) {
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            connect();
            
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM transactions WHERE shop_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?")) {
                ps.setString(1, shopId.toString());
                ps.setInt(2, limit);
                ps.setInt(3, offset);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        UUID playerId = UUID.fromString(rs.getString("player_id"));
                        UUID itemId = UUID.fromString(rs.getString("item_id"));
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        String type = rs.getString("type");
                        String timestamp = rs.getString("timestamp");
                        
                        Transaction transaction = new Transaction(id, shopId, playerId, itemId, quantity, price, type, timestamp);
                        transactions.add(transaction);
                    }
                }
            }
            
            return transactions;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get transactions for shop: " + shopId, e);
            return transactions;
        }
    }
    
    /**
     * Serialize a location to a string
     *
     * @param location The location
     * @return The serialized location
     */
    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," +
               location.getX() + "," +
               location.getY() + "," +
               location.getZ() + "," +
               location.getYaw() + "," +
               location.getPitch();
    }
    
    /**
     * Deserialize a location from a string
     *
     * @param serialized The serialized location
     * @return The location
     */
    private Location deserializeLocation(String serialized) {
        String[] parts = serialized.split(",");
        return new Location(
            plugin.getServer().getWorld(parts[0]),
            Double.parseDouble(parts[1]),
            Double.parseDouble(parts[2]),
            Double.parseDouble(parts[3]),
            Float.parseFloat(parts[4]),
            Float.parseFloat(parts[5])
        );
    }
    
    /**
     * Serialize an ItemStack to a string
     *
     * @param item The item
     * @return The serialized item
     */
    private String serializeItemStack(ItemStack item) {
        // This is a simplified implementation
        // In a real plugin, you'd use Bukkit's serialization or a library like XStream
        return item.getType().name() + "," + item.getAmount();
    }
    
    /**
     * Deserialize an ItemStack from a string
     *
     * @param serialized The serialized item
     * @return The item
     */
    private ItemStack deserializeItemStack(String serialized) {
        // This is a simplified implementation
        String[] parts = serialized.split(",");
        Material material = Material.valueOf(parts[0]);
        int amount = Integer.parseInt(parts[1]);
        return new ItemStack(material, amount);
    }
    
    /**
     * Represents a transaction
     */
    public static class Transaction {
        private final UUID id;
        private final UUID shopId;
        private final UUID playerId;
        private final UUID itemId;
        private final int quantity;
        private final double price;
        private final String type;
        private final String timestamp;
        
        /**
         * Creates a new transaction
         *
         * @param id The transaction ID
         * @param shopId The shop ID
         * @param playerId The player ID
         * @param itemId The item ID
         * @param quantity The quantity
         * @param price The price
         * @param type The transaction type
         * @param timestamp The timestamp
         */
        public Transaction(UUID id, UUID shopId, UUID playerId, UUID itemId, int quantity, double price, String type, String timestamp) {
            this.id = id;
            this.shopId = shopId;
            this.playerId = playerId;
            this.itemId = itemId;
            this.quantity = quantity;
            this.price = price;
            this.type = type;
            this.timestamp = timestamp;
        }
        
        /**
         * Get the transaction ID
         *
         * @return The transaction ID
         */
        public UUID getId() {
            return id;
        }
        
        /**
         * Get the shop ID
         *
         * @return The shop ID
         */
        public UUID getShopId() {
            return shopId;
        }
        
        /**
         * Get the player ID
         *
         * @return The player ID
         */
        public UUID getPlayerId() {
            return playerId;
        }
        
        /**
         * Get the item ID
         *
         * @return The item ID
         */
        public UUID getItemId() {
            return itemId;
        }
        
        /**
         * Get the quantity
         *
         * @return The quantity
         */
        public int getQuantity() {
            return quantity;
        }
        
        /**
         * Get the price
         *
         * @return The price
         */
        public double getPrice() {
            return price;
        }
        
        /**
         * Get the transaction type
         *
         * @return The transaction type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get the timestamp
         *
         * @return The timestamp
         */
        public String getTimestamp() {
            return timestamp;
        }
    }
} 