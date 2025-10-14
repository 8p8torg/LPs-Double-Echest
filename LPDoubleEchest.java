package com.yourname.lpdoubleechest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

public class LPDoubleEchest extends JavaPlugin implements Listener {

    private final HashMap<UUID, Inventory> playerChests = new HashMap<>();
    private final String CHEST_TITLE = ChatColor.DARK_PURPLE + "LP's Double Echest";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        loadChests();
        getLogger().info("LP's Double Echest by Lplaysgames enabled!");
    }

    @Override
    public void onDisable() {
        saveChests();
        getLogger().info("LP's Double Echest by Lplaysgames disabled!");
    }

    // Player clicks on an Ender Chest
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.ENDER_CHEST) return;

        event.setCancelled(true); // prevent default Ender Chest GUI

        Inventory chest = playerChests.computeIfAbsent(player.getUniqueId(),
                uuid -> Bukkit.createInventory(null, 54, CHEST_TITLE));

        // Open chest safely on main thread (Folia-safe)
        Bukkit.getServer().getScheduler().runTask(this, () -> player.openInventory(chest));
    }

    // Optional: handle merging two placed Ender Chests (vanilla-style)
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ENDER_CHEST) return;

        // Check for adjacent Ender Chest to visually "merge"
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block adjacent = block.getRelative(face);
            if (adjacent.getType() == Material.ENDER_CHEST) {
                // Can add extra visual effects if desired
                break;
            }
        }
    }

    // Save player inventories to file
    private void saveChests() {
        try {
            File file = new File(getDataFolder(), "chests.dat");
            if (!file.exists()) getDataFolder().mkdirs();

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(playerChests);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load player inventories from file
    private void loadChests() {
        try {
            File file = new File(getDataFolder(), "chests.dat");
            if (!file.exists()) return;

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            HashMap<UUID, Inventory> loaded = (HashMap<UUID, Inventory>) ois.readObject();
            playerChests.putAll(loaded);
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
