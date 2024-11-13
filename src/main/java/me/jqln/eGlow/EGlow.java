package me.jqln.eGlow;

import me.jqln.eGlow.commands.GlowCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class EGlow extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Register commands
        getCommand("glow").setExecutor(new GlowCommand());

        new BukkitRunnable() {
            // Define teams
            final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team lightTeam = scoreboard.getTeam("glow_LIGHT_RAINBOW");
            Team darkTeam = scoreboard.getTeam("glow_DARK_RAINBOW");

            int i = 0;

            // Define colors
            final ChatColor[] lightColors = {
                    ChatColor.RED,
                    ChatColor.YELLOW,
                    ChatColor.GREEN,
                    ChatColor.AQUA,
                    ChatColor.BLUE,
                    ChatColor.LIGHT_PURPLE
            };
            final ChatColor[] darkColors = {
                    ChatColor.DARK_RED,
                    ChatColor.GOLD,
                    ChatColor.DARK_GREEN,
                    ChatColor.DARK_AQUA,
                    ChatColor.DARK_BLUE,
                    ChatColor.DARK_PURPLE
            };

            @Override
            public void run() {
                // Create teams if not created already
                if (lightTeam == null) {
                    lightTeam = scoreboard.registerNewTeam("glow_LIGHT_RAINBOW");
                    lightTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                }
                if (darkTeam == null) {
                    darkTeam = scoreboard.registerNewTeam("glow_DARK_RAINBOW");
                    darkTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                }

                // Change colors
                lightTeam.setColor(lightColors[i % lightColors.length]);
                darkTeam.setColor(darkColors[i % darkColors.length]);

                i++;
            }
        }.runTaskTimer(this, 0, 10); // Execute run() every 10 ticks (half second)
    }
}