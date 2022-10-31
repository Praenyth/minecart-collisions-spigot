package me.praenyth.plugins.minecartcollisions;

import me.praenyth.plugins.minecartcollisions.events.CollisionListener;
import me.praenyth.plugins.minecartcollisions.events.MinecartKillListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecartCollisions extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new CollisionListener(this), this);
        getServer().getPluginManager().registerEvents(new MinecartKillListener(this), this);
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getServer().getLogger().info("Finished loading the plugin!!! Thanks for downloading my silly little project :)");
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("See ya later bozos");
    }
}
