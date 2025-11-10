/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI for editing boolean values in config.yml
 */
public class ConfigEditorGUI extends BaseEditorGUI {

    public ConfigEditorGUI(PxCalendarPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        createInventory(4, "editor.title-config");

        // Load current config values
        FileConfiguration config = plugin.getConfigManager().getConfig();

        // Non-editable info items
        inventory.setItem(10, createDisplayItem(Material.CLOCK, "editor.config-menu.timezone", "%value%", config.getString("calendar.timezone")));

        // JAVÍTÁS: A Material.CALENDAR (ami 1.20.5+ anyag) cserélve Material.MAP-ra (ami 1.16.5-ben létezik)
        inventory.setItem(11, createDisplayItem(Material.MAP, "editor.config-menu.start-date", "%value%", config.getString("calendar.start-date")));

        inventory.setItem(12, createDisplayItem(Material.HOPPER, "editor.config-menu.ip-limit", "%value%", String.valueOf(config.getInt("calendar.ip-claim-limit"))));

        // Toggleable boolean items
        inventory.setItem(19, createToggleItem(config.getBoolean("calendar.allow-late-claims"), "editor.config-menu.late-claim"));
        inventory.setItem(20, createToggleItem(config.getBoolean("reminders.enable"), "editor.config-menu.reminders"));
        inventory.setItem(21, createToggleItem(config.getBoolean("physical-calendars.enable"), "editor.config-menu.holograms"));

        // Back Button
        ItemStack back = new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button"))
                .build();
        inventory.setItem(31, back);

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        FileConfiguration config = plugin.getConfigManager().getConfig();

        switch (slot) {
            case 19: // Toggle Late Claims
                toggleBoolean(config, "calendar.allow-late-claims");
                break;
            case 20: // Toggle Reminders
                toggleBoolean(config, "reminders.enable");
                break;
            case 21: // Toggle Holograms
                toggleBoolean(config, "physical-calendars.enable");
                break;
            case 31: // Back button
                new MainEditorGUI(plugin, player).open();
                return; // Don't refresh
            default:
                return; // Clicked filler
        }

        // Save config and refresh GUI
        plugin.getConfigManager().saveConfig();
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        open(); // Refresh the GUI to show new status
    }

    /**
     * Toggles a boolean value in the config.
     */
    private void toggleBoolean(FileConfiguration config, String path) {
        boolean currentValue = config.getBoolean(path);
        config.set(path, !currentValue);
    }

    /**
     * Creates an item that just displays information (non-clickable).
     */
    private ItemStack createDisplayItem(Material material, String langPath, String placeholder, String value) {
        String name = lang.getMessage(langPath + ".name");
        List<String> lore = lang.getLangConfig().getStringList(langPath + ".lore").stream()
                .map(line -> line.replace(placeholder, value))
                .collect(Collectors.toList());

        return new ItemBuilder(material).name(name).lore(lore).build();
    }

    /**
     * Creates a toggleable (boolean) item (LIME_DYE / GRAY_DYE).
     */
    private ItemStack createToggleItem(boolean enabled, String langPath) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? lang.getMessage("editor.status-enabled") : lang.getMessage("editor.status-disabled");

        String name = lang.getMessage(langPath + ".name");
        List<String> lore = lang.getLangConfig().getStringList(langPath + ".lore").stream()
                .map(line -> line.replace("%status%", status))
                .collect(Collectors.toList());

        return new ItemBuilder(material).name(name).lore(lore).build();
    }
}