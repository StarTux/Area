package com.cavetale.area;

import com.cavetale.area.worldedit.CoreSelectionProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AreaPlugin extends JavaPlugin {
    private static AreaPlugin instance;
    private final AreaCommand areaCommand = new AreaCommand(this);
    private CoreSelectionProvider coreSelectionProvider;

    @Override
    public void onLoad() {
        instance = this;
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null) {
            coreSelectionProvider = new CoreSelectionProvider();
            coreSelectionProvider.register();
        }
    }

    @Override
    public void onEnable() {
        areaCommand.enable();
    }

    @Override
    public void onDisable() {
        if (coreSelectionProvider != null) {
            coreSelectionProvider.unregister();
            coreSelectionProvider = null;
        }
    }

    public static AreaPlugin areaPlugin() {
        return instance;
    }
}
