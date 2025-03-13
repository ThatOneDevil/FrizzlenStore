# Database Management

FrizzlenShop's Database Management system provides robust data storage and retrieval capabilities, ensuring shop and transaction data persists across server restarts.

[← Back to Main Documentation](../README.md)

## Overview

The Database Management module handles all data persistence for FrizzlenShop, including shops, items, transactions, and market analysis data. It supports both SQLite (for simple setups) and MySQL (for larger servers and multi-server networks).

## Features

### Multiple Database Support

The system supports two database types:

- **SQLite**: Default option, stores data in a local file
- **MySQL**: For larger servers or networks, stores data in a remote database

### Comprehensive Data Storage

The Database Manager stores:

- Shop information (name, location, type, owner)
- Shop items (with pricing and stock data)
- Transaction history
- Market trend data
- Item transaction statistics

### Data Migration

The system includes tools for:

- Automatic database schema updates
- Data conversion between storage formats
- Data export and import

### Connection Pooling

For MySQL connections, the system uses connection pooling to:

- Reduce connection overhead
- Prevent connection leaks
- Improve performance under load

## Database Schema

The database includes several tables:

### Shops Table
```sql
CREATE TABLE IF NOT EXISTS shops (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(32) NOT NULL,
  type VARCHAR(10) NOT NULL,
  owner VARCHAR(36),
  location TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_open BOOLEAN DEFAULT 1
)
```

### Shop Items Table
```sql
CREATE TABLE IF NOT EXISTS shop_items (
  id VARCHAR(36) PRIMARY KEY,
  shop_id VARCHAR(36) NOT NULL,
  item_data TEXT NOT NULL,
  price DOUBLE NOT NULL,
  stock INT DEFAULT -1,
  FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
)
```

### Transactions Table
```sql
CREATE TABLE IF NOT EXISTS transactions (
  id VARCHAR(36) PRIMARY KEY,
  shop_id VARCHAR(36) NOT NULL,
  player_id VARCHAR(36) NOT NULL,
  item_id VARCHAR(36) NOT NULL,
  quantity INT NOT NULL,
  price DOUBLE NOT NULL,
  type VARCHAR(10) NOT NULL,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES shop_items(id) ON DELETE CASCADE
)
```

### Market Trends Table
```sql
CREATE TABLE IF NOT EXISTS market_trends (
  material VARCHAR(64) PRIMARY KEY,
  demand_index DOUBLE,
  supply_index DOUBLE,
  volatility DOUBLE,
  last_updated BIGINT
)
```

### Item Transactions Table
```sql
CREATE TABLE IF NOT EXISTS item_transactions (
  item_id VARCHAR(36) PRIMARY KEY,
  material VARCHAR(64),
  buy_count INT,
  sell_count INT,
  last_buy_time BIGINT,
  last_sell_time BIGINT,
  price_adjustment_factor DOUBLE
)
```

## Configuration

Database settings can be configured in `config.yml`:

```yaml
database:
  # Database type (sqlite or mysql)
  type: sqlite
  
  # For SQLite only - database file path
  path: frizzlenshop.db
  
  # For MySQL only - connection settings
  host: localhost
  port: 3306
  name: frizzlenshop
  username: root
  password: ''
  
  # Table prefix for all tables
  table_prefix: fs_
  
  # Connection pool settings (MySQL only)
  pool_size: 10
  max_lifetime: 1800000
  
  # Backup settings
  auto_backup: true
  backup_interval: 86400
  max_backups: 7
```

## Data Serialization

The Database Manager includes methods for serializing and deserializing Bukkit objects:

- `serializeLocation(Location)`: Converts a location to a string
- `deserializeLocation(String)`: Converts a string back to a location
- `serializeItemStack(ItemStack)`: Converts an item to a string
- `deserializeItemStack(String)`: Converts a string back to an item

This allows complex objects to be stored in the database.

## Data Operations

The `DatabaseManager` class provides methods for:

### Shop Operations
- `saveShop(Shop)`: Saves a shop to the database
- `loadShops()`: Loads all shops from the database
- `deleteShop(UUID)`: Deletes a shop from the database

### Item Operations
- `saveShopItem(ShopItem)`: Saves a shop item
- `loadShopItems(Shop)`: Loads items for a shop
- `deleteShopItem(UUID)`: Deletes a shop item

### Transaction Operations
- `recordTransaction(...)`: Records a transaction
- `getTransactions(UUID, int, int)`: Gets transactions for a shop
- `getPlayerTransactions(UUID)`: Gets a player's transactions

### Market Operations
- `updateMarketTrends(...)`: Updates market trend data
- `getMarketData(Material)`: Gets market data for a material

## Database Maintenance

The plugin includes tools for database maintenance:

- **Automatic Backups**: Creates periodic database backups
- **Data Cleaning**: Removes old transaction data after a configurable period
- **Integrity Checks**: Verifies database integrity and repairs issues

## Integration with Other Features

The Database Manager integrates with:

- **[Admin Shop System](admin-shop.md)**: Stores admin shop data
- **[Player Shop System](player-shop.md)**: Stores player shop data
- **[Dynamic Pricing](dynamic-pricing.md)**: Stores market trend data
- **[Market Analysis](market-analysis.md)**: Provides transaction data for analysis

## Developer API

Developers can interact with the database system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get database manager
DatabaseManager dbManager = plugin.getDatabaseManager();

// Get a database connection
Connection conn = dbManager.getConnection();

// Get the table prefix
String tablePrefix = dbManager.getTablePrefix();

// Execute a custom query
String query = "SELECT * FROM " + tablePrefix + "shops WHERE owner = ?";
PreparedStatement ps = conn.prepareStatement(query);
ps.setString(1, playerUUID.toString());
ResultSet rs = ps.executeQuery();

// Don't forget to close your resources
rs.close();
ps.close();
conn.close();

// Record a transaction using the built-in method
dbManager.recordTransaction(
    shopId,
    playerId,
    itemId,
    quantity,
    price,
    "buy"
);
```

## Troubleshooting

### Common Database Issues

#### Cannot Connect to MySQL

If you're having trouble connecting to MySQL:

1. Verify MySQL server is running
2. Check connection settings in config.yml
3. Ensure the database exists and the user has proper permissions
4. Check for firewalls blocking the connection

#### Data Corruption

If data appears corrupted:

1. Use the `/fs admin db repair` command to attempt repair
2. Restore from the latest backup if available
3. Check server logs for disk space issues

#### Performance Issues

If database operations are slow:

1. Consider switching to MySQL for large servers
2. Increase connection pool size
3. Add appropriate indexes to frequently queried columns
4. Enable query caching
5. Schedule regular cleanups of old transaction data 