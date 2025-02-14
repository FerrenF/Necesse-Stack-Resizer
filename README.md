# Necesse Stack Resizer Mod
![Preview](./preview.png)  

## v0.1.0 Latest

## Shoutout  
This mod is inspired by the original **Increased Stack Size** mod by [@dianchia](https://github.com/dianchia). However, it aims to be a more robust implementation with expanded features and greater flexibility.  

## Features  
- Set **custom default stack sizes**.  
- Configure **individual stack sizes** for specific item names and engine item classes (e.g., `necesse.item.X`).  
- Maintain an **item blacklist** and **class blacklist** to exclude specific items from being modified.  
- Utilize a **comprehensive set of in-game commands** for managing stack sizes and blacklists.  
- Provide **external mod support**, allowing other mods to modify the blacklist dynamically.  
- AutoSaves when the server does.
- All settings are specific to individual worlds, stored at %APPDATA%/Necesse/cfg/mods

## Commands  
These commands allow server administrators and external mods to adjust stack sizes and blacklists:  

| Command                         | Description |
|---------------------------------|-------------|
| `stackresize.blacklist`                 | View the current blacklist. Anyone may use this command. |
| `stackresize.blacklist.add <item/class> <is_class[true/false]> <quiet[true/false]>` | Add an item or class to the blacklist. |
| `stackresize.blacklist.remove <item/class> <is_class[true/false]> <quiet[true/false]>` | Remove an item or class from the blacklist. |
| `stackresize.stacksize.set <item/class> <value> <is_class[true/false]> <quiet[true/false]>` | Set a custom stack size modifier. |
| `stackresize.stacksize.default <value>` | Set the default stack size modifier. |
| `stackresize.stacksize.remove <item/class> <is_class[true/false]> <quiet[true/false]>` | Remove a custom stack size modifier. |
| `stackresize.stacksize.get <item/class> <is_class[true/false]> <quiet[true/false]>` | Get the current stack size modifier for an item or class. Anyone may use this command. |
| `stackresize.stacksize` | List stack size information command. Anyone may use this command. |
| `stackresize.save` | Save current settings to configuration. |
| `stackresize.reload` | Reload the current settings from configuration. |
| `stackresize` | Display mod information. Anyone may use this command. |

There are some unlisted commands that I can't recommend the regular use of.

## External Mod Integration  
There are three ways to interact with this mod from another mod:

### Using Chat Commands

Other mods can interact with this mod by sending chat commands as if they were a player, using Necesse's built-in classes and methods.
 - Example 1:
	```
	 private static boolean setItemStackSizeExternally(Client client, Item item, int count) {
    	if(client == null) return false;
    	client.chat.addMessage(String.format("/stackresize.stacksize.set %s %d false true", item.getStringID(), count));
    	return true;
    }
	```
Here, we have a method that will send a formatted string to the chat adding an item name to the stacksize modifier list.
	
**This can only be used when the client is in-game or the ServerClient is at such a state that chat messages can be send to the command log.**

To prevent command spam from appearing in the chat log, **most** commands support a `quiet` parameter.  
- Example:  
  ```
  /stackresize.blacklist.add myCustomItem false <quiet[true/false]>
  /stackresize.blacklist.add necesse.inventory.item.miscItem.EnchantingScrollItem true <quiet[true/false]>
  /stackresize.stacksize.set myItem 500 false <quiet[true/false]>
  /stackresize.stacksize.set necesse.inventory.item.miscItem.EnchantingScrollItem 1 true <quiet[true/false]>
  ```
- When `quiet` is included and set to true, the command will execute without displaying output in the chat, keeping logs clean.  

### Using CommandManager

 - Example 1:
	```
	   private static boolean setItemStackSizeExternallyWithCmdMgr(Client client, Item item, int count) {
    	if(client == null) return false;
    	String commandString = String.format("/stackresize.stacksize.set %s %d false true", item.getStringID(), count);
    	ServerClient myServerClient = client.getLocalServer().getLocalServerClient();
    	return client.commandsManager.runServerCommand(new ParsedCommand(commandString), myServerClient);
    }    
	```
There are a few ways to do run a command directly through the CommandManager, and here is one of them. In this static method, we are running the command as a ServerCommand from the client. 
The client needs to have the correct permission level (probably OWNER or SERVER) to do this.

### Editing the configuration externally.

This mod stores it's configuration information in the game's default settings path at %APPDATA%/Necesse/cfg/mods (on windows). Your path may differ on a unix server.
You can edit this file directly both before and after the mod has loaded. 

If modified before this mod loads it's configuration (done in preInit()), then nothing more needs to be done. The mod will read the new settings on load.

Here are some boilerplate methods that might help show you how to do this:

```
	...
	
    public static String getResizerCfg(String worldName){
	    return "Stack Resizer_" + worldName + GlobalData.saveSuffix;
    }
    
    public static Map<String, Integer> itemModifierListFromString(String str) throws IllegalArgumentException {
        return Arrays.stream(str.split(";"))
            .filter(entry -> !entry.isEmpty())
            .map(entry -> entry.split("=", 2))
            .collect(Collectors.toMap(
                keyValue -> {
                    if (keyValue.length != 2) throw new IllegalArgumentException("Invalid format for modifiers entry: " + Arrays.toString(keyValue));
                    return keyValue[0];
                },
                keyValue -> Integer.valueOf(keyValue[1])
            ));
    }

    public static Map<Class<?>, Integer> classModifierListFromString(String str) throws ClassNotFoundException {
        return Arrays.stream(str.split(";"))
            .filter(entry -> !entry.isEmpty())
            .map(entry -> entry.split("=", 2))
            .collect(Collectors.toMap(
                keyValue -> {
                    if (keyValue.length != 2) throw new IllegalArgumentException("Invalid format for modifiers entry: " + Arrays.toString(keyValue));
                    try {
                        return Class.forName(keyValue[0]);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e); // Rethrow as unchecked to simplify lambda
                    }
                },
                keyValue -> Integer.valueOf(keyValue[1])
            ));
    }

    public static void loadResizerData(String path) {      
        File saveFileIn = new File(path);      
        LoadData s;
        try {
            s = LoadData.newRaw(saveFileIn, false);
        } catch (IOException | DataFormatException e) {         
            e.printStackTrace();
            return;
        }

        
        try {
            loadedClassModifiers = s.getUnsafeString("classModifiers");	
            loadedItemModifiers = itemModifierListFromString(s.getUnsafeString("itemModifiers"));	
            loadedClassBlacklist = classBlacklistFromString(s.getUnsafeString("classBlacklist"));			
            loadedItemBlacklist = itemBlacklistFromString(s.getUnsafeString("itemBlacklist"));			
           
            defaultStackSize = s.getInt("defaultStackSize");			         
            dbg_state = s.getBoolean("debugState");			
            enabled_state = s.getBoolean("enabledState");	
            
			// Assumes these are static fields in a class.
			
        } catch (ClassNotFoundException e) {
            dbg_oops(e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void saveResizerData(String path) {  
         SaveData s = new SaveData("Stack Resizer");
         
         s.addUnsafeString("itemModifiers", itemModifierListToString()); // itemModifierListToString is not implemented here, but it should be easy enough
         s.addUnsafeString("classModifiers", classModifierListToString()); // Same here: classModifierListToString
         s.addUnsafeString("classBlacklist", classBlacklistToString());
         s.addUnsafeString("itemBlacklist", itemBlacklistToString());
         s.addInt("defaultStackSize", defaultStackSize);
         s.addBoolean("debugState", debug_state);
         s.addBoolean("enabledState", enabled_state);
         
         String targetSavePath = path;    	
         File saveFileOut = new File(targetSavePath);         
         if (saveFileOut.exists()) saveFileOut.delete();
         
         try {
             s.saveScriptRaw(saveFileOut, false);
         } catch (IOException e) {
             e.printStackTrace();
         }        
    }
    
    public static Set<String> classBlacklistFromString(String str) {
    return Arrays.stream(str.split(";"))
        .filter(entry -> !entry.isEmpty())
        .collect(Collectors.toSet());
    }

    public static Set<String> itemBlacklistFromString(String str) {
    return Arrays.stream(str.split(";"))
        .filter(entry -> !entry.isEmpty())
        .collect(Collectors.toSet());
    }
    
    public void preInit() {
        // Get the configuration path for "test world"
        String configPath = getResizerCfg("test world");

        // Load existing data from the config file
        loadResizerData(configPath);

        // Example: Modify some settings
        Map<String, Integer> itemModifiers = itemModifierListFromString(s.getUnsafeString("itemModifiers"));
        Set<String> itemBlacklist = itemBlacklistFromString(s.getUnsafeString("itemBlacklist"));

        // Modify an item modifier
        itemModifiers.put("exampleItem", 500); // Set "exampleItem" stack size to 500

        // Add an item to the blacklist
        itemBlacklist.add("forbiddenItem");

        // Save the modified data back to the config file
        saveResizerData(configPath);
    }
	...
```
	
## Base Blacklist
Certain item classes are blacklisted by default to help mitigate issues. These classes include those that extend:

- necesse.inventory.item.mountItem.MountItem
- necesse.inventory.item.trinketItem.TrinketItem
- necesse.inventory.item.miscItem.AmmoPouch
- necesse.inventory.item.miscItem.AmmoBag
- necesse.inventory.item.miscItem.CoinPouch
- necesse.inventory.item.miscItem.Lunchbox
- necesse.inventory.item.miscItem.EnchantingScrollItem
- necesse.inventory.item.miscItem.GatewayTabletItem
- necesse.inventory.item.miscItem.PotionBag
- necesse.inventory.item.miscItem.PotionPouch
- necesse.inventory.item.miscItem.VoidBagPouch

## Installation  
1. Download the mod `.jar` file.  
2. Place it in the `mods` folder of your Necesse installation.  
3. Start the game, and use `/stackresize` to verify the mod is loaded.  

## Compatibility  
- This mod is both **server-side and client-side**. Server and client must both have this mod.
- Compatible with most other mods that modify stack sizes, as long as they do not override the same configuration settings.  

## What happens if..
- I change the default stack size while items are stacked to over the new amount?
-- Nothing, until you try to take an item from the stack or split it. Then it'll kick in.

## Bugs
- Contact me on discord @ferrenfx
- Or, submit an issue here.

## History
- 02-14-2025 - v0.1.0 Initial Release
