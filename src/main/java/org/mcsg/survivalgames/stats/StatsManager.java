package org.mcsg.survivalgames.stats;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mcsg.survivalgames.Game;
import org.mcsg.survivalgames.GameManager;
import org.mcsg.survivalgames.MessageManager;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;
import org.mcsg.survivalgames.MessageManager.PrefixType;
import org.mcsg.survivalgames.util.DatabaseManager;



public class StatsManager {

    private static StatsManager instance = new StatsManager();
    
    private ArrayList<PreparedStatement> queue = new ArrayList<PreparedStatement>();
    private DatabaseDumper dumper = new DatabaseDumper();
    private DatabaseManager dbman = DatabaseManager.getInstance();
    private HashMap<Integer, HashMap<UUID, PlayerStatsSession>>arenas  = new HashMap<Integer, HashMap<UUID, PlayerStatsSession>>();
    private boolean enabled = true;
    MessageManager msgmgr;

    private StatsManager(){
    	msgmgr = MessageManager.getInstance();;
    }

    public static StatsManager getInstance(){
        return instance;
    }

    public void setup(Plugin p, boolean b){
        enabled = b;
        if(b){
        	PreparedStatement s = null;
        	PreparedStatement s1 = null;
        	ResultSet tables = null;
        	ResultSet tables1 = null;
        	try{
                s = dbman.createStatement(" CREATE TABLE "+SettingsManager.getSqlPrefix() + 
                        "playerstats(id int NOT NULL AUTO_INCREMENT PRIMARY KEY, gameno int,arenaid int, player text, points int,position int," +
                        " kills int, death int, killed text,time int, ks1 int, ks2 int,ks3 int, ks4 int, ks5 int)");

                s1 = dbman.createStatement(" CREATE TABLE "+SettingsManager.getSqlPrefix() + 
                        "gamestats(gameno int NOT NULL AUTO_INCREMENT PRIMARY KEY, arenaid int, players int, winner text, time int )");


                DatabaseMetaData dbm = dbman.getMysqlConnection().getMetaData();
                tables = dbm.getTables(null, null, SettingsManager.getSqlPrefix()+"playerstats", null);
                tables1 = dbm.getTables(null, null, SettingsManager.getSqlPrefix()+"gamestats", null);

                if (tables.next()) { }
                else {
                    s.execute();
                }
                if (tables1.next()) { }
                else {
                    s1.execute();
                }
    		}catch(Exception e){
        			e.printStackTrace();
    		} finally {
    			try {
    				if (tables != null) { tables.close(); tables = null; }
    				if (tables1 != null) { tables1.close(); tables1 = null; }
    				if (s != null) { s.close(); s = null; }
    				if (s1 != null) { s1.close(); s1 = null; }
    			} catch (SQLException e) {
    				System.out.println("ERROR: Failed to close PreparedStatements or ResultSets!");
    				e.printStackTrace();
    			}
    		}
        }
    }

    public void addArena(int arenaid){
        arenas.put(arenaid, new HashMap<UUID, PlayerStatsSession>());
    }



    public void addPlayer(Player p, int arenaid){
        arenas.get(arenaid).put(p.getUniqueId(), new PlayerStatsSession(p, arenaid));
    }

    public void removePlayer(Player p, int id){
        arenas.get(id).remove(p.getUniqueId());
    }

    public void playerDied(Player p, int pos, int arenaid,long time){
        /*    System.out.println("player null "+(p == null));
        System.out.println("arena null "+(arenas == null));
        System.out.println("arenagetplayer null "+(arenas.get(arenaid).get(p) == null));*/

        arenas.get(arenaid).get(p.getUniqueId()).died(pos, time);
    }

    public void playerWin(Player p, int arenaid, long time){
        arenas.get(arenaid).get(p.getUniqueId()).win(time);
    }
    
    public void outputStatsDebug(CommandSender sender) {
    	GameManager gm = GameManager.getInstance();
    	for (Integer a : arenas.keySet()) {
    		sender.sendMessage(ChatColor.YELLOW + "Arena: " + a + " - " + gm.getGame(a).getName());
    		for (UUID u : arenas.get(a).keySet()) {
    			PlayerStatsSession pss = arenas.get(a).get(u);
    			sender.sendMessage(ChatColor.GOLD + "  + " + u + " => " + pss.pdispname);
    		}
    	}
    }

    public void addKill(Player p, Player killed, int arenaid, String name){
        PlayerStatsSession s = arenas.get(arenaid).get(p.getUniqueId());

        int kslevel = s.addKill(killed);
        if(kslevel > 3){
        	msgmgr.broadcastFMessage(PrefixType.INFO, "killstreak.level"+((kslevel>5)?5:kslevel), "player-"+p.getName(), "arenaname-"+name);
        }
        else if(kslevel > 0){
            for (Player pl : GameManager.getInstance().getGame(arenaid).getAllPlayers()) {
            	msgmgr.sendFMessage(PrefixType.INFO, "killstreak.level"+((kslevel>5)?5:kslevel), pl, "player-"+p.getName(), "arenaname-"+name);
            }
        }
    }

    public void saveGame(int arenaid, Player winner,int players, long time ){
        if(!enabled)return;
        int gameno = 0;
        Game g = GameManager.getInstance().getGame(arenaid);

        PreparedStatement s2 = null;
        ResultSet rs = null;
        try {
            long time1 = new Date().getTime();
            s2 = dbman.createStatement("SELECT * FROM "+SettingsManager.getSqlPrefix() + 
                    "gamestats ORDER BY gameno DESC LIMIT 1");
            rs = s2.executeQuery();
            rs.next();
            gameno = rs.getInt(1) + 1;

            if(time1 + 5000 < new Date().getTime())System.out.println("Your database took a long time to respond. Check the connection between the server and database");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            g.setRBStatus("Error: getno");
		} finally {
			try {
				if (rs != null) { rs.close(); rs = null; }
				if (s2 != null) { s2.close(); s2 = null; }
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
				e.printStackTrace();
			}
		}

        addSQL("INSERT INTO "+SettingsManager.getSqlPrefix()+"gamestats VALUES(NULL,"+arenaid+","+players+",'"+winner.getName()+"',"+time+")");

        for(PlayerStatsSession s:arenas.get(arenaid).values()){
            s.setGameID(gameno);
            addSQL(s.createQuery());
        }
        arenas.get(arenaid).clear();
    }

    private void addSQL(String query){
        addSQL( dbman.createStatement(query));
    }

    private void addSQL(PreparedStatement s){
        queue.add(s);
        if(!dumper.isAlive()){
            dumper = new DatabaseDumper();
            dumper.start();
        }
    }

    class DatabaseDumper extends Thread{
        public void run(){
            while(queue.size()>0){
                PreparedStatement s = queue.remove(0);
                try{
                    s.execute();
                }
                catch(Exception e) {
                	dbman.connect();
        		} finally {
        			try {
        				if (s != null) { s.close(); s = null; }
        			} catch (SQLException e) {
        				System.out.println("ERROR: Failed to close PreparedStatement or ResultSet!");
        				e.printStackTrace();
        			}
        		}
            }
        }
    }
}