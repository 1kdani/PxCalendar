/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.core.managers.LangManager; // ÚJ IMPORT
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * Executes a RewardBundle for a player (runs commands, gives items, plays effects).
 */
public class RewardExecutor {

    private final PxCalendarPlugin plugin;
    private final boolean particlesEnabled;
    private final LangManager lang; // ÚJ MEZŐ

    public RewardExecutor(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager(); // ÚJ PÉLDÁNYOSÍTÁS
        this.particlesEnabled = plugin.getConfigManager().getConfig().getBoolean("effects.enable-particles", true);
    }

    /**
     * Gives a reward bundle to a player.
     * @param player The player receiving the reward.
     * @param bundle The RewardBundle to execute.
     */
    public void execute(Player player, RewardBundle bundle) {
        if (bundle == null) {
            plugin.getLogger().warning("Attempted to execute a null reward bundle for " + player.getName());
            return;
        }

        // 1. Run Commands
        for (String command : bundle.getCommands()) {
            String parsedCommand = command.replace("%player_name%", player.getName())
                    .replace("%player_uuid%", player.getUniqueId().toString());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }

        // 2. Give Items
        for (ItemStack item : bundle.getItems()) {
            if (player.getInventory().firstEmpty() == -1) {
                // Inventory is full, drop it at their feet
                player.getWorld().dropItem(player.getLocation(), item);
                lang.sendMessage(player, "messages.inventory-full"); // Ez most már működni fog
            } else {
                player.getInventory().addItem(item);
            }
        }

        // 3. Play Sounds
        // ... (a kódod többi része itt változatlan) ...
        for (String soundString : bundle.getSounds()) {
            try {
                String[] parts = soundString.split(":");
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float volume = (parts.length > 1) ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = (parts.length > 2) ? Float.parseFloat(parts[2]) : 1.0f;
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid sound format in reward bundle '" + bundle.getId() + "': " + soundString);
            }
        }

        // 4. Spawn Particles
        if (particlesEnabled) {
            for (String particleString : bundle.getParticles()) {
                try {
                    String[] parts = particleString.split(":");
                    Particle particle = Particle.valueOf(parts[0].toUpperCase());
                    int count = (parts.length > 1) ? Integer.parseInt(parts[1]) : 50;
                    double speed = (parts.length > 2) ? Double.parseDouble(parts[2]) : 0.1;
                    player.spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.5, 0.5, 0.5, speed);
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid particle format in reward bundle '" + bundle.getId() + "': " + particleString);
                }
            }
        }
    }
}