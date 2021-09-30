package com.cavetale.area;

import org.bukkit.plugin.java.JavaPlugin;

public final class AreaPlugin extends JavaPlugin {
    AreaCommand areaCommand = new AreaCommand(this);

    @Override
    public void onEnable() {
        areaCommand.enable();
    }

    @Override
    public void onDisable() {
    }
}
