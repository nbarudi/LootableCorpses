package ca.bungo.lc.api;

import ca.bungo.lc.events.PlayerInteractCorpseEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

/**
 * Handles Version Control for creating Corpses and Handling Interactions
 *
 * @author Nick
 * @since 1.0
 * */
public interface CorpseCore extends Listener {


    /**
     * Creates the 'Corpse' Entity and Spawns it into the world at the Players current Location
     *
     * @param player    The player to create the 'corpse' for
     * @param inventory The Inventory that will be stored on the 'corpse'
     * */
    void createCorpse(Player player, Inventory inventory);

    /**
     * Creates the 'Corpse' Entity and Spawns it into the world at the Location Provided
     *
     * @param uuid      The UUID of the 'Corpse' Entity to create (Generally stored in a Configuration File).
     * @param txt       The Texture String for the 'Corpse' Skin (Generally stored in a Configuration File).
     * @param sig       The Signature STring for the 'Corpse' Skin (Generally stored in a Configuration File).
     * @param name      Name of the 'Corpse' Entity.
     * @param loc       Location to spawn the 'Corpse' at
     * @param inv       Inventory that will be spawned on the corpse.
     * */
    void createCorpse(String uuid, String txt, String sig, String name, Location loc, Inventory inv);

    /**
     * Spawn all stored corpses for the player.
     *
     * @param player    Player to spawn all the corpses for.
     * */
    void spawnCorpses(Player player);

    /**
     * Get a Corpse Entity based on its ID.
     *
     * @param id    The ID of the corpse you need to obtain the Entity Data from
     * @return      The Bukkit Entity of the Corpse.
     * */
    Entity getCorpseById(int id);

    /**
     * Clear all stored corpse information
     * */
    void clearCorpses();

    /**
     * Removed a Corpse from the world.
     *
     * @param entityId  Entity ID of the corpse.
     * */
    void removeCorpse(int entityId);

    /**
     * Save corpse information to a Configuration File.
     * */
    void saveCorpseData();

    /**
     * Load all corpse data on server boot to avoid constant configuration searching
     * */
    void loadCorpseData();

    /**
     * When the player interacts with one of the PacketCorpses this event is called
     * */
    @EventHandler
    void onPlayerInteractCorpseEvent(PlayerInteractCorpseEvent event);


}