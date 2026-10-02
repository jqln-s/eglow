package me.jqln.eGlow.util;

import me.jqln.eGlow.EGlow;
import me.jqln.eGlow.commands.GlowCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GlowTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        // Complete for first argument only
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            LinkedHashMap<String, String> colorList = GlowCommand.createColors();

            // Add basic colors
            for (String color : colorList.keySet()) {
                if (player.hasPermission("eglow.color." + color.toLowerCase().replace("_", ""))) {
                    list.add(color);
                }
            }

            // Add color effects
            ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");
            for (String effect : effects.getKeys(false)) {
                if (player.hasPermission(effects.getConfigurationSection(effect).getString("permission"))) {
                    list.add(effect.toUpperCase());
                }
            }

            // Add reset option
            list.add("RESET");

            return list;
        }
        return null;
    }
}
