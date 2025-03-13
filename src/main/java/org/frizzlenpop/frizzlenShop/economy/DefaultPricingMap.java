package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides default pricing for all items in admin shops
 * Organized by game progression tiers
 */
public class DefaultPricingMap {
    
    // Maps materials to their pricing tier and multiplier
    private static final Map<Material, ItemPricing> pricingMap = new HashMap<>();
    
    // Static initialization block to populate the pricing map
    static {
        initializeStarterItems();
        initializeEarlyGameItems();
        initializeMidGameItems();
        initializeLateGameItems();
        initializeEndGameItems();
        initializeLuxuryItems();
    }
    
    /**
     * Gets the pricing for a material
     * 
     * @param material The material to get pricing for
     * @return The item pricing, or null if no specific pricing exists
     */
    public static ItemPricing getPricing(Material material) {
        return pricingMap.getOrDefault(material, new ItemPricing(PricingTier.STARTER, 1.0));
    }
    
    /**
     * Gets the buy price for a material
     * 
     * @param material The material to get price for
     * @return The calculated buy price
     */
    public static double getBuyPrice(Material material) {
        ItemPricing pricing = getPricing(material);
        return pricing.getTier().calculatePrice(pricing.getMultiplier());
    }
    
    /**
     * Gets the sell price for a material
     * 
     * @param material The material to get sell price for
     * @return The calculated sell price
     */
    public static double getSellPrice(Material material) {
        ItemPricing pricing = getPricing(material);
        return pricing.getTier().calculateSellPrice(pricing.getMultiplier());
    }
    
    /**
     * Initialize starter tier items (0-100 coins)
     * Basic resources and tools that players need at the start
     */
    private static void initializeStarterItems() {
        // Basic blocks
        pricingMap.put(Material.DIRT, new ItemPricing(PricingTier.STARTER, 0.5));      // 2.5 coins
        pricingMap.put(Material.SAND, new ItemPricing(PricingTier.STARTER, 0.5));      // 2.5 coins
        pricingMap.put(Material.GRAVEL, new ItemPricing(PricingTier.STARTER, 0.5));    // 2.5 coins
        pricingMap.put(Material.COBBLESTONE, new ItemPricing(PricingTier.STARTER, 0.4)); // 2 coins
        pricingMap.put(Material.STONE, new ItemPricing(PricingTier.STARTER, 0.6));     // 3 coins
        
        // Wood types
        pricingMap.put(Material.OAK_LOG, new ItemPricing(PricingTier.STARTER, 1.0));   // 5 coins
        pricingMap.put(Material.SPRUCE_LOG, new ItemPricing(PricingTier.STARTER, 1.0)); // 5 coins
        pricingMap.put(Material.BIRCH_LOG, new ItemPricing(PricingTier.STARTER, 1.0));  // 5 coins
        pricingMap.put(Material.JUNGLE_LOG, new ItemPricing(PricingTier.STARTER, 1.0)); // 5 coins
        pricingMap.put(Material.ACACIA_LOG, new ItemPricing(PricingTier.STARTER, 1.0)); // 5 coins
        pricingMap.put(Material.DARK_OAK_LOG, new ItemPricing(PricingTier.STARTER, 1.0)); // 5 coins
        
        // Basic foods
        pricingMap.put(Material.WHEAT_SEEDS, new ItemPricing(PricingTier.STARTER, 0.6)); // 3 coins
        pricingMap.put(Material.WHEAT, new ItemPricing(PricingTier.STARTER, 1.0));     // 5 coins
        pricingMap.put(Material.APPLE, new ItemPricing(PricingTier.STARTER, 1.5));     // 7.5 coins
        pricingMap.put(Material.BREAD, new ItemPricing(PricingTier.STARTER, 2.0));     // 10 coins
        pricingMap.put(Material.CARROT, new ItemPricing(PricingTier.STARTER, 1.0));    // 5 coins
        pricingMap.put(Material.POTATO, new ItemPricing(PricingTier.STARTER, 1.0));    // 5 coins
        
        // Basic tools
        pricingMap.put(Material.WOODEN_SWORD, new ItemPricing(PricingTier.STARTER, 2.0));   // 10 coins
        pricingMap.put(Material.WOODEN_PICKAXE, new ItemPricing(PricingTier.STARTER, 2.0)); // 10 coins
        pricingMap.put(Material.WOODEN_AXE, new ItemPricing(PricingTier.STARTER, 2.0));     // 10 coins
        pricingMap.put(Material.WOODEN_SHOVEL, new ItemPricing(PricingTier.STARTER, 1.5));  // 7.5 coins
        pricingMap.put(Material.WOODEN_HOE, new ItemPricing(PricingTier.STARTER, 1.5));     // 7.5 coins
        
        pricingMap.put(Material.STONE_SWORD, new ItemPricing(PricingTier.STARTER, 4.0));    // 20 coins
        pricingMap.put(Material.STONE_PICKAXE, new ItemPricing(PricingTier.STARTER, 4.0));  // 20 coins
        pricingMap.put(Material.STONE_AXE, new ItemPricing(PricingTier.STARTER, 4.0));      // 20 coins
        pricingMap.put(Material.STONE_SHOVEL, new ItemPricing(PricingTier.STARTER, 3.0));   // 15 coins
        pricingMap.put(Material.STONE_HOE, new ItemPricing(PricingTier.STARTER, 3.0));      // 15 coins
        
        // Crafting blocks
        pricingMap.put(Material.CRAFTING_TABLE, new ItemPricing(PricingTier.STARTER, 5.0)); // 25 coins
        pricingMap.put(Material.FURNACE, new ItemPricing(PricingTier.STARTER, 8.0));       // 40 coins
    }
    
    /**
     * Initialize early game items (100-500 coins)
     * Resources and tools for early progression
     */
    private static void initializeEarlyGameItems() {
        // Ores and minerals
        pricingMap.put(Material.COAL, new ItemPricing(PricingTier.EARLY_GAME, 0.5));      // 25 coins
        pricingMap.put(Material.IRON_ORE, new ItemPricing(PricingTier.EARLY_GAME, 1.0));  // 50 coins
        pricingMap.put(Material.IRON_INGOT, new ItemPricing(PricingTier.EARLY_GAME, 1.5)); // 75 coins
        pricingMap.put(Material.COPPER_ORE, new ItemPricing(PricingTier.EARLY_GAME, 0.7)); // 35 coins
        pricingMap.put(Material.COPPER_INGOT, new ItemPricing(PricingTier.EARLY_GAME, 1.0)); // 50 coins
        
        // Iron tools and weapons
        pricingMap.put(Material.IRON_SWORD, new ItemPricing(PricingTier.EARLY_GAME, 4.0));    // 200 coins
        pricingMap.put(Material.IRON_PICKAXE, new ItemPricing(PricingTier.EARLY_GAME, 5.0));  // 250 coins
        pricingMap.put(Material.IRON_AXE, new ItemPricing(PricingTier.EARLY_GAME, 5.0));      // 250 coins
        pricingMap.put(Material.IRON_SHOVEL, new ItemPricing(PricingTier.EARLY_GAME, 3.0));   // 150 coins
        pricingMap.put(Material.IRON_HOE, new ItemPricing(PricingTier.EARLY_GAME, 3.0));      // 150 coins
        
        // Iron armor
        pricingMap.put(Material.IRON_HELMET, new ItemPricing(PricingTier.EARLY_GAME, 5.0));      // 250 coins
        pricingMap.put(Material.IRON_CHESTPLATE, new ItemPricing(PricingTier.EARLY_GAME, 8.0));  // 400 coins
        pricingMap.put(Material.IRON_LEGGINGS, new ItemPricing(PricingTier.EARLY_GAME, 7.0));    // 350 coins
        pricingMap.put(Material.IRON_BOOTS, new ItemPricing(PricingTier.EARLY_GAME, 4.0));       // 200 coins
        
        // Advanced foods
        pricingMap.put(Material.COOKED_BEEF, new ItemPricing(PricingTier.EARLY_GAME, 1.0));    // 50 coins
        pricingMap.put(Material.COOKED_PORKCHOP, new ItemPricing(PricingTier.EARLY_GAME, 1.0)); // 50 coins
        pricingMap.put(Material.COOKED_CHICKEN, new ItemPricing(PricingTier.EARLY_GAME, 0.8));  // 40 coins
        pricingMap.put(Material.COOKED_MUTTON, new ItemPricing(PricingTier.EARLY_GAME, 0.8));   // 40 coins
        pricingMap.put(Material.BAKED_POTATO, new ItemPricing(PricingTier.EARLY_GAME, 0.6));    // 30 coins
        
        // Utility blocks
        pricingMap.put(Material.CHEST, new ItemPricing(PricingTier.EARLY_GAME, 1.0));         // 50 coins
        pricingMap.put(Material.BARREL, new ItemPricing(PricingTier.EARLY_GAME, 1.2));        // 60 coins
        pricingMap.put(Material.SMOKER, new ItemPricing(PricingTier.EARLY_GAME, 3.0));        // 150 coins
        pricingMap.put(Material.BLAST_FURNACE, new ItemPricing(PricingTier.EARLY_GAME, 4.0)); // 200 coins
        pricingMap.put(Material.COMPOSTER, new ItemPricing(PricingTier.EARLY_GAME, 1.5));     // 75 coins
    }
    
    /**
     * Initialize mid-game items (500-2500 coins)
     * More advanced resources and equipment
     */
    private static void initializeMidGameItems() {
        // Valuable ores and materials
        pricingMap.put(Material.GOLD_ORE, new ItemPricing(PricingTier.MID_GAME, 1.0));      // 250 coins
        pricingMap.put(Material.GOLD_INGOT, new ItemPricing(PricingTier.MID_GAME, 1.5));    // 375 coins
        pricingMap.put(Material.REDSTONE, new ItemPricing(PricingTier.MID_GAME, 0.5));      // 125 coins
        pricingMap.put(Material.LAPIS_LAZULI, new ItemPricing(PricingTier.MID_GAME, 0.8));  // 200 coins
        pricingMap.put(Material.AMETHYST_SHARD, new ItemPricing(PricingTier.MID_GAME, 1.2)); // 300 coins
        
        // Gold equipment
        pricingMap.put(Material.GOLDEN_SWORD, new ItemPricing(PricingTier.MID_GAME, 3.0));      // 750 coins
        pricingMap.put(Material.GOLDEN_PICKAXE, new ItemPricing(PricingTier.MID_GAME, 4.0));    // 1000 coins
        pricingMap.put(Material.GOLDEN_AXE, new ItemPricing(PricingTier.MID_GAME, 4.0));        // 1000 coins
        pricingMap.put(Material.GOLDEN_HELMET, new ItemPricing(PricingTier.MID_GAME, 5.0));     // 1250 coins
        pricingMap.put(Material.GOLDEN_CHESTPLATE, new ItemPricing(PricingTier.MID_GAME, 8.0)); // 2000 coins
        pricingMap.put(Material.GOLDEN_LEGGINGS, new ItemPricing(PricingTier.MID_GAME, 7.0));   // 1750 coins
        pricingMap.put(Material.GOLDEN_BOOTS, new ItemPricing(PricingTier.MID_GAME, 4.0));      // 1000 coins
        
        // Redstone components
        pricingMap.put(Material.REDSTONE_TORCH, new ItemPricing(PricingTier.MID_GAME, 0.6));  // 150 coins
        pricingMap.put(Material.REPEATER, new ItemPricing(PricingTier.MID_GAME, 1.0));        // 250 coins
        pricingMap.put(Material.COMPARATOR, new ItemPricing(PricingTier.MID_GAME, 1.2));      // 300 coins
        pricingMap.put(Material.HOPPER, new ItemPricing(PricingTier.MID_GAME, 2.8));          // 700 coins
        pricingMap.put(Material.DROPPER, new ItemPricing(PricingTier.MID_GAME, 1.0));         // 250 coins
        pricingMap.put(Material.DISPENSER, new ItemPricing(PricingTier.MID_GAME, 1.2));       // 300 coins
        pricingMap.put(Material.OBSERVER, new ItemPricing(PricingTier.MID_GAME, 2.0));        // 500 coins
        pricingMap.put(Material.PISTON, new ItemPricing(PricingTier.MID_GAME, 1.5));          // 375 coins
        pricingMap.put(Material.STICKY_PISTON, new ItemPricing(PricingTier.MID_GAME, 2.0));   // 500 coins
        
        // Transportation
        pricingMap.put(Material.RAIL, new ItemPricing(PricingTier.MID_GAME, 0.2));             // 50 coins
        pricingMap.put(Material.POWERED_RAIL, new ItemPricing(PricingTier.MID_GAME, 1.0));     // 250 coins
        pricingMap.put(Material.DETECTOR_RAIL, new ItemPricing(PricingTier.MID_GAME, 0.6));    // 150 coins
        pricingMap.put(Material.ACTIVATOR_RAIL, new ItemPricing(PricingTier.MID_GAME, 0.6));   // 150 coins
        pricingMap.put(Material.MINECART, new ItemPricing(PricingTier.MID_GAME, 2.0));         // 500 coins
        
        // Advanced utility
        pricingMap.put(Material.BREWING_STAND, new ItemPricing(PricingTier.MID_GAME, 4.0));    // 1000 coins
        pricingMap.put(Material.CAULDRON, new ItemPricing(PricingTier.MID_GAME, 3.0));        // 750 coins
        pricingMap.put(Material.ANVIL, new ItemPricing(PricingTier.MID_GAME, 10.0));          // 2500 coins
        pricingMap.put(Material.ENCHANTING_TABLE, new ItemPricing(PricingTier.MID_GAME, 10.0)); // 2500 coins
    }
    
    /**
     * Initialize late game items (2500-10000 coins)
     * High-tier resources and equipment
     */
    private static void initializeLateGameItems() {
        // High-value materials
        pricingMap.put(Material.DIAMOND, new ItemPricing(PricingTier.LATE_GAME, 1.0));        // 1000 coins
        pricingMap.put(Material.DIAMOND_BLOCK, new ItemPricing(PricingTier.LATE_GAME, 9.0));  // 9000 coins
        pricingMap.put(Material.EMERALD, new ItemPricing(PricingTier.LATE_GAME, 1.2));        // 1200 coins
        pricingMap.put(Material.EMERALD_BLOCK, new ItemPricing(PricingTier.LATE_GAME, 10.0)); // 10000 coins
        
        // Diamond tools and weapons
        pricingMap.put(Material.DIAMOND_SWORD, new ItemPricing(PricingTier.LATE_GAME, 3.0));     // 3000 coins
        pricingMap.put(Material.DIAMOND_PICKAXE, new ItemPricing(PricingTier.LATE_GAME, 4.0));   // 4000 coins
        pricingMap.put(Material.DIAMOND_AXE, new ItemPricing(PricingTier.LATE_GAME, 4.0));       // 4000 coins
        pricingMap.put(Material.DIAMOND_SHOVEL, new ItemPricing(PricingTier.LATE_GAME, 2.0));    // 2000 coins
        pricingMap.put(Material.DIAMOND_HOE, new ItemPricing(PricingTier.LATE_GAME, 2.0));       // 2000 coins
        
        // Diamond armor
        pricingMap.put(Material.DIAMOND_HELMET, new ItemPricing(PricingTier.LATE_GAME, 5.0));      // 5000 coins
        pricingMap.put(Material.DIAMOND_CHESTPLATE, new ItemPricing(PricingTier.LATE_GAME, 8.0));  // 8000 coins
        pricingMap.put(Material.DIAMOND_LEGGINGS, new ItemPricing(PricingTier.LATE_GAME, 7.0));    // 7000 coins
        pricingMap.put(Material.DIAMOND_BOOTS, new ItemPricing(PricingTier.LATE_GAME, 4.0));       // 4000 coins
        
        // Special items
        pricingMap.put(Material.ENDER_PEARL, new ItemPricing(PricingTier.LATE_GAME, 0.5));       // 500 coins
        pricingMap.put(Material.ENDER_EYE, new ItemPricing(PricingTier.LATE_GAME, 1.0));         // 1000 coins
        pricingMap.put(Material.EXPERIENCE_BOTTLE, new ItemPricing(PricingTier.LATE_GAME, 0.7)); // 700 coins
        pricingMap.put(Material.BLAZE_ROD, new ItemPricing(PricingTier.LATE_GAME, 0.6));         // 600 coins
        pricingMap.put(Material.BLAZE_POWDER, new ItemPricing(PricingTier.LATE_GAME, 0.3));      // 300 coins
        
        // Advanced blocks
        pricingMap.put(Material.END_STONE, new ItemPricing(PricingTier.LATE_GAME, 0.1));         // 100 coins
        pricingMap.put(Material.PURPUR_BLOCK, new ItemPricing(PricingTier.LATE_GAME, 0.2));      // 200 coins
        pricingMap.put(Material.CHORUS_FLOWER, new ItemPricing(PricingTier.LATE_GAME, 0.8));     // 800 coins
        pricingMap.put(Material.DRAGON_BREATH, new ItemPricing(PricingTier.LATE_GAME, 5.0));     // 5000 coins
    }
    
    /**
     * Initialize end game items (10000-45000 coins)
     * Top-tier resources and equipment
     */
    private static void initializeEndGameItems() {
        // Nether materials
        pricingMap.put(Material.NETHERITE_SCRAP, new ItemPricing(PricingTier.END_GAME, 0.5));     // 2500 coins
        pricingMap.put(Material.NETHERITE_INGOT, new ItemPricing(PricingTier.END_GAME, 2.0));     // 10000 coins
        pricingMap.put(Material.NETHERITE_BLOCK, new ItemPricing(PricingTier.END_GAME, 9.0));     // 45000 coins
        
        // Netherite tools and weapons
        pricingMap.put(Material.NETHERITE_SWORD, new ItemPricing(PricingTier.END_GAME, 3.0));     // 15000 coins
        pricingMap.put(Material.NETHERITE_PICKAXE, new ItemPricing(PricingTier.END_GAME, 4.0));   // 20000 coins
        pricingMap.put(Material.NETHERITE_AXE, new ItemPricing(PricingTier.END_GAME, 4.0));       // 20000 coins
        pricingMap.put(Material.NETHERITE_SHOVEL, new ItemPricing(PricingTier.END_GAME, 2.0));    // 10000 coins
        pricingMap.put(Material.NETHERITE_HOE, new ItemPricing(PricingTier.END_GAME, 2.0));       // 10000 coins
        
        // Netherite armor
        pricingMap.put(Material.NETHERITE_HELMET, new ItemPricing(PricingTier.END_GAME, 5.0));     // 25000 coins
        pricingMap.put(Material.NETHERITE_CHESTPLATE, new ItemPricing(PricingTier.END_GAME, 8.0)); // 40000 coins
        pricingMap.put(Material.NETHERITE_LEGGINGS, new ItemPricing(PricingTier.END_GAME, 7.0));   // 35000 coins
        pricingMap.put(Material.NETHERITE_BOOTS, new ItemPricing(PricingTier.END_GAME, 4.0));      // 20000 coins
        
        // Special end-game items
        pricingMap.put(Material.BEACON, new ItemPricing(PricingTier.END_GAME, 9.0));             // 45000 coins
        pricingMap.put(Material.CONDUIT, new ItemPricing(PricingTier.END_GAME, 6.0));           // 30000 coins
        pricingMap.put(Material.SHULKER_BOX, new ItemPricing(PricingTier.END_GAME, 4.0));       // 20000 coins
        pricingMap.put(Material.ELYTRA, new ItemPricing(PricingTier.END_GAME, 8.0));            // 40000 coins
        pricingMap.put(Material.TRIDENT, new ItemPricing(PricingTier.END_GAME, 6.0));           // 30000 coins
        pricingMap.put(Material.NETHER_STAR, new ItemPricing(PricingTier.END_GAME, 7.0));       // 35000 coins
        pricingMap.put(Material.END_CRYSTAL, new ItemPricing(PricingTier.END_GAME, 5.0));       // 25000 coins
    }
    
    /**
     * Initialize luxury items (45000+ coins)
     * The rarest and most prestigious items
     */
    private static void initializeLuxuryItems() {
        // Super rare decorative blocks
        pricingMap.put(Material.DRAGON_EGG, new ItemPricing(PricingTier.LUXURY, 3.5));         // 52500 coins
        pricingMap.put(Material.COMMAND_BLOCK, new ItemPricing(PricingTier.LUXURY, 10.0));     // 150000 coins
        
        // Special enchanted items would be handled separately through enchantment system
    }
    
    /**
     * Inner class for storing pricing information for an item
     */
    public static class ItemPricing {
        private final PricingTier tier;
        private final double multiplier;
        
        /**
         * Creates a new item pricing
         * 
         * @param tier The pricing tier for this item
         * @param multiplier The price multiplier (relative to base tier price)
         */
        public ItemPricing(PricingTier tier, double multiplier) {
            this.tier = tier;
            this.multiplier = multiplier;
        }
        
        /**
         * Gets the pricing tier for this item
         * 
         * @return The pricing tier
         */
        public PricingTier getTier() {
            return tier;
        }
        
        /**
         * Gets the price multiplier for this item
         * 
         * @return The price multiplier
         */
        public double getMultiplier() {
            return multiplier;
        }
    }
} 