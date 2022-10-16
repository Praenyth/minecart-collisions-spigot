package me.praenyth.plugins.minecartcollisions.events;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class CollisionListener implements Listener {


    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        Vehicle vehicle = e.getVehicle();
        RideableMinecart cart = null;
        if (vehicle instanceof RideableMinecart) {
            cart = (RideableMinecart) vehicle;
            cart.setMaxSpeed(20f);
        }
        for (Entity entity : vehicle.getNearbyEntities(1, 1,1)) {
            if (!vehicle.getPassengers().contains(entity)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity hitEntity = (LivingEntity) entity;
                    if (cart != null) {
                        if (cart.getVelocity().isInAABB(hitEntity.getVelocity(), new Vector(5, 5 ,5))) {

                            if (hitEntity instanceof Player) {
                                damagePlayer(((Player)hitEntity), 12);
                            } else {
                                hitEntity.damage(12);
                            }

                            hitEntity.setVelocity(vehicle.getVelocity().subtract(hitEntity.getVelocity().normalize()));
                        }
                    }
                }
            }
        }
    }

    public void damagePlayer(Player p, double damage) {
        double points = p.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double toughness = p.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        PotionEffect effect = p.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int epf = getEPF(p.getInventory());

        p.damage(calculateDamageApplied(damage, points, toughness, resistance, epf));
    }

    public double calculateDamageApplied(double damage, double points, double toughness, int resistance, int epf) {
        double withArmorAndToughness = damage * (1 - Math.min(20, Math.max(points / 5, points - damage / (2 + toughness / 4))) / 25);
        double withResistance = withArmorAndToughness * (1 - (resistance * 0.2));
        double withEnchants = withResistance * (1 - (Math.min(20.0, epf) / 25));
        return withEnchants;
    }

    public static int getEPF(PlayerInventory inv) {
        ItemStack helm = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boot = inv.getBoots();

        return (helm != null ? helm.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (chest != null ? chest.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (legs != null ? legs.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0) +
                (boot != null ? boot.getEnchantmentLevel(Enchantment.DAMAGE_ALL) : 0);
    }
}
