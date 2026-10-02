package me.jqln.eGlow.util;

import me.jqln.eGlow.EGlow;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Check if inventory is GUI
        if (player.hasMetadata("OpenedMenu")) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getItemMeta().getDisplayName().equals(" ") || event.getClickedInventory().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
                return;
            }

            String itemName = item.getItemMeta().getDisplayName();
            String chatColor = itemName.toUpperCase().replace(" ", "_");

            // Handle reset option
            if (chatColor.equals("RESET_COLOR")) {
                GlowManager.removeGlowColor(player);
                player.sendMessage(ChatColor.GREEN + "Reset your glow color!");
                event.setCancelled(true);
                player.closeInventory();
                return;
            }

            // Add clicked color
            GlowManager.addGlowColor(player, chatColor);

            // Cancel click event and close GUI
            event.setCancelled(true);
            player.closeInventory();
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (player.hasMetadata("OpenedMenu")) {
            player.removeMetadata("OpenedMenu", EGlow.getInstance());
        }
    }
}
