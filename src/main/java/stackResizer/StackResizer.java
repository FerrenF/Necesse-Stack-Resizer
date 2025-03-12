package stackResizer;

import necesse.engine.GameLog;
import necesse.engine.GlobalData;
import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.state.MainGame;
import necesse.engine.state.MainMenu;
import necesse.engine.state.State;
import necesse.engine.world.World;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.DataFormatException;

import stackResizer.commands.*;
import necesse.inventory.item.Item;

@ModEntry
public class StackResizer {
	
	private static final Class<?>[] BASE_CLASS_BLACKLIST = {
		    necesse.inventory.item.mountItem.MountItem.class,
		    necesse.inventory.item.trinketItem.TrinketItem.class,
		    necesse.inventory.item.miscItem.AmmoPouch.class,
		    necesse.inventory.item.miscItem.AmmoBag.class,
		    necesse.inventory.item.miscItem.CoinPouch.class,
		    necesse.inventory.item.miscItem.Lunchbox.class,
		    necesse.inventory.item.miscItem.EnchantingScrollItem.class,
		    necesse.inventory.item.miscItem.GatewayTabletItem.class,
		    necesse.inventory.item.miscItem.PotionBag.class,
		    necesse.inventory.item.miscItem.PotionPouch.class,
		    necesse.inventory.item.miscItem.VoidPouchItem.class
		};
	
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
        oops("Increased Stack Size+ loaded.");        
    }
    
    public void postInit() {
    	
    }
    
    public static void worldStartEvent(World world) {
    	dbg_oops("World start event triggered.");
    	StackResizer.currentServer = world.server;	
    	
    	try {    		
    		dbg_oops("Loading world settings for "+world.displayName);
			StackResizer.currentSettings = SRSettings.fromWorldName(world.displayName);
			
		} catch (IOException | DataFormatException e) {			
			dbg_oops("Problem loading world settings for "+world.displayName);
			e.printStackTrace();			
		}
    	ensureBaseBlacklist();
	}
    
    public static void worldStartEvent(Server server) {
    	worldStartEvent(server.world);
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
    
    public static void serverSaveEvent(Server server) {
    	//StackResizer.currentServer = server;
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
    	
        CommandsManager.registerServerCommand(new GetBlacklistCommand());
        CommandsManager.registerServerCommand(new AddBlacklistCommand());
        CommandsManager.registerServerCommand(new AddAllBlacklistCommand());
        CommandsManager.registerServerCommand(new RemoveBlacklistCommand());
        CommandsManager.registerServerCommand(new SetStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new SetStackSizesCommand());
        CommandsManager.registerServerCommand(new SetDefaultStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new SaveCommand());
        CommandsManager.registerServerCommand(new RemoveStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new GetStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new StackSizeCommand());
        CommandsManager.registerServerCommand(new InfoCommand());
        CommandsManager.registerServerCommand(new ReloadCommand());
        CommandsManager.registerServerCommand(new SetDebugStateCommand());
        CommandsManager.registerServerCommand(new SetEnabledStateCommand());
        CommandsManager.registerServerCommand(new TestExternalCommand());
        
    }    
     
    public static void setDebugState(boolean val) {
    	getCurrentSettings().debug_state = val;
    }
  
    public static int addItemToBlacklist(String item) {    	
    	boolean itemExists = ItemRegistry.itemExists(item);
        if (!itemExists) return -1;
        getCurrentSettings().itemBlacklist.add(item);
        return ItemRegistry.getItemID(item);
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
        return getCurrentSettings().classBlacklist.add(clzz) ? 1 : -1;
    }

    public static int removeItemFromBlacklist(String item) {
        return getCurrentSettings().itemBlacklist.remove(item) ? 1 : -1;
    }
    
    public static int removeClassFromBlacklist(Class<?> clzz) {
		return getCurrentSettings().classBlacklist.remove(clzz) ? 1 : -1;
    }
    
    public static int removeClassFromBlacklist(String clazz) {
    	Class<?> clzz;
		try {
			clzz = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			return -1;
		}
		return removeClassFromBlacklist(clzz);
    }
    
    public static boolean isInBlacklist(Item item) {
    	Class<?> itemClass = item.getClass();
    	if (getCurrentSettings().classBlacklist.contains(itemClass)) return true;
    	if (getCurrentSettings().itemBlacklist.contains(item.getStringID())) return true;
        return false;
    }

    public static int setItemStackSizeModifier(String item, int stackSize) {
    	boolean itemExists = ItemRegistry.itemExists(item);
    	if(!itemExists) return -1;
	    getCurrentSettings().itemModifiers.put(item, stackSize);       
        return 1;
    }
    
    public static int setClassStackSizeModifier(Class<?> clzz, int stackSize) {    
    	getCurrentSettings().classModifiers.put(clzz, stackSize);       
        return 1;
    }
    
    public static int setClassStackSizeModifier(String clazz, int stackSize) {  
    	Class<?> clzz;
		try {
			clzz = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			return -1;
		}
    	return setClassStackSizeModifier(clzz, stackSize);      
    }
    
    public static int removeClassStackSizeModifier(Class<?> clzz) {   
    	return getCurrentSettings().classModifiers.remove(clzz) != null ? 1 : -1;       
    }
    
    public static int removeClassStackSizeModifier(String clazz) {
    	Class<?> clzz;
		try {
			clzz = Class.forName(clazz);
		} catch (ClassNotFoundException e) {
			return -1;
		}
		return removeClassStackSizeModifier(clzz);
	}

	public static int removeItemStackSizeModifier(String item) {
		return getCurrentSettings().itemModifiers.remove(item) != null ? 1 : -1;       
	}
	
    public static void setDefaultStackSizeModifier(int stackSize) {
    	getCurrentSettings().default_stackSize_modifier = Math.abs(stackSize);
    }
    
    public static int getStackSizeModification(Item item, int currentStackSize) {     
    	
    	// Check if item class has a field named "SR_NO_MODIFY".
        try {
            Field field = item.getClass().getDeclaredField("SR_NO_MODIFY");
            if (field != null) {
                return currentStackSize; // Treat as blacklisted if it does.
            }
        } catch (NoSuchFieldException e) {
        	// Do nothing.
        }
        
        // Check if item class has a field named "SR_MODIFY" and use its value as the new stackSize if it exists.
        try {
            Field modifyField = item.getClass().getDeclaredField("SR_MODIFY");
            if (Modifier.isStatic(modifyField.getModifiers()) && Modifier.isFinal(modifyField.getModifiers()) 
                && modifyField.getType() == int.class) {
                return modifyField.getInt(null); // Because the field SHOULD be static, we can use null as the parameter here.
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Field not found or not accessible.
        }        
        
        if (isInBlacklist(item)) {
            return currentStackSize;
        }               
        
        if (getCurrentSettings().modify_stackSize_enabled) {
            currentStackSize = getCurrentSettings().default_stackSize_modifier;
        }
        
        if (getCurrentSettings().itemModifiers.containsKey(item.getStringID())) return getCurrentSettings().itemModifiers.get(item.getStringID()).intValue();
        for (Class<?> clz : getCurrentSettings().classModifiers.keySet()) {
        	if(item.getClass().isInstance(clz)) return getCurrentSettings().classModifiers.get(clz);
        }        
        
        return Math.abs(currentStackSize);
    }
    
    public static int getItemStackSize(String target) {
		return getCurrentSettings().itemModifiers.getOrDefault(target, -1);
	}

	public static int getClassStackSize(String target) {
		try {
			return getCurrentSettings().classModifiers.getOrDefault(Class.forName(target), -1);
		} catch (ClassNotFoundException e) {
			return -1;
		}
	}   
   
    public static String getSavePath() {
    	String appDataCfgPath = GlobalData.cfgPath();
    	String loadedModName = currentModInstance.name;
    	return appDataCfgPath+"mods/" + loadedModName;   
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

    private static void setEnabledState(boolean enabled_state) {
    	ig_oops("Enabled state of stack size mod has been changed. Be careful.");
    	getCurrentSettings().modify_stackSize_enabled = enabled_state;	
	}

	public static Map<String, Integer> getItemModifiers() {
        return new HashMap<>(getCurrentSettings().itemModifiers); // Return a copy to prevent external modification
    }

    public static Map<Class<?>, Integer> getClassModifiers() {
        return new HashMap<>(getCurrentSettings().classModifiers); // Return a copy to prevent external modification
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

	public static MainGame mainGameOrNull() {
		return ((necesse.engine.GlobalData.getCurrentState()) instanceof MainGame) ?
				((MainGame)necesse.engine.GlobalData.getCurrentState()) : null;
	}
	
	public static String getCurrentWorld() {
		MainGame mg = mainGameOrNull();
		if (mg == null) return "none";
		String displayName;
		if(mg.getClient().worldEntity.serverWorld != null) {
			displayName= mg.getClient().worldEntity.serverWorld.displayName;
		}
		else {		
			displayName = mg.getClient().playingOnDisplayName.toString();
			if(displayName == null) {
				return "unknown"; // Fallback
			}
		}
		return displayName;
	}
	
	public static boolean getDebugState() {
		return getCurrentSettings().debug_state;
	}

	public static SRSettings getCurrentSettings() {
			if(currentSettings==null) return SRSettings.getDefaultSettings(getCurrentWorld());
		return currentSettings;
	}

	public static void setEnabled(boolean result_state) {
		currentSettings.modify_stackSize_enabled = result_state;		
	}

	public static int setClassStackSizeModifiersFromString(String formattedstring) {
		Map<Class<?>, Integer> newAdditions = SRSettings.classModifierListFromString(formattedstring);
		currentSettings.classModifiers.putAll(newAdditions);
		return newAdditions.size();
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

	

	



	
	

	


	


	

 
}
