/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

/**
 * Hub menu for editing a single calendar (layout, rewards, delete).
 */
public class CalendarEditGUI extends BaseEditorGUI {

    private final Calendar calendar;

    public CalendarEditGUI(PxCalendarPlugin plugin, Player player, Calendar calendar) {
        super(plugin, player);
        this.calendar = calendar;
    }

    @Override
    public void open() {
        String title = lang.getMessage("editor.title-edit-calendar").replace("%name%", calendar.getId());

        // JAVÍTÁS: A BaseEditorGUI createInventory metódusa lang path-t vár
        // De nekünk dinamikus cím kell, ezért felülírjuk
        inventory = Bukkit.createInventory(this, 3 * 9, title);

        // ... (inventory.setItem hívások változatlanok) ...
        // Edit Layout (Slot 11)
        inventory.setItem(11, new ItemBuilder(Material.CRAFTING_TABLE)
                .name(lang.getMessage("editor.calendar-edit.edit-slots.name"))
                .lore(lang.getLangConfig().getStringList("editor.calendar-edit.edit-slots.lore"))
                .build());
        // ... (többi item) ...
        inventory.setItem(13, new ItemBuilder(Material.DIAMOND)
                .name(lang.getMessage("editor.calendar-edit.edit-rewards.name"))
                .lore(lang.getLangConfig().getStringList("editor.calendar-edit.edit-rewards.lore"))
                .build());
        inventory.setItem(15, new ItemBuilder(Material.TNT)
                .name(lang.getMessage("editor.calendar-edit.delete.name"))
                .lore(lang.getLangConfig().getStringList("editor.calendar-edit.delete.lore").stream()
                        .map(l -> l.replace("%file_name%", calendar.getId() + ".yml"))
                        .collect(Collectors.toList()))
                .build());
        inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button"))
                .build());

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        switch (slot) {
            case 11: // Edit Layout
                // JAVÍTVA: Megnyitja a vizuális szerkesztőt
                new CalendarSlotEditorGUI(plugin, player, calendar).open();
                break;
            case 13: // Edit Rewards
                // JAVÍTVA: Megnyitja a megfelelő jutalom listát
                String rewardFileId = calendar.getId() + "_rewards";
                RewardFile rewardFile = plugin.getRewardManager().getRewardFile(rewardFileId);

                if (rewardFile == null) {
                    lang.sendMessage(player, "&cCould not find matching reward file: " + rewardFileId + ".yml");
                    player.closeInventory();
                } else {
                    new RewardListGUI(plugin, player, rewardFile).open();
                }
                break;
            case 15: // Delete
                // TODO: Add a confirmation GUI
                player.sendMessage(ChatUtil.format("&c(Confirmation GUI needed)"));
                break;
            case 22: // Back
                new CalendarListGUI(plugin, player).open();
                break;
        }
    }
}