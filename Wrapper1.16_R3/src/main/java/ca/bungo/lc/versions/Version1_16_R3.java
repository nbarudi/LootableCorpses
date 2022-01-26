package ca.bungo.lc.versions;

import ca.bungo.lc.api.CorpseCore;
import ca.bungo.lc.events.PlayerInteractCorpseEvent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class Version1_16_R3 implements CorpseCore {

    private List<EntityPlayer> corpses = new ArrayList<>();
    private Map<String, Inventory> items = new HashMap<>();
    private Map<Integer, EntityPlayer> IdToCorpse = new HashMap<>();

    private Plugin plugin;
    private org.bukkit.inventory.ItemStack emptyItem;

    public Version1_16_R3(Plugin plugin, ItemStack emptyItem){
        this.plugin = plugin;
        this.emptyItem = emptyItem;
    }

    @Override
    public void createCorpse(Player player, Inventory inventory) {
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
            watcher.set(DataWatcherRegistry.a.a(16), b);

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
        items.put(corpse.getUniqueIDString(), inventory);
        IdToCorpse.put(corpse.getId(), corpse);

        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipment = new ArrayList<>();
        if(inventory.getItem(36) != null && inventory.getItem(36).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inventory.getItem(36))));
        if(inventory.getItem(37) != null && inventory.getItem(37).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inventory.getItem(37))));
        if(inventory.getItem(38) != null && inventory.getItem(38).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inventory.getItem(38))));
        if(inventory.getItem(39) != null && inventory.getItem(39).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inventory.getItem(39))));


        for(Player plr : Bukkit.getOnlinePlayers()) {
            PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
            conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse));
            conn.sendPacket(new PacketPlayOutNamedEntitySpawn(corpse));
            conn.sendPacket(new PacketPlayOutEntityMetadata(corpse.getId(), watcher, false));
            conn.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), equipment));
            conn.sendPacket(move);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.hidePlayer(plugin, corpse.getBukkitEntity().getPlayer());
            }, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.showPlayer(plugin, corpse.getBukkitEntity().getPlayer());
                conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
            }, 15);
        }
    }

    @Override
    public void createCorpse(String uuid, String txt, String sig, String name, Location loc, Inventory inv) {
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
        IdToCorpse.put(corpse.getId(), corpse);
        items.put(corpse.getUniqueIDString(), inv);

        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> equipment = new ArrayList<>();
        if(inv.getItem(36) != null && inv.getItem(36).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(inv.getItem(36))));
        if(inv.getItem(37) != null && inv.getItem(37).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(inv.getItem(37))));
        if(inv.getItem(38) != null && inv.getItem(38).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(inv.getItem(38))));
        if(inv.getItem(39) != null && inv.getItem(39).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(inv.getItem(39))));

        for(Player plr : Bukkit.getOnlinePlayers()) {
            PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
            conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, corpse));
            conn.sendPacket(new PacketPlayOutNamedEntitySpawn(corpse));
            conn.sendPacket(new PacketPlayOutEntityMetadata(corpse.getId(), watcher, false));
            conn.sendPacket(move);
            conn.sendPacket(new PacketPlayOutEntityEquipment(corpse.getId(), equipment));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                plr.hidePlayer(plugin, corpse.getBukkitEntity().getPlayer());
            }, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                plr.showPlayer(plugin, corpse.getBukkitEntity().getPlayer());
                conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
            }, 15);
        }
    }

    @Override
    public void spawnCorpses(Player player) {
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.hidePlayer(plugin, corpse.getBukkitEntity().getPlayer());
            }, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.showPlayer(plugin, corpse.getBukkitEntity().getPlayer());
                conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
            }, 15);
        }
    }

    @Override
    public void clearCorpses() {
        for(EntityPlayer corpse : corpses) {
            removeCorpse(corpse.getId());
        }
        corpses.clear();
        items.clear();
    }

    @Override
    public void removeCorpse(int entityId) {
        EntityPlayer corpse = IdToCorpse.get(entityId);
        corpses.remove(corpse);
        items.remove(corpse.getUniqueIDString());
        for(Player plr : Bukkit.getOnlinePlayers()) {
            PlayerConnection conn = ((CraftPlayer) plr).getHandle().playerConnection;
            conn.sendPacket(new PacketPlayOutEntityDestroy(corpse.getId()));
            conn.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, corpse));
        }
    }

    @Override
    public void saveCorpseData() {
        FileConfiguration cfg = plugin.getConfig();

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
            plugin.saveConfig();
        }
    }

    @Override
    public void loadCorpseData() {
        FileConfiguration cfg = plugin.getConfig();

        ConfigurationSection sec = cfg.getConfigurationSection("Corpses");
        if(sec == null)
            return;

        for(String uuid : sec.getKeys(false)) {

            double x = sec.getDouble(uuid + ".x");
            double y = sec.getDouble(uuid + ".y");
            double z = sec.getDouble(uuid + ".z");
            String w = sec.getString(uuid + ".world");
            org.bukkit.World world = Bukkit.getWorld(w);

            Location loc = new Location(world, x, y , z);

            String b64 = sec.getString(uuid + ".inventory");
            String txtr = sec.getString(uuid + ".txtr");
            String sig = sec.getString(uuid +".sig");
            String name = sec.getString(uuid + ".name");

            Inventory inv = fromBase64(b64);

            createCorpse(uuid, txtr, sig, name, loc, inv);
            sec.set(uuid, null);
        }
        plugin.saveConfig();
    }

    @Override
    @EventHandler
    public void onPlayerInteractCorpseEvent(PlayerInteractCorpseEvent event) {
        if(IdToCorpse.containsKey(event.getCorpseID())){
            EntityPlayer corpse = IdToCorpse.get(event.getCorpseID());
            Inventory inv = items.get(corpse.getUniqueIDString());
            if(inv == null)
                return;
            event.getPlayer().openInventory(inv);
        }
    }

    private boolean isInvEmpty(Inventory inv) {
        boolean hasItems = false;
        for(int i = 0; i < inv.getSize(); i++) {
            if(inv.getItem(i) == null)
                continue;
            if(!inv.getItem(i).getItemMeta().equals(emptyItem.getItemMeta()))
                hasItems = true;
        }
        return !hasItems;
    }

    private EntityPlayer getCorpseFromInventory(Inventory inv) {
        for(EntityPlayer sp : corpses) {
            if(items.get(sp.getUniqueIDString()) != null && items.get(sp.getUniqueIDString()).equals(inv)) {
                return sp;
            }
        }
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(!this.items.containsValue(event.getClickedInventory()))
            return;
        if(isInvEmpty(event.getClickedInventory()))
            event.getWhoClicked().closeInventory();
        if(event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null)
            return;
        if(event.getCurrentItem().getItemMeta().equals(emptyItem.getItemMeta()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        if(items.containsValue(event.getInventory())) {
            if(isInvEmpty(event.getInventory()))
                removeCorpse(getCorpseFromInventory(event.getInventory()).getId());
        }
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
