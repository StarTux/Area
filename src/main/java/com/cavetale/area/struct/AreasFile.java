package com.cavetale.area.struct;

import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.util.Json;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.World;

@Getter
public final class AreasFile {
    public final Map<String, List<Area>> areas = new HashMap<>();
    private transient String worldName;
    private transient String fileName;

    public static AreasFile load(World world, String fileName) {
        File folder = new File(world.getWorldFolder(), "areas");
        if (!folder.isDirectory()) return null;
        File file = new File(folder, fileName + ".json");
        if (!file.isFile()) return null;
        AreasFile areasFile = Json.load(file, AreasFile.class, () -> null);
        areasFile.worldName = world.getName();
        areasFile.fileName = fileName;
        return areasFile;
    }

    public static AreasFile require(World world, String fileName) {
        AreasFile result = load(world, fileName);
        if (result == null) {
            throw new CommandWarn("Areas file not found: " + world.getName() + "/" + fileName);
        }
        return result;
    }

    public static AreasFile requireSingular(World world) {
        File folder = new File(world.getWorldFolder(), "areas");
        if (!folder.isDirectory()) {
            throw new CommandWarn("There is no areas folder: " + world.getName());
        }
        File[] files = folder.listFiles(f -> f.isFile() && f.getName().endsWith(".json"));
        if (files.length != 1) {
            if (!folder.isDirectory()) throw new CommandWarn("There is no single areas file: " + world.getName());
        }
        File file = files[0];
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 5);
        if (!file.isFile()) return null;
        AreasFile result = Json.load(file, AreasFile.class, () -> null);
        if (result == null) {
            throw new CommandWarn("Failed to load single areas file: " + world.getName() + "/" + fileName);
        }
        result.worldName = world.getName();
        result.fileName = fileName;
        return result;
    }

    public void save(World world, String theFileName) {
        File folder = new File(world.getWorldFolder(), "areas");
        folder.mkdirs();
        File file = new File(folder, theFileName + ".json");
        Json.save(file, this, true);
    }

    public List<Area> all() {
        List<Area> result = new ArrayList<>();
        for (List<Area> it : areas.values()) {
            result.addAll(it);
        }
        return result;
    }

    public List<Area> find(String name) {
        List<Area> result = areas.get(name);
        return result != null ? result : List.of();
    }

    public List<Area> find(String name, String subname) {
        List<Area> list = areas.get(name);
        if (list == null) return List.of();
        List<Area> result = new ArrayList<>(list.size());
        for (Area cuboid : list) {
            if (Objects.equals(subname, cuboid.name)) {
                result.add(cuboid);
            }
        }
        return result;
    }

    public String getPath() {
        return worldName + "/" + fileName;
    }
}
