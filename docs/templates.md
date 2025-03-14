# Shop Templates

The Shop Templates system allows players and administrators to create, save, and reuse shop configurations. Templates simplify the process of creating and setting up shops with predefined items, prices, and settings.

## Overview

Shop templates serve as blueprints for creating new shops quickly. A template contains all the necessary information to set up a shop, including:

- Shop type (player shop or admin shop)
- Items with buy/sell prices
- Stock settings
- Category information

Templates can be created by players (with proper permissions) or administrators. Admin templates are available to all players, while player templates are personal to the creator unless shared.

## Using Templates

### Accessing Templates

Templates can be accessed from the main menu by clicking on the "Shop Templates" button. This will open the Template Management screen.

From the template management menu, you can:

- Browse templates by category
- View your own templates
- View admin templates
- Create new templates
- Import templates from existing shops
- Backup and restore templates

### Creating a Shop from a Template

1. Navigate to the Template Management menu
2. Browse and select your desired template
3. Click on "Use Template"
4. Follow the prompts to place your new shop
5. The shop will be created with all items and settings from the template

## Creating Templates

### New Template

To create a new template:

1. In the Template Management menu, click "Create New Template"
2. Select whether to create a Player Shop or Admin Shop template
   - Admin shop templates require the `frizzlenshop.admin.templates` permission
3. Enter a name for your template
4. Enter a description
5. Select a category
6. Add items to your template, setting the buy/sell prices and stock

### From Existing Shop

To create a template from an existing shop:

1. In the Template Management menu, click "Create From Existing Shop"
2. Select the shop you want to use as a template
3. Enter a name and description for the template
4. The template will be created with all items and settings from the selected shop

## Managing Templates

### Editing Templates

If you have the appropriate permissions, you can edit templates:

1. Select the template you want to edit
2. Click the "Edit Template" button
3. Make your changes to the template details or items
4. Save your changes

### Deleting Templates

To delete a template:

1. Select the template you want to delete
2. Click the "Delete Template" button
3. Confirm the deletion

Note that you can only delete your own templates unless you have admin permissions.

## Template Categories

Templates are organized by categories to make them easier to find. When creating a template, you can assign it to one of the existing categories or create a new one.

Common categories include:

- Tools
- Weapons
- Armor
- Food
- Blocks
- Potions
- Resources
- Miscellaneous

## Permissions

The following permissions control access to the template features:

- `frizzlenshop.templates.view` - Allows viewing templates
- `frizzlenshop.templates.create` - Allows creating templates
- `frizzlenshop.templates.edit` - Allows editing own templates
- `frizzlenshop.templates.delete` - Allows deleting own templates
- `frizzlenshop.admin.templates` - Grants full access to all template features (admin)

## Configuration

Template settings can be configured in `config.yml`:

```yaml
templates:
  enabled: true  # Enable/disable the template system
  max_per_player: 10  # Maximum number of templates a player can create
  auto_backup: true  # Automatically backup templates
  backup_interval: 24  # Backup interval in hours
```

## Data Storage

Templates are stored in `templates.yml` in the plugin directory. This file contains all template data, including:

- Template ID
- Name
- Description
- Creator
- Creation time
- Items with prices and stock
- Category
- Version

## Commands

The following commands are available for managing templates:

- `/fs template list` - Lists all available templates
- `/fs template create <name>` - Creates a new empty template
- `/fs template import <shop> <name>` - Creates a template from an existing shop
- `/fs template delete <name>` - Deletes a template
- `/fs template backup` - Creates a backup of all templates
- `/fs template restore <backup>` - Restores templates from a backup

## API for Developers

FrizzlenShop provides an API for developers to integrate with the template system:

```java
// Get the template manager
TemplateManager templateManager = frizzlenShop.getTemplateManager();

// Get all templates
Collection<ShopTemplate> allTemplates = templateManager.getAllTemplates();

// Get templates by category
List<ShopTemplate> weaponTemplates = templateManager.getTemplatesByCategory("weapons");

// Create a template from a shop
ShopTemplate template = templateManager.createTemplateFromShop(shop, "My Template", "A description", playerName);

// Create a shop from a template
Shop shop = templateManager.createShopFromTemplate(templateId, "My Shop", playerUuid, location);
``` 