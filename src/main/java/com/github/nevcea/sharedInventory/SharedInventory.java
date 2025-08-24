package com.github.nevcea.sharedInventory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SharedInventory extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
    }
}
