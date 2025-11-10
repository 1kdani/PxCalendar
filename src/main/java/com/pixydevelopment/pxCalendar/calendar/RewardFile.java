/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single loaded rewards.yml file (e.g., "example1_rewards.yml")
 * and all the RewardBundles contained within it.
 */
public class RewardFile {

    private final File file;
    private final FileConfiguration config;
    private final String fileId; // e.g., "example1_rewards"
    private final Map<String, RewardBundle> rewardBundles; // Map<BundleID, Bundle>

    public RewardFile(File file, FileConfiguration config) {
        this.file = file;
        this.config = config;
        this.fileId = file.getName().replace(".yml", "");
        this.rewardBundles = new HashMap<>();

        loadBundles();
    }

    private void loadBundles() {
        ConfigurationSection bundlesSection = config.getConfigurationSection("reward-bundles");
        if (bundlesSection == null) {
            return;
        }

        for (String bundleId : bundlesSection.getKeys(false)) {
            ConfigurationSection bundleConfig = bundlesSection.getConfigurationSection(bundleId);
            if (bundleConfig == null) continue;

            RewardBundle bundle = new RewardBundle(bundleId, bundleConfig);
            rewardBundles.put(bundleId.toLowerCase(), bundle);
        }
    }

    public String getFileId() {
        return fileId;
    }

    public Map<String, RewardBundle> getRewardBundles() {
        return rewardBundles;
    }

    public RewardBundle getBundle(String bundleId) {
        return rewardBundles.get(bundleId.toLowerCase());
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}