/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.editor.ConfigSaver; // ÚJ IMPORT
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound; // ÚJ IMPORT
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

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
        // JAVÍTÁS: A BaseEditorGUI createInventory metódusa lang path-t vár
        createInventory(3, "editor.title-slot-edit-menu-placeholder"); // Ideiglenes

        // JAVÍTÁS: Manuálisan állítjuk a címet, hogy a placeholder működjön
        inventory = Bukkit.createInventory(this, 3 * 9, title);

        // ... (a többi item beállítása változatlan) ...
        inventory.setItem(10, new ItemBuilder(Material.GREEN_WOOL)
                .name(lang.getMessage("editor.slot-edit-menu.set-day.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-day.lore"))
                .build());
        inventory.setItem(12, new ItemBuilder(Material.GRAY_DYE)
                .name(lang.getMessage("editor.slot-edit-menu.set-filler.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-filler.lore"))
                .build());
        inventory.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
                .name(lang.getMessage("editor.slot-edit-menu.set-static.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.set-static.lore"))
                .build());
        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.slot-edit-menu.clear-slot.name"))
                .lore(lang.getLangConfig().getStringList("editor.slot-edit-menu.clear-slot.lore"))
                .build());
        inventory.setItem(22, new ItemBuilder(Material.ARROW)
                .name(lang.getMessage("editor.slot-editor.back-to-cal-editor"))
                .build());

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ConfigSaver saver = new ConfigSaver(plugin, calendar); // ÚJ

        switch (slot) {
            case 10: // Set as Day
                // JAVÍTVA: Megnyitja a DaySelectorGUI-t
                new DaySelectorGUI(plugin, player, calendar, editingSlot).open();
                return; // Nem kell refreshelni ezt a menüt

            case 12: // Set as Filler
                saver.setSlotAsFiller(editingSlot);
                break;

            case 14: // Set as Static
                // TODO: This needs a chat session to ask for the item ID (e.g., 'exit-button')
                lang.sendMessage(player, "&cSetting static items is coming soon!");
                return; // Ne zárja be a menüt

            case 16: // Clear Slot
                saver.clearSlot(editingSlot);
                break;

            case 22: // Back
                new CalendarSlotEditorGUI(plugin, player, calendar).open();
                return;

            default:
                return;
        }

        // Visszalépés a fő slot szerkesztőbe a változtatás után
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
        new CalendarSlotEditorGUI(plugin, player, plugin.getGuiManager().getCalendar(calendar.getId())).open();
    }
}