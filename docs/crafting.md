# Crafting System

The Crafting System in FrizzlenShop analyzes crafting relationships between items to provide dynamic pricing and profitability insights.

[← Back to Main Documentation](../README.md)

## Overview

The Crafting System analyzes the relationships between crafted items and their components, allowing the plugin to provide realistic pricing based on crafting costs and identify profitable crafting opportunities for players.

## Features

### Crafting Relationship Analysis

The system models crafting relationships by:

- Identifying which items are craftable
- Tracking component materials needed for crafting
- Calculating the base cost of crafting an item
- Determining the profit margin for crafting

### Component Price Propagation

When prices change, the effects propagate through the crafting chain:

- When a component's price increases, crafted items using that component become more expensive
- When a crafted item is in high demand, its components may increase in value
- The propagation strength is controlled by a configurable multiplier

### Crafting Profitability Detection

The system automatically identifies profitable crafting opportunities based on:

- Current market prices of components
- Current market prices of crafted items
- Crafting costs and fees
- Recent market trends

These opportunities are displayed to players in the Crafting Opportunities GUI.

## How It Works

### Crafting Detection

The `ShopItem` class includes methods to support crafting analysis:

- `isCrafted()`: Determines if an item is considered craftable
- `getCraftingComponents()`: Returns a map of components and quantities
- `getCraftingMultiplier()`: Gets the market influence multiplier
- `getCraftingValue()`: Calculates the value based on components

### Crafting Cost Calculation

When calculating crafting costs, the system:

1. Identifies all required components and quantities
2. Gets the current market value of each component
3. Multiplies each component value by its quantity
4. Adds all component costs together
5. Applies a crafting fee (typically 15%)

This produces a realistic crafting cost that reflects market conditions.

### Profit Margin Calculation

Profit margins are calculated using the formula:

```
profitMargin = ((marketValue - craftCost) / craftCost) * 100.0
```

Where:
- `marketValue` is the current selling price of the crafted item
- `craftCost` is the total cost of crafting components plus crafting fee

This produces a percentage that shows how profitable crafting an item is.

## Crafting Opportunities GUI

The Crafting Opportunities GUI (`/fs market crafting`) shows:

- Items that are currently profitable to craft
- The current profit margin percentage
- Required crafting components and their costs
- Visual indicators for profitability levels

Items are sorted by profitability, with the most profitable items at the top.

## Integration with Dynamic Pricing

The Crafting System integrates with the [Dynamic Pricing](dynamic-pricing.md) feature:

1. **Price Floors**: Crafted items typically won't drop below their crafting cost
2. **Component Demand**: When crafted items are frequently purchased, component demand increases
3. **Price Propagation**: Price changes flow through crafting relationships

This creates a realistic economy where raw materials and crafted goods have coherent price relationships.

## Configuration

Crafting system settings can be configured in `config.yml`:

```yaml
crafting:
  # Whether to use crafting relationships in pricing
  use_crafting_relationships: true
  
  # Default crafting fee (percentage added to component costs)
  crafting_fee: 0.15
  
  # How strongly crafted item demand affects component prices
  component_demand_multiplier: 0.25
  
  # Minimum profit margin to show crafting opportunities (percentage)
  min_profit_margin: 10.0
  
  # Maximum craftable items to display in the GUI
  max_displayed_opportunities: 27
  
  # Whether to show crafting costs in the shop GUI
  show_crafting_cost_in_shop: true
```

## Commands

| Command | Description |
|---------|-------------|
| `/fs market crafting` | Opens the crafting opportunities GUI |
| `/fs admin crafting reload` | Reloads crafting recipes from config |
| `/fs admin crafting add <material> <components...>` | Adds or updates a custom crafting recipe |
| `/fs admin crafting remove <material>` | Removes a custom crafting recipe |
| `/fs admin crafting list` | Lists all custom crafting recipes |

## Performance Considerations

The Crafting System includes optimizations to prevent performance issues:

- Component data is cached for frequently accessed items
- Profit calculations are performed asynchronously 
- Results are cached for configurable periods
- Custom recipes can be defined for commonly used items

## Developer API

Developers can interact with the crafting system programmatically:

```java
// Get FrizzlenShop instance
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");

// Get market analyzer
MarketAnalyzer marketAnalyzer = plugin.getMarketAnalyzer();

// Check if an item is profitable to craft
double profitMargin = marketAnalyzer.getCraftingProfitMargin(Material.DIAMOND_PICKAXE);
if (profitMargin > 10.0) {
    // Item is profitable to craft (more than 10% margin)
}

// Get crafting components for an item
ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
ShopItem shopItem = new ShopItem(pickaxe, 100.0, 80.0, "coin", -1);
Map<Material, Integer> components = shopItem.getCraftingComponents();

// Calculate crafting value
double craftingValue = shopItem.getCraftingValue();

// Check if item is crafted
boolean isCrafted = shopItem.isCrafted();
```

## Custom Crafting Recipes

Server administrators can define custom crafting recipes in the config to override the default ones:

```yaml
custom_recipes:
  DIAMOND_SWORD:
    components:
      DIAMOND: 2
      STICK: 1
    
  BEACON:
    components:
      NETHER_STAR: 1
      OBSIDIAN: 3
      GLASS: 5
```

This is useful for:
- Adding recipes for items that don't have standard crafting recipes
- Adjusting component quantities for game balance
- Creating server-specific special items

## Integration with Other Features

The Crafting System integrates with several other features:

- **[Dynamic Pricing](dynamic-pricing.md)**: Uses crafting costs in price calculations
- **[Market Analysis](market-analysis.md)**: Provides crafting profitability data
- **[Admin Shop System](admin-shop.md)**: Ensures admin shop prices reflect crafting costs
- **[Player Shop System](player-shop.md)**: Suggests profitable items for players to craft and sell 