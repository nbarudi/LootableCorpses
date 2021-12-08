package ca.bungo.lc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import ca.bungo.lc.LootableCorpses;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityPose;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;

@SuppressWarnings("unchecked")
public class CorpseController {

	public static List<EntityPlayer> corpses = new ArrayList<>();
	public static Map<String, Inventory> items = new HashMap<>();
	
	public static void createCorpse(Player player, Inventory inv) {
		
		EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
		
		//Setting up GameProfile / Skin Information
        Property textures = (Property) ePlayer.getProfile().getProperties().get("textures").toArray()[0];
        GameProfile gProfile = new GameProfile(UUID.randomUUID(), ChatColor.RED + "DEAD: " + player.getName());
        gProfile.getProperties().put("textures", new Property("textures", textures.getValue(), textures.getSignature()));
		
		//Creating Entity
		EntityPlayer corpse = new EntityPlayer(
				((CraftServer) Bukkit.getServer()).getServer(),
				((CraftWorld) player.getWorld()).getHandle(),
				gProfile,
				new PlayerInteractManager(((CraftWorld) player.getWorld()).getHandle())
				);
		
		corpse.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
		
		DataWatcher watcher = corpse.getDataWatcher();
		try {
			byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40; // each of the overlays (cape, jacket, sleeves, pants, hat)
	        watcher.set(DataWatcherRegistry.a.a(16), b); // To find value use wiki.vg
	        
			Field poseField = Entity.class.getDeclaredField("POSE");
            poseField.setAccessible(true);
            DataWatcherObject<EntityPose> POSE = (DataWatcherObject<EntityPose>) poseField.get(null);
            watcher.set(POSE, EntityPose.SLEEPING);
		} catch(Exception e) {
			e.printStackTrace();
		}

        
        PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                corpse.getId(), (byte) 0, (byte) ((player.getLocation()
                .getY() - 1.7 - player.getLocation().getY()) * 32),
                (byte) 0, false);
        
        corpses.add(corpse);
        items.put(corpse.getUniqueIDString(), inv);
        
        for(Player plr : Bukkit.getOnlinePlayers()) {
        	PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
        	conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse));
        	conn.sendPacket(new PacketPlayOutNamedEntitySpawn(corpse));
        	conn.sendPacket(new PacketPlayOutEntityMetadata(corpse.getId(), watcher, false));
        	conn.sendPacket(move);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(LootableCorpses.getPlugin(LootableCorpses.class), ()->{
        		conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
        	}, 2);
        }
	}
	
	private static void createCorpse(String uuid, String txt, String sig, String name, Location loc, Inventory inv) {
		
        GameProfile gProfile = new GameProfile(UUID.fromString(uuid), name);
        gProfile.getProperties().put("textures", new Property("textures", txt, sig));
		
		//Creating Entity
		EntityPlayer corpse = new EntityPlayer(
				((CraftServer) Bukkit.getServer()).getServer(),
				((CraftWorld) loc.getWorld()).getHandle(),
				gProfile,
				new PlayerInteractManager(((CraftWorld) loc.getWorld()).getHandle())
				);
		
		corpse.setPosition(loc.getX(), loc.getY(), loc.getZ());
		
		
		DataWatcher watcher = corpse.getDataWatcher();
		try {
			byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40; // each of the overlays (cape, jacket, sleeves, pants, hat)
	        watcher.set(DataWatcherRegistry.a.a(16), b); // To find value use wiki.vg
	        
			Field poseField = Entity.class.getDeclaredField("POSE");
            poseField.setAccessible(true);
            DataWatcherObject<EntityPose> POSE = (DataWatcherObject<EntityPose>) poseField.get(null);
            watcher.set(POSE, EntityPose.SLEEPING);
		} catch(Exception e) {
			e.printStackTrace();
		}
        
        PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                corpse.getId(), (byte) 0, (byte) ((loc
                .getY() - 1.7 - loc.getY()) * 32),
                (byte) 0, false);
        
        corpses.add(corpse);
        
        items.put(corpse.getUniqueIDString(), inv);
        
        for(Player plr : Bukkit.getOnlinePlayers()) {
        	PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
        	conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse));
        	conn.sendPacket(new PacketPlayOutNamedEntitySpawn(corpse));
        	conn.sendPacket(new PacketPlayOutEntityMetadata(corpse.getId(), watcher, false));
        	conn.sendPacket(move);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(LootableCorpses.getPlugin(LootableCorpses.class), ()->{
        		conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
        	}, 5);
        }
	}
	
	public static void spawnCorpses(Player player) {
		for(EntityPlayer c : corpses) {
			
			EntityPlayer corpse = new EntityPlayer(
					((CraftServer) Bukkit.getServer()).getServer(),
					((CraftWorld) player.getLocation().getWorld()).getHandle(),
					c.getProfile(),
					c.playerInteractManager
					);
			
			setValue(corpse, "id", c.getId(), Entity.class);
			
			Location cLoc = c.getBukkitEntity().getLocation();
			Location loc = corpse.getBukkitEntity().getLocation();
			corpse.setPosition(cLoc.getX(), cLoc.getY(), cLoc.getZ());
			System.out.println("BLAH " + loc.getX() );
			
	
			DataWatcher watcher = corpse.getDataWatcher();
			try {
				byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40; // each of the overlays (cape, jacket, sleeves, pants, hat)
		        watcher.set(DataWatcherRegistry.a.a(16), b); // To find value use wiki.vg
		        
				Field poseField = Entity.class.getDeclaredField("POSE");
	            poseField.setAccessible(true);
	            DataWatcherObject<EntityPose> POSE = (DataWatcherObject<EntityPose>) poseField.get(null);
	            watcher.set(POSE, EntityPose.SLEEPING);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			PacketPlayOutEntity.PacketPlayOutRelEntityMove move = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
	                corpse.getId(), (byte) 0, (byte) ((loc
	                .getY() - 1.7 - loc.getY()) * 32),
	                (byte) 0, false);
	        
	        
	        PlayerConnection conn = ((CraftPlayer) player).getHandle().playerConnection;
        	conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse));
        	conn.sendPacket(new PacketPlayOutNamedEntitySpawn(corpse));
        	conn.sendPacket(new PacketPlayOutEntityMetadata(corpse.getId(), watcher, false));
        	conn.sendPacket(move);
        	Bukkit.getScheduler().scheduleSyncDelayedTask(LootableCorpses.getPlugin(LootableCorpses.class), ()->{
        		conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
        	}, 20);
		}
	}
	
	public static void clearCorpses() {
		for(EntityPlayer corpse : corpses) {
			removeCorpsePacket(corpse);
		}
		corpses.clear();
		items.clear();
	}
	
	public static void removeCorpsePacket(EntityPlayer corpse) {
		for(Player plr : Bukkit.getOnlinePlayers()) {
			PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
			conn.sendPacket(new PacketPlayOutEntityDestroy(corpse.getId()));
		}
	}
	
	
	public static void removeCorpse(EntityPlayer corpse) {
		corpses.remove(corpse);
		items.remove(corpse.getUniqueIDString());
		for(Player plr : Bukkit.getOnlinePlayers()) {
			PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
			conn.sendPacket(new PacketPlayOutEntityDestroy(corpse.getId()));
		}
	}
	
	public static void saveCorpseData() {
		LootableCorpses lc = LootableCorpses.getPlugin(LootableCorpses.class);
		
		YamlConfiguration cfg = lc.fm.getConfig("corpses.yml").get();
		
		ConfigurationSection sec = cfg.getConfigurationSection("Corpses");
		if(sec == null)
			sec = cfg.createSection("Corpses");
		
		for(EntityPlayer corpse : corpses) {
			Location loc = corpse.getBukkitEntity().getLocation();
			String uuid = corpse.getUniqueIDString();
			Property textures = (Property) corpse.getProfile().getProperties().get("textures").toArray()[0];
			sec.set(uuid + ".world", loc.getWorld().getName());
			sec.set(uuid + ".x", loc.getX());
			sec.set(uuid + ".y", loc.getY());
			sec.set(uuid + ".z", loc.getZ());
			sec.set(uuid + ".name", corpse.getName());
			sec.set(uuid + ".txtr", textures.getValue());
			sec.set(uuid + ".sig", textures.getSignature());
			sec.set(uuid + ".inventory", toBase64(items.get(corpse.getUniqueIDString())));
			lc.fm.saveConfig("corpses.yml");
		}
	}
	
	public static void loadCorpseData() {
		LootableCorpses lc = LootableCorpses.getPlugin(LootableCorpses.class);
		
		YamlConfiguration cfg = lc.fm.getConfig("corpses.yml").get();
		
		ConfigurationSection sec = cfg.getConfigurationSection("Corpses");
		if(sec == null)
			return;
		
		for(String uuid : sec.getKeys(false)) {
			
			double x = sec.getDouble(uuid + ".x");
			double y = sec.getDouble(uuid + ".y");
			double z = sec.getDouble(uuid + ".z");
			String w = sec.getString(uuid + ".world");
			World world = Bukkit.getWorld(w);
			
			Location loc = new Location(world, x, y , z);
			
			String b64 = sec.getString(uuid + ".inventory");
			String txtr = sec.getString(uuid + ".txtr");
			String sig = sec.getString(uuid +".sig");
			String name = sec.getString(uuid + ".name");
			
			Inventory inv = fromBase64(b64);
			
			createCorpse(uuid, txtr, sig, name, loc, inv);
			sec.set(uuid, null);
		}
		lc.fm.saveConfig("corpses.yml");
		
	}
	
	
	//Helper Functions
	private static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(outputStream);
            data.writeInt(inventory.getSize());
            data.writeObject("Corpse");
            for (int i = 0; i < inventory.getSize(); i++) {
                data.writeObject(inventory.getItem(i));
            }
            data.close();
           
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
	
	private static Inventory fromBase64(String base64) {
	    try {
	        ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
	        BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
	        int size = data.readInt();
	        if (size % 9 != 0) return null;
	       
	        Inventory inventory = Bukkit.createInventory(null, size, data.readObject().toString());
	       
	        for (int i = 0; i < inventory.getSize(); i++) {
	            inventory.setItem(i, (ItemStack) data.readObject());
	        }
	        data.close();
	       
	        return inventory;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	private static void setValue(Object object, String name, Object value, Class<?> base) {
		try {
			Field f = base.getDeclaredField(name);
			f.setAccessible(true);
			f.set(object, value);
			f.setAccessible(false);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
