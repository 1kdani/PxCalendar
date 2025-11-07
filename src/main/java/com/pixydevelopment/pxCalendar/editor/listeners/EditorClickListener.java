/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.listeners;

import com.pixydevelopment.pxCalendar.editor.guis.BaseEditorGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Listener that handles clicks ONLY within Admin Editor GUIs
 * (GUIs that implement BaseEditorGUI).
 */
public class EditorClickListener implements Listener {

    @EventHandler
    public void onEditorClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }

        InventoryHolder holder = event.getClickedInventory().getHolder();

        // Check if the clicked inventory is one of our Editor GUIs
        if (holder instanceof BaseEditorGUI) {
            event.setCancelled(true); // Cancel all clicks in editor GUIs

            BaseEditorGUI gui = (BaseEditorGUI) holder;
            gui.handleClick(event); // Delegate the click logic to the GUI class
        }
    }

    // TODO: Add session management (e.g., if a player is editing a reward
    // and closes the GUI, we need to cancel the edit session)
    @EventHandler
    public void onEditorClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof BaseEditorGUI) {
            // Handle closing editor menus if needed
        }
    }
}