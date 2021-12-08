package ca.bungo.lc.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import ca.bungo.lc.LootableCorpses;
import ca.bungo.lc.events.customEvents.InteractCorpseEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInUseEntity;

public class PacketReader {
	
	
	private Map<UUID, Channel> channels = new HashMap<>();
	Channel channel;

	
	public void inject(Player player) {
		
		channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
		channels.put(player.getUniqueId(), channel);
		
		if(channel.pipeline().get("PacketInjector") != null)
			return;
		
		channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInUseEntity>() {

			@Override
			protected void decode(ChannelHandlerContext channelHandler, PacketPlayInUseEntity packet,
					List<Object> args) throws Exception {
				args.add(packet);
				readPacket(player, packet);
				
			}
		});
	}
	
	public void unInject(Player player) {
		channel = channels.get(player.getUniqueId());
		if(channel.pipeline().get("PacketInjector") != null)
			channel.pipeline().remove("PacketInjector");
	}
	
	public void readPacket(Player player, Packet<?> packet) {
		
		if(packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {

			//System.out.println(getValue(packet, "action").toString());
			
			int id = (int)getValue(packet, "a");
			
			if((getValue(packet, "action")).toString().equalsIgnoreCase("INTERACT") && !(getValue(packet, "d")).toString().equalsIgnoreCase("OFF_HAND")){
				for(EntityPlayer corpse : CorpseController.corpses) {
					if(id == corpse.getId()) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(LootableCorpses.getPlugin(LootableCorpses.class), () ->{
							Bukkit.getPluginManager().callEvent(new InteractCorpseEvent(player, corpse));
						}, 0);
						break;
					}

				}
				//Bukkit.getPluginManager().callEvent(new InteractCorpseEvent(player, null));
			}
		
		}
	}
	
	private Object getValue(Object cls, String name) {
		Object val = null;
		try {
			Field f = cls.getClass().getDeclaredField(name);
			f.setAccessible(true);
			val = f.get(cls);
			f.setAccessible(false);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return val;
	}

}
