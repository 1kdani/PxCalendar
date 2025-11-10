/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

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

    // TODO: Add methods for add/remove items, sounds, particles

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