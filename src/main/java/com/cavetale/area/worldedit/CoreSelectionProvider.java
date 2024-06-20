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
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            return null;
        }
        final WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        final LocalSession session = we.getSession(player);
        final World world = session.getSelectionWorld();
        final Region region;
        try {
            region = session.getSelection(world);
        } catch (Exception e) {
            return null;
        }
        if (!(region instanceof CuboidRegion cuboidRegion)) {
            return null;
        }
        final BlockVector3 min = cuboidRegion.getMinimumPoint();
        final BlockVector3 max = cuboidRegion.getMaximumPoint();
        return new Cuboid(min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                          max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }
}
