package me.praenyth.plugins.minecartcollisions.events;

import me.praenyth.plugins.minecartcollisions.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

public class CollisionListener implements Listener {

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {

        float MAX_SPEED = 20f;
        float MAX_SPEED_INTERSECTION = 1f;

        Minecart cart = null;

        if (e.getVehicle() instanceof Minecart) {
            cart = (Minecart) e.getVehicle();

            // set max speed of the minecart depending on item
            float tempSpeed = MAX_SPEED;
            if (cart.getPassengers().size() >= 1) {
                if (cart.getPassengers().get(0) instanceof Player) {

                    Player riding = (Player) cart.getPassengers().get(0);
                    Material mainHandItemType = riding.getInventory().getItemInMainHand().getType();
                    Material offHandItemType = riding.getInventory().getItemInOffHand().getType();

                    if (offHandItemType.equals(Material.FEATHER) || mainHandItemType.equals(Material.FEATHER)) {
                        tempSpeed = 100f;
                    } else if (offHandItemType.equals(Material.PUFFERFISH_BUCKET) || mainHandItemType.equals(Material.PUFFERFISH_BUCKET)) {
                        tempSpeed = 1f;
                    }
                }
            }

            cart.setMaxSpeed(tempSpeed);
        }

        // minecart movement
        if (cart != null) {
            Location cartLocation = cart.getLocation();
            Vector cartVelocity = cart.getVelocity();

            Location locationInFront = cartLocation.clone();
            Vector cartDirection = cartVelocity.clone().normalize();
            Rails railUnderCart;
            try
            {
                railUnderCart = (Rails) cart.getLocation().getBlock().getState().getData();
            } catch (ClassCastException exception)
            {
                return;
            }
            for (int i = 1; i < 3; i++) // checks 3 blocks ahead
            {
                locationInFront.add(cartDirection.multiply(i));
                Rails railInFront = Utils.getRailInFront(locationInFront);

                if (railInFront != null)
                {

                    if (railInFront.isCurve() || railInFront.isOnSlope())
                    {
                        if (railUnderCart.isOnSlope() && Utils.isMovingDown(e))
                        {
                            // Don't do anything if we are on a downward slope
                            return;
                        } else
                        {
                            cart.setMaxSpeed(MAX_SPEED_INTERSECTION);
                            return;
                        }
                    } else if ((cartVelocity.length() > MAX_SPEED_INTERSECTION)
                            && Utils.isIntersection(locationInFront, cartDirection))
                    {
                        cart.setMaxSpeed(MAX_SPEED_INTERSECTION);
                        return;
                    }
                }
            }
        }



        // minecart damage mechanic
        if (cart != null) {
            String hitSound = "prae.danielsmp.minecart";
            Vector cartVelocity = cart.getVelocity();
            Location cartLocation = cart.getLocation();

            for (Entity entity : cart.getNearbyEntities(1.5, 1,1.5)) {
                if (!cart.getPassengers().contains(entity)) {
                    if (entity instanceof LivingEntity) {

                        LivingEntity hitEntity = (LivingEntity) entity;
                        Vector hitEntityVelocity = hitEntity.getVelocity();
                        Location hitEntityLocation = hitEntity.getLocation();

                        if (cart.getVelocity().length() > 1) {
                            if (cart.getPassengers().size() >= 1) {
                                if (hitEntity instanceof Player) {

                                    Player hitPlayer = (Player) hitEntity;

                                    int tempDamage = 12;
                                    if (!hitPlayer.isFlying()) {
                                        hitPlayer.setVelocity(cartVelocity.subtract(hitEntityVelocity.normalize()).setY(cartVelocity.length()));
                                        if (cart.getPassengers().get(0) instanceof Player) {

                                            Player riding = (Player) cart.getPassengers().get(0);
                                            Material offHandItemType = riding.getInventory().getItemInOffHand().getType();
                                            Material mainHandItemType = riding.getInventory().getItemInMainHand().getType();

                                            if (offHandItemType.equals(Material.TNT) || mainHandItemType.equals(Material.TNT)) {
                                                riding.getWorld().createExplosion(riding.getLocation(), 3f);
                                            } else if (offHandItemType.equals(Material.PUFFERFISH_BUCKET) || mainHandItemType.equals(Material.PUFFERFISH_BUCKET)) {
                                                tempDamage = 24;
                                            } else if (offHandItemType.equals(Material.FEATHER) || mainHandItemType.equals(Material.FEATHER)) {
                                                tempDamage = 0;
                                            } else if (offHandItemType.equals(Material.ANVIL) || mainHandItemType.equals(Material.ANVIL)) {
                                                tempDamage = 2000;
                                            }
                                        }
                                    }
                                    Utils.damagePlayer(((Player)hitEntity), tempDamage, cart);

                                    hitEntity.getWorld().playSound(hitEntityLocation, hitSound, 1f, 1f);

                                } else {
                                    if (!(hitEntity instanceof Item)) {
                                        int tempDamage = 12;
                                        Vector tempVelocity = cartVelocity.subtract(hitEntityVelocity.normalize());
                                        if (cart.getPassengers().get(0) instanceof Player) {

                                            Player riding = (Player) cart.getPassengers().get(0);
                                            Material offHandItemType = riding.getInventory().getItemInOffHand().getType();
                                            Material mainHandItemType = riding.getInventory().getItemInMainHand().getType();

                                            if (offHandItemType.equals(Material.TNT) || mainHandItemType.equals(Material.TNT)) {
                                                riding.getWorld().createExplosion(riding.getLocation(), 3f);
                                            } else if (offHandItemType.equals(Material.PUFFERFISH_BUCKET) || mainHandItemType.equals(Material.PUFFERFISH_BUCKET)) {
                                                tempDamage = 24;
                                            } else if (offHandItemType.equals(Material.FEATHER) || mainHandItemType.equals(Material.FEATHER)) {
                                                tempDamage = 0;
                                                tempVelocity = cart.getVelocity().setY(cart.getVelocity().length());
                                            } else if (offHandItemType.equals(Material.ANVIL) || mainHandItemType.equals(Material.ANVIL)) {
                                                tempDamage = 2000;
                                            }
                                        }
                                        hitEntity.damage(tempDamage);
                                        hitEntity.setVelocity(tempVelocity);

                                        hitEntity.getWorld().playSound(hitEntityLocation, hitSound, 1f, 1f);
                                    }
                                }
                            } else {
                                if (hitEntity instanceof Player) {

                                    Player hitPlayer = (Player) hitEntity;

                                    if (!hitPlayer.isFlying()) {
                                        hitPlayer.setVelocity(cartVelocity.subtract(hitEntityVelocity.normalize()).setY(cartVelocity.length()));
                                    }
                                    Utils.damagePlayer(hitPlayer, 6, cart);

                                    hitPlayer.getWorld().playSound(hitEntityLocation, hitSound, 1f, 1f);
                                } else {
                                    if (!(hitEntity instanceof Item)) {
                                        hitEntity.damage(6);
                                        hitEntity.setVelocity(cartVelocity.subtract(hitEntityVelocity.normalize()).setY(cartVelocity.length()));

                                        hitEntity.getWorld().playSound(hitEntityLocation, hitSound, 1f, 1f);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
