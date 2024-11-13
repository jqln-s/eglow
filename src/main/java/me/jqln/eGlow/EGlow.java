package me.jqln.eGlow;

import me.jqln.eGlow.commands.GlowCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class EGlow extends JavaPlugin implements Listener {
    private static EGlow plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Write default config.yml file
        saveDefaultConfig();

        // Register commands
        getCommand("glow").setExecutor(new GlowCommand());

        // Get the effects section of config.yml
        ConfigurationSection effects = getConfig().getConfigurationSection("effects");

        // Iterate through each effect
        for (String effect : effects.getKeys(false)) {
            int timer = effects.getConfigurationSection(effect).getInt("cycle-tick-speed");

            new BukkitRunnable() {
                // Define teams
                final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team team = scoreboard.getTeam("glow_" + effect.toUpperCase());

                int i = 0;

                // Define colors
                final String[] colors = effects.getConfigurationSection(effect).getStringList("colors").toArray(new String[0]);

                @Override
                public void run() {
                    // Create team if not already created
                    if (team == null) {
                        team = scoreboard.registerNewTeam("glow_" + effect.toUpperCase());
                        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                    }

                    // Validate and set color
                    try {
                        ChatColor color = ChatColor.valueOf(colors[i % colors.length]);
                        team.setColor(color);
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning(colors[i % colors.length] + " color in " + effect + " effect is not a valid color!");
                        this.cancel();
                    }

                    i++;
                }
            }.runTaskTimer(this, 0, timer);
        }
    }

    public static EGlow getInstance() {
        return plugin;
    }
}