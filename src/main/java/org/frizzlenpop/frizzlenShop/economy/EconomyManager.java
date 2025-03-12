package org.frizzlenpop.frizzlenShop.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles integration with the Vault economy plugin
 */
public class EconomyManager {

    private final FrizzlenShop plugin;
    private Economy vaultEconomy;
    private boolean vaultHooked;

    public EconomyManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    /**
     * Setup the economy integration with Vault
     */
    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().severe("Vault not found! Economy features will not work.");
            vaultHooked = false;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().severe("No economy provider found! Economy features will not work.");
            vaultHooked = false;
            return;
        }

        vaultEconomy = rsp.getProvider();
        if (vaultEconomy == null) {
            plugin.getLogger().severe("Failed to hook into Vault economy provider!");
            vaultHooked = false;
            return;
        }

        vaultHooked = true;
        plugin.getLogger().info("Successfully hooked into Vault economy: " + vaultEconomy.getName());
    }

    /**
     * Check if a player has enough funds
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to check
     * @param currency   The currency to check (note: Vault only supports one currency)
     * @return True if the player has enough funds, false otherwise
     */
    public boolean has(UUID playerUuid, double amount, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return false;
        }

        // For testing and debugging, we can return true
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test has() result for player " + playerUuid);
            return true; // Always return true in test mode
        }
        
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            return vaultEconomy.has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player has funds", e);
            return false;
        }
    }

    /**
     * Get the balance of a player
     *
     * @param playerUuid The UUID of the player
     * @param currency   The currency to check (note: Vault only supports one currency)
     * @return The player's balance
     */
    public double getBalance(UUID playerUuid, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return 0;
        }

        // For testing and debugging, we can return a fixed amount
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test balance for player " + playerUuid);
            return 10000.0; // High test balance for easy testing
        }

        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            return vaultEconomy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player balance", e);
            return 0;
        }
    }

    /**
     * Transfer funds from one player to another
     *
     * @param fromUuid The UUID of the player to withdraw from
     * @param toUuid   The UUID of the player to deposit to
     * @param amount   The amount to transfer
     * @param currency The currency to use (note: Vault only supports one currency)
     * @return True if the transfer was successful, false otherwise
     */
    public boolean transfer(UUID fromUuid, UUID toUuid, double amount, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test transfer from " + fromUuid + " to " + toUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to transfer a non-positive amount: " + amount);
            return false;
        }

        try {
            OfflinePlayer fromPlayer = Bukkit.getOfflinePlayer(fromUuid);
            OfflinePlayer toPlayer = Bukkit.getOfflinePlayer(toUuid);
            
            // Check if the sender has enough funds
            if (!vaultEconomy.has(fromPlayer, amount)) {
                return false;
            }
            
            // Perform the transfer
            boolean withdrawSuccess = vaultEconomy.withdrawPlayer(fromPlayer, amount).transactionSuccess();
            if (!withdrawSuccess) {
                return false;
            }
            
            boolean depositSuccess = vaultEconomy.depositPlayer(toPlayer, amount).transactionSuccess();
            if (!depositSuccess) {
                // Rollback the withdrawal
                vaultEconomy.depositPlayer(fromPlayer, amount);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error transferring funds", e);
            return false;
        }
    }

    /**
     * Withdraw funds from a player
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to withdraw
     * @param currency   The currency to use (note: Vault only supports one currency)
     * @return True if the withdrawal was successful, false otherwise
     */
    public boolean withdraw(UUID playerUuid, double amount, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test withdraw for player " + playerUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to withdraw a non-positive amount: " + amount);
            return false;
        }

        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            
            // Check if the player has enough funds
            if (!vaultEconomy.has(player, amount)) {
                return false;
            }
            
            return vaultEconomy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error withdrawing funds", e);
            return false;
        }
    }

    /**
     * Deposit funds to a player
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to deposit
     * @param currency   The currency to use (note: Vault only supports one currency)
     * @return True if the deposit was successful, false otherwise
     */
    public boolean deposit(UUID playerUuid, double amount, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test deposit for player " + playerUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to deposit a non-positive amount: " + amount);
            return false;
        }

        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
            return vaultEconomy.depositPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error depositing funds", e);
            return false;
        }
    }

    /**
     * Create an account for a shop
     *
     * @param shopUuid   The UUID of the shop
     * @param shopName   The name of the shop
     * @param currency   The currency to use (note: Vault only supports one currency)
     * @return True if the account was created successfully
     */
    public boolean createShopAccount(UUID shopUuid, String shopName, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return false;
        }
        
        // In Vault, there is usually no explicit account creation
        // It's created automatically when a deposit happens
        // But we can check if the account already exists
        try {
            OfflinePlayer shop = Bukkit.getOfflinePlayer(shopUuid);
            if (!vaultEconomy.hasAccount(shop)) {
                // Try to create the account by doing a small deposit then withdrawal
                vaultEconomy.depositPlayer(shop, 1);
                vaultEconomy.withdrawPlayer(shop, 1);
            }
            return vaultEconomy.hasAccount(shop);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating shop account", e);
            return false;
        }
    }

    /**
     * Get the default currency
     *
     * @return The default currency (always returns the config default as Vault only has one currency)
     */
    public String getDefaultCurrency() {
        return plugin.getConfigManager().getDefaultCurrency();
    }

    /**
     * Format the balance for display
     *
     * @param player The player to format the balance for
     * @return The formatted balance
     */
    public String formatBalance(Player player) {
        if (!vaultHooked || vaultEconomy == null) {
            return "0";
        }
        
        try {
            double balance = vaultEconomy.getBalance(player);
            return vaultEconomy.format(balance);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error formatting balance", e);
            return "0";
        }
    }

    /**
     * Format an amount with the currency
     *
     * @param amount   The amount to format
     * @param currency The currency to use (note: Vault only supports one currency)
     * @return The formatted amount
     */
    public String formatCurrency(double amount, String currency) {
        if (!vaultHooked || vaultEconomy == null) {
            return String.format("%.2f", amount);
        }
        
        try {
            return vaultEconomy.format(amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error formatting currency", e);
            return String.format("%.2f", amount);
        }
    }

    /**
     * Get a list of available currencies
     * Note: Vault only supports a single currency, so this will always return a single-item list
     *
     * @return An array of available currencies
     */
    public String[] getAvailableCurrencies() {
        // Vault only supports one currency, so we return a list with only the default currency
        return new String[]{getDefaultCurrency()};
    }
    
    /**
     * Check if Vault is hooked and working
     *
     * @return True if Vault is hooked, false otherwise
     */
    public boolean isVaultHooked() {
        return vaultHooked;
    }
} 