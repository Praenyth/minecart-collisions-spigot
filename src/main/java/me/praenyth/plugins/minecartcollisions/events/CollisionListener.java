package me.praenyth.plugins.minecartcollisions.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Rail;
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
            cart = (Minecart) vehicle;

            // set max speed of the minecart depending on item
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

            // set velocity depending on rail direction
            if (cart.getLocation().getBlock().getType().equals(Material.RAIL)) {
                Block rail = cart.getLocation().getBlock();
                if (rail.getBlockData() instanceof Rail) {
                    Rail rail2 = (Rail) rail.getBlockData();
                    switch (rail2.getShape()) {
                        case ASCENDING_SOUTH:
                            cart.setVelocity(new Vector(0.0, 0.0, cart.getVelocity().getZ()*-1));
                            cart.setFlyingVelocityMod(new Vector(0.0, 0.0, cart.getVelocity().getZ()*-1));
                            break;
                        case ASCENDING_NORTH:
                            cart.setVelocity(new Vector(0.0, 0.0, cart.getVelocity().getZ()));
                            cart.setFlyingVelocityMod(new Vector(0.0, 0.0, cart.getVelocity().getZ()));
                            break;
                        case ASCENDING_WEST:
                            cart.setVelocity(new Vector(cart.getVelocity().getX(), 0.0, 0.0));
                            cart.setFlyingVelocityMod(new Vector(cart.getVelocity().getX(), 0.0, 0.0));
                            break;
                        case ASCENDING_EAST:
                            cart.setVelocity(new Vector(cart.getVelocity().getX()*-1, 0.0, 0.0));
                            cart.setFlyingVelocityMod(new Vector(cart.getVelocity().getX()*-1, 0.0, 0.0));
                    }
                }
            }

            cart.setMaxSpeed(tempSpeed);
        }

        // minecart damage mechanic
        if (cart != null) {
            for (Entity entity : cart.getNearbyEntities(1.5, -1,1.5)) {
                if (!cart.getPassengers().contains(entity)) {
                    if (entity instanceof LivingEntity) {

                        LivingEntity hitEntity = (LivingEntity) entity;

                        if (cart.getVelocity().length() > 1) {

                            if (cart.getPassengers().size() >= 1) {
                                if (hitEntity instanceof Player) {
                                    int tempDamage = 12;
                                    if (!((Player)hitEntity).isFlying()) {
                                        hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()).setY(1));
                                        if (vehicle.getPassengers().get(0) instanceof Player) {
                                            Player riding = (Player) vehicle.getPassengers().get(0);
                                            if (riding.getInventory().getItemInOffHand().getType().equals(Material.TNT) || riding.getInventory().getItemInMainHand().getType().equals(Material.TNT)) {
                                                riding.getWorld().createExplosion(riding.getLocation(), 3f);
                                            } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.PUFFERFISH_BUCKET) || riding.getInventory().getItemInMainHand().getType().equals(Material.PUFFERFISH_BUCKET)) {
                                                tempDamage = 24;
                                            } else if (riding.getInventory().getItemInOffHand().getType().equals(Material.FEATHER) || riding.getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
                                                tempDamage = 0;
                                            } else if (riding.getInventory().getItemInMainHand().getType().equals(Material.ANVIL) || riding.getInventory().getItemInOffHand().getType().equals(Material.ANVIL)) {
                                                tempDamage = 2000;
                                            }
                                        }
                                    }
                                    damagePlayer(((Player)hitEntity), tempDamage, cart);

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);

                                } else {
                                    if (!(hitEntity instanceof Item)) {
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
                                            } else if (riding.getInventory().getItemInMainHand().getType().equals(Material.ANVIL) || riding.getInventory().getItemInOffHand().getType().equals(Material.ANVIL)) {
                                                tempDamage = 2000;
                                            }
                                        }
                                        hitEntity.damage(tempDamage);
                                        hitEntity.setVelocity(tempVelocity);

                                        hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                    }
                                }
                            } else {
                                if (hitEntity instanceof Player) {
                                    if (!((Player)hitEntity).isFlying()) {
                                        hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()).setY(1));
                                    }
                                    damagePlayer(((Player)hitEntity), 6, cart);

                                    hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                } else {
                                    if (!(hitEntity instanceof Item)) {
                                        hitEntity.damage(6);
                                        hitEntity.setVelocity(cart.getVelocity().subtract(hitEntity.getVelocity().normalize()).setY(1));

                                        hitEntity.getWorld().playSound(hitEntity.getLocation(), "prae.danielsmp.minecart", 1f, 1f);
                                    }
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
