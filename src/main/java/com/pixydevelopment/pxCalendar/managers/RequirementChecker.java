/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Handles checking reward requirements (Vault, Permission, etc.)
 */
public class RequirementChecker {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;
    private Economy vaultEconomy = null;

    public RequirementChecker(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        setupVault();
    }

    private void setupVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().info("Vault not found. Money requirements will be disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Vault found, but no Economy provider (e.g., Essentials) is registered.");
            return;
        }
        vaultEconomy = rsp.getProvider();
        plugin.getLogger().info("Successfully hooked into Vault for economy requirements.");
    }

    /**
     * Checks if a player meets all requirements for a specific reward.
     * @param player The player
     * @param requirementsSection The "requirements" section from rewards.yml
     * @return true if they pass, false otherwise.
     */
    public boolean checkRequirements(Player player, ConfigurationSection requirementsSection) {
        if (requirementsSection == null) {
            return true; // No requirements
        }

        // Check Permission
        if (requirementsSection.contains("permission")) {
            String perm = requirementsSection.getString("permission");
            if (!player.hasPermission(perm)) {
                lang.sendMessage(player, "calendar.requirement-fail-permission");
                return false;
            }
        }

        // Check Vault Money
        if (requirementsSection.contains("money")) {
            if (vaultEconomy == null) {
                plugin.getLogger().warning("A reward requires 'money' but Vault is not hooked. Requirement skipped.");
                return true; // Don't block if Vault isn't loaded
            }
            double requiredMoney = requirementsSection.getDouble("money");
            if (vaultEconomy.getBalance(player) < requiredMoney) {
                lang.sendMessage(player, "calendar.requirement-fail-money",
                        "%amount%", String.valueOf(requiredMoney));
                return false;
            }
        }

        // TODO: Add other checks like "level", "items", "placeholderapi"

        return true;
    }
}