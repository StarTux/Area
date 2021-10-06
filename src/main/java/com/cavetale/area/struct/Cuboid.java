package com.cavetale.area.struct;

import com.cavetale.core.util.Json;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

@Value @RequiredArgsConstructor
public final class Cuboid {
    public static final Cuboid ZERO = new Cuboid(Vec3i.ZERO, Vec3i.ZERO);
    public final Vec3i min;
    public final Vec3i max;
    public final String name;
    public final Map<String, Object> raw;

    public Cuboid(final Vec3i min, final Vec3i max) {
        this(min, max, (String) null, null);
    }

    public Cuboid named(String newName) {
        return new Cuboid(min, max, newName, null);
    }

    public boolean contains(int x, int y, int z) {
        return x >= min.x && x <= max.x
            && y >= min.y && y <= max.y
            && z >= min.z && z <= max.z;
    }

    public boolean contains(Block block) {
        return contains(block.getX(), block.getY(), block.getZ());
    }

    public boolean contains(Location loc) {
        return contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean contains(Vec3i v) {
        return contains(v.x, v.y, v.z);
    }

    @Override
    public String toString() {
        return (name != null ? name + " " : "")
            + (min.equals(max)
               ? "(" + min + ")"
               : "(" + min + "|" + max + ")")
            + (raw != null
               ? Json.serialize(raw)
               : "");
    }

    public int getSizeX() {
        return max.x - min.x + 1;
    }

    public int getSizeY() {
        return max.y - min.y + 1;
    }

    public int getSizeZ() {
        return max.z - min.z + 1;
    }

    public int getVolume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    public Vec3i getCenter() {
        return new Vec3i((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2);
    }

    public List<Vec3i> enumerate() {
        List<Vec3i> result = new ArrayList<>();
        for (int y = min.y; y <= max.y; y += 1) {
            for (int z = min.z; z <= max.z; z += 1) {
                for (int x = min.x; x <= max.x; x += 1) {
                    result.add(new Vec3i(x, y, z));
                }
            }
        }
        return result;
    }

    public Vec3i clamp(Vec3i other) {
        int x = Math.max(other.x, min.x);
        int y = Math.max(other.y, min.y);
        int z = Math.max(other.z, min.z);
        x = Math.min(x, max.x);
        y = Math.min(y, max.y);
        z = Math.min(z, max.z);
        return new Vec3i(x, y, z);
    }

    public void highlight(World world, Player player) {
        if (!world.equals(player.getWorld())) return;
        highlight(world, location -> {
                player.spawnParticle(Particle.END_ROD, location, 1, 0.0, 0.0, 0.0, 0.0);
            });
    }

    public void highlight(World world, Consumer<Location> callback) {
        if (!world.isChunkLoaded(min.x >> 4, min.z >> 4)) return;
        if (!world.isChunkLoaded(max.x >> 4, max.z >> 4)) return;
        Block a = min.toBlock(world);
        Block b = max.toBlock(world);
        final int ax = a.getX();
        final int ay = a.getY();
        final int az = a.getZ();
        final int bx = b.getX();
        final int by = b.getY();
        final int bz = b.getZ();
        Location loc = a.getLocation();
        int sizeX = bx - ax + 1;
        int sizeY = by - ay + 1;
        int sizeZ = bz - az + 1;
        for (int y = 0; y < sizeY; y += 1) {
            double dy = (double) y;
            callback.accept(loc.clone().add(0, dy, 0));
            callback.accept(loc.clone().add(0, dy, sizeZ));
            callback.accept(loc.clone().add(sizeX, dy, 0));
            callback.accept(loc.clone().add(sizeX, dy, sizeZ));
        }
        for (int z = 0; z < sizeZ; z += 1) {
            double dz = (double) z;
            callback.accept(loc.clone().add(0, 0, dz));
            callback.accept(loc.clone().add(0, sizeY, dz));
            callback.accept(loc.clone().add(sizeX, 0, dz));
            callback.accept(loc.clone().add(sizeX, sizeY, dz));
        }
        for (int x = 0; x < sizeX; x += 1) {
            double dx = (double) x;
            callback.accept(loc.clone().add(dx, 0, 0));
            callback.accept(loc.clone().add(dx, 0, sizeZ));
            callback.accept(loc.clone().add(dx, sizeY, 0));
            callback.accept(loc.clone().add(dx, sizeY, sizeZ));
        }
    }
}
