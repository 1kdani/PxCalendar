/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor; // JAVÍTÁS: Hozzáadva a hiányzó import
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI for listing all reward bundles (day-1, day-2) inside a single reward file.
 */
public class RewardListGUI extends BaseEditorGUI {

    private final RewardFile rewardFile;

    public RewardListGUI(PxCalendarPlugin plugin, Player player, RewardFile rewardFile) {
        super(plugin, player);
        this.rewardFile = rewardFile;
    }

    @Override
    public void open() {
        String title = lang.getMessage("editor.title-list-rewards").replace("%file%", rewardFile.getFileId());
        inventory = Bukkit.createInventory(this, 6 * 9, title); // 6 rows

        List<RewardBundle> bundles = new ArrayList<>(rewardFile.getRewardBundles().values());

        // TODO: Add pagination
        for (int i = 0; i < bundles.size(); i++) {
            if (i >= 45) break;
            RewardBundle bundle = bundles.get(i);

            // JAVÍTÁS: Material.GIFT_LOOT_TABLE (1.20.5+) cserélve Material.CHEST-re (1.16.5)
            ItemStack item = new ItemBuilder(Material.CHEST)
                    .name(lang.getMessage("editor.reward-list.reward-bundle-item.name")
                            .replace("%bundle_id%", bundle.getId()))
                    .lore(lang.getLangConfig().getStringList("editor.reward-list.reward-bundle-item.lore").stream()
                            .map(line -> line
                                    .replace("%commands%", String.valueOf(bundle.getCommands().size()))
                                    .replace("%items%", String.valueOf(bundle.getItems().size()))
                            )
                            .collect(Collectors.toList()))
                    .build();
            inventory.setItem(i, item);
        }

        // --- Bottom Navigation Bar ---
        inventory.setItem(48, new ItemBuilder(Material.BARRIER)
                .name(lang.getMessage("editor.back-button")) // Goes back to File List
                .build());
        inventory.setItem(49, new ItemBuilder(Material.LIME_DYE)
                .name(lang.getMessage("editor.reward-list.create-new.name"))
                .lore(lang.getLangConfig().getStringList("editor.reward-list.create-new.lore"))
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
            new RewardFileListGUI(plugin, player).open();
            return;
        }

        if (slot == 49) { // Create New Bundle
            // TODO: Add chat session for new bundle ID
            lang.sendMessage(player, "&cCreating new bundles is coming soon!");
            return;
        }

        // JAVÍTÁS: Material.GIFT_LOOT_TABLE cserélve Material.CHEST-re
        if (clickedItem.getType() == Material.CHEST) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            // JAVÍTÁS: A 'ChatUtil.stripColor' (ami nem létezik)
            // cserélve a Bungee 'ChatColor.stripColor'-ra.
            String bundleId = ChatColor.stripColor(meta.getDisplayName()); // "day-1"
            RewardBundle bundle = rewardFile.getBundle(bundleId);

            if (bundle == null) {
                lang.sendMessage(player, "&cError: Could not find loaded bundle: " + bundleId);
                return;
            }

            // JAVÍTVA: Megnyitja a RewardEditGUI-t
            new RewardEditGUI(plugin, player, rewardFile, bundle).open();
        }
    }
}