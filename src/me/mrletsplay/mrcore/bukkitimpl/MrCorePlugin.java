package me.mrletsplay.mrcore.bukkitimpl;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrletsplay.mrcore.bukkitimpl.ChatUI.UIListener;
import me.mrletsplay.mrcore.bukkitimpl.GUIUtils.GUIListener;
import me.mrletsplay.mrcore.config.CustomConfig;
import me.mrletsplay.mrcore.main.ExtensionLoader;
import me.mrletsplay.mrcore.main.MrCore;

public class MrCorePlugin extends JavaPlugin{
	
	private static CustomConfig config;
	public static JavaPlugin pl;
	
	@Override
	public void onEnable() {
		pl = this;
		getLogger().info("And MrCore is on board as well! :wave:");
		Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
		getCommand("mrcoreui").setExecutor(new UIListener());
		config = new CustomConfig(new File(getDataFolder(), "config.yml")).loadConfigSafely();
		if(config.getBoolean("versioning.check-update", true, true)) {
			String version = config.getString("versioning.version-to-use", "latest", true);
			MrCoreUpdateChecker.checkForUpdate(version);
		}
		config.saveConfigSafely();
		
		try {
			ExtensionLoader.loadExtension(new File("D:/Testserver/Testserver Spigot 1.8 - Kopie - Kopie/plugins/MrCore_BukkitImpl/extensions/EI2.jar"));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		//Close all GUIs because them staying open would cause bugs
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() != null) {
				Inventory inv = p.getOpenInventory().getTopInventory();
				if(GUIUtils.getGUI(inv) != null) p.closeInventory();
			}
		}
		getLogger().info("Goodbye");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("mrcore")) {
			sender.sendMessage("MrCore version "+MrCore.getVersion());
			return true;
		}
		return true;
	}
	
}
