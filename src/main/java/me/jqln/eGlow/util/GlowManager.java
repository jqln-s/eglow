package me.jqln.eGlow.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    public static void addGlowColor(Player p, ChatColor color) {
        // Find or create team
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