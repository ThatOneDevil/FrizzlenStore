# Template Management Guide

This guide is intended for server administrators who need to manage the template system in FrizzlenShop. It covers advanced configuration, administrative tasks, and troubleshooting.

## Administrative Tasks

As an administrator with the `frizzlenshop.admin.templates` permission, you have access to all template functions, including:

- Creating, editing, and deleting any template (including those created by other players)
- Creating admin shop templates that are available to all players
- Managing template categories
- Creating and restoring template backups

## Admin Templates

Admin templates are special templates that are:

1. Available to all players with the `frizzlenshop.templates.view` permission
2. Used to create admin shops (server-owned shops)
3. Only editable by administrators

### Creating Admin Templates

To create an admin template:

1. Access the Template Management menu
2. Click "Create New Template"
3. Select "Admin Shop Template"
4. Fill in the template details
5. Add items to the template

Admin templates are a powerful way to standardize shops across your server and ensure consistent pricing.

### Default Templates

You can set up default templates that are automatically available to all players. These templates can provide balanced shop configurations for new players.

To create default templates:

1. Create the templates as admin templates
2. In the `templates.yml` file, set the `isDefault` property to `true` for these templates
3. Restart the server

## Template Backups

Regular backups of templates are important to prevent data loss. The template system includes automatic backup functionality.

### Automatic Backups

Automatic backups are configured in `config.yml`:

```yaml
templates:
  auto_backup: true  # Enable/disable automatic backups
  backup_interval: 24  # Backup interval in hours
  backup_limit: 10  # Maximum number of backups to keep
```

Backups are stored in the `plugins/FrizzlenShop/backups/templates` directory with timestamps in the filename.

### Manual Backups

To create a manual backup:

1. In-game: `/fs template backup`
2. Console: `fs template backup`

### Restoring from Backup

To restore templates from a backup:

1. In-game: `/fs template restore <backup-name>`
2. Console: `fs template restore <backup-name>`

You can list available backups with:
- In-game: `/fs template backups`
- Console: `fs template backups`

## Template Categories Management

Administrators can manage template categories to keep the templates organized.

### Adding Categories

Categories are created automatically when a template is assigned to a new category. There is no need to pre-create categories.

### Renaming Categories

To rename a category:

1. Use the console command: `fs template category rename <old-name> <new-name>`

This will update all templates assigned to the old category.

### Merging Categories

To merge two categories:

1. Use the console command: `fs template category merge <source-category> <target-category>`

All templates from the source category will be moved to the target category.

## Template Permissions Management

Administrators can manage who has access to templates through permissions.

### Permission Nodes

- `frizzlenshop.templates.view` - Allows viewing templates
- `frizzlenshop.templates.create` - Allows creating templates
- `frizzlenshop.templates.edit` - Allows editing own templates
- `frizzlenshop.templates.delete` - Allows deleting own templates
- `frizzlenshop.admin.templates` - Grants full access to all template features

### Permission Groups

Here are recommended permission setups for different user types:

1. **Regular Players**:
   - `frizzlenshop.templates.view`

2. **Trusted Players**:
   - `frizzlenshop.templates.view`
   - `frizzlenshop.templates.create`

3. **Shop Managers**:
   - `frizzlenshop.templates.view`
   - `frizzlenshop.templates.create`
   - `frizzlenshop.templates.edit`
   - `frizzlenshop.templates.delete`

4. **Administrators**:
   - `frizzlenshop.admin.templates` (includes all template permissions)

## Troubleshooting

### Common Issues

1. **Templates Not Saving**:
   - Check file permissions on `templates.yml`
   - Ensure the plugin has write access to the directory

2. **Template Items Missing**:
   - Verify that the items were properly added to the template
   - Check for any errors in the console during template creation

3. **Players Can't See Templates**:
   - Verify they have the `frizzlenshop.templates.view` permission
   - Check if the templates are admin templates (they should be visible to all with view permission)

4. **Template Creation Failing**:
   - Ensure players have the correct permissions
   - Check for name conflicts with existing templates

### Console Commands

These commands can help diagnose template issues:

- `fs debug templates` - Shows diagnostic information about templates
- `fs repair templates` - Attempts to repair the template system if corrupted
- `fs template check <template-id>` - Validates a specific template for errors

## Advanced Configuration

### Template File Structure

The `templates.yml` file uses the following structure:

```yaml
templates:
  template-uuid:
    id: template-uuid
    name: "Template Name"
    description: "Template Description"
    isAdminTemplate: true/false
    creationTime: timestamp
    creator: "PlayerName"
    category: "Category"
    version: 1
    metadata:
      key1: value1
      key2: value2
    items:
      0:
        item: serialized-item-data
        buyPrice: 100.0
        sellPrice: 50.0
        currency: "default"
        stock: -1  # -1 means unlimited
      1:
        item: serialized-item-data
        buyPrice: 200.0
        sellPrice: 100.0
        currency: "default"
        stock: 100
```

### Performance Considerations

The template system is designed to be lightweight, but with many templates, it can impact performance. Consider:

1. Limiting the maximum number of templates per player
2. Using a scheduled task to save templates rather than saving on every change
3. Regularly cleaning up unused templates

## API Examples

Here are some examples of using the template API in your own plugins:

```java
// Import the necessary classes
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.templates.TemplateManager;
import org.frizzlenpop.frizzlenShop.templates.ShopTemplate;
import org.frizzlenpop.frizzlenShop.templates.TemplateItem;

// Get the template manager
FrizzlenShop plugin = (FrizzlenShop) Bukkit.getPluginManager().getPlugin("FrizzlenShop");
TemplateManager templateManager = plugin.getTemplateManager();

// Create a new template
ShopTemplate template = new ShopTemplate("My Template", "A description", false, "PlayerName");
template.setCategory("Weapons");

// Add an item to the template
ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
template.addItem(sword, 100.0, 50.0, "default", 10);

// Save the template
templateManager.addTemplate(template);

// Get templates by category
List<ShopTemplate> weaponsTemplates = templateManager.getTemplatesByCategory("Weapons");

// Create a shop from a template
templateManager.createShopFromTemplate(templateId, "Sword Shop", playerUuid, location);
``` 