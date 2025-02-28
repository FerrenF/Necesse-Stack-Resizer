# Necesse Stack Resizer Mod
![Preview](./preview.png)  

## v0.3.2 Latest

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
These commands allow server administrators and server-side mods to adjust stack sizes and blacklists:  

| Command                         | Description | Example |
|---------------------------------|-------------|-------------|
| `stackresize.blacklist`                 | View the current blacklist. Anyone may use this command. | "/stackresize.blacklist" |
| `stackresize.blacklist.add <item/class> <is_class[true/false]>** <quiet[true/false]>**` | Add an item or class to the blacklist. | "/stackresize.blacklist.add wormbait false false" <br/> "/stackresize.blacklist.add necesse.inventory.item.miscItem.Lunchbox true false"|
| `stackresize.blacklist.remove <item/class> <is_class[true/false]>** <quiet[true/false]>**` | Remove an item or class from the blacklist. | "/stackresize.blacklist.remove wormbait false false" <br/> "/stackresize.blacklist.remove necesse.inventory.item.miscItem.Lunchbox true false" |
| `stackresize.blacklist.addall <formatted_string>*** <is_class[true/false]>** <quiet[true/false]>**` | Add a list of items or classes to the blacklist. They must all be of one type, item or class. | See Below for Examples |
| `stackresize.stacksize.set <item/class> <value> <is_class[true/false]>** <quiet[true/false]>**` | Set a custom stack size modifier. | "/stackresize.stacksize.set wormbait 100 false false" <br/> "/stackresize.stacksize.set necesse.inventory.item.miscItem.Lunchbox 1 true false" |
| `stackresize.stacksize.setall <formatted_string>*** <is_class[true/false]>** <quiet[true/false]>**` | Bulk set class or item mofidiers from a formatted string. | See Below for Examples |
| `stackresize.stacksize.remove <item/class> <is_class[true/false]>** <quiet[true/false]>**` | Remove a custom stack size modifier. | "/stackresize.stacksize.remove wormbait 100 false false" <br/> "/stackresize.stacksize.remove necesse.inventory.item.miscItem.Lunchbox 1 true false" |
| `stackresize.stacksize.default <value>` | Set the default stack size modifier. | "/stackresize.stacksize.default 5000" |
| `stackresize.stacksize.get <item/class> <is_class[true/false]>** <quiet[true/false]>**` | Get the current stack size modifier for an item or class. Anyone may use this command. | "/stackresize.stacksize.get wormbait false" <br/> "/stackresize.stacksize.get necesse.inventory.item.miscItem.Lunchbox false" |
| `stackresize.stacksize` | List stack size information command. Anyone may use this command. | "/stackresize.stacksize" |
| `stackresize.save` | Save current settings to configuration. | "/stackresize.save" |
| `stackresize.reload` | Reload the current settings from configuration. | "/stackresize.reload" |
| `stackresize` | Display mod information. Anyone may use this command. | "/stackresize" |

** denotes an optional parameter. These may or may not be present. For example: <br/> "/stackresize. blacklist.add wormbait" will perform the same action as "/stackresize. blacklist.add wormbait false true"

*** denotes a formatted string. The commands that use these strings require that they be formatted in a particular manner:

- Command: stackresize.blacklist.addall
  - Format: "item;item;item;" "class;class;class;" 
  - Examples: 
      - ```/stackresize.blacklist.addall wormbait;craftingguide;someotheritem;```<br/>
      - ```/stackresize.blacklist.addall necesse.inventory.item.mountItem.MountItem;necesse.inventory.item.trinketItem.TrinketItem;necesse.inventory.item.miscItem.AmmoBag;```
		 
- Command: stackresize.stacksize.setall
  - Format: "item=stacksize;item=stacksize;item=stacksize;" "class=stacksize;class=stacksize;class=stacksize;"
  - Examples:
    -	```/stackresize.stacksize.setall wormbait=100;craftingguide=1;somethingelse=9999;```
	-	```/stackresize.stacksize.setall necesse.inventory.item.mountItem.MountItem=1;necesse.inventory.item.trinketItem.TrinketItem=10;necesse.inventory.item.miscItem.AmmoBag=999;```


There are some unlisted commands that are for testing purposes.

## External Mod Integration  
There are three ways to interact with this mod from another mod:
	
### Static Class Fields

When Stack Resizer modifies a stack size, it will first check the class of the item for the presence of a few fields:

```public static final boolean SR_NO_MODIFY = true;```<br/>
Classes that contain this field, regardless of the value of the field, will be ignored by Stack Resizer.

```public static final int SR_MODIFY = 1000;```<br/>
Classes that contain this field will have their stack sizes modified to the amount specified.

The fields must have public visibility and also be static in order to function.


### Using CommandManager

These examples can be seen running in the /stackresize.tests.external command! The script for this example and similar ones are in the 'TestExternalCommand.java' file in the root directory of this repository.

 - Example 1:
	```

        // We want to execute the command on the server client.
	    private void runServerCmd(Client client, String full_command) {
    	    ServerClient myServerClient = client.getLocalServer().getLocalServerClient();
    	    client.commandsManager.runServerCommand(new ParsedCommand(full_command), myServerClient);    	
        }

        // Note the '/' is not present at the beginning of the command.
        private void setItemStackSizeExternallyWithCmdMgr(Client client, String item, int count,boolean is_class, boolean quiet) {
    	    if(client == null) return;
    	    String commandString = String.format("stackresize.stacksize.set %s %d %s %s", item, count, String.valueOf(is_class), String.valueOf(quiet));
    	    runServerCmd(client, commandString);
    	    return;
        }

         this.setItemStackSizeExternallyWithCmdMgr(server.getLocalClient(), "craftingguide", 5, false, false);
        
	```

There are a few ways to do run a command directly through the server client's CommandManager and here is one of them. The client that calls the command needs to have the correct permission level ( OWNER or SERVER) to run commands using runServerCommand. 


### Editing the configuration externally.

This mod stores it's configuration information in the game's default settings path at %APPDATA%/Necesse/cfg/mods (on windows) using the game's built-in LoadData and SaveData classes. 

The path where this config file may differ on a unix server. In the root directory of this repository is a java file called 'SRSettings.java' which contains a standalone class (SRSettings) that you can add to your project to help manipulate the settings that this mod uses. 

If the file is edited before Stack Resizer loads, then the changes will be loaded automatically. If the settings are edited after Stack Resizer loads it's settings (On server start event, after a world is selected and initialized), then you can use the CommandManager to use the in-game reload command to reload the contents of the settings file dynamically.

	
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

## FAQ
- What happens if I change the default stack size while items are stacked to over the new amount?
-- Nothing, until you try to take an item from the stack or split it. Then it'll kick in.

- ModifyISS...? Isn't this Stack Resizer
-- This project evolved from a mod designed to alter another mod (being increased stack size, hence ModifyISS) to being an entire standalone mod itself. The class name will eventually change, as well as some of the ingame messages.

## Bugs
- Contact me on discord @ferrenfx
- Or, submit an issue here.

## History
- 02-28-2025 - v0.3.2 Fixed issue with clients crashing on connect to remote server.
- 02-21-2025 - v0.3.1 Fixed issue where lists would not save.
- 02-19-2025 - v0.3.0 Added static field checks, added bulk set commands.
- 02-15-2025 - v0.2.0 Major Refactor
- 02-14-2025 - v0.1.0 Initial Release

# Future
- Clone settings between servers
- Multi-language locales
- Removal of server specific debug state (There's no point!)