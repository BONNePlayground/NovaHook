//
// Created by BONNe
// Copyright - 2023
//


package lv.id.bonne.novahook.listeners;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import world.bentobox.bentobox.api.events.island.IslandDeleteChunksEvent;
import world.bentobox.bentobox.database.objects.IslandDeletion;
import xyz.xenondevs.nova.api.Nova;
import xyz.xenondevs.nova.api.block.BlockManager;


/**
 * This listener checks when island chunks are removed and at that point, remove all Nova blocks
 * from the island. Resource intensive thing. May require rewriting :)
 */
public class IslandDeleteListener implements Listener
{
    @EventHandler
    public void onIslandDeletion(IslandDeleteChunksEvent event)
    {
        final IslandDeletion info = event.getDeletedIslandInfo();
        final World world = event.getDeletedIslandInfo().getWorld();
        final BlockManager blockManager = Nova.getNova().getBlockManager();

        if (world == null)
        {
            return;
        }

        final int maxX = info.getMaxX();
        final int maxZ = info.getMaxZ();

        for (int x = info.getMinX(); x < maxX; x++)
        {
            for (int z = info.getMinZ(); z < maxZ; z++)
            {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++)
                {
                    Location location = new Location(world, x, y, z);

                    if (blockManager.hasBlock(location))
                    {
                        blockManager.removeBlock(location);
                    }
                }
            }
        }
    }
}
