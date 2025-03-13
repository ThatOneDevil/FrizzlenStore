package org.frizzlenpop.frizzlenShop.economy;

/**
 * Defines pricing tiers for items in the economy
 * Used to categorize items for pricing in Admin Shops
 */
public enum PricingTier {
    /**
     * Basic items available at game start (0-100 coins)
     * Examples: Dirt, Wood, Stone
     */
    STARTER(5, 3, "Starter items for new players"),
    
    /**
     * Early game items (100-500 coins)
     * Examples: Iron, Coal, Basic Tools
     */
    EARLY_GAME(50, 35, "Early game resources and tools"),
    
    /**
     * Mid game items (500-2500 coins)
     * Examples: Gold, Redstone, Diamond Tools
     */
    MID_GAME(250, 175, "Mid-game materials and equipment"),
    
    /**
     * Late game items (2500-10000 coins)
     * Examples: Diamond, Emerald, Special Materials
     */
    LATE_GAME(1000, 700, "Advanced materials and equipment"),
    
    /**
     * End game items (10000-45000 coins)
     * Examples: Netherite, Beacons, End Crystals
     */
    END_GAME(5000, 3500, "End-game rare materials and special items"),
    
    /**
     * Luxury items (>45000 coins)
     * Examples: Special enchanted items, Collectors items
     */
    LUXURY(15000, 10500, "Luxury and prestige items");
    
    private final double basePrice;
    private final double baseSellPrice;
    private final String description;
    
    PricingTier(double basePrice, double baseSellPrice, String description) {
        this.basePrice = basePrice;
        this.baseSellPrice = baseSellPrice;
        this.description = description;
    }
    
    /**
     * Gets the base buy price for items in this tier
     * 
     * @return The base buy price
     */
    public double getBasePrice() {
        return basePrice;
    }
    
    /**
     * Gets the base sell price for items in this tier
     * 
     * @return The base sell price
     */
    public double getBaseSellPrice() {
        return baseSellPrice;
    }
    
    /**
     * Gets the description of this tier
     * 
     * @return The tier description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Calculates a specific price for an item in this tier
     * 
     * @param multiplier The item-specific multiplier
     * @return The calculated price
     */
    public double calculatePrice(double multiplier) {
        return basePrice * multiplier;
    }
    
    /**
     * Calculates a specific sell price for an item in this tier
     * 
     * @param multiplier The item-specific multiplier
     * @return The calculated sell price
     */
    public double calculateSellPrice(double multiplier) {
        return baseSellPrice * multiplier;
    }
} 