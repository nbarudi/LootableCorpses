package ca.bungo.lc.util;

import org.bukkit.Bukkit;

public class ReflectionUtility {
	
	//net.minecraft.server.v1_16_R3
	//org.bukkit.craftbukkit.v1_16_R3
	
	public static Class<?> getNMSClass(String path) {
		String ver = Bukkit.getServer().getClass().getPackage().getName().replace('.', ',').split(",") [3] + "." ;
		String name = "net.minecraft.server." + ver + "." + path;
		try {
			Class<?> NMSClass = Class.forName(name);
			return NMSClass;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Class<?> getCBClass(String path) {
		String ver = Bukkit.getServer().getClass().getPackage().getName().replace('.', ',').split(",") [3] + "." ;
		String name = "org.bukkit.craftbukkit." + ver + "." + path;
		try {
			Class<?> CBClass = Class.forName(name);
			return CBClass;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
