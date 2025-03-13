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
        return config.getBoolean("economy.dynamic-pricing.enabled", false);
    }

    /**
     * Check if price fluctuation is enabled
     * Price fluctuation causes prices to change regularly based on time
     *
     * @return True if price fluctuation is enabled, false otherwise
     */
    public boolean isPriceFluctuationEnabled() {
        return config.getBoolean("economy.price-fluctuation.enabled", false);
    }

    /**
     * Get the sell price ratio
     * This is the ratio of sell price to buy price (e.g. 0.8 means items sell for 80% of buy price)
     *
     * @return The sell price ratio
     */
    public double getSellPriceRatio() {
        return config.getDouble("economy.sell-price-ratio", 0.8);
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
     * Set whether dynamic pricing is enabled
     *
     * @param enabled Whether dynamic pricing should be enabled
     */
    public void setDynamicPricingEnabled(boolean enabled) {
        config.set("economy.dynamic-pricing.enabled", enabled);
    }

    /**
     * Set whether price fluctuation is enabled
     *
     * @param enabled Whether price fluctuation should be enabled
     */
    public void setPriceFluctuationEnabled(boolean enabled) {
        config.set("economy.price-fluctuation.enabled", enabled);
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
} 