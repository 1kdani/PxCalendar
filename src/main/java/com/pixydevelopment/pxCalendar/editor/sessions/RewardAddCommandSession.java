/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.sessions;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.calendar.RewardFile;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.editor.RewardConfigSaver;
import com.pixydevelopment.pxCalendar.editor.guis.RewardEditGUI;
import org.bukkit.entity.Player;

/**
 * Logic for handling chat input when adding a new command to a reward.
 */
public class RewardAddCommandSession {

    private final PxCalendarPlugin plugin;
    private final Player player;
    private final String input;
    private final LangManager lang;
    private final RewardFile rewardFile;
    private final RewardBundle bundle;

    public RewardAddCommandSession(PxCalendarPlugin plugin, Player player, String input, RewardFile rewardFile, RewardBundle bundle) {
        this.plugin = plugin;
        this.player = player;
        this.input = input.trim(); // Do not strip /
        this.lang = plugin.getLangManager();
        this.rewardFile = rewardFile;
        this.bundle = bundle;
    }

    public void process() {
        if (input.equalsIgnoreCase("cancel")) {
            lang.sendMessage(player, "editor.prompt-cancelled");
            reOpenGui();
            return;
        }

        String command = input;
        if (command.startsWith("/")) { // Remove / if they added it
            command = command.substring(1);
        }

        // Save the new command
        RewardConfigSaver saver = new RewardConfigSaver(plugin, rewardFile, bundle);
        saver.addCommand(command);

        lang.sendMessage(player, "editor.prompt-command-added");
        reOpenGui();
    }

    /**
     * Re-opens the RewardEditGUI with the fresh, reloaded data.
     */
    private void reOpenGui() {
        RewardFile freshFile = plugin.getRewardManager().getRewardFile(rewardFile.getFileId());
        RewardBundle freshBundle = freshFile.getBundle(bundle.getId());
        new RewardEditGUI(plugin, player, freshFile, freshBundle).open();
    }
}