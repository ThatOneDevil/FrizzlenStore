# Player Shop System

The Player Shop system allows players to create and manage their own shops on your Minecraft server, fostering a player-driven economy.

[← Back to Main Documentation](../README.md)

## Overview

Player shops in FrizzlenShop enable players to sell their items to other players, creating a dynamic marketplace beyond the server's admin shops. These shops have limited stock (unlike admin shops) and are physically located in the world.

## Creating a Shop

There are two ways to create a player shop:

### Method 1: Command Creation

1. Run the command `/fs shop create <name>`
2. Follow the interactive creation process to set up your shop
3. Add items to your shop inventory

### Method 2: Using a Template

For quick shop setup, you can use templates:

1. Access the templates menu via `/fs template` or from the main menu
2. Browse available templates by category or creator
3. Select a template that matches your needs
4. Click "Use Template" and follow the prompts
5. Your shop will be created with all the items and settings from the template

Using templates is the fastest way to set up a fully stocked shop with optimal pricing.

### Shop Customization

Once created, players can customize their shop:

- Set a description: `/fs shop desc <name> <description>`
- Change appearance: `/fs shop design <name>`
- Add items: `/fs shop stock <name>`

## Managing Shop Inventory

### Adding Items

Players add items to their shops by using:

```
/fs shop stock <name>
```

This opens an interface where they can:

1. Place items they want to sell
2. Set buy/sell prices for each item
3. Set the maximum stock quantity

### Setting Prices

Prices can be set for both buying from and selling to the shop:

```
/fs shop price <name> <item> <buy-price> <sell-price>
```

The buy price is what players pay to purchase the item, while the sell price is what the shop pays when buying items from players.

## Shop Economy

### Transaction Taxes

All player shop transactions are subject to a configurable tax rate:

```yaml
shop:
  tax_rate: 0.05  # 5% tax on transactions
```

This tax is collected by the server and can be configured to go to a specific account.

### Shop Balance

Each player shop maintains its own balance, which:

- Increases when players buy items from the shop
- Decreases when the shop buys items from players
- Can be withdrawn by the shop owner

### Withdrawing Profits

Shop owners can withdraw earnings with:

```
/fs shop withdraw <name> <amount>
```

## Shop Management

### Shop Information

View information about a shop:

```
/fs shop info <name>
```

This displays:
- Shop owner
- Location
- Description
- Current balance
- Number of items stocked
- Transaction history

### Shop Listing

List all shops on the server:

```
/fs shop list
```

Or search for a specific type of shop:

```
/fs shop search <item>
```

### Shop Removal

Remove a shop with:

```
/fs shop delete <name>
```

Only the shop owner or server administrators can delete shops.

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlenshop.shop.create` | Allow creating player shops |
| `frizzlenshop.shop.delete` | Allow deleting own shops |
| `frizzlenshop.shop.stock` | Allow stocking items in shops |
| `frizzlenshop.shop.withdraw` | Allow withdrawing money from shops |
| `frizzlenshop.shop.use` | Allow using player shops |
| `frizzlenshop.shop.create.limit.<number>` | Set maximum number of shops a player can create |

## Configuration

Player shops can be configured in `config.yml`:

```yaml
player_shop:
  # Maximum number of shops a player can own by default
  max_shops_per_player: 3
  
  # Maximum number of unique items per shop
  max_items_per_shop: 54
  
  # Tax rate for player shop transactions (percentage)
  tax_rate: 0.05
  
  # Minimum price allowed for items
  min_price: 0.1
  
  # Maximum price allowed for items
  max_price: 1000000.0
  
  # Cost to create a shop
  creation_cost: 100.0
  
  # Whether to allow shop owners to set buy prices
  allow_buy_pricing: true
  
  # Whether to allow shop owners to set sell prices
  allow_sell_pricing: true
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs shop create <name>` | Creates a new player shop |
| `/fs shop delete <name>` | Deletes a player shop |
| `/fs shop list` | Lists all player shops |
| `/fs shop open <name>` | Opens a player shop GUI |
| `/fs shop info <name>` | Shows information about a shop |
| `/fs shop stock <name>` | Opens the stocking interface |
| `/fs shop price <name> <item> <buy> <sell>` | Sets prices for an item |
| `/fs shop withdraw <name> <amount>` | Withdraws money from shop balance |
| `/fs shop search <item>` | Searches for shops selling a specific item |
| `/fs shop desc <name> <description>` | Sets a shop description |
| `/fs shop design <name>` | Opens the shop design interface |

## Integration with Other Features

The player shop system integrates with several other features:

- **[Market Analysis](market-analysis.md)**: Transaction data from player shops contributes to market trends
- **[Economy Integration](economy.md)**: All transactions use the server's economy system
- **[Database Management](database.md)**: Shop data is stored persistently

## Developer API

Developers can interact with the player shop system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get ShopManager
ShopManager shopManager = plugin.getShopManager();

// Create a player shop
UUID ownerId = player.getUniqueId();
PlayerShop playerShop = new PlayerShop(UUID.randomUUID(), "PlayerShop", ownerId, player.getLocation());
shopManager.registerShop(playerShop);

// Add an item to the shop
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
ShopItem shopItem = new ShopItem(item, 1000.0, 800.0, "coin", 10);
playerShop.addItem(shopItem);

// Get shops owned by a player
List<Shop> playerShops = shopManager.getShopsByOwner(ownerId);
``` 