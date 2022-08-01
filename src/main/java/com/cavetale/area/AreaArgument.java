package com.cavetale.area;

import com.cavetale.area.struct.Area;
import com.cavetale.area.struct.AreasFile;
import com.cavetale.core.command.CommandWarn;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

@Data
public final class AreaArgument {
    private String worldName;
    private String fileArg;
    private String nameArg;
    private String indexArg;
    private int index = -1;

    private AreasFile areasFile;
    private List<Area> areaList;
    private List<Area> areas;

    public static AreaArgument of(Player player, String[] args) {
        if (args.length == 0 || args.length > 3) return null;
        AreaArgument result = new AreaArgument();
        result.parse(player, args);
        return result;
    }

    private void parse(Player player, String[] args) {
        World world = player.getWorld();
        this.worldName = world.getName();
        this.fileArg = args.length >= 1 ? args[0] : null;
        this.nameArg = args.length >= 2 ? args[1] : null;
        this.indexArg = args.length >= 3 ? args[2] : null;
        // File
        if (fileArg == null) return;
        this.areasFile = AreasFile.load(world, fileArg);
        if (areasFile == null) return;
        // List
        if (nameArg == null) return;
        areaList = areasFile.areas.get(nameArg);
        if (areaList == null) return;
        // Index
        if (indexArg == null) return;
        try {
            this.index = Integer.parseInt(indexArg);
        } catch (NumberFormatException nfe) {
            this.index = -1;
        }
        if (index >= 0) {
            this.areas = index < areaList.size()
                ? List.of(areaList.get(index))
                : null;
        } else {
            this.areas = new ArrayList<>();
            for (Area it : areaList) {
                if (indexArg.equals(it.getName())) {
                    areas.add(it);
                }
            }
        }
    }

    public String getPath() {
        return worldName
            + (fileArg != null ? ("/" + fileArg) : "")
            + (nameArg != null ? ("." + nameArg) : "")
            + (indexArg != null ? ("[" + indexArg + "]") : "");
    }

    public boolean hasAreasFileArg() {
        return fileArg != null;
    }

    public boolean hasAreasFile() {
        return areasFile != null;
    }

    public AreasFile requireAreasFile() {
        if (areasFile == null) {
            throw new CommandWarn("Areas file not found: " + getPath());
        }
        return areasFile;
    }

    public boolean hasAreaListArg() {
        return nameArg != null;
    }

    public boolean hasAreaList() {
        return areaList != null;
    }

    public List<Area> requireAreaList() {
        requireAreasFile();
        if (areaList == null) {
            throw new CommandWarn("Area list not found: " + getPath());
        }
        return areaList;
    }

    public boolean hasIndexArg() {
        return indexArg != null;
    }

    public boolean hasAreas() {
        return areas != null && !areas.isEmpty();
    }

    public List<Area> requireAreas() {
        requireAreaList();
        if (areas == null || areas.isEmpty()) {
            throw new CommandWarn("Areas not found: " + getPath());
        }
        return areas;
    }

    public boolean hasSingleArea() {
        return areas != null && areas.size() == 1;
    }

    public Area requireSingleArea() {
        List<Area> list = requireAreas();
        if (list.size() != 1) {
            throw new CommandWarn("Area not unique: " + getPath());
        }
        return list.get(0);
    }

    public int requireNumberIndex() {
        if (index < 0) {
            throw new CommandWarn("Index expected: " + indexArg);
        }
        return index;
    }

    public List<Area> requireAnyAreas() {
        if (hasIndexArg()) {
            return requireAreas();
        } else if (hasAreaListArg()) {
            return requireAreaList();
        } else if (hasAreasFileArg()) {
            return requireAreasFile().all();
        } else {
            throw new CommandWarn("Not found: " + getPath());
        }
    }

    public void save() {
        requireAreasFile().save(Bukkit.getWorld(worldName), fileArg);
    }
}
