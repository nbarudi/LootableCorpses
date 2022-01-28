package ca.bungo.lc.events;

import ca.bungo.lc.core.LootableCorpses;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CorpseInteract implements Listener {

    LootableCorpses lc;

    public CorpseInteract(LootableCorpses lc){
        this.lc = lc;
    }

    @EventHandler
    public void onCorpseInteract(PlayerInteractCorpseEvent event){
        Entity corpse = lc.corpseCore.getCorpseById(event.getCorpseID());
        event.getPlayer().sendMessage(corpse.getName());
    }

}
