/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages loading and storing all RewardBundles from the /rewards/ folder.
 */
public class RewardManager {

    private final PxCalendarPlugin plugin;
    // Map<BundleID, RewardBundleObject>
    private final Map<String, RewardBundle> rewardBundles;

    public RewardManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.rewardBundles = new HashMap<>();
    }

    /**
     * Loads/reloads all .yml files from the /rewards/ folder.
     */
    public void loadRewards() {
        rewardBundles.clear();
        File rewardsFolder = new File(plugin.getDataFolder(), "rewards");

        if (!rewardsFolder.exists()) {
            rewardsFolder.mkdirs();
            // Save default examples
            plugin.saveResource("rewards/example1_rewards.yml", false);
            plugin.saveResource("rewards/example2_rewards.yml", false);
        }

        File[] rewardFiles = rewardsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (rewardFiles == null) {
            plugin.getLogger().severe("Could not read /rewards/ folder!");
            return;
        }

        int count = 0;
        for (File file : rewardFiles) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection bundlesSection = config.getConfigurationSection("reward-bundles");

                if (bundlesSection == null) {
                    plugin.getLogger().warning("File " + file.getName() + " does not contain a 'reward-bundles' section. Skipping.");
                    continue;
                }

                for (String bundleId : bundlesSection.getKeys(false)) {
                    ConfigurationSection bundleConfig = bundlesSection.getConfigurationSection(bundleId);
                    if (bundleConfig == null) continue;

                    RewardBundle bundle = new RewardBundle(bundleId, bundleConfig);

                    if (rewardBundles.containsKey(bundleId)) {
                        plugin.getLogger().warning("Duplicate reward bundle ID found: '" + bundleId + "'. Overwriting.");
                    }

                    rewardBundles.put(bundleId.toLowerCase(), bundle);
                    count++;
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load reward file: " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + count + " reward bundles.");
    }

    /**
     * Gets a loaded RewardBundle by its ID.
     * @param id The ID (e.g., "day-1")
     * @return The RewardBundle, or null if not found.
     */
    public RewardBundle getBundle(String id) {
        if (id == null) return null;
        return rewardBundles.get(id.toLowerCase());
    }
}