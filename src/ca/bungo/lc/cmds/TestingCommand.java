package ca.bungo.lc.cmds;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import ca.bungo.lc.LootableCorpses;
import ca.bungo.lc.util.CorpseController;

public class TestingCommand implements CommandExecutor {

	LootableCorpses lootableCorpses;
	
	public TestingCommand(LootableCorpses lootableCorpses) {
		this.lootableCorpses = lootableCorpses;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		
		Inventory inv = Bukkit.createInventory(null, 54);
		
		inv.setContents(player.getInventory().getContents());
		
		CorpseController.createCorpse(player, inv);
		
		
		return true;
	}
	
	
	
	

}
