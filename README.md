# FrizzlenShop

A comprehensive shop system for Minecraft servers, providing an intuitive GUI-based shopping experience that integrates seamlessly with FrizzlenEco.

## Features

- **Shop Types**
  - Admin Shops with infinite stock
  - Player Shops with limited stock and rental system
  
- **Core Functionality**
  - Beautiful, intuitive GUI interface
  - Category-based item browsing
  - Buy and sell items with ease
  - Multi-currency support via FrizzlenEco
  - Shop statistics tracking
  - Transaction logging
  
- **Advanced Features**
  - Shop rental system with auto-renewal
  - Market analysis tools
  - Customizable tax rates
  - Transaction history tracking
  - Extensive permission system

## Installation

1. **Prerequisites**
   - Minecraft server running Paper 1.21+
   - FrizzlenEco plugin installed
   
2. **Install Method 1: Maven (Recommended)**
   - Clone this repository
   - Run `mvn clean package`
   - Copy the generated JAR from the `target/` folder to your server's `plugins/` folder

3. **Install Method 2: Direct Download**
   - Download the latest JAR file from the releases page
   - Place it in your server's `plugins/` folder

4. **Post-Installation**
   - Start your server
   - The plugin will generate configuration files

## Configuration

### Basic Configuration (config.yml)

```yaml
# General settings
general:
  default-tax-rate: 5.0
  default-currency: "dollars"
  maintenance-mode: false
  shop-creation-cost: 1000.0
  max-shops-per-player: 3

# Admin shop settings
admin-shops:
  enabled: true
  infinite-stock: true
  tax-rate: 3.0
  prefix: "&c[Admin] "

# Player shop settings
player-shops:
  enabled: true
  tax-rate: 7.0
  max-items: 27
  rental-period: 7
  rental-cost: 500.0
  auto-renew: true
```

### Categories

Edit the categories section in the config.yml to customize shop categories:

```yaml
categories:
  tools:
    icon: DIAMOND_PICKAXE
    name: "&e&lTools"
    description: "Mining and crafting tools"
  food:
    icon: BREAD
    name: "&e&lFood"
    description: "Edible items"
  # Add more categories as needed
```

## Commands

### Player Commands

- `/shop` - Open the main shop menu
- `/shop browse [category]` - Browse shops by category
- `/shop search <query>` - Search for specific items
- `/shop create` - Start shop creation wizard
- `/shop manage` - Manage your shops
- `/shop history` - View your transaction history
- `/shop sell <item> [amount] [price]` - Quick-sell items
- `/shop buy <item> [amount]` - Quick-buy items
- `/shop help` - Show help message

### Admin Commands

- `/shopadmin create <name>` - Create an admin shop
- `/shopadmin remove <shop-id>` - Remove a shop
- `/shopadmin edit <shop-id>` - Edit shop settings
- `/shopadmin price <shop-id> <buy> <sell> [currency]` - Set prices
- `/shopadmin reload` - Reload configuration
- `/shopadmin logs <player> [timeframe]` - View transaction logs
- `/shopadmin tax <rate>` - Set global tax rate
- `/shopadmin maintenance <on|off>` - Toggle maintenance mode

## Permissions

### Basic Permissions

- `frizzlenshop.use` - Allow use of the shop system (default: true)
- `frizzlenshop.create` - Allow creation of personal shops (default: true)
- `frizzlenshop.sell` - Allow selling items to shops (default: true)
- `frizzlenshop.buy` - Allow buying items from shops (default: true)

### Advanced Permissions

- `frizzlenshop.create.limit.*` - Set shop limit tiers:
  - `frizzlenshop.create.limit.1`
  - `frizzlenshop.create.limit.3`
  - `frizzlenshop.create.limit.5`
  - `frizzlenshop.create.limit.10`

- `frizzlenshop.create.size.*` - Set shop size tiers:
  - `frizzlenshop.create.size.9`
  - `frizzlenshop.create.size.18`
  - `frizzlenshop.create.size.27`
  - `frizzlenshop.create.size.54`

### Admin Permissions

- `frizzlenshop.admin` - Full access to all shop features (includes all permissions below)
- `frizzlenshop.admin.create` - Create admin shops
- `frizzlenshop.admin.edit` - Edit any shop
- `frizzlenshop.admin.remove` - Remove any shop
- `frizzlenshop.admin.prices` - Override prices
- `frizzlenshop.admin.logs` - View all transaction logs
- `frizzlenshop.admin.tax` - Manage tax settings
- `frizzlenshop.admin.maintenance` - Control maintenance mode

## FrizzlenEco Integration

FrizzlenShop integrates with FrizzlenEco for all economy operations:

- Multi-currency support
- Transaction handling
- Balance checking
- Shop account management

### Dependency Setup

If you're building from source, ensure FrizzlenEco is properly set up:

1. Use the included `install_frizzleneco.bat` to install FrizzlenEco to your local Maven repository, or
2. Update the `pom.xml` with the correct path to your FrizzlenEco JAR file

## Troubleshooting

### Common Issues

**Issue**: Missing artifact org.frizzlenpop:FrizzlenEco:jar:1.0-SNAPSHOT  
**Solution**: Run the included `install_frizzleneco.bat` file or adjust the system path in pom.xml

**Issue**: Commands not working  
**Solution**: Check if the plugin is properly enabled in the console. Verify permissions.

**Issue**: GUI not opening  
**Solution**: Check console for errors. Ensure you have the correct permissions.

## Support

If you encounter any issues or have questions:

1. Check the troubleshooting section above
2. Open an issue on our GitHub repository
3. Contact the development team at [support email]

## License

[Your license information here] 