package ca.bungo.lc.util;

import ca.bungo.lc.core.LootableCorpses;
import ca.bungo.lc.events.PlayerInteractCorpseEvent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketListening extends PacketAdapter {

    LootableCorpses lc;

    public PacketListening(LootableCorpses lc){
        super(lc, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY);
        this.lc = lc;
    }

    @Override
    public void onPacketReceiving(PacketEvent event){
        Player player = event.getPlayer();
        if(event.getPacketType().equals(PacketType.Play.Client.USE_ENTITY)){

            PacketContainer container = event.getPacket();
            int entityId = container.getIntegers().read(0);

            Bukkit.getScheduler().scheduleSyncDelayedTask(lc, ()->{
                Bukkit.getPluginManager().callEvent(new PlayerInteractCorpseEvent(player, entityId));
            },0);

        }
    }


}
