/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.sessions;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.editor.guis.RewardFileListGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Logic for handling chat input when creating a new reward file.
 */
public class RewardFileCreateSession {

    private final PxCalendarPlugin plugin;
    private final Player player;
    private final String input;
    private final LangManager lang;

    public RewardFileCreateSession(PxCalendarPlugin plugin, Player player, String input) {
        this.plugin = plugin;
        this.player = player;
        this.input = input.trim();
        this.lang = plugin.getLangManager();
    }

    public void process() {
        if (input.equalsIgnoreCase("cancel")) {
            lang.sendMessage(player, "editor.prompt-cancelled");
            new RewardFileListGUI(plugin, player).open(); // Re-open previous menu
            return;
        }

        // Validate
        if (!input.matches("^[a-zA-Z0-9_]+$")) {
            lang.sendMessage(player, "&cInvalid ID. Use only letters, numbers, and underscores.");
            return;
        }

        String fileId = input.toLowerCase();
        if (!fileId.endsWith("_rewards")) {
            fileId += "_rewards"; // Enforce naming convention
        }

        File rewardFile = new File(plugin.getDataFolder(), "rewards/" + fileId + ".yml");
        if (rewardFile.exists()) {
            lang.sendMessage(player, "editor.prompt-reward-file-exists", "%id%", fileId);
            return;
        }

        // Create new file
        try {
            rewardFile.createNewFile();
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(rewardFile);
            newConfig.set("reward-bundles.default-bundle.rewards.commands", List.of("msg %player_name% &aDefault Reward!"));
            newConfig.save(rewardFile);
        } catch (IOException e) {
            e.printStackTrace();
            lang.sendMessage(player, "messages.error-generic");
            return;
        }

        lang.sendMessage(player, "editor.prompt-reward-file-created", "%id%", fileId);

        // Reload and re-open the list
        plugin.getRewardManager().loadRewards();
        new RewardFileListGUI(plugin, player).open();
    }
}