package ca.bungo.lc.events.customEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.minecraft.server.v1_16_R3.EntityPlayer;

public class InteractCorpseEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	
	private boolean isCancelled = false;
	private final Player player;
	private final EntityPlayer corpse;
	
	public InteractCorpseEvent(Player player, EntityPlayer corpse) {
		this.player = player;
		this.corpse = corpse;
	}
	

	public HandlerList getHandlers() {
	    return handlers;
	}

	public static HandlerList getHandlerList() {
	    return handlers;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
		
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public EntityPlayer getCorpse() {
		return corpse;
	}

}
