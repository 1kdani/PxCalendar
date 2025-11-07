/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.editor.ConfigSaver;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * GUI for selecting which Day (1-31) to assign to a slot.
 */
public class DaySelectorGUI extends BaseEditorGUI {

    private final Calendar calendar;
    private final int editingSlot;
    private int page = 0; // For future pagination

    public DaySelectorGUI(PxCalendarPlugin plugin, Player player, Calendar calendar, int editingSlot) {
        super(plugin, player);
        this.calendar = calendar;
        this.editingSlot = editingSlot;
    }

    @Override
    public void open() {
        createInventory(5, "editor.title-day-selector"); // 5 rows = 45 slots

        Map<Integer, Integer> daySlotMap = calendar.getDaySlotMap(); // Get map of (Day -> Slot)

        // We show 31 days (max for a month)
        for (int day = 1; day <= 31; day++) {
            if (day > 45) break; // GUI limit

            if (daySlotMap.containsKey(day)) {
                // This day is already in use
                int usedSlot = daySlotMap.get(day);
                ItemStack item = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .name("&c&lDay " + day)
                        .lore(List.of(
                                "&7This day is already assigned to slot &e" + usedSlot,
                                (usedSlot == editingSlot) ? "&a(This is the current slot)" : "&c(Click to override)"
                        )).build();
                inventory.setItem(day - 1, item);
            } else {
                // This day is available
                ItemStack item = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .name("&a&lDay " + day)
                        .lore(List.of(
                                "&7Click to assign Day &e" + day + "&7 to slot &e" + editingSlot
                        )).build();
                inventory.setItem(day - 1, item);
            }
        }

        // Back button
        inventory.setItem(40, new ItemBuilder(Material.ARROW)
                .name(lang.getMessage("editor.slot-editor.back-to-cal-editor"))
                .build());

        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int clickedSlot = event.getSlot();

        if (clickedSlot == 40) { // Back button
            new CalendarSlotEditorGUI(plugin, player, calendar).open();
            return;
        }

        if (clickedSlot >= 0 && clickedSlot <= 30) { // Clicked Day 1-31
            int dayNumber = clickedSlot + 1;

            // Save the change
            ConfigSaver saver = new ConfigSaver(plugin, calendar);
            saver.setSlotAsDay(editingSlot, dayNumber);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);

            // Re-open the main slot editor to show the change
            new CalendarSlotEditorGUI(plugin, player, plugin.getGuiManager().getCalendar(calendar.getId())).open();
        }
    }
}