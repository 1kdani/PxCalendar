/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * The main visual editor for a calendar's layout.
 * This GUI mirrors the actual calendar, but allows editing.
 */
public class CalendarSlotEditorGUI extends BaseEditorGUI {

    private final Calendar calendar;

    public CalendarSlotEditorGUI(PxCalendarPlugin plugin, Player player, Calendar calendar) {
        super(plugin, player);
        this.calendar = calendar;
    }

    @Override
    public void open() {
        String title = lang.getMessage("editor.title-slot-editor").replace("%name%", calendar.getId());
        // We create an inventory one size larger to hold the navigation bar
        int guiRows = calendar.getRows();
        int editorRows = Math.min(6, guiRows + 1); // Max 6 rows

        createInventory(editorRows, title); // A BaseEditorGUI automatikusan beállítja a címet

        // 1. Replicate the calendar GUI exactly
        // Fill static items (fillers)
        for (Map.Entry<Integer, ItemStack> entry : calendar.getStaticItems().entrySet()) {
            if (entry.getKey() >= inventory.getSize()) continue; // Skip if slot is outside editor bounds
            inventory.setItem(entry.getKey(), entry.getValue().clone());
        }

        // Fill day items
        for (Map.Entry<Integer, CalendarDay> entry : calendar.getDaysBySlot().entrySet()) {
            if (entry.getKey() >= inventory.getSize()) continue;
            // In the editor, we *always* show the "locked" item as the placeholder
            ItemStack displayItem = entry.getValue().getLockedItem();
            ItemMeta meta = displayItem.getItemMeta();
            meta.setDisplayName(ChatUtil.format("&a&l[Day " + entry.getValue().getDay() + "]"));
            displayItem.setItemMeta(meta);
            inventory.setItem(entry.getKey(), displayItem);
        }

        // 2. Add the editor navigation bar (last row)
        int navBarStartSlot = (editorRows - 1) * 9;

        // Fill empty nav slots with a different filler
        ItemStack navFiller = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        for (int i = navBarStartSlot; i < inventory.getSize(); i++) {
            inventory.setItem(i, navFiller);
        }

        // Add info item
        inventory.setItem(navBarStartSlot + 3, new ItemBuilder(Material.BOOK)
                .name(lang.getMessage("editor.slot-editor.info-item.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-editor.info-item.lore"))
                .build());

        // Add back button
        inventory.setItem(navBarStartSlot + 5, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.slot-editor.back-to-cal-editor"))
                .build());

        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        int navBarStartSlot = (calendar.getRows()) * 9;

        // Check if clicking navigation bar
        if (slot >= navBarStartSlot) {
            if (slot == navBarStartSlot + 5) { // Back Button
                new CalendarEditGUI(plugin, player, calendar).open();
            }
            return;
        }

        // Clicked inside the actual GUI area
        if (event.getClick() == ClickType.RIGHT) {
            // Clear slot
            // TODO: Add logic to remove this slot from the guis/calendar.yml file
            player.sendMessage(ChatUtil.format("&cDEBUG: Right-clicked slot " + slot + " (Clear)"));
        } else {
            // Open the Slot Edit Menu
            new SlotEditMenu(plugin, player, calendar, slot).open();
        }
    }
}