package com.cavetale.area.struct;

import lombok.Value;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

@Value
public final class Vec3i {
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    public static final Vec3i ONE = new Vec3i(1, 1, 1);
    public final int x;
    public final int y;
    public final int z;

    public static Vec3i of(Block block) {
        return new Vec3i(block.getX(), block.getY(), block.getZ());
    }

    public static Vec3i of(Location location) {
        return new Vec3i(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Vec3i of(Vector vector) {
        return new Vec3i(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static Vec3i of(BlockFace blockFace) {
        return new Vec3i(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
    }

    public Block toBlock(World world) {
        return world.getBlockAt(x, y, z);
    }

    public Location toLocation(World world) {
        return toBlock(world).getLocation().add(0.5, 0.0, 0.5);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public BlockFace horizontalBlockFace() {
        if (Math.abs(x) > Math.abs(z)) {
            return x > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return z > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }

    /**
     * Try to convert this vector to a BlockFace.
     */
    public BlockFace toSingleBlockFace() {
        if (x > 0) return BlockFace.EAST;
        if (x < 0) return BlockFace.WEST;
        if (z > 0) return BlockFace.SOUTH;
        if (z < 0) return BlockFace.NORTH;
        if (y > 0) return BlockFace.UP;
        if (y < 0) return BlockFace.DOWN;
        return BlockFace.SELF;
    }

    public Vec3i add(int dx, int dy, int dz) {
        return new Vec3i(x + dx, y + dy, z + dz);
    }

    public Vec3i add(Vec3i other) {
        return new Vec3i(x + other.x, y + other.y, z + other.z);
    }

    public Vec3i subtract(int dx, int dy, int dz) {
        return new Vec3i(x - dx, y - dy, z - dz);
    }

    public Vec3i subtract(Vec3i other) {
        return new Vec3i(x - other.x, y - other.y, z - other.z);
    }

    public int distanceSquared(Vec3i other) {
        int dx = other.x - x;
        int dy = other.y - y;
        int dz = other.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean isSimilar(Block block) {
        return x == block.getX()
            && y == block.getY()
            && z == block.getZ();
    }

    @Override
    public String toString() {
        return "" + x + "," + y + "," + z;
    }

    public Vec3i withY(int newY) {
        return new Vec3i(x, newY, z);
    }

    public int maxDistance(Vec3i other) {
        return Math.max(Math.abs(x - other.x),
                        Math.max(Math.abs(y - other.y),
                                 Math.abs(z - other.z)));
    }
}
