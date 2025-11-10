/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import com.pixydevelopment.pxCalendar.editor.ConfigSaver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * This menu opens when an admin clicks a slot in the CalendarSlotEditorGUI.
 * It asks *what* they want to set that slot as (Day, Filler, Static).
 */
public class SlotEditMenu extends BaseEditorGUI {

    private final Calendar calendar;
    private final int editingSlot;

    public SlotEditMenu(PxCalendarPlugin plugin, Player player, Calendar calendar, int editingSlot) {
        super(plugin, player);
        this.calendar = calendar;
        this.editingSlot = editingSlot;
    }

    @Override
    public void open() {
        String title = lang.getMessage("editor.title-slot-edit-menu").replace("%slot%", String.valueOf(editingSlot));
        inventory = Bukkit.createInventory(this, 3 * 9, title);

        // Set as Day (Slot 10)
        inventory.setItem(10, new ItemBuilder(Material.GREEN_WOOL)
                .name(lang.getMessage("editor.slot-edit-menu.set-day.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-day.lore"))
                .build());

        // Set as Filler (Slot 12)
        inventory.setItem(12, new ItemBuilder(Material.GRAY_DYE)
                .name(lang.getMessage("editor.slot-edit-menu.set-filler.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-filler.lore"))
                .build());

        // Set as Static Item (Slot 14)
        inventory.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(lang.getMessage("editor.slot-edit-menu.set-static.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-static.lore"))
                .build());

        // Clear Slot (Slot 16)
        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.slot-edit-menu.clear-slot.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.clear-slot.lore"))
                .build());

        // Back Button (Slot 22)
        inventory.setItem(22, new ItemBuilder(Material.ARROW)
                .name(lang.getMessage("editor.slot-editor.back-to-cal-editor"))
                .build());

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ConfigSaver saver = new ConfigSaver(plugin, calendar);

        switch (slot) {
            case 10: // Set as Day
                // Open the Day Selector
                new DaySelectorGUI(plugin, player, calendar, editingSlot).open();
                return; // Don't refresh this menu

            case 12: // Set as Filler
                saver.setSlotAsFiller(editingSlot);
                break;

            case 14: // Set as Static
                // TODO: This needs a chat session to ask for the item ID (e.g., 'exit-button')
                lang.sendMessage(player, "&cSetting static items is coming soon!");
                return; // Don't close

            case 16: // Clear Slot
                saver.clearSlot(editingSlot, true);
                break;

            case 22: // Back
                new CalendarSlotEditorGUI(plugin, player, calendar).open();
                return;

            default:
                return;
        }

        // Go back to the main slot editor after a choice (except "Set as Day")
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        new CalendarSlotEditorGUI(plugin, player, plugin.getGuiManager().getCalendar(calendar.getId())).open();
    }
}