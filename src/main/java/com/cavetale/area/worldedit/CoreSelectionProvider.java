package com.cavetale.area.worldedit;

import com.cavetale.core.selection.SelectionProvider;
import com.cavetale.core.struct.Cuboid;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CoreSelectionProvider implements SelectionProvider {
    @Override
    public Cuboid getCuboidSelection(Player player) {
        WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        LocalSession session = we.getSession(player);
        World world = session.getSelectionWorld();
        final Region region;
        try {
            region = session.getSelection(world);
        } catch (Exception e) {
            return null;
        }
        if (!(region instanceof CuboidRegion cuboidRegion)) return null;
        BlockVector3 min = cuboidRegion.getMinimumPoint();
        BlockVector3 max = cuboidRegion.getMaximumPoint();
        return new Cuboid(min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                          max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }
}
