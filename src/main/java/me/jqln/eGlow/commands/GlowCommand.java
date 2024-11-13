package me.jqln.eGlow.commands;

import me.jqln.eGlow.EGlow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GlowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("glow")) {
            if (commandSender instanceof Player p) {
                // Disable glow or redirect player if no color provided
                if (strings.length == 0) {
                    if (p.isGlowing()) {
                        removeGlowColor(p);
                        p.sendMessage(ChatColor.GREEN + "Removed your glow color.");
                    } else {
                        p.sendMessage(ChatColor.RED + "Usage: /glow <color>");
                    }

                    return true;
                }

                ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");

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
                            String effectName = getColorName(effect);
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
                        removeGlowColor(p);
                    }
                    addGlowColor(p, chatColor);

                    // Format color name and send confirmation message
                    String colorName = getColorName(chatColor.name());
                    p.sendMessage(ChatColor.GREEN + "Changed your glow color to " + ChatColor.BOLD + chatColor + colorName);
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }
        }
        return true;
    }

    private static String getColorName(String color) {
        // Replace all underscores with spaces and capitalize all words
        String colorName = color.toLowerCase().replace("_", " ");
        return Arrays.stream(colorName.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static void addGlowColor(Player p, ChatColor color) {
        // Find or create team
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "glow_" + color.name();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setColor(color);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }

        // Add player to team and enable their glow
        team.addEntry(p.getName());
        p.setGlowing(true);
    }

    public static void removeGlowColor(Player p) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

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