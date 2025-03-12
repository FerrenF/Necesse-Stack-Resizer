package stackResizer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.zip.DataFormatException;

import necesse.engine.GlobalData;
import necesse.engine.modLoader.LoadedMod;
import necesse.engine.network.client.Client;
import necesse.engine.network.server.Server;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;

public class SRSettings{
    	
		public static boolean DEFAULT_DEBUG_STATE = false;
    	public Set<String> itemBlacklist;
    	public Set<Class<?>> classBlacklist;
    	public Map<Class<?>, Integer> classModifiers;
    	public Map<String, Integer> itemModifiers;
        
    	public boolean debug_state;
    	public int default_stackSize_modifier;
    	public boolean modify_stackSize_enabled;
        
        private String world_name;
        public static final int default_default_stackSize_modifier = 5000;
        public static final String SRModName = "Stack Resizer";
        
        private SRSettings(String _world_name, Set<String> _itemBlacklist, Set<Class<?>> _classBlacklist, Map<Class<?>, Integer> _classModifiers, Map<String, Integer> _itemModifiers,
        		int _default_stackSize_modifier, boolean _debug_state, boolean _modify_stackSize_enabled) {
        	
        	this.world_name = _world_name;
        	this.itemBlacklist = _itemBlacklist;
        	this.classBlacklist = _classBlacklist;
        	this.classModifiers = _classModifiers;
        	this.itemModifiers = _itemModifiers;
        	this.debug_state = _debug_state;
        	this.default_stackSize_modifier = _default_stackSize_modifier;
        	this.modify_stackSize_enabled = _modify_stackSize_enabled;        
        	
        }
        
        public String getWorldName() {
        	return this.world_name;
        }
        
        public static SRSettings fromWorldName(String _world_name) throws IOException, DataFormatException {
        	
            String targetSavePath = getWorldSpecificSavePath(_world_name);
            
            File saveFileIn = new File(targetSavePath);        
            if (!saveFileIn.exists()) return null;

            LoadData s = LoadData.newRaw(saveFileIn, false);
            
            Map<String, Integer> n_itemModifiers = SRSettings.itemModifierListFromString(s.getUnsafeString("itemModifiers"));
            Map<Class<?>, Integer> n_classModifiers = SRSettings.classModifierListFromString(s.getUnsafeString("classModifiers"));
            Set<String> n_itemBlacklist = SRSettings.itemBlacklistFromString(s.getUnsafeString("itemBlacklist"));
            Set<Class<?>> n_classBlacklist = SRSettings.classBlacklistFromString(s.getUnsafeString("classBlacklist"));
            int n_defaultStackSize = s.getInt("defaultStackSize");
            boolean n_debug_state = s.getBoolean("debugState");
            boolean n_enabled_state = s.getBoolean("enabledState");
            
            return new SRSettings(_world_name, n_itemBlacklist, n_classBlacklist, n_classModifiers, n_itemModifiers, n_defaultStackSize, n_debug_state, n_enabled_state);
          
        }
        
        public static boolean mod_is_loaded() {
        	for (LoadedMod _mod : necesse.engine.modLoader.ModLoader.getAllMods()) {
        		if(_mod.name.contains(SRModName)) return true;
        	}
        	return false;
        }
        
        public static class ClientNotInitializedException extends IllegalArgumentException{
			private static final long serialVersionUID = 3520351653303309969L;
			public ClientNotInitializedException(){
				super("Client has not been initialized.");
			}
        }
        
        public static class WorldNotInitializedException extends IllegalArgumentException{
			private static final long serialVersionUID = 3520351653303309970L;
			public WorldNotInitializedException(){
				super("World has not been initialized.");
			}
        }
        
        public static SRSettings getDefaultSettings(String world_name) {
        	return new SRSettings(world_name, new HashSet<String>(), new HashSet<Class<?>>(), new HashMap<Class<?>, Integer>(), new HashMap<String,Integer>(), default_default_stackSize_modifier, DEFAULT_DEBUG_STATE, true);
        }
        
        public static SRSettings fromCurrentWorld(Client client) throws ClientNotInitializedException, WorldNotInitializedException, IOException, DataFormatException{        	
        	if (client == null) throw new SRSettings.ClientNotInitializedException();        	
        	if(client.worldEntity == null) throw new WorldNotInitializedException();        	
        	return fromWorldName(client.worldEntity.serverWorld.displayName);
        }
                
        // Save data only if the calling client is the server or the owner
        public void save() throws IOException {      	
            SaveData s = new SaveData(SRSettings.SRModName);            
            s.addUnsafeString("itemModifiers", itemModifierListToString(this.itemModifiers));
            s.addUnsafeString("classModifiers", classModifierListToString(this.classModifiers));
            
            s.addUnsafeString("classBlacklist", classBlacklistToString(this.classBlacklist));
            s.addUnsafeString("itemBlacklist", itemBlacklistToString(this.itemBlacklist));
            
            s.addInt("defaultStackSize", this.default_stackSize_modifier);
            
            s.addBoolean("debugState", this.debug_state);
            s.addBoolean("enabledState", this.modify_stackSize_enabled);
            
            String targetSavePath = getWorldSpecificSavePath(this.getWorldName());    	
            File saveFileOut = new File(targetSavePath);        
            if (saveFileOut.exists()) saveFileOut.delete();
            
            s.saveScriptRaw(saveFileOut, false);               
        }
        
        
        public static <T> Map<T,Integer> lambdaListFromString(String source, Function<String, T> action){
    		
    		Map<T,Integer> returnResult = new HashMap<T, Integer>();
            if (source.isEmpty()) return returnResult;
            
            String[] entries = source.split(";");
            for (String entry : entries) {
                if (entry.isEmpty()) continue;
                
                String[] keyValue = entry.split("=", 2);
                if (keyValue.length != 2) throw new IllegalArgumentException("Invalid format for modifiers entry: " + entry);

                T key = action.apply(keyValue[0]);
                if(key==null) throw new IllegalArgumentException("Null key encountered as result from transformation action. ");
                Integer value = Integer.valueOf(keyValue[1]);
                returnResult.put(key, value);
            }
            return returnResult;
    	        
    	}
        
    	public static Map<String, Integer> itemModifierListFromString(String str) throws IllegalArgumentException {		
    		return lambdaListFromString(str, (key)->{return (String)key;});    	
        }
    	
    	private static Class<?> helperClassForname(String s){
    		try {
    			return Class.forName(s);
    		} catch (ClassNotFoundException e) {
    			return null;
    		}
    	}
    	
    	public static Map<Class<?>, Integer> classModifierListFromString(String str) {		
    		return lambdaListFromString(str, (key)->{return helperClassForname(key);});    		
        }
    	
    	public static <T> Set<T> lambdaBlacklistFromString(String source, Function<String, T> action){
	    	Set<T> resultList = new HashSet<>();
	    	if (source.isEmpty()) return resultList;
	        for (String key : source.split(";")) {
	             if (key.isEmpty()) continue;
	             resultList.add(action.apply(key));
	         }         
	    	return resultList;
	    }
	    
	    public static Set<Class<?>> classBlacklistFromString(String str) {
	        return lambdaBlacklistFromString(str, (key)->{return helperClassForname(key);});               
	    }
	    
	    public static Set<String> itemBlacklistFromString(String str)  {
	    	return lambdaBlacklistFromString(str, (key)->{return (String)key;});      
	    }
	    
	    public static <T> String lambdaBlacklistToString(Set<T> source, Function<T, String> action) {
			StringBuilder sb = new StringBuilder();
	        for (T clazz : source) sb.append(action.apply(clazz)).append(";");
	        return sb.toString();
		}
		
	    public static String classBlacklistToString(Set<Class<?>> source) {
	        return lambdaBlacklistToString(source, (key)->{return key.getName();});
	    }
	    
	    public static String itemBlacklistToString(Set<String> source) {
	    	 return lambdaBlacklistToString(source, (key)->{return (String)key;});
	    }

	    public static <T> String lambdaToString(Map<T, Integer> source, Function<Entry<T, Integer>, String> action) {
	    	StringBuilder sb = new StringBuilder();
	        for (Map.Entry<T, Integer> entry : source.entrySet()) {
	            sb.append(action.apply((Entry<T, Integer>) entry)).append("=").append(entry.getValue().toString()).append(";");
	        }
	        return sb.toString();
	    }
		
	    public static String itemModifierListToString(Map<String, Integer> source) {
			return lambdaToString(source,(entry)->{return entry.getKey();});
		}
	    
		public static String classModifierListToString(Map<Class<?>, Integer> source) {
			return lambdaToString(source,(entry)->{return entry.getKey().getName();});
		}
		
	    public static String getSavePath() {
	    	String appDataCfgPath = GlobalData.cfgPath().replace('\\', '/');
	    	return appDataCfgPath + "mods/" + SRSettings.SRModName;   
	    }
	    
	    public String savePath() {
			return getSavePath() + "_" + this.world_name + GlobalData.saveSuffix;
		}	    
	    
	    public static String getWorldSpecificSavePath(String _world_name) {
			return getSavePath() + "_" + _world_name + GlobalData.saveSuffix;
		}
	    
		public static String getServerSpecificSavePath(Server server) {
			return getWorldSpecificSavePath(server.world.displayName);
		}
				
		public String classBlacklistToString() {
			return classBlacklistToString(this.classBlacklist);
		}
		
		public String itemBlacklistToString() {
			return itemBlacklistToString(this.itemBlacklist);
		}
		
		public void reloadSettings() throws IOException, DataFormatException {
			SRSettings placeholder = SRSettings.fromWorldName(this.world_name);
			this.classBlacklist = placeholder.classBlacklist;
			this.classModifiers = placeholder.classModifiers;
			this.itemBlacklist = placeholder.itemBlacklist;
			this.classBlacklist = placeholder.classBlacklist;
			this.debug_state = placeholder.debug_state;
			this.modify_stackSize_enabled = placeholder.modify_stackSize_enabled;
			this.default_stackSize_modifier = placeholder.default_stackSize_modifier;
		}

		public String classModifierListToString() {
			return classModifierListToString(this.classModifiers);
		}
		
		public String itemModifierListToString() {
			return itemModifierListToString(this.itemModifiers);
		}
		
    	
    }