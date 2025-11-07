/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.guis;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all Admin Editor GUIs.
 * Implements InventoryHolder to be easily trackable in listeners.
 */
public abstract class BaseEditorGUI implements InventoryHolder {

    protected final PxCalendarPlugin plugin;
    protected final LangManager lang;
    protected final Player player;
    protected Inventory inventory;

    public BaseEditorGUI(PxCalendarPlugin plugin, Player player) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.player = player;
    }

    /**
     * Called when the GUI needs to be opened.
     */
    public abstract void open();

    /**
     * Called by the EditorClickListener when this GUI is clicked.
     * @param event The InventoryClickEvent
     */
    public abstract void handleClick(InventoryClickEvent event);

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Creates the inventory with a standard size and title.
     * @param rows Number of rows (1-6)
     * @param langTitlePath Path in lang.yml for the title
     */
    protected void createInventory(int rows, String langTitlePath) {
        this.inventory = Bukkit.createInventory(this, rows * 9, lang.getMessage(langTitlePath));
    }

    /**
     * Fills all empty slots with a standard filler item.
     */
    protected void fillEmptySlots() {
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
}