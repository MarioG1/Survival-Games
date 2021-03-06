package org.mcsg.survivalgames.util;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.mcsg.survivalgames.SettingsManager;
import org.mcsg.survivalgames.SurvivalGames;

public class ChestRatioStorage {

	public static ChestRatioStorage instance = new ChestRatioStorage();
	private ArrayList<Chest> chests = new ArrayList<Chest>();

	private ChestRatioStorage() { 
		
	}

	public static ChestRatioStorage getInstance(){
		return instance;
	}
	
	public void setup(){

		File chestFile = SettingsManager.getInstance().getChestFile();

		try {
			
			JSONParser parser = new JSONParser();			
			JSONObject root = (JSONObject)parser.parse(new FileReader(chestFile));
			
			JSONArray jsonChests = (JSONArray)root.get("chests");
			for (Object chestObject : jsonChests) {
				
				JSONObject chest = (JSONObject)chestObject;
				double chance = (Double) chest.get("chance");
				SurvivalGames.$("Loading chest (" + chance + "):");
				
				ArrayList<ItemStack> chestContents = new ArrayList<ItemStack>();
				JSONArray contents = (JSONArray) chest.get("items");
				for (Object itemObject : contents) {
					ItemStack item = parseChestItem((JSONObject)itemObject);	
					if (item != null) {
						chestContents.add(item);
					}
				}				
				
				Chest newChest = new Chest();
				newChest.setChance(chance);
				newChest.setContents(chestContents);			
				this.chests.add(newChest);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	private ItemStack parseChestItem(JSONObject itemObject) {
		
		// We have to get a valid material. If we don't output an error and return null
		Material itemMaterial = Material.AIR;
		try {
			if (!itemObject.containsKey("Material")) {
				SurvivalGames.$(Level.SEVERE, "Item in chest does not have required material parameter!");
				return null;
			}
			
			itemMaterial = Material.valueOf((String)itemObject.get("Material"));
			
		} catch(Exception ex) {
			SurvivalGames.$(Level.SEVERE, "Item \""+ (String)itemObject.get("Material") + "\" does not have required material parameter!");
			return null;
		}
		
		// amount if specified
		Long stackSize = 1L;
				
		if (itemObject.containsKey("Amount")) {
			stackSize = (Long)itemObject.get("Amount");
		}
		
		// Create the item stack.
		ItemStack item = new ItemStack(itemMaterial, stackSize.intValue());
		
		// Get the meta data so we can update it
		ItemMeta meta = item.getItemMeta();
		String dispname = "";
		
		// Try and set the items name
		if (itemObject.containsKey("Name")) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String)itemObject.get("Name")));
			dispname = " \"" + meta.getDisplayName() + "\"";
		}

		SurvivalGames.$("  Chest item (" + stackSize + "): " + itemMaterial + dispname);
		
		if (itemObject.containsKey("Damage")) {
			Long damageValue = (Long)itemObject.get("Damage");
			short maxDamage = itemMaterial.getMaxDurability();
			
			short actualDurability = (short) (((float)maxDamage) * (damageValue.floatValue() / 100.0f));
			item.setDurability(actualDurability);
			
			SurvivalGames.$("    Damage: " + damageValue + " / " + maxDamage + " (" + actualDurability + ")");
		}
		
		if (itemObject.containsKey("Data")) {
			Long dataValue = (Long)itemObject.get("Data");
			item.setDurability(dataValue.shortValue());
			SurvivalGames.$("    Data: " + dataValue);
		}		
		/////////////////////////////////////////
		
		// Set the item lore
		if (itemObject.containsKey("Lore")) {
			ArrayList<String> lore = new ArrayList<String>();
			
			JSONArray loreArray = (JSONArray)itemObject.get("Lore");
			for (Object loreObject : loreArray) {
				String loreLine = ChatColor.translateAlternateColorCodes('&', (String)loreObject);
				lore.add(loreLine);
				SurvivalGames.$("    Lore: " + loreLine);
			}
			
			if (!lore.isEmpty()) {
				meta.setLore(lore);
			}
		}
		/////////////////////////////////////////
		
		// Enchantment's
		if (itemObject.containsKey("Enchantments")) {
			JSONArray enchantmentArray = (JSONArray)itemObject.get("Enchantments");
			for (Object enchantmentObject : enchantmentArray) {
				JSONObject jsonEnchantment = (JSONObject)enchantmentObject;
				
				String enchantmentName = (String)jsonEnchantment.get("Name");
				Long enchantmentLevel = (Long)jsonEnchantment.get("Level");
				
				Enchantment enchantment = Enchantment.getByName(enchantmentName);
				if (enchantment != null && enchantmentLevel != null) {
					SurvivalGames.$("    Enchantment (Lvl " + enchantmentLevel + "): " + enchantmentName);
					meta.addEnchant(enchantment, enchantmentLevel.intValue(), true);
				}
			}
		}
		/////////////////////////////////////////

		item.setItemMeta(meta);
		return item;
	}

	public ArrayList<ItemStack> getItems() {
		
		Random random = new Random();
		int noofItems = random.nextInt(5) + 1;
		
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		
		int loopSafty = 200 / chests.size();
		while (noofItems != 0) {
			Chest chestToUse = null;
			while (chestToUse == null) {
				if (loopSafty <= 0) {
					chestToUse = chests.get(random.nextInt(chests.size()));
					break;
				}
				
				for (Chest chest : chests) {
					if (chest.useThisChest(random)) {
						chestToUse = chest;
					}
				}
				loopSafty--;
			}
			
			items.add(chestToUse.getRandomItem(random));
			
			noofItems--;
		}
		
		return items;
	}

}