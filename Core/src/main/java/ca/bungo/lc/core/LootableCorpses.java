package ca.bungo.lc.core;

import ca.bungo.lc.api.CorpseCore;
import ca.bungo.lc.cmds.TestCommand;
import ca.bungo.lc.events.PlayerConnectionEvents;
import ca.bungo.lc.events.PlayerDeath;
import ca.bungo.lc.util.PacketListening;
import ca.bungo.lc.versions.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LootableCorpses extends JavaPlugin {

    public CorpseCore corpseCore;
    public ProtocolManager protocolManager;
    public ItemStack emptySlot;

    @Override
    public void onEnable(){
        protocolManager = ProtocolLibrary.getProtocolManager();

        emptySlot = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta meta = emptySlot.getItemMeta();
        meta.setDisplayName(ChatColor.BLACK + "#");
        emptySlot.setItemMeta(meta);

        registerConfigs();
        registerNMS();
        registerCommands();
        registerEvents();

        corpseCore.loadCorpseData();


    }

    @Override
    public void onDisable(){
        corpseCore.saveCorpseData();
    }

    private void registerEvents(){
        PluginManager manager = Bukkit.getServer().getPluginManager();
        manager.registerEvents(new PlayerConnectionEvents(this), this);
        manager.registerEvents(new PlayerDeath(this), this);
        manager.registerEvents(corpseCore, this);
        protocolManager.addPacketListener(new PacketListening(this));
    }

    private void registerConfigs(){
        saveDefaultConfig();
    }

    private void registerCommands(){
        this.getCommand("test").setExecutor(new TestCommand(this));
    }

    private void registerNMS(){
        String ver = Bukkit.getServer().getClass().getPackage().getName().replace('.', ',').split(",") [3];
        switch(ver){
            case "v1_18_R1":
                corpseCore = new Version1_18_1_R1(this, emptySlot);
                return;
            case "v1_17_R1":
                corpseCore = new Version1_17_R1(this, emptySlot);
                return;
            case "v1_16_R3":
                corpseCore = new Version1_16_R3(this, emptySlot);
                return;
            case "v1_16_R2":
                corpseCore = new Version1_16_R2(this, emptySlot);
                return;
            case "v1_16_R1":
                corpseCore = new Version1_16_R1(this, emptySlot);
                return;
            case "v1_15_R1":
                corpseCore = new Version1_15_R1(this, emptySlot);
                return;
            case "v1_14_R1":
                corpseCore = new Version1_14_R1(this, emptySlot);
                return;
            case "v1_13_R2":
                corpseCore = new Version1_13_R2(this, emptySlot);
                return;
            case "v1_13_R1":
                return;
            case "v1_12_R1":
                return;
            case "v1_11_R1":
                return;
            case "v1_10_R1":
                return;
            case "v1_9_R2":
                return;
            case "v1_9_R1":
                return;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Invalid Minecraft Server Version! Only Supports 1.9-1.18.1");
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
