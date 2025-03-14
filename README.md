# FrizzlenShop

FrizzlenShop is a comprehensive shop plugin for Minecraft servers that enables both admin and player shops with dynamic pricing features based on market analysis.

## Features

FrizzlenShop offers a wide range of features to enhance your server's economy:

- **[Admin Shop System](docs/admin-shop.md)**: Server-run shops with tiered pricing for balanced game progression
- **[Player Shop System](docs/player-shop.md)**: Allow players to create and manage their own shops
- **[Shop Templates](docs/templates.md)**: Create, share, and use templates for quick shop setup
- **[Dynamic Pricing](docs/dynamic-pricing.md)**: Prices that change based on supply and demand
- **[Market Analysis](docs/market-analysis.md)**: Track market trends and suggest profitable crafting opportunities
- **[Economy Integration](docs/economy.md)**: Seamless integration with Vault and popular economy plugins
- **[Database Management](docs/database.md)**: Robust storage and retrieval of shop and transaction data
- **[Crafting System](docs/crafting.md)**: Analysis of crafting relationships for dynamic pricing

## Installation

1. Download the latest version of FrizzlenShop from [SpigotMC](https://www.spigotmc.org/) or [Bukkit](https://dev.bukkit.org/)
2. Place the `.jar` file in your server's `plugins` folder
3. Restart your server
4. The plugin will generate default configuration files

## Quick Start

After installation, you can create an admin shop with:

```
/fs admin shop create <name>
```

Players can create their own shops with:

```
/fs shop create <name>
```

For more detailed commands, see the [Commands](#commands) section below.

## Configuration

The main configuration file is located at `plugins/FrizzlenShop/config.yml`. Here are some key configuration options:

```yaml
# Database configuration
database:
  type: sqlite  # sqlite or mysql
  path: frizzlenshop.db  # For sqlite only
  host: localhost  # For mysql only
  port: 3306  # For mysql only
  name: frizzlenshop  # For mysql only
  username: root  # For mysql only
  password: ''  # For mysql only
  table_prefix: fs_

# General settings
settings:
  starting_coins: 100  # Coins given to new players
  dynamic_pricing: true  # Enable/disable dynamic pricing
  admin_shop_refresh: false  # Force refresh admin shop on startup

# Shop settings
shop:
  max_player_shops: 3  # Maximum number of shops a player can own
  max_items_per_shop: 54  # Maximum number of items per shop
  tax_rate: 0.05  # 5% tax on player shop transactions
```

For more detailed configuration options, see each feature's dedicated documentation.

## Commands

### Admin Commands

- `/fs admin shop create <n>` - Creates a new admin shop
- `/fs admin shop delete <n>` - Deletes an admin shop
- `/fs admin shop list` - Lists all admin shops
- `/fs admin price tier <item> <tier>` - Sets an item's pricing tier
- `/fs admin price set <item> <price>` - Sets a specific price for an item
- `/fs admin stats` - Shows statistics about shops and transactions
- `/fs admin template manage` - Opens the template management interface
- `/fs admin template backup` - Creates a backup of all templates
- `/fs admin template restore <backup>` - Restores templates from a backup

### Player Commands

- `/fs shop create <n>` - Creates a new player shop
- `/fs shop delete <n>` - Deletes a player shop
- `/fs shop open <n>` - Opens a shop
- `/fs shop list` - Lists all available shops
- `/fs balance` - Shows your current balance
- `/fs template` - Opens the template management interface
- `/fs template list` - Lists all available templates
- `/fs template create <n>` - Creates a new template
- `/fs template import <shop>` - Creates a template from an existing shop

## Permissions

- `frizzlenshop.admin` - Access to all admin commands
- `frizzlenshop.shop.create` - Ability to create player shops
- `frizzlenshop.shop.use` - Ability to use shops
- `frizzlenshop.shop.delete` - Ability to delete own shops
- `frizzlenshop.templates.view` - Ability to view shop templates
- `frizzlenshop.templates.create` - Ability to create shop templates
- `frizzlenshop.templates.edit` - Ability to edit own templates
- `frizzlenshop.templates.delete` - Ability to delete own templates
- `frizzlenshop.admin.templates` - Ability to manage all templates (admin)

## API for Developers

FrizzlenShop provides an API for developers to integrate their plugins:

```java
// Get FrizzlenShop instance
FrizzlenShop frizzlenShop = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get shop manager
ShopManager shopManager = frizzlenShop.getShopManager();

// Get economy manager
EconomyManager economyManager = frizzlenShop.getEconomyManager();

// Get template manager
TemplateManager templateManager = frizzlenShop.getTemplateManager();
```

## Troubleshooting

If you encounter any issues:

1. Check the server console for error messages
2. Verify your configuration settings
3. Ensure you have Vault and a compatible economy plugin installed
4. For database issues, check connection settings in the config file
5. For template issues, see the [Template Management Guide](docs/template-management.md)

## Support and Contributions

For support, please visit our [Discord server](https://discord.gg/) or open an issue on our [GitHub repository](https://github.com/).

Contributions are welcome! Please feel free to submit a pull request.

## License

FrizzlenShop is licensed under the [MIT License](LICENSE).