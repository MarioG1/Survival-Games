package org.mcsg.survivalgames.commands;

import org.bukkit.entity.Player;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.Game.GameMode;

public class Kit implements SubCommand {
	   public boolean onCommand(Player player, String[] args) {
	        if (!player.hasPermission(permission()) && !player.isOp()) {
	            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
	            return false;
	        }
	        int game = GameManager.getInstance().getPlayerGameId(player);
	        if(game == -1){
	            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "You can only use this command in a game.", player);
	            return true;
	        }

	        if (GameManager.getInstance().getGame(game).getMode() == GameMode.WAITING) {
		        GameManager.getInstance().getGame(game).showMenu(player);
	        } else {
	            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "You cannot select a kit now.", player);
	        }

	        return true;
	    }
	    
	    @Override
	    public String help(Player p) {
	        return "/sg kit - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.kit", "Select a kit for the game");
	    }

		@Override
		public String permission() {
			return "sg.arena.kit";
		}

}
