package com.cavetale.area;

import com.cavetale.area.struct.AreasFile;
import com.cavetale.area.struct.Cuboid;
import com.cavetale.area.worldedit.WorldEdit;
import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class AreaCommand extends AbstractCommand<AreaPlugin> {
    protected AreaCommand(final AreaPlugin plugin) {
        super(plugin, "area");
    }

    protected void onEnable() {
        rootNode.addChild("add")
            .arguments("<file> <name> [subname]")
            .description("Add an area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::add);
        rootNode.addChild("remove")
            .arguments("<file> <name> <index>")
            .description("Remove area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::remove);
        rootNode.addChild("list")
            .arguments("[file] [name] [subname]")
            .description("List areas")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::list);
        rootNode.addChild("highlight")
            .alias("hl")
            .arguments("[file] [name] [subname]")
            .description("Highlight areas")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::highlight);
    }

    boolean add(Player player, String[] args) {
        if (args.length != 2 && args.length != 3) return false;
        String fileArg = args[0];
        String nameArg = args[1];
        String subnameArg = args.length >= 3 ? args[2] : null;
        Cuboid cuboid = getSelection(player).named(subnameArg);
        World world = player.getWorld();
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) areasFile = new AreasFile();
        areasFile.areas.computeIfAbsent(nameArg, u -> new ArrayList<>()).add(cuboid);
        areasFile.save(world, fileArg);
        player.sendMessage("Area added to " + world.getName() + "/" + fileArg + "/" + nameArg + ": " + cuboid);
        return true;
    }

    boolean list(Player player, String[] args) {
        if (args.length > 3) return false;
        World world = player.getWorld();
        if (args.length == 0) {
            File folder = new File(world.getWorldFolder(), "areas");
            if (!folder.isDirectory()) throw new CommandWarn("No areas to show!");
            List<String> names = new ArrayList<>();
            for (File file : folder.listFiles()) {
                String name = file.getName();
                if (name.endsWith(".json")) {
                    names.add(name.substring(0, name.length() - 5));
                }
            }
            player.sendMessage(Component.text(names.size() + " area files: " + String.join(", ", names),
                                              NamedTextColor.YELLOW));
            return true;
        }
        String fileArg = args[0];
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) {
            throw new CommandWarn("No areas file found: " + fileArg);
        }
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Map.Entry<String, List<Cuboid>> entry : areasFile.areas.entrySet()) {
                names.add(entry.getKey() + "(" + entry.getValue().size() + ")");
            }
            player.sendMessage(Component.text(fileArg + ": " + names.size() + " area lists: " + String.join(", ", names),
                                              NamedTextColor.YELLOW));
            return true;
        }
        String nameArg = args[1];
        if (args.length == 2) {
            String path = world.getName() + "/" + fileArg + "/" + nameArg;
            List<Cuboid> list = areasFile.areas.get(nameArg);
            if (list == null) {
                throw new CommandWarn("Area list not found: " + path);
            }
            player.sendMessage(ChatColor.YELLOW + path + ": " + list.size() + " areas");
            int index = 0;
            for (Cuboid cuboid : list) {
                player.sendMessage("" + ChatColor.YELLOW + index + ") " + ChatColor.WHITE + cuboid);
                index += 1;
            }
            return true;
        }
        String subnameArg = args[2];
        if (args.length == 3) {
            String path = world.getName() + "/" + fileArg + "/" + nameArg + "/" + subnameArg;
            List<Cuboid> list = areasFile.find(nameArg, subnameArg);
            if (list.isEmpty()) {
                throw new CommandWarn("Sublist not found: " + path);
            }
            player.sendMessage(ChatColor.YELLOW + path + ": " + list.size() + " areas");
            int index = 0;
            for (Cuboid cuboid : list) {
                player.sendMessage("" + ChatColor.YELLOW + index + ") " + ChatColor.WHITE + cuboid);
                index += 1;
            }
        }
        return true;
    }

    boolean highlight(Player player, String[] args) {
        if (args.length < 1 || args.length > 3) return false;
        World world = player.getWorld();
        String fileArg = args[0];
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) {
            throw new CommandWarn("No areas file found: " + fileArg);
        }
        final String path;
        final List<Cuboid> list;
        if (args.length == 1) {
            path = fileArg;
            list = areasFile.all();
        } else if (args.length == 2) {
            path = fileArg + "/" + args[1];
            list = areasFile.find(args[1]);
        } else if (args.length == 3) {
            path = fileArg + "/" + args[1] + "/" + args[2];
            list = areasFile.find(args[1], args[2]);
        } else {
            throw new IllegalStateException();
        }
        if (list.isEmpty()) {
            throw new CommandWarn("Areas not found: " + path);
        }
        player.sendMessage(Component.text("Highlighting " + list.size() + " areas:", NamedTextColor.YELLOW));
        for (Cuboid cuboid : list) {
            cuboid.highlight(world, player);
            player.sendMessage(Component.text("- " + cuboid, NamedTextColor.WHITE));
        }
        return true;
    }

    boolean remove(Player player, String[] args) {
        if (args.length != 3) return false;
        World world = player.getWorld();
        String fileArg = args[0];
        String nameArg = args[1];
        String indexArg = args[2];
        int index;
        try {
            index = Integer.parseInt(indexArg);
        } catch (NumberFormatException nfe) {
            throw new CommandWarn("Number expected: " + indexArg);
        }
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) {
            throw new CommandWarn("Areas file not found: " + fileArg);
        }
        List<Cuboid> areas = areasFile.areas.get(nameArg);
        if (areas == null) {
            throw new CommandWarn("Areas list not found: " + nameArg);
        }
        if (index < 0 || index >= areas.size()) {
            throw new CommandWarn("Index out of bouns: " + index + "/" + areas.size());
        }
        Cuboid cuboid = areas.remove(index);
        if (areas.isEmpty()) {
            areasFile.areas.remove(nameArg);
        }
        areasFile.save(world, fileArg);
        player.sendMessage("Cuboid removed: " + world.getName() + "/" + fileArg + "/" + nameArg
                           + "[" + index + "]: " + cuboid);
        return true;
    }

    Cuboid getSelection(Player player) {
        Cuboid cuboid = WorldEdit.getSelection(player);
        if (cuboid == null) throw new CommandWarn("WorldEdit selection required!");
        return cuboid;
    }

    List<String> fileAreaCompleter(CommandContext context, CommandNode node, String[] args) {
        System.out.println("complete " + args.length + " " + context.player);
        if (args.length == 0) return null;
        if (context.player == null) return null;
        String arg = args[args.length - 1];
        if (args.length == 1) {
            File folder = new File(context.player.getWorld().getWorldFolder(), "areas");
            if (!folder.isDirectory()) return List.of();
            List<String> result = new ArrayList<>();
            for (File file : folder.listFiles()) {
                String name = file.getName();
                if (!name.endsWith(".json")) continue;
                name = name.substring(0, name.length() - 5);
                if (name.contains(arg)) {
                    result.add(name);
                }
            }
            return result;
        }
        if (args.length == 2) {
            AreasFile areasFile = AreasFile.load(context.player.getWorld(), args[0]);
            if (areasFile == null) return List.of();
            return areasFile.areas.keySet().stream()
                .filter(s -> s.contains(arg))
                .collect(Collectors.toList());
        }
        if (args.length == 3) {
            AreasFile areasFile = AreasFile.load(context.player.getWorld(), args[0]);
            if (areasFile == null) return List.of();
            List<Cuboid> list = areasFile.find(args[1]);
            if (list.isEmpty()) return List.of();
            return list.stream()
                .map(Cuboid::getName)
                .filter(Objects::nonNull)
                .filter(s -> args[2].contains(s))
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
