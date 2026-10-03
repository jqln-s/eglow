package me.jqln.eGlow.commands;

import me.jqln.eGlow.EGlow;
import me.jqln.eGlow.util.GlowManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.UUID;

public class GlowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Handle /glow
        if (command.getName().equalsIgnoreCase("glow")) {
            if (commandSender instanceof Player p) {
                // Open GUI if no args
                if (strings.length == 0) {
                    Inventory inventory = Bukkit.createInventory(p, 9 * 6, "Glow Color");
                    // Color - Skull URL map
                    LinkedHashMap<String, String> colors = createColors();

                    // Get GUI config
                    ConfigurationSection guiConfig = EGlow.getInstance().getConfig().getConfigurationSection("gui");
                    ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");
                    ConfigurationSection slots = guiConfig.getConfigurationSection("slots");
                    Material emptySlot = Material.getMaterial(guiConfig.getString("empty-slot"));

                    // Set empty slots and basic colors
                    for (int i = 0; i < 54; i ++) {
                        String slot = slots.getString(Integer.toString(i));

                        if (slot == null) {
                            ItemStack item = new ItemStack(emptySlot);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(" ");
                            item.setItemMeta(meta);
                            inventory.setItem(i, item);
                        } else if (colors.containsKey(slot)) {
                            // Create skull item
                            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta skull = (SkullMeta) item.getItemMeta();

                            // Assign UUID to player profile
                            final UUID uuid = UUID.randomUUID();
                            final PlayerProfile profile = Bukkit.createPlayerProfile(uuid, uuid.toString().substring(0, 16));

                            // Apply skin URL to player profile
                            PlayerTextures texture = profile.getTextures();
                            try {
                                texture.setSkin(URI.create(colors.get(slot)).toURL());
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                            profile.setTextures(texture);
                            skull.setOwnerProfile(profile);

                            // Assign player profile to skull item
                            item.setItemMeta(skull);
                            ItemMeta meta = item.getItemMeta();

                            // Assign color name to skull item
                            meta.setDisplayName(GlowManager.getColorName(slot));
                            item.setItemMeta(meta);

                            // Set item in GUI
                            inventory.setItem(i, item);
                        } else if (slot.equals("RESET")) {
                            ItemStack item = new ItemStack(Material.BARRIER);
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName("Reset Color");
                            item.setItemMeta(meta);
                            inventory.setItem(i, item);
                        } else if (effects.getConfigurationSection(slot.toLowerCase()) != null) {
                            ConfigurationSection effect = effects.getConfigurationSection(slot.toLowerCase());
                            Material guiItem = Material.getMaterial(effect.getString("gui-item"));
                            ItemStack item = new ItemStack(guiItem);

                            if (guiItem == Material.PLAYER_HEAD) {
                                SkullMeta skull = (SkullMeta) item.getItemMeta();

                                // Assign UUID to player profile
                                final UUID uuid = UUID.randomUUID();
                                final PlayerProfile profile = Bukkit.createPlayerProfile(uuid, uuid.toString().substring(0, 16));

                                // Apply skin URL to player profile
                                PlayerTextures texture = profile.getTextures();
                                try {
                                    texture.setSkin(URI.create(effect.getString("skull-url")).toURL());
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                                profile.setTextures(texture);
                                skull.setOwnerProfile(profile);

                                // Assign player profile to skull item
                                item.setItemMeta(skull);
                            }

                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(GlowManager.getColorName(slot));
                            item.setItemMeta(meta);

                            // Set item in GUI
                            inventory.setItem(i, item);
                        }
                    }

                    // Open GUI
                    p.openInventory(inventory);
                    p.setMetadata("OpenedMenu", new FixedMetadataValue(EGlow.getInstance(), inventory));

                    return true;
                }

                // Remove glow
                if (strings[0].equalsIgnoreCase("reset")) {
                    GlowManager.removeGlowColor(p);
                    p.sendMessage(ChatColor.GREEN + "Reset your glow color!");
                    return true;
                }

                if (strings[0].equalsIgnoreCase("reload")) {
                    if (!p.hasPermission("eglow.reload")) {
                        p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                        return true;
                    }

                    // Cancel scheduler and reload config
                    EGlow.getGlowCycle().cancel();
                    EGlow.getInstance().reloadConfig();

                    // Get the effects section of config.yml
                    ConfigurationSection effects = EGlow.getInstance().getConfig().getConfigurationSection("effects");

                    // Iterate through each effect
                    for (String effect : effects.getKeys(false)) {
                        int timer = effects.getConfigurationSection(effect).getInt("cycle-tick-speed");

                        EGlow.setGlowCycle(new BukkitRunnable() {
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
                        }.runTaskTimer(EGlow.getInstance(), 0, timer));
                    }

                    p.sendMessage(ChatColor.GREEN + "Config reloaded successfully!");
                    return true;
                }

                // Directly add color if specified
                GlowManager.addGlowColor(p, strings[0]);
            }
        }
        return true;
    }

    public static @NotNull LinkedHashMap<String, String> createColors() {
        LinkedHashMap<String, String> colors = new LinkedHashMap<>();
        colors.put("RED", "http://textures.minecraft.net/texture/63f79b207d61e122523b83d61508d99cfa079d45bf23df2a9a5127f9071d4b00");
        colors.put("DARK_RED", "http://textures.minecraft.net/texture/68d40935279771adc63936ed9c8463abdf5c5ba78d2e86cb1ec10b4d1d225fb");
        colors.put("GOLD", "http://textures.minecraft.net/texture/2090d09e173ee34138c3b01b48ee0be534bbb1ace0ddf5ff98e66f7b02113995");
        colors.put("YELLOW", "http://textures.minecraft.net/texture/a89b93fd616ed3670ccf647a0f9380398c0d4615634f2deff46c6edbdc712885");
        colors.put("GREEN", "http://textures.minecraft.net/texture/925b8eed5c565bd440ec47c79c20d5cf370162b1d9b5dd3100ed6283fe01d6e");
        colors.put("DARK_GREEN", "http://textures.minecraft.net/texture/1e38fa3131e2da04a8f8d50872a8232d7d9dea340d06c9097ffa3cc48208df1d");
        colors.put("AQUA", "http://textures.minecraft.net/texture/2d6a8b47da923b7d10142447fdbdcfd1e8e82eb484964252bb36ddb5f73b51c2");
        colors.put("DARK_AQUA", "http://textures.minecraft.net/texture/31f57051130e850848e8e37e72110a16f09dbdab7d9d6e33a9fecfd348d5a110");
        colors.put("BLUE", "http://textures.minecraft.net/texture/3f9c32138c9764c639aebd819cd91992aed01bf448f0e710a03ab443ac490ee9");
        colors.put("DARK_BLUE", "http://textures.minecraft.net/texture/e1194fe9edf583c0ebe7dc1d34309beefb229bb15b6a8c3c7b0c76de27c8b7bf");
        colors.put("LIGHT_PURPLE", "http://textures.minecraft.net/texture/dcf2835180cbfec3b317d6a47491a74ae71435ba169a57925b9096ea2f9c61b6");
        colors.put("DARK_PURPLE", "http://textures.minecraft.net/texture/9b82e72b8e4832e5a114ab0fc127c8acb83f31fd4d266d08b2cacc5b6401a400");
        colors.put("BLACK", "http://textures.minecraft.net/texture/2a52d579afe2fdf7b8ecfa746cd016150d96beb75009bb2733ade15d487c42a1");
        colors.put("DARK_GRAY", "http://textures.minecraft.net/texture/ff9bb9e56125c8227b94bbda9f6e0f862931c229255ba8f1205d13c44c1bb561");
        colors.put("GRAY", "http://textures.minecraft.net/texture/1c8e0ddf2432f4332b87691b5952c7679763ef4f275b874e9bceb888ed5b5b9");
        colors.put("WHITE", "http://textures.minecraft.net/texture/1884d5dabe073e28e6b7eb166ff61247905c79f838b6f5752e7ad406091eeaf3");
        return colors;
    }
}