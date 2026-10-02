package me.jqln.eGlow.util;

import me.jqln.eGlow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GlowManager {
    private static final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

    public static String getColorName(String color) {
        // Replace all underscores with spaces and capitalize all words
        String colorName = color.toLowerCase().replace("_", " ");
        return Arrays.stream(colorName.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static void addGlowColor(Player p, String color) {
        ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");
        for (String effect : effects.getKeys(false)) {
            if (color.equalsIgnoreCase(effect)) {
                if (p.hasPermission(effects.getConfigurationSection(effect).getString("permission"))) {
                    // Remove old color and add new color
                    if (p.isGlowing()) {
                        removeGlowColor(p);
                    }

                    // Find team
                    Team team = scoreboard.getTeam("glow_" + effect.toUpperCase());

                    // Add player to team and enable their glow
                    team.addEntry(p.getName());
                    p.setGlowing(true);

                    // Send appropriately colored confirmation message
                    String effectName = GlowManager.getColorName(effect);
                    String[] colors = effects.getConfigurationSection(effect).getStringList("colors").toArray(new String[0]);
                    String message = ChatColor.GREEN + "Changed your glow effect to " + ChatColor.BOLD;
                    for (int i = 0; i < effectName.length(); i++) {
                        ChatColor chatColor = ChatColor.valueOf(colors[i % colors.length]);
                        message += chatColor + effectName.substring(i, i + 1);
                    }
                    p.sendMessage(message);
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
                return;
            }
        }

        // Parse color
        ChatColor chatColor;
        try {
            chatColor = ChatColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            p.sendMessage(ChatColor.RED + "Invalid color selection.");
            return;
        }

        // Check for permissions
        String permission = "eglow.color." + chatColor.name().toLowerCase().replace("_", "");
        if (p.hasPermission(permission)) {
            // Remove old color and add new color
            if (p.isGlowing()) {
                removeGlowColor(p);
            }

            // Find or create team
            String teamName = "glow_" + color;
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
                team.setColor(ChatColor.valueOf(color));
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }

            // Add player to team and enable their glow
            team.addEntry(p.getName());
            p.setGlowing(true);
            p.sendMessage(ChatColor.GREEN + "Changed your glow color to " + ChatColor.BOLD + ChatColor.valueOf(color) + getColorName(color));
        } else {
            p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
        }
    }

    public static void removeGlowColor(Player p) {
        // Remove player from all glow teams
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(p.getName()) && team.getName().startsWith("glow_")) {
                team.removeEntry(p.getName());
            }
        }

        // Disable player's glow
        p.setGlowing(false);
    }
}