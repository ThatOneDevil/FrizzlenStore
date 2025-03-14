package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.DatabaseManager;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Analyzes market trends and calculates dynamic prices based on supply and demand
 */
public class MarketAnalyzer {

    private final FrizzlenShop plugin;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    
    // Cache market data for performance
    private Map<Material, MarketData> marketDataCache;
    private Map<UUID, ItemTransactionData> itemTransactionCache;
    
    // Recent transaction tracking
    private Map<Material, Integer> recentBuys = new ConcurrentHashMap<>();
    private Map<Material, Integer> recentSells = new ConcurrentHashMap<>();
    
    // Market volatility factors (how quickly prices change)
    private static final double DEFAULT_VOLATILITY = 0.05; // 5% change per cycle
    private static final int ANALYSIS_PERIOD_DAYS = 7; // Analyze data from the last 7 days
    
    // Config-based values
    private double volatilityMultiplier;
    private double maxPriceChange;
    private double normalizationRate;
    private long analysisIntervalMinutes;
    private boolean useCraftingRelationships;
    private boolean fluctuationEnabled;
    private double fluctuationMagnitude;
    
    /**
     * Creates a new market analyzer
     * 
     * @param plugin The plugin instance
     */
    public MarketAnalyzer(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.configManager = plugin.getConfigManager();
        this.marketDataCache = new ConcurrentHashMap<>();
        this.itemTransactionCache = new ConcurrentHashMap<>();
        
        // Load config values
        this.volatilityMultiplier = configManager.getVolatilityMultiplier();
        this.maxPriceChange = configManager.getMaxPriceChange();
        this.normalizationRate = configManager.getNormalizationRate();
        this.analysisIntervalMinutes = configManager.getAnalysisInterval();
        this.useCraftingRelationships = configManager.useCraftingRelationships();
        this.fluctuationEnabled = configManager.isPriceFluctuationEnabled();
        this.fluctuationMagnitude = configManager.getFluctuationMagnitude();
        
        // Initialize the database tables
        initializeDatabase();
        
        // Log initialization
        plugin.getLogger().info("Market analyzer initialized with volatility: " + volatilityMultiplier + 
                                ", max price change: " + maxPriceChange +
                                ", analysis interval: " + analysisIntervalMinutes + " minutes" +
                                ", fluctuation: " + (fluctuationEnabled ? "enabled" : "disabled"));
    }
    
    /**
     * Initializes the database tables required for market analysis
     */
    private void initializeDatabase() {
        try {
            // Create market_trends table
            String createMarketTrendsTable = "CREATE TABLE IF NOT EXISTS " 
                + databaseManager.getTablePrefix() + "market_trends ("
                + "material VARCHAR(64) PRIMARY KEY, "
                + "demand_index DOUBLE, "
                + "supply_index DOUBLE, "
                + "volatility DOUBLE, "
                + "last_updated BIGINT)";
            
            // Create item_transactions table
            String createItemTransactionsTable = "CREATE TABLE IF NOT EXISTS " 
                + databaseManager.getTablePrefix() + "item_transactions ("
                + "item_id VARCHAR(36) PRIMARY KEY, "
                + "material VARCHAR(64), "
                + "buy_count INT, "
                + "sell_count INT, "
                + "last_buy_time BIGINT, "
                + "last_sell_time BIGINT, "
                + "price_adjustment_factor DOUBLE)";
            
            // Execute queries
            Connection conn = databaseManager.getConnection();
            databaseManager.executeUpdate(conn, createMarketTrendsTable);
            databaseManager.executeUpdate(conn, createItemTransactionsTable);
            conn.close();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize market analyzer database tables", e);
        }
    }
    
    /**
     * Records a transaction for market analysis
     * 
     * @param item The shop item that was transacted
     * @param quantity The quantity that was transacted
     * @param isBuy True if this was a buy transaction (player buying from shop), false for sell
     */
    public void recordTransaction(ShopItem item, int quantity, boolean isBuy) {
        if (item == null || quantity <= 0) {
            return;
        }
        
        UUID itemId = item.getId();
        Material material = item.getItem().getType();
        String materialName = material.toString();
        
        // Record the transaction for this item
        try {
            Connection conn = databaseManager.getConnection();
            
            // Update item transaction data
            updateItemTransactionData(conn, itemId, material, quantity, isBuy);
            
            // Update market trends
            updateMarketTrends(conn, material, quantity, isBuy);
            
            // Propagate effects to components if this is a crafted item
            if (item.isCrafted()) {
                Map<Material, Integer> components = item.getCraftingComponents();
                double componentMultiplier = item.getCraftingMultiplier();
                
                for (Map.Entry<Material, Integer> entry : components.entrySet()) {
                    Material componentMaterial = entry.getKey();
                    int componentQuantity = entry.getValue() * quantity;
                    
                    // Scale the effect based on component multiplier
                    int scaledQuantity = (int)(componentQuantity * componentMultiplier);
                    if (scaledQuantity > 0) {
                        // We propagate the OPPOSITE transaction type to components
                        // If an item is bought (demand increases), then demand for components increases (as if they were sold)
                        updateMarketTrends(conn, componentMaterial, scaledQuantity, !isBuy);
                    }
                }
            }
            
            conn.close();
            
            // Cache this transaction in memory for quick access
            if (material.isBlock() || material.isItem()) {
                synchronized (this) {
                    if (isBuy) {
                        recentBuys.put(material, recentBuys.getOrDefault(material, 0) + quantity);
                    } else {
                        recentSells.put(material, recentSells.getOrDefault(material, 0) + quantity);
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error recording transaction: " + e.getMessage());
        }
    }
    
    /**
     * Updates the item transaction data in the database
     * 
     * @param conn The database connection
     * @param itemId The UUID of the shop item
     * @param material The material of the item
     * @param quantity The quantity that was transacted
     * @param isBuy Whether this was a buy transaction
     * @throws SQLException If a database error occurs
     */
    private void updateItemTransactionData(Connection conn, UUID itemId, Material material, int quantity, boolean isBuy) throws SQLException {
        // Check if item already exists in database
        String query = "SELECT * FROM " + databaseManager.getTablePrefix() + "item_transactions WHERE item_id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, itemId.toString());
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            // Update existing entry
            String update = "UPDATE " + databaseManager.getTablePrefix() + "item_transactions SET "
                + (isBuy ? "buy_count = buy_count + ?, last_buy_time = ?" : "sell_count = sell_count + ?, last_sell_time = ?")
                + " WHERE item_id = ?";
            PreparedStatement updatePs = conn.prepareStatement(update);
            updatePs.setInt(1, quantity);
            updatePs.setLong(2, System.currentTimeMillis());
            updatePs.setString(3, itemId.toString());
            updatePs.executeUpdate();
            updatePs.close();
        } else {
            // Insert new entry
            String insert = "INSERT INTO " + databaseManager.getTablePrefix() + "item_transactions "
                + "(item_id, material, buy_count, sell_count, last_buy_time, last_sell_time, price_adjustment_factor) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insert);
            insertPs.setString(1, itemId.toString());
            insertPs.setString(2, material.toString());
            insertPs.setInt(3, isBuy ? quantity : 0);
            insertPs.setInt(4, isBuy ? 0 : quantity);
            insertPs.setLong(5, isBuy ? System.currentTimeMillis() : 0);
            insertPs.setLong(6, isBuy ? 0 : System.currentTimeMillis());
            insertPs.setDouble(7, 1.0); // Start with neutral adjustment factor
            insertPs.executeUpdate();
            insertPs.close();
        }
        
        rs.close();
        ps.close();
    }
    
    /**
     * Updates the global market trends for a material
     * 
     * @param conn The database connection
     * @param material The material being updated
     * @param quantity The quantity that was transacted
     * @param isBuy Whether this was a buy transaction
     * @throws SQLException If a database error occurs
     */
    private void updateMarketTrends(Connection conn, Material material, int quantity, boolean isBuy) throws SQLException {
        String materialName = material.toString();
        long currentTime = System.currentTimeMillis();
        
        // Check if material already exists in database
        String query = "SELECT * FROM " + databaseManager.getTablePrefix() + "market_trends WHERE material = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, materialName);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            // Update existing entry
            double demandIndex = rs.getDouble("demand_index");
            double supplyIndex = rs.getDouble("supply_index");
            
            // Adjust indices based on transaction
            if (isBuy) {
                // Buying increases demand
                demandIndex = Math.min(2.0, demandIndex + (quantity * 0.01));
                
                // If demand is high, supply might decrease slightly
                supplyIndex = Math.max(0.5, supplyIndex - (quantity * 0.002));
            } else {
                // Selling increases supply
                supplyIndex = Math.min(2.0, supplyIndex + (quantity * 0.01));
                
                // If supply is high, demand might decrease slightly
                demandIndex = Math.max(0.5, demandIndex - (quantity * 0.002));
            }
            
            // Update database
            String update = "UPDATE " + databaseManager.getTablePrefix() + "market_trends SET "
                + "demand_index = ?, supply_index = ?, last_updated = ? WHERE material = ?";
            PreparedStatement updatePs = conn.prepareStatement(update);
            updatePs.setDouble(1, demandIndex);
            updatePs.setDouble(2, supplyIndex);
            updatePs.setLong(3, currentTime);
            updatePs.setString(4, materialName);
            updatePs.executeUpdate();
            updatePs.close();
        } else {
            // Insert new entry with default values
            double demandIndex = 1.0; // Neutral demand
            double supplyIndex = 1.0; // Neutral supply
            
            // Adjust based on the first transaction
            if (isBuy) {
                demandIndex += (quantity * 0.01);
            } else {
                supplyIndex += (quantity * 0.01);
            }
            
            String insert = "INSERT INTO " + databaseManager.getTablePrefix() + "market_trends "
                + "(material, demand_index, supply_index, volatility, last_updated) "
                + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insert);
            insertPs.setString(1, materialName);
            insertPs.setDouble(2, demandIndex);
            insertPs.setDouble(3, supplyIndex);
            insertPs.setDouble(4, DEFAULT_VOLATILITY);
            insertPs.setLong(5, currentTime);
            insertPs.executeUpdate();
            insertPs.close();
        }
        
        rs.close();
        ps.close();
    }
    
    /**
     * Calculates a dynamic price for an item, taking into account its components if it's a crafted item
     * 
     * @param basePrice The static base price for the item
     * @param item The shop item
     * @param isBuyPrice Whether this is a buy price calculation (true) or sell price (false)
     * @return The calculated dynamic price
     */
    public double calculateDynamicPrice(double basePrice, ShopItem item, boolean isBuyPrice) {
        // Don't process null items
        if (item == null) {
            return basePrice;
        }
        
        // Get the material for this item
        Material material = item.getItem().getType();
        
        // Get market data for this material
        MarketData marketData = getMarketData(material);
        
        // If no market data, return base price
        if (marketData == null) {
            return basePrice;
        }
        
        // Calculate market multiplier based on supply and demand
        double demandIndex = marketData.getDemandIndex();
        double supplyIndex = marketData.getSupplyIndex();
        double marketMultiplier;
        
        if (isBuyPrice) {
            // For buy prices, higher demand increases price, higher supply decreases price
            marketMultiplier = (demandIndex / supplyIndex);
        } else {
            // For sell prices, higher supply decreases price, higher demand increases price
            marketMultiplier = (demandIndex / supplyIndex) * 0.8; // Sell prices are generally lower
        }
        
        // Apply crafting relationships if enabled
        double craftMultiplier = 1.0;
        if (useCraftingRelationships && item.isCrafted()) {
            // Get crafting multiplier based on component costs
            craftMultiplier = getCraftingMultiplier(item);
        }
        
        // Apply natural price fluctuation if enabled
        double fluctuation = 0.0;
        if (fluctuationEnabled) {
            // Random fluctuation within the configured magnitude
            fluctuation = (Math.random() * 2 - 1) * fluctuationMagnitude;
        }
        
        // Calculate final price
        double adjustedPrice = basePrice * marketMultiplier * craftMultiplier * (1 + fluctuation);
        
        // Limit maximum price change based on config
        double minPrice = basePrice * (1 - maxPriceChange);
        double maxPrice = basePrice * (1 + maxPriceChange);
        
        // Ensure price stays within limits
        return Math.max(minPrice, Math.min(maxPrice, adjustedPrice));
    }
    
    /**
     * Calculates a price fluctuation factor based on time
     * 
     * @param material The material to calculate for
     * @return The fluctuation factor
     */
    private double calculateFluctuationFactor(Material material) {
        // Use a sine wave to create natural price fluctuations over time
        // Each material has a slightly different phase to ensure prices don't all move together
        double phase = material.ordinal() % 10 * 0.1;
        double dayFraction = (System.currentTimeMillis() % 86400000) / 86400000.0;
        double fluctuation = Math.sin(2 * Math.PI * (dayFraction + phase));
        
        // Get the market volatility
        MarketData marketData = getMarketData(material);
        double volatility = marketData != null ? marketData.getVolatility() : DEFAULT_VOLATILITY;
        
        // Calculate the fluctuation factor (range: 1-volatility to 1+volatility)
        return 1.0 + (fluctuation * volatility);
    }
    
    /**
     * Gets the market data for a material
     * 
     * @param material The material to get data for
     * @return The market data, or null if not found
     */
    public MarketData getMarketData(Material material) {
        // Check cache first
        if (marketDataCache.containsKey(material)) {
            return marketDataCache.get(material);
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            String query = "SELECT * FROM " + databaseManager.getTablePrefix() + "market_trends WHERE material = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, material.toString());
            ResultSet rs = ps.executeQuery();
            
            MarketData data = null;
            if (rs.next()) {
                data = new MarketData(
                    material,
                    rs.getDouble("demand_index"),
                    rs.getDouble("supply_index"),
                    rs.getDouble("volatility"),
                    rs.getLong("last_updated")
                );
                
                // Cache the data
                marketDataCache.put(material, data);
            }
            
            rs.close();
            ps.close();
            conn.close();
            
            return data;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to retrieve market data for " + material, e);
            return null;
        }
    }
    
    /**
     * Gets the transaction data for a specific shop item
     * 
     * @param itemId The UUID of the shop item
     * @return The transaction data, or null if not found
     */
    public ItemTransactionData getItemTransactionData(UUID itemId) {
        // Check cache first
        if (itemTransactionCache.containsKey(itemId)) {
            return itemTransactionCache.get(itemId);
        }
        
        try {
            Connection conn = databaseManager.getConnection();
            String query = "SELECT * FROM " + databaseManager.getTablePrefix() + "item_transactions WHERE item_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, itemId.toString());
            ResultSet rs = ps.executeQuery();
            
            ItemTransactionData data = null;
            if (rs.next()) {
                data = new ItemTransactionData(
                    UUID.fromString(rs.getString("item_id")),
                    Material.valueOf(rs.getString("material")),
                    rs.getInt("buy_count"),
                    rs.getInt("sell_count"),
                    rs.getLong("last_buy_time"),
                    rs.getLong("last_sell_time"),
                    rs.getDouble("price_adjustment_factor")
                );
                
                // Cache the data
                itemTransactionCache.put(itemId, data);
            }
            
            rs.close();
            ps.close();
            conn.close();
            
            return data;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to retrieve transaction data for item " + itemId, e);
            return null;
        }
    }
    
    /**
     * Performs a full market analysis to update prices based on trends
     * Should be called periodically (e.g., once per day)
     */
    public void performMarketAnalysis() {
        if (!plugin.getConfigManager().isDynamicPricingEnabled()) {
            return; // Dynamic pricing is disabled
        }
        
        plugin.getLogger().info("Performing market analysis for dynamic pricing...");
        
        try {
            Connection conn = databaseManager.getConnection();
            
            // Get all materials from the database
            String query = "SELECT material FROM " + databaseManager.getTablePrefix() + "market_trends";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            long currentTime = System.currentTimeMillis();
            List<String> materials = new ArrayList<>();
            
            while (rs.next()) {
                materials.add(rs.getString("material"));
            }
            
            rs.close();
            ps.close();
            
            // Analyze each material
            for (String materialName : materials) {
                try {
                    Material material = Material.valueOf(materialName);
                    normalizeMarketData(conn, material, currentTime);
                } catch (IllegalArgumentException e) {
                    // Skip invalid materials
                    continue;
                }
            }
            
            conn.close();
            
            // Clear caches to ensure fresh data is loaded
            marketDataCache.clear();
            
            plugin.getLogger().info("Market analysis completed for " + materials.size() + " materials");
            
        } catch (SQLException e) {
            plugin.getLogger().severe("Error performing market analysis: " + e.getMessage());
        }
    }
    
    /**
     * Normalizes market data for a material over time
     * This gradually returns prices to baseline if there's no activity
     * 
     * @param conn The database connection
     * @param material The material to normalize
     * @param currentTime The current time in milliseconds
     * @throws SQLException If a database error occurs
     */
    private void normalizeMarketData(Connection conn, Material material, long currentTime) throws SQLException {
        String query = "SELECT * FROM " + databaseManager.getTablePrefix() + "market_trends WHERE material = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, material.toString());
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            double demandIndex = rs.getDouble("demand_index");
            double supplyIndex = rs.getDouble("supply_index");
            long lastUpdated = rs.getLong("last_updated");
            
            // Calculate how long it's been since the last update
            // The longer it's been, the more we normalize
            double daysSinceUpdate = (currentTime - lastUpdated) / (1000.0 * 60 * 60 * 24);
            
            // Skip recent updates
            if (daysSinceUpdate < 0.5) { // Less than 12 hours
                return;
            }
            
            // Calculate normalization factor based on time and config
            double normalizationFactor = daysSinceUpdate * normalizationRate;
            
            // Limit the maximum normalization
            normalizationFactor = Math.min(normalizationFactor, 0.5);
            
            // Normalize demand and supply indices towards 1.0 (neutral)
            demandIndex = demandIndex + normalizationFactor * (1.0 - demandIndex);
            supplyIndex = supplyIndex + normalizationFactor * (1.0 - supplyIndex);
            
            // Update the database
            String update = "UPDATE " + databaseManager.getTablePrefix() + "market_trends SET " +
                    "demand_index = ?, supply_index = ?, last_updated = ? WHERE material = ?";
            PreparedStatement updatePs = conn.prepareStatement(update);
            updatePs.setDouble(1, demandIndex);
            updatePs.setDouble(2, supplyIndex);
            updatePs.setLong(3, currentTime);
            updatePs.setString(4, material.toString());
            updatePs.executeUpdate();
            updatePs.close();
        }
        
        rs.close();
        ps.close();
    }
    
    /**
     * Gets a summary of current market trends
     * 
     * @return A map containing material to price trend (positive = rising, negative = falling)
     */
    public Map<Material, Double> getMarketTrendSummary() {
        Map<Material, Double> trends = new HashMap<>();
        
        try {
            Connection conn = databaseManager.getConnection();
            String query = "SELECT material, demand_index, supply_index FROM " 
                + databaseManager.getTablePrefix() + "market_trends";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String materialName = rs.getString("material");
                double demandIndex = rs.getDouble("demand_index");
                double supplyIndex = rs.getDouble("supply_index");
                
                try {
                    Material material = Material.valueOf(materialName);
                    
                    // Calculate price trend (-1.0 to 1.0, where positive means rising price)
                    double trend = demandIndex - supplyIndex;
                    trends.put(material, trend);
                } catch (IllegalArgumentException e) {
                    // Invalid material name, ignore
                }
            }
            
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get market trend summary", e);
        }
        
        return trends;
    }
    
    /**
     * Gets the top trending items (highest price increases)
     * 
     * @param limit The maximum number of items to return
     * @return A map of materials to price trend values, sorted by highest trend
     */
    public Map<Material, Double> getTopTrendingItems(int limit) {
        Map<Material, Double> trends = getMarketTrendSummary();
        
        // Sort by trend value (highest first)
        return trends.entrySet().stream()
            .sorted(Map.Entry.<Material, Double>comparingByValue().reversed())
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (e1, e2) -> e1, 
                HashMap::new
            ));
    }
    
    /**
     * Clears the trend data after prices have been updated
     * This prevents the same trends from affecting prices again
     */
    public void clearTrendData() {
        try {
            Connection conn = databaseManager.getConnection();
            
            // Reset all demand and supply indices to 1.0 (neutral)
            String update = "UPDATE " + databaseManager.getTablePrefix() + "market_trends " +
                            "SET demand_index = 1.0, supply_index = 1.0";
            
            PreparedStatement ps = conn.prepareStatement(update);
            ps.executeUpdate();
            ps.close();
            conn.close();
            
            // Also clear the cache
            marketDataCache.clear();
            
            plugin.getLogger().info("Market trend data has been cleared after price updates");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to clear market trend data", e);
        }
    }
    
    /**
     * Represents market data for a specific material
     */
    public static class MarketData {
        private final Material material;
        private final double demandIndex;
        private final double supplyIndex;
        private final double volatility;
        private final long lastUpdated;
        
        public MarketData(Material material, double demandIndex, double supplyIndex, double volatility, long lastUpdated) {
            this.material = material;
            this.demandIndex = demandIndex;
            this.supplyIndex = supplyIndex;
            this.volatility = volatility;
            this.lastUpdated = lastUpdated;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public double getDemandIndex() {
            return demandIndex;
        }
        
        public double getSupplyIndex() {
            return supplyIndex;
        }
        
        public double getVolatility() {
            return volatility;
        }
        
        public long getLastUpdated() {
            return lastUpdated;
        }
    }
    
    /**
     * Represents transaction data for a specific shop item
     */
    public static class ItemTransactionData {
        private final UUID itemId;
        private final Material material;
        private final int buyCount;
        private final int sellCount;
        private final long lastBuyTime;
        private final long lastSellTime;
        private final double priceAdjustmentFactor;
        
        public ItemTransactionData(UUID itemId, Material material, int buyCount, int sellCount, 
                                  long lastBuyTime, long lastSellTime, double priceAdjustmentFactor) {
            this.itemId = itemId;
            this.material = material;
            this.buyCount = buyCount;
            this.sellCount = sellCount;
            this.lastBuyTime = lastBuyTime;
            this.lastSellTime = lastSellTime;
            this.priceAdjustmentFactor = priceAdjustmentFactor;
        }
        
        public UUID getItemId() {
            return itemId;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public int getBuyCount() {
            return buyCount;
        }
        
        public int getSellCount() {
            return sellCount;
        }
        
        public long getLastBuyTime() {
            return lastBuyTime;
        }
        
        public long getLastSellTime() {
            return lastSellTime;
        }
        
        public double getPriceAdjustmentFactor() {
            return priceAdjustmentFactor;
        }
    }

    /**
     * Gets the current base price for a material
     * 
     * @param material The material to get the price for
     * @return The base price for the material
     */
    public double getBasePrice(Material material) {
        // First check if we have a configured base price
        double configPrice = plugin.getConfigManager().getMaterialBasePrice(material);
        if (configPrice > 0) {
            return configPrice;
        }
        
        // If no configured price, use a default algorithm based on material properties
        if (material.isEdible()) {
            return 5.0 + (material.getMaxDurability() * 0.5);
        } else if (material.getMaxDurability() > 0) {
            return 10.0 + (material.getMaxDurability() * 2.0);
        } else {
            // Basic classification for common materials
            switch (material) {
                case DIAMOND:
                    return 1000.0;
                case GOLD_INGOT:
                    return 250.0;
                case IRON_INGOT:
                    return 100.0;
                case EMERALD:
                    return 500.0;
                case REDSTONE:
                    return 10.0;
                case LAPIS_LAZULI:
                    return 20.0;
                case COAL:
                    return 5.0;
                case STICK:
                    return 1.0;
                case COBBLESTONE:
                    return 1.0;
                case OAK_LOG:
                case SPRUCE_LOG:
                case BIRCH_LOG:
                case JUNGLE_LOG:
                case ACACIA_LOG:
                case DARK_OAK_LOG:
                    return 5.0;
                default:
                    return 20.0; // Default fallback price
            }
        }
    }

    /**
     * Gets a suggested price for a player shop item based on market data
     * 
     * @param material The material to get a suggested price for
     * @param isBuyPrice Whether this is for a buy price or sell price
     * @return The suggested price
     */
    public double getSuggestedPrice(Material material, boolean isBuyPrice) {
        // Get base price
        double basePrice = getBasePrice(material);
        
        // Get market data
        MarketData marketData = getMarketData(material);
        
        // Calculate adjustment based on market trends
        double demandIndex = marketData.getDemandIndex();
        double supplyIndex = marketData.getSupplyIndex();
        
        // Different calculations for buy and sell prices
        double suggestedPrice;
        if (isBuyPrice) {
            // Buy price increases with demand, decreases with supply
            suggestedPrice = basePrice * (1 + (demandIndex - 0.5) * 0.4 - (supplyIndex - 0.5) * 0.3);
        } else {
            // Sell price increases with supply, decreases with demand
            suggestedPrice = basePrice * 0.8 * (1 - (demandIndex - 0.5) * 0.3 + (supplyIndex - 0.5) * 0.4);
        }
        
        // Add a small random factor for variety (±5%)
        double randomFactor = 0.95 + Math.random() * 0.1;
        suggestedPrice *= randomFactor;
        
        // Round to nearest 0.5
        return Math.round(suggestedPrice * 2) / 2.0;
    }

    /**
     * Gets the profit margin for crafting an item
     * This shows whether it's profitable to craft an item from components
     * 
     * @param material The crafted item material
     * @return The profit margin percentage (positive means profitable), or 0 if not applicable
     */
    public double getCraftingProfitMargin(Material material) {
        // Create a dummy item to check crafting info
        ItemStack itemStack = new ItemStack(material);
        ShopItem dummyItem = new ShopItem(itemStack, 0.0, 0.0, "coin", -1);
        
        if (!dummyItem.isCrafted()) {
            return 0.0;
        }
        
        double craftCost = dummyItem.getCraftingValue();
        double marketValue = getSuggestedPrice(material, true);
        
        if (craftCost <= 0 || marketValue <= 0) {
            return 0.0;
        }
        
        return ((marketValue - craftCost) / craftCost) * 100.0;
    }

    /**
     * Determines if a price should change based on transaction quantity and volatility
     * 
     * @param quantity The quantity of items being transacted
     * @param material The material type
     * @return True if the price should be updated
     */
    public boolean shouldUpdatePrice(int quantity, Material material) {
        // Always update prices for transactions of 16 or more items
        if (quantity >= 16) {
            return true;
        }
        
        // Get material volatility (some materials change price more easily)
        double materialVolatility = getMaterialVolatility(material);
        
        // Combine material volatility with config volatility
        double effectiveVolatility = materialVolatility * volatilityMultiplier;
        
        // Calculate probability of price change based on quantity and volatility
        // Higher quantities and volatility increase chance of price change
        double changeChance = Math.min(1.0, quantity * effectiveVolatility * 0.1);
        
        // Return true with probability equal to changeChance
        return Math.random() < changeChance;
    }
    
    /**
     * Gets the volatility factor for a specific material
     * Some materials are more volatile than others
     * 
     * @param material The material to check
     * @return The volatility factor (higher = more volatile)
     */
    private double getMaterialVolatility(Material material) {
        // Get from database if available
        MarketData marketData = getMarketData(material);
        if (marketData != null && marketData.getVolatility() > 0) {
            return marketData.getVolatility();
        }
        
        // Default volatilities for material categories
        String name = material.name();
        
        // Rare materials are more volatile
        if (name.contains("DIAMOND") || name.contains("EMERALD") || 
            name.contains("NETHERITE") || name.contains("BEACON")) {
            return 2.0;
        }
        
        // Moderately rare materials
        if (name.contains("GOLD") || name.contains("LAPIS") || 
            name.contains("ENDER") || name.contains("BLAZE")) {
            return 1.5;
        }
        
        // Common materials are less volatile
        if (name.contains("STONE") || name.contains("DIRT") || 
            name.contains("SAND") || name.contains("WOOD")) {
            return 0.5;
        }
        
        // Default volatility
        return 1.0;
    }

    /**
     * Updates market data with the configuration values
     * Call this when configuration changes to ensure market analysis uses the latest settings
     */
    public void updateConfigSettings() {
        this.volatilityMultiplier = configManager.getVolatilityMultiplier();
        this.maxPriceChange = configManager.getMaxPriceChange();
        this.normalizationRate = configManager.getNormalizationRate();
        this.analysisIntervalMinutes = configManager.getAnalysisInterval();
        this.useCraftingRelationships = configManager.useCraftingRelationships();
        this.fluctuationEnabled = configManager.isPriceFluctuationEnabled();
        this.fluctuationMagnitude = configManager.getFluctuationMagnitude();
        
        plugin.getLogger().info("Market analyzer settings updated");
    }

    /**
     * Gets a multiplier based on the crafting cost of an item
     * 
     * @param item The shop item
     * @return A multiplier for the price based on crafting costs
     */
    private double getCraftingMultiplier(ShopItem item) {
        if (!item.isCrafted()) {
            return 1.0;
        }
        
        // Get the components and their costs
        Map<Material, Integer> components = item.getCraftingComponents();
        if (components == null || components.isEmpty()) {
            return 1.0;
        }
        
        // Calculate the base cost of crafting
        double craftingCost = 0.0;
        for (Map.Entry<Material, Integer> entry : components.entrySet()) {
            Material componentMaterial = entry.getKey();
            int quantity = entry.getValue();
            
            // Get component's market value
            MarketData componentData = getMarketData(componentMaterial);
            double componentPrice = plugin.getShopManager().getDefaultBuyPrice(new ItemStack(componentMaterial));
            
            // Adjust component price by market data if available
            if (componentData != null) {
                double demandIndex = componentData.getDemandIndex();
                double supplyIndex = componentData.getSupplyIndex();
                componentPrice *= (demandIndex / supplyIndex);
            }
            
            // Add to total cost
            craftingCost += componentPrice * quantity;
        }
        
        // Add crafting fee (15% of component cost)
        craftingCost *= 1.15;
        
        // Calculate the multiplier based on the relation between crafting cost and item's base price
        double basePrice = plugin.getShopManager().getDefaultBuyPrice(item.getItem());
        
        if (basePrice <= 0) {
            return 1.0;
        }
        
        // If crafting cost is higher than base price, increase the multiplier
        // If crafting cost is lower than base price, decrease the multiplier
        double multiplier = craftingCost / basePrice;
        
        // Limit extreme values
        return Math.max(0.8, Math.min(1.5, multiplier));
    }

    /**
     * Clears the cache for a specific material
     * Used when resetting pricing data for a material
     * 
     * @param material The material to clear from cache
     */
    public void clearCacheForMaterial(Material material) {
        if (material != null) {
            // Remove from market data cache
            marketDataCache.remove(material);
            
            // Log the cache clear
            plugin.getLogger().info("Cleared market data cache for " + material.name());
        }
    }
} 