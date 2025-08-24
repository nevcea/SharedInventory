package com.github.nevcea.sharedInventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;

public class EventListener implements Listener {
    private final SharedInventory plugin;

    public EventListener(SharedInventory plugin) {
        this.plugin = plugin;
    }

    private PlayerInventory inventory;
    private ItemStack[] contents;
    private ItemStack[] armorContents;
    private ItemStack[] extraContents;

    private final Set<Player> dirtyPlayers = new HashSet<>();

    private void markDirty(Player player) {
        dirtyPlayers.add(player);
        Bukkit.getScheduler().runTask(plugin, this::syncDirty);
    }

    private void syncDirty() {
        for (Player player : dirtyPlayers) {
            syncFrom(player);
            syncToAll(player);
        }
        dirtyPlayers.clear();
    }

    private void syncFrom(Player player) {
        contents = player.getInventory().getContents();
        armorContents = player.getInventory().getArmorContents();
        extraContents = player.getInventory().getExtraContents();
    }

    private void syncToAll(Player source) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != source) {
                p.getInventory().setContents(contents);
                p.getInventory().setArmorContents(armorContents);
                p.getInventory().setExtraContents(extraContents);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (inventory == null) {
            inventory = player.getInventory();
        } else {
            if (contents == null) {
                contents = player.getInventory().getContents();
                armorContents = player.getInventory().getArmorContents();
                extraContents = player.getInventory().getExtraContents();
            } else {
                player.getInventory().setContents(contents);
                if (armorContents != null) player.getInventory().setArmorContents(armorContents);
                if (extraContents != null) player.getInventory().setExtraContents(extraContents);
            }
        }
        event.joinMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        markDirty(event.getPlayer());
        event.quitMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            markDirty(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            markDirty(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            markDirty(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        markDirty(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int newSlot = event.getNewSlot();

        markDirty(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p != player) {
                    p.getInventory().setHeldItemSlot(newSlot);
                }
            }
        });
    }
}