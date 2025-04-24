package stackResizer;

import necesse.engine.GameEventListener;
import necesse.engine.GameEvents;
import necesse.engine.GameLog;
import necesse.engine.GlobalData;
import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.events.ServerClientConnectedEvent;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.registries.PacketRegistry;
import necesse.engine.state.MainGame;
import necesse.engine.state.MainMenu;
import necesse.engine.state.State;
import necesse.engine.util.GameUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.DataFormatException;

import necesse.inventory.item.Item;
import necesse.inventory.item.ItemAttackerWeaponItem;
import necesse.inventory.item.ItemCategory;
import stackResizer.network.PacketSyncStackresizer;
import stackResizer.network.SyncType;


@ModEntry
public class StackResizer {
	
	private static final Class<?>[] BASE_CLASS_BLACKLIST = {
		    necesse.inventory.item.mountItem.MountItem.class,
		    necesse.inventory.item.trinketItem.TrinketItem.class,
		    necesse.inventory.item.trinketItem.BlinkScepterTrinketItem.class,
		    necesse.inventory.item.trinketItem.CactusShieldTrinketItem.class,
		    necesse.inventory.item.trinketItem.CalmingMinersBouquetTrinketItem.class,
		    necesse.inventory.item.trinketItem.CrystalShieldTrinketItem.class,
		    
		    necesse.inventory.item.trinketItem.CombinedTrinketItem.class,
		    necesse.inventory.item.trinketItem.FoolsGambitTrinketItem.class,
		    necesse.inventory.item.trinketItem.ForceOfWindTrinketItem.class,
		    necesse.inventory.item.trinketItem.GhostBootsTrinketItem.class,
		    necesse.inventory.item.trinketItem.HoverBootsTrinketItem.class,
		    necesse.inventory.item.trinketItem.KineticBootsTrinketItem.class,
		    necesse.inventory.item.trinketItem.LeatherDashersTrinketItem.class,
		    necesse.inventory.item.trinketItem.MinersBouquetTrinketItem.class,
		    necesse.inventory.item.trinketItem.ShieldTrinketItem.class,
		    necesse.inventory.item.trinketItem.SimpleTrinketItem.class,
		    necesse.inventory.item.trinketItem.SiphonShieldTrinketItem.class,
		    necesse.inventory.item.trinketItem.WindBootsTrinketItem.class,
		    necesse.inventory.item.trinketItem.WoodShieldTrinketItem.class,
		    necesse.inventory.item.trinketItem.ZephyrBootsTrinketItem.class,
		    
		    necesse.inventory.item.miscItem.AmmoPouch.class,
		    necesse.inventory.item.miscItem.AmmoBag.class,
		    necesse.inventory.item.miscItem.CoinPouch.class,
		    necesse.inventory.item.miscItem.Lunchbox.class,
		    necesse.inventory.item.miscItem.EnchantingScrollItem.class,
		    necesse.inventory.item.miscItem.GatewayTabletItem.class,
		    necesse.inventory.item.miscItem.PotionBag.class,
		    necesse.inventory.item.miscItem.PotionPouch.class,
		    necesse.inventory.item.miscItem.VoidPouchItem.class,
		    necesse.inventory.item.armorItem.ArmorItem.class,
		    necesse.inventory.item.toolItem.ToolItem.class
		    
		};
	private static String currentWorldName;
	private static SRSettings currentSettings;	
    private static LoadedMod currentModInstance;
	private static Server currentServer;

    // Regular oops. Straight to log.
    public static void oops(String how) {
        GameLog.out.println(SRSettings.SRModName +": "+how);
    }

    // Debug oops. To log if debug_state
    public static void dbg_oops(String how) {
        if (getDebugState()) oops(how);
    }
    
    // In-game oops. To in-game chat AND log.
    public static void ig_oops(String string) {    	
    	if((necesse.engine.GlobalData.getCurrentState() instanceof MainGame)) {
    		((MainGame)necesse.engine.GlobalData.getCurrentState()).getClient().chat.addMessage(SRSettings.SRModName +": "+ string);
    	}
    	oops(string);
	}
    
    public void init() {
    	currentModInstance = LoadedMod.getRunningMod();  	
    	this.registerCommands();
    	
    	PacketRegistry.registerPacket(PacketSyncStackresizer.class);
    	if(GlobalData.isServer()) {
    	
    		class ConnectedSyncEvent extends GameEventListener<ServerClientConnectedEvent>{			
		
				@Override
				public void onEvent(ServerClientConnectedEvent arg0) {
					arg0.client.sendPacket(new PacketSyncStackresizer(SyncType.ALL));
				}			
    			
    		}
    		ConnectedSyncEvent h = new ConnectedSyncEvent();
    		GameEvents.addListener(ServerClientConnectedEvent.class, h);
    	}
        oops("Increased Stack Size+ loaded.");    
    }
    
    
    public static void worldStartEvent(String string, Server server) {
    	StackResizer.currentServer = server;
    	currentWorldName = string;
    	dbg_oops("World start event triggered.");    
    	
    
	    	try {    		
	    		dbg_oops("Loading world settings for "+string);
				StackResizer.currentSettings = SRSettings.fromWorldName(getCurrentWorld());	
			} catch (IOException | DataFormatException e) {			
				dbg_oops("Problem loading world settings for "+getCurrentWorld());
				e.printStackTrace();			
			}
	    	ensureBaseBlacklist();		
    	
	}
    
    public static void reloadSettings() {
    	dbg_oops("Reloading current world settings...");
    	
	    	try {
				currentSettings.reloadSettings();
			} catch (IOException | DataFormatException e) {
				dbg_oops("Problem reloading world settings: " +e.getMessage());
				e.printStackTrace();
			}
	    	ensureBaseBlacklist();
    	
	}
    
    public static void serverSaveEvent() {
    	
    	
	    	dbg_oops("Server save event triggered.");    	
	    	dbg_oops("Saving mod settings at "+getCurrentSettings().savePath());
			try {
				getCurrentSettings().save();
			} catch (IOException e) {
				dbg_oops("Failed to save: "+e.getMessage());
				e.printStackTrace();
			}
    	
	}
    
    public static Client getGameClient() {    	
    	
        State currentState = necesse.engine.GlobalData.getCurrentState();
        
        
        return currentState instanceof MainGame ? ((MainGame) currentState).getClient() : ((MainMenu) currentState).getClient();
    }  
    
    // Check if the current client is the owner or the server itself
    public static boolean clientIsOwnerAuth() {
        Client c_c = getGameClient();
        return c_c.isSingleplayer() || ( (c_c.getPermissionLevel() == PermissionLevel.OWNER) || 
               (c_c.getPermissionLevel() == PermissionLevel.SERVER) );
    }       
        
    public static void ensureBaseBlacklist() {    	
    	
    	for (Class<?> clazz : BASE_CLASS_BLACKLIST) {
    		getCurrentSettings().classBlacklist.add(clazz);
    	}    	
    	dbg_oops("Base blacklist items added.");
    }
    
    public void registerCommands() {    	
    	SRCommandHandler.buildAutocompletes();
        CommandsManager.registerServerCommand(new SRCommandHandler.StackSizeCommand());
    }    
     
    public static void setDebugState(boolean val) {
    	getCurrentSettings().debug_state = val;
    }
  
    public static int addItemToBlacklist(String item) { 
    	boolean itemExists = ItemRegistry.itemExists(item);
        if (!itemExists) return -1;
        getCurrentSettings().itemBlacklist.add(item);
        syncClients();
        return ItemRegistry.getItemID(item);
    }
    
    public static void setAddTooltips(boolean value) {
    	getCurrentSettings().add_stacksize_tooltips = value;
    }
    
    public static boolean getAddTooltips() {
    	return getCurrentSettings().add_stacksize_tooltips;
    }
    
    public static void syncClients() {
    	if(GlobalData.isServer() && currentServer != null) {
    		currentServer.network.sendToAllClients(new PacketSyncStackresizer(SyncType.ALL));
    	}
    	
    }
 
    public static int addClassToBlacklist(String clzz) {
    	Class<?> clazz;
		try {
			clazz = Class.forName(clzz);
		} catch (ClassNotFoundException e) {
			return -1;
		}		
        return addClassToBlacklist(clazz);
    	
    }
    
    public static int addClassToBlacklist(Class<?> clzz) {
        int r =  getCurrentSettings().classBlacklist.add(clzz) ? 1 : -1;
        syncClients();
        return r;
    }

    public static int removeItemFromBlacklist(String item) {
        int r = getCurrentSettings().itemBlacklist.remove(item) ? 1 : -1;
        syncClients();
        return r;
    }
    
    public static int removeClassFromBlacklist(Class<?> clzz) {
    	int r = getCurrentSettings().classBlacklist.remove(clzz) ? 1 : -1;
    	 syncClients();
         return r;
    }
    
    public static int removeClassFromBlacklist(String clazz) {
    	Class<?> clzz;
		try {
			clzz = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			return -1;
		}
		 syncClients();
		return removeClassFromBlacklist(clzz);
    }
    
    public static boolean itemIsCategory(String it) {
    	return (it.contains("."));
    }
    
    public static List<String> categoryBlacklistItems(){
    	return getCurrentSettings().itemBlacklist.stream().filter((item)->itemIsCategory(item)).toList();
    }
	public static List<String> itemBlacklistItems() {
		return List.of(getCurrentSettings().itemBlacklist.toArray(new String[] {}));
	}

    public static boolean isInBlacklist(Item item) {
    	    	
    	if(item instanceof ItemAttackerWeaponItem) {
    		return true;
    	}
    	
    	if (getCurrentSettings().itemBlacklist.contains(item.getStringID())) {
    		return true;
    	}    
    	
    	for(ItemCategory ic: getCurrentSettings().categoryBlacklist) {
    		if(ic.containsItem(item)) {
    			return true;
    		}
    	}
    	
    	Class<?> itemClass = item.getClass();
    	for(Class<?> clazz : getCurrentSettings().classBlacklist) {
    		if(clazz.isAssignableFrom(itemClass)) {
    			return true;
    		}
    	}    	    
    
        return false;
    }

    public static int setItemStackSizeModifier(String item, int stackSize) {
    	boolean itemExists = ItemRegistry.itemExists(item);
    	if(!itemExists) return -1;
	    getCurrentSettings().itemModifiers.put(item, stackSize);    
	    syncClients();
        return 1;
    }
    
    public static int setCategoryStackSizeModifier(ItemCategory ic, int stackSize) {
    
	    getCurrentSettings().categoryModifiers.put(ic, stackSize);	
	    syncClients();
	    return 1;
    }
    
    public static int addCategoryToBlacklist(ItemCategory ic) {
	    getCurrentSettings().categoryBlacklist.add(ic);
	    syncClients();
        return 1;
    }
    
    public static int removeItemCategoryFromBlacklist(ItemCategory ic) {
    	int r =   getCurrentSettings().categoryBlacklist.remove(ic) ? 1 : -1;      
    	syncClients();
		 return r;
	}
	
    public static int removeItemCategoryFromModifiers(ItemCategory ic) {
    	int r =   getCurrentSettings().categoryModifiers.remove(ic) != null ? 1 : -1;   
    	syncClients();
		 return r;
	}
	
	public static int removeItemStackSizeModifier(String item) {
		int r =  getCurrentSettings().itemModifiers.remove(item) != null ? 1 : -1;       
		 syncClients();
		 return r;
	}
	
    public static void setDefaultStackSizeModifier(int stackSize) {
    	getCurrentSettings().default_stackSize_modifier = Math.abs(stackSize);
    	 syncClients();
    }
    
    public static boolean itemHasNoModifyField(Item item) {
    	 try {
             Field field = item.getClass().getDeclaredField("SR_NO_MODIFY");
             if (field != null) {
                 return true; 
             }
         } catch (NoSuchFieldException e) {}
    	 return false;
         
    }
	public static int itemGetModifyField(Item item) {
		 try {
	            Field modifyField = item.getClass().getDeclaredField("SR_MODIFY");
	            if (Modifier.isStatic(modifyField.getModifiers()) && Modifier.isFinal(modifyField.getModifiers()) 
	                && modifyField.getType() == int.class) {
	                return modifyField.getInt(null); // Because the field SHOULD be static, we can use null as the parameter here.
	            }
	        } catch (NoSuchFieldException | IllegalAccessException e) {
	            // Field not found or not accessible.
	        }  
		 return -1;
    }	
	public static int itemGetCustomStacksize(Item item) {
		 if (getCurrentSettings().itemModifiers.containsKey(item.getStringID())) {
	        	return getCurrentSettings().itemModifiers.get(item.getStringID()).intValue();
	     } 
		 return -1;
    } 
	
	public static ItemCategory itemGetCategoryModifierCategory(Item item) {	
		for (ItemCategory ic : getCurrentSettings().categoryModifiers.keySet()) {
	        if(ic.containsItem(item)) return ic;
        }  
		return null;
   } 
	
	public static int categoryGetCustomStacksize(Item item) {
		 if (getCurrentSettings().itemModifiers.containsKey(item.getStringID())) {
	        	return getCurrentSettings().itemModifiers.get(item.getStringID()).intValue();
	     } 
		 return -1;
    } 
	
    public static int getStackSizeModification(Item item, int currentStackSize) {     
    	
    	// Check if item class has a field named "SR_NO_MODIFY".
    	
    	if(itemHasNoModifyField(item)) return currentStackSize;
        
        // Check if item class has a field named "SR_MODIFY" and use its value as the new stackSize if it exists.
    	int modifyFieldCheck = itemGetModifyField(item);
    	if(modifyFieldCheck != -1) return modifyFieldCheck;
    	    
    	int customStacksizeCheck = itemGetCustomStacksize(item);
        if (customStacksizeCheck != -1) {
        	return customStacksizeCheck;
        }
        else {
        	
        	ItemCategory itemCategoryModifierCheck = itemGetCategoryModifierCategory(item);
        	if(itemCategoryModifierCheck != null) {
        		return getCurrentSettings().categoryModifiers.get(itemCategoryModifierCheck);
        	}
        	        
	        if (isInBlacklist(item)) {
	            return currentStackSize;
	        }     
	        
	        if(getCurrentSettings().modify_stackSize_enabled){	        	
	            return getCurrentSettings().default_stackSize_modifier;	             
	        }
        }
        
        return 1;
    }
    
    public static int getItemStackSize(String target) {
		return getCurrentSettings().itemModifiers.getOrDefault(target, -1);
	}

	public static int getCategoryStackSize(ItemCategory itemCategory) {
		if(currentSettings.categoryModifiers.containsKey(itemCategory)) {
			return currentSettings.categoryModifiers.get(itemCategory);
		}
		return -1;
	}
	
    public static String getSavePath() {
    	String appDataCfgPath = GlobalData.cfgPath();
    	String loadedModName = currentModInstance.name;
    	return appDataCfgPath + "mods" + System.getProperty("file.separator") + loadedModName;   
    }
    
    public static String getSavePath(boolean suffix) {
    	return suffix ? getSavePath() + GlobalData.saveSuffix : getSavePath();
    }    
	
	public static String[] getAllWorlds() {
		return necesse.engine.world.World.loadWorldsFromPaths();
	}
	
    // Save data only if the calling client is the server or the owner
    public static void saveModData() {  
    	//if (getCurrentServer() == null || !clientIsOwnerAuth()) return;    	 
        try {
			getCurrentSettings().save();
		} catch (IOException e) {
			ig_oops("Failed to save mod information..." + e.getMessage());
			e.printStackTrace();
		}
    }   

	public static Map<String, Integer> getItemModifiers() {
        return new HashMap<>(getCurrentSettings().itemModifiers); // Return a copy to prevent external modification
    }

	public static int getDefaultStackSizeModifier() {
		return getCurrentSettings().default_stackSize_modifier;
	}

	public static boolean getEnabled() {
		return getCurrentSettings().modify_stackSize_enabled;
	}

	public static LoadedMod getCurrentModInstance() {
		return currentModInstance;
	}

	public static String getCurrentWorld() {
		if(currentWorldName != null) {
			return GameUtils.removeFileExtension(currentWorldName);
		}
		return "none";
	}
	
	public static boolean getDebugState() {
		return getCurrentSettings().debug_state;
	}

	public static SRSettings getCurrentSettings() {
		if(currentSettings==null) {
			currentSettings = SRSettings.getDefaultSettings(getCurrentWorld());
		}
		return currentSettings;
	}

	public static void setEnabled(boolean result_state) {
		currentSettings.modify_stackSize_enabled = result_state;		
	}

	public static int setItemStackSizeModifiersFromString(String formattedstring) {
		Map<String, Integer> newAdditions = SRSettings.itemModifierListFromString(formattedstring);
		currentSettings.itemModifiers.putAll(newAdditions);
		return newAdditions.size();
	}

	public static int setBlacklistClassesFromString(String formattedstring) {
		Set<Class<?>> newAdditions = SRSettings.classBlacklistFromString(formattedstring);
		currentSettings.classBlacklist.addAll(newAdditions);
		return newAdditions.size();
	}

	public static int setBlacklistItemsFromString(String formattedstring) {
		Set<String> newAdditions = SRSettings.itemBlacklistFromString(formattedstring);
		currentSettings.itemBlacklist.addAll(newAdditions);
		return newAdditions.size();
	}

	public static Map<ItemCategory, Integer> getCategoryModifiers() {
		
		return currentSettings.categoryModifiers;
	}

	public static String getItemStackSizeStateString(Item th) {		
		
		if(StackResizer.itemHasNoModifyField(th) || StackResizer.itemGetModifyField(th) != -1) {
			return "overridden";
		} else if(StackResizer.itemGetCategoryModifierCategory(th) != null) {
			return "category";
		} else if(StackResizer.itemGetCustomStacksize(th) != -1) { 
			return "item";
		} else if(StackResizer.isInBlacklist(th)) {
			return "blacklisted";
		} else {
			return "default";
		}
	}



}
