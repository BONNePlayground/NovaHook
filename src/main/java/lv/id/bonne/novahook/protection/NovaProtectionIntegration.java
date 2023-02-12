//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.novahook.protection;


import com.google.common.base.Enums;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import lv.id.bonne.novahook.NovaHookAddon;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.admin.AdminSwitchCommand;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.metadata.MetaDataValue;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.util.Util;
import xyz.xenondevs.nova.api.Nova;
import xyz.xenondevs.nova.api.protection.ProtectionIntegration;
import xyz.xenondevs.nova.api.tileentity.TileEntity;


/**
 * This class implements all protections that are provided by Nova Plugin.
 */
public class NovaProtectionIntegration implements ProtectionIntegration
{
    /**
     * The protection handler executioner.
     * @return None... we do not have any special requirements.
     */
    @NotNull
    @Override
    public ExecutionMode getExecutionMode()
    {
        return ExecutionMode.NONE;
    }


// ---------------------------------------------------------------------
// Section: Player Protection Checks
// ---------------------------------------------------------------------


    /**
     * Checks if that [player] can break a block at that [location] using that [item]
     */
    @Override
    public boolean canBreak(@NotNull OfflinePlayer offlinePlayer,
        @Nullable ItemStack itemStack,
        @NotNull Location location)
    {
        // Protection should be handled by BentoBox core
        return true;
    }


    /**
     * Checks if the [player] can place that [item] at that [location]
     */
    @Override
    public boolean canPlace(@NotNull OfflinePlayer offlinePlayer,
        @NotNull ItemStack itemStack,
        @NotNull Location location)
    {
        // Protection should be handled by BentoBox core
        return true;
    }


    /**
     * Checks if the [player] can hurt the [entity] with this [item]
     */
    @Override
    public boolean canHurtEntity(@NotNull OfflinePlayer offlinePlayer,
        @NotNull Entity entity,
        @Nullable ItemStack itemStack)
    {
        // Protection should be handled by BentoBox core
        return true;
    }


    /**
     * Checks if the [player] can interact with the [entity] while holding [item]
     */
    @Override
    public boolean canInteractWithEntity(@NotNull OfflinePlayer offlinePlayer,
        @NotNull Entity entity,
        @Nullable ItemStack itemStack)
    {
        // Protection should be handled by BentoBox core.
        return true;
    }


    /**
     * Checks if the [player] can interact with a block at that [location] using that [item]
     */
    @Override
    public boolean canUseBlock(@NotNull OfflinePlayer offlinePlayer,
        @Nullable ItemStack itemStack,
        @NotNull Location location)
    {
        boolean returnValue;

        if (Nova.getNova().getBlockManager().hasBlock(location))
        {
            // If the block is a Nova TileEntity, then use interaction protection.
            returnValue = this.checkProtection(offlinePlayer, location, NovaHookAddon.NOVA_INTERACTION_PROTECTION, false);
        }
        else if (itemStack != null && Nova.getNova().getMaterialRegistry().getOrNull(itemStack) != null)
        {
            // Use block break flag, because it would be too tricky to detect every possible correct usage.
            returnValue = this.checkProtection(offlinePlayer, location, NovaHookAddon.NOVA_USE_PROTECTION, false);
        }
        else
        {
            // Return true by default.
            returnValue = true;
        }

        return returnValue;
    }


    /**
     * Checks if the [player] can use that [item] at that [location]
     */
    @Override
    public boolean canUseItem(@NotNull OfflinePlayer offlinePlayer,
        @NotNull ItemStack itemStack,
        @NotNull Location location)
    {
        boolean returnValue;

        if (Nova.getNova().getBlockManager().hasBlock(location))
        {
            // If the block is a Nova TileEntity, then use interaction protection.
            returnValue = this.checkProtection(offlinePlayer, location, NovaHookAddon.NOVA_INTERACTION_PROTECTION, false);
        }
        else if (Nova.getNova().getMaterialRegistry().getOrNull(itemStack) != null)
        {
            // Use block break flag, because it would be too tricky to detect every possible correct usage.
            returnValue = this.checkProtection(offlinePlayer, location, NovaHookAddon.NOVA_USE_PROTECTION, false);
        }
        else
        {
            // Return true by default.
            returnValue = true;
        }

        return returnValue;
    }


// ---------------------------------------------------------------------
// Section: Tile entity related methods
// ---------------------------------------------------------------------


    /**
     * Checks if that tileEntity can break a block at that [location] using that [item]
     */
    @Override
    public boolean canBreak(@NotNull TileEntity tileEntity,
        @Nullable ItemStack itemStack,
        @NotNull Location location)
    {
        return this.checkProtection(tileEntity.getOwner(), location, Flags.BREAK_BLOCKS);
    }


    /**
     * Checks if the tileEntity can place that [item] at that [location]
     */
    @Override
    public boolean canPlace(@NotNull TileEntity tileEntity,
        @NotNull ItemStack itemStack,
        @NotNull Location location)
    {
        return this.checkProtection(tileEntity.getOwner(), location, Flags.PLACE_BLOCKS);
    }


    /**
     * Checks if the tileEntity can hurt the [entity] with this [item]
     */
    @Override
    public boolean canHurtEntity(@NotNull TileEntity tileEntity,
        @NotNull Entity entity,
        @Nullable ItemStack itemStack)
    {
        // Mobs being hurt

        if (Util.isPassiveEntity(entity))
        {
            return this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.HURT_ANIMALS);
        }
        else if (entity instanceof AbstractVillager)
        {
            return this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.HURT_VILLAGERS);
        }
        else if (Util.isHostileEntity(entity))
        {
            return this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.HURT_MONSTERS);
        }
        else
        {
            return true;
        }
    }


    /**
     * Checks if the tileEntity can interact with the [entity] while holding [item]
     */
    @Override
    public boolean canInteractWithEntity(@NotNull TileEntity tileEntity,
        @NotNull Entity entity,
        @Nullable ItemStack itemStack)
    {
        boolean returnValue;

        if (entity instanceof Vehicle)
        {
            if (entity instanceof Animals)
            {
                // Animal riding
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.RIDING);
            }
            else if (entity instanceof RideableMinecart)
            {
                // Minecart riding
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.MINECART);
            }
            else if (entity instanceof StorageMinecart)
            {
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.CHEST);
            }
            else if (entity instanceof HopperMinecart)
            {
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.HOPPER);
            }
            else if (entity instanceof PoweredMinecart)
            {
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.FURNACE);
            }
            else if (entity instanceof Boat)
            {
                // Boat riding
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.BOAT);
            }
            else
            {
                returnValue = true;
            }
        }
        else if (entity instanceof Villager || entity instanceof WanderingTrader)
        {
            // Villager trading
            // Check naming and check trading
            returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.TRADING);

            if (itemStack != null && itemStack.getType().equals(Material.NAME_TAG))
            {
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.NAME_TAG);
            }
        }
        else if (entity instanceof Allay)
        {
            // Allay item giving/taking
            returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.ALLAY);

            // Check naming
            if (itemStack != null && itemStack.getType().equals(Material.NAME_TAG))
            {
                returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.NAME_TAG);
            }
        }
        else if (itemStack != null && itemStack.getType().equals(Material.NAME_TAG))
        {
            // Name tags
            returnValue = this.checkProtection(tileEntity.getOwner(), entity.getLocation(), Flags.NAME_TAG);
        }
        else if (itemStack != null && entity instanceof Animals)
        {
            // Check Breeding
            if (BREEDING_ITEMS.containsKey(entity.getType()) &&
                BREEDING_ITEMS.get(entity.getType()).contains(itemStack.getType()))
            {
                returnValue = !this.checkProtection(tileEntity.getOwner(),
                    entity.getLocation(),
                    Flags.BREEDING);
            }
            else
            {
                returnValue = false;
            }
        }
        else
        {
            returnValue = true;
        }

        return returnValue;
    }


    /**
     * Checks if the tileEntity can interact with a block at that [location] using that [item]
     */
    @Override
    public boolean canUseBlock(@NotNull TileEntity tileEntity,
        @Nullable ItemStack itemStack,
        @NotNull Location location)
    {
        return this.canUseBlock(tileEntity.getOwner(), itemStack, location);
    }


    /**
     * Checks if the tileEntity can use that [item] at that [location]
     */
    @Override
    public boolean canUseItem(@NotNull TileEntity tileEntity,
        @NotNull ItemStack itemStack,
        @NotNull Location location)
    {
        return this.canUseItem(tileEntity.getOwner(), itemStack, location);
    }


// ---------------------------------------------------------------------
// Section: Actual check methods
// ---------------------------------------------------------------------


    /**
     * This method returns the flag protection value for OfflinePlayer in the given Location.
     * Method is silent (will not produce message).
     * @param player Player instance that triggers protection check.
     * @param loc Location where protection need to be checked.
     * @param flag The flag that triggers protection.
     * @return {@code true} if flag is allowing action, {@code false} otherwise.
     */
    private boolean checkProtection(@NotNull OfflinePlayer player, @NotNull Location loc, @NotNull Flag flag)
    {
        return this.checkProtection(player, loc, flag, true);
    }


    /**
     * This method returns the flag protection value for OfflinePlayer in the given Location.
     * This is a clone of FlagsListener#checkIsland
     * @param player Player instance that triggers protection check.
     * @param loc Location where protection need to be checked.
     * @param flag The flag that triggers protection.
     * @param silent The boolean that indicate if message about blocking should be displayed or not.
     * @return {@code true} if flag is allowing action, {@code false} otherwise.
     */
    private boolean checkProtection(@NotNull OfflinePlayer player, @NotNull Location loc, @NotNull Flag flag, boolean silent)
    {
        // Set user
        User user = User.getInstance(player);

        // If this is not an Island World or a standard Nether or End, skip
        if (!this.plugin.getIWM().inWorld(loc))
        {
            this.report(user, loc, flag, Why.UNPROTECTED_WORLD);
            return true;
        }

        // Get the island and if present
        Optional<Island> island = this.plugin.getIslandsManager().getProtectedIslandAt(loc);

        // Handle Settings Flag
        if (flag.getType().equals(Flag.Type.SETTING))
        {
            // If the island exists, return the setting, otherwise return the default setting for this flag
            if (island.isPresent())
            {
                this.report(user,
                    loc,
                    flag,
                    island.map(x -> x.isAllowed(flag)).orElse(false) ? Why.SETTING_ALLOWED_ON_ISLAND :
                        Why.SETTING_NOT_ALLOWED_ON_ISLAND);
            }
            else
            {
                this.report(user,
                    loc,
                    flag,
                    flag.isSetForWorld(loc.getWorld()) ? Why.SETTING_ALLOWED_IN_WORLD :
                        Why.SETTING_NOT_ALLOWED_IN_WORLD);
            }

            return island.map(x -> x.isAllowed(flag)).orElseGet(() -> flag.isSetForWorld(loc.getWorld()));
        }

        // Protection flag

        // Ops or "bypass everywhere" moderators can do anything unless they have switched it off
        if (!user.getMetaData(AdminSwitchCommand.META_TAG).map(MetaDataValue::asBoolean).orElse(false) &&
            (user.hasPermission(this.plugin.getIWM().getPermissionPrefix(loc.getWorld()) + "mod.bypassprotect") ||
                user.hasPermission(this.plugin.getIWM().getPermissionPrefix(loc.getWorld()) + "mod.bypass." + flag.getID() + ".everywhere")))
        {
            if (user.isOp())
            {
                this.report(user, loc, flag, Why.OP);
            }
            else
            {
                this.report(user, loc, flag, Why.BYPASS_EVERYWHERE);
            }

            return true;
        }

        // Handle World Settings
        if (flag.getType().equals(Flag.Type.WORLD_SETTING))
        {
            if (flag.isSetForWorld(loc.getWorld()))
            {
                this.report(user, loc, flag, Why.ALLOWED_IN_WORLD);
                return true;
            }

            this.report(user, loc, flag, Why.NOT_ALLOWED_IN_WORLD);

            if (!silent)
            {
                user.notify("protection.world-protected",
                    TextVariables.DESCRIPTION,
                    user.getTranslation(flag.getHintReference()));
            }

            return false;
        }

        // Check if the plugin is set in User (required for testing)
        User.setPlugin(plugin);

        if (island.isPresent())
        {
            // If it is not allowed on the island, "bypass island" moderators can do anything
            if (island.get().isAllowed(user, flag))
            {
                this.report(user, loc, flag, Why.RANK_ALLOWED);
                return true;
            }
            else if (!user.getMetaData(AdminSwitchCommand.META_TAG).map(MetaDataValue::asBoolean).orElse(false)
                && (user.hasPermission(this.plugin.getIWM().getPermissionPrefix(loc.getWorld()) + "mod.bypass." + flag.getID() + ".island")))
            {
                this.report(user, loc, flag, Why.BYPASS_ISLAND);
                return true;
            }

            this.report(user, loc, flag, Why.NOT_ALLOWED_ON_ISLAND);

            if (!silent)
            {
                user.notify(island.get().isSpawn() ? "protection.spawn-protected" : "protection.protected",
                    TextVariables.DESCRIPTION,
                    user.getTranslation(flag.getHintReference()));
            }

            return false;
        }

        // The player is in the world, but not on an island, so general world settings apply
        if (flag.isSetForWorld(loc.getWorld()))
        {
            this.report(user, loc, flag, Why.ALLOWED_IN_WORLD);
            return true;
        }
        else
        {
            this.report(user, loc, flag, Why.NOT_ALLOWED_IN_WORLD);

            if (!silent)
            {
                user.notify("protection.world-protected",
                    TextVariables.DESCRIPTION,
                    user.getTranslation(flag.getHintReference()));
            }

            return false;
        }
    }


 // ---------------------------------------------------------------------
 // Section: Reporting
 // ---------------------------------------------------------------------


    /**
     * BentoBox WHY functionally.
     * @param user User whoes flags are checked.
     * @param loc Location of the check.
     * @param flag Flag that operates.
     * @param why Reason of WHY.
     */
    private void report(User user, @NotNull Location loc, @NotNull Flag flag, @NotNull Why why)
    {
        // A quick way to debug flag listener unit tests is to add this line here: System.out.println(why.name()); NOSONAR
        if (user != null && user.isPlayer() &&
            user.getPlayer().getMetadata(loc.getWorld().getName() + "_why_debug").stream().
                filter(p -> p.getOwningPlugin().equals(this.plugin)).
                findFirst().
                map(MetadataValue::asBoolean).
                orElse(false))
        {
            String whyEvent = "Why: " + flag.getID() + " in world " + loc.getWorld().getName() + " at " + Util.xyz(loc.toVector());
            String whyBypass = "Why: " + user.getName() + " " + flag.getID() + " - " + why.name();

            this.plugin.log(whyEvent);
            this.plugin.log(whyBypass);

            // See if there is a player that issued the debug
            String issuerUUID = user.getPlayer().getMetadata(loc.getWorld().getName() + "_why_debug_issuer").stream().
                filter(p -> this.plugin.equals(p.getOwningPlugin())).
                findFirst().
                map(MetadataValue::asString).
                orElse("");

            if (!issuerUUID.isEmpty())
            {
                User issuer = User.getInstance(UUID.fromString(issuerUUID));

                if (issuer.isPlayer())
                {
                    user.sendRawMessage(whyEvent);
                    user.sendRawMessage(whyBypass);
                }
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Classes
// ---------------------------------------------------------------------


    /**
     * This enum stores reasons why action was allowed/prevented by BentoBox.
     * This is a clone of {@see FlagListener#Why} enum. The enum is protected :(
     */
    private enum Why
    {
        UNPROTECTED_WORLD,
        OP,
        BYPASS_EVERYWHERE,
        BYPASS_ISLAND,
        RANK_ALLOWED,
        ALLOWED_IN_WORLD,
        NOT_ALLOWED_ON_ISLAND,
        NOT_ALLOWED_IN_WORLD,
        SETTING_ALLOWED_ON_ISLAND,
        SETTING_NOT_ALLOWED_ON_ISLAND,
        SETTING_ALLOWED_IN_WORLD,
        SETTING_NOT_ALLOWED_IN_WORLD
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * Instance of NovaHook addon.
     */
    private final BentoBox plugin = BentoBox.getInstance();

    /**
     * Map of things that can breed entities.
     */
    private static final Map<EntityType, List<Material>> BREEDING_ITEMS;

    /*
      The static populator of all entity breeding materials.
      Clone from {@see BreedingListener#BREEDING_ITEMS}
     */
    static {
        Map<EntityType, List<Material>> bi = new HashMap<>();
        bi.put(EntityType.HORSE, Arrays.asList(Material.GOLDEN_APPLE, Material.GOLDEN_CARROT));
        bi.put(EntityType.DONKEY, Arrays.asList(Material.GOLDEN_APPLE, Material.GOLDEN_CARROT));
        bi.put(EntityType.COW, Collections.singletonList(Material.WHEAT));
        bi.put(EntityType.MUSHROOM_COW, Collections.singletonList(Material.WHEAT));
        bi.put(EntityType.SHEEP, Collections.singletonList(Material.WHEAT));
        bi.put(EntityType.PIG, Arrays.asList(Material.CARROT, Material.POTATO, Material.BEETROOT));
        bi.put(EntityType.CHICKEN, Arrays.asList(Material.WHEAT_SEEDS, Material.PUMPKIN_SEEDS, Material.MELON_SEEDS, Material.BEETROOT_SEEDS));
        bi.put(EntityType.WOLF, Arrays.asList(Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.BEEF, Material.COOKED_BEEF, Material.CHICKEN, Material.COOKED_CHICKEN, Material.RABBIT, Material.COOKED_RABBIT, Material.MUTTON, Material.COOKED_MUTTON, Material.ROTTEN_FLESH));
        bi.put(EntityType.CAT, Arrays.asList(Material.COD, Material.SALMON));
        bi.put(EntityType.OCELOT, Arrays.asList(Material.COD, Material.SALMON));
        bi.put(EntityType.RABBIT, Arrays.asList(Material.DANDELION, Material.CARROT, Material.GOLDEN_CARROT));
        bi.put(EntityType.LLAMA, Collections.singletonList(Material.HAY_BLOCK));
        bi.put(EntityType.TRADER_LLAMA, Collections.singletonList(Material.HAY_BLOCK));
        bi.put(EntityType.TURTLE, Collections.singletonList(Material.SEAGRASS));
        bi.put(EntityType.PANDA, Collections.singletonList(Material.BAMBOO));
        bi.put(EntityType.FOX, Collections.singletonList(Material.SWEET_BERRIES));
        bi.put(EntityType.BEE, Arrays.asList(Material.SUNFLOWER, Material.ORANGE_TULIP, Material.PINK_TULIP, Material.RED_TULIP, Material.WHITE_TULIP, Material.ALLIUM, Material.AZURE_BLUET, Material.BLUE_ORCHID, Material.CORNFLOWER, Material.DANDELION, Material.OXEYE_DAISY, Material.PEONY, Material.POPPY));
        bi.put(EntityType.HOGLIN, Collections.singletonList(Material.CRIMSON_FUNGUS));
        bi.put(EntityType.STRIDER, Collections.singletonList(Material.WARPED_FUNGUS));
        bi.put(EntityType.AXOLOTL, Collections.singletonList(Material.TROPICAL_FISH_BUCKET));
        bi.put(EntityType.GOAT, Collections.singletonList(Material.WHEAT));

        if (Enums.getIfPresent(EntityType.class, "FROG").isPresent()) {
            bi.put(EntityType.FROG, Collections.singletonList(Material.SLIME_BALL));
            bi.put(EntityType.ALLAY, Collections.singletonList(Material.AMETHYST_SHARD));
        }

        BREEDING_ITEMS = Collections.unmodifiableMap(bi);
    }
}
