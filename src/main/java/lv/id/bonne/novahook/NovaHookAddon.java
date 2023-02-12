package lv.id.bonne.novahook;


import org.bukkit.Material;

import lv.id.bonne.novahook.protection.NovaProtectionIntegration;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.clicklisteners.CycleClick;
import world.bentobox.bentobox.managers.RanksManager;
import xyz.xenondevs.nova.api.Nova;


/**
 * This class inits NovaHook addon.
 */
public final class NovaHookAddon extends Addon
{
    /**
     * Executes code when loading the addon. This is called before {@link #onEnable()}.
     * This <b>must</b> be used to setup configuration, worlds and commands.
     */
    public void onLoad()
    {
    }


    /**
     * Executes code when enabling the addon. This is called after {@link #onLoad()}.
     * <br/> Note that commands and worlds registration <b>must</b> be done in {@link
     * #onLoad()}, if need be. Failure to do so <b>will</b> result in issues such as
     * tab-completion not working for commands.
     */
    public void onEnable()
    {
        // Check if it is enabled - it might be loaded, but not enabled.
        if (this.getPlugin() == null || !this.getPlugin().isEnabled())
        {
            this.logError("BentoBox is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Check if RoseStacker exists.
        if (!this.getServer().getPluginManager().isPluginEnabled("Nova"))
        {
            this.logError("Nova is not available or disabled!");
            this.setState(State.DISABLED);
            return;
        }

        // Register flags
        this.registerFlag(NovaHookAddon.NOVA_INTERACTION_PROTECTION);
        this.registerFlag(NovaHookAddon.NOVA_USE_PROTECTION);

        // Register nova protection
        Nova.getNova().registerProtectionIntegration(new NovaProtectionIntegration());
    }


    /**
     * Executes code when disabling the addon.
     */
    public void onDisable()
    {
    }


// ---------------------------------------------------------------------
// Section: Flags
// ---------------------------------------------------------------------


    /**
     * The flag that prevents interactions with Nova Machines.
     */
    public static Flag NOVA_INTERACTION_PROTECTION = new Flag.Builder("NOVA_INTERACTION_PROTECTION", Material.FURNACE).
        type(Flag.Type.PROTECTION).
        defaultRank(RanksManager.MEMBER_RANK).
        defaultSetting(false).
        clickHandler(new CycleClick("NOVA_INTERACTION_PROTECTION",
            RanksManager.VISITOR_RANK,
            RanksManager.OWNER_RANK)).
        build();

    /**
     * The flag that prevents usage of Nova tools.
     */
    public static Flag NOVA_USE_PROTECTION = new Flag.Builder("NOVA_USE_PROTECTION", Material.WOODEN_PICKAXE).
        type(Flag.Type.PROTECTION).
        defaultRank(RanksManager.MEMBER_RANK).
        defaultSetting(false).
        clickHandler(new CycleClick("NOVA_USE_PROTECTION",
            RanksManager.VISITOR_RANK,
            RanksManager.OWNER_RANK)).
        build();
}
