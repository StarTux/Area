package com.cavetale.area;

import com.cavetale.area.struct.Area;
import com.cavetale.area.struct.AreasFile;
import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandContext;
import com.cavetale.core.command.CommandNode;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.struct.Cuboid;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class AreaCommand extends AbstractCommand<AreaPlugin> {
    protected AreaCommand(final AreaPlugin plugin) {
        super(plugin, "area");
    }

    protected void onEnable() {
        rootNode.addChild("add").arguments("<file> <name> [subname]")
            .description("Add an area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::add);
        rootNode.addChild("addhere").arguments("[file] [subname]")
            .description("Add an area to the named list here")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::addhere);
        rootNode.addChild("addslice").arguments("<file> <name> [subname]")
            .description("Add an top-to-bottom area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::addSlice);
        rootNode.addChild("remove").arguments("<file> <name> <index>")
            .description("Remove area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::remove);
        rootNode.addChild("redefine").arguments("<file> <name> <index>")
            .description("Redefine area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::redefine);
        rootNode.addChild("list").arguments("[file] [name] [index]")
            .description("List areas")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::list);
        rootNode.addChild("listhere").arguments("[file]")
            .alias("lshere")
            .description("List subareas belonging to current area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::listHere);
        rootNode.addChild("here").arguments("[file]")
            .description("List areas here")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::here);
        rootNode.addChild("highlight")
            .alias("hl").arguments("[file] [name] [index]")
            .description("Highlight areas")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::highlight);
        rootNode.addChild("highlighthere").arguments("[file]")
            .alias("hlhere")
            .description("Highlight subareas belonging to current area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::highlightHere);
        rootNode.addChild("teleport")
            .alias("tp").arguments("<file> <name> <index>")
            .description("Teleport to area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::teleport);
        rootNode.addChild("rename").arguments("<file> <name> <index> <newname>")
            .description("Rename an area")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::rename);
        rootNode.addChild("renamelist").arguments("<file> <name> <newname>")
            .description("Rename an area list")
            .completer(this::fileAreaCompleter)
            .playerCaller(this::renameList);
    }

    private boolean add(Player player, String[] args) {
        if (args.length != 2 && args.length != 3) return false;
        String fileArg = args[0];
        String nameArg = args[1];
        String subnameArg = args.length >= 3 ? args[2] : null;
        Area area = getSelection(player).withName(subnameArg);
        World world = player.getWorld();
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) areasFile = new AreasFile();
        areasFile.areas.computeIfAbsent(nameArg, u -> new ArrayList<>()).add(area);
        areasFile.save(world, fileArg);
        player.sendMessage(textOfChildren(text("Area added to ", GRAY),
                                          text(areasFile.getPath() + "/" + nameArg),
                                          text(": ", GRAY),
                                          text(area.toString(), YELLOW)));
        return true;
    }

    private boolean addhere(Player player, String[] args) {
        if (args.length != 0 && args.length != 1 && args.length != 2) return false;
        String fileArg = args.length >= 1 ? args[0] : null;
        String subnameArg = args.length >= 2 ? args[1] : null;
        Area area = getSelection(player).withName(subnameArg);
        World world = player.getWorld();
        AreasFile areasFile = fileArg != null ? AreasFile.require(world, args[0]) : AreasFile.requireSingular(world);
        if (areasFile == null) areasFile = new AreasFile();
        Location location = player.getLocation();
        String areaName = null;
        for (Map.Entry<String, List<Area>> entry : areasFile.areas.entrySet()) {
            Area mainArea = entry.getValue().get(0);
            if (mainArea.contains(location)) {
                areaName = entry.getKey();
                entry.getValue().add(area);
                break;
            }
        }
        if (areaName == null) {
            throw new CommandWarn("There is no area list here");
        }
        areasFile.save(world, areasFile.getFileName());
        player.sendMessage(textOfChildren(text("Area added to ", GRAY),
                                          text(areasFile.getPath() + "/" + areaName, YELLOW),
                                          text(": ", GRAY),
                                          text(area.toString(), YELLOW)));
        return true;
    }

    private boolean addSlice(Player player, String[] args) {
        if (args.length != 2 && args.length != 3) return false;
        String fileArg = args[0];
        String nameArg = args[1];
        String subnameArg = args.length >= 3 ? args[2] : null;
        World world = player.getWorld();
        Cuboid cuboid = Cuboid.requireSelectionOf(player);
        cuboid = new Cuboid(cuboid.ax, world.getMinHeight(), cuboid.az,
                            cuboid.bx, world.getMaxHeight(), cuboid.bz);
        AreasFile areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) areasFile = new AreasFile();
        if (areasFile.areas.containsKey(nameArg)) {
            throw new CommandWarn("Already exists: " + nameArg);
        }
        for (String oldName : areasFile.areas.keySet()) {
            List<Area> oldList = areasFile.areas.get(oldName);
            if (oldList.get(0).toCuboid().overlaps(cuboid)) {
                throw new CommandWarn("Slice would overlap with " + oldName + ": " + oldList.get(0));
            }
        }
        final List<Area> list = new ArrayList<>();
        final Area area = new Area(cuboid.getMin(), cuboid.getMax());
        list.add(area);
        areasFile.areas.put(nameArg, list);
        areasFile.save(world, fileArg);
        player.sendMessage(textOfChildren(text("Slice added ", GRAY),
                                          text(areasFile.getPath() + "/" + nameArg),
                                          text(": ", GRAY),
                                          text(area.toString(), YELLOW)));
        return true;
    }

    private boolean list(Player player, String[] args) {
        if (args.length == 0) {
            File folder = new File(player.getWorld().getWorldFolder(), "areas");
            if (!folder.isDirectory()) {
                throw new CommandWarn("Areas folder not found: " + player.getWorld().getWorldFolder());
            }
            List<Component> names = new ArrayList<>();
            for (File file : folder.listFiles()) {
                if (!file.isFile()) continue;
                String name = file.getName();
                if (!name.endsWith(".json")) continue;
                names.add(text(name.substring(0, name.length() - 5), YELLOW));
            }
            if (names.isEmpty()) {
                throw new CommandWarn("No area files found: " + player.getWorld().getWorldFolder());
            }
            player.sendMessage(join(noSeparators(),
                                    text(names.size() + " area files: ", GRAY),
                                    join(separator(text(", ", DARK_GRAY)), names)));
            return true;
        }
        AreaArgument areaArgument = AreaArgument.of(player, args);
        if (areaArgument == null) return true;
        if (areaArgument.hasIndexArg()) {
            // Print indexed/named areas
            List<Area> areas = areaArgument.requireAreas();
            player.sendMessage(join(noSeparators(),
                                    text(areas.size() + " areas: ", AQUA),
                                    text(areaArgument.getPath(), YELLOW)));
            int index = 0;
            for (Area area : areas) {
                player.sendMessage(join(noSeparators(),
                                        text((index++) + ") ", GRAY),
                                        text(area.toString(), YELLOW)));
            }
            return true;
        } else if (areaArgument.hasAreaListArg()) {
            // Print named list
            List<Area> areaList = areaArgument.requireAreaList();
            player.sendMessage(join(noSeparators(),
                                    text(areaList.size() + " areas: ", AQUA),
                                    text(areaArgument.getPath(), YELLOW)));
            int index = 0;
            for (Area area : areaList) {
                player.sendMessage(join(noSeparators(),
                                        text((index++) + ") ", GRAY),
                                        text(area.toString(), YELLOW)));
            }
            return true;
        } else if (areaArgument.hasAreasFileArg()) {
            // Print lists
            AreasFile areasFile = areaArgument.requireAreasFile();
            player.sendMessage(join(noSeparators(),
                                    text(areasFile.areas.size() + " area lists: ", AQUA),
                                    text(areaArgument.getPath(), YELLOW)));
            for (Map.Entry<String, List<Area>> entry : areasFile.areas.entrySet()) {
                String name = entry.getKey();
                List<Area> areaList = entry.getValue();
                Area area = !areaList.isEmpty() ? areaList.get(0) : null;
                player.sendMessage(join(noSeparators(),
                                        text("." + name + " ", GRAY),
                                        text("(" + areaList.size() + ") ", AQUA),
                                        (area != null
                                         ? text(area.toString(), YELLOW)
                                         : text("null", DARK_GRAY, ITALIC))));
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean listHere(Player player, String[] args) {
        if (args.length != 0 && args.length != 1) return false;
        AreaArgument areaArgument = args.length >= 1
            ? AreaArgument.at(player, args[0])
            : AreaArgument.at(player, AreasFile.requireSingular(player.getWorld()));
        if (areaArgument == null) return false;
        List<Area> areaList = areaArgument.requireAreaList();
        player.sendMessage(join(noSeparators(),
                                text(areaList.size() + " areas in ", GRAY),
                                text(areaArgument.getNameArg(), YELLOW)));
        int index = 0;
        for (Area area : areaList) {
            player.sendMessage(join(noSeparators(),
                                    text((index++) + ") ", GRAY),
                                    text(area.toString(), YELLOW)));
        }
        return true;
    }

    private boolean here(Player player, String[] args) {
        if (args.length != 0 && args.length != 1) return false;
        World world = player.getWorld();
        AreasFile areasFile = args.length >= 1 ? AreasFile.require(world, args[0]) : AreasFile.requireSingular(world);
        Location location = player.getLocation();
        player.sendMessage(textOfChildren(text("Areas here in ", GRAY), text(areasFile.getPath(), YELLOW)));
        for (Map.Entry<String, List<Area>> entry : areasFile.areas.entrySet()) {
            String name = entry.getKey();
            int i = 0;
            for (Area area : entry.getValue()) {
                int index = i++;
                if (area.contains(player.getLocation())) {
                    player.sendMessage(join(noSeparators(),
                                            text(name, GOLD),
                                            text(" " + index + ") ", GRAY),
                                            text(area.toString(), YELLOW)));
                }
            }
        }
        return true;
    }

    private boolean highlight(Player player, String[] args) {
        AreaArgument areaArgument = AreaArgument.of(player, args);
        if (areaArgument == null) return false;
        List<Area> list = areaArgument.requireAnyAreas();
        player.sendMessage(join(noSeparators(),
                                text("Highlighting " + list.size() + " area(s): ", GRAY),
                                text(areaArgument.getPath(), YELLOW)));
        Location location = player.getLocation();
        for (Area area : list) {
            player.sendMessage(join(noSeparators(),
                                    text("- ", GRAY),
                                    text(area.toString(), YELLOW)));
            if (!area.outset(64).contains(location)) continue;
            area.highlight(player.getWorld(), player);
        }
        return true;
    }

    private boolean highlightHere(Player player, String[] args) {
        if (args.length != 0 && args.length != 1) return false;
        AreaArgument areaArgument = args.length >= 1
            ? AreaArgument.at(player, args[0])
            : AreaArgument.at(player, AreasFile.requireSingular(player.getWorld()));
        if (areaArgument == null) return false;
        List<Area> areaList = areaArgument.requireAreaList();
        player.sendMessage(join(noSeparators(),
                                text(areaList.size() + " areas in ", GRAY),
                                text(areaArgument.getNameArg(), YELLOW)));
        int index = 0;
        Location location = player.getLocation();
        for (Area area : areaList) {
            player.sendMessage(join(noSeparators(),
                                    text((index++) + ") ", GRAY),
                                    text(area.toString(), YELLOW)));
            if (!area.outset(64).contains(location)) continue;
            area.highlight(player.getWorld(), player);
        }
        return true;
    }

    private boolean remove(Player player, String[] args) {
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
        List<Area> areas = areasFile.areas.get(nameArg);
        if (areas == null) {
            throw new CommandWarn("Areas list not found: " + nameArg);
        }
        if (index < 0 || index >= areas.size()) {
            throw new CommandWarn("Index out of bounds: " + index + "/" + areas.size());
        }
        Area area = areas.remove(index);
        if (areas.isEmpty()) {
            areasFile.areas.remove(nameArg);
        }
        areasFile.save(world, fileArg);
        player.sendMessage(textOfChildren(text("Area removed: ", GRAY),
                                          text(areasFile.getPath() + "/" + nameArg + "[" + index + "]", YELLOW),
                                          text(": ", GRAY),
                                          text(area.toString(), YELLOW)));
        return true;
    }

    private boolean redefine(Player player, String[] args) {
        AreaArgument areaArgument = AreaArgument.of(player, args);
        if (areaArgument == null || !areaArgument.hasIndexArg()) return false;
        Area area = areaArgument.requireSingleArea();
        Area selection = getSelection(player);
        Area newArea = area.withArea(selection);
        List<Area> areaList = areaArgument.requireAreaList();
        int index = areaList.indexOf(area);
        areaList.set(index, newArea);
        areaArgument.save();
        player.sendMessage(join(noSeparators(),
                                text("Redefined ", GRAY),
                                text(areaArgument.getPath(), YELLOW),
                                text(" => ", GRAY),
                                text(newArea.toString(), YELLOW)));
        return true;
    }

    private boolean teleport(Player player, String[] args) {
        AreaArgument areaArgument = AreaArgument.of(player, args);
        if (areaArgument == null || !areaArgument.hasIndexArg()) return false;
        Area area = areaArgument.requireSingleArea();
        Location location = area.getCenter().toBlock(player.getWorld()).getLocation().add(0.5, 0.0, 0.5);
        player.teleport(location, TeleportCause.COMMAND);
        player.sendMessage(join(noSeparators(),
                                text("Teleported to ", GRAY),
                                text(areaArgument.getPath(), YELLOW)));
        return true;
    }

    private boolean rename(Player player, String[] args) {
        if (args.length != 4) return false;
        AreaArgument areaArgument = AreaArgument.of(player, Arrays.copyOfRange(args, 0, 3));
        if (areaArgument == null) return false;
        areaArgument.requireNumberIndex();
        final String newName = args[3];
        areaArgument.requireAreaList().set(areaArgument.getIndex(), areaArgument.requireSingleArea().withName(newName));
        areaArgument.save();
        player.sendMessage(join(noSeparators(),
                                text("Renamed ", GRAY),
                                text(areaArgument.getPath(), YELLOW)));
        return true;
    }

    private boolean renameList(Player player, String[] args) {
        if (args.length != 3) return false;
        final String fileArg = args[0];
        final String from = args[1];
        final String to = args[2];
        World world = player.getWorld();
        AreasFile areasFile = AreasFile.require(world, fileArg);
        if (areasFile.areas.containsKey(to)) {
            throw new CommandWarn("Area list already exists: " + world.getName() + "/" + fileArg + "/" + to);
        }
        List<Area> list = areasFile.areas.remove(from);
        if (list == null) {
            throw new CommandWarn("Area list not found: " + world.getName() + "/" + fileArg + "/" + from);
        }
        areasFile.areas.put(to, list);
        areasFile.save(world, fileArg);
        player.sendMessage(join(noSeparators(),
                                text("List "),
                                text(world.getName() + "/" + fileArg + "/" + from, YELLOW),
                                text(" renamed to "),
                                text(to, YELLOW))
                           .color(AQUA));
        return true;
    }

    protected Area getSelection(Player player) {
        Cuboid cuboid = Cuboid.requireSelectionOf(player);
        Area area = new Area(cuboid.getMin(), cuboid.getMax());
        return area;
    }

    private List<String> fileAreaCompleter(CommandContext context, CommandNode node, String[] args) {
        if (args.length == 0) return null;
        if (context.player == null) return null;
        String arg = args[args.length - 1];
        String lower = arg.toLowerCase();
        if (args.length == 1) {
            File folder = new File(context.player.getWorld().getWorldFolder(), "areas");
            if (!folder.isDirectory()) return List.of();
            List<String> result = new ArrayList<>();
            for (File file : folder.listFiles()) {
                String name = file.getName();
                if (!name.endsWith(".json")) continue;
                name = name.substring(0, name.length() - 5);
                if (name.toLowerCase().contains(lower)) {
                    result.add(name);
                }
            }
            return result;
        }
        if (args.length == 2) {
            AreasFile areasFile = AreasFile.load(context.player.getWorld(), args[0]);
            if (areasFile == null) return List.of();
            return areasFile.areas.keySet().stream()
                .filter(s -> s.toLowerCase().contains(lower))
                .collect(Collectors.toList());
        }
        if (args.length == 3) {
            AreasFile areasFile = AreasFile.load(context.player.getWorld(), args[0]);
            if (areasFile == null) return List.of();
            List<Area> list = areasFile.find(args[1]);
            if (list.isEmpty()) return List.of();
            List<String> result = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i += 1) {
                String term = "" + i;
                if (term.contains(lower)) {
                    result.add(term);
                }
            }
            for (Area area : list) {
                String name = area.getName();
                if (name != null && name.toLowerCase().contains(lower)) {
                    result.add(area.getName());
                }
            }
            return result;
        }
        return List.of();
    }
}
