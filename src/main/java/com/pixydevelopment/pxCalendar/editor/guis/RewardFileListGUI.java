/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor; // JAVÍTÁS: Hozzáadva a hiányzó import
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
 * GUI for listing all reward files (e.g., example1_rewards.yml)
 */
public class RewardFileListGUI extends BaseEditorGUI {

    public RewardFileListGUI(PxCalendarPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public void open() {
        createInventory(6, "editor.title-list-reward-files");

        List<RewardFile> rewardFiles = new ArrayList<>(plugin.getRewardManager().getLoadedRewardFiles());

        // TODO: Add pagination
        for (int i = 0; i < rewardFiles.size(); i++) {
            if (i >= 45) break;
            RewardFile rewardFile = rewardFiles.get(i);

            ItemStack item = new ItemBuilder(Material.CHEST_MINECART)
                    .name(lang.getMessage("editor.reward-file-list.reward-file-item.name")
                            .replace("%file_name%", rewardFile.getFileId() + ".yml"))
                    .lore(lang.getLangConfig().getStringList("editor.reward-file-list.reward-file-item.lore").stream()
                            .map(line -> line.replace("%count%", String.valueOf(rewardFile.getRewardBundles().size())))
                            .collect(Collectors.toList()))
                    .build();
            inventory.setItem(i, item);
        }

        // --- Bottom Navigation Bar ---
        inventory.setItem(48, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button"))
                .build());
        inventory.setItem(49, new ItemBuilder(Material.LIME_DYE)
                .name(lang.getMessage("editor.reward-file-list.create-new.name"))
                .lore(lang.getLangConfig().getStringList("editor.reward-file-list.create-new.lore"))
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

        if (slot == 49) { // Create New Reward File
            plugin.getEditorSessionManager().startRewardFileCreateSession(player);
            return;
        }

        if (clickedItem.getType() == Material.CHEST_MINECART) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            // JAVÍTÁS: A 'ChatUtil.stripColor' (ami nem létezik)
            // cserélve a Bungee 'ChatColor.stripColor'-ra.
            String fileName = ChatColor.stripColor(meta.getDisplayName()); // "example1_rewards.yml"
            String fileId = fileName.replace(".yml", "");

            RewardFile rewardFile = plugin.getRewardManager().getRewardFile(fileId);
            if (rewardFile == null) {
                lang.sendMessage(player, "&cError: Could not find loaded reward file: " + fileId);
                return;
            }

            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                // DELETE
                // TODO: Add confirmation GUI
                lang.sendMessage(player, "&cDeleting... (TODO: Add confirmation)");
                File file = rewardFile.getFile();
                if (file.delete()) {
                    lang.sendMessage(player, "&aDeleted file. Reloading...");
                    plugin.getRewardManager().loadRewards();
                    open(); // Refresh
                } else {
                    lang.sendMessage(player, "&cCould not delete file.");
                }
            } else {
                // EDIT (Open list of bundles)
                new RewardListGUI(plugin, player, rewardFile).open();
            }
        }
    }
}