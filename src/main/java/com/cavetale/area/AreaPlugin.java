package com.cavetale.area;

import com.cavetale.area.worldedit.CoreSelectionProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class AreaPlugin extends JavaPlugin {
    AreaCommand areaCommand = new AreaCommand(this);
    CoreSelectionProvider selectionProvider = new CoreSelectionProvider();

    @Override
    public void onLoad() {
        selectionProvider.register();
    }

    @Override
    public void onEnable() {
        areaCommand.enable();
    }

    @Override
    public void onDisable() {
        selectionProvider.unregister();
    }
}
