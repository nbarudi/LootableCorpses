package ca.bungo.lc.events;

import ca.bungo.lc.core.LootableCorpses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerDeath implements Listener {

    LootableCorpses lc;

    public PlayerDeath(LootableCorpses lc){
        this.lc = lc;
    }
    /**
     * Checking if a players inventory is empty
     * Although Inventory.isEmpty() exists in newer versions of minecraft
     * it does not infact exist in 1.9 spigot API.. Today I learned
     *
     * @param inv   Inventory to check if empty.
     * @return      True if the Inventory is Empty. False otherwise.
     */
    private boolean isEmpty(Inventory inv){
        for(ItemStack itm : inv.getContents()){
            if(itm != null) return false;
        }
        return true;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(isEmpty(event.getEntity().getInventory()) || event.getKeepInventory())
            return;
        Inventory inv = buildCorpseInventory(event.getEntity());

        lc.corpseCore.createCorpse(event.getEntity(), inv);
        event.getDrops().clear();
    }


    private Inventory buildCorpseInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Corpse");
        inv.setContents(player.getInventory().getContents());

        for(int i = 0; i < inv.getSize(); i++) {
            if(inv.getItem(i) == null)
                inv.setItem(i, lc.emptySlot);
        }

        return inv;

    }


}
