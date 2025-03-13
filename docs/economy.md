# Economy Integration

FrizzlenShop integrates with Vault and popular economy plugins to provide a robust shop system with seamless currency transactions.

[← Back to Main Documentation](../README.md)

## Overview

The Economy Integration module handles all monetary transactions within FrizzlenShop, providing a bridge between the plugin and various economy plugins through the Vault API. This allows for a consistent economy experience regardless of the economy plugin you choose to use.

## Features

### Vault Integration

FrizzlenShop uses Vault as an abstraction layer to interact with economy plugins, ensuring compatibility with:

- EssentialsX Economy
- CMI Economy
- Treasury
- iConomy
- And many other economy plugins

### Transaction Management

The `EconomyManager` handles various transaction types:

- Player purchases from shops
- Player sales to shops
- Shop balance management
- Transaction taxes
- Transfer between shop and owner accounts

### Multiple Currency Support

The plugin supports servers with multiple currencies:

- Each shop can specify its preferred currency
- Different items can be sold for different currencies
- Exchange rates between currencies can be configured

### Starting Balance

New players joining the server for the first time automatically receive a configurable starting balance:

```yaml
economy:
  starting_coins: 100
```

This helps new players get started with shopping immediately.

## How It Works

The Economy Integration is handled by the `EconomyManager` class, which:

1. Registers with Vault during plugin initialization
2. Verifies economy plugin availability
3. Provides methods for deposit, withdrawal, and balance checking
4. Applies transaction taxes when configured
5. Records all transactions for analysis

## Transaction Process

When a player makes a purchase:

1. The system checks if the player has sufficient funds
2. Funds are withdrawn from the player's account
3. A configured tax percentage is deducted if applicable
4. The remaining amount is added to the shop's balance
5. The transaction is recorded in the database
6. For player shops, the owner can later withdraw the profits

## Configuration

The economy integration can be configured in `config.yml`:

```yaml
economy:
  # Starting coins for new players
  starting_coins: 100
  
  # Default currency name (for Vault integration)
  default_currency: "coin"
  
  # Whether to format currency values with symbols
  use_currency_symbols: true
  
  # Default currency symbol
  currency_symbol: "$"
  
  # Minimum transaction amount
  min_transaction: 0.01
  
  # Whether to use a dedicated account for tax collection
  use_tax_account: false
  
  # Account name for tax collection
  tax_account: "server_taxes"
  
  # Whether to allow negative shop balances
  allow_negative_shop_balance: false
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs balance` | Shows your current balance |
| `/fs shop balance <name>` | Shows a shop's balance |
| `/fs shop withdraw <name> <amount>` | Withdraws money from your shop |
| `/fs admin eco give <player> <amount>` | Gives money to a player |
| `/fs admin eco take <player> <amount>` | Takes money from a player |
| `/fs admin eco set <player> <amount>` | Sets a player's balance |
| `/fs admin eco reset <player>` | Resets a player's balance to starting amount |

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlenshop.balance` | Ability to check your balance |
| `frizzlenshop.shop.withdraw` | Ability to withdraw money from shops |
| `frizzlenshop.admin.eco` | Access to economy administration commands |

## Transaction Logs

All economic transactions are logged in the database for:

- Auditing purposes
- Market analysis
- Dynamic pricing calculations
- Player history tracking

## Integration with Other Features

The Economy Integration module connects with:

- **[Admin Shop System](admin-shop.md)**: Processes admin shop transactions
- **[Player Shop System](player-shop.md)**: Handles player shop earnings and withdrawals
- **[Dynamic Pricing](dynamic-pricing.md)**: Uses transaction data to adjust prices
- **[Market Analysis](market-analysis.md)**: Provides transaction data for analysis

## Developer API

Developers can interact with the economy system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get economy manager
EconomyManager economyManager = plugin.getEconomyManager();

// Get player balance
double balance = economyManager.getBalance(player);

// Check if player has enough funds
boolean hasEnough = economyManager.has(player, 100.0);

// Make a transaction
boolean success = economyManager.withdraw(player, 50.0, "Item purchase");
if (success) {
    economyManager.deposit(shopOwner, 47.5, "Shop sale (after 5% tax)");
}

// Get the Vault economy provider
Economy economy = economyManager.getEconomy();
```

## Troubleshooting

Common issues with economy integration:

### No Economy Plugin Found

If Vault cannot find an economy plugin:

1. Make sure a compatible economy plugin is installed
2. Ensure Vault is properly configured
3. Check the console for errors on startup

### Failed Transactions

If transactions fail:

1. Verify player has sufficient funds
2. Check economy plugin permissions
3. Ensure no conflict with other economy plugins
4. Check transaction logs for errors

### Multiple Currency Conflicts

If using multiple currencies:

1. Verify each currency is properly registered
2. Check exchange rates are configured correctly
3. Ensure the default currency is set properly 