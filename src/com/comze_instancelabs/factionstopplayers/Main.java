package com.comze_instancelabs.factionstopplayers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.FactionColls;


public class Main extends JavaPlugin implements Listener {

	
	@Override
    public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		// reset timer
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				//TODO: update top signs
				try{
					ArrayList<String> torem = new ArrayList<String>();
					if(getConfig().isSet("signs")){
						for(String p_ : getConfig().getConfigurationSection("signs.").getKeys(false)){
				        	Location t = new Location(Bukkit.getWorld(getConfig().getString("signs." + p_+ ".world")), getConfig().getDouble("signs." + p_ + ".x"), getConfig().getDouble("signs." + p_ + ".y"), getConfig().getDouble("signs." + p_ + ".z"));

				        	Sign s = null;
				        	try{
				        		s = (Sign)t.getWorld().getBlockAt(t).getState(); 	
				        	}catch (Exception e1){
				        		torem.add(p_);
				        	}
			    	    	
			                if(s != null){
			                	s.setLine(2, getTopFaction());
			                	s.update();
			                }else{
			                	torem.add(p_);
			                }
						}
						for(String rem : torem){
							getConfig().set("signs." + rem, null);
						}
					}	
				}catch(Exception e){
					for(StackTraceElement i : e.getStackTrace()){
						getLogger().info(i.toString());
					}
				}
				
			}
		}, 1200, 1200); // 60 seconds
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("factionstopplayer") || cmd.getName().equalsIgnoreCase("ftop")){
			//
			return true;
		}
		return false;
	}
	
	 
	@EventHandler
	public void onSignUse(PlayerInteractEvent event) throws IOException {
		if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getClickedBlock().getType() == Material.SIGN_POST
					|| event.getClickedBlock().getType() == Material.WALL_SIGN) {
				Sign s = (Sign) event.getClickedBlock().getState();

				for (int i = 0; i < s.getLines().length - 1; i++) {
					if (s.getLine(i).equalsIgnoreCase("§4[drakonnastop]")) {
	                	s.setLine(2, getTopFaction());
	                	s.update();
					}
				}
			}
		}
	}

	 
	static Map sortByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	 
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().contains("/factionstop")) {
			if (event.getPlayer().hasPermission("factionstopplayers.create")) {
				String x = Integer.toString(event.getBlock().getLocation().getBlockX());
				String y = Integer.toString(event.getBlock().getLocation().getBlockY());
				String z = Integer.toString(event.getBlock().getLocation().getBlockZ());
				String base = x + y + z;
				getConfig().set("signs." + base + ".world", event.getBlock().getLocation().getWorld().getName());
				getConfig().set("signs." + base + ".x", event.getBlock().getLocation().getBlockX());
				getConfig().set("signs." + base + ".y", event.getBlock().getLocation().getBlockY());
				getConfig().set("signs." + base + ".z", event.getBlock().getLocation().getBlockZ());
				this.saveConfig();
				event.setLine(0, "§4[DrakonnasTop]");
				event.setLine(1, "§3Top Faction:");
				event.setLine(2, getTopFaction());
			} else {
				event.getBlock().breakNaturally();
				p.sendMessage(ChatColor.GOLD + "[FactionsTopPlayers] "
						+ ChatColor.DARK_RED + "You don't have permission!");
			}
		}
	}
	
	public String getTopFaction(){
		try{
			LinkedHashMap<String, Double> factions = new LinkedHashMap<String, Double>();
			for (FactionColl fc : FactionColls.get().getColls())
			{
			    for (Faction f : fc.getAll())
			    {
			    	factions.put(f.getName(), f.getPower());
			        //System.out.println(f.getUniverse() + " - " + f.getName() + " - " + f.getPower());
			    }
			}
			LinkedHashMap<String, Double> factionstop = new LinkedHashMap<String, Double>(sortByValue(factions));
			String t = ""; //factionstop.entrySet().iterator().next().getKey();
			for(Entry et : factionstop.entrySet()){
				if(!factionstop.entrySet().iterator().hasNext()){
					t = (String) et.getKey();
				}
			}
			return t;
		}catch(Exception e){
			getLogger().info(e.toString());
			for(StackTraceElement i : e.getStackTrace()){
				getLogger().info(i.toString());
			}
			return "null";
		}
	}
	
}
