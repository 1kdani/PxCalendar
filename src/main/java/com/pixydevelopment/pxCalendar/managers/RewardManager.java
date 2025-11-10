/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile; // NEW
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection; // NEW
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages loading and storing all RewardFiles from the /rewards/ folder.
 */
public class RewardManager {

    private final PxCalendarPlugin plugin;
    // Map<FileID, RewardFileObject>
    private final Map<String, RewardFile> rewardFiles;

    public RewardManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.rewardFiles = new HashMap<>();
    }

    /**
     * Loads/reloads all .yml files from the /rewards/ folder.
     */
    public void loadRewards() {
        rewardFiles.clear();
        File rewardsFolder = new File(plugin.getDataFolder(), "rewards");

        if (!rewardsFolder.exists()) {
            rewardsFolder.mkdirs();
            // Save default examples
            plugin.saveResource("rewards/example1_rewards.yml", false);
            plugin.saveResource("rewards/example2_rewards.yml", false);
        }

        File[] files = rewardsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            plugin.getLogger().severe("Could not read /rewards/ folder!");
            return;
        }

        for (File file : files) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                RewardFile rewardFile = new RewardFile(file, config);

                rewardFiles.put(rewardFile.getFileId().toLowerCase(), rewardFile);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load reward file: " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + rewardFiles.size() + " reward files.");
    }

    /**
     * Gets a loaded RewardFile by its ID.
     * @param fileId The ID (e.g., "example1_rewards")
     * @return The RewardFile, or null if not found.
     */
    public RewardFile getRewardFile(String fileId) {
        return rewardFiles.get(fileId.toLowerCase());
    }

    /**
     * Gets all loaded reward files.
     */
    public Collection<RewardFile> getLoadedRewardFiles() {
        return rewardFiles.values();
    }

    /**
     * Gets a specific RewardBundle by searching all loaded files.
     * @param bundleId The ID (e.g., "day-1")
     * @return The RewardBundle, or null if not found.
     */
    public RewardBundle getBundle(String bundleId) {
        if (bundleId == null) return null;

        for (RewardFile file : rewardFiles.values()) {
            RewardBundle bundle = file.getBundle(bundleId);
            if (bundle != null) {
                return bundle;
            }
        }
        return null; // Not found in any file
    }
}