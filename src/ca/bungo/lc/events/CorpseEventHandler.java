package ca.bungo.lc.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ca.bungo.lc.LootableCorpses;
import ca.bungo.lc.events.customEvents.InteractCorpseEvent;
import ca.bungo.lc.util.CorpseController;
import net.minecraft.server.v1_16_R3.EntityPlayer;

public class CorpseEventHandler implements Listener {
	
	private LootableCorpses lc;
	public ItemStack emptySlot;
	
	public CorpseEventHandler(LootableCorpses lc) {
		this.lc = lc;
		emptySlot = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
		ItemMeta meta = emptySlot.getItemMeta();
		meta.setDisplayName(ChatColor.BLACK + "#");
		emptySlot.setItemMeta(meta);
	}
	
	//Handling Packet Injection
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		lc.pr.inject(player);
		CorpseController.spawnCorpses(player);
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		lc.pr.unInject(player);
	}
	
	@EventHandler
	public void onCorpseInteract(InteractCorpseEvent event) {
		Inventory inv = CorpseController.items.get(event.getCorpse().getUniqueIDString());
		
		event.getPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent event) {
		if(CorpseController.items.containsValue(event.getInventory())) {
			if(isInvEmpty(event.getInventory()))
				CorpseController.removeCorpse(getCorpseFromInventory(event.getInventory()));
		}
	}

	
	private EntityPlayer getCorpseFromInventory(Inventory inv) {
		EntityPlayer retVal = null;
		for(EntityPlayer ePl : CorpseController.corpses) {
			if(CorpseController.items.get(ePl.getUniqueIDString()).equals(inv)) {
				retVal = ePl;
				break;
			}
		}
		return retVal;
	}
	
	private boolean isInvEmpty(Inventory inv) {
		boolean hasItems = false;
		for(int i = 0; i < inv.getSize(); i++) {
			if(inv.getItem(i) == null)
				continue;
			if(!inv.getItem(i).getItemMeta().equals(emptySlot.getItemMeta()))
				hasItems = true;
		}
		return !hasItems;
	}
	
	//Death Handling
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if(event.getEntity().getInventory().isEmpty() || event.getKeepInventory())
			return;
		Inventory inv = buildCorpseInventory(event.getEntity());
		
		CorpseController.createCorpse(event.getEntity(), inv);
		event.getDrops().clear();
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if(!CorpseController.items.containsValue(event.getClickedInventory()))
			return;
		if(isInvEmpty(event.getClickedInventory()))
			event.getWhoClicked().closeInventory();
		if(event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null)
			return;
		
		if(event.getCurrentItem().getItemMeta().equals(emptySlot.getItemMeta()))
			event.setCancelled(true);
		
		
	}
	
	private Inventory buildCorpseInventory(Player player) {
		Inventory inv = Bukkit.createInventory(null, 54, "Corpse");
		inv.setContents(player.getInventory().getContents());
		
		for(int i = 0; i < inv.getSize(); i++) {
			if(inv.getItem(i) == null)
				inv.setItem(i, emptySlot);
		}
		
		return inv;
		
	}

}
