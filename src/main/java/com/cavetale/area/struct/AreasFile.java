package com.cavetale.area.struct;

import com.cavetale.core.util.Json;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.World;

public final class AreasFile {
    public final Map<String, List<Cuboid>> areas = new HashMap<>();

    public static AreasFile load(World world, String fileName) {
        File folder = new File(world.getWorldFolder(), "areas");
        if (!folder.isDirectory()) return null;
        File file = new File(folder, fileName + ".json");
        if (!file.isFile()) return null;
        AreasFile areasFile = Json.load(file, AreasFile.class, () -> null);
        return areasFile;
    }

    public void save(World world, String fileName) {
        File folder = new File(world.getWorldFolder(), "areas");
        folder.mkdirs();
        File file = new File(folder, fileName + ".json");
        Json.save(file, this, true);
    }

    public List<Cuboid> all() {
        List<Cuboid> result = new ArrayList<>();
        for (List<Cuboid> it : areas.values()) {
            result.addAll(it);
        }
        return result;
    }

    public List<Cuboid> find(String name) {
        List<Cuboid> result = areas.get(name);
        return result != null ? result : List.of();
    }

    public List<Cuboid> find(String name, String subname) {
        List<Cuboid> list = areas.get(name);
        if (list == null) return List.of();
        List<Cuboid> result = new ArrayList<>(list.size());
        for (Cuboid cuboid : list) {
            if (Objects.equals(subname, cuboid.name)) {
                result.add(cuboid);
            }
        }
        return result;
    }
}
