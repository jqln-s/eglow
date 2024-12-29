package me.jqln.eGlow.commands;

import me.jqln.eGlow.EGlow;
import me.jqln.eGlow.util.GlowManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GlowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("glow")) {
            if (commandSender instanceof Player p) {
                // Disable glow or redirect player if no color provided
                if (strings.length == 0) {
                    if (p.isGlowing()) {
                        GlowManager.removeGlowColor(p);
                        p.sendMessage(ChatColor.GREEN + "Removed your glow color.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Usage: /glow <color>");
                    }

                    return true;
                }

                ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");

                // Reload the config file
                if (strings[0].equalsIgnoreCase("reload")) {
                    if (p.hasPermission("eglow.reload")) {
                        EGlow.getInstance().reloadConfig();

                        p.sendMessage(ChatColor.GREEN + "eGlow configuration file reloaded successfully.");

                        return true;
                    }
                }

                for (String effect : effects.getKeys(false)) {
                    if (strings[0].equalsIgnoreCase(effect)) {
                        if (p.hasPermission(effects.getConfigurationSection(effect).getString("permission"))) {
                            // Find team
                            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                            Team team = scoreboard.getTeam("glow_" + effect.toUpperCase());

                            // Add player to team and enable their glow
                            team.addEntry(p.getName());
                            p.setGlowing(true);

                            // Send appropriately colored confirmation message
                            String effectName = GlowManager.getColorName(effect);
                            String[] colors = effects.getConfigurationSection(effect).getStringList("colors").toArray(new String[0]);
                            String message = ChatColor.GREEN + "Changed your glow effect to " + ChatColor.BOLD;
                            for (int i = 0; i < effectName.length(); i++) {
                                ChatColor color = ChatColor.valueOf(colors[i % colors.length]);
                                message += color + effectName.substring(i, i + 1);
                            }
                            p.sendMessage(message);

                            return true;
                        }
                    }
                }

                // Parse color
                String color = strings[0].toUpperCase();
                ChatColor chatColor;
                try {
                    chatColor = ChatColor.valueOf(color);
                } catch (IllegalArgumentException e) {
                    p.sendMessage(ChatColor.RED + "Invalid color selection.");
                    return true;
                }

                // Check for permissions
                String permission = "eglow.color." + chatColor.name().toLowerCase().replace("_", "");
                if (p.hasPermission(permission)) {
                    // Remove old color and add new color
                    if (p.isGlowing()) {
                        GlowManager.removeGlowColor(p);
                    }
                    GlowManager.addGlowColor(p, chatColor);

                    // Format color name and send confirmation message
                    String colorName = GlowManager.getColorName(chatColor.name());
                    p.sendMessage(ChatColor.GREEN + "Changed your glow color to " + ChatColor.BOLD + chatColor + colorName);
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }
        }
        return true;
    }
}