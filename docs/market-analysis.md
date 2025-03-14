# Market Analysis System

The Market Analysis system in FrizzlenShop tracks transaction data, analyzes economic trends, and provides insights for both players and administrators.

[← Back to Main Documentation](../README.md)

## Overview

The Market Analysis system monitors all shop transactions, detects patterns, and provides various tools to understand the server economy. This helps players make profitable decisions and administrators maintain a balanced economy.

## Features

### Transaction Tracking

The system records detailed information about every transaction:

- Item purchased/sold
- Quantity
- Price
- Transaction type (buy/sell)
- Player involved
- Timestamp

This data forms the foundation for all market analysis features.

### Market Trend Detection

The `MarketAnalyzer` class processes transaction data to identify trends:

- **Rising Prices**: Items whose prices are increasing
- **Falling Prices**: Items whose prices are decreasing
- **High Demand**: Items frequently purchased by players
- **High Supply**: Items frequently sold to shops

These trends are displayed in a dedicated Market Trends GUI accessible via `/fs market trends`.

### Crafting Profitability Analysis

For crafted items, the system analyzes:

- Cost of crafting components
- Current market value of the crafted item
- Profit margin percentage

This data helps players identify profitable crafting opportunities, available via `/fs market crafting`.

## Supply and Demand Indices

Each material has two key metrics:

1. **Demand Index**: Represents player demand for the item
   - Range: 0.5 to 2.0
   - Above 1.0 indicates high demand
   - Below 1.0 indicates low demand

2. **Supply Index**: Represents item availability in the economy
   - Range: 0.5 to 2.0
   - Above 1.0 indicates high supply
   - Below 1.0 indicates low supply

These indices directly influence dynamic pricing when enabled.

## Price Recommendations

The system can suggest appropriate prices for player shops based on:

- Base price of the material
- Current market trends
- Recent transaction history
- Crafting relationships

Players can access these recommendations when setting prices in their shops.

## Market Analysis GUIs

### Market Trends Menu

The Market Trends GUI shows:

- Items with the highest price increases
- Items with the largest price decreases
- Most frequently traded items
- General market health indicators

This GUI is accessible to players and administrators through `/fs market trends`.

### Crafting Opportunities Menu

The Crafting Opportunities GUI displays:

- Items that are profitable to craft
- Current profit margin percentage
- Required crafting components
- Recommended crafting quantity

This helps players quickly identify profitable crafting activities.

## Database Storage

Market data is stored in two main tables:

1. **market_trends**:
   - Global supply/demand data for each material
   - Updated with each transaction

2. **item_transactions**:
   - Transaction history for specific items
   - Used to calculate item-specific adjustment factors

## Integration with Other Features

Market Analysis integrates closely with:

- **[Dynamic Pricing](dynamic-pricing.md)**: Provides data for price adjustments
- **[Admin Shop System](admin-shop.md)**: Records and analyzes admin shop transactions
- **[Player Shop System](player-shop.md)**: Records and analyzes player shop transactions
- **[Crafting System](crafting.md)**: Analyzes crafting relationships and profit margins
- **[Shop Templates](templates.md)**: Uses market data to create optimized templates

## Templates and Market Analysis

The Market Analysis system integrates with the Shop Templates feature to provide data-driven template creation and management:

### Market-Optimized Templates

Administrators can create market-optimized templates based on current market analysis:

1. **Trending Items Templates**: Automatically create templates containing items with rising prices
2. **High-Demand Templates**: Generate templates with the most frequently purchased items
3. **Profitable Items Templates**: Create templates with items that have the highest profit margins

### Template Analysis Reports

The system can generate analysis reports for existing templates:

1. **Profitability Analysis**: Evaluate the overall profitability of items in a template
2. **Market Alignment**: Compare template prices with current market prices
3. **Trend Prediction**: Predict how template items might perform in the future

### Template Recommendations

The Market Analyzer provides recommendations for template management:

1. **Template Updates**: Suggests when templates should be updated based on market shifts
2. **Item Recommendations**: Suggests items to add or remove from templates
3. **Price Adjustments**: Recommends price adjustments for template items

To access these features, use the Template Management menu via `/fs admin template manage` and select the "Market Analysis" option.

## Configuration

Market Analysis settings can be configured in `config.yml`:

```yaml
market_analysis:
  # How often to update market analysis (in minutes)
  update_interval: 60
  
  # How many days of transaction history to consider
  analysis_period_days: 7
  
  # Whether to show market trends to players
  show_trends_to_players: true
  
  # Whether to show crafting opportunities to players
  show_crafting_to_players: true
  
  # Maximum number of trends to display
  max_trends_displayed: 10
  
  # Minimum profit margin to show crafting opportunities (percentage)
  min_profit_margin: 10.0
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs market trends` | Opens the market trends GUI |
| `/fs market crafting` | Opens the crafting opportunities GUI |
| `/fs admin market reset` | Resets market analysis data |
| `/fs admin market analyze` | Forces a market analysis update |
| `/fs admin market report` | Generates a detailed market report |

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlenshop.market.view` | Allows viewing market trends |
| `frizzlenshop.market.crafting` | Allows viewing crafting opportunities |
| `frizzlenshop.admin.market` | Admin access to market analysis features |

## Developer API

Developers can interact with the market analysis system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get market analyzer
MarketAnalyzer marketAnalyzer = plugin.getMarketAnalyzer();

// Get market trend data
Map<Material, Double> trends = marketAnalyzer.getMarketTrendSummary();

// Get top trending items (price increases)
Map<Material, Double> topTrends = marketAnalyzer.getTopTrendingItems(10);

// Get item market data
MarketAnalyzer.MarketData marketData = marketAnalyzer.getMarketData(Material.DIAMOND);
double demandIndex = marketData.getDemandIndex();
double supplyIndex = marketData.getSupplyIndex();

// Get crafting profit margin for an item
double profitMargin = marketAnalyzer.getCraftingProfitMargin(Material.DIAMOND_SWORD);

// Get suggested price for a material
double suggestedPrice = marketAnalyzer.getSuggestedPrice(Material.DIAMOND, true); // true = buy price
```

## Performance Considerations

The Market Analysis system includes several optimizations:

- Caching of market data to reduce database queries
- Asynchronous market analysis to prevent server lag
- Configurable analysis periods to control data size
- Gradual normalization of market indices
- Transaction data pruning for long-term performance 