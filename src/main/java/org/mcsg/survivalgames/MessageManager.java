package org.mcsg.survivalgames;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.util.MessageUtil;



public class MessageManager {

	public static MessageManager instance = new MessageManager();
	public String pre = ChatColor.BLUE + "" + ChatColor.BOLD + "[" + ChatColor.GOLD + "" + ChatColor.BOLD + "SG" + ChatColor.BLUE + "" + ChatColor.BOLD + "] " + ChatColor.RESET;
	private HashMap<PrefixType, String>prefix = new HashMap<PrefixType, String>();
	public enum PrefixType {

		MAIN, INFO, WARNING, ERROR;
		
	}
	
	public static MessageManager getInstance() {
		return instance;
	}
	
	public void setup() {
		FileConfiguration f = SettingsManager.getInstance().getMessageConfig();
		prefix.put(PrefixType.MAIN, MessageUtil.replaceColors(f.getString("prefix.main")));
		prefix.put(PrefixType.INFO, MessageUtil.replaceColors(f.getString("prefix.states.info")));
		prefix.put(PrefixType.WARNING, MessageUtil.replaceColors(f.getString("prefix.states.warning")));
		prefix.put(PrefixType.ERROR, MessageUtil.replaceColors(f.getString("prefix.states.error")));
		
	}
	
	
	/**
	 * SendMessage
	 * 
	 * Loads a Message from messages.yml, converts its colors and replaces vars in the form of {$var} with its correct values, 
	 * then sends to the player, adding the correct prefix 
	 *  
	 * @param type
	 * @param input
	 * @param player
	 * @param vars
	 */
	public void sendFMessage(PrefixType type, String input, Player player, String ... args) {
		CommandSender sender = (CommandSender) player;
		sendFMessage(type, input, sender, args);
	}
	
	public void sendFMessage(PrefixType type, String input, CommandSender sender, String ... args) {
		String msg = SettingsManager.getInstance().getMessageConfig().getString("messages."+input);
		boolean enabled = SettingsManager.getInstance().getMessageConfig().getBoolean("messages."+input+"_enabled", true);
		if(msg == null){sender.sendMessage(ChatColor.RED+"Failed to load message for messages."+input); return;}
		if(!enabled)return;
		if(args != null && args.length != 0){msg = MessageUtil.replaceVars(msg, args);}
		msg = MessageUtil.replaceColors(msg);
		sender.sendMessage(prefix.get(PrefixType.MAIN)+ " "+prefix.get(type)+ msg );
	}
	
	/**
	 * SendMessage
	 * 
	 * Sends a pre formated message from the plugin to a player, adding correct prefix first
	 * 
	 * @param type
	 * @param msg
	 * @param p
	 */
	
	public void sendMessage(PrefixType type, String msg, CommandSender sender){
		sender.sendMessage(prefix.get(PrefixType.MAIN)+ " "+prefix.get(type)+ msg );
	}
	
	public void logMessage(PrefixType type, String msg) {
		Logger logger = Bukkit.getServer().getLogger();
		switch (type) {
		case INFO:  logger.info(prefix.get(type)+ msg); break;
		case WARNING: logger.warning(prefix.get(type) + msg); break;
		case ERROR: logger.severe(prefix.get(type) + msg); break;
		default:
			break;
		}
	}
	
	public String getFMessage(PrefixType type, String input, String ...args ) {
		String msg = SettingsManager.getInstance().getMessageConfig().getString("messages."+input);
		if(msg == null){
			Bukkit.broadcastMessage(ChatColor.RED+"Failed to load message for messages." + input);
			return "";
		}
		if (args != null && args.length != 0) {
			msg = MessageUtil.replaceVars(msg, args);
		}
		msg = prefix.get(PrefixType.MAIN) + prefix.get(type) + " " + MessageUtil.replaceColors(msg);
		return msg;
	}

	public void broadcastFMessage(PrefixType type, String input, String ...args ) {
		if (SettingsManager.getInstance().getMessageConfig().getBoolean("messages."+input+"_enabled", true)) {
			String msg = getFMessage(type, input, args);
			Bukkit.broadcastMessage(msg);
		}
	}
	
	public void broadcastMessage(PrefixType type, String msg, Player player){
		Bukkit.broadcastMessage(prefix.get(PrefixType.MAIN)+ " "+prefix.get(type)+ " "+msg );
	}

}