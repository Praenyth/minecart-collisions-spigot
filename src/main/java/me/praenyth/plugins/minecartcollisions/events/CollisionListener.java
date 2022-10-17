package me.praenyth.plugins.minecartcollisions.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;

public class CollisionListener implements Listener {

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        Vehicle vehicle = e.getVehicle();

        Minecart cart = null;

        if (vehicle instanceof Minecart) {
            cart = (RideableMinecart) vehicle;
            float tempSpeed = 20f;
            if (cart.getPassengers().size() >= 1) {
                if (vehicle.getPassengers().get(0) instanceof Player) {
                    Player riding = (Player) vehicle.getPassengers().get(0);
                    if (riding.getInventory().getItemInOffHand().getType().equals(Material.FEATHER) || riding.getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
                        tempSpeed = 100f;
                    } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.PUFFERFISH_BUCKET) || riding.getInventory().getItemInMainHand().getType().equals(Material.PUFFERFISH_BUCKET)) {
                        tempSpeed = 1f;
                    }
                }
            }
            cart.setMaxSpeed(tempSpeed);
        }

        if (cart != null) {
            for (Entity entity : cart.getNearbyEntities(2, -1,2)) {
                if (!cart.getPassengers().contains(entity)) {
                    if (entity instanceof LivingEntity) {

                        LivingEntity hitEntity = (LivingEntity) entity;

                        if (cart.getVelocity().length() > 2) {

                            if (cart.getPassengers().size() >= 1) {
                                if (hitEntity instanceof Player) {
                                    int tempDamage = 12;
                                    if (!((Player)hitEntity).isFlying()) {
                                        hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()));
                                        if (vehicle.getPassengers().get(0) instanceof Player) {
                                            Player riding = (Player) vehicle.getPassengers().get(0);
                                            if (riding.getInventory().getItemInOffHand().getType().equals(Material.TNT) || riding.getInventory().getItemInMainHand().getType().equals(Material.TNT)) {
                                                riding.getWorld().createExplosion(riding.getLocation(), 3f);
                                            } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.PUFFERFISH_BUCKET) || riding.getInventory().getItemInMainHand().getType().equals(Material.PUFFERFISH_BUCKET)) {
                                                tempDamage = 24;
                                            } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.FEATHER) || riding.getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
                                                tempDamage = 0;
                                            }
                                        }
                                    }
                                    damagePlayer(((Player)hitEntity), tempDamage, cart);

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);

                                } else {
                                    int tempDamage = 12;
                                    Vector tempVelocity = cart.getVelocity().subtract(hitEntity.getVelocity().normalize());
                                    if (vehicle.getPassengers().get(0) instanceof Player) {
                                        Player riding = (Player) vehicle.getPassengers().get(0);
                                        if (riding.getInventory().getItemInOffHand().getType().equals(Material.TNT) || riding.getInventory().getItemInMainHand().getType().equals(Material.TNT)) {
                                            riding.getWorld().createExplosion(riding.getLocation(), 3f);
                                        } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.PUFFERFISH_BUCKET) || riding.getInventory().getItemInMainHand().getType().equals(Material.PUFFERFISH_BUCKET)) {
                                            tempDamage = 24;
                                        } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.FEATHER) || riding.getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
                                            tempDamage = 0;
                                            tempVelocity = cart.getVelocity().setY(cart.getVelocity().getY()+1);
                                        }
                                    }
                                    hitEntity.damage(tempDamage);
                                    hitEntity.setVelocity(tempVelocity);

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                }
                            } else {
                                if (hitEntity instanceof Player) {
                                    if (!((Player)hitEntity).isFlying()) {
                                        hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()));
                                    }
                                    damagePlayer(((Player)hitEntity), 6, cart);

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                } else {
                                    hitEntity.damage(6);
                                    hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()));

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private void damagePlayer(Player p, double damage, Entity source) {
        double points = p.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double toughness = p.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        PotionEffect effect = p.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int epf = getEPF(p.getInventory());

        p.damage(calculateDamageApplied(damage, points, toughness, resistance, epf), source);
    }

    private double calculateDamageApplied(double damage, double points, double toughness, int resistance, int epf) {
        double withArmorAndToughness = damage * (1 - Math.min(20, Math.max(points / 5, points - damage / (2 + toughness / 4))) / 25);
        double withResistance = withArmorAndToughness * (1 - (resistance * 0.2));
        double withEnchants = withResistance * (1 - (Math.min(20.0, epf) / 25));
        return withEnchants;
    }

    private static int getEPF(PlayerInventory inv) {
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
