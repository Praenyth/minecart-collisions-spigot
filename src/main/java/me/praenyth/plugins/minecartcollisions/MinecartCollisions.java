package me.praenyth.plugins.minecartcollisions;

import me.praenyth.plugins.minecartcollisions.events.CollisionListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecartCollisions extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new CollisionListener(), this);

        getServer().getLogger().info("ok plugin done loading now wowza");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
