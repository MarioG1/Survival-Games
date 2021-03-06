package org.mcsg.survivalgames;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mcsg.survivalgames.lobbysigns.LobbySign;
import org.mcsg.survivalgames.lobbysigns.LobbySignManager;
import org.mcsg.survivalgames.lobbysigns.LobbySignType;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinner;
import org.mcsg.survivalgames.lobbysigns.LobbySignWinnerSign;

public class LobbyManager {

	private static LobbyManager instance = null;
	public static HashSet < Chunk > lobbychunks = new HashSet < Chunk > ();
	LobbySignManager signManager = null;
	
	private LobbyManager(LobbySignManager signManager) {
		this.signManager = signManager;
		signManager.loadSigns();
		updateAll();
	}
	
	public static void createInstance(LobbySignManager signManager) {
		instance = new LobbyManager(signManager);
	}

	public static LobbyManager getInstance() {
		return instance;
	}

	public void updateAll() {
		signManager.updateSigns();
	}

	public void updateWall(int gameId) {
		signManager.updateSigns(gameId);
	}

	public void removeSignsForArena(int arena) {
		signManager.removeArena(arena);
	}
	
	public void gameEnd(int gameID, Player winner) {
		final Location loc = winner.getLocation();
		launchEndFireworks(loc);
				
		List<LobbySign> winnerSign = signManager.getSignsByType(gameID, LobbySignType.Winner);
		for (LobbySign sign : winnerSign) {
			
			if (!(sign instanceof LobbySignWinner))
				continue;
			
			((LobbySignWinner)sign).setWinner(winner.getName());
			sign.update();
		}

        List<LobbySign> winnerSigns = signManager.getSignsByType(gameID, LobbySignType.WinnerSign);
        for (LobbySign sign : winnerSigns) {
            ((LobbySignWinnerSign)sign).setWinner(winner.getName());
            sign.update();
        }
		
	}
	
	public static void launchEndFireworks(final Location loc) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
			public void run() {
				FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 0, new Color[]{Color.WHITE, Color.BLUE, Color.SILVER}, false, false, 1, 3);
				FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BURST, 2, new Color[]{Color.ORANGE, Color.RED, Color.WHITE}, true, true, 10, 0);
				FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.STAR, 1, new Color[]{Color.RED, Color.YELLOW}, true, true, 10, 0);
				FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 1, new Color[]{Color.SILVER, Color.RED}, true, true, 10, 0);
				FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BALL_LARGE, 0, new Color[]{Color.YELLOW, Color.SILVER}, true, true, 20, 0);
				//FireworkFactory.LaunchFirework(loc, FireworkEffect.Type.BURST, 1, Color.FUCHSIA);
			}
		}, 15);
	}
}