package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.economy.DefaultPricingMap;
import org.frizzlenpop.frizzlenShop.economy.PricingTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Populates the admin shop with items using the defined pricing tiers
 */
public class AdminShopPopulator {

    private final FrizzlenShop plugin;
    
    /**
     * Creates a new admin shop populator
     * 
     * @param plugin The plugin instance
     */
    public AdminShopPopulator(FrizzlenShop plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Creates the main admin shop with all items categorized by tier
     * 
     * @return The created admin shop
     */
    public AdminShop createMainAdminShop() {
        // Create the shop
        AdminShop shop = new AdminShop(
            plugin,
            "Main Admin Shop",
            null // Location will be set later when placed
        );
        
        try {
            // Add items by tier
            addStarterItems(shop);
            addEarlyGameItems(shop);
            addMidGameItems(shop);
            addLateGameItems(shop);
            addEndGameItems(shop);
            addLuxuryItems(shop);
            
            // Register shop with shop manager
            addShopToManager(shop);
            plugin.getLogger().info("Successfully created main admin shop with " + shop.getItems().size() + " items.");
            
            return shop;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create admin shop", e);
            return null;
        }
    }
    
    /**
     * Creates a category-specific admin shop
     * 
     * @param category The category name
     * @return The created category shop
     */
    public AdminShop createCategoryShop(String category) {
        AdminShop shop = new AdminShop(
            plugin,
            category + " Shop",
            null // Location will be set later when placed
        );
        
        try {
            // Add items based on category
            switch (category.toLowerCase()) {
                case "starter":
                    addStarterItems(shop);
                    break;
                case "early_game":
                case "early":
                    addEarlyGameItems(shop);
                    break;
                case "mid_game":
                case "mid":
                    addMidGameItems(shop);
                    break;
                case "late_game":
                case "late":
                    addLateGameItems(shop);
                    break;
                case "end_game":
                case "end":
                    addEndGameItems(shop);
                    break;
                case "luxury":
                    addLuxuryItems(shop);
                    break;
                case "tools":
                    addAllTools(shop);
                    break;
                case "weapons":
                    addAllWeapons(shop);
                    break;
                case "armor":
                    addAllArmor(shop);
                    break;
                case "food":
                    addAllFood(shop);
                    break;
                case "blocks":
                    addAllBlocks(shop);
                    break;
                case "resources":
                    addAllResources(shop);
                    break;
                default:
                    plugin.getLogger().warning("Unknown shop category: " + category);
                    break;
            }
            
            // Register shop with shop manager
            addShopToManager(shop);
            plugin.getLogger().info("Successfully created " + category + " shop with " + shop.getItems().size() + " items.");
            
            return shop;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create category shop: " + category, e);
            return null;
        }
    }
    
    /**
     * Helper method to add shop to the shop manager
     * 
     * @param shop The shop to add
     */
    private void addShopToManager(Shop shop) {
        // Use the registerShop method we just added to ShopManager
        plugin.getShopManager().registerShop(shop);
        plugin.getLogger().info("Shop " + shop.getName() + " registered with ID: " + shop.getId());
    }
    
    /**
     * Adds starter tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addStarterItems(Shop shop) {
        // Basic blocks
        addShopItem(shop, Material.DIRT);
        addShopItem(shop, Material.SAND);
        addShopItem(shop, Material.GRAVEL);
        addShopItem(shop, Material.COBBLESTONE);
        addShopItem(shop, Material.STONE);
        
        // Wood types
        addShopItem(shop, Material.OAK_LOG);
        addShopItem(shop, Material.SPRUCE_LOG);
        addShopItem(shop, Material.BIRCH_LOG);
        addShopItem(shop, Material.JUNGLE_LOG);
        addShopItem(shop, Material.ACACIA_LOG);
        addShopItem(shop, Material.DARK_OAK_LOG);
        
        // Wood planks
        addShopItem(shop, Material.OAK_PLANKS);
        addShopItem(shop, Material.SPRUCE_PLANKS);
        addShopItem(shop, Material.BIRCH_PLANKS);
        addShopItem(shop, Material.JUNGLE_PLANKS);
        addShopItem(shop, Material.ACACIA_PLANKS);
        addShopItem(shop, Material.DARK_OAK_PLANKS);
        
        // Basic foods
        addShopItem(shop, Material.WHEAT_SEEDS);
        addShopItem(shop, Material.WHEAT);
        addShopItem(shop, Material.APPLE);
        addShopItem(shop, Material.BREAD);
        addShopItem(shop, Material.CARROT);
        addShopItem(shop, Material.POTATO);
        
        // Basic tools
        addShopItem(shop, Material.WOODEN_SWORD);
        addShopItem(shop, Material.WOODEN_PICKAXE);
        addShopItem(shop, Material.WOODEN_AXE);
        addShopItem(shop, Material.WOODEN_SHOVEL);
        addShopItem(shop, Material.WOODEN_HOE);
        
        addShopItem(shop, Material.STONE_SWORD);
        addShopItem(shop, Material.STONE_PICKAXE);
        addShopItem(shop, Material.STONE_AXE);
        addShopItem(shop, Material.STONE_SHOVEL);
        addShopItem(shop, Material.STONE_HOE);
        
        // Crafting blocks
        addShopItem(shop, Material.CRAFTING_TABLE);
        addShopItem(shop, Material.FURNACE);
    }
    
    /**
     * Adds early game tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addEarlyGameItems(Shop shop) {
        // Ores and minerals
        addShopItem(shop, Material.COAL);
        addShopItem(shop, Material.IRON_ORE);
        addShopItem(shop, Material.IRON_INGOT);
        addShopItem(shop, Material.COPPER_ORE);
        addShopItem(shop, Material.COPPER_INGOT);
        
        // Iron tools and weapons
        addShopItem(shop, Material.IRON_SWORD);
        addShopItem(shop, Material.IRON_PICKAXE);
        addShopItem(shop, Material.IRON_AXE);
        addShopItem(shop, Material.IRON_SHOVEL);
        addShopItem(shop, Material.IRON_HOE);
        
        // Iron armor
        addShopItem(shop, Material.IRON_HELMET);
        addShopItem(shop, Material.IRON_CHESTPLATE);
        addShopItem(shop, Material.IRON_LEGGINGS);
        addShopItem(shop, Material.IRON_BOOTS);
        
        // Advanced foods
        addShopItem(shop, Material.COOKED_BEEF);
        addShopItem(shop, Material.COOKED_PORKCHOP);
        addShopItem(shop, Material.COOKED_CHICKEN);
        addShopItem(shop, Material.COOKED_MUTTON);
        addShopItem(shop, Material.BAKED_POTATO);
        
        // Utility blocks
        addShopItem(shop, Material.CHEST);
        addShopItem(shop, Material.BARREL);
        addShopItem(shop, Material.SMOKER);
        addShopItem(shop, Material.BLAST_FURNACE);
        addShopItem(shop, Material.COMPOSTER);
    }
    
    /**
     * Adds mid-game tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addMidGameItems(Shop shop) {
        // Valuable ores and materials
        addShopItem(shop, Material.GOLD_ORE);
        addShopItem(shop, Material.GOLD_INGOT);
        addShopItem(shop, Material.REDSTONE);
        addShopItem(shop, Material.LAPIS_LAZULI);
        
        // Gold equipment
        addShopItem(shop, Material.GOLDEN_SWORD);
        addShopItem(shop, Material.GOLDEN_PICKAXE);
        addShopItem(shop, Material.GOLDEN_AXE);
        addShopItem(shop, Material.GOLDEN_HELMET);
        addShopItem(shop, Material.GOLDEN_CHESTPLATE);
        addShopItem(shop, Material.GOLDEN_LEGGINGS);
        addShopItem(shop, Material.GOLDEN_BOOTS);
        
        // Redstone components
        addShopItem(shop, Material.REDSTONE_TORCH);
        addShopItem(shop, Material.REPEATER);
        addShopItem(shop, Material.COMPARATOR);
        addShopItem(shop, Material.HOPPER);
        addShopItem(shop, Material.DROPPER);
        addShopItem(shop, Material.DISPENSER);
        addShopItem(shop, Material.OBSERVER);
        addShopItem(shop, Material.PISTON);
        addShopItem(shop, Material.STICKY_PISTON);
        
        // Transportation
        addShopItem(shop, Material.RAIL);
        addShopItem(shop, Material.POWERED_RAIL);
        addShopItem(shop, Material.DETECTOR_RAIL);
        addShopItem(shop, Material.ACTIVATOR_RAIL);
        addShopItem(shop, Material.MINECART);
        
        // Advanced utility
        addShopItem(shop, Material.BREWING_STAND);
        addShopItem(shop, Material.CAULDRON);
        addShopItem(shop, Material.ANVIL);
        addShopItem(shop, Material.ENCHANTING_TABLE);
    }
    
    /**
     * Adds late game tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addLateGameItems(Shop shop) {
        // High-value materials
        addShopItem(shop, Material.DIAMOND);
        addShopItem(shop, Material.DIAMOND_BLOCK);
        addShopItem(shop, Material.EMERALD);
        addShopItem(shop, Material.EMERALD_BLOCK);
        
        // Diamond tools and weapons
        addShopItem(shop, Material.DIAMOND_SWORD);
        addShopItem(shop, Material.DIAMOND_PICKAXE);
        addShopItem(shop, Material.DIAMOND_AXE);
        addShopItem(shop, Material.DIAMOND_SHOVEL);
        addShopItem(shop, Material.DIAMOND_HOE);
        
        // Diamond armor
        addShopItem(shop, Material.DIAMOND_HELMET);
        addShopItem(shop, Material.DIAMOND_CHESTPLATE);
        addShopItem(shop, Material.DIAMOND_LEGGINGS);
        addShopItem(shop, Material.DIAMOND_BOOTS);
        
        // End materials
        addShopItem(shop, Material.ENDER_PEARL);
        addShopItem(shop, Material.ENDER_EYE);
        addShopItem(shop, Material.EXPERIENCE_BOTTLE);
        addShopItem(shop, Material.BLAZE_ROD);
        addShopItem(shop, Material.BLAZE_POWDER);
        
        // End blocks
        addShopItem(shop, Material.END_STONE);
        addShopItem(shop, Material.PURPUR_BLOCK);
        addShopItem(shop, Material.CHORUS_FLOWER);
        addShopItem(shop, Material.DRAGON_BREATH);
    }
    
    /**
     * Adds end game tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addEndGameItems(Shop shop) {
        // Nether materials
        addShopItem(shop, Material.NETHERITE_SCRAP);
        addShopItem(shop, Material.NETHERITE_INGOT);
        addShopItem(shop, Material.NETHERITE_BLOCK);
        
        // Netherite tools and weapons
        addShopItem(shop, Material.NETHERITE_SWORD);
        addShopItem(shop, Material.NETHERITE_PICKAXE);
        addShopItem(shop, Material.NETHERITE_AXE);
        addShopItem(shop, Material.NETHERITE_SHOVEL);
        addShopItem(shop, Material.NETHERITE_HOE);
        
        // Netherite armor
        addShopItem(shop, Material.NETHERITE_HELMET);
        addShopItem(shop, Material.NETHERITE_CHESTPLATE);
        addShopItem(shop, Material.NETHERITE_LEGGINGS);
        addShopItem(shop, Material.NETHERITE_BOOTS);
        
        // Special end-game items
        addShopItem(shop, Material.BEACON);
        addShopItem(shop, Material.CONDUIT);
        addShopItem(shop, Material.SHULKER_BOX);
        addShopItem(shop, Material.ELYTRA);
        addShopItem(shop, Material.TRIDENT);
        addShopItem(shop, Material.NETHER_STAR);
        addShopItem(shop, Material.END_CRYSTAL);
    }
    
    /**
     * Adds luxury tier items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addLuxuryItems(Shop shop) {
        // Super rare items
        addShopItem(shop, Material.DRAGON_EGG);
        
        // Only add command block in development mode
        if (plugin.getConfigManager().isTestingMode()) {
            addShopItem(shop, Material.COMMAND_BLOCK);
        }
    }
    
    /**
     * Adds all tools to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllTools(Shop shop) {
        // All tiers of tools
        List<Material> tools = Arrays.asList(
            // Wooden tools
            Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE,
            // Stone tools
            Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE,
            // Iron tools
            Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE,
            // Golden tools
            Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE,
            // Diamond tools
            Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
            // Netherite tools
            Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE
        );
        
        for (Material tool : tools) {
            addShopItem(shop, tool);
        }
    }
    
    /**
     * Adds all weapons to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllWeapons(Shop shop) {
        // All tiers of weapons
        List<Material> weapons = Arrays.asList(
            // Swords
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            // Bows
            Material.BOW, Material.CROSSBOW,
            // Other weapons
            Material.TRIDENT
        );
        
        for (Material weapon : weapons) {
            addShopItem(shop, weapon);
        }
    }
    
    /**
     * Adds all armor to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllArmor(Shop shop) {
        // All tiers of armor
        List<Material> armor = Arrays.asList(
            // Leather armor
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            // Iron armor
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            // Golden armor
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
            // Diamond armor
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            // Netherite armor
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
        );
        
        for (Material armorPiece : armor) {
            addShopItem(shop, armorPiece);
        }
    }
    
    /**
     * Adds all food items to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllFood(Shop shop) {
        // Common food items
        List<Material> foods = Arrays.asList(
            Material.APPLE, Material.BREAD, Material.CARROT, Material.POTATO, Material.BAKED_POTATO,
            Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN, Material.COOKED_MUTTON,
            Material.COOKED_COD, Material.COOKED_SALMON, Material.PUMPKIN_PIE, Material.CAKE,
            Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.ENCHANTED_GOLDEN_APPLE
        );
        
        for (Material food : foods) {
            addShopItem(shop, food);
        }
    }
    
    /**
     * Adds common building blocks to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllBlocks(Shop shop) {
        // Common building blocks
        List<Material> blocks = Arrays.asList(
            // Basic blocks
            Material.DIRT, Material.SAND, Material.GRAVEL, Material.STONE, Material.COBBLESTONE,
            Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS,
            // Stone variants
            Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
            Material.SMOOTH_STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE,
            // Colored blocks
            Material.WHITE_WOOL, Material.GRAY_WOOL, Material.BLACK_WOOL, Material.RED_WOOL,
            Material.BLUE_WOOL, Material.GREEN_WOOL, Material.YELLOW_WOOL, Material.PURPLE_WOOL,
            // Glass
            Material.GLASS, Material.WHITE_STAINED_GLASS, Material.BLACK_STAINED_GLASS
        );
        
        for (Material block : blocks) {
            addShopItem(shop, block);
        }
    }
    
    /**
     * Adds various resources and rare materials to a shop
     * 
     * @param shop The shop to add items to
     */
    private void addAllResources(Shop shop) {
        // Resources ordered by value
        List<Material> resources = Arrays.asList(
            // Basic resources
            Material.COAL, Material.IRON_INGOT, Material.COPPER_INGOT,
            // Mid-tier resources
            Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI,
            // High-tier resources
            Material.DIAMOND, Material.EMERALD, Material.QUARTZ,
            // End-game resources
            Material.NETHERITE_INGOT, Material.NETHER_STAR
        );
        
        for (Material resource : resources) {
            addShopItem(shop, resource);
        }
    }
    
    /**
     * Adds a shop item to the given shop with pricing based on the pricing map
     * 
     * @param shop The shop to add the item to
     * @param material The material to add
     * @return The created shop item
     */
    private ShopItem addShopItem(Shop shop, Material material) {
        double buyPrice = DefaultPricingMap.getBuyPrice(material);
        double sellPrice = DefaultPricingMap.getSellPrice(material);
        
        ShopItem item = new ShopItem(
            new ItemStack(material),
            buyPrice,
            sellPrice,
            "coin", // Default currency
            -1 // Infinite stock for admin shops
        );
        
        shop.addItem(item);
        return item;
    }
} 