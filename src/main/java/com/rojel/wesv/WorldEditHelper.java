/*
 * Decompiled with CFR 0_110.
 *
 * Could not load the following classes:
 *  com.sk89q.worldedit.BlockVector2D
 *  com.sk89q.worldedit.IncompleteRegionException
 *  com.sk89q.worldedit.LocalSession
 *  com.sk89q.worldedit.Vector
 *  com.sk89q.worldedit.WorldEdit
 *  com.sk89q.worldedit.bukkit.WorldEditPlugin
 *  com.sk89q.worldedit.regions.Polygonal2DRegion
 *  com.sk89q.worldedit.regions.Region
 *  com.sk89q.worldedit.regions.RegionSelector
 *  com.sk89q.worldedit.session.SessionManager
 *  com.sk89q.worldedit.world.World
 *  org.bukkit.Server
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Event
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 *  org.bukkit.scheduler.BukkitRunnable
 *  org.bukkit.scheduler.BukkitTask
 */
package com.rojel.wesv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;

public class WorldEditHelper
implements Listener {
    private final JavaPlugin plugin;
    private final WorldEditPlugin we;
    private final Map<UUID, Region> lastSelectedRegions;

    public WorldEditHelper(final JavaPlugin plugin, final Configuration config) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin)plugin.getServer().getPluginManager().getPlugin("WorldEdit");
        this.lastSelectedRegions = new HashMap<UUID, Region>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable(){

            @Override
			public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    Region currentRegion;
                    if (!config.isEnabled(player) || !player.hasPermission("wesv.use") || WorldEditHelper.this.compareRegion(WorldEditHelper.this.lastSelectedRegions.get(player.getUniqueId()), currentRegion = WorldEditHelper.this.getSelectedRegion(player))) {
						continue;
					}
                    if (currentRegion != null) {
                        WorldEditHelper.this.lastSelectedRegions.put(player.getUniqueId(), currentRegion.clone());
                    } else {
                        WorldEditHelper.this.lastSelectedRegions.remove(player.getUniqueId());
                    }
                    plugin.getServer().getPluginManager().callEvent(new WorldEditSelectionChangeEvent(player, currentRegion));
                }
            }
        }.runTaskTimer(plugin, 0, config.updateSelectionInterval());
    }

    public Region getSelectedRegion(Player player) {
        RegionSelector selector;
        LocalSession session = this.we.getWorldEdit().getSessionManager().findByName(player.getName());
        if (session != null && session.getSelectionWorld() != null && (selector = session.getRegionSelector(session.getSelectionWorld())).isDefined()) {
            try {
                return selector.getRegion();
            }
            catch (IncompleteRegionException e) {
                this.plugin.getServer().getLogger().info("Region still incomplete.");
            }
        }
        return null;
    }

    public boolean compareRegion(Region r1, Region r2) {
        boolean points;
        if (r1 == null && r2 == null) {
            return true;
        }
        if (r1 != null && r2 == null) {
            return false;
        }
        if (r1 == null && r2 != null) {
            return false;
        }
        points = r1.getMinimumPoint().equals(r2.getMinimumPoint()) && r1.getMaximumPoint().equals(r2.getMaximumPoint()) && r1.getCenter().equals(r2.getCenter());
        boolean worlds = r1.getWorld() != null ? r1.getWorld().equals(r2.getWorld()) : r2.getWorld() == null;
        boolean dimensions = r1.getWidth() == r2.getWidth() && r1.getHeight() == r2.getHeight() && r1.getLength() == r2.getLength();
        boolean type = r1.getClass().equals(r2.getClass());
        boolean polyPoints = true;
        if (r1 instanceof Polygonal2DRegion && r2 instanceof Polygonal2DRegion) {
            Polygonal2DRegion r1Poly = (Polygonal2DRegion)r1;
            Polygonal2DRegion r2Poly = (Polygonal2DRegion)r2;
            if (r1Poly.getPoints().size() != r2Poly.getPoints().size()) {
                polyPoints = false;
            } else {
                for (int i = 0; i < r1Poly.getPoints().size(); ++i) {
                    if (r1Poly.getPoints().get(i).equals(r2Poly.getPoints().get(i))) {
						continue;
					}
                    polyPoints = false;
                }
            }
        }
        return points && worlds && dimensions && type && polyPoints;
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        this.lastSelectedRegions.remove(event.getPlayer().getUniqueId());
    }

}

