package me.jqln.eGlow.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jqln.eGlow.EGlow;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public String getAuthor() {
        return "jqln";
    }

    @Override
    public String getIdentifier() {
        return "eglow";
    }

    @Override
    public String getVersion() {
        return "3.1";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (identifier.equals("color")) {
            return "" + EGlow.getColor(p);
        }

        return null;
    }
}