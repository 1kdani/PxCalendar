/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single reward bundle (e.g., "day-1") loaded from a rewards file.
 * This class holds the parsed reward data.
 */
public class RewardBundle {

    private final String id;
    private final ConfigurationSection requirements;
    private final List<String> commands;
    private final List<ItemStack> items;
    private final List<String> particles;
    private final List<String> sounds;

    public RewardBundle(String id, ConfigurationSection config) {
        this.id = id;

        // Load requirements
        this.requirements = config.getConfigurationSection("requirements");

        // Load rewards section
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            this.commands = rewardsSection.getStringList("commands");
            this.sounds = rewardsSection.getStringList("sounds");
            this.particles = rewardsSection.getStringList("effects.particles"); // Legacy path check

            // Parse items
            this.items = rewardsSection.getStringList("items").stream()
                    .map(ItemBuilder::fromConfigString) // Use our ItemBuilder
                    .collect(Collectors.toList());
        } else {
            // Empty bundle
            this.commands = Collections.emptyList();
            this.items = Collections.emptyList();
            this.sounds = Collections.emptyList();
            this.particles = Collections.emptyList();
        }
    }

    // Getters for the RewardExecutor
    public String getId() { return id; }
    public ConfigurationSection getRequirements() { return requirements; }
    public List<String> getCommands() { return commands; }
    public List<ItemStack> getItems() { return items; }
    public List<String> getParticles() { return particles; }
    public List<String> getSounds() { return sounds; }
}