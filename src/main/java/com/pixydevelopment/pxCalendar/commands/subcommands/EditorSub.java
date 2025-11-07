/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.editor.EditorManager; // ÚJ IMPORT
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player; // ÚJ IMPORT

public class EditorSub implements CommandBase {

    private final LangManager lang;
    private final EditorManager editorManager; // ÚJ

    public EditorSub(PxCalendarPlugin plugin) {
        this.lang = plugin.getLangManager();
        this.editorManager = plugin.getEditorManager(); // ÚJ (ezt hozzá kell adni a PxCalendarPlugin.java-hoz)
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "messages.player-only");
            return;
        }

        // Megnyitja a főmenüt
        editorManager.openMainMenu((Player) sender);
    }

    @Override
    public String getPermission() {
        return "pxcalendar.editor";
    }
}