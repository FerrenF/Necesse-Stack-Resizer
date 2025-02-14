package main.java.ModifyISS;

import necesse.engine.GameLog;
import necesse.engine.GlobalData;
import necesse.engine.commands.CommandsManager;
import necesse.engine.commands.PermissionLevel;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.modLoader.annotations.ModEntry;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.network.server.ServerSettings;
import necesse.engine.registries.ItemRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.state.MainGame;
import necesse.engine.state.MainMenu;
import necesse.engine.state.State;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.DataFormatException;

import main.java.ModifyISS.commands.*;
import necesse.inventory.item.Item;

@ModEntry
public class ModifyISS {
	
	private static final Set<String> itemBlacklist = new HashSet<>();
    private static final Set<Class<?>> classBlacklist = new HashSet<>();
    private static final Map<Class<?>, Integer> classModifiers = new HashMap<>();
    private static final Map<String, Integer> itemModifiers = new HashMap<>();
    
    private static boolean debug_state = false;
    private static int default_stackSize_modifier = 5000;
    private static boolean modify_stack_size_enabled = true;
    private static LoadedMod currentModInstance;
    private static Server currentServer;
    
    // Regular oops. Straight to log.
    public static void oops(String how) {
        GameLog.out.println(how);
    }

    // Debug oops. To log if debug_state
    public static void dbg_oops(String how) {
        if (debug_state) oops(how);
    }
    
    // In-game oops. To in-game chat AND log.
    public static void ig_oops(String string) {
    	if((necesse.engine.GlobalData.getCurrentState() instanceof MainGame)) {
    		((MainGame)necesse.engine.GlobalData.getCurrentState()).getClient().chat.addMessage(string);
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
    
    public static void serverConnectEvent(Server server, ServerSettings hostSettings) {
    	dbg_oops("Server start event triggered.");
    	ModifyISS.currentServer = server;	
    	loadModData();
    	ensureBaseBlacklist();
	}
    
    public static void serverSaveEvent(Server server) {
    	dbg_oops("Server save event triggered.");
    	ModifyISS.currentServer = server;
    	dbg_oops("Saving mod information for "+ModifyISS.currentModInstance.name+" at "+ModifyISS.getServerSpecificSavePath());
		saveModData();
	}
    
    public static Client getGameClient() {    	
        State currentState = necesse.engine.GlobalData.getCurrentState();
        return currentState instanceof MainGame ? ((MainGame) currentState).getClient() : ((MainMenu) currentState).getClient();
    }  
    
    // Check if the current client is the owner or the server itself
    public static boolean clientIsOwnerAuth() {
        Client c_c = getGameClient();
        return (c_c.getPermissionLevel() == PermissionLevel.OWNER) || 
               (c_c.getPermissionLevel() == PermissionLevel.SERVER);
    }
    
    private static Server getCurrentServer() {
    	return ModifyISS.currentServer;
    }
    
    public static void ensureBaseBlacklist() {    	
    	
    	classBlacklist.add(necesse.inventory.item.mountItem.MountItem.class);
    	classBlacklist.add(necesse.inventory.item.trinketItem.TrinketItem.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.AmmoPouch.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.AmmoBag.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.CoinPouch.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.Lunchbox.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.EnchantingScrollItem.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.GatewayTabletItem.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.PotionBag.class);
    	classBlacklist.add(necesse.inventory.item.miscItem.PotionPouch.class); 
    	dbg_oops("Base blacklist items added.");
    }
    
    public void registerCommands() {
        CommandsManager.registerServerCommand(new GetBlacklistCommand());
        CommandsManager.registerServerCommand(new AddBlacklistCommand());
        CommandsManager.registerServerCommand(new RemoveBlacklistCommand());
        CommandsManager.registerServerCommand(new SetStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new SetDefaultStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new SaveCommand());
        CommandsManager.registerServerCommand(new RemoveStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new GetStackSizeModifierCommand());
        CommandsManager.registerServerCommand(new StackSizeCommand());
        CommandsManager.registerServerCommand(new InfoCommand());
        CommandsManager.registerServerCommand(new ReloadCommand());
        CommandsManager.registerServerCommand(new SetDebugStateCommand());
        CommandsManager.registerServerCommand(new SetEnabledStateCommand());
    }     
    public static void setDebugState(boolean val) {
    	ModifyISS.debug_state = val;
    }
    public static Set<Class<?>> getClassBlacklist() {
        return classBlacklist;
    }  
    
    public static Set<String> getItemBlacklist() {
        return itemBlacklist;
    }     
    
    public static String getClassBlacklistString() {
        return classBlacklist.stream()
                .map(Class::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Blacklist is empty.");
    }
    
    public static String getItemBlacklistString() {
        return itemBlacklist.stream()
                .reduce((a, b) -> a + ", " + b)
                .orElse("Blacklist is empty.");
    }
    
    public static int addItemToBlacklist(String item) {
    	boolean itemExists = ItemRegistry.itemExists(item);
        if (!itemExists) return -1;
    	itemBlacklist.add(item);
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
    	boolean classExists = classBlacklist.contains(clzz);
    	if (classExists) return 0;    	
    	classBlacklist.add(clzz);
        return 1;
    }

    public static int removeItemFromBlacklist(String item) {
		if(!itemBlacklist.contains(item)) return -1;
		itemBlacklist.remove(item);
        return 1;
    }
    
    public static int removeClassFromBlacklist(Class<?> clzz) {
		if(!classBlacklist.contains(clzz)) return -1;
		classBlacklist.remove(clzz);
        return 1;
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
    	if (classBlacklist.contains(itemClass)) return true;
    	if (itemBlacklist.contains(item.getStringID())) return true;
        return false;
    }

    public static int setItemStackSizeModifier(String item, int stackSize) {
    	boolean itemExists = ItemRegistry.itemExists(item);
    	if(!itemExists) return -1;
	    itemModifiers.put(item, stackSize);       
        return 1;
    }
    public static int setClassStackSizeModifier(Class<?> clzz, int stackSize) {    
    	classModifiers.put(clzz, stackSize);       
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
    	if(!classModifiers.containsKey(clzz)) return 0;
    	classModifiers.remove(clzz);       
        return 1;
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
		if(!itemModifiers.containsKey(item)) return 0;
		itemModifiers.remove(item);       
        return 1;
	}
	
    public static boolean setDefaultStackSizeModifier(int stackSize) {
        ModifyISS.default_stackSize_modifier = Math.abs(stackSize);
        return true;
    }
    
    public static int getStackSizeModification(Item item, int currentStackSize) {
    	
        dbg_oops(item.getStringID() + " stacks to " + currentStackSize);        
        if (isInBlacklist(item)) {
            return currentStackSize;
        }               
        
        if (ModifyISS.modify_stack_size_enabled) {
            currentStackSize = ModifyISS.default_stackSize_modifier;
        }
        if (itemModifiers.containsKey(item.getStringID())) return itemModifiers.get(item.getStringID()).intValue();
        for (Class<?> clz : classModifiers.keySet()) {
        	if(item.getClass().isInstance(clz)) return classModifiers.get(clz);
        }        
        return currentStackSize; // Fail-safe to prevent negative values
    }
    
    public static int getItemStackSize(String target) {
		return itemModifiers.getOrDefault(target, -1);
	}

	public static int getClassStackSize(String target) {
		try {
			return classModifiers.getOrDefault(Class.forName(target), -1);
		} catch (ClassNotFoundException e) {
			return -1;
		}
	}
   
     
    public static String itemModifierListToString() {
		StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : itemModifiers.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(";");
        }
        return sb.toString();
	}
	public static String classModifierListToString() {
		StringBuilder sb = new StringBuilder();
        for (Map.Entry<Class<?>, Integer> entry : classModifiers.entrySet()) {
            sb.append(entry.getKey().getName()).append("=").append(entry.getValue().toString()).append(";");
        }
        return sb.toString();
	}
    
	public static int itemModifierListFromString(String str) throws IllegalArgumentException {
        itemModifiers.clear();
        if (str.isEmpty()) return 0;
        int count = 0;
        String[] entries = str.split(";");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length != 2) throw new IllegalArgumentException("Invalid format for modifiers entry: " + entry);

            String key = keyValue[0];
            Integer value = Integer.valueOf(keyValue[1]);
            itemModifiers.put(key, value);
            count++;
        }
        return count;
    }
	public static int classModifierListFromString(String str) throws ClassNotFoundException {
        itemModifiers.clear();
        if (str.isEmpty()) return 0;
        int count = 0;
        String[] entries = str.split(";");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            
            String[] keyValue = entry.split("=", 2);
            if (keyValue.length != 2) throw new IllegalArgumentException("Invalid format for modifiers entry: " + entry);

            Class<?> key = Class.forName(keyValue[0]);
            Integer value = Integer.valueOf(keyValue[1]);
            classModifiers.put(key, value);
            count++;
        }
        return count;
    }
    
    public static String classBlacklistToString() {
        StringBuilder sb = new StringBuilder();
        for (Class<?> clazz : classBlacklist) sb.append(clazz.getName()).append(";");
        return sb.toString();
    }
    
    public static String itemBlacklistToString() {
        StringBuilder sb = new StringBuilder();
        for (String item : itemBlacklist) sb.append(item).append(";");
        return sb.toString();
    }

    public static int classBlacklistFromString(String str) throws ClassNotFoundException {
        classBlacklist.clear();
        if (str.isEmpty()) return 0;

        int count = 0;
        String[] classNames = str.split(";");
        for (String className : classNames) {
            if (className.isEmpty()) continue;
            classBlacklist.add(Class.forName(className));
            count++;
        }
        return count;
    }
    public static int itemBlacklistFromString(String str) throws ClassNotFoundException {
        itemBlacklist.clear();
        if (str.isEmpty()) return 0;

        int count = 0;
        String[] itemNames = str.split(";");
        for (String itemName : itemNames) {
            if (itemName.isEmpty()) continue;
            itemBlacklist.add(itemName);
            count++;
        }
        return count;
    }

    public static String getSavePath() {
    	String appDataCfgPath = GlobalData.cfgPath();
    	String loadedModName = currentModInstance.name;
    	return appDataCfgPath+"mods/" + loadedModName;   
    }
    public static String getSavePath(boolean suffix) {
    	return suffix ? getSavePath() + GlobalData.saveSuffix : getSavePath();
    }
    
    // Get the save path based on the current server name
    public static String getServerSpecificSavePath() {
        if (getCurrentServer() == null) return getSavePath(); // Default path if no server connected
        return getSavePath(false) + "_" + getCurrentServer().world.displayName + GlobalData.saveSuffix; // Append server name
    }
	public static String getServerSpecificSavePath(Server server) {
		return getSavePath(false) + "_" + server.world.displayName + GlobalData.saveSuffix;
	}
    // Save data only if the caller is the server or the owner
    public static void saveModData() {  
        if (getCurrentServer() == null || !clientIsOwnerAuth()) {
            dbg_oops("Unauthorized attempt to save mod data.");
            return;
        }

        String loadedModName = currentModInstance.name;
        SaveData s = new SaveData(loadedModName);
        
        s.addUnsafeString("itemModifiers", itemModifierListToString());
        s.addUnsafeString("classModifiers", classModifierListToString());
        s.addUnsafeString("classBlacklist", classBlacklistToString());
        s.addUnsafeString("itemBlacklist", itemBlacklistToString());
        s.addInt("defaultStackSize", getDefaultStackSizeModifier());
        s.addBoolean("debugState", debug_state);
        s.addBoolean("enabledState", getEnabled());
        
        String targetSavePath = getServerSpecificSavePath();    	
        File saveFileOut = new File(targetSavePath);
        
        if (saveFileOut.exists()) saveFileOut.delete();
        
        try {
            s.saveScriptRaw(saveFileOut, false);
        } catch (IOException e) {
            dbg_oops("Failed to save " + loadedModName + " mod data: " + e.getMessage());
            e.printStackTrace();
        }
        
        oops("Saved mod data to " + targetSavePath);
    }

   

	// Load data only if the caller is the server or the owner
    public static void loadModData() {    
        String loadedModName = currentModInstance.name;
        String targetSavePath = getServerSpecificSavePath();
        
        File saveFileIn = new File(targetSavePath);
        
        if (!saveFileIn.exists()) {
            oops("No save data present for mod " + loadedModName);
            return;
        }

        LoadData s;
        try {
            s = LoadData.newRaw(saveFileIn, false);
        } catch (IOException | DataFormatException e) {
            dbg_oops("Failed to load " + loadedModName + " mod save data: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            int loadedClassModifiers = classModifierListFromString(s.getUnsafeString("classModifiers"));	
            oops("Loaded " + loadedClassModifiers + " item classes to modifier list.");
            dbg_oops("Modifiers: " + classModifierListToString());

            int loadedItemModifiers = itemModifierListFromString(s.getUnsafeString("itemModifiers"));	
            oops("Loaded " + loadedItemModifiers + " item names to modifier list.");
            dbg_oops("Modifiers: " + itemModifierListToString());

            int loadedClassBlacklist = classBlacklistFromString(s.getUnsafeString("classBlacklist"));			
            oops("Loaded " + loadedClassBlacklist + " item classes to blacklist.");
            dbg_oops("Blacklist: " + classBlacklistToString());

            int loadedItemBlacklist = itemBlacklistFromString(s.getUnsafeString("itemBlacklist"));			
            oops("Loaded " + loadedItemBlacklist + " item names to blacklist.");
            dbg_oops("Blacklist: " + itemBlacklistToString());
            
            int defaultStackSize = s.getInt("defaultStackSize");			
            oops("Loaded default stack size: "+defaultStackSize);      
            ModifyISS.setDefaultStackSizeModifier(defaultStackSize);
            
            boolean dbg_state = s.getBoolean("debugState");			
            oops("Debug state: "+dbg_state);      
            ModifyISS.setDebugState(dbg_state);
            
            boolean enabled_state = s.getBoolean("enabledState");			
            oops("Enabled: "+enabled_state);      
            ModifyISS.setEnabledState(enabled_state);

        } catch (ClassNotFoundException e) {
            dbg_oops(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void setEnabledState(boolean enabled_state) {
    	ig_oops("Enabled state of stack size mod has been changed. This feature is.. unstable?");
		ModifyISS.modify_stack_size_enabled = enabled_state;		
	}

	public static Map<String, Integer> getItemModifiers() {
        return new HashMap<>(itemModifiers); // Return a copy to prevent external modification
    }

    public static Map<Class<?>, Integer> getClassModifiers() {
        return new HashMap<>(classModifiers); // Return a copy to prevent external modification
    }

    public static void setItemBlacklist(Set<String> itemBlacklist2) {
        itemBlacklist.clear();
        itemBlacklist.addAll(itemBlacklist2);
    }

    public static void setClassBlacklist(Set<String> classBlacklist2) {
        classBlacklist.clear();
        for (String className : classBlacklist2) {
            try {
                classBlacklist.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + className);
            }
        }
    }

    public static void setItemModifiers(Map<String, Integer> itemModifiers2) {
        itemModifiers.clear();
        itemModifiers.putAll(itemModifiers2);
    }

    public static void setClassModifiers(Map<String, Integer> classModifiers2) {
        classModifiers.clear();
        for (Map.Entry<String, Integer> entry : classModifiers2.entrySet()) {
            try {
                classModifiers.put(Class.forName(entry.getKey()), entry.getValue());
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + entry.getKey());
            }
        }
    }

	public static int getDefaultStackSizeModifier() {
		return default_stackSize_modifier;
	}

	public static boolean getEnabled() {
		return modify_stack_size_enabled;
	}

	public static LoadedMod getCurrentModInstance() {
		return currentModInstance;
	}

	public static void setEnabled(boolean b) {
		ModifyISS.modify_stack_size_enabled = b;		
	}

	public static boolean getDebugState() {
		return ModifyISS.modify_stack_size_enabled;
	}



	
	

	


	


	

 
}
