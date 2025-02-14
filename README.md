# Necesse Stack Resizer Mod
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
| `iss.blacklist`                 | View the current blacklist. Anyone may use this command. |
| `iss.blacklist.add <item/class> <is_class> <quiet>` | Add an item or class to the blacklist. |
| `iss.blacklist.remove <item/class> <is_class> <quiet>` | Remove an item or class from the blacklist. |
| `iss.stacksize.set <item/class> <value> <is_class> <quiet>` | Set a custom stack size modifier. |
| `iss.stacksize.default <value>` | Set the default stack size modifier. |
| `iss.stacksize.remove <item/class> <is_class> <quiet>` | Remove a custom stack size modifier. |
| `iss.stacksize.get <item/class> <is_class> <quiet>` | Get the current stack size modifier for an item or class. Anyone may use this command. |
| `iss.stacksize` | List stack size information command. Anyone may use this command. |
| `iss.save` | Save current settings to configuration. |
| `iss.reload` | Reload the current settings from configuration. |
| `iss` | Display mod information. Anyone may use this command. |

There are some unlisted commands that I can't recommend the regular use of.

## External Mod Integration  
Other mods can interact with this mod by sending chat commands as if they were a player.  
To prevent command spam from appearing in the chat log, all commands support a `quiet` parameter.  
- Example:  
  ```
  /iss.blacklist.add myCustomItem <quiet[true/false]>
  /iss.blacklist.add necesse.inventory.item.miscItem.EnchantingScrollItem 1 <quiet[true/false]>
  /iss.stacksize.set myItem 500 <quiet[true/false]>
  /iss.stacksize.set necesse.inventory.item.miscItem.EnchantingScrollItem 1 <quiet[true/false]>
  ```
- When `quiet` is included and set to true, the command will execute without displaying output in the chat, keeping logs clean.  

- Example code:
	```
	((MainGame)necesse.engine.GlobalData.getCurrentState()).getClient().chat.addMessage(null)
	```
	Here, we send a chat message through the current client.
	
## Base Blacklist
Certain item classes are blacklisted by default to help mitigate issues. These classes include those that extend:

- necesse.inventory.item.mountItem.MountItem.class
- necesse.inventory.item.trinketItem.TrinketItem.class
- necesse.inventory.item.miscItem.AmmoPouch.class
- necesse.inventory.item.miscItem.AmmoBag.class
- necesse.inventory.item.miscItem.CoinPouch.class
- necesse.inventory.item.miscItem.Lunchbox.class
- necesse.inventory.item.miscItem.EnchantingScrollItem.class
- necesse.inventory.item.miscItem.GatewayTabletItem.class
- necesse.inventory.item.miscItem.PotionBag.class
- necesse.inventory.item.miscItem.PotionPouch.class

## Installation  
1. Download the mod `.jar` file.  
2. Place it in the `mods` folder of your Necesse installation.  
3. Start the game, and use `/iss` to verify the mod is loaded.  

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
