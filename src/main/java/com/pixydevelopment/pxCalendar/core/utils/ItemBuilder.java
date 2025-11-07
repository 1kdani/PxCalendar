/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.core.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PlayerProfile;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class to easily create ItemStacks from configs or code.
 * Supports legacy and modern (1.18+) Player Head methods.
 */
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ChatUtil.format(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore.stream()
                .map(ChatUtil::format)
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Sets the texture for a PLAYER_HEAD item.
     * @param texture The Base64 texture string.
     */
    public ItemBuilder texture(String texture) {
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;
            try {
                // Modern 1.18+ Method (PlayerProfile)
                PlayerProfile profile = org.bukkit.Bukkit.createPlayerProfile(UUID.randomUUID());
                profile.getTextures().setSkin(new java.net.URL("http://textures.minecraft.net/texture/" + texture));
                skullMeta.setOwnerProfile(profile);
            } catch (Exception e) {
                // Fallback to legacy 1.16 method (GameProfile reflection)
                try {
                    GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                    profile.getProperties().put("textures", new Property("textures", texture));
                    Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                    profileField.set(skullMeta, profile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates an ItemStack from a simple config string.
     * Format: MATERIAL_NAME:AMOUNT or PLAYER_HEAD:TEXTURE_STRING
     * @param configString The string from the config
     * @return An ItemStack, or null if invalid.
     */
    public static ItemStack fromConfigString(String configString) {
        if (configString == null || configString.isEmpty()) {
            return null;
        }

        String[] parts = configString.split(":");
        if (parts.length == 0) {
            return null;
        }

        try {
            Material material = Material.matchMaterial(parts[0].toUpperCase());
            if (material == null) {
                material = Material.STONE; // Fallback
            }

            ItemBuilder builder = new ItemBuilder(material);

            if (material == Material.PLAYER_HEAD && parts.length > 1) {
                // PLAYER_HEAD:TEXTURE
                builder.texture(parts[1]);
            } else if (parts.length > 1) {
                // MATERIAL:AMOUNT
                builder.amount(Integer.parseInt(parts[1]));
            }

            return builder.build();

        } catch (Exception e) {
            System.err.println("[PxCalendar] Failed to parse item string: " + configString);
            e.printStackTrace();
            return new ItemStack(Material.BARRIER);
        }
    }
}