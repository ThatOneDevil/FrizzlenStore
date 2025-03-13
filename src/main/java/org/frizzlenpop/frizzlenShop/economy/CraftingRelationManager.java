package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;

import java.util.*;

/**
 * Manages relationships between crafted items and their components
 * for more realistic economic simulation
 */
public class CraftingRelationManager {
    
    private final FrizzlenShop plugin;
    
    // Maps crafted items to their component materials and quantities
    private final Map<Material, Map<Material, Integer>> craftingComponents;
    
    // Maps components to all items they're used in
    private final Map<Material, Set<Material>> componentUsages;
    
    // Component demand multiplier (how much component prices are affected by crafted item demand)
    private double componentDemandMultiplier = 0.4; // Default value
    
    /**
     * Creates a new crafting relation manager
     * 
     * @param plugin The plugin instance
     */
    public CraftingRelationManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.craftingComponents = new HashMap<>();
        this.componentUsages = new HashMap<>();
        
        // Load component relationships
        loadDefaultRecipes();
        
        // Apply custom crafting relationships from config
        loadCustomRecipes();
        
        plugin.getLogger().info("Loaded " + craftingComponents.size() + " crafting relationships for dynamic pricing");
    }
    
    /**
     * Loads default Minecraft recipes into the relationship maps
     */
    private void loadDefaultRecipes() {
        // Diamond Pickaxe (3 Diamonds + 2 Sticks)
        Map<Material, Integer> pickaxeComponents = new HashMap<>();
        pickaxeComponents.put(Material.DIAMOND, 3);
        pickaxeComponents.put(Material.STICK, 2);
        craftingComponents.put(Material.DIAMOND_PICKAXE, pickaxeComponents);
        
        // Diamond Sword (2 Diamonds + 1 Stick)
        Map<Material, Integer> swordComponents = new HashMap<>();
        swordComponents.put(Material.DIAMOND, 2);
        swordComponents.put(Material.STICK, 1);
        craftingComponents.put(Material.DIAMOND_SWORD, swordComponents);
        
        // Diamond Axe (3 Diamonds + 2 Sticks)
        Map<Material, Integer> axeComponents = new HashMap<>();
        axeComponents.put(Material.DIAMOND, 3);
        axeComponents.put(Material.STICK, 2);
        craftingComponents.put(Material.DIAMOND_AXE, axeComponents);
        
        // Diamond Shovel (1 Diamond + 2 Sticks)
        Map<Material, Integer> shovelComponents = new HashMap<>();
        shovelComponents.put(Material.DIAMOND, 1);
        shovelComponents.put(Material.STICK, 2);
        craftingComponents.put(Material.DIAMOND_SHOVEL, shovelComponents);
        
        // Diamond Hoe (2 Diamonds + 2 Sticks)
        Map<Material, Integer> hoeComponents = new HashMap<>();
        hoeComponents.put(Material.DIAMOND, 2);
        hoeComponents.put(Material.STICK, 2);
        craftingComponents.put(Material.DIAMOND_HOE, hoeComponents);
        
        // Diamond Helmet (5 Diamonds)
        Map<Material, Integer> helmetComponents = new HashMap<>();
        helmetComponents.put(Material.DIAMOND, 5);
        craftingComponents.put(Material.DIAMOND_HELMET, helmetComponents);
        
        // Diamond Chestplate (8 Diamonds)
        Map<Material, Integer> chestplateComponents = new HashMap<>();
        chestplateComponents.put(Material.DIAMOND, 8);
        craftingComponents.put(Material.DIAMOND_CHESTPLATE, chestplateComponents);
        
        // Diamond Leggings (7 Diamonds)
        Map<Material, Integer> leggingsComponents = new HashMap<>();
        leggingsComponents.put(Material.DIAMOND, 7);
        craftingComponents.put(Material.DIAMOND_LEGGINGS, leggingsComponents);
        
        // Diamond Boots (4 Diamonds)
        Map<Material, Integer> bootsComponents = new HashMap<>();
        bootsComponents.put(Material.DIAMOND, 4);
        craftingComponents.put(Material.DIAMOND_BOOTS, bootsComponents);
        
        // Add more recipes as needed
        
        // Build the reverse mapping (components to crafted items)
        buildComponentUsageMap();
    }
    
    /**
     * Builds the component usage map from the crafting components map
     */
    private void buildComponentUsageMap() {
        componentUsages.clear();
        
        for (Map.Entry<Material, Map<Material, Integer>> entry : craftingComponents.entrySet()) {
            Material craftedItem = entry.getKey();
            Map<Material, Integer> components = entry.getValue();
            
            for (Material component : components.keySet()) {
                componentUsages.computeIfAbsent(component, k -> new HashSet<>()).add(craftedItem);
            }
        }
    }
    
    /**
     * Loads custom recipes from config
     */
    private void loadCustomRecipes() {
        // Implementation would load custom recipes from config
        // For now, we'll leave this as a stub for future enhancement
    }
    
    /**
     * Gets the components required to craft an item
     * 
     * @param material The crafted item material
     * @return Map of component materials to quantities, or empty map if not a crafted item
     */
    public Map<Material, Integer> getComponents(Material material) {
        return craftingComponents.getOrDefault(material, Collections.emptyMap());
    }
    
    /**
     * Gets all items that use a component in crafting
     * 
     * @param component The component material
     * @return Set of materials that use this component, or empty set if not used
     */
    public Set<Material> getItemsUsingComponent(Material component) {
        return componentUsages.getOrDefault(component, Collections.emptySet());
    }
    
    /**
     * Checks if a material is a crafted item with known components
     * 
     * @param material The material to check
     * @return True if this is a crafted item with known components
     */
    public boolean isCraftedItem(Material material) {
        return craftingComponents.containsKey(material);
    }
    
    /**
     * Checks if a material is used as a component in crafting
     * 
     * @param material The material to check
     * @return True if this material is used as a component
     */
    public boolean isComponent(Material material) {
        return componentUsages.containsKey(material);
    }
    
    /**
     * Gets the component demand multiplier
     * This controls how much component prices are affected by crafted item demand
     * 
     * @return The component demand multiplier
     */
    public double getComponentDemandMultiplier() {
        return componentDemandMultiplier;
    }
    
    /**
     * Sets the component demand multiplier
     * 
     * @param multiplier The new multiplier value
     */
    public void setComponentDemandMultiplier(double multiplier) {
        this.componentDemandMultiplier = multiplier;
    }
    
    /**
     * Calculates the theoretical craft value of an item based on its components
     * 
     * @param material The crafted item
     * @param marketAnalyzer The market analyzer to get component prices
     * @return The calculated craft value, or -1 if not a crafted item
     */
    public double calculateCraftValue(Material material, MarketAnalyzer marketAnalyzer) {
        Map<Material, Integer> components = getComponents(material);
        if (components.isEmpty()) {
            return -1.0;
        }
        
        double totalValue = 0.0;
        for (Map.Entry<Material, Integer> entry : components.entrySet()) {
            Material component = entry.getKey();
            int quantity = entry.getValue();
            
            // Get the market price for this component
            double componentPrice = marketAnalyzer.getBasePrice(component);
            totalValue += componentPrice * quantity;
        }
        
        // Add a small crafting fee (10%)
        return totalValue * 1.1;
    }
} 