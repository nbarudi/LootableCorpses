package ca.bungo.lc.versions;

import ca.bungo.lc.api.CorpseCore;
import ca.bungo.lc.events.PlayerInteractCorpseEvent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class Version1_18_1_R1 implements CorpseCore {

    private Plugin plugin;
    private ItemStack emptyItem;

    public Version1_18_1_R1(Plugin plugin, ItemStack emptyItem){
        this.plugin = plugin;
        this.emptyItem = emptyItem;
    }

    ArrayList<ServerPlayer> corpses = new ArrayList<>();
    Map<String, Inventory> items = new HashMap<>();
    Map<Integer, ServerPlayer> idToPlayer = new HashMap<>();

    @Override
    public void createCorpse(Player player, Inventory inventory) {
        ServerPlayer sPlayer = ((CraftPlayer) player).getHandle();
        Property textures = (Property) sPlayer.getGameProfile().getProperties().get("textures").toArray()[0];
        GameProfile gp = new GameProfile(UUID.randomUUID(), ChatColor.RED + "DEAD " + player.getName());
        gp.getProperties().put("textures", new Property("textures", textures.getValue(), textures.getSignature()));

        ServerPlayer corpse = new ServerPlayer(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) player.getWorld()).getHandle(), gp);

        corpse.setPos(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        corpse.setPose(Pose.SLEEPING);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new ArrayList<>();


        if(inventory.getItem(36) != null && inventory.getItem(36).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(inventory.getItem(36))));
        if(inventory.getItem(37) != null && inventory.getItem(37).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(inventory.getItem(37))));
        if(inventory.getItem(38) != null && inventory.getItem(38).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(inventory.getItem(38))));
        if(inventory.getItem(39) != null && inventory.getItem(39).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(inventory.getItem(39))));

        corpses.add(corpse);
        idToPlayer.put(corpse.getId(), corpse);
        items.put(corpse.getUUID().toString(), inventory);

        for(Player plr: Bukkit.getOnlinePlayers()){
            ServerPlayer oPlayer = ((CraftPlayer) plr).getHandle();
            ServerGamePacketListenerImpl connection = oPlayer.connection;
            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, corpse));
            connection.send(new ClientboundAddPlayerPacket(corpse));
            connection.send(new ClientboundSetEntityDataPacket(corpse.getId(), corpse.getEntityData(), true));
            connection.send(new ClientboundSetEquipmentPacket(corpse.getId(), equipment));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, corpse));
            }, 2);
        }
    }

    @Override
    public void createCorpse(String uuid, String txt, String sig, String name, Location loc, Inventory inv) {
        GameProfile gp = new GameProfile(UUID.fromString(uuid), name);
        gp.getProperties().put("textures", new Property("textures", txt, sig));

        ServerPlayer corpse = new ServerPlayer(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) loc.getWorld()).getHandle(), gp);

        corpse.setPose(Pose.SLEEPING);
        corpse.setPos(loc.getX(), loc.getY(), loc.getZ());

        corpses.add(corpse);
        idToPlayer.put(corpse.getId(), corpse);
        items.put(corpse.getUUID().toString(), inv);

        List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new ArrayList<>();
        if(inv.getItem(36) != null && inv.getItem(36).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.FEET, CraftItemStack.asNMSCopy(inv.getItem(36))));
        if(inv.getItem(37) != null && inv.getItem(37).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.LEGS, CraftItemStack.asNMSCopy(inv.getItem(37))));
        if(inv.getItem(38) != null && inv.getItem(38).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.CHEST, CraftItemStack.asNMSCopy(inv.getItem(38))));
        if(inv.getItem(39) != null && inv.getItem(39).getItemMeta() != emptyItem.getItemMeta())
            equipment.add(new Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(inv.getItem(39))));

        for(Player plr: Bukkit.getOnlinePlayers()){
            ServerPlayer oPlayer = ((CraftPlayer) plr).getHandle();
            ServerGamePacketListenerImpl connection = oPlayer.connection;
            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, corpse));
            connection.send(new ClientboundAddPlayerPacket(corpse));
            connection.send(new ClientboundSetEntityDataPacket(corpse.getId(), corpse.getEntityData(), true));
            connection.send(new ClientboundSetEquipmentPacket(corpse.getId(), equipment));

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                plr.hidePlayer(plugin, corpse.getBukkitEntity().getPlayer());
            },1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                plr.showPlayer(plugin, corpse.getBukkitEntity().getPlayer());
                connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, corpse));
            },15);
        }
    }

    @Override
    public void spawnCorpses(Player player) {

        for(ServerPlayer sp : corpses){
            player.sendMessage(ChatColor.GOLD + "Spawning Corpse: " + sp.displayName + " With ID of: " + sp.getId());
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, sp));
            connection.send(new ClientboundAddPlayerPacket(sp));
            connection.send(new ClientboundSetEntityDataPacket(sp.getId(), sp.getEntityData(), true));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.hidePlayer(plugin, sp.getBukkitEntity().getPlayer());
            },1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                player.showPlayer(plugin, sp.getBukkitEntity().getPlayer());
                connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, sp));
            },15);

        }
    }

    @Override
    public Entity getCorpseById(int id){
        return this.idToPlayer.get(id).getBukkitEntity();
    }

    private void removeCorpsePacket(ServerPlayer corpse){
        for(Player player : Bukkit.getOnlinePlayers()){
            ((CraftPlayer)player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(corpse.getId()));
        }
    }

    @Override
    public void clearCorpses() {
        for(ServerPlayer corpse : corpses) {
            removeCorpsePacket(corpse);
        }
        corpses.clear();
        items.clear();
    }

    @Override
    public void removeCorpse(int entityId) {
        ServerPlayer corpse = idToPlayer.get(entityId);
        items.remove(corpse.getUUID().toString());
        corpses.remove(corpse);
        idToPlayer.remove(entityId);
        removeCorpsePacket(corpse);
    }

    @Override
    public void saveCorpseData() {
        FileConfiguration cfg = plugin.getConfig();

        ConfigurationSection sec = cfg.getConfigurationSection("Corpses");
        if(sec == null)
            sec = cfg.createSection("Corpses");

        for(ServerPlayer corpse : corpses) {
            Location loc = corpse.getBukkitEntity().getLocation();
            String uuid = corpse.getStringUUID();
            Property textures = (Property) corpse.getGameProfile().getProperties().get("textures").toArray()[0];
            sec.set(uuid + ".world", loc.getWorld().getName());
            sec.set(uuid + ".x", loc.getX());
            sec.set(uuid + ".y", loc.getY());
            sec.set(uuid + ".z", loc.getZ());
            sec.set(uuid + ".name", corpse.getBukkitEntity().getName());
            sec.set(uuid + ".txtr", textures.getValue());
            sec.set(uuid + ".sig", textures.getSignature());
            sec.set(uuid + ".inventory", toBase64(items.get(corpse.getStringUUID())));
        }
        plugin.saveConfig();
        clearCorpses();
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
        plugin.saveConfig();
    }

    //Helper Functions Because Storing Inventory Data Between Reloads / Restarts Sucks.
    private String toBase64(Inventory inventory) {
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

    private Inventory fromBase64(String base64) {
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

    @Override
    @EventHandler
    public void onPlayerInteractCorpseEvent(PlayerInteractCorpseEvent event) {
        if(event.isCancelled())
            return;
        if(this.idToPlayer.containsKey(event.getCorpseID())){
            ServerPlayer corpse = this.idToPlayer.get(event.getCorpseID());
            Player player = event.getPlayer();
            String uuid = corpse.getStringUUID();
            Inventory inventory = this.items.get(uuid);
            if(inventory == null)
                return;
            player.openInventory(inventory);
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

    private ServerPlayer getCorpseFromInventory(Inventory inv) {
        for(ServerPlayer sp : corpses) {
            if(items.get(sp.getStringUUID()) != null && items.get(sp.getStringUUID()).equals(inv)) {
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
}
