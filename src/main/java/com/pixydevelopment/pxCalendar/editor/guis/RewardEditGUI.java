/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import com.pixydevelopment.pxCalendar.editor.RewardConfigSaver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for editing a single RewardBundle (Commands, Items).
 */
public class RewardEditGUI extends BaseEditorGUI {

    private final RewardFile rewardFile;
    private final RewardBundle bundle;
    private int commandPage = 0; // For pagination
    private int itemPage = 0; // For pagination

    public RewardEditGUI(PxCalendarPlugin plugin, Player player, RewardFile rewardFile, RewardBundle bundle) {
        super(plugin, player);
        this.rewardFile = rewardFile;
        this.bundle = bundle;
    }

    @Override
    public void open() {
        String title = lang.getMessage("editor.title-reward-editor").replace("%bundle%", bundle.getId());
        inventory = Bukkit.createInventory(this, 6 * 9, title);

        // --- Navigation ---
        inventory.setItem(48, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button"))
                .build());

        inventory.setItem(49, new ItemBuilder(Material.COMMAND_BLOCK)
                .name(lang.getMessage("editor.reward-editor.add-command.name"))
                .lore(lang.getLangConfig().getStringList("editor.reward-editor.add-command.lore"))
                .build());

        inventory.setItem(50, new ItemBuilder(Material.CHEST)
                .name(lang.getMessage("editor.reward-editor.add-item.name"))
                .lore(lang.getLangConfig().getStringList("editor.reward-editor.add-item.lore"))
                .build());

        // --- Content ---
        // Slots 0-17 (Commands) | Slots 18-35 (Items)

        // Headers
        inventory.setItem(0, new ItemBuilder(Material.PAPER).name(lang.getMessage("editor.reward-editor.current-commands")).build());
        inventory.setItem(18, new ItemBuilder(Material.DIAMOND).name(lang.getMessage("editor.reward-editor.current-items")).build());

        // Load Commands
        List<String> commands = bundle.getCommands();
        for (int i = 0; i < 17; i++) { // Max 17 command slots (1-17)
            int index = (commandPage * 17) + i;
            if (index >= commands.size()) break;

            inventory.setItem(i + 1, new ItemBuilder(Material.PAPER)
                    .name(lang.getMessage("editor.reward-editor.command-item.name")
                            .replace("%command%", commands.get(index)))
                    .lore(lang.getLangConfig().getStringList("editor.reward-editor.command-item.lore"))
                    .build());
        }

        // Load Items
        List<ItemStack> items = bundle.getItems();
        for (int i = 0; i < 17; i++) { // Max 17 item slots (19-35)
            int index = (itemPage * 17) + i;
            if (index >= items.size()) break;

            ItemStack item = items.get(index).clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(" ");
            // KIEGÉSZÍTVE: Biztonsági ellenőrzés, hogy a lore ne legyen null
            if (lang.getLangConfig().getStringList("editor.reward-editor.item-item.lore") != null) {
                lore.addAll(lang.getLangConfig().getStringList("editor.reward-editor.item-item.lore"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(i + 19, item);
        }

        fillEmptySlots();
        player.openInventory(getInventory());
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        ClickType click = event.getClick();
        RewardConfigSaver saver = new RewardConfigSaver(plugin, rewardFile, bundle);

        switch (slot) {
            case 48: // Back
                new RewardListGUI(plugin, player, rewardFile).open();
                return;
            case 49: // Add Command
                plugin.getEditorSessionManager().startRewardAddCommandSession(player, rewardFile, bundle);
                return;
            case 50: // KIEGÉSZÍTVE: Add Item
                ItemStack itemOnCursor = player.getItemOnCursor();
                if (itemOnCursor == null || itemOnCursor.getType() == Material.AIR) {
                    lang.sendMessage(player, "&cFogj meg egy tárgyat a kurzoroddal, és úgy kattints ide a hozzáadáshoz!");
                    return;
                }
                saver.addItem(itemOnCursor.clone()); // Elmentjük a tárgyat
                player.setItemOnCursor(null); // Töröljük a kurzorról
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                refreshGUI(); // Frissítjük a GUI-t
                return;
        }

        // Clicked on a command (Slots 1-17)
        if (slot >= 1 && slot <= 17 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PAPER) {
            if (click == ClickType.RIGHT) {
                int index = (commandPage * 17) + (slot - 1);
                saver.removeCommand(index);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
                refreshGUI(); // Frissítés
            }
            return;
        }

        // KIEGÉSZÍTVE: Clicked on an item (Slots 19-35)
        if (slot >= 19 && slot <= 35 && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.GRAY_STAINED_GLASS_PANE) {
            if (click == ClickType.RIGHT) {
                int index = (itemPage * 17) + (slot - 19);
                saver.removeItem(index); // Tárgy eltávolítása
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
                refreshGUI(); // Frissítés
            }
            return;
        }
    }

    /**
     * Helper metódus a GUI frissítésére a mentett adatokkal.
     */
    private void refreshGUI() {
        // Újra kell tölteni a RewardFile-t és a Bundle-t, mert a ConfigSaver frissítette őket
        RewardFile freshFile = plugin.getRewardManager().getRewardFile(rewardFile.getFileId());
        if (freshFile == null) {
            player.closeInventory();
            return;
        }
        RewardBundle freshBundle = freshFile.getBundle(bundle.getId());
        if (freshBundle == null) {
            player.closeInventory();
            return;
        }

        new RewardEditGUI(plugin, player, freshFile, freshBundle).open();
    }
}