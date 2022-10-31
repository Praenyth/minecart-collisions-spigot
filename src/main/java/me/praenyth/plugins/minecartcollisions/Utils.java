package me.praenyth.plugins.minecartcollisions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.PoweredRail;
import org.bukkit.material.Rails;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public class Utils {

    // damages the player (if you didn't know)
    public static void damagePlayer(Player p, double damage, Entity source) {
        double points = p.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double toughness = p.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        PotionEffect effect = p.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        int resistance = effect == null ? 0 : effect.getAmplifier();
        int epf = getEPF(p.getInventory());

        p.damage(calculateDamageApplied(damage, points, toughness, resistance, epf), source);
    }

    private static double calculateDamageApplied(double damage, double points, double toughness, int resistance, int epf) {
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

    // checks if the minecart is a valid cart
    public static RideableMinecart getValidMinecart(Vehicle vehicle, boolean mustHavePassenger)
    {
        RideableMinecart cart = null;

        if (!(vehicle instanceof RideableMinecart))
            return null;

        cart = (RideableMinecart) vehicle;

        Entity firstPassenger = GetFirstPassenger(cart);
        if (firstPassenger == null)
            return null;

        if (mustHavePassenger && (cart.isEmpty() || !(firstPassenger instanceof Player)))
            return null;

        return cart;
    }

    // get the passengers of a minecart
    public static Entity getFirstPassenger(Minecart toCart)
    {
        List<Entity> passengers = toCart.getPassengers();

        if (passengers.isEmpty())
            return null;

        return passengers.get(0);
    }

    // checks if the rail is flat
    public static boolean isFlatRail(Location location)
    {
        if (location.getBlock().getType() == Material.RAIL)
        {
            Rails testRail = (Rails) location.getBlock().getState().getData();
            if (!testRail.isOnSlope())
            {
                return true;
            }
        }
        return false;
    }

    // checks if a rail is perpendicular to another
    public static boolean isRailPerpendicular(Location myLocation, Location otherLocation)
    {
        Block myBlock = myLocation.getBlock();
        Block otherBlock = otherLocation.getBlock();
        if (otherBlock.getType() == Material.RAIL)
        {
            if (myBlock.getData() == (byte) 0 && otherBlock.getData() == (byte) 1)
            {
                return true;
            } else if (myBlock.getData() == (byte) 1 && otherBlock.getData() == (byte) 0)
            {
                return true;
            }
        }
        return false;
    }

    // checks if a rail is parallel to another
    public static boolean isRailParallel(Location myLocation, Location otherLocation)
    {
        Block myBlock = myLocation.getBlock();
        Block otherBlock = otherLocation.getBlock();
        if (otherBlock.getType() == Material.RAIL)
        {
            if (myBlock.getData() == otherBlock.getData())
            {
                return true;
            }
        }
        return false;
    }

    // get vector from blockface
    public static Vector getUnitVectorFromYaw(float yaw)
    {
        BlockFace facing = getBlockFaceFromYaw(yaw);
        switch (facing)
        {
            case SOUTH:
                return new Vector(0, 0, 1);
            case WEST:
                return new Vector(-1, 0, 0);
            case NORTH:
                return new Vector(0, 0, -1);
            default: // EAST
                return new Vector(1, 0, 0);
        }
    }

    // checks if the area is an intersection
    public static boolean isIntersection(Location myLocation, Vector movementDirection)
    {
        if (Utils.isFlatRail(myLocation))
        {
            // Search for intersection
            Location front = myLocation.clone().add(movementDirection.normalize());
            Location back = myLocation.clone().subtract(movementDirection.normalize());
            Location left = myLocation.clone().add(movementDirection.getZ(), 0, -movementDirection.getX()); // go one left
            Location right = myLocation.clone().add(-movementDirection.getZ(), 0, movementDirection.getX()); // go one right

            if (Utils.isRailPerpendicular(myLocation, left) && Utils.isRailPerpendicular(myLocation, right))
            {
                return true;
            } else if ((Utils.isRailPerpendicular(myLocation, left)
                    && (Utils.isRailParallel(myLocation, front) || Utils.isRailParallel(myLocation, back)))
                    || (Utils.isRailPerpendicular(myLocation, right)
                    && (Utils.isRailParallel(myLocation, front) || Utils.isRailParallel(myLocation, back))))
            {
                return true;
            } else if ((Utils.isRailParallel(myLocation, left)
                    && (Utils.isRailPerpendicular(myLocation, front) || Utils.isRailPerpendicular(myLocation, back)))
                    || (Utils.isRailParallel(myLocation, right)
                    && (Utils.isRailPerpendicular(myLocation, front) || Utils.isRailPerpendicular(myLocation, back))))
            {
                return true;
            }
        }
        return false;
    }
    
    public static BlockFace getBlockFaceFromYaw(float yaw)
    {

        if (yaw < 0)
        { // Map all negative values to positives. E.g. -45 degree = +315 degree
            yaw = yaw + 360;
        }
        yaw = yaw % 360; // crop value, e.g. if it's 460 degree --> 100 degree

        float straightAngle = 90;

        if ((yaw >= 0) && (yaw < (straightAngle / 2)) || (yaw >= (360 - (straightAngle / 2))))
        {
            return BlockFace.SOUTH;
        } else if ((yaw >= (straightAngle / 2)) && (yaw < 135))
        {
            return BlockFace.WEST;
        } else if ((yaw >= 135) && (yaw < (360 - (straightAngle * 1.5))))
        {
            return BlockFace.NORTH;
        } else
        {
            return BlockFace.EAST;
        }
    }

    public static boolean isMovingUp(VehicleMoveEvent event)
    {
        return event.getTo().getY() - event.getFrom().getY() > 0;
    }

    public static boolean isMovingDown(VehicleMoveEvent event)
    {
        return event.getTo().getY() - event.getFrom().getY() < 0;
    }

    public static Rails getRailInFront(Location testLoc)
    {
        try
        {
            // Slopes that go down/fall have the blocks underneath the current y-level
            Location testLocUnder = testLoc.clone().subtract(0, 1, 0);

            if (testLoc.getBlock().getType() == Material.RAIL)
            {
                // Detects rising slope
                return (Rails) testLoc.getBlock().getState().getData();
            } else if (testLocUnder.getBlock().getType() == Material.RAIL)
            {
                // Detects falling slope
                return (Rails) testLocUnder.getBlock().getState().getData();
            } else if (testLoc.getBlock().getType() == Material.POWERED_RAIL)
            {
                return (PoweredRail) testLoc.getBlock().getState().getData();
            } else if (testLocUnder.getBlock().getType() == Material.POWERED_RAIL)
            {
                return (PoweredRail) testLocUnder.getBlock().getState().getData();
            }
        } catch (ClassCastException e)
        {
            // no valid rail found
        }
        return null;
    }
}
