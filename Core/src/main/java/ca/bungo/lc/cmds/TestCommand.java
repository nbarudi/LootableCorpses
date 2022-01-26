package ca.bungo.lc.cmds;

import ca.bungo.lc.core.LootableCorpses;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TestCommand implements CommandExecutor {

    LootableCorpses lc;

    public TestCommand(LootableCorpses lc){
        this.lc = lc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if(!(sender instanceof Player))
            return true;
        Player player = (Player) sender;

        Inventory inv = Bukkit.createInventory(null, 54);

        inv.setContents(player.getInventory().getContents());

        lc.corpseCore.createCorpse(player, inv);
        sender.sendMessage("Spawned Corpse!");




        return true;
    }
}
