# Dynamic Pricing System

The Dynamic Pricing system in FrizzlenShop adjusts item prices based on supply and demand, creating a realistic and self-balancing economy.

[← Back to Main Documentation](../README.md)

## Overview

Dynamic pricing automatically adjusts item prices in shops based on transaction history, market trends, and economic factors. This creates a living economy where prices reflect actual server activity rather than static values.

## How Dynamic Pricing Works

### Core Principles

1. **Supply and Demand**: When an item is bought frequently, its price increases. When it's sold frequently to shops, its price decreases.

2. **Market Indices**: Each material has associated demand and supply indices that track market activity.

3. **Volatility**: Different items have different volatility rates, determining how quickly their prices change.

4. **Price Normalization**: Prices gradually return to baseline over time without activity.

5. **Crafting Relationships**: Crafted items' prices are influenced by their component materials.

## Market Data Tracking

The system tracks several key metrics:

- **Buy/Sell Counts**: Number of times an item has been bought or sold
- **Transaction Times**: When transactions occurred
- **Demand Index**: How in-demand an item is (range: 0.5-2.0)
- **Supply Index**: How well-supplied an item is (range: 0.5-2.0)
- **Recent Transactions**: Recent buy/sell activity

## Price Calculation

Dynamic prices are calculated using the following factors:

1. **Base Price**: The static base price of an item
2. **Market Multiplier**: Based on demand and supply indices
3. **Item-Specific Adjustment**: Based on the item's transaction history
4. **Crafting Value**: For crafted items, considering component costs
5. **Fluctuation Factor**: Time-based natural price variation

The calculation looks like:

```
finalPrice = basePrice × marketMultiplier × itemMultiplier × craftValueMultiplier × (1 + fluctuation)
```

With limits to prevent prices from changing too dramatically (max ±50% from base price).

## Crafting Relationship Analysis

For crafted items, the system considers:

- The cost of crafting components
- A crafting fee (typically 15% of component cost)
- The relationship between crafted items and their components

When a crafted item is purchased, it increases demand for its components. When components are purchased, it can affect the crafted item's price.

## Market Analysis

The `MarketAnalyzer` class provides several tools for analyzing the economy:

- **Trend Analysis**: Track which items are increasing or decreasing in price
- **Profitable Crafting**: Identify items that are profitable to craft
- **Price Suggestions**: Get recommended prices for player shops

This data is available to administrators and can be shown to players through the market trends GUI.

## Configuration

Dynamic pricing can be configured in `config.yml`:

```yaml
dynamic_pricing:
  # Enable or disable dynamic pricing
  enabled: true
  
  # How quickly prices change (higher = more volatile)
  volatility_multiplier: 1.0
  
  # How often to perform market analysis (in minutes)
  analysis_interval: 60
  
  # Maximum price change allowed (as a percentage of base price)
  max_price_change: 0.5
  
  # How quickly prices normalize when no activity (higher = faster)
  normalization_rate: 0.1
  
  # Whether crafting relationships affect pricing
  use_crafting_relationships: true
  
  # Price fluctuation settings
  fluctuation:
    enabled: true
    magnitude: 0.05
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs admin pricing toggle` | Enables or disables dynamic pricing |
| `/fs admin pricing analyze` | Forces a market analysis update |
| `/fs admin pricing reset <material>` | Resets pricing data for a material |
| `/fs market trends` | Opens the market trends GUI |
| `/fs market crafting` | Shows profitable crafting opportunities |

## Permissions

| Permission | Description |
|------------|-------------|
| `frizzlenshop.admin.pricing` | Access to pricing administration |
| `frizzlenshop.market.view` | Ability to view market trends |

## Market Trends GUI

The plugin provides a GUI for viewing market trends:

- Items with rising prices
- Items with falling prices
- Most traded items
- Profitable crafting opportunities

This information helps players make smart economic decisions.

## Database Schema

Dynamic pricing data is stored in two database tables:

1. **market_trends**: Stores global material market data
   - material (Primary Key)
   - demand_index
   - supply_index
   - volatility
   - last_updated
   
2. **item_transactions**: Stores specific item transaction history
   - item_id (Primary Key)
   - material
   - buy_count
   - sell_count
   - last_buy_time
   - last_sell_time
   - price_adjustment_factor

## Integration with Other Features

Dynamic pricing integrates with:

- **[Admin Shop System](admin-shop.md)**: Provides base prices and shop infrastructure
- **[Market Analysis](market-analysis.md)**: Uses and feeds data to the market analyzer
- **[Crafting System](crafting.md)**: Considers crafting relationships in pricing

## Developer API

Developers can interact with the dynamic pricing system:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get market analyzer
MarketAnalyzer marketAnalyzer = plugin.getMarketAnalyzer();

// Record a transaction
ShopItem item = /* get shop item */;
marketAnalyzer.recordTransaction(item, 5, true); // true = buy, false = sell

// Get market data for a material
MarketAnalyzer.MarketData marketData = marketAnalyzer.getMarketData(Material.DIAMOND);
double demandIndex = marketData.getDemandIndex();
double supplyIndex = marketData.getSupplyIndex();

// Get price trend information
Map<Material, Double> trends = marketAnalyzer.getMarketTrendSummary();
Map<Material, Double> topTrends = marketAnalyzer.getTopTrendingItems(10);

// Calculate dynamic price
double dynamicPrice = marketAnalyzer.calculateDynamicPrice(basePrice, item, true);
``` 