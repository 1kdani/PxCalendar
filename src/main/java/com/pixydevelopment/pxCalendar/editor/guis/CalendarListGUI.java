/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI for listing, creating, and deleting Calendars.
 */
public class CalendarListGUI extends BaseEditorGUI {

    public CalendarListGUI(PxCalendarPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        createInventory(6, "editor.title-list-calendars");

        List<Calendar> calendars = new ArrayList<>(plugin.getGuiManager().getLoadedCalendars());

        // TODO: Add pagination if calendars.size() > 45

        // Display all loaded calendars
        for (int i = 0; i < calendars.size(); i++) {
            if (i >= 45) break; // Max 45 items in this GUI
            Calendar calendar = calendars.get(i);

            ItemStack item = new ItemBuilder(Material.BOOKSHELF)
                    .name(lang.getMessage("editor.calendar-item.name").replace("%name%", calendar.getTitle()))
                    .lore(lang.getLangConfig().getStringList("editor.calendar-item.lore").stream()
                            .map(line -> line
                                    .replace("%file_name%", calendar.getId() + ".yml")
                                    .replace("%permission%", calendar.getPermission())
                                    .replace("%rows%", String.valueOf(calendar.getRows()))
                            )
                            .collect(Collectors.toList())
                    ).build();

            inventory.setItem(i, item);
        }

        // --- Bottom Navigation Bar ---

        // Back Button (Slot 48)
        inventory.setItem(48, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button"))
                .build());

        // Create New (Slot 49)
        inventory.setItem(49, new ItemBuilder(Material.LIME_DYE)
                .name(lang.getMessage("editor.calendar-list.create-new.name"))
                .lore(lang.getLangConfig().getStringList("editor.calendar-list.create-new.lore"))
                .build());

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        if (slot == 48) { // Back
            new MainEditorGUI(plugin, player).open();
            return;
        }

        if (slot == 49) { // Create New
            plugin.getEditorSessionManager().startCalendarCreateSession(player);
            return;
        }

        // Clicked on a calendar item
        if (clickedItem.getType() == Material.BOOKSHELF) {
            // Get the calendar ID from the lore (line 1: "File: example1.yml")
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasLore()) return;

            String fileNameLine = ChatUtil.stripColor(meta.getLore().get(0)); // "File: example1.yml"
            String calendarId = fileNameLine.replace("File: ", "").replace(".yml", "");

            Calendar calendar = plugin.getGuiManager().getCalendar(calendarId);
            if (calendar == null) return; // Should not happen

            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                // DELETE
                // TODO: Add a confirmation GUI
                lang.sendMessage(player, "&cDeleting... (TODO: Add confirmation)");
                File file = new File(plugin.getDataFolder(), "guis/" + calendar.getId() + ".yml");
                if (file.delete()) {
                    lang.sendMessage(player, "&aDeleted file. Reloading...");
                    plugin.getGuiManager().loadGUIs();
                    open(); // Refresh
                } else {
                    lang.sendMessage(player, "&cCould not delete file.");
                }
            } else {
                // EDIT
                new CalendarEditGUI(plugin, player, calendar).open();
            }
        }
    }
}