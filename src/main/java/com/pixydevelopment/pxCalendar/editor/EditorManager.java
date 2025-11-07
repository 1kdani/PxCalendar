/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.editor.guis.MainEditorGUI;
import org.bukkit.entity.Player;

/**
 * Manages the entry point into the Admin GUI editor.
 */
public class EditorManager {

    private final PxCalendarPlugin plugin;

    public EditorManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main editor menu for a player.
     * @param player The admin player
     */
    public void openMainMenu(Player player) {
        new MainEditorGUI(plugin, player).open();
    }
}