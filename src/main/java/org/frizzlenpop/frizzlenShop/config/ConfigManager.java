package org.frizzlenpop.frizzlenShop.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class ConfigManager {

    private final FrizzlenShop plugin;
    private FileConfiguration config;

    public ConfigManager(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    /**
     * Gets the configuration object
     * 
     * @return The file configuration
     */
    public FileConfiguration getConfig() {
        return config;
    }

    public double getDefaultTaxRate() {
        return config.getDouble("general.default-tax-rate", 5.0);
    }

    public String getDefaultCurrency() {
        return config.getString("general.default-currency", "dollars");
    }

    public boolean isMaintenanceMode() {
        return config.getBoolean("general.maintenance-mode", false);
    }

    public double getShopCreationCost() {
        return config.getDouble("general.shop-creation-cost", 1000.0);
    }

    public int getMaxShopsPerPlayer() {
        return config.getInt("general.max-shops-per-player", 3);
    }

    public boolean areAdminShopsEnabled() {
        return config.getBoolean("admin-shops.enabled", true);
    }

    public boolean haveAdminShopsInfiniteStock() {
        return config.getBoolean("admin-shops.infinite-stock", true);
    }

    public double getAdminShopTaxRate() {
        return config.getDouble("admin-shops.tax-rate", 3.0);
    }

    public String getAdminShopPrefix() {
        return config.getString("admin-shops.prefix", "&c[Admin] ");
    }

    public boolean arePlayerShopsEnabled() {
        return config.getBoolean("player-shops.enabled", true);
    }

    public double getPlayerShopTaxRate() {
        return config.getDouble("player-shops.tax-rate", 10.0);
    }

    public int getMaxItemsPerPlayerShop() {
        return config.getInt("player-shops.max-items", 27);
    }

    public int getShopRentalPeriod() {
        return config.getInt("player-shops.rental-period", 7);
    }

    public double getShopRentalCost() {
        return config.getDouble("player-shops.rental-cost", 500.0);
    }

    public boolean isAutoRenewEnabled() {
        return config.getBoolean("player-shops.auto-renew-enabled", true);
    }

    public String getMainMenuTitle() {
        return config.getString("gui.main-title", "&6&lFRIZZLEN SHOP");
    }

    public String getCategoryMenuTitle(String category) {
        return config.getString("gui.category-title", "&6&l{CATEGORY} CATEGORY")
                .replace("{CATEGORY}", category.toUpperCase());
    }

    public String getItemMenuTitle(String item) {
        return config.getString("gui.item-title", "&6&l{ITEM}")
                .replace("{ITEM}", item.toUpperCase());
    }

    public String getMyShopsMenuTitle() {
        return config.getString("gui.my-shops-title", "&6&lMY SHOPS");
    }

    public String getCreateShopMenuTitle(int step) {
        return config.getString("gui.create-shop-title", "&6&lCREATE SHOP - STEP {STEP}/4")
                .replace("{STEP}", String.valueOf(step));
    }

    public String getAdminMenuTitle() {
        return config.getString("gui.admin-title", "&4&lSHOP ADMIN");
    }

    public int getItemsPerPage() {
        return config.getInt("gui.items-per-page", 21);
    }

    public boolean showEnchantments() {
        return config.getBoolean("gui.show-enchantments", true);
    }

    public boolean showLore() {
        return config.getBoolean("gui.show-lore", true);
    }

    public boolean showDurability() {
        return config.getBoolean("gui.show-durability", true);
    }

    public List<String> getShopLimitTiers() {
        return config.getStringList("permissions.shop-limit-tiers");
    }

    public List<String> getShopSizeTiers() {
        return config.getStringList("permissions.shop-size-tiers");
    }

    public boolean isTransactionLoggingEnabled() {
        return config.getBoolean("logging.log-transactions", true);
    }

    public boolean isShopChangeLoggingEnabled() {
        return config.getBoolean("logging.log-shop-changes", true);
    }

    public boolean isPriceChangeLoggingEnabled() {
        return config.getBoolean("logging.log-price-changes", true);
    }

    public int getLogRetentionPeriod() {
        return config.getInt("logging.retention-period", 30);
    }

    public String getDatabaseType() {
        return config.getString("database.type", "SQLITE");
    }

    public String getMysqlHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMysqlPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMysqlDatabase() {
        return config.getString("database.mysql.database", "frizzlenshop");
    }

    public String getMysqlUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMysqlPassword() {
        return config.getString("database.mysql.password", "password");
    }

    public String getMysqlTablePrefix() {
        return config.getString("database.mysql.table-prefix", "fs_");
    }

    /**
     * Set maintenance mode
     *
     * @param enabled Whether maintenance mode should be enabled
     */
    public void setMaintenanceMode(boolean enabled) {
        config.set("general.maintenance-mode", enabled);
    }

    /**
     * Get the global price multiplier
     * This affects all prices in the shop system
     *
     * @return The global price multiplier
     */
    public double getGlobalPriceMultiplier() {
        return config.getDouble("economy.global-price-multiplier", 1.0);
    }

    /**
     * Get the default buy price for items
     * This is the base price before applying category/item specific multipliers
     *
     * @return The default buy price
     */
    public double getDefaultBuyPrice() {
        return config.getDouble("economy.default-buy-price", 100.0);
    }

    /**
     * Get the default sell price for items
     * This is the base price before applying category/item specific multipliers
     *
     * @return The default sell price
     */
    public double getDefaultSellPrice() {
        return config.getDouble("economy.default-sell-price", 80.0);
    }

    /**
     * Check if dynamic pricing is enabled
     * Dynamic pricing adjusts prices based on supply and demand
     *
     * @return True if dynamic pricing is enabled, false otherwise
     */
    public boolean isDynamicPricingEnabled() {
        return config.getBoolean("dynamic_pricing.enabled", false);
    }

    /**
     * Get the dynamic pricing volatility multiplier
     * This determines how quickly prices change based on transactions
     *
     * @return The volatility multiplier
     */
    public double getVolatilityMultiplier() {
        return config.getDouble("dynamic_pricing.volatility_multiplier", 1.0);
    }

    /**
     * Get the dynamic pricing analysis interval in minutes
     * 
     * @return The analysis interval in minutes
     */
    public int getAnalysisInterval() {
        return config.getInt("dynamic_pricing.analysis_interval", 60);
    }
    
    /**
     * Get the maximum price change allowed as a percentage of base price
     * 
     * @return The maximum price change as a decimal (0.5 = 50%)
     */
    public double getMaxPriceChange() {
        return config.getDouble("dynamic_pricing.max_price_change", 0.5);
    }
    
    /**
     * Get the normalization rate for prices
     * This determines how quickly prices return to baseline when there's no activity
     * 
     * @return The normalization rate
     */
    public double getNormalizationRate() {
        return config.getDouble("dynamic_pricing.normalization_rate", 0.1);
    }
    
    /**
     * Check if crafting relationships should be considered in dynamic pricing
     * 
     * @return True if crafting relationships are used, false otherwise
     */
    public boolean useCraftingRelationships() {
        return config.getBoolean("dynamic_pricing.use_crafting_relationships", true);
    }

    /**
     * Check if price fluctuation is enabled
     * Price fluctuation causes prices to change regularly based on time
     *
     * @return True if price fluctuation is enabled, false otherwise
     */
    public boolean isPriceFluctuationEnabled() {
        return config.getBoolean("dynamic_pricing.fluctuation.enabled", false);
    }
    
    /**
     * Set whether price fluctuation is enabled
     *
     * @param enabled True to enable price fluctuation, false to disable
     */
    public void setPriceFluctuationEnabled(boolean enabled) {
        config.set("dynamic_pricing.fluctuation.enabled", enabled);
        saveConfig();
    }
    
    /**
     * Get the price fluctuation magnitude
     * This determines how much prices can fluctuate naturally
     * 
     * @return The fluctuation magnitude as a decimal (0.05 = 5%)
     */
    public double getFluctuationMagnitude() {
        return config.getDouble("dynamic_pricing.fluctuation.magnitude", 0.05);
    }

    /**
     * Set the global price multiplier
     *
     * @param multiplier The new global price multiplier
     */
    public void setGlobalPriceMultiplier(double multiplier) {
        config.set("economy.global-price-multiplier", multiplier);
    }

    /**
     * Set the default buy price for items
     *
     * @param price The new default buy price
     */
    public void setDefaultBuyPrice(double price) {
        config.set("economy.default-buy-price", price);
    }

    /**
     * Set the default sell price for items
     *
     * @param price The new default sell price
     */
    public void setDefaultSellPrice(double price) {
        config.set("economy.default-sell-price", price);
    }

    /**
     * Set the sell price ratio
     *
     * @param ratio The new sell price ratio
     */
    public void setSellPriceRatio(double ratio) {
        config.set("economy.sell-price-ratio", ratio);
    }
    
    /**
     * Get the ratio of sell price to buy price
     *
     * @return The sell price ratio (default: 0.75)
     */
    public double getSellPriceRatio() {
        return config.getDouble("economy.sell-price-ratio", 0.75);
    }

    /**
     * Set whether dynamic pricing is enabled
     * Dynamic pricing adjusts prices based on supply and demand
     *
     * @param enabled Whether dynamic pricing should be enabled
     */
    public void setDynamicPricingEnabled(boolean enabled) {
        config.set("dynamic_pricing.enabled", enabled);
    }

    /**
     * Get the tax rate for a specific category
     *
     * @param category The category
     * @return The tax rate for the category
     */
    public double getCategoryTaxRate(String category) {
        return config.getDouble("economy.taxes.categories." + category, getGlobalTaxRate());
    }

    /**
     * Get the global tax rate
     *
     * @return The global tax rate
     */
    public double getGlobalTaxRate() {
        return config.getDouble("economy.taxes.global-rate", 5.0);
    }

    /**
     * Set the global tax rate
     *
     * @param rate The new global tax rate
     */
    public void setGlobalTaxRate(double rate) {
        config.set("economy.taxes.global-rate", rate);
    }

    /**
     * Get the total tax collected today
     *
     * @return The total tax collected today
     */
    public double getTaxCollectedToday() {
        // In a real implementation, this would be calculated from a database
        return config.getDouble("economy.taxes.collected-today", 0.0);
    }

    /**
     * Get the total tax collected overall
     *
     * @return The total tax collected
     */
    public double getTotalTaxCollected() {
        // In a real implementation, this would be calculated from a database
        return config.getDouble("economy.taxes.total-collected", 0.0);
    }

    /**
     * Get the UUID of the account where taxes are collected
     *
     * @return The UUID of the tax collection account, or null if taxes are removed from economy
     */
    public UUID getTaxCollectionAccount() {
        String uuidString = config.getString("economy.taxes.collection-account");
        if (uuidString == null || uuidString.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get the minimum tax amount
     *
     * @return The minimum tax amount
     */
    public double getMinimumTax() {
        return config.getDouble("economy.taxes.minimum", 1.0);
    }

    /**
     * Get the maximum tax amount
     * A value of 0 means no maximum
     *
     * @return The maximum tax amount
     */
    public double getMaximumTax() {
        return config.getDouble("economy.taxes.maximum", 0.0);
    }

    /**
     * Get the list of available currencies
     *
     * @return The list of available currencies
     */
    public List<String> getAvailableCurrencies() {
        return config.getStringList("economy.currencies");
    }

    /**
     * Check if testing mode is enabled
     *
     * @return Whether testing mode is enabled
     */
    public boolean isTestingMode() {
        return config.getBoolean("general.testing-mode", false);
    }

    /**
     * Get the shop rent days (how many days a shop rental lasts)
     * 
     * @return The number of days a shop rental lasts
     */
    public int getShopRentDays() {
        return config.getInt("player-shops.rent-days", 30);
    }

    /**
     * Set the minimum tax amount
     *
     * @param amount The new minimum tax amount
     */
    public void setMinimumTax(double amount) {
        config.set("economy.taxes.minimum", amount);
    }

    /**
     * Set the maximum tax amount
     *
     * @param amount The new maximum tax amount (0 for no maximum)
     */
    public void setMaximumTax(double amount) {
        config.set("economy.taxes.maximum", amount);
    }

    /**
     * Set the admin shop tax rate
     *
     * @param rate The new admin shop tax rate
     */
    public void setAdminShopTaxRate(double rate) {
        config.set("admin-shops.tax-rate", rate);
    }

    /**
     * Set the player shop tax rate
     *
     * @param rate The new player shop tax rate
     */
    public void setPlayerShopTaxRate(double rate) {
        config.set("player-shops.tax-rate", rate);
    }

    /**
     * Get whether craft-based pricing is enabled
     * Craft-based pricing adjusts prices based on crafting components
     *
     * @return True if craft-based pricing is enabled, false otherwise
     */
    public boolean isCraftBasedPricingEnabled() {
        return config.getBoolean("economy.craft-based-pricing.enabled", true);
    }

    /**
     * Set whether craft-based pricing is enabled
     *
     * @param enabled Whether craft-based pricing should be enabled
     */
    public void setCraftBasedPricingEnabled(boolean enabled) {
        config.set("economy.craft-based-pricing.enabled", enabled);
    }

    /**
     * Get the component demand multiplier
     * Controls how much demand for crafted items affects component prices
     *
     * @return The component demand multiplier
     */
    public double getComponentDemandMultiplier() {
        return config.getDouble("economy.craft-based-pricing.component-demand-multiplier", 0.4);
    }

    /**
     * Set the component demand multiplier
     *
     * @param multiplier The new component demand multiplier
     */
    public void setComponentDemandMultiplier(double multiplier) {
        config.set("economy.craft-based-pricing.component-demand-multiplier", multiplier);
    }

    /**
     * Gets the base price for a material from configuration
     * 
     * @param material The material to get the price for
     * @return The configured base price, or 0 if not configured
     */
    public double getMaterialBasePrice(org.bukkit.Material material) {
        String path = "economy.material_prices." + material.toString();
        if (config.contains(path)) {
            return config.getDouble(path);
        }
        return 0.0;
    }

    /**
     * Set the base price for a specific material
     *
     * @param material The material
     * @param price The base price
     */
    public void setMaterialBasePrice(org.bukkit.Material material, double price) {
        String path = "economy.material-prices." + material.toString().toLowerCase();
        config.set(path, price);
    }

    /**
     * Get whether price suggestions are enabled for player shops
     *
     * @return True if price suggestions are enabled
     */
    public boolean arePriceSuggestionsEnabled() {
        return config.getBoolean("player-shops.price-suggestions-enabled", true);
    }

    /**
     * Set whether price suggestions are enabled for player shops
     *
     * @param enabled Whether price suggestions should be enabled
     */
    public void setPriceSuggestionsEnabled(boolean enabled) {
        config.set("player-shops.price-suggestions-enabled", enabled);
    }

    /**
     * Check if profit margin display is enabled
     * This shows players the potential profit from crafting items
     *
     * @return True if profit margin display is enabled
     */
    public boolean isProfitMarginDisplayEnabled() {
        return config.getBoolean("economy.craft-based-pricing.show-profit-margins", true);
    }

    /**
     * Set whether profit margin display is enabled
     *
     * @param enabled Whether profit margin display should be enabled
     */
    public void setProfitMarginDisplayEnabled(boolean enabled) {
        config.set("economy.craft-based-pricing.show-profit-margins", enabled);
    }

    /**
     * Check if this is the first run of the plugin
     * Used to determine if shops should be initialized
     *
     * @return True if this is the first run
     */
    public boolean isFirstRun() {
        return config.getBoolean("general.first-run", true);
    }

    /**
     * Set whether this is the first run of the plugin
     *
     * @param isFirstRun Whether this is the first run
     */
    public void setFirstRun(boolean isFirstRun) {
        config.set("general.first-run", isFirstRun);
    }

    /**
     * Check if admin shops should be forcibly refreshed
     * This is useful for updating shops after config changes
     *
     * @return True if admin shops should be refreshed
     */
    public boolean isForceAdminShopRefresh() {
        return config.getBoolean("admin-shops.force-refresh", false);
    }

    /**
     * Set whether admin shops should be forcibly refreshed
     *
     * @param forceRefresh Whether admin shops should be refreshed
     */
    public void setForceAdminShopRefresh(boolean forceRefresh) {
        config.set("admin-shops.force-refresh", forceRefresh);
    }

    /**
     * Check if admin shops should use the tier-based pricing system
     *
     * @return True if admin shops should use tier-based pricing
     */
    public boolean useTierBasedPricing() {
        return config.getBoolean("admin-shops.use-tier-based-pricing", true);
    }

    /**
     * Set whether admin shops should use the tier-based pricing system
     *
     * @param useTierPricing Whether admin shops should use tier-based pricing
     */
    public void setUseTierBasedPricing(boolean useTierPricing) {
        config.set("admin-shops.use-tier-based-pricing", useTierPricing);
    }

    /**
     * Get the starting coin amount for new players
     * 
     * @return The starting coin amount
     */
    public double getStartingCoins() {
        return config.getDouble("economy.starting-coins", 100.0);
    }

    /**
     * Set the starting coin amount for new players
     * 
     * @param amount The starting coin amount
     */
    public void setStartingCoins(double amount) {
        config.set("economy.starting-coins", amount);
    }
} 