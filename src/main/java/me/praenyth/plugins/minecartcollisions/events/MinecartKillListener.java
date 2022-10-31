package me.praenyth.plugins.minecartcollisions.events;

import me.praenyth.plugins.minecartcollisions.MinecartCollisions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Map;

public class MinecartKillListener implements Listener {

    private final MinecartCollisions plugin;

    public MinecartKillListener(MinecartCollisions collisions) {
        this.plugin = collisions;
    }

    @EventHandler
    public void onMinecartKill(PlayerDeathEvent event) {
        Map<Player, Minecart> recentlyHitPlayers = CollisionListener.recentlyHitPlayers;
        Player player = event.getEntity();
        FileConfiguration config = plugin.getConfig();
        if (recentlyHitPlayers.containsKey(player)) {
            List<Entity> passengers = recentlyHitPlayers.get(player).getPassengers();
            if (recentlyHitPlayers.get(player).getPassengers().isEmpty()) {
                event.setDeathMessage(player.getDisplayName() + " " + config.getString("minecart-death-messages.minecart-no-passenger", "was ran over by a minecart"));
                recentlyHitPlayers.remove(player);
            } else {
                if (passengers.get(0).getType().equals(EntityType.PLAYER)) {
                    event.setDeathMessage(player.getDisplayName() + " " + config.getString("minecart-death-messages.minecart-with-passenger", "was ran over by a minecart from") + " " + ((Player)passengers.get(0)).getDisplayName());
                    recentlyHitPlayers.remove(player);
                } else {
                    event.setDeathMessage(player.getDisplayName() + " " + config.getString("minecart-death-messages.minecart-no-passenger", "was ran over by a minecart"));
                    recentlyHitPlayers.remove(player);
                }
            }
        }
    }

}
