package com.comze_instancelabs.factionstopplayers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.UPlayer;


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
			                	if(s.getLine(0).equalsIgnoreCase("§4[factionstop]")){
			                		if(s.getLine(1).equalsIgnoreCase("§1top faction:")){
				                		s.setLine(2, "§f" + getTopFaction());
					                	s.update();		
			                		}else if(s.getLine(1).equalsIgnoreCase("§1player info:")){
			                			// do not update player info
			                		}else{
			                			s.setLine(2, "§f" + getTopFaction());
					                	s.update();	
			                		}
				                	
			                	}else{
			                		torem.add(p_);
			                	}
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
		}, 2*1200, 2*1200); // 2x 60 seconds = 2 mins
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("factionstopplayer") || cmd.getName().equalsIgnoreCase("ftop")){
			sender.sendMessage("§2 You can create signs in the following format:");
			sender.sendMessage("§2 1st line: §6/factionstop");
			sender.sendMessage("§2 2nd line: §6faction");
			sender.sendMessage("§2 or");
			sender.sendMessage("§2 1st line: §6/factionstop");
			sender.sendMessage("§2 2nd line: §6player");
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
				if (s.getLine(0).equalsIgnoreCase("§4[factionstop]")) {
					if(s.getLine(1).equalsIgnoreCase("§1top faction:")){
						s.setLine(2, "§f" + getTopFaction());
	                	s.update();	
					}else if(s.getLine(1).equalsIgnoreCase("§1player info:")){
						s.setLine(2, "§f" + event.getPlayer().getName());
						s.setLine(3, "§f" + getPlayer(event.getPlayer().getName()));
	                	s.update();
					}
				}
			}
		}
	}

	 
	public LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
		   List mapKeys = new ArrayList(passedMap.keySet());
		   List mapValues = new ArrayList(passedMap.values());
		   Collections.sort(mapValues);
		   Collections.sort(mapKeys);

		   LinkedHashMap sortedMap = new LinkedHashMap();

		   Iterator valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = passedMap.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put((String)key, (Double)val);
		               break;
		           }

		       }

		   }
		   return sortedMap;
		}
	 
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().contains("/factionstop")) {
			if (event.getPlayer().hasPermission("factionstopplayers.create")) {				
				if(event.getLine(1).equalsIgnoreCase("faction")){
					event.setLine(0, "§4[FactionsTop]");
					event.setLine(1, "§1Top Faction:");
					event.setLine(2, "§f" + getTopFaction());
				}else if(event.getLine(1).equalsIgnoreCase("player")){
					event.setLine(0, "§4[FactionsTop]");
					event.setLine(1, "§1Player Info:");
					event.setLine(2, "§f" + event.getPlayer().getName());
					event.setLine(3, "§f" + getPlayer(event.getPlayer().getName()));
					return;
				}else{
					event.setLine(0, "§4[FactionsTop]");
					event.setLine(1, "§1Top Faction:");
					event.setLine(2, "§f" + getTopFaction());
				}
				
				String x = Integer.toString(event.getBlock().getLocation().getBlockX());
				String y = Integer.toString(event.getBlock().getLocation().getBlockY());
				String z = Integer.toString(event.getBlock().getLocation().getBlockZ());
				String base = x + y + z;
				getConfig().set("signs." + base + ".world", event.getBlock().getLocation().getWorld().getName());
				getConfig().set("signs." + base + ".x", event.getBlock().getLocation().getBlockX());
				getConfig().set("signs." + base + ".y", event.getBlock().getLocation().getBlockY());
				getConfig().set("signs." + base + ".z", event.getBlock().getLocation().getBlockZ());
				this.saveConfig();
			} else {
				event.getBlock().breakNaturally();
				p.sendMessage(ChatColor.GOLD + "[FactionsTopPlayers] "
						+ ChatColor.DARK_RED + "You don't have permission!");
			}
		}
	}
	
	public String getPlayer(String player){
		String ret = "null";
		try{	
			UPlayer p = UPlayer.get(player);
			ret = "Power: " +  Double.toString(Math.round(p.getPower()*100)/100.0d);
		}catch(Exception e){
			System.out.println(e.getMessage());
			for(StackTraceElement m : e.getStackTrace()){
				System.out.println(m.toString());
			}
		}
		return ret;
	}
	
	public String getTopFaction(){
		try{
			LinkedHashMap<String, Double> factions = new LinkedHashMap<String, Double>();
			for (FactionColl fc : FactionColls.get().getColls())
			{
			    for (Faction f : fc.getAll())
			    {
			    	//getServer().broadcastMessage(f.getName() + " " + Double.toString(f.getPower()));
			    	//getLogger().info(f.getName() + " " + Double.toString(f.getPower()));
			    	factions.put(f.getName(), f.getPower());
			    }
			}
			LinkedHashMap<String, Double> factionstop = this.sortHashMapByValuesD(factions);
			//getLogger().info(Integer.toString(factionstop.size()));
			String t = ""; //factionstop.entrySet().iterator().next().getKey();

			final Set<Entry<String, Double>> mapValues = factionstop.entrySet();
		    final int maplength = mapValues.size();
		    final Entry<String,Double>[] test = new Entry[maplength];
		    mapValues.toArray(test);

		    //System.out.print("Last Key:"+test[maplength-1].getKey());
		    //System.out.println("Last Value:"+test[maplength-1].getValue());
		    
		    t = test[maplength-4].getKey();
		    
			/*for (Entry<String, Double> entry : factionstop.entrySet()){
				if(!factionstop.entrySet().iterator().hasNext()){
					getServer().broadcastMessage(entry.getKey() + " " + Double.toString(entry.getValue()));
			    	getLogger().info(entry.getKey() + " " + Double.toString(entry.getValue()));
					t = entry.getKey();
				}
			}*/
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
