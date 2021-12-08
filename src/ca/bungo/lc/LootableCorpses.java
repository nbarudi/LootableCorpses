package ca.bungo.lc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ca.bungo.lc.cmds.TestingCommand;
import ca.bungo.lc.events.CorpseEventHandler;
import ca.bungo.lc.util.CorpseController;
import ca.bungo.lc.util.FileManager;
import ca.bungo.lc.util.PacketReader;

public class LootableCorpses extends JavaPlugin {
	
	public PacketReader pr;
	public FileManager fm;
	
	@Override
	public void onEnable() {
		
		pr = new PacketReader();
		fm = new FileManager(this);
		
		registerEvents();
		registerCommands();
		registerConfigs();
		
		CorpseController.loadCorpseData();
		
		if(!Bukkit.getOnlinePlayers().isEmpty()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				pr.inject(player);
			}
		}
		
	}
	


	@Override
	public void onDisable() {
		if(!Bukkit.getOnlinePlayers().isEmpty()) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				pr.unInject(player);
			}
		}
		
		CorpseController.saveCorpseData();
		CorpseController.clearCorpses();
	}
	
	
	private void registerEvents() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new CorpseEventHandler(this), this);
		
	}

	private void registerCommands() {
		this.getCommand("test").setExecutor(new TestingCommand(this));
		
	}
	
	private void registerConfigs() {
		fm.saveConfig("corpses.yml");
	}

}
