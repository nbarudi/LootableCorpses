package ca.bungo.lc.events;

import ca.bungo.lc.core.LootableCorpses;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerConnectionEvents implements Listener {

    LootableCorpses lc;

    public PlayerConnectionEvents(LootableCorpses lc){
        this.lc = lc;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        lc.corpseCore.spawnCorpses(player);
    }


}
