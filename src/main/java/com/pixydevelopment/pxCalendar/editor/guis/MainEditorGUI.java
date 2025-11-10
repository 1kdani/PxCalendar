/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil; // Importálva
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * The main menu for the /pxc editor command.
 */
public class MainEditorGUI extends BaseEditorGUI {

    public MainEditorGUI(PxCalendarPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        createInventory(3, "editor.title-main"); // 3 rows, Title from lang.yml

        // Calendar List Item (Slot 11)
        ItemStack calendars = new ItemBuilder(Material.CHEST)
                .name(lang.getMessage("editor.main-menu.calendars.name"))
                .lore(lang.getLangConfig().getStringList("editor.main-menu.calendars.lore"))
                .build();
        inventory.setItem(11, calendars);

        // Reward Editor Item (Slot 13)
        ItemStack rewards = new ItemBuilder(Material.DIAMOND)
                .name(lang.getMessage("editor.main-menu.rewards.name"))
                .lore(lang.getLangConfig().getStringList("editor.main-menu.rewards.lore"))
                .build();
        inventory.setItem(13, rewards);

        // General Settings Item (Slot 15)
        ItemStack config = new ItemBuilder(Material.COMPARATOR)
                .name(lang.getMessage("editor.main-menu.config.name"))
                .lore(lang.getLangConfig().getStringList("editor.main-menu.config.lore"))
                .build();
        inventory.setItem(15, config);

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Calendar List
                new CalendarListGUI(plugin, player).open();
                break;
            case 13: // Reward Editor
                // JAVÍTVA: Megnyitja a Jutalom FÁJL Listát
                new RewardFileListGUI(plugin, player).open();
                break;
            case 15: // General Settings
                new ConfigEditorGUI(plugin, player).open();
                break;
        }
    }
}