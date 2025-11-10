/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.util.List;

/**
 * Utility class to handle saving changes made in the editor to reward .yml files.
 */
public class RewardConfigSaver {

    private final PxCalendarPlugin plugin;
    private final RewardFile rewardFile;
    private final RewardBundle bundle;
    private final FileConfiguration config;
    private final String basePath;

    public RewardConfigSaver(PxCalendarPlugin plugin, RewardFile rewardFile, RewardBundle bundle) {
        this.plugin = plugin;
        this.rewardFile = rewardFile;
        this.bundle = bundle;
        this.config = rewardFile.getConfig();
        this.basePath = "reward-bundles." + bundle.getId() + ".rewards.";
    }

    /**
     * Adds a command string to the rewards list.
     * @param command The command to add (without /)
     */
    public void addCommand(String command) {
        List<String> commands = config.getStringList(basePath + "commands");
        commands.add(command);
        config.set(basePath + "commands", commands);
        save();
    }

    /**
     * Removes a command from the list by its index.
     * @param index The index to remove
     */
    public void removeCommand(int index) {
        List<String> commands = config.getStringList(basePath + "commands");
        if (index >= 0 && index < commands.size()) {
            commands.remove(index);
            config.set(basePath + "commands", commands);
            save();
        }
    }

    /**
     * KIEGÉSZÍTVE: Tárgy hozzáadása a jutalmakhoz.
     * @param item A hozzáadandó ItemStack
     */
    public void addItem(ItemStack item) {
        List<String> items = config.getStringList(basePath + "items");
        String configString = toConfigString(item); // Átalakítás szöveggé
        if (configString != null) {
            items.add(configString);
            config.set(basePath + "items", items);
            save();
        }
    }

    /**
     * KIEGÉSZÍTVE: Tárgy eltávolítása index alapján.
     * @param index Az eltávolítandó tárgy indexe
     */
    public void removeItem(int index) {
        List<String> items = config.getStringList(basePath + "items");
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            config.set(basePath + "items", items);
            save();
        }
    }

    /**
     * KIEGÉSZÍTVE: Helper metódus, ami az ItemBuilder által támogatott formátumra alakít egy ItemStack-et.
     * FIGYELEM: Ez a metódus jelenleg csak az Anyagot és a Mennyiséget menti.
     * A PLAYER_HEAD és bonyolultabb NBT adatok mentése további logikát igényel.
     */
    private String toConfigString(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        // TODO: A PLAYER_HEAD textúrák kinyerésének implementálása (bonyolult)
        // Jelenleg csak az alap MAT:AMOUNT formátumot támogatjuk, ahogy az ItemBuilder is.
        // A 'NATIVE:' prefixet automatikusan feltételezzük, ahogy az example fájlokban van.
        // pl. "NATIVE:DIAMOND:5"
        if (item.getType() == Material.PLAYER_HEAD && item.getItemMeta() instanceof SkullMeta) {
            // A textúra visszafejtése bonyolult, egyelőre kihagyva.
            // A felhasználónak manuálisan kell hozzáadnia a textúrát.
            plugin.getLogger().warning("Player heads cannot be saved via the GUI editor yet. Please add them manually.");
            return null;
        }

        // Alapértelmezett formátum
        return "NATIVE:" + item.getType().name() + ":" + item.getAmount();
    }

    /**
     * Saves the changes back to the .yml file.
     */
    private void save() {
        try {
            config.save(rewardFile.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to save " + rewardFile.getFile().getName());
        }

        // Reload rewards to reflect changes
        plugin.getRewardManager().loadRewards();
    }
}