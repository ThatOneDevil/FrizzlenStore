# Admin Shop System

The Admin Shop system in FrizzlenShop provides server-operated shops with tiered pricing to create a balanced economy and progression curve for players.

[← Back to Main Documentation](../README.md)

## Overview

Admin shops are server-run shops with unlimited stock that serve as the backbone of your server's economy. Unlike player shops, admin shops are created and managed by server administrators and operate with infinite resources.

## Tiered Pricing System

The plugin implements a sophisticated tiered pricing system to balance the in-game economy:

### Pricing Tiers

| Tier | Price Range | Description |
|------|-------------|-------------|
| STARTER | 0-100 coins | Basic resources and tools for new players |
| EARLY_GAME | 100-500 coins | Iron tier materials and basic commodities |
| MID_GAME | 500-2500 coins | Gold and redstone materials, enchanting tables |
| LATE_GAME | 2500-10000 coins | Diamond tier items and advanced resources |
| END_GAME | 10000-45000 coins | Netherite items and high-end gear |
| LUXURY | 45000+ coins | Extremely rare items like dragon eggs |

This tiered system is designed to provide clear progression goals for players, typically spanning a 1-3 month gameplay cycle.

## Default Pricing Map

The `DefaultPricingMap` maintains a comprehensive mapping of Minecraft items to their appropriate pricing tiers. This ensures consistent pricing across the server and serves as the foundation for dynamic pricing if enabled.

## Admin Shop Setup

### Creating an Admin Shop

To create an admin shop:

```
/fs admin shop create <name>
```

This creates a new admin shop at your current location.

### Populating the Admin Shop

The plugin includes an `AdminShopPopulator` that automatically stocks admin shops with predefined items based on their pricing tiers. You can:

1. Create a main admin shop with all items:
   ```
   /fs admin shop populate <shop-name> all
   ```

2. Create category-specific shops:
   ```
   /fs admin shop populate <shop-name> <category>
   ```

   Available categories: building, combat, tools, food, redstone, brewing, decoration, miscellaneous

### Refreshing Admin Shops

You can force a refresh of all admin shops with:

```
/fs admin shop refresh
```

You can also enable auto-refresh on server restart in the configuration.

## Configuration

The admin shop system can be configured in `config.yml`:

```yaml
admin_shop:
  # Force refresh admin shops on server startup
  refresh_on_startup: false
  
  # Default tax rate for admin shops (percentage)
  tax_rate: 0.05
  
  # Allow admin shops to buy items from players
  allow_selling_to_shop: true
  
  # Pricing tier multipliers (relative to base prices)
  tier_multipliers:
    STARTER: 1.0
    EARLY_GAME: 3.0
    MID_GAME: 10.0
    LATE_GAME: 30.0
    END_GAME: 100.0
    LUXURY: 300.0
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs admin shop create <name>` | Creates a new admin shop |
| `/fs admin shop delete <name>` | Deletes an admin shop |
| `/fs admin shop list` | Lists all admin shops |
| `/fs admin shop open <name>` | Opens an admin shop GUI |
| `/fs admin shop populate <name> <category>` | Populates a shop with items |
| `/fs admin shop refresh` | Refreshes all admin shops |
| `/fs admin price tier <item> <tier>` | Sets an item's pricing tier |
| `/fs admin price set <item> <price>` | Sets a specific price for an item |

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlenshop.admin` | Access to all admin shop commands |
| `frizzlenshop.admin.shop.create` | Permission to create admin shops |
| `frizzlenshop.admin.shop.delete` | Permission to delete admin shops |
| `frizzlenshop.admin.shop.populate` | Permission to populate admin shops |
| `frizzlenshop.admin.price` | Permission to adjust prices |

## Admin Shop GUI

The admin shop provides a clean, intuitive interface for players to buy and sell items. The GUI includes:

- Category navigation
- Item display with buy/sell prices
- Quantity selection
- Transaction confirmation

## Integration with Other Features

The admin shop system integrates with several other features:

- **[Dynamic Pricing](dynamic-pricing.md)**: Admin shop prices can fluctuate based on supply and demand
- **[Market Analysis](market-analysis.md)**: Transaction data from admin shops is used for market trend analysis
- **[Economy Integration](economy.md)**: All transactions use the server's economy system

## Developer API

Developers can interact with the admin shop system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get ShopManager
ShopManager shopManager = plugin.getShopManager();

// Create an admin shop
AdminShop adminShop = new AdminShop(UUID.randomUUID(), "MyAdminShop", player.getLocation());
shopManager.registerShop(adminShop);

// Add an item to the shop
ItemStack item = new ItemStack(Material.DIAMOND);
ShopItem shopItem = new ShopItem(item, 100.0, 80.0, "coin", -1);
adminShop.addItem(shopItem);
``` 