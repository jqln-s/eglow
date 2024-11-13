package me.jqln.eGlow.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

                if (strings[0].equalsIgnoreCase("rainbow")) {
                    if (p.hasPermission("eglow.effect.darkrainbow")) {
                        // Find dark rainbow team
                        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                        Team team = scoreboard.getTeam("glow_DARK_RAINBOW");

                        // Add player to light rainbow team and enable their glow
                        team.addEntry(p.getName());
                        p.setGlowing(true);

                        // Send appropriately colored confirmation message
                        p.sendMessage(ChatColor.GREEN + "Changed your glow color to " +
                                ChatColor.DARK_RED + "R" +
                                ChatColor.GOLD + "a" +
                                ChatColor.DARK_GREEN + "i" +
                                ChatColor.DARK_AQUA + "n" +
                                ChatColor.DARK_BLUE + "b" +
                                ChatColor.DARK_PURPLE + "o" +
                                ChatColor.DARK_RED + "w");

                        return true;
                    } else {
                        p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        return true;
                    }
                }

                if (strings[0].equalsIgnoreCase("light_rainbow")) {
                    if (p.hasPermission("eglow.effect.lightrainbow")) {
                        // Find light rainbow team
                        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                        Team team = scoreboard.getTeam("glow_LIGHT_RAINBOW");

                        // Add player to light rainbow team and enable their glow
                        team.addEntry(p.getName());
                        p.setGlowing(true);

                        // Send appropriately colored confirmation message
                        p.sendMessage(ChatColor.GREEN + "Changed your glow color to " +
                                ChatColor.RED + "R" +
                                ChatColor.YELLOW + "a" +
                                ChatColor.GREEN + "i" +
                                ChatColor.AQUA + "n" +
                                ChatColor.BLUE + "b" +
                                ChatColor.LIGHT_PURPLE + "o" +
                                ChatColor.RED + "w");

                        return true;
                    } else {
                        p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        return true;
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
                    String colorName = getColorName(chatColor);
                    p.sendMessage(ChatColor.GREEN + "Changed your glow color to " + chatColor + colorName);
                } else {
                    p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                }
            }
        }
        return true;
    }

    private static String getColorName(ChatColor chatColor) {
        // Replace all underscores with spaces and capitalize all words
        String colorName = chatColor.name().toLowerCase().replace("_", " ");
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